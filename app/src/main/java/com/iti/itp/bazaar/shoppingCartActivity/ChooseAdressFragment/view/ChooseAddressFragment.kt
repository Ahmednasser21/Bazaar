package com.iti.itp.bazaar.shoppingCartActivity.ChooseAdressFragment.view

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.iti.itp.bazaar.databinding.FragmentChooseAddressBinding
import com.iti.itp.bazaar.dto.CustomerAddress
import com.iti.itp.bazaar.dto.ListOfAddresses
import com.iti.itp.bazaar.dto.OrderAddress
import com.iti.itp.bazaar.mainActivity.ui.DataState
import com.iti.itp.bazaar.mainActivity.ui.order.SharedOrderViewModel
import com.iti.itp.bazaar.network.shopify.ShopifyRemoteDataSource
import com.iti.itp.bazaar.network.shopify.ShopifyRetrofitObj
import com.iti.itp.bazaar.repo.Repository
import com.iti.itp.bazaar.settings.SettingsActivity
import com.iti.itp.bazaar.settings.ui.addressFragment.view.AddressAdapter
import com.iti.itp.bazaar.settings.ui.addressFragment.view.AddressFragment
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
    private val sharedOrderViewModel by activityViewModels<SharedOrderViewModel> ()

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

        binding.chooseAntoherAddress.setOnClickListener {
            val intent = Intent(requireActivity(), SettingsActivity::class.java)
            startActivity(intent)
        }

        lifecycleScope.launch(Dispatchers.IO){
            chooseAddressViewModel.getAddressForCustomer(8220771418416)
            delay(1500)
            chooseAddressViewModel.addressesOfCustomer.collect{state ->
                when(state){
                    DataState.Loading -> handleLoading()
                    is DataState.OnFailed -> {}
                    is DataState.OnSuccess<*> -> {
                        withContext(Dispatchers.Main){
                        handleSuccess()
                        val data = state.data as ListOfAddresses
                            val defaultAddress = data.addresses.find {
                                it.default == true
                            }
                            val orderAddress = OrderAddress(
                                first_name = defaultAddress?.first_name?:"",
                                last_name = defaultAddress?.last_name?:"",
                                address1 = defaultAddress?.address1?:"",
                                address2 = defaultAddress?.address2?:"",
                                city = defaultAddress?.city?:"",
                                province = defaultAddress?.province?:"",
                                country = defaultAddress?.country?:"",
                                zip = defaultAddress?.zip?:"",
                                phone = defaultAddress?.phone?:""
                            )
                            sharedOrderViewModel.updateBillingAddress(orderAddress)
                            sharedOrderViewModel.updateShippingAddress(orderAddress)
                            binding.countryValue.text = defaultAddress?.country?:"unknown country"
                            binding.cityValue.text = defaultAddress?.city?: "unknown city"
                            binding.phoneValue.text = defaultAddress?.phone?: "unknown phone"
                        }
                    }
                }


            }
        }
    }

    private fun handleLoading() {
        binding.progressBar.visibility = View.VISIBLE
        binding.addressCardView.visibility = View.GONE
        binding.country.visibility = View.GONE
        binding.city.visibility = View.GONE
        binding.phone.visibility = View.GONE
        binding.countryValue.visibility = View.GONE
        binding.cityValue.visibility = View.GONE
        binding.phoneValue.visibility = View.GONE
        binding.constraintAddress.visibility = View.GONE
    }

    private fun handleSuccess(){
        binding.progressBar.visibility = View.GONE
        binding.addressCardView.visibility = View.VISIBLE
        binding.country.visibility = View.VISIBLE
        binding.city.visibility = View.VISIBLE
        binding.phone.visibility = View.VISIBLE
        binding.countryValue.visibility = View.VISIBLE
        binding.cityValue.visibility = View.VISIBLE
        binding.phoneValue.visibility = View.VISIBLE
        binding.constraintAddress.visibility = View.VISIBLE
    }

    override fun onAddressClick(customerAddress: CustomerAddress) {

    }


}