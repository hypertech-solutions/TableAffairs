package com.hypertech.tableaffairs.products

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.storage.FirebaseStorage
import com.hypertech.tableaffairs.GlideApp
import com.hypertech.tableaffairs.R
import com.hypertech.tableaffairs.cart.TempCart
import com.hypertech.tableaffairs.helper.DBHelper
import com.hypertech.tableaffairs.helper.loadCart
import com.hypertech.tableaffairs.helper.toast

/*
*Created by Fadsoft on 16, August,2019
*Email: fahadimuhumuza@gmail.com
*Hypertech Solutions, Uganda
*/

class ProductAdapter(private var context: Context, private var productList: ArrayList<Product>) : BaseAdapter() {

    private var storage = FirebaseStorage.getInstance()
    private val dbHelper = DBHelper(context)

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View? {

        var listLayout = convertView

        if (convertView == null) {
            val layoutInflater = LayoutInflater.from(context)
            listLayout = layoutInflater.inflate(R.layout.ticket_product, parent, false)
        }

        val product = productList[position]

//        val brand = product.brand
        val id = product.id
        val image = product.image
        val name = product.name
        val desc = product.desc
        val price = product.price
        val stock = product.stock

        val storageRef = storage.getReferenceFromUrl("gs://tableaffairs-994b4.appspot.com/tableaffairs/products/$image")


        val productImage = listLayout?.findViewById<ImageView>(R.id.productImage)
        val productName = listLayout?.findViewById<TextView>(R.id.productName)
        val productDesc = listLayout?.findViewById<TextView>(R.id.productDescription)
        val productPrice = listLayout?.findViewById<TextView>(R.id.productPrice)
        val productStock = listLayout?.findViewById<TextView>(R.id.productStock)
        val btnAddToCart = listLayout?.findViewById<Button>(R.id.btnAddToCart)

        val circularProgressDrawable = CircularProgressDrawable(context)
        circularProgressDrawable.strokeWidth = 5f
        circularProgressDrawable.centerRadius = 30f
        circularProgressDrawable.start()

        GlideApp.with(context)
            .load(storageRef)
            .apply(RequestOptions().override(600, 300))
            .placeholder(circularProgressDrawable)
            .into(productImage!!)

        productName!!.text = name
        productDesc!!.text = desc
        productPrice!!.text = listLayout?.context!!.getString(R.string.setPrice, price)
        productStock!!.text = listLayout.context!!.getString(R.string.set_inStock, stock)

        if (stock > 0) {
            btnAddToCart?.setOnClickListener {

                val check = dbHelper.checkForItemExistence(id!!)
                if (check){
                    context.toast("Item already added to cart!")
                    context.loadCart()
                }else{
                    val tempCart = TempCart(null, id, image!!, name!!, price, 1, stock)
                    val result = dbHelper.addItemToTempCart(tempCart)
                    if (result > 1)
                        context.toast("Item added to cart!")
                }
            }
        }else{
            btnAddToCart!!.setBackgroundColor(listLayout.context.resources.getColor(R.color.gray))
            btnAddToCart.isEnabled = false
        }


        return listLayout
    }

    override fun getItem(position: Int): Any {
        return productList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount(): Int {
        return productList.size
    }

}