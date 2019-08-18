package com.hypertech.tableaffairs.brands

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.storage.FirebaseStorage
import com.hypertech.tableaffairs.helper.BRAND_NAME
import com.hypertech.tableaffairs.GlideApp
import com.hypertech.tableaffairs.R

/*
*Created by Fadsoft on 16, August,2019
*Email: fahadimuhumuza@gmail.com
*Hypertech Solutions, Uganda
*/

class BrandAdapter (private var context: Context, private var brandList:ArrayList<Brand>): BaseAdapter(){

    private var storage = FirebaseStorage.getInstance()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View? {

        var listLayout = convertView

        if (convertView == null){
            val layoutInflater = LayoutInflater.from(context)
            listLayout = layoutInflater.inflate(R.layout.ticket_brand, parent, false)
        }

        val brand = brandList[position]

        val id = brand.id
        val image = brand.image
        val name = brand.name
        val desc = brand.desc

        val storageRef = storage.getReferenceFromUrl("gs://tableaffairs-994b4.appspot.com/tableaffairs/brands/$image")


        val brandImage = listLayout?.findViewById<ImageView>(R.id.brandImage)
        val brandName = listLayout?.findViewById<TextView>(R.id.brandName)
        val brandDesc = listLayout?.findViewById<TextView>(R.id.brandDesc)

        val circularProgressDrawable = CircularProgressDrawable(context)
        circularProgressDrawable.strokeWidth = 5f
        circularProgressDrawable.centerRadius = 30f
        circularProgressDrawable.start()

        GlideApp.with(context)
            .load(storageRef)
            .apply(RequestOptions().override(600, 300))
            .placeholder(circularProgressDrawable)
            .into(brandImage!!)

        brandName!!.text = name
        brandDesc!!.text = desc

        listLayout?.setOnClickListener {
            val intent = Intent(context, BrandActivity::class.java)
            intent.putExtra(BRAND_NAME, name)
            context.startActivity(intent)
        }

        return listLayout
    }

    override fun getItem(position: Int): Any {
        return brandList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount(): Int {
        return brandList.size
    }

}
