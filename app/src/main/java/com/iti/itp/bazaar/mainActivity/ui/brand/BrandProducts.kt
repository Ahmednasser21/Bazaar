package com.iti.itp.bazaar.mainActivity.ui.brand

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.iti.itp.bazaar.databinding.FragmentBrandProductsBinding
import com.iti.itp.bazaar.mainActivity.ui.DataState
import com.iti.itp.bazaar.network.responses.ProductResponse
import com.iti.itp.bazaar.network.shopify.ShopifyRemoteDataSource
import com.iti.itp.bazaar.network.shopify.ShopifyRetrofitObj
import com.iti.itp.bazaar.repo.Repository
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

private const val TAG = "BrandProducts"

class BrandProducts : Fragment(), OnBrandProductClickListener, OnFavouriteClickListener {
    private lateinit var brandTitle: TextView
    private lateinit var productRecycler: RecyclerView
    private lateinit var brandProductsViewModel: BrandProductsViewModel
    private lateinit var binding: FragmentBrandProductsBinding
    private lateinit var productsAdapter: BrandProductsAdapter
    private lateinit var progBrandProducts: ProgressBar

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val factory = BrandProductViewModelFactory(
            Repository.getInstance(
                ShopifyRemoteDataSource(ShopifyRetrofitObj.productService)
            )
        )
        brandProductsViewModel =
            ViewModelProvider(this, factory)[BrandProductsViewModel::class.java]
        binding = FragmentBrandProductsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val args: BrandProductsArgs by navArgs()
        val brandName = args.vendorName
        Log.i(TAG, "onViewCreated: $brandName")

        productsAdapter = BrandProductsAdapter(this,this)
        initialiseUI(brandName)
        brandProductsViewModel.getVendorProducts(brandName)
        getVendorProducts()
    }

    private fun initialiseUI(brandName: String) {
        brandTitle = binding.brandName.apply {
            text = brandName
        }
        progBrandProducts = binding.progBrandProducts
        productRecycler = binding.recBrandProducts.apply {
            adapter = productsAdapter
            layoutManager = GridLayoutManager(requireContext(), 2)
        }
    }

    private fun getVendorProducts() {
        lifecycleScope.launch {
            brandProductsViewModel.productStateFlow.collectLatest { result ->
                when (result) {
                    is DataState.Loading -> {
                        progBrandProducts.visibility = View.VISIBLE
                        productRecycler.visibility = View.INVISIBLE
                    }

                    is DataState.OnSuccess<*> -> {
                        progBrandProducts.visibility = View.GONE
                        productRecycler.visibility = View.VISIBLE
                        val productResponse = result.data as ProductResponse
                        val productsList = productResponse.products
                        Log.i(TAG, "getProductVendors:${productsList}")
                        if (productsList.isEmpty()) {
                            binding.emptyBoxAnimationFav.visibility = View.VISIBLE
                        }
                        productsAdapter.submitList(productsList)
                    }

                    is DataState.OnFailed -> {
                        progBrandProducts.visibility = View.GONE
                        Snackbar.make(requireView(), "Failed to get data", Snackbar.LENGTH_SHORT)
                            .show()
                    }
                }
            }
        }
    }

    override fun onBrandProductClick(productID: Long) {
// navigate to prouductDetails fragment using args with productID
        val action = BrandProductsDirections.actionNavBrandProductsToProuductnfoFragment(productID)
        Navigation.findNavController(binding.root).navigate(action)
    }

    override fun onFavClick() {

    }
}
