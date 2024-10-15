package com.iti.itp.bazaar.shoppingCartActivity.paypalFragment

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.iti.itp.bazaar.databinding.FragmentPayPalBinding

class PayPalFragment : Fragment() {
    private lateinit var binding: FragmentPayPalBinding
    private val TAG = "PayPalFragment"

    // PayPal Configuration
    private val clientId = "AYF_7hasq1akGkaUz04HUmMoC-Iplw7jPfoLFuEFQzfqsB3rfGQCqUw5ZcWVGY5cO0LMGjFCooWunB5N"
    private val clientSecret = "EEiwJFW8iAefjIcgWYfq9hWtLHOg2OCn-YFy-TFhlaGD2qzIkz4Hu3hmFnJPGrSNLefrQwREeaaMI9c5"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentPayPalBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }



}