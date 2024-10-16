package com.iti.itp.bazaar.settings.ui.newAddressFragment.view

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.iti.itp.bazaar.databinding.FragmentNewAddressBinding
import com.iti.itp.bazaar.dto.CustomerAddress
import com.iti.itp.bazaar.mainActivity.ui.DataState
import com.iti.itp.bazaar.network.shopify.ShopifyRemoteDataSource
import com.iti.itp.bazaar.network.shopify.ShopifyRetrofitObj
import com.iti.itp.bazaar.repo.Repository
import com.iti.itp.bazaar.settings.ui.newAddressFragment.viewModel.NewAddressViewModel
import com.iti.itp.bazaar.settings.ui.newAddressFragment.viewModel.NewAddressViewModelFactory
import kotlinx.coroutines.launch

class NewAddressFragment : Fragment() {
    private lateinit var binding: FragmentNewAddressBinding
    private lateinit var newAddressViewModel: NewAddressViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val factory = NewAddressViewModelFactory(Repository.getInstance(ShopifyRemoteDataSource(ShopifyRetrofitObj.productService)))
        newAddressViewModel = ViewModelProvider(this, factory).get(NewAddressViewModel::class.java)
        binding = FragmentNewAddressBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lifecycleScope.launch {
            newAddressViewModel.addressState.collect { state ->
                when (state) {
                    is DataState.Loading -> { /* Show loading indicator */ }
                    is DataState.OnSuccess<*> -> {
                        // Handle successful address creation
                        val newAddress = state.data as CustomerAddress
                        Log.i("TAG", "onViewCreated: success")
                        // Update UI or navigate
                    }
                    is DataState.OnFailed -> {
                        // Show error message
                        Toast.makeText(requireContext(), state.msg.message, Toast.LENGTH_LONG).show()
                    }
                }
            }
        }

        binding.btnAddAddress.setOnClickListener {
            val customerAddress = CustomerAddress(22,22, country_name = "egypt")
            newAddressViewModel.addNewAddress(22,customerAddress)
        }

    }
}