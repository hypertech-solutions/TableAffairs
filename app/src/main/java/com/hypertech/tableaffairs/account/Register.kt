package com.hypertech.tableaffairs.account

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.hypertech.tableaffairs.*
import com.hypertech.tableaffairs.helper.Helper
import com.hypertech.tableaffairs.helper.USERS
import com.hypertech.tableaffairs.helper.toast
import kotlinx.android.synthetic.main.activity_register.*

class Register : AppCompatActivity() {

    private var mAuth: FirebaseAuth? = null
    private var db = FirebaseFirestore.getInstance()
    private var progressBar:ProgressBar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        mAuth = FirebaseAuth.getInstance()
        progressBar = sign_up_progressBar

        sign_up_button.setOnClickListener {

            val fullName = name.text.toString().trim()
            val phoneNumber = phone_number.text.toString().trim()
            val email = email_address.text.toString().trim()
            val password = register_password.text.toString().trim()

            if (validateInputs())
                registerUser(fullName, phoneNumber, email, password)
        }

        signUpLogin.setOnClickListener {
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun registerUser(fullName: String, phoneNumber: String, email: String, password: String) {
        progressBar!!.visibility = View.VISIBLE
        sign_up_button.isEnabled = false

        mAuth!!.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    val currentUser = mAuth!!.currentUser

                    val updates = UserProfileChangeRequest.Builder()
                        .setDisplayName(fullName).build()
                    currentUser?.updateProfile(updates)

                    val userId = currentUser?.uid

                    val user = User(userId, fullName, phoneNumber, email, Timestamp.now())

                    db.collection(USERS).document(userId!!).set(user)
                        .addOnSuccessListener {
                            this.toast("Account created successfully")
                            val intent = Intent(this, MainActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
                        .addOnFailureListener { result ->
                            this.toast(result.message!!)
                            progressBar!!.visibility = View.INVISIBLE
                            sign_up_button.isEnabled = true
                        }

                } else {

                    this.toast(it.exception?.message!!)
                    progressBar!!.visibility = View.INVISIBLE
                    sign_up_button.isEnabled = true
                }
            }
    }

    private fun validateInputs(): Boolean {

        val fullName = name.text.toString()
        val phoneNumber = phone_number.text.toString()
        val email = email_address.text.toString()
        val password = register_password.text.toString()
        val confirmPassword = confirm_password.text.toString()

        var valid = true
        if (fullName.isEmpty() && fullName.length < 3) {
            name.error = "Name should be at least 3 characters"
            valid = false
        } else
            name.error = null

        if (phoneNumber.isEmpty()) {
            phone_number.error = "Required"
            valid = false
        } else if (!Helper.validNumber(phoneNumber)) {
            phone_number.error = "Phone number not valid"
            valid = false
        } else
            phone_number.error = null

        if (email.isEmpty()) {
            email_address.error = "Required"
            valid = false
        } else if (!Helper.validateEmail(email)) {
            email_address.error = "Email address not valid"
            valid = false
        } else
            email_address.error = null

        if (password.isEmpty()) {
            register_password.error = "Required"
            valid = false
        } else if (!Helper.validatePassword(password)) {
            register_password.error = "Must be alphanumeric with at least 6 characters in upper and lower cases"
            valid = false
        } else
            register_password.error = null

        when {
            confirmPassword.isEmpty() -> {
                confirm_password.error = "Please confirm password."
                valid = false
            }
            password != confirmPassword -> {
                this.toast("Passwords do not match")
                valid = false
            }
            else -> confirm_password.error = null
        }
        return valid
    }
}
