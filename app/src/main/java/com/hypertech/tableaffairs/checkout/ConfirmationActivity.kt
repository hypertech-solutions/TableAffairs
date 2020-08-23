package com.hypertech.tableaffairs.checkout

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.hypertech.tableaffairs.R
import org.json.JSONException
import org.json.JSONObject
import android.widget.TextView
import android.view.View
import com.google.firebase.auth.FirebaseAuth
import com.hypertech.tableaffairs.helper.*
import kotlinx.android.synthetic.main.activity_confirmation.*


class ConfirmationActivity : AppCompatActivity() {

    private var mAuth: FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_confirmation)

        mAuth = FirebaseAuth.getInstance()

        //Getting Intent
        val intent = intent

        try {
            val orderId = intent.getStringExtra(ORDER_ID)
            val jsonDetails = JSONObject(intent.getStringExtra(PAYMENT_DETAILS))
            val paymentAmount = intent.getDoubleExtra(PAYMENT_AMOUNT, 0.00)

            //Displaying payment details
            showDetails(orderId, jsonDetails.getJSONObject("response"), paymentAmount)
        } catch (e: JSONException) {
            this.toast(e.toString())
        }

        tv_confirmBackToHome.setOnClickListener {
            this.loadMainActivity()
        }
    }

    override fun onBackPressed() {

    }

    @Throws(JSONException::class)
    private fun showDetails(orderId: String?,jsonDetails: JSONObject, paymentAmount: Double?) {
        //Views
        val textViewId = findViewById<View>(R.id.paymentId) as TextView
        val textViewStatus = findViewById<View>(R.id.paymentStatus) as TextView
        val textViewAmount = findViewById<View>(R.id.paymentAmount) as TextView

        val userName = mAuth!!.currentUser?.displayName

        tv_confirmGreetings.text = getString(R.string.set_greetings, userName)
        tv_confirmOrderId.text = orderId
        //Showing the details from json object
        textViewAmount.text = getString(R.string.set_payment, paymentAmount)
        textViewId.text = jsonDetails.getString("id")
        textViewStatus.text = jsonDetails.getString("state")

    }
}
