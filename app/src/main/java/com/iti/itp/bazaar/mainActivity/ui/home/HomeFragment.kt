package com.iti.itp.bazaar.mainActivity.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.HORIZONTAL
import com.google.android.material.snackbar.Snackbar
import com.iti.itp.bazaar.R
import com.iti.itp.bazaar.databinding.FragmentHomeBinding
import com.iti.itp.bazaar.mainActivity.ui.DataState
import com.iti.itp.bazaar.network.shopify.ShopifyRemoteDataSource
import com.iti.itp.bazaar.network.shopify.ShopifyRetrofitObj
import com.iti.itp.bazaar.network.dto.Products
import com.iti.itp.bazaar.network.responses.ProductResponse
import com.iti.itp.bazaar.repo.Repository
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

private const val TAG = "HomeFragment"
class HomeFragment : Fragment() , OnBrandClickListener {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var homeViewModel: HomeViewModel
    private lateinit var brandsAdapter: BrandsAdapter
    private lateinit var brandsRecycler: RecyclerView
    private lateinit var brandsProgressBar:ProgressBar

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val factory = HomeViewModelFactory(
            Repository.getInstance(
                ShopifyRemoteDataSource(ShopifyRetrofitObj.productService)
            )
        )
        homeViewModel = ViewModelProvider(this, factory)[HomeViewModel::class.java]
        brandsAdapter = BrandsAdapter(this)
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        brandsRecycler = binding.recBrands.apply {
            adapter = brandsAdapter
            layoutManager = GridLayoutManager(requireContext(),2, HORIZONTAL,false)
        }
        brandsProgressBar = binding.progBrands
        homeViewModel.getVendors("vendor")
        getProductVendors()

    }

    private fun getProductVendors() {
        lifecycleScope.launch {
            homeViewModel.brandStateFlow.collectLatest { result ->

                when (result) {

                    is DataState.Loading -> {
                        brandsProgressBar.visibility = View.VISIBLE
                        brandsRecycler.visibility = View.INVISIBLE
                    }

                    is DataState.OnSuccess<*> ->{
                        brandsProgressBar.visibility = View.GONE
                        brandsRecycler.visibility = View.VISIBLE
                        val productResponse = result.data as ProductResponse
                        val productsList = productResponse.products
                        Log.i(TAG, "getProductVendors: $productsList ")
                        brandsAdapter.submitList(createBrandsList(productsList))

                    }
                    is DataState.OnFailed->{
                        brandsProgressBar.visibility = View.GONE
                        Snackbar.make(requireView(),"Failed to get data",Snackbar.LENGTH_SHORT).show()
                    }

                }

            }
        }
    }
    private fun createBrandsList(productsList: List<Products>): List<BrandsDTO> {
        return productsList
            .asSequence()
            .map { it.vendor }
            .distinct()
            .filter { it != "Your Vendor Name" }
            .map { vendorName ->
                BrandsDTO(
                    getImageResourceForVendor(vendorName),
                    vendorName
                )
            }
            .toList()
    }


    private fun getImageResourceForVendor(vendorName: String): Int {
        return when (vendorName) {
            "ADIDAS" -> R.drawable.adidas
            "ASICS TIGER" -> R.drawable.asics_tiger
            "Burton" -> R.drawable.burton
            "CONVERSE" -> R.drawable.converse
            "DR MARTENS" -> R.drawable.dr_martens
            "FLEX FIT" -> R.drawable.flexfit
            "HERSCHEL" -> R.drawable.herschel
            "NIKE" -> R.drawable.nike
            "PALLADIUM" -> R.drawable.palladium
            "PUMA" -> R.drawable.puma
            "SUPRA" -> R.drawable.supra
            "TIMBERLAND" -> R.drawable.timberland
            "VANS" -> R.drawable.vans
            else -> R.drawable.curved_brownish_background
        }
    }

    override fun onBrandClick(brandTitle: String) {
        val action = HomeFragmentDirections.actionNavHomeToBrandProducts(brandTitle)
        Navigation.findNavController(requireView()).navigate(action)
    }

}