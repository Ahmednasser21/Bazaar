package com.iti.itp.bazaar.mainActivity

import android.animation.ValueAnimator
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.VectorDrawable
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.snackbar.Snackbar
import com.iti.itp.bazaar.R
import com.iti.itp.bazaar.auth.MyConstants
//import com.iti.itp.bazaar.shoppingCartActivity.ShoppingCartActivity
import com.iti.itp.bazaar.databinding.ActivityMainBinding
import com.iti.itp.bazaar.settings.SettingsActivity

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration
    lateinit var mySharedPrefrence: SharedPreferences
    lateinit var IsGuestMode: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mySharedPrefrence = getSharedPreferences(
            MyConstants.MY_SHARED_PREFERANCE,
            Context.MODE_PRIVATE
        )
        IsGuestMode = mySharedPrefrence.getString(MyConstants.IS_GUEST, "false") ?: "false"


        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        val navView: BottomNavigationView = binding.navView
        navController = findNavController(R.id.nav_host_fragment_activity_main)
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home, R.id.nav_categories, R.id.nav_me, R.id.nav_brand_products,
                R.id.prouductnfoFragment, R.id.searchFragment, R.id.orderFragment,
                R.id.favoriteProductsFragment
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, destination, arguments ->
            val toolbarTitle: TextView = findViewById(R.id.toolbar_title)
            toolbarTitle.text = destination.label

            if (destination.id == R.id.nav_brand_products) {
                toolbarTitle.text = arguments?.getString("vendorName") ?: "Brands"
            }

            invalidateOptionsMenu()
        }

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.nav_me -> {
                    disableEdgeToEdge()
                    showToolBar()
                    when (IsGuestMode) {
                        "true" -> {
                            Snackbar.make(
                                binding.navView,
                                "cant go to Me screen in guest Mode ",
                                2000
                            ).show()

                        }

                        else -> {
                            val toolbarTitle: TextView = findViewById(R.id.toolbar_title)
                            toolbarTitle.text = destination.label
                            invalidateOptionsMenu()
                        }
                    }


                }

                R.id.nav_home -> {
                    hideToolBar()
                    enableEdgeToEdge()
                    animateIconFill(R.id.nav_home)
                }

                else -> {
                    showToolBar()
                    disableEdgeToEdge()
                }
            }
        }
        binding.toolbar.searchImg.setOnClickListener {
            navController.navigate(R.id.searchFragment)
        }
    }

    private fun disableEdgeToEdge() {
        WindowCompat.setDecorFitsSystemWindows(window, true)
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        val isNavMe = navController.currentDestination?.id == R.id.nav_me
        menu.findItem(R.id.nav_settings)?.isVisible = isNavMe
        menu.findItem(R.id.nav_favourite)?.isVisible = !isNavMe
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.action_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.nav_cart -> {
                when (IsGuestMode) {
                    "true" -> {
                        Snackbar.make(binding.navView, "cant go to cart in guest Mode ", 2000)
                            .show()

                        true
                    }

                    else -> {
//                        val intent = Intent(this, ShoppingCartActivity::class.java)
//                        startActivity(intent)
                        true
                    }
                }

            }

            R.id.nav_favourite -> {
                when (IsGuestMode) {
                    "true" -> {
                        Snackbar.make(binding.root, "cant go to favorites in guest Mode ", 2000)
                            .show()

                        true
                    }

                    else -> {
                        navController.navigate(R.id.favoriteProductsFragment)
                        true
                    }
                }

            }

            R.id.nav_settings -> {
                when (IsGuestMode) {

                    "true" -> {
                        Snackbar.make(binding.root, "cant go to settings in guest Mode ", 2000)
                        true
                    }

                    else -> {
                        val intent = Intent(this, SettingsActivity::class.java)
                        startActivity(intent)
                        true
                    }
                }


            }

            else -> super.onOptionsItemSelected(item)

        }
    }


    fun hideToolBar() {
        binding.toolbar.toolbar.visibility = View.GONE
    }


    fun showToolBar() {
        binding.toolbar.toolbar.visibility = View.VISIBLE
    }


    private fun animateIconFill(itemId: Int) {
        val menuItem = findViewById<BottomNavigationView>(R.id.nav_view).menu.findItem(itemId)
        val drawable = menuItem.icon?.mutate() ?: return  // Mutate to avoid affecting other instances

        // Create fill animation
        val fillAnimator = ValueAnimator.ofArgb(
            ContextCompat.getColor(this, android.R.color.transparent),
            ContextCompat.getColor(this, R.color.primaryColor)
        ).apply {
            duration = 300 // Animation duration in milliseconds
            addUpdateListener { animator ->
                val color = animator.animatedValue as Int
                drawable.setColorFilter(color, PorterDuff.Mode.SRC_IN)
            }
        }

        fillAnimator.start()
    }
}