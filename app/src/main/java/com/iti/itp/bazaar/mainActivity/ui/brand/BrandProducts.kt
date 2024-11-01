package com.iti.itp.bazaar.mainActivity.ui.brand

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.productinfoform_commerce.productInfo.viewModel.ProductInfoViewModel
import com.example.productinfoform_commerce.productInfo.viewModel.ProuductIfonViewModelFactory
import com.google.android.material.snackbar.Snackbar
import com.iti.itp.bazaar.R
import com.iti.itp.bazaar.auth.AuthActivity
import com.iti.itp.bazaar.auth.MyConstants
import com.iti.itp.bazaar.databinding.FragmentBrandProductsBinding
import com.iti.itp.bazaar.dto.DraftOrderRequest
import com.iti.itp.bazaar.handlers.FavoritesHandler
import com.iti.itp.bazaar.mainActivity.ui.DataState
import com.iti.itp.bazaar.mainActivity.ui.products.ProductsAdapter
import com.iti.itp.bazaar.mainActivity.ui.products.OnFavouriteClickListener
import com.iti.itp.bazaar.mainActivity.ui.products.OnProductClickListener
import com.iti.itp.bazaar.network.exchangeCurrencyApi.CurrencyRemoteDataSource
import com.iti.itp.bazaar.network.exchangeCurrencyApi.ExchangeRetrofitObj
import com.iti.itp.bazaar.network.products.Products
import com.iti.itp.bazaar.network.responses.ProductResponse
import com.iti.itp.bazaar.network.shopify.ShopifyRemoteDataSource
import com.iti.itp.bazaar.network.shopify.ShopifyRetrofitObj
import com.iti.itp.bazaar.repo.CurrencyRepository
import com.iti.itp.bazaar.repo.Repository
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

private const val TAG = "BrandProducts"

class BrandProducts : Fragment(), OnProductClickListener, OnFavouriteClickListener {
    private lateinit var productRecycler: RecyclerView
    private lateinit var brandProductsViewModel: BrandProductsViewModel
    private lateinit var binding: FragmentBrandProductsBinding
    private lateinit var productsAdapter: ProductsAdapter
    private lateinit var progBrandProducts: ProgressBar
    private lateinit var productInfoViewModel: ProductInfoViewModel
    private lateinit var sharedPrefs: SharedPreferences
    private lateinit var favoritesHandler: FavoritesHandler
    private var customerId: String? = null
    private var favoriteDraftOrderId: String? = null
    private var draftOrder: DraftOrderRequest? = null
    private lateinit var isGuestMode: String


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val productInfoViewModelFactory = ProuductIfonViewModelFactory(
            Repository.getInstance(
                ShopifyRemoteDataSource(ShopifyRetrofitObj.productService)
            ),
            CurrencyRepository(CurrencyRemoteDataSource(ExchangeRetrofitObj.service))
        )
        val factory = BrandProductViewModelFactory(
            Repository.getInstance(
                ShopifyRemoteDataSource(ShopifyRetrofitObj.productService)
            )
        )
        productInfoViewModel = ViewModelProvider(this, productInfoViewModelFactory)[ProductInfoViewModel::class.java]
        brandProductsViewModel =
            ViewModelProvider(this, factory)[BrandProductsViewModel::class.java]
        sharedPrefs = requireActivity().getSharedPreferences(MyConstants.MY_SHARED_PREFERANCE, Context.MODE_PRIVATE)
        isGuestMode = sharedPrefs.getString(MyConstants.IS_GUEST, "false") ?: "false"
        binding = FragmentBrandProductsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val args: BrandProductsArgs by navArgs()
        val brandName = args.vendorName
        productsAdapter = ProductsAdapter(false,this,this)
        initialiseUI()
        customerId = sharedPrefs.getString(MyConstants.CUSOMER_ID, "0")
        favoriteDraftOrderId = sharedPrefs.getString(MyConstants.FAV_DRAFT_ORDERS_ID, "0")
        favoritesHandler = FavoritesHandler(productInfoViewModel, sharedPrefs)
        brandProductsViewModel.getVendorProducts(brandName)
        getVendorProducts()
        lifecycleScope.launch {
            favoritesHandler.initialize()
        }
    }

    private fun initialiseUI() {
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

    override fun onProductClick(id: Long) {
        val action = BrandProductsDirections.actionNavBrandProductsToProuductnfoFragment(id)
        Navigation.findNavController(binding.root).navigate(action)
    }

    override fun onFavProductClick(product: Products) {
        if (isGuestMode == "true") {
            Snackbar.make(
                requireView(),
                "Signup first to use this feature",
                Snackbar.LENGTH_LONG
            ).setAction("Signup") {
                val intent = Intent(requireActivity(), AuthActivity::class.java)
                intent.putExtra("navigateToFragment", "SignUpFragment")
                startActivity(intent)
            }.setActionTextColor(ContextCompat.getColor(requireContext(), R.color.primaryColor))
                .show()
        } else {
            favoritesHandler.addProductToFavorites(
                product = product,
                onAdded = {
                    Snackbar.make(requireView(), "Added to favorites", Snackbar.LENGTH_SHORT).show()
                },
                onAlreadyExists = {
                    favoritesHandler.removeFromFavorites(
                        product = product,
                        onRemoved = {
                            Snackbar.make(
                                requireView(),
                                "Removed from favorites",
                                Snackbar.LENGTH_SHORT
                            ).show()
                        }
                    )
                }
            )

        }
    }
    suspend fun getSpecificDraftOrder() {
        productInfoViewModel.getSpecificDraftOrder(favoriteDraftOrderId?.toLong() ?: 0)
        productInfoViewModel.specificDraftOrders.collect {
            when (it) {
                DataState.Loading -> {}
                is DataState.OnFailed -> {}
                is DataState.OnSuccess<*> -> {
                    draftOrder = it.data as DraftOrderRequest
                }
            }
        }
    }

}
