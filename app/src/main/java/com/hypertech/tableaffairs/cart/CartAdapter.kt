package com.hypertech.tableaffairs.cart

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.hypertech.tableaffairs.GlideApp
import com.hypertech.tableaffairs.R
import com.hypertech.tableaffairs.helper.ITEM_ID
import com.hypertech.tableaffairs.helper.PRODUCTS
import com.hypertech.tableaffairs.products.Product

/*
*Created by Fadsoft on 18, August,2019
*Email: fahadimuhumuza@gmail.com
*Hypertech Solutions, Uganda
*/

class CartAdapter (private var context: Context, private var tempCartList:ArrayList<TempCart>): BaseAdapter(){

    private var db = FirebaseFirestore.getInstance()
    private var storage = FirebaseStorage.getInstance()
    private var image = ""
    private var name = ""
    private var price = 0

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View? {

        var listLayout = convertView

        if (convertView == null){
            val layoutInflater = LayoutInflater.from(context)
            listLayout = layoutInflater.inflate(R.layout.ticket_shopping_cart_item, parent, false)
        }

        val cartItem = tempCartList[position]

        val cartId = cartItem.id
        val itemId = cartItem.itemId
        val quantity = cartItem.qty

        val docRef = db.collection(PRODUCTS).whereEqualTo(ITEM_ID, itemId)
        docRef.get().addOnSuccessListener {
            if (!it.isEmpty) {
                val product = it.documents[0].toObject(Product::class.java)

                image = product?.image.toString()
                name = product?.name.toString()
                price = product?.price!!
            }
        }

        val storageRef = storage.getReferenceFromUrl("gs://safehostel-fb28f.appspot.com/safehostel/universities/$image")

        val carItemImage = listLayout?.findViewById<ImageView>(R.id.product_image)
        val cartItemName = listLayout?.findViewById<TextView>(R.id.product_name)
        val cartItemPrice = listLayout?.findViewById<TextView>(R.id.product_price)
        val cartItemQuantity = listLayout?.findViewById<TextView>(R.id.item_quantity)
        val removeFromCart = listLayout?.findViewById<TextView>(R.id.remove_from_cart)
        val decreaseQuantity = listLayout?.findViewById<TextView>(R.id.decrease)
        val increaseQuantity = listLayout?.findViewById<TextView>(R.id.increase)

        val circularProgressDrawable = CircularProgressDrawable(context)
        circularProgressDrawable.strokeWidth = 5f
        circularProgressDrawable.centerRadius = 30f
        circularProgressDrawable.start()

        GlideApp.with(context)
            .load(storageRef)
            .apply(RequestOptions().override(600, 300))
            .placeholder(circularProgressDrawable)
            .into(carItemImage!!)

        cartItemName!!.text = name
        cartItemPrice!!.text = price.toString()
        cartItemQuantity!!.text = quantity.toString()

        removeFromCart?.setOnClickListener {

        }

        decreaseQuantity?.setOnClickListener {
            val qty = cartItemQuantity.text.toString().toInt() + 1
            cartItemQuantity.text = qty.toString()
        }

        increaseQuantity?.setOnClickListener {
            var qty = 0

            val qtyOld = cartItemQuantity.text.toString().toInt()
            if (qtyOld > 0)
                qty = cartItemQuantity.text.toString().toInt() - 1

            cartItemQuantity.text = qty.toString()
        }

        val finalPrice = cartItemPrice.text.toString().toInt()*cartItemQuantity.text.toString().toInt()
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
