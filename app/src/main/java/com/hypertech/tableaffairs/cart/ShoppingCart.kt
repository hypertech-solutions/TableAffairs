package com.hypertech.tableaffairs.cart

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.hypertech.tableaffairs.MainActivity
import com.hypertech.tableaffairs.R
import com.hypertech.tableaffairs.checkout.CheckOut
import com.hypertech.tableaffairs.helper.DBHelper
import kotlinx.android.synthetic.main.activity_shopping_cart.*

class ShoppingCart : AppCompatActivity() {

    private lateinit var dbHelper: DBHelper
    private var cartAdapter:CartAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shopping_cart)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        dbHelper = DBHelper(this)
        val tempCart = dbHelper.retrieveTempCart()

        if (tempCart.isEmpty()){
            tv_empty_cart.visibility = View.VISIBLE
            iv_empty_cart.visibility = View.VISIBLE
            tv_shopping.text = "Shop Here"
        }else{
            var totalPrice = 0.0
            cartAdapter = CartAdapter(this, tempCart)
            listViewCartItems.adapter = cartAdapter

            tv_price.visibility = View.VISIBLE
            btn_checkOut.visibility = View.VISIBLE

            btn_checkOut.setOnClickListener {

                Toast.makeText(this, "You clicked CheckOut", Toast.LENGTH_SHORT).show()

                val intent = Intent(this, CheckOut::class.java)
                startActivity(intent)
            }

        }

        tv_shopping.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if (item.itemId == android.R.id.home)
            finish()

        return super.onOptionsItemSelected(item)
    }
}
