package com.iti.itp.bazaar.settings.ui.newAddressFragment.view

import android.content.Context
import android.content.SharedPreferences
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
import com.iti.itp.bazaar.auth.MyConstants
import com.iti.itp.bazaar.databinding.FragmentNewAddressBinding
import com.iti.itp.bazaar.dto.AddAddressResponse
import com.iti.itp.bazaar.dto.AddedAddressRequest
import com.iti.itp.bazaar.dto.AddedCustomerAddress
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
//        val customerAddress = AddedCustomerAddress(
//            address1 = "${binding.governorate.text}",
//            address2 = "${binding.etCity.text}",
//            city = "${binding.governorate.text}",
//            country_name = "${binding.nonEditable.text}",
//            first_name = "ahmed1",
//            last_name = "samy1",
//            company = "esfd1",
//            phone = "01010095281",
//            country_code = "EG"
//        )
//        val address = AddedAddressRequest(customerAddress)



        binding.btnAddAddress.setOnClickListener {
            val address = AddAddressResponse(
                CustomerAddress(
                    id = customerId?.toLong()?:0,
                    customer_id = customerId?.toLong()?:0,
                    first_name = "ahmed",
                    last_name = "samy",
                    company = "ahmed's company",
                    address1 = binding.etCity.text.toString(),
                    address2 = null,
                    city = binding.etCity.text.toString(),
                    province = null,
                    country = binding.governorate.text.toString(),
                    zip = "11511",
                    phone = binding.etPhone.text.toString(),
                    name = "ahmed samy",
                    province_code = null,
                    country_code = "EG",
                    country_name = binding.nonEditable.text.toString(),
                    default = false,
                )
            )
            newAddressViewModel.addNewAddress(customerId?.toLong()?:0,address)
            binding.etCity.text.clear()
            binding.governorate.text.clear()
            binding.etPhone.text.clear()
            Snackbar.make(requireView(),"Address is added", 2000).show()
        }


    }
}