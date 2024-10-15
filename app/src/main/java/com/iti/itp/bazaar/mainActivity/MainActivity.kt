package com.iti.itp.bazaar.mainActivity

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.iti.itp.bazaar.R
import com.iti.itp.bazaar.shoppingCartActivity.ShoppingCartActivity
import com.iti.itp.bazaar.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController:NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val toolbar:Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        val navView: BottomNavigationView = binding.navView
        navController = findNavController(R.id.nav_host_fragment_activity_main)
        val appBarConfiguration = AppBarConfiguration(
            setOf(R.id.nav_home, R.id.nav_categories, R.id.nav_me)
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            val toolbarTitle: TextView = findViewById(R.id.toolbar_title)
            toolbarTitle.text = destination.label

            invalidateOptionsMenu()
        }

        val searchView: SearchView = findViewById<SearchView?>(R.id.search_view).apply {

            val searchIcon = findViewById<ImageView>(androidx.appcompat.R.id.search_button)
            searchIcon.setColorFilter(Color.WHITE)

            val searchEditText: EditText = this.findViewById(androidx.appcompat.R.id.search_src_text)
            searchEditText.setTextColor(Color.WHITE)
            searchEditText.textSize = 20f

            val closeIcon = findViewById<ImageView>(androidx.appcompat.R.id.search_close_btn)
            closeIcon.setColorFilter(Color.WHITE)
        }

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {

                return false
            }


            override fun onQueryTextChange(newText: String?): Boolean {

                return false
            }

        })
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when(destination.id){
                R.id.nav_me->{
                    val toolbarTitle: TextView = findViewById(R.id.toolbar_title)
                    toolbarTitle.text = destination.label
                    searchView.visibility = View.INVISIBLE
                    invalidateOptionsMenu()
                }
                R.id.nav_home->{
                    searchView.visibility = View.VISIBLE
                }
                R.id.nav_categories->{
                    searchView.visibility = View.VISIBLE
                }
            }
        }
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
                val intent = Intent(this, ShoppingCartActivity::class.java)
                startActivity(intent)
                true
            }

            R.id.nav_favourite -> {

                true
            }
            R.id.nav_settings->{

                true
            }
            else -> super.onOptionsItemSelected(item)

        }
    }
}