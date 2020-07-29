package com.chandsemma.greatkitchens.activity

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.android.volley.toolbox.Volley
import com.google.android.material.navigation.NavigationView
import com.chandsemma.greatkitchens.R
import com.chandsemma.greatkitchens.fragment.*
import com.chandsemma.greatkitchens.adapter.RestaurantMenuAdapter
import com.chandsemma.greatkitchens.fragment.RestaurantFragment.Companion.resId
import com.chandsemma.greatkitchens.util.DrawerLocker
import com.chandsemma.greatkitchens.util.SessionManager

class MainActivity : AppCompatActivity(), DrawerLocker {

    override fun setDrawerEnabled(enabled: Boolean) {
        val lockMode = if (enabled)
            DrawerLayout.LOCK_MODE_UNLOCKED
        else
            DrawerLayout.LOCK_MODE_LOCKED_CLOSED


        drawerLayout.setDrawerLockMode(lockMode)
   //     actionBarDrawerToggle.isDrawerIndicatorEnabled = enabled
    }

    private lateinit var toolbar: Toolbar
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private var previousMenuItem: MenuItem? = null
    private lateinit var actionBarDrawerToggle: ActionBarDrawerToggle
    private lateinit var sessionManager: SessionManager
    private lateinit var sharedPrefs: SharedPreferences


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        sessionManager = SessionManager(this@MainActivity)
        sharedPrefs = this@MainActivity.getSharedPreferences(
            sessionManager.PREF_NAME,
            sessionManager.PRIVATE_MODE
        )

        toolbar = findViewById(R.id.toolbar)
        drawerLayout = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.navigation_view)
        setupToolbar()
        showAllRestaurants()
        val actionBarDrawerToggle=ActionBarDrawerToggle(this@MainActivity,drawerLayout,R.string.open_drawer,R.string.close_drawer)
        drawerLayout.addDrawerListener(actionBarDrawerToggle)
        actionBarDrawerToggle.syncState()
        navigationView.setNavigationItemSelectedListener { item: MenuItem ->
            if (previousMenuItem != null) {
                previousMenuItem?.isChecked = false
            }
            item.isCheckable = true
            item.isChecked = true
            previousMenuItem = item
            when (item.itemId) {
                R.id.home -> {
                    supportFragmentManager.beginTransaction().replace(R.id.frame,
                        AllRestaurantsFragment()
                    ).addToBackStack("All Restaurants").commit()
                    supportActionBar?.title = "All Restaurants"
                    drawerLayout.closeDrawers()
                }
                R.id.myProfile -> {
                    val profileFragment =
                        ProfileFragment()
                    supportFragmentManager.beginTransaction().replace(R.id.frame, profileFragment).addToBackStack("My profile") .commit()
                    supportActionBar?.title = "My profile"
                    drawerLayout.closeDrawers()
                }
                R.id.order_history -> {
                    val orderHistoryFragment =
                        OrderHistoryFragment()
                    supportFragmentManager.beginTransaction().replace(R.id.frame, orderHistoryFragment).addToBackStack("My Previous Orders") .commit()
                    supportActionBar?.title = "My Previous Orders"
                    drawerLayout.closeDrawers()
                }
                R.id.favRes -> {
                    val favFragment =
                        FavouritesFragment()
                    supportFragmentManager.beginTransaction().replace(R.id.frame, favFragment).addToBackStack("Favorite Restaurants") .commit()
                    supportActionBar?.title = "My Favorite Restaurants"
                    drawerLayout.closeDrawers()
                }
                R.id.faqs -> {
                    val faqFragment =
                        FAQFragment()
                    supportFragmentManager.beginTransaction().replace(R.id.frame, faqFragment).addToBackStack("Frequently Asked Questions").commit()
                    supportActionBar?.title = "Frequently Asked Questions"
                    drawerLayout.closeDrawers()
                }
                R.id.logout -> {
                    val dialog = AlertDialog.Builder(this@MainActivity)
                    dialog.setTitle("Logout")
                    dialog.setMessage("Do you want to logout?")
                    dialog.setPositiveButton("Yes, logout") { _, _ ->
                        sessionManager.setLogin(false)
                        sharedPrefs.edit().clear().apply()
                        startActivity(Intent(this@MainActivity, LoginActivity::class.java))
                        Volley.newRequestQueue(this).cancelAll(this::class.java.simpleName)
                        ActivityCompat.finishAffinity(this)
                    }
                    dialog.setNegativeButton("No, I don't") { _, _ ->
                        showAllRestaurants()
                    }
                    dialog.create()
                    dialog.show()
                }
            }
            return@setNavigationItemSelectedListener true
        }

        val convertView = LayoutInflater.from(this@MainActivity).inflate(R.layout.drawer_header, null)
        val userName: TextView = convertView.findViewById(R.id.txtDrawerText)
        val userPhone: TextView = convertView.findViewById(R.id.txtDrawerSecondaryText)
        val appIcon: ImageView = convertView.findViewById(R.id.imgDrawerImage)
        userName.text = sharedPrefs.getString("user_name", null)
        val phoneText = "+91-${sharedPrefs.getString("user_mobile_number", null)}"
        userPhone.text = phoneText
        navigationView.addHeaderView(convertView)

        userName.setOnClickListener {
            val profileFragment =
                ProfileFragment()
            val transaction = supportFragmentManager.beginTransaction()
            transaction.replace(R.id.frame, profileFragment)
            transaction.commit()
            supportActionBar?.title = "My profile"
            val mPendingRunnable = Runnable { drawerLayout.closeDrawer(GravityCompat.START) }
            Handler().postDelayed(mPendingRunnable, 50)
        }
        appIcon.setOnClickListener {
            val profileFragment =
                ProfileFragment()
            val transaction = supportFragmentManager.beginTransaction()
            transaction.replace(R.id.frame, profileFragment)
            transaction.commit()
            supportActionBar?.title = "My profile"
            val mPendingRunnable = Runnable { drawerLayout.closeDrawer(GravityCompat.START) }
            Handler().postDelayed(mPendingRunnable, 50)
        }

    }

    private fun showAllRestaurants() {
        val fragment =
            AllRestaurantsFragment()
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.frame, fragment)
        transaction.commit()
        supportActionBar?.title = "All Restaurants"
        navigationView.setCheckedItem(R.id.home)
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = ""
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        val f = supportFragmentManager.findFragmentById(R.id.frame)
        when (id) {
            android.R.id.home -> {
                if (f is RestaurantFragment) {
                    onBackPressed()
                } else {
                    drawerLayout.openDrawer(GravityCompat.START)
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        val f = supportFragmentManager.findFragmentById(R.id.frame)
        when (f) {
            is AllRestaurantsFragment -> {
                Volley.newRequestQueue(this).cancelAll(this::class.java.simpleName)
                super.onBackPressed()
            }
            is RestaurantFragment -> {
                if (!RestaurantMenuAdapter.isCartEmpty) {
                    val builder = AlertDialog.Builder(this@MainActivity)
                    builder.setTitle("Confirmation")
                        .setMessage("Going back will reset cart items. Do you still want to proceed?")
                        .setPositiveButton("Yes") { _, _ ->
                            val clearCart =
                                CartActivity.ClearDBAsync(applicationContext, resId.toString()).execute().get()
                            showAllRestaurants()
                            RestaurantMenuAdapter.isCartEmpty = true
                        }
                        .setNegativeButton("No") { _, _ ->
                        }
                        .create()
                        .show()
                } else {
                    showAllRestaurants()
                }
            }
            else -> showAllRestaurants()
        }
    }

}
