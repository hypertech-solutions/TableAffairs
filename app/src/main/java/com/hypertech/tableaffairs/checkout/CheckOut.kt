package com.hypertech.tableaffairs.checkout

import android.app.Activity
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
import com.paypal.android.sdk.payments.PayPalConfiguration
import com.paypal.android.sdk.payments.PayPalService
import com.paypal.android.sdk.payments.PayPalPayment
import com.paypal.android.sdk.payments.PaymentActivity
import java.math.BigDecimal

import android.util.Log
import com.paypal.android.sdk.payments.PaymentConfirmation
import java.lang.Exception

class CheckOut : AppCompatActivity() {

    private var mAuth: FirebaseAuth? = null
    private var db = FirebaseFirestore.getInstance()
    private var dbHelper: DBHelper? = null
    private var orderId: String? = null
    private var totalAmount:Double = 0.0
    private var address:String? = null
    private var deliveryMethod = ""
    private var paymentMethod = ""
    private var userId: String? = null
    private val productList = ArrayList<HashMap<String, Int>>()

    //Paypal Configuration Object
    private val paypalConfig = PayPalConfiguration()
        .environment(PayPalConfiguration.ENVIRONMENT_SANDBOX)
        .clientId(PayPalConfig.PAYPAL_CLIENT_ID)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_check_out)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        mAuth = FirebaseAuth.getInstance()
        dbHelper = DBHelper(this)

        val finalAmount = findViewById<TextView>(R.id.finalAmount)
        val tvAddress = findViewById<TextView>(R.id.tv_address)
        val deliveryRadioGroup = findViewById<RadioGroup>(R.id.radioGroupDelivery)
        val paymentRadioGroup = findViewById<RadioGroup>(R.id.radioGroupPayment)
        val buttonConfirm = findViewById<Button>(R.id.button_confirmOrder)

        startPayPalService()

        address = tvAddress.text.toString()

        userId = mAuth!!.currentUser?.uid
        val tempCart = dbHelper!!.retrieveTempCart()

        if (tempCart.isNotEmpty()) {

            val product = HashMap<String, Int>()
            for (i in 0 until tempCart.size) {

                product[tempCart[i].itemId] = tempCart[i].qty
                productList.add(product)

                totalAmount += tempCart[i].qty * tempCart[i].price
            }

            finalAmount.text = "TOTAL AMOUNT : $totalAmount"

            deliveryRadioGroup.setOnCheckedChangeListener { _, radioId ->
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

            paymentRadioGroup.setOnCheckedChangeListener { _, radioId ->
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
                    orderId = "$userId@${Timestamp.now()}"

                    if (paymentMethod == CASH) {

                        if (createOrderInDatabase())
                            loadOrdered(orderId.toString())

                    } else if (paymentMethod == PAYPAL) {
                        if (createOrderInDatabase())
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

    override fun onDestroy() {
        stopService(Intent(this, PayPalService::class.java))
        super.onDestroy()
    }

    private fun startPayPalService() {
        // Starting PayPal service
        val intent = Intent(this, PayPalService::class.java)
        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, paypalConfig)
        startService(intent)
    }

    private fun loadPayPalPayment() {
        val paymentAmount = totalAmount

        //Creating a paypalpayment
        val payment = PayPalPayment(
            BigDecimal.valueOf(paymentAmount), PayPalConfig.DEFAULT_CURRENCY, BUSINESS_NAME,
            PayPalPayment.PAYMENT_INTENT_SALE
        )

        //Creating Paypal Payment activity intent
        val intent = Intent(this, PaymentActivity::class.java)
        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, paypalConfig)

        //putting the paypal configuration to the intent
        intent.putExtra(PaymentActivity.EXTRA_PAYMENT, payment)

        //Starting the intent activity for result
        //the request code will be used on the method onActivityResult
        startActivityForResult(intent, PAYPAL_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        //If the result is from paypal
        if (requestCode == PAYPAL_REQUEST_CODE) {

            //If the result is OK i.e. user has not canceled the payment
            if (resultCode == Activity.RESULT_OK) {
                //Getting the payment confirmation
                val confirm: PaymentConfirmation? = data?.getParcelableExtra(PaymentActivity.EXTRA_RESULT_CONFIRMATION)

                //if confirmation is not null
                if (confirm != null) {
                    val docRef = db.collection(ORDERS).document(orderId.toString())
                    docRef.update(PAYMENT_STATUS, PAID)
                        .addOnSuccessListener {
                            this.toast("Payment process completed successfully")
                        }
                        .addOnFailureListener {
                            this.toast("Payment process completed but not validated in our database : $it")
                        }

                    try {
                        //Getting the payment details
                        val paymentDetails = confirm.toJSONObject().toString(4)
                        Log.i("PayPal Payment", paymentDetails)

                        //Starting a new activity for the payment details and also putting the payment details with intent

//                        val paymentId = confirm.toJSONObject().getJSONObject("response").getString("id")
//                        verifyPaymentOnServer(paymentId , confirm)

                        startActivity(Intent(this, ConfirmationActivity::class.java)
                            .putExtra(ORDER_ID, orderId)
                            .putExtra(PAYMENT_DETAILS, paymentDetails)
                            .putExtra(PAYMENT_AMOUNT, totalAmount))

                    }catch (ex:Exception){
                        Log.e(PAYPAL_PAYMENT, "An extremely unlikely failure occurred: ", ex)
                    }
                }

            }else if (resultCode == Activity.RESULT_CANCELED){
                Log.i(PAYPAL_PAYMENT, "The user canceled.")
            }else if (resultCode == PaymentActivity.RESULT_EXTRAS_INVALID) {
                Log.i(PAYPAL_PAYMENT, "An invalid Payment or PayPalConfiguration was submitted. Please see the docs.")
            }
        }
    }

    //Verifying the mobile payment on the server to avoid fraudulent payment
//
//    private fun verifyPaymentOnServer(paymentId: String, confirm: PaymentConfirmation) {
//        verify_progressBar.visibility = View.VISIBLE
//
//        try {
//
//            val amount = confirm.payment.toJSONObject().getString("amount")
//            val currency = confirm.payment.toJSONObject().getString("currency_code")
//            val userID = userId
//
//            //TODO: Complete this method
//
//        }catch (e:JSONException){
//            e.printStackTrace()
//        }
//        verify_progressBar.visibility = View.GONE
//
//    }

    private fun createOrderInDatabase():Boolean {
        var ordered = false
        val order = Order(orderId, userId, productList, totalAmount, paymentMethod, deliveryMethod, address, PENDING, Timestamp.now())
        db.collection(ORDERS).document(orderId.toString()).set(order)
            .addOnSuccessListener {
                dbHelper?.deleteTempCart()
                this.toast("Order placed!")
                ordered = true
            }
            .addOnFailureListener { result ->
                this.toast("Can't place your order: $result")
                ordered = false
            }
        return ordered
    }

    private fun loadOrdered(orderId: String) {
        val intent = Intent(this, Ordered::class.java)
        intent.putExtra(ORDER_ID, orderId)
        intent.putExtra(PAYMENT_AMOUNT, totalAmount)
        startActivity(intent)
    }
}
