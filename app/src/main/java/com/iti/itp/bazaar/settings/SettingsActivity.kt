package com.iti.itp.bazaar.settings

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.iti.itp.bazaar.R

class SettingsActivity : AppCompatActivity() {
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        // Initialize NavController
        navController = findNavController(R.id.nav_host_fragment_activity_settings)


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