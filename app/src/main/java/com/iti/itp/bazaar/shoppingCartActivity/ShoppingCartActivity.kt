package com.iti.itp.bazaar.shoppingCartActivity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.iti.itp.bazaar.R
import com.iti.itp.bazaar.databinding.ActivityShoppingCartBinding

class ShoppingCartActivity : AppCompatActivity() {
    private lateinit var binding: ActivityShoppingCartBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityShoppingCartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up the NavController
        navController = findNavController(R.id.nav_host_fragment_activity_shopping_cart)



    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent?.let {
            if (it.action == Intent.ACTION_VIEW) {
                val uri = it.data
                if (uri != null && uri.host == "paypalpay") {
                    // Navigate to PayPalFragment
                    val navController = findNavController(R.id.nav_host_fragment_activity_main)
                    navController.navigate(R.id.payPalFragment) // Ensure the ID is correct
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (navController.currentDestination?.id == navController.graph.startDestinationId) {
            // If we're at the start destination, finish the activity
            super.onBackPressed()
        } else {
            // Otherwise navigate up within the settings navigation graph
            navController.navigateUp()
        }
    }


}