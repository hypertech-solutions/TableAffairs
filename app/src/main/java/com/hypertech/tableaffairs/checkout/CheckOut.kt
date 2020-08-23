package com.hypertech.tableaffairs.checkout

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
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
import android.view.View
import com.paypal.android.sdk.payments.PaymentConfirmation
import kotlinx.android.synthetic.main.activity_check_out.*
import java.lang.Exception
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class CheckOut : AppCompatActivity() {

    private var mAuth: FirebaseAuth? = null
    private var db = FirebaseFirestore.getInstance()
    private var dbHelper: DBHelper? = null
    private var orderId: String? = null
    private var finalAmount:Double = 0.0
    private var deliveryTax:Double = 0.0
    private var address:String = ""
    private var deliveryMethod:String = ""
    private var paymentMethod:String = ""
    private var userId: String? = null
    private val productList = ArrayList<HashMap<String, Any>>()

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

        val tvFinalAmount = tv_finalAmount
        val tvAddress = tv_address
        val deliveryRadioGroup = radioGroupDelivery
        val paymentRadioGroup = radioGroupPayment
        val buttonConfirm = button_confirmOrder

        startPayPalService()

        userId = mAuth!!.currentUser?.uid
        val tempCart = dbHelper!!.retrieveTempCart()

        if (tempCart.isNotEmpty()) {

            var totalAmount = 0.0
            for (i in 0 until tempCart.size) {
                val product = HashMap<String, Any>()
                product["productId"] = tempCart[i].itemId
                product["quantity"] = tempCart[i].qty
                totalAmount += tempCart[i].qty * tempCart[i].price

                productList.add(product)
            }


            tvFinalAmount.text = getString(R.string.setTotal, totalAmount)

            deliveryRadioGroup.setOnCheckedChangeListener { _, radioId ->
                finalAmount = 0.0
                when (radioId) {
                    R.id.delivery_normal -> {
                        deliveryMethod = NORMAL
                        deliveryTax = 2.0
                    }
                    R.id.delivery_express -> {
                        deliveryMethod = EXPRESS
                        deliveryTax = 3.0
                    }
                }

                finalAmount = totalAmount + deliveryTax
                tvFinalAmount.text = getString(R.string.setTotal, finalAmount)
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

                address = tvAddress.text.toString()

                if (address.isEmpty()){
                    tvAddress.error = "Please enter your address"
                    return@setOnClickListener
                }

                if (address.length < 5){
                    tvAddress.error = "Enter valid address"
                    return@setOnClickListener
                }

                tvAddress.error = null

                if (deliveryMethod.isEmpty()) {
                    deliveryRadioGroup.requestFocus()
                    this.toast("Please choose your preferred delivery method")
                    return@setOnClickListener
                }

                if (paymentMethod.isEmpty()) {
                    paymentRadioGroup.requestFocus()
                    this.toast("Please choose your preferred payment method")
                    return@setOnClickListener
                }

                if (address.isNotEmpty() && deliveryMethod.isNotEmpty() && paymentMethod.isNotEmpty()) {

                    button_confirmOrder.isEnabled = false
                    verify_progressBar.visibility = View.VISIBLE

                    val dateNow = Calendar.getInstance().time
                    val formattedDate = Helper.formatDate(dateNow)
                    orderId = "$userId@$formattedDate"

                    if (paymentMethod == CASH) createCashOrderInDatabase() else if (paymentMethod == PAYPAL) createPayPalOrderInDatabase()

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

    private fun createCashOrderInDatabase(){
        val order = Order(orderId, userId, productList, finalAmount, paymentMethod, deliveryMethod, address, PENDING, PENDING, Timestamp.now())
        db.collection(ORDERS).document(orderId.toString()).set(order)
            .addOnSuccessListener {
                this.toast("Order placed!")
                loadOrdered(orderId.toString())
            }
            .addOnFailureListener { result ->
                verify_progressBar.visibility = View.GONE
                button_confirmOrder.isEnabled = true
                this.toast("Can't place your order: ${result.message}")

            }
    }

    private fun createPayPalOrderInDatabase(){
        val order = Order(orderId, userId, productList, finalAmount, paymentMethod, deliveryMethod, address, PENDING, PENDING, Timestamp.now())
        db.collection(ORDERS).document(orderId.toString()).set(order)
            .addOnSuccessListener {
                this.toast("Order placed!")
                loadPayPalPayment()
            }
            .addOnFailureListener { result ->
                verify_progressBar.visibility = View.GONE
                button_confirmOrder.isEnabled = true
                this.toast("Can't place your order: ${result.message}")
            }
    }

    private fun loadPayPalPayment() {
        val paymentAmount = finalAmount

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
                            this.toast("Payment process completed but not validated in our database : ${it.message}")
                        }

                    try {
                        //Getting the payment details
                        val paymentDetails = confirm.toJSONObject().toString(4)
                        Log.i("PayPal Payment", paymentDetails)

                        //Starting a new activity for the payment details and also putting the payment details with intent

//                        val paymentId = confirm.toJSONObject().getJSONObject("response").getString("id")
//                        verifyPaymentOnServer(paymentId , confirm)
                        val intent = Intent(this, ConfirmationActivity::class.java)
                        intent.putExtra(ORDER_ID, orderId)
                        intent.putExtra(PAYMENT_DETAILS, paymentDetails)
                        intent.putExtra(PAYMENT_AMOUNT, finalAmount)
                        dbHelper?.deleteTempCart()
                        startActivity(intent)


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

        verify_progressBar.visibility = View.GONE
        button_confirmOrder.isEnabled = true
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

    private fun loadOrdered(orderId: String) {
        val intent = Intent(this, Ordered::class.java)
        intent.putExtra(ORDER_ID, orderId)
        intent.putExtra(PAYMENT_AMOUNT, finalAmount)
        dbHelper?.deleteTempCart()
        startActivity(intent)
    }
}
