package com.hypertech.tableaffairs.helper

import android.content.Context
import android.content.SharedPreferences
import android.content.Context.MODE_PRIVATE



/*
*Created by Fadsoft on 18, August,2019
*Email: fahadimuhumuza@gmail.com
*Hypertech Solutions, Uganda
*/

class MySharedPreference(private val context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(SHARED_PREF, MODE_PRIVATE)

    fun addProductToCart(name:String, productId: String) {
        val edits = prefs.edit()
        edits.putString(name, productId)
        edits.apply()
    }

    fun retrieveProductFromCart(): String? {
        return prefs.getString(PRODUCT_ID, "")
    }

    fun addQuantityCount(quantityCount: Int) {
        val edits = prefs.edit()
        edits.putInt(QUANTITY_COUNT, quantityCount)
        edits.apply()
    }

    fun retrieveQuantityCount(): Int {
        return prefs.getInt(QUANTITY_COUNT, 0)
    }
}