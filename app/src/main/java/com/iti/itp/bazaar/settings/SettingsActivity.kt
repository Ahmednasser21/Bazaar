package com.iti.itp.bazaar.settings

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.iti.itp.bazaar.R
import com.iti.itp.bazaar.databinding.FragmentSettingsBinding

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        findNavController(R.id.nav_host_fragment_activity_settings)
    }
}