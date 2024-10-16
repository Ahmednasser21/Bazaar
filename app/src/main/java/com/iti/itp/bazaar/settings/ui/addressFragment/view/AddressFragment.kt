package com.iti.itp.bazaar.settings.ui.addressFragment.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.iti.itp.bazaar.databinding.FragmentAddressBinding
import com.iti.itp.bazaar.dto.ListOfAddresses
import com.iti.itp.bazaar.mainActivity.ui.DataState
import com.iti.itp.bazaar.network.shopify.ShopifyRemoteDataSource
import com.iti.itp.bazaar.network.shopify.ShopifyRetrofitObj
import com.iti.itp.bazaar.repo.Repository
import com.iti.itp.bazaar.settings.ui.addressFragment.viewModel.AddressViewModel
import com.iti.itp.bazaar.settings.ui.addressFragment.viewModel.AddressViewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class AddressFragment : Fragment() {
    private lateinit var binding:FragmentAddressBinding
    private lateinit var factory:AddressViewModelFactory
    private lateinit var addressViewModel:AddressViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        factory = AddressViewModelFactory(Repository.getInstance(ShopifyRemoteDataSource(ShopifyRetrofitObj.productService)))
        addressViewModel = ViewModelProvider(this,factory).get(AddressViewModel::class.java)
        binding = FragmentAddressBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnAddNewAddresss.setOnClickListener {
            val action = AddressFragmentDirections.actionAddressFragmentToNewAddressFragment()
            Navigation.findNavController(view).navigate(action)
        }
    }

    override fun onStart() {
        super.onStart()
        lifecycleScope.launch(Dispatchers.IO) {
            addressViewModel.getAddressesForCustomer(8220771418416)
            withContext(Dispatchers.Main){
                addressViewModel.addresses.collect{State->
                    when(State){
                        is DataState.Loading -> hideDataAndShowProgressbar()
                        is DataState.OnFailed -> hideData()
                        is DataState.OnSuccess<*> ->{
                            showDataAndHideProgressbar()
                            val data = State.data as  ListOfAddresses
                            val adapter = AddressAdapter()
                            binding.addressRv.apply {
                                this.adapter = adapter
                                this.layoutManager = LinearLayoutManager(requireContext())
                            }
                            adapter.submitList(data.addresses)
                        }
                    }
                }
            }
        }
    }

    private fun hideDataAndShowProgressbar(){
        binding.progressBar.visibility = View.VISIBLE
        binding.btnAddNewAddresss.visibility = View.GONE
        binding.addressRv.visibility = View.GONE
    }

    private fun hideData(){
        binding.progressBar.visibility = View.GONE
        binding.btnAddNewAddresss.visibility = View.GONE
        binding.addressRv.visibility = View.GONE
    }

    private fun showDataAndHideProgressbar(){
        binding.progressBar.visibility = View.GONE
        binding.btnAddNewAddresss.visibility = View.VISIBLE
        binding.addressRv.visibility = View.VISIBLE
    }

}