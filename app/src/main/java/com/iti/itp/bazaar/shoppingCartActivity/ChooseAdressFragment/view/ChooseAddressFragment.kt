package com.iti.itp.bazaar.shoppingCartActivity.ChooseAdressFragment.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.iti.itp.bazaar.databinding.FragmentChooseAddressBinding
import com.iti.itp.bazaar.dto.CustomerAddress
import com.iti.itp.bazaar.dto.ListOfAddresses
import com.iti.itp.bazaar.mainActivity.ui.DataState
import com.iti.itp.bazaar.network.shopify.ShopifyRemoteDataSource
import com.iti.itp.bazaar.network.shopify.ShopifyRetrofitObj
import com.iti.itp.bazaar.repo.Repository
import com.iti.itp.bazaar.settings.ui.addressFragment.view.AddressAdapter
import com.iti.itp.bazaar.settings.ui.addressFragment.view.OnAddressClickListener
import com.iti.itp.bazaar.shoppingCartActivity.ChooseAdressFragment.viewModel.ChooseAddressViewModel
import com.iti.itp.bazaar.shoppingCartActivity.ChooseAdressFragment.viewModel.ChooseAddressViewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ChooseAddressFragment : Fragment(), OnAddressClickListener {
    private lateinit var binding:FragmentChooseAddressBinding
    private lateinit var chooseAddressViewModel: ChooseAddressViewModel
    private lateinit var factory: ChooseAddressViewModelFactory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        factory = ChooseAddressViewModelFactory(Repository.getInstance(ShopifyRemoteDataSource(ShopifyRetrofitObj.productService)))
        chooseAddressViewModel = ViewModelProvider(this,factory).get(ChooseAddressViewModel::class.java)
        binding = FragmentChooseAddressBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


    }

    override fun onStart() {
        super.onStart()
        binding.continueToPayment.setOnClickListener {
            val action = ChooseAddressFragmentDirections.actionChooseAddressFragmentToPaymentMethods()
            Navigation.findNavController(requireView()).navigate(action)
        }

        lifecycleScope.launch(Dispatchers.IO){
            chooseAddressViewModel.getAddressForCustomer(8220771418416)
            delay(1500)
            chooseAddressViewModel.addressesOfCustomer.collect{state ->
                when(state){
                    DataState.Loading -> {}
                    is DataState.OnFailed -> {}
                    is DataState.OnSuccess<*> -> {
                        val data = state.data as ListOfAddresses
                        withContext(Dispatchers.Main){
                            val defaultAddress = data.addresses.find {
                                it.default == true
                            }
                            binding.countryValue.text = defaultAddress?.country?:"unknown country"
                            binding.cityValue.text = defaultAddress?.city?: "unknown city"
                            binding.phoneValue.text = defaultAddress?.phone?: "unknown phone"
                        }
                    }
                }


            }
        }
    }

    override fun onAddressClick(customerAddress: CustomerAddress) {

    }
}