package com.hypertech.tableaffairs.account

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.hypertech.tableaffairs.R
import com.hypertech.tableaffairs.checkout.Order

/*
*Created by Fadsoft on 22, August,2019
*Email: fahadimuhumuza@gmail.com
*Hypertech Solutions, Uganda
*/

class MyOrdersAdapter (context: Context, private val ordersList:ArrayList<Order>) :RecyclerView.Adapter<MyOrdersAdapter.MyViewHolder>(){

    private var myOnItemClickListener: View.OnClickListener? = null

    inner class MyViewHolder(view: View):RecyclerView.ViewHolder(view){

        var orderId:TextView? = null
        var orderDate:TextView? = null

        init {
            orderId = view.findViewById(R.id.orderId)
            orderDate = view.findViewById(R.id.orderDate)
            view.tag = this
            view.setOnClickListener(myOnItemClickListener)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.ticket_my_orders, parent, false)
        return MyViewHolder(itemView)

    }

    override fun getItemCount(): Int {
        return ordersList.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val order = ordersList[position]
        holder.orderId!!.text = order.orderId
        holder.orderDate!!.text = order.orderedAt.toString()
    }

    //Step 2 of 4: Assign itemClickListener to your local View.OnClickListener variable
    fun setOnItemClickListener(itemClickListener: View.OnClickListener) {
        myOnItemClickListener = itemClickListener
    }

}