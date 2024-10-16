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
import com.google.android.material.snackbar.Snackbar
import com.iti.itp.bazaar.databinding.FragmentNewAddressBinding
import com.iti.itp.bazaar.dto.AddressRequest
import com.iti.itp.bazaar.dto.CustomerAddress
import com.iti.itp.bazaar.mainActivity.ui.DataState
import com.iti.itp.bazaar.network.shopify.ShopifyRemoteDataSource
import com.iti.itp.bazaar.network.shopify.ShopifyRetrofitObj
import com.iti.itp.bazaar.repo.Repository
import com.iti.itp.bazaar.settings.ui.newAddressFragment.viewModel.NewAddressViewModel
import com.iti.itp.bazaar.settings.ui.newAddressFragment.viewModel.NewAddressViewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NewAddressFragment : Fragment() {
    private lateinit var binding: FragmentNewAddressBinding
    private lateinit var newAddressViewModel: NewAddressViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val factory = NewAddressViewModelFactory(
            Repository.getInstance(
                ShopifyRemoteDataSource(ShopifyRetrofitObj.productService)
            )
        )
        newAddressViewModel = ViewModelProvider(this, factory).get(NewAddressViewModel::class.java)
        binding = FragmentNewAddressBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)




        binding.btnAddAddress.setOnClickListener {
            if (binding.country.text.isEmpty() || binding.etCity.text.isEmpty() || binding.etPhone.text.isEmpty()){
                Snackbar.make(view,"All fields must be required",2000).show()
            }else{
                val customerAddress = CustomerAddress(
                    city = binding.etCity.text.toString(),
                    country = binding.country.text.toString(),
                    phone = binding.etPhone.text.toString(),
                )
                val address = AddressRequest(customerAddress)
                newAddressViewModel.addNewAddress(8220771352880, address)
            }
        }
    }
}