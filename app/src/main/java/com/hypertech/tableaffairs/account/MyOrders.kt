package com.hypertech.tableaffairs.account

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.hypertech.tableaffairs.R
import com.hypertech.tableaffairs.checkout.Order
import com.hypertech.tableaffairs.helper.MY_ORDER_MODEL
import com.hypertech.tableaffairs.helper.ORDERS
import com.hypertech.tableaffairs.helper.USER_ID
import kotlinx.android.synthetic.main.activity_my_orders.*

class MyOrders : AppCompatActivity() {

    private var mAuth: FirebaseAuth? = null
    private var db = FirebaseFirestore.getInstance()
    private lateinit var orderRecyclerView: RecyclerView
    private lateinit var orderList:ArrayList<Order>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_orders)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        mAuth = FirebaseAuth.getInstance()
        populateOrderRecyclerView()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if (item.itemId == android.R.id.home)
            finish()

        return super.onOptionsItemSelected(item)
    }

    private fun populateOrderRecyclerView() {
        orderRecyclerView = findViewById(R.id.recycleMyOrders)
        orderList = ArrayList()
        val userId = mAuth?.currentUser!!.uid
        try {

            val docRef = db.collection(ORDERS).whereEqualTo(USER_ID, userId)
            docRef.get().addOnSuccessListener {
                if (!it.isEmpty){
                    title_orders.text = getString(R.string.my_orders)

                    for (i in it.documents){
                        val order = i.toObject(Order::class.java)
                        if (order != null) {
                            orderList.add(order)
                        }
                    }

                    val adapter = MyOrdersAdapter(this, orderList)
                    val myLayout = LinearLayoutManager(this)
                    orderRecyclerView.addItemDecoration(DividerItemDecoration(orderRecyclerView.context, myLayout.orientation))
                    orderRecyclerView.layoutManager = myLayout
                    orderRecyclerView.itemAnimator = DefaultItemAnimator()
                    orderRecyclerView.adapter = adapter

                    adapter.setOnItemClickListener(onItemClickListener)
                    adapter.notifyDataSetChanged()


                }else
                    title_orders.text = getString(R.string.no_orders)
            }
        }catch (ex:Exception){}
    }

    private val onItemClickListener = View.OnClickListener {
        val viewHolder = it.tag as RecyclerView.ViewHolder
        val position = viewHolder.adapterPosition

        val order = orderList[position]
        val myOrderModel = MyOrderModel(order.orderId, order.userId, order.products, order.paymentAmount,
            order.paymentMethod, order.deliveryMethod, order.address, order.paymentStatus, order.deliveryStatus,
            order.orderedAt)

        val intent = Intent(this, MyOrder::class.java)
        intent.putExtra(MY_ORDER_MODEL, myOrderModel)
        startActivity(intent)
    }
}
