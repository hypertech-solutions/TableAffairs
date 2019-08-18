package com.hypertech.tableaffairs.helper

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.hypertech.tableaffairs.cart.TempCart

/*
*Created by Fadsoft on 18, August,2019
*Email: fahadimuhumuza@gmail.com
*Hypertech Solutions, Uganda
*/

class DBHelper (context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    // create table sql query
    private val createCartTable = ("CREATE TABLE IF NOT EXISTS $TABLE_CART ($COLUMN_CART_ID INTEGER PRIMARY KEY AUTOINCREMENT, $COLUMN_ITEM_ID TEXT, $COLUMN_ITEM_QTY INTEGER)")

    // drop table sql query
    private val dropCartTable = "DROP TABLE IF EXISTS $TABLE_CART"

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(createCartTable)
    }


    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {

        //Drop Cart Table if exist
        db.execSQL(dropCartTable)

        // Create tables again
        onCreate(db)

    }

    fun addItemToTempCart(tempCart: TempCart):Int{

        val db = this.writableDatabase
        val cartId:Int

        val values = ContentValues()
        values.put(COLUMN_ITEM_ID, tempCart.itemId)
        values.put(COLUMN_ITEM_QTY, tempCart.qty)
        cartId = db.insert(TABLE_CART, null, values).toInt()
        //Close Database
        db.close()
        return cartId
    }

    fun retrieveTempCart():ArrayList<TempCart>{

        val db = this.readableDatabase

        // array of columns to fetch
        val columns = arrayOf(COLUMN_ITEM_ID, COLUMN_ITEM_QTY)

        // sorting orders
        val sortOrder = "$COLUMN_CART_ID ASC"
        val tempCartList = ArrayList<TempCart>()

        val cursor = db.query(TABLE_CART, //Table to query
            columns,            //columns to return
            null,     //columns for the WHERE clause
            null,  //The values for the WHERE clause
            null,      //group the rows
            null,       //filter by row groups
            sortOrder)         //The sort order

        if (cursor.moveToFirst()) {
            do {
                val tempCart = TempCart(
                    id = cursor.getString(cursor.getColumnIndex(COLUMN_CART_ID)).toInt(),
                    itemId = cursor.getString(cursor.getColumnIndex(COLUMN_ITEM_ID)),
                    qty = cursor.getString(cursor.getColumnIndex(COLUMN_ITEM_QTY)).toInt())

                tempCartList.add(tempCart)
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return tempCartList
    }

    fun updateTempCart(tempCart: TempCart){
        val db = this.writableDatabase

        val values = ContentValues()
        values.put(COLUMN_ITEM_QTY, tempCart.qty)

        // updating row
        db.update(TABLE_CART, values, "$COLUMN_CART_ID = ?",
            arrayOf(tempCart.id.toString()))
        db.close()
    }

    fun deleteTempCart(tempCart: TempCart):Int{
        val db = this.writableDatabase
        val result:Int
        // delete user record by id
        result = db.delete(TABLE_CART, "$COLUMN_CART_ID = ?",
            arrayOf(tempCart.id.toString()))
        db.close()

        return result
    }

    companion object {

        // Database Version
        private const val DATABASE_VERSION = 1

        // Database Name
        private const val DATABASE_NAME = "CartManager.db"

        // User table name
        private const val TABLE_CART = "temp_cart"

        // User Table Columns names
        private const val COLUMN_CART_ID = "cart_id"
        private const val COLUMN_ITEM_ID = "item_id"
        private const val COLUMN_ITEM_QTY = "item_qty"
    }
}