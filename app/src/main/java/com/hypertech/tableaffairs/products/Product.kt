package com.hypertech.tableaffairs.products

/*
*Created by Fadsoft on 16, August,2019
*Email: fahadimuhumuza@gmail.com
*Hypertech Solutions, Uganda
*/

data class Product(val brand:String? = null, val id:String? = null, val image:String? = null,
                   val name:String? = null, val desc:String? = null, val price:Double = 0.0,
                   val stock:Int = 0)