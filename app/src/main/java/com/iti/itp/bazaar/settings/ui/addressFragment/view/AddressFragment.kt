package com.iti.itp.bazaar.settings.ui.addressFragment.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.iti.itp.bazaar.databinding.FragmentAddressBinding
import com.iti.itp.bazaar.dto.CustomerAddress
import com.iti.itp.bazaar.dto.CustomerAddressResponse
import com.iti.itp.bazaar.dto.ListOfAddresses
import com.iti.itp.bazaar.mainActivity.ui.DataState
import com.iti.itp.bazaar.network.shopify.ShopifyRemoteDataSource
import com.iti.itp.bazaar.network.shopify.ShopifyRetrofitObj
import com.iti.itp.bazaar.repo.Repository
import com.iti.itp.bazaar.settings.ui.addressFragment.viewModel.AddressViewModel
import com.iti.itp.bazaar.settings.ui.addressFragment.viewModel.AddressViewModelFactory
import kotlinx.coroutines.launch

class AddressFragment : Fragment(), OnAddressClickListener {
    private lateinit var binding: FragmentAddressBinding
    private lateinit var factory: AddressViewModelFactory
    private lateinit var addressViewModel: AddressViewModel
    private lateinit var adapter: AddressAdapter
    private var isDeleting = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        factory = AddressViewModelFactory(Repository.getInstance(ShopifyRemoteDataSource(ShopifyRetrofitObj.productService)))
        addressViewModel = ViewModelProvider(this, factory)[AddressViewModel::class.java]
        binding = FragmentAddressBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        setupSwipeToDelete()
        observeAddresses()
    }

    private fun setupUI() {
        binding.btnAddNewAddresss.setOnClickListener {
            val action = AddressFragmentDirections.actionAddressFragmentToNewAddressFragment()
            Navigation.findNavController(it).navigate(action)
        }

        adapter = AddressAdapter(this)
        binding.addressRv.apply {
            adapter = this@AddressFragment.adapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun setupSwipeToDelete() {
        val swipeToDeleteCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val address = adapter.currentList[position] as CustomerAddress
                deleteAddress(address)
            }
        }
        ItemTouchHelper(swipeToDeleteCallback).attachToRecyclerView(binding.addressRv)
    }

    private fun deleteAddress(address: CustomerAddress) {
        if (isDeleting) {
            showMessage("Please wait for the previous delete operation to complete.")
            refreshAddressList()
            return
        }

        isDeleting = true
        lifecycleScope.launch {
            try {
                addressViewModel.deleteAddressForSpecificCustomer(8220771418416, address.id)
                showMessage("Address deleted successfully")
            } catch (e: Exception) {
                showMessage("Failed to delete address: ${e.message}")
            } finally {
                isDeleting = false
                refreshAddressList()
            }
        }
    }

    private fun refreshAddressList() {
        lifecycleScope.launch {
            addressViewModel.getAddressesForCustomer(8220771418416)
        }
    }

    private fun observeAddresses() {
        lifecycleScope.launch {
            addressViewModel.addresses.collect { state ->
                when (state) {
                    is DataState.Loading -> hideDataAndShowProgressbar()
                    is DataState.OnFailed -> {
                        hideData()
                        showMessage("Failed to load addresses: ${state.msg.message}")
                    }
                    is DataState.OnSuccess<*> -> {
                        showDataAndHideProgressbar()
                        val data = state.data as ListOfAddresses
                        adapter.submitList(data.addresses)
                    }
                }
            }
        }
    }

    private fun hideDataAndShowProgressbar() {
        binding.progressBar.visibility = View.VISIBLE
        binding.btnAddNewAddresss.visibility = View.GONE
        binding.addressRv.visibility = View.GONE
    }

    private fun hideData() {
        binding.progressBar.visibility = View.GONE
        binding.btnAddNewAddresss.visibility = View.GONE
        binding.addressRv.visibility = View.GONE
    }

    private fun showDataAndHideProgressbar() {
        binding.progressBar.visibility = View.GONE
        binding.btnAddNewAddresss.visibility = View.VISIBLE
        binding.addressRv.visibility = View.VISIBLE
    }

    private fun showMessage(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }

    override fun onAddressClick(customerAddress: CustomerAddress) {
        val newAddress = customerAddress.copy(default = true)
        val customerAddressResponse = CustomerAddressResponse(newAddress)
        addressViewModel.updateAddress(customerAddress.customer_id!!, customerAddress.id!!, customerAddressResponse)
    }

    override fun onStart() {
        super.onStart()
        refreshAddressList()
    }
}