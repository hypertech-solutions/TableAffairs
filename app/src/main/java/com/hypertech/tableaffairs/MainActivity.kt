package com.hypertech.tableaffairs

import android.content.ActivityNotFoundException
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.core.view.GravityCompat
import androidx.appcompat.app.ActionBarDrawerToggle
import android.view.MenuItem
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import android.view.Menu
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.firestore.FirebaseFirestore
import com.hypertech.tableaffairs.account.Login
import com.hypertech.tableaffairs.account.MyOrders
import com.hypertech.tableaffairs.brands.BrandsActivity
import com.hypertech.tableaffairs.contact.AboutUs
import com.hypertech.tableaffairs.contact.ContactUs
import com.hypertech.tableaffairs.helper.PRODUCTS
import com.hypertech.tableaffairs.helper.loadCart
import com.hypertech.tableaffairs.products.Product
import com.hypertech.tableaffairs.products.ProductAdapter
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private var mFirebaseAnalytics: FirebaseAnalytics? = null
    private var mAuth: FirebaseAuth? = null
    private var mStorageRef: StorageReference? = null
    private lateinit var navHeader:View
    private var db = FirebaseFirestore.getInstance()
    private var productAdapter: ProductAdapter? = null
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this)
        mAuth = FirebaseAuth.getInstance()
        mStorageRef = FirebaseStorage.getInstance().reference

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        navView.setNavigationItemSelectedListener(this)

        navHeader = navView.getHeaderView(0) //Nav Header access

        navHeader.setOnClickListener{
            drawerLayout.closeDrawers()
            loadProfile()
        }

        val fab: FloatingActionButton = findViewById(R.id.fabCart)
        fab.setOnClickListener {
            loadCart()
        }

        progressBar = findViewById(R.id.products_progressBar)
        progressBar.isIndeterminate = true
        progressBar.visibility = View.VISIBLE

        userDetails() //Load User Details

        loadProducts() //Load all products with their data

        pullToRefreshProducts.setOnRefreshListener {
            loadProducts() //Load all products with their data
            productAdapter?.notifyDataSetChanged()
            pullToRefreshProducts.isRefreshing = false

        }
    }

    override fun onBackPressed() {
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Close App")
            builder.setMessage("Are you sure you want to close the app?")
            builder.setPositiveButton("Yes") { _: DialogInterface, _: Int ->
                finish()
            }
            builder.setNegativeButton("No") { dialogInterface: DialogInterface, _: Int ->
                dialogInterface.dismiss()
            }
            builder.show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        when (item.itemId) {
            R.id.action_cart -> {
                loadCart()
            }
            R.id.action_settings -> {

            }
            R.id.action_logout -> {
                logOutUser()
            }

        }

        return super.onOptionsItemSelected(item)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        when (item.itemId) {
            R.id.nav_home -> {
            }
            R.id.nav_brands -> {
                val intent = Intent(this, BrandsActivity::class.java)
                startActivity(intent)
            }
            R.id.nav_myorders -> {
                val intent = Intent(this, MyOrders::class.java)
                startActivity(intent)
            }
            R.id.nav_settings -> {
            }
            R.id.nav_myprofile -> {
                loadProfile()
            }
            R.id.nav_logout -> {
                drawer_layout.closeDrawers()
                logOutUser()
            }
            R.id.nav_share -> {
                shareApp()
            }
            R.id.nav_contactus -> {
                val intent = Intent(this, ContactUs::class.java)
                startActivity(intent)
            }
            R.id.nav_about -> {
                val intent = Intent(this, AboutUs::class.java)
                startActivity(intent)
            }
            R.id.nav_rateus -> {
                rateApp()
            }
        }
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun userDetails(){

        val username = navHeader.findViewById<TextView>(R.id.nav_username)
        val email = navHeader.findViewById<TextView>(R.id.nav_email)

        val currentUser = mAuth!!.currentUser
        username.text = currentUser?.displayName ?: ""
        email.text = currentUser?.email ?: ""
    }

    private fun loadProfile() {

    }

    private fun loadProducts() {
        try {
            val docRef = db.collection(PRODUCTS)
            docRef.get().addOnSuccessListener {
                if (!it.isEmpty) {
                    productsTitle.text = getString(R.string.our_products)
                    val productList = ArrayList<Product>()

                    for (i in it.documents) {
                        val product = i.toObject(Product::class.java)
                        if (product != null)
                            productList.add(product)
                    }

                    productAdapter = ProductAdapter(this, productList)
                    val productListView = listViewProducts
                    productListView.adapter = productAdapter
                }else
                    productsTitle.text = getString(R.string.no_products)

                progressBar.visibility = View.GONE
            }
        } catch (e: Exception) {}
    }

    private fun shareApp() {

        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.type = "text/plain"
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Share This App")
        val appUrl = "http://play.google.com/store/apps/details?id=${this.packageName}"
        shareIntent.putExtra(Intent.EXTRA_TEXT, appUrl)
        startActivity(Intent.createChooser(shareIntent, "Share via"))
    }

    private fun rateApp() {
        val uri = Uri.parse("market://details?id=" + this.packageName)
        val goToMarket = Intent(Intent.ACTION_VIEW, uri)
        goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY or Intent.FLAG_ACTIVITY_NEW_DOCUMENT or Intent.FLAG_ACTIVITY_MULTIPLE_TASK)

        try {
            startActivity(goToMarket)
        } catch (ex: ActivityNotFoundException) {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("http://play.google.com/store/apps/details?id=${this.packageName}")
                )
            )
        }
    }

    private fun logOutUser() {

        val builder = AlertDialog.Builder(this)
        builder.setCancelable(false)
        builder.setTitle("Log out")
        builder.setMessage("Are you sure you want to Log out?")
        builder.setPositiveButton("YES") { _: DialogInterface, _ ->
            val intent = Intent(this, Login::class.java)
            mAuth!!.signOut()
            startActivity(intent)
            finish()
        }
        builder.setNegativeButton("CANCEL") { dialog: DialogInterface, _ ->
            dialog.dismiss()
        }
        builder.create().show()

    }
}
