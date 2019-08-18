package com.hypertech.tableaffairs.account

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.google.firebase.auth.FirebaseAuth
import com.hypertech.tableaffairs.helper.Helper
import com.hypertech.tableaffairs.R
import com.hypertech.tableaffairs.helper.toast
import kotlinx.android.synthetic.main.activity_reset_password.*

class ResetPassword : AppCompatActivity() {

    private var mAuth: FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reset_password)

        mAuth = FirebaseAuth.getInstance()

        button_reset_password.setOnClickListener {
            sendPasswordResetLink()
        }
    }

    private fun sendPasswordResetLink() {
        val email = reset_text_email.text.toString()

        if (email.isEmpty()) {
            reset_text_email.error = "Email Required"
            reset_text_email.requestFocus()
            return
        }

        if (!Helper.validateEmail(email)) {
            reset_text_email.error = "Valid Email Required"
            reset_text_email.requestFocus()
            return

        }

        progressbar.visibility = View.VISIBLE
        button_reset_password.isEnabled = false
        mAuth!!.sendPasswordResetEmail(email).addOnCompleteListener {
            progressbar.visibility = View.GONE
            button_reset_password.isEnabled = true

            if (it.isSuccessful) {
                linkSent.visibility = View.VISIBLE
            } else {
                this.toast(it.exception?.message!!)
            }
        }
    }
}
