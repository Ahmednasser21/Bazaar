package com.iti.itp.bazaar.auth

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import com.iti.itp.bazaar.R
import com.iti.itp.bazaar.auth.view.SignUpFragment


class AuthActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_auth)

        var navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main)
        val navigateToFragment = intent.getStringExtra("navigateToFragment")
        if (navigateToFragment == "SignUpFragment") {
            navController.navigate(R.id.signUpFragment)


        }
    }

}