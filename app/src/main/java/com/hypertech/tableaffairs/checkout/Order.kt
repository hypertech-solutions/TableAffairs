package com.hypertech.tableaffairs.checkout

import com.google.firebase.Timestamp

/*
*Created by Fadsoft on 20, August,2019
*Email: fahadimuhumuza@gmail.com
*Hypertech Solutions, Uganda
*/

data class Order (val orderId:String? = null, val userId:String? = null,
                  val products:ArrayList<HashMap<String, Int>>? = null,
                  val totalAmount:Double? = null,
                  val paymentMethod:String? = null,
                  val deliveryMethod:String? = null,
                  val address:String? = null,
                  val PaymentStatus:String? = null,
                  val orderedAt: Timestamp = Timestamp.now())