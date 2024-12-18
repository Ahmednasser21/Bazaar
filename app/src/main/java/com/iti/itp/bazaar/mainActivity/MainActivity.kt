package com.iti.itp.bazaar.mainActivity

import android.animation.ValueAnimator
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.snackbar.Snackbar
import com.iti.itp.bazaar.R
import com.iti.itp.bazaar.auth.AuthActivity
import com.iti.itp.bazaar.auth.MyConstants
import com.iti.itp.bazaar.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var mySharedPreference: SharedPreferences
    private lateinit var isGuestMode: String
    private lateinit var toolbar: Toolbar
    private lateinit var toolbarTitle: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        mySharedPreference = getSharedPreferences(
            MyConstants.MY_SHARED_PREFERANCE,
            Context.MODE_PRIVATE
        )
        isGuestMode = mySharedPreference.getString(MyConstants.IS_GUEST, "false") ?: "false"

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayShowTitleEnabled(false)
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_arrow_back)
        }

        val navView: BottomNavigationView = binding.navView
        navController = findNavController(R.id.nav_host_fragment_activity_main)

        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home,
                R.id.nav_search,
                R.id.nav_profile,
                R.id.nav_cart,
                R.id.nav_favourite
            )
        )

        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, destination, arguments ->
            toolbarTitle = findViewById(R.id.toolbar_title)
            toolbarTitle.text = destination.label

            when (destination.id) {
                R.id.nav_brand_products -> {
                    toolbarTitle.text = arguments?.getString("vendorName") ?: "Brands"
                    showToolBar()
                    disableEdgeToEdge()
                }

                R.id.nav_profile, R.id.nav_favourite, R.id.nav_cart -> {
                    applyGuestConstrains(destination)
                }

                R.id.nav_home -> {
                    hideToolBar()
                    enableEdgeToEdge()
                    animateIconFill(R.id.nav_home)
                }

                R.id.nav_search -> {
                    hideToolBar()
                    disableEdgeToEdge()
                }

                R.id.successOrderPage->{
                    hideToolBar()
                }

                else -> {
                    showToolBar()
                    disableEdgeToEdge()
                }
            }

            supportActionBar?.setDisplayHomeAsUpEnabled(
                !appBarConfiguration.topLevelDestinations.contains(destination.id)
            )

            invalidateOptionsMenu()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    private fun disableEdgeToEdge() {
        WindowCompat.setDecorFitsSystemWindows(window, true)
    }

    private fun hideToolBar() {
        binding.toolbar.toolbar.visibility = View.GONE
    }

    private fun showToolBar() {
        binding.toolbar.toolbar.visibility = View.VISIBLE
    }

    private fun animateIconFill(itemId: Int) {
        val menuItem = findViewById<BottomNavigationView>(R.id.nav_view).menu.findItem(itemId)
        val drawable =
            menuItem.icon?.mutate() ?: return

        val fillAnimator = ValueAnimator.ofArgb(
            ContextCompat.getColor(this, android.R.color.transparent),
            ContextCompat.getColor(this, R.color.primaryColor)
        ).apply {
            duration = 300
            addUpdateListener { animator ->
                val color = animator.animatedValue as Int
                drawable.setColorFilter(color, PorterDuff.Mode.SRC_IN)
            }
        }

        fillAnimator.start()
    }

    private fun applyGuestConstrains(destination: NavDestination) {
        disableEdgeToEdge()
        showToolBar()
        if (isGuestMode == "true") {
            Snackbar.make(
                binding.navView,
                "Signup first to use this feature",
                Snackbar.LENGTH_LONG
            ).setAction("Signup") {
                val intent = Intent(this, AuthActivity::class.java)
                intent.putExtra("navigateToFragment", "SignUpFragment")
                startActivity(intent)
            }.setActionTextColor(getColor(R.color.primaryColor)).show()
            navController.popBackStack()
        } else {
            toolbarTitle.text = destination.label
        }
    }
}