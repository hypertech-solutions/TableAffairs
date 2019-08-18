package com.hypertech.tableaffairs.account

import com.google.firebase.Timestamp

data class User(val userId:String? = null, val fullName:String? = null, val phoneNumber:String? = null,
                val email:String? = null, val createdAt: Timestamp = Timestamp.now())