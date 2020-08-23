package com.hypertech.tableaffairs.account

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.hypertech.tableaffairs.R
import com.hypertech.tableaffairs.helper.loadMainActivity
import com.hypertech.tableaffairs.helper.toast
import kotlinx.android.synthetic.main.activity_login.*


class Login : AppCompatActivity() {

    private var mAuth: FirebaseAuth? = null
    private var progressBar:ProgressBar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        mAuth = FirebaseAuth.getInstance()
        progressBar = login_progressBar

        login_button.setOnClickListener {
            loginUser()
        }

        login_signUp.setOnClickListener {
            val intent = Intent(this, Register::class.java)
            startActivity(intent)
        }

        login_forgot_password.setOnClickListener {
            val intent = Intent(this, ResetPassword::class.java)
            startActivity(intent)
        }
    }

    private fun loginUser() {
        val email = login_email.text.toString().trim()
        val password = login_password.text.toString().trim()

        if (email.isNotEmpty() && password.isNotEmpty()){
            progressBar!!.visibility = View.VISIBLE
            login_button.isEnabled = false
            mAuth?.signInWithEmailAndPassword(email, password)?.addOnCompleteListener {
                progressBar!!.visibility = View.GONE
                login_button.isEnabled = true
                if (it.isSuccessful){
                    this.loadMainActivity()
                }else{
                    this.toast(it.exception?.message!!)
                }
            }?.addOnFailureListener {
                this.toast(it.message!!)
            }
        }else{

            if (email.isEmpty()) login_email.error = "Email is empty" else login_email.error = null
            if (password.isEmpty()) login_password.error = "Password is empty" else login_password.error = null
        }
    }



}
