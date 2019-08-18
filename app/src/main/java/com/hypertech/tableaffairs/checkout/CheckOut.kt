package com.hypertech.tableaffairs.checkout

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import com.hypertech.tableaffairs.R

class CheckOut : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_check_out)

        val finalAmount = findViewById<TextView>(R.id.finalAmount)
        val address = findViewById<TextView>(R.id.tv_address)
        val deliveryRadioGroup = findViewById<RadioGroup>(R.id.radioGroupDelivery)
        val paymentRadioGroup = findViewById<RadioGroup>(R.id.radioGroupPayment)
        val buttonConfirm = findViewById<Button>(R.id.button_confirmOrder)

        deliveryRadioGroup.setOnCheckedChangeListener { radioGroup, radioId ->
            when(radioId){
                R.id.delivery_normal -> {
                    Toast.makeText(this, "Normal", Toast.LENGTH_SHORT).show()
                }
                R.id.delivery_express -> {
                    Toast.makeText(this, "Express", Toast.LENGTH_SHORT).show()
                }
            }
        }

        paymentRadioGroup.setOnCheckedChangeListener { radioGroup, radioId ->
            when(radioId){
                R.id.payment_cash -> {
                    Toast.makeText(this, "Cash", Toast.LENGTH_SHORT).show()
                }
                R.id.payment_payPal -> {
                    Toast.makeText(this, "PAY PAL", Toast.LENGTH_SHORT).show()
                }
            }
        }

        buttonConfirm.setOnClickListener {
            val intent = Intent(this, Ordered::class.java)
            startActivity(intent)
        }
    }
}
