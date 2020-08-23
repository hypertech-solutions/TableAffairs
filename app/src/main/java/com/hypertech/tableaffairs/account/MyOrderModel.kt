package com.hypertech.tableaffairs.account

import android.os.Parcelable
import com.google.firebase.Timestamp
import kotlinx.android.parcel.Parcelize
import kotlinx.android.parcel.RawValue

/*
*Created by Fadsoft on 22, August,2019
*Email: fahadimuhumuza@gmail.com
*Hypertech Solutions, Uganda
*/
@Parcelize
data class MyOrderModel(val orderId:String? = null,
                        val userId:String? = null,
                        val products:@RawValue ArrayList<HashMap<String, Any>>? = null,
                        val paymentAmount:Double? = null,
                        val paymentMethod:String? = null,
                        val deliveryMethod:String? = null,
                        val address:String? = null,
                        val paymentStatus:String? = null,
                        val deliveryStatus:String? = null,
                        val orderedAt: Timestamp = Timestamp.now()):Parcelable