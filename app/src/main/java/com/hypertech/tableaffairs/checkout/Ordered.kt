package com.hypertech.tableaffairs.checkout

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth
import com.hypertech.tableaffairs.R
import com.hypertech.tableaffairs.helper.ORDER_ID
import com.hypertech.tableaffairs.helper.PAYMENT_AMOUNT
import com.hypertech.tableaffairs.helper.loadMainActivity
import kotlinx.android.synthetic.main.activity_ordered.*

class Ordered : AppCompatActivity() {

    private var mAuth: FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ordered)

        mAuth = FirebaseAuth.getInstance()

        val bundle = intent.extras
        val orderId = bundle?.getString(ORDER_ID)
        val totalAmount = bundle?.getDouble(PAYMENT_AMOUNT)
        val userName = mAuth!!.currentUser?.displayName

        tv_greetings.text = "Hello $userName"
        tv_orderId.text = orderId
        tv_totalAmount.text = "ORDER TOTAL AMOUNT : $totalAmount"

        tv_backToHome.setOnClickListener {
            this.loadMainActivity()
        }
    }

    override fun onBackPressed() {

    }
}
