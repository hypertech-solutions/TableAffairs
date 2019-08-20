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
    private val createCartTable = ("CREATE TABLE IF NOT EXISTS $TABLE_CART ($COLUMN_CART_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "$COLUMN_ITEM_ID TEXT, $COLUMN_ITEM_IMAGE TEXT, $COLUMN_ITEM_NAME TEXT, $COLUMN_ITEM_PRICE REAL, $COLUMN_ITEM_QTY INTEGER, $COLUMN_ITEM_STOCK INTEGER)")

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

    fun checkForItemExistence(itemId:String):Boolean{
        // array of columns to fetch
        val columns = arrayOf(COLUMN_CART_ID)
        val db = this.readableDatabase

        // selection criteria
        val selection = "$COLUMN_ITEM_ID = ?"

        // selection argument
        val selectionArgs = arrayOf(itemId)

        val cursor = db.query(
            TABLE_CART, //Table to query
            columns,        //columns to return
            selection,      //columns for the WHERE clause
            selectionArgs,  //The values for the WHERE clause
            null,  //group the rows
            null,   //filter by row groups
            null)  //The sort order


        val cursorCount = cursor.count
        cursor.close()
        db.close()

        if (cursorCount > 0) {
            return true
        }

        return false
    }

    fun retrieveQuantity(itemId:String):Int{

        val columns = arrayOf(COLUMN_ITEM_QTY)
        val db = this.readableDatabase

        var quantity = 1

        // selection criteria
        val selection = "$COLUMN_ITEM_ID = ?"

        // selection argument
        val selectionArgs = arrayOf(itemId)

        val cursor = db.query(
            TABLE_CART, //Table to query
            columns,        //columns to return
            selection,      //columns for the WHERE clause
            selectionArgs,  //The values for the WHERE clause
            null,  //group the rows
            null,   //filter by row groups
            null)  //The sort order

        if (cursor.moveToFirst()) {
            do {
                quantity = cursor.getString(cursor.getColumnIndex(COLUMN_ITEM_QTY)).toInt()
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()

        return quantity
    }

    fun addItemToTempCart(tempCart: TempCart):Int{

        val db = this.writableDatabase
        val cartId:Int

        val values = ContentValues()
        values.put(COLUMN_ITEM_ID, tempCart.itemId)
        values.put(COLUMN_ITEM_IMAGE, tempCart.itemImage)
        values.put(COLUMN_ITEM_NAME, tempCart.itemName)
        values.put(COLUMN_ITEM_PRICE, tempCart.price)
        values.put(COLUMN_ITEM_QTY, tempCart.qty)
        values.put(COLUMN_ITEM_STOCK, tempCart.stock)
        cartId = db.insert(TABLE_CART, null, values).toInt()
        //Close Database
        db.close()
        return cartId
    }

    fun retrieveTempCart():ArrayList<TempCart>{

        val db = this.readableDatabase

        // array of columns to fetch
        val columns = arrayOf(COLUMN_CART_ID, COLUMN_ITEM_ID, COLUMN_ITEM_IMAGE, COLUMN_ITEM_NAME, COLUMN_ITEM_PRICE,  COLUMN_ITEM_QTY, COLUMN_ITEM_STOCK)

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
                    itemImage = cursor.getString(cursor.getColumnIndex(COLUMN_ITEM_IMAGE)),
                    itemName = cursor.getString(cursor.getColumnIndex(COLUMN_ITEM_NAME)),
                    price = cursor.getString(cursor.getColumnIndex(COLUMN_ITEM_PRICE)).toDouble(),
                    qty = cursor.getString(cursor.getColumnIndex(COLUMN_ITEM_QTY)).toInt(),
                    stock = cursor.getString(cursor.getColumnIndex(COLUMN_ITEM_STOCK)).toInt())

                tempCartList.add(tempCart)
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return tempCartList
    }

    fun updateTempCart(itemId:String, itemQty:Int):Int{
        val db = this.writableDatabase

        val values = ContentValues()
        values.put(COLUMN_ITEM_QTY, itemQty)

        // updating row
        val result = db.update(TABLE_CART, values, "$COLUMN_ITEM_ID = ?",
            arrayOf(itemId))
        db.close()
        return result
    }

    fun deleteItemTempCart(tempCart: TempCart):Int{
        val db = this.writableDatabase
        val result:Int
        // delete user record by id
        result = db.delete(TABLE_CART, "$COLUMN_CART_ID = ?",
            arrayOf(tempCart.id.toString()))
        db.close()

        return result
    }

    fun deleteTempCart():Int{
        val db = this.writableDatabase
        val result:Int

        result = db.delete(TABLE_CART, "$COLUMN_CART_ID = ?",
            arrayOf("%"))
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
        private const val COLUMN_ITEM_IMAGE = "item_image"
        private const val COLUMN_ITEM_NAME = "item_name"
        private const val COLUMN_ITEM_PRICE = "item_price"
        private const val COLUMN_ITEM_QTY = "item_qty"
        private const val COLUMN_ITEM_STOCK = "item_stock"
    }
}