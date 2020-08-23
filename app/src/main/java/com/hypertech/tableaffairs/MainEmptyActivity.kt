package com.hypertech.tableaffairs

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import com.google.firebase.auth.FirebaseAuth
import com.hypertech.tableaffairs.account.Login

class MainEmptyActivity : AppCompatActivity() {

    private var mAuth: FirebaseAuth? = null
    private var handler: Handler? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_empty)

        mAuth = FirebaseAuth.getInstance()

        handler = Handler()
        handler!!.postDelayed({

            startApp()

        }, 4000)

    }

    private fun startApp(){

        val currentUser = mAuth!!.currentUser
        val intent: Intent

        intent = if (currentUser != null){
            Intent(this, MainActivity::class.java)
        }else{
            Intent(this, Login::class.java)
        }

        startActivity(intent)
        finish()
    }
}
