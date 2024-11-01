package com.iti.itp.bazaar.favoriteProducts.view

import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import androidx.recyclerview.widget.GridLayoutManager
import com.example.productinfoform_commerce.productInfo.viewModel.ProuductIfonViewModelFactory
import com.example.productinfoform_commerce.productInfo.viewModel.ProductInfoViewModel
import com.google.android.material.snackbar.Snackbar
import com.iti.itp.bazaar.auth.MyConstants
import com.iti.itp.bazaar.databinding.FragmentFavoriteProductsBinding
import com.iti.itp.bazaar.dto.DraftOrderRequest
import com.iti.itp.bazaar.dto.LineItem
import com.iti.itp.bazaar.dto.UpdateDraftOrderRequest
import com.iti.itp.bazaar.favoriteProducts.OnFavProductCardClick
import com.iti.itp.bazaar.favoriteProducts.OnFavProductDelete
import com.iti.itp.bazaar.mainActivity.ui.DataState
import com.iti.itp.bazaar.network.exchangeCurrencyApi.CurrencyRemoteDataSource
import com.iti.itp.bazaar.network.exchangeCurrencyApi.ExchangeRetrofitObj
import com.iti.itp.bazaar.network.shopify.ShopifyRemoteDataSource
import com.iti.itp.bazaar.network.shopify.ShopifyRetrofitObj
import com.iti.itp.bazaar.repo.CurrencyRepository
import com.iti.itp.bazaar.repo.Repository
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class FavoriteProductsFragment : Fragment(), OnFavProductCardClick, OnFavProductDelete {
    private lateinit var productInfoViewModel: ProductInfoViewModel
    private lateinit var binding: FragmentFavoriteProductsBinding
    private lateinit var favAdapter: FavoriteProductsAdapter
    private lateinit var sharedPreferences: SharedPreferences
    private var favDraftOrderId: Long = 0
    private var favDraftOrder: DraftOrderRequest? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFavoriteProductsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initSharedPreferences()

        setupViewModel()

        setupRecyclerView()

        observeViewModelStates()

        loadDraftOrder()
    }

    private fun initSharedPreferences() {
        sharedPreferences = requireContext().getSharedPreferences(
            MyConstants.MY_SHARED_PREFERANCE,
            Context.MODE_PRIVATE
        )
        favDraftOrderId = (sharedPreferences.getString(MyConstants.FAV_DRAFT_ORDERS_ID, "0") ?: "0").toLong()
    }

    private fun setupViewModel() {
        val vmFactory = ProuductIfonViewModelFactory(
            Repository.getInstance(ShopifyRemoteDataSource(ShopifyRetrofitObj.productService)),
            CurrencyRepository(CurrencyRemoteDataSource(ExchangeRetrofitObj.service))
        )
        productInfoViewModel = ViewModelProvider(requireActivity(), vmFactory)
            .get(ProductInfoViewModel::class.java)
    }

    private fun setupRecyclerView() {
        favAdapter = FavoriteProductsAdapter(this, this)
        binding.rvFavProducts.apply {
            adapter = favAdapter
            layoutManager = GridLayoutManager(requireContext(), 2)
        }
    }

    private fun observeViewModelStates() {
        lifecycleScope.launch {
            productInfoViewModel.specificDraftOrders.collectLatest { result ->
                handleDraftOrderResult(result)
            }
        }

        lifecycleScope.launch {
            productInfoViewModel.updatedOrder.collectLatest { dataState ->
                handleOrderUpdateResult(dataState)
            }
        }
    }

    private fun loadDraftOrder() {
        if (favDraftOrderId == 0L) {
            showEmptyState("There are No Favorite Products to Display")
            return
        }

        binding.progressBar4.visibility = View.VISIBLE
        binding.rvFavProducts.visibility = View.GONE
        binding.emptyFavAnimation.visibility = View.GONE

        productInfoViewModel.getSpecificDraftOrder(favDraftOrderId)
    }

    private fun handleDraftOrderResult(result: DataState) {
        when (result) {
            DataState.Loading -> {
                binding.progressBar4.visibility = View.VISIBLE
                binding.rvFavProducts.visibility = View.GONE
                binding.emptyFavAnimation.visibility = View.GONE
            }
            is DataState.OnFailed -> {
                binding.progressBar4.visibility = View.GONE
                showEmptyState("Failed to load favorite products")
            }
            is DataState.OnSuccess<*> -> {
                binding.progressBar4.visibility = View.GONE

                favDraftOrder = result.data as DraftOrderRequest
                updateFavAdapter(favDraftOrder!!)
            }
        }
    }

    private fun updateFavAdapter(draftOrder: DraftOrderRequest) {
        val lineItems = draftOrder.draft_order.line_items.filter { it.sku != "emptySKU" }

        if (lineItems.isEmpty()) {
            showEmptyState("No favorite products")
        } else {
            binding.emptyFavAnimation.visibility = View.GONE
            binding.rvFavProducts.visibility = View.VISIBLE
            favAdapter.submitList(lineItems)
        }
    }

    private fun handleOrderUpdateResult(dataState: DataState) {
        when (dataState) {
            is DataState.Loading -> {
                binding.progressBar4.visibility = View.VISIBLE
                binding.rvFavProducts.visibility = View.GONE
                binding.emptyFavAnimation.visibility = View.GONE
            }
            is DataState.OnSuccess<*> -> {
                loadDraftOrder()
            }
            is DataState.OnFailed -> {
                binding.progressBar4.visibility = View.GONE
                binding.rvFavProducts.visibility = View.VISIBLE
                Snackbar.make(requireView(), "Failed to update favorites", Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    private fun showEmptyState(message: String) {
        Snackbar.make(requireView(), message, Snackbar.LENGTH_SHORT).show()
        binding.progressBar4.visibility = View.GONE
        binding.rvFavProducts.visibility = View.GONE
        binding.emptyFavAnimation.visibility = View.VISIBLE
    }

    override fun onFavDelete(lineItem: LineItem) {
        AlertDialog.Builder(context)
            .setTitle("Confirm Item Delete")
            .setMessage("Are you sure you want to delete this item from your favourites?")
            .setPositiveButton("Yes") { _, _ ->
                favDraftOrder?.let { currentDraftOrder ->
                    val updatedLineItems = currentDraftOrder.draft_order.line_items.toMutableList()
                    updatedLineItems.remove(lineItem)


                    if (updatedLineItems.isEmpty()) {
                        updatedLineItems.add(lineItem.copy(sku = "emptySKU"))
                    }

                    currentDraftOrder.draft_order.line_items = updatedLineItems

                    productInfoViewModel.updateDraftOrder(
                        favDraftOrderId,
                        UpdateDraftOrderRequest(currentDraftOrder.draft_order)
                    )
                }
            }
            .setNegativeButton("No") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    override fun onCardClick(productId: Long) {
        val action = FavoriteProductsFragmentDirections
            .actionFavoriteProductsFragmentToProuductnfoFragment(productId)
        Navigation.findNavController(binding.root).navigate(action)
    }
}