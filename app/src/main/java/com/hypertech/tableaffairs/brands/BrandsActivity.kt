package com.hypertech.tableaffairs.brands

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import com.google.firebase.firestore.FirebaseFirestore
import com.hypertech.tableaffairs.helper.BRANDS
import com.hypertech.tableaffairs.R
import kotlinx.android.synthetic.main.activity_brands.*

class BrandsActivity : AppCompatActivity() {

    private var db = FirebaseFirestore.getInstance()
    private var brandsAdapter:BrandAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_brands)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        loadBrands() //Load all universities with their data

        pullToRefreshBrands.setOnRefreshListener {
            loadBrands() //Load all universities with their data
            brandsAdapter?.notifyDataSetChanged()
            pullToRefreshBrands.isRefreshing = false

        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if (item.itemId == android.R.id.home)
            finish()

        return super.onOptionsItemSelected(item)
    }

    private fun loadBrands() {
        try {
            val docRef = db.collection(BRANDS)
            docRef.get().addOnSuccessListener {
                if (!it.isEmpty) {

                    val brandList = ArrayList<Brand>()

                    for (i in it.documents) {
                        val brand = i.toObject(Brand::class.java)
                        if (brand != null)
                            brandList.add(brand)
                    }

                    brandsAdapter = BrandAdapter(this, brandList)
                    val brandGridView = listViewBrands
                    brandGridView.adapter = brandsAdapter
                }
            }
        } catch (e: Exception) {}
    }
}
