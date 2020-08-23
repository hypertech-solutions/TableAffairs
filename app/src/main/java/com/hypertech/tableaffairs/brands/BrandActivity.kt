package com.hypertech.tableaffairs.brands

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ProgressBar
import com.google.firebase.firestore.FirebaseFirestore
import com.hypertech.tableaffairs.helper.BRAND_NAME
import com.hypertech.tableaffairs.helper.PRODUCTS
import com.hypertech.tableaffairs.R
import com.hypertech.tableaffairs.helper.loadCart
import com.hypertech.tableaffairs.products.Product
import com.hypertech.tableaffairs.products.ProductAdapter
import kotlinx.android.synthetic.main.activity_brand.*

class BrandActivity : AppCompatActivity() {

    private var db = FirebaseFirestore.getInstance()
    private var productAdapter: ProductAdapter? = null
    private lateinit var progressBar: ProgressBar
    var name:String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_brand)

        val bundle = intent.extras
        name = bundle?.getString(BRAND_NAME)

        supportActionBar?.title = name?.capitalize()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        progressBar = findViewById(R.id.brand_progressBar)
        progressBar.isIndeterminate = true
        progressBar.visibility = View.VISIBLE

        loadProducts() //Load all brands with their data

        pullToRefreshBrandProducts.setOnRefreshListener {
            loadProducts() //Load all universities with their data
            productAdapter?.notifyDataSetChanged()
            pullToRefreshBrandProducts.isRefreshing = false

        }

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_brands, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when(item.itemId){
            android.R.id.home ->
                finish()
            R.id.brand_cart -> {
                this.loadCart()
            }
        }

        return super.onOptionsItemSelected(item)
    }

    private fun loadProducts() {
        try {
            val docRef = db.collection(PRODUCTS).whereEqualTo(BRAND_NAME, name)

            docRef.get().addOnSuccessListener {
                if (!it.isEmpty) {

                    brandProductTitle.text = getString(R.string.brand_with_products, name)
                    val productList = ArrayList<Product>()

                    for (i in it.documents) {
                        val product = i.toObject(Product::class.java)
                        if (product != null)
                            productList.add(product)
                    }

                    productAdapter = ProductAdapter(this, productList)
                    val productListView = listViewBrandProducts
                    productListView.adapter = productAdapter
                }else
                    brandProductTitle.text = getString(R.string.brand_no_products, name)

                progressBar.visibility = View.GONE
            }
        } catch (e: Exception) {}
    }
}
