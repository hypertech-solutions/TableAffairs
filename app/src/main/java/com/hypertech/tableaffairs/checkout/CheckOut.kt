package com.hypertech.tableaffairs.checkout

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isEmpty
import androidx.core.view.isNotEmpty
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.hypertech.tableaffairs.R
import com.hypertech.tableaffairs.helper.*

class CheckOut : AppCompatActivity() {

    private var mAuth: FirebaseAuth? = null
    private var db = FirebaseFirestore.getInstance()
    private var dbHelper:DBHelper? = null
    private var totalAmount = 0.0
    private var deliveryMethod = ""
    private var paymentMethod = ""
    private var userId:String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_check_out)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        mAuth = FirebaseAuth.getInstance()
        dbHelper = DBHelper(this)

        val finalAmount = findViewById<TextView>(R.id.finalAmount)
        val address = findViewById<TextView>(R.id.tv_address)
        val deliveryRadioGroup = findViewById<RadioGroup>(R.id.radioGroupDelivery)
        val paymentRadioGroup = findViewById<RadioGroup>(R.id.radioGroupPayment)
        val buttonConfirm = findViewById<Button>(R.id.button_confirmOrder)

        userId = mAuth!!.currentUser?.uid
        val tempCart = dbHelper!!.retrieveTempCart()

        if (tempCart.isNotEmpty()) {

            val product = HashMap<String,Int>()
            val productIdList = ArrayList<HashMap<String, Int>>()

            for (i in 0 until tempCart.size) {
                product[tempCart[i].itemId] = tempCart[i].qty
                productIdList.add(product)
                totalAmount += tempCart[i].qty * tempCart[i].price
            }

            finalAmount.text = "TOTAL AMOUNT : $totalAmount"

            deliveryRadioGroup.setOnCheckedChangeListener { radioGroup, radioId ->
                when (radioId) {
                    R.id.delivery_normal -> {
                        deliveryMethod = NORMAL
                        totalAmount += 2
                        finalAmount.text = "TOTAL AMOUNT : $totalAmount"
                    }
                    R.id.delivery_express -> {
                        deliveryMethod = EXPRESS
                        totalAmount += 2
                        finalAmount.text = "TOTAL AMOUNT : $totalAmount"
                    }
                }
            }

            paymentRadioGroup.setOnCheckedChangeListener { radioGroup, radioId ->
                when (radioId) {
                    R.id.payment_cash -> {
                        paymentMethod = CASH
                    }
                    R.id.payment_payPal -> {
                        paymentMethod = PAYPAL
                    }
                }
            }

            buttonConfirm.setOnClickListener {

                if (deliveryRadioGroup.isNotEmpty() && paymentRadioGroup.isNotEmpty()) {
                    val orderId = "$userId@${Timestamp.now()}"

                    if (paymentMethod == CASH) {
                        createOrderInDatabase(orderId, productIdList)
                    } else if (paymentMethod == PAYPAL) {
                        loadPayPalPayment()
                    }

                } else {
                    if (deliveryRadioGroup.isEmpty()) {
                        deliveryRadioGroup.requestFocus()
                        Toast.makeText(this, "Please choose your preferred delivery method", Toast.LENGTH_LONG).show()
                    } else if (paymentRadioGroup.isEmpty()) {
                        paymentRadioGroup.requestFocus()
                        Toast.makeText(this, "Please choose your preferred payment method", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if (item.itemId == android.R.id.home)
            finish()

        return super.onOptionsItemSelected(item)
    }

    private fun createOrderInDatabase(orderId: String, productIdList: ArrayList<HashMap<String, Int>>) {
        val order = Order(orderId, userId, productIdList, Timestamp.now())
        db.collection(ORDERS).document(orderId).set(order)
            .addOnSuccessListener {
                dbHelper?.deleteTempCart()
                this.toast("Order placed!")
                loadOrdered(orderId)
            }
            .addOnFailureListener { result ->
                this.toast(result.message!!)
            }
    }

    private fun loadPayPalPayment() {

    }

    private fun loadOrdered(orderId:String) {
        val intent = Intent(this, Ordered::class.java)
        intent.putExtra(ORDER_ID, orderId)
        startActivity(intent)
    }
}
