package com.hypertech.tableaffairs.cart

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.storage.FirebaseStorage
import com.hypertech.tableaffairs.GlideApp
import com.hypertech.tableaffairs.R
import com.hypertech.tableaffairs.checkout.CheckOut
import com.hypertech.tableaffairs.helper.DBHelper
import com.hypertech.tableaffairs.helper.loadMainActivity
import com.hypertech.tableaffairs.helper.toast

import kotlinx.android.synthetic.main.activity_shopping_cart.*

class ShoppingCart : AppCompatActivity() {

    private var tempCart = ArrayList<TempCart>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shopping_cart)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        loadCartData()
    }

    override fun onResume() {
        super.onResume()
        loadCartData()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if (item.itemId == android.R.id.home)
            finish()

        return super.onOptionsItemSelected(item)
    }

    private fun loadCartData(){

        val dbHelper = DBHelper(this)

        tempCart.clear()
        tempCart = dbHelper.retrieveTempCart()

        if (tempCart.isEmpty()){
            tv_empty_cart.visibility = View.VISIBLE
            iv_empty_cart.visibility = View.VISIBLE
            layoutBottom.visibility = View.GONE

            iv_empty_cart.setOnClickListener {
                this.loadMainActivity()
            }

        }else{

            layoutBottom.visibility = View.VISIBLE

            val cartAdapter = CartAdapter(this, tempCart)

            listViewCartItems.adapter = cartAdapter

//            var totalAmount = 0.0
//            for (i in 0 until tempCart.size){
//                totalAmount += tempCart[i].qty * tempCart[i].price
//            }
//            tv_price.text = "TOTAL : $totalAmount"

            btn_checkOut.setOnClickListener {

                val intent = Intent(this, CheckOut::class.java)
                startActivity(intent)
            }

            tv_shopping.setOnClickListener {
                this.loadMainActivity()
            }

        }
    }

    inner class CartAdapter (private var context: Context, private var tempCartList:ArrayList<TempCart>): BaseAdapter(){

        private var storage = FirebaseStorage.getInstance()
        private var dbHelper = DBHelper(context)

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View? {

            var listLayout = convertView

            if (convertView == null){
                val layoutInflater = LayoutInflater.from(context)
                listLayout = layoutInflater.inflate(R.layout.ticket_shopping_cart_item, parent, false)
            }

            val cartItem = tempCartList[position]

//            val cartId = cartItem.id
            val itemId = cartItem.itemId
            val itemImage = cartItem.itemImage
            val itemName = cartItem.itemName
            val price = cartItem.price
            val quantity = cartItem.qty
            val stock = cartItem.stock

            val storageRef = storage.getReferenceFromUrl("gs://tableaffairs-994b4.appspot.com/tableaffairs/products/$itemImage")

            val carItemImage = listLayout?.findViewById<ImageView>(R.id.product_image)
            val cartItemName = listLayout?.findViewById<TextView>(R.id.product_name)
            val cartItemPrice = listLayout?.findViewById<TextView>(R.id.product_price)
            val cartItemQuantity = listLayout?.findViewById<TextView>(R.id.item_quantity)
            val removeFromCart = listLayout?.findViewById<ImageView>(R.id.remove_from_cart)
            val decreaseQuantity = listLayout?.findViewById<ImageView>(R.id.decrease)
            val increaseQuantity = listLayout?.findViewById<ImageView>(R.id.increase)
            val netPrice = listLayout?.findViewById<TextView>(R.id.netPrice)

            val circularProgressDrawable = CircularProgressDrawable(context)
            circularProgressDrawable.strokeWidth = 5f
            circularProgressDrawable.centerRadius = 30f
            circularProgressDrawable.start()

            GlideApp.with(context)
                .load(storageRef)
                .apply(RequestOptions().override(600, 300))
                .placeholder(circularProgressDrawable)
                .into(carItemImage!!)

            cartItemName!!.text = itemName
            cartItemPrice!!.text = price.toString()
            cartItemQuantity!!.text = quantity.toString()


            removeFromCart?.setOnClickListener {
                dbHelper.deleteItemTempCart(cartItem)
                loadCartData()
                context.toast("Product removed")

            }

            var netAmount = price *cartItemQuantity.text.toString().toInt()
            netPrice!!.text = listLayout?.context!!.getString(R.string.set_netPrice, netAmount)

            increaseQuantity?.setOnClickListener {
                if (cartItemQuantity.text.toString().toInt() < stock){
                    val qty = cartItemQuantity.text.toString().toInt() + 1
                    val result = dbHelper.updateTempCart(itemId, qty)

                    if (result > 0){
                        cartItemQuantity.text = qty.toString()
                        netAmount = price *cartItemQuantity.text.toString().toInt()
                        netPrice.text = listLayout.context!!.getString(R.string.set_netPrice, netAmount)
//                        notifyDataSetChanged()
                    }

                }else{
                    context.toast("Can't exceed what is in stock!!")
                }

            }

            decreaseQuantity?.setOnClickListener {
                if (cartItemQuantity.text.toString().toInt() > 0){
                    val qty = cartItemQuantity.text.toString().toInt() - 1
                    val result = dbHelper.updateTempCart(itemId, qty)

                    if (result > 0){
                        cartItemQuantity.text = qty.toString()
                        netAmount = price *cartItemQuantity.text.toString().toInt()
                        netPrice.text = listLayout.context!!.getString(R.string.set_netPrice, netAmount)
//                        notifyDataSetChanged()
                    }
                }

            }

            return listLayout
        }

        override fun getItem(position: Int): Any {
            return tempCartList[position]
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getCount(): Int {
            return tempCartList.size
        }

    }


}
