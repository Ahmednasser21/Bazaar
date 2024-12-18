package com.iti.itp.bazaar.mainActivity.newAddressFragment.view

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import com.iti.itp.bazaar.auth.MyConstants
import com.iti.itp.bazaar.databinding.FragmentNewAddressBinding
import com.iti.itp.bazaar.dto.AddAddressResponse
import com.iti.itp.bazaar.dto.CustomerAddress
import com.iti.itp.bazaar.network.shopify.ShopifyRemoteDataSource
import com.iti.itp.bazaar.network.shopify.ShopifyRetrofitObj
import com.iti.itp.bazaar.repo.Repository
import com.iti.itp.bazaar.mainActivity.newAddressFragment.viewModel.NewAddressViewModel
import com.iti.itp.bazaar.mainActivity.newAddressFragment.viewModel.NewAddressViewModelFactory

class NewAddressFragment : Fragment() {
    private lateinit var binding: FragmentNewAddressBinding
    private lateinit var newAddressViewModel: NewAddressViewModel
    private lateinit var draftOrderSharedPreferences: SharedPreferences
    private var customerId:String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val factory = NewAddressViewModelFactory(
            Repository.getInstance(
                ShopifyRemoteDataSource(ShopifyRetrofitObj.productService)
            )
        )
        draftOrderSharedPreferences = requireContext().getSharedPreferences(MyConstants.MY_SHARED_PREFERANCE, Context.MODE_PRIVATE)
        newAddressViewModel = ViewModelProvider(this, factory).get(NewAddressViewModel::class.java)
        binding = FragmentNewAddressBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        customerId = draftOrderSharedPreferences.getString(MyConstants.CUSOMER_ID, "0")
        Log.i("TAG", "onViewCreated: $customerId")

        binding.btnAddAddress.setOnClickListener {
            if (binding.etCity.text.trim().isEmpty() || binding.governorate.text.trim()
                    .isEmpty() || binding.etPhone.text.trim().isEmpty()
                || binding.streetName.text.trim().isEmpty() || binding.buildingNumber.text.trim()
                    .isEmpty() || binding.lastName.text.trim().isEmpty()
                || binding.firstName.text.trim().isEmpty()
            ) {
                Snackbar.make(requireView(), "All fields are required", 2000).show()
            } else {
                val address = AddAddressResponse(
                    CustomerAddress(
                        id = customerId?.toLong() ?: 0,
                        customer_id = customerId?.toLong() ?: 0,
                        first_name = "${binding.firstName.text}",
                        last_name = "${binding.lastName.text}",
                        company = "ahmed's company",
                        address1 = "${binding.streetName.text}",
                        address2 = "${binding.buildingNumber.text}",
                        city = "${binding.governorate.text}, ${binding.etCity.text}",
                        province = null,
                        country = "",
                        zip = "11511",
                        phone = binding.etPhone.text.toString(),
                        name = "${binding.firstName.text} ${binding.lastName.text}",
                        province_code = null,
                        country_code = "EG",
                        country_name = binding.nonEditable.text.toString(),
                        default = false,
                    )
                )
                newAddressViewModel.addNewAddress(customerId?.toLong() ?: 0, address)
                binding.etCity.text.clear()
                binding.governorate.text.clear()
                binding.etPhone.text.clear()
                binding.streetName.text.clear()
                binding.buildingNumber.text.clear()
                binding.lastName.text.clear()
                binding.firstName.text.clear()
                Snackbar.make(requireView(), "Address is added", 2000).show()
            }
        }

    }
}