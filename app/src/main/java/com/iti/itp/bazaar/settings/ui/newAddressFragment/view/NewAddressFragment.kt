package com.iti.itp.bazaar.settings.ui.newAddressFragment.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import com.iti.itp.bazaar.databinding.FragmentNewAddressBinding
import com.iti.itp.bazaar.dto.AddedAddressRequest
import com.iti.itp.bazaar.dto.AddedCustomerAddress
import com.iti.itp.bazaar.network.shopify.ShopifyRemoteDataSource
import com.iti.itp.bazaar.network.shopify.ShopifyRetrofitObj
import com.iti.itp.bazaar.repo.Repository
import com.iti.itp.bazaar.settings.ui.newAddressFragment.viewModel.NewAddressViewModel
import com.iti.itp.bazaar.settings.ui.newAddressFragment.viewModel.NewAddressViewModelFactory

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
                val addedCustomerAddress = AddedCustomerAddress(
                    address1 = "egypt1",
                    address2 = "ismailia1",
                    city = "fayed1",
                    country = binding.country.text.toString(),
                    country_name = "Egypt1",
                    first_name = "ahmed1",
                    last_name = "samy1",
                    company = "esfd1",
                    phone = "01010095281",
                )
                val address = AddedAddressRequest(addedCustomerAddress)
                newAddressViewModel.addNewAddress(8220771221808,address)
            }
        }
    }
}