package com.hypertech.tableaffairs.helper

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import com.hypertech.tableaffairs.MainActivity
import com.hypertech.tableaffairs.cart.ShoppingCart
import java.util.regex.Pattern

/*
*Created by Fadsoft on 18, July,2019
*Email: fahadimuhumuza@gmail.com
*Hypertech Solutions, Uganda
*/

class Helper {

    companion object {

        fun validatePassword(password: String): Boolean {
            val passwordPattern = "^(?=.*[0-9])(?=.*[A-Z])(?=.*[a-z])(?=\\S+$).{6,}$"
            val pattern = Pattern.compile(passwordPattern)
            val matcher = pattern.matcher(password)
            return matcher.matches()

        }

        fun validateEmail(email: String): Boolean {
            return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
        }


        fun validNumber(number: String): Boolean {
            var valid: Boolean
            valid = number.startsWith("07") || number.startsWith("+2567")
            if (number.startsWith("07") && number.length != 10)
                valid = false
            if (number.startsWith("+2567") && number.length != 13)
                valid = false

            return valid
        }

        fun Context.hideKeyboardFrom(view: View) {
            val imm = this.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
        }

    }

}

fun Context.loadCart(){
    val intent = Intent(this, ShoppingCart::class.java)
    startActivity(intent)
}

fun Context.loadMainActivity(){
    val intent = Intent(this, MainActivity::class.java)
        .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
    startActivity(intent)
    (this as Activity).finish()
}

fun Context.toast(message: String) = Toast.makeText(this, message, Toast.LENGTH_LONG).show()
