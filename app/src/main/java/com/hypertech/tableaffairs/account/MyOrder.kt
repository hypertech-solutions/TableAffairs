package com.hypertech.tableaffairs.account

import android.content.Context
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
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.hypertech.tableaffairs.GlideApp
import com.hypertech.tableaffairs.R
import com.hypertech.tableaffairs.helper.MY_ORDER_MODEL
import com.hypertech.tableaffairs.helper.PRODUCTS
import com.hypertech.tableaffairs.products.Product
import kotlinx.android.synthetic.main.activity_my_order.*
import java.lang.Exception

class MyOrder : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_order)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        val order = intent.getParcelableExtra<MyOrderModel>(MY_ORDER_MODEL)

        order_OrderId.text = order.orderId
        order_OrderDate.text = order.orderedAt.toString()
        order_OrderAddress.text = order.address
        order_OrderPayment.text = order.paymentAmount.toString()
        order_OrderPaymentMethod.text = order.paymentMethod
        order_OrderDeliveryMethod.text = order.deliveryMethod
        order_PaymentStatus.text = order.paymentStatus
        order_DeliveryStatus.text = order.deliveryStatus

        val productList = order.products
        val adapter = productList?.let { OrderAdapter(this, it) }
        listViewOrderProducts.adapter = adapter

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if (item.itemId == android.R.id.home)
            finish()

        return super.onOptionsItemSelected(item)
    }

    inner class OrderAdapter(private var context: Context, private var productList: ArrayList<HashMap<String, Any>>) :
        BaseAdapter() {

        private var db = FirebaseFirestore.getInstance()
        private var storage = FirebaseStorage.getInstance()

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View? {
            var listLayout = convertView

            if (convertView == null) {
                val layoutInflater = LayoutInflater.from(context)
                listLayout = layoutInflater.inflate(R.layout.ticket_order_product, parent, false)

                val orderProductImage = listLayout?.findViewById<ImageView>(R.id.order_productImage)
                val orderProductName = listLayout?.findViewById<TextView>(R.id.order_productName)
                val orderProductPrice = listLayout?.findViewById<TextView>(R.id.order_productPrice)
                val orderProductQty = listLayout?.findViewById<TextView>(R.id.order_productQty)
                val orderProductDesc = listLayout?.findViewById<TextView>(R.id.order_productDesc)

                val order = productList[position]

                val productId = order["productId"]
                val productQty = order["quantity"]

                try {
                    val docRef = db.collection(PRODUCTS).whereEqualTo("id", productId)
                    docRef.get().addOnSuccessListener {
                        if (!it.isEmpty) {
                            val product = it.documents[0].toObject(Product::class.java)
                            if (product != null) {
                                val itemImage = product.image

                                val storageRef =
                                    storage.getReferenceFromUrl("gs://tableaffairs-994b4.appspot.com/tableaffairs/products/$itemImage")

                                val circularProgressDrawable = CircularProgressDrawable(context)
                                circularProgressDrawable.strokeWidth = 5f
                                circularProgressDrawable.centerRadius = 30f
                                circularProgressDrawable.start()

                                GlideApp.with(context)
                                    .load(storageRef)
                                    .apply(RequestOptions().override(600, 300))
                                    .placeholder(circularProgressDrawable)
                                    .into(orderProductImage!!)


                                orderProductName!!.text = product.name
                                orderProductPrice!!.text = "Price: ${product.price.toString()}"
                                orderProductQty!!.text = "Qty: ${productQty.toString()}"
                                orderProductDesc!!.text = product.desc
                            }
                        }
                    }
                } catch (ex: Exception) {
                }


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

}
