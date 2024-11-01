package com.iti.itp.bazaar.favoriteProducts.view

import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


class FavoriteProductsFragment : Fragment(), OnFavProductCardClick, OnFavProductDelete {
    // will try to sue it as a shared viewmodel
    lateinit var productInfoViewModel: ProductInfoViewModel
    lateinit var vmFActory: ProuductIfonViewModelFactory
    lateinit var binding: FragmentFavoriteProductsBinding
    lateinit var FavAdapter: FavoriteProductsAdapter
    lateinit var sharedPreferences: SharedPreferences
    lateinit var FavDraftOrder: DraftOrderRequest
    var favDraftOrderId: Long = 0


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentFavoriteProductsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sharedPreferences = requireContext().getSharedPreferences(
            MyConstants.MY_SHARED_PREFERANCE,
            Context.MODE_PRIVATE
        )
        favDraftOrderId =
            (sharedPreferences.getString(MyConstants.FAV_DRAFT_ORDERS_ID, "0") ?: "0").toLong()

        vmFActory = ProuductIfonViewModelFactory(
            Repository.getInstance(
                ShopifyRemoteDataSource(ShopifyRetrofitObj.productService)
            ), CurrencyRepository(CurrencyRemoteDataSource(ExchangeRetrofitObj.service))
        )
        productInfoViewModel =
            ViewModelProvider(requireActivity(), vmFActory).get(ProductInfoViewModel::class.java)

        FavAdapter = FavoriteProductsAdapter(this, this)
        binding.rvFavProducts.apply {
            adapter = FavAdapter
            layoutManager = GridLayoutManager(requireContext(), 2)

        }


        lifecycleScope.launch {
            if (favDraftOrderId == 0L) {
                Snackbar.make(requireView(), "There is No Favorite Products to Display", 2000)
                    .show()
            } else {
                productInfoViewModel.getSpecificDraftOrder(favDraftOrderId)
                productInfoViewModel.specificDraftOrders.collectLatest { result ->
                    when (result) {
                        DataState.Loading -> {
                            binding.rvFavProducts.visibility = View.INVISIBLE
                            binding.progressBar4.visibility = View.VISIBLE
                        }
                        is DataState.OnFailed -> {binding.progressBar4.visibility = View.GONE
                            binding.rvFavProducts.visibility = View.VISIBLE}
                        is DataState.OnSuccess<*> -> {
                            binding.progressBar4.visibility = View.GONE
                            binding.rvFavProducts.visibility = View.VISIBLE
                            // i must make this list golbal to use it in the delete click to delete the targted lineItm from the list of lineItems in This List
                            FavDraftOrder = result.data as DraftOrderRequest
                            // print this list here ASAP
                            var lineItems: MutableList<LineItem> = mutableListOf()
                            FavDraftOrder.draft_order.line_items.forEach {
                                if (it.sku != "emptySKU") {
                                    lineItems.add(it) // containes every eleemnts exepts the last item which is updated sku to Aa
                                    // tb be able to delete it
                                }
                            }
                            if (lineItems.isNullOrEmpty()) {
                                binding.rvFavProducts.visibility = View.INVISIBLE
                                binding.emptyFavAnimation.visibility = View.VISIBLE
                            } else {
                                binding.emptyFavAnimation.visibility = View.GONE
                                binding.rvFavProducts.visibility = View.VISIBLE
                                FavAdapter.submitList(lineItems)
                            }


                        }
                    }


                }

            }

        }

    }

    override fun onCardClick(productId: Long) {
        val action =
            FavoriteProductsFragmentDirections.actionFavoriteProductsFragmentToProuductnfoFragment(
                productId
            )
        Navigation.findNavController(binding.root).navigate(action)
    }

    override fun onFavDelete(lineItem: LineItem) {

        AlertDialog.Builder(context)
            .setTitle("Confirm item Delete")
            .setMessage("Are you sure that you want to delete this Item from your favourites?")
            .setPositiveButton("Yes") { dialog, _ ->
                lifecycleScope.launch {
                    if (FavDraftOrder.draft_order.line_items.size > 1) {
                        Log.d("TAG", "onFavDelete: just came ")
                        var currentDraftOrderItems: MutableList<LineItem> = mutableListOf()
                        FavDraftOrder.draft_order.line_items.forEach {

                            currentDraftOrderItems.add(it)
                            Log.d("TAG", "onFavDelete: 3mlt add ${currentDraftOrderItems.size} ")
                            if (it == lineItem) {
                                Log.d(
                                    "TAG",
                                    "onFavDelete: d5alt el if >- ezan el it == pressed line item  "
                                )
                                currentDraftOrderItems.remove(it)// kda 3addelt el list of line items
                                Log.d(
                                    "TAG",
                                    "onFavDelete: hal 3mlt delete walla la2? new size ->> ${currentDraftOrderItems.size} "
                                )
                            } else {
                                Log.d(
                                    "TAG",
                                    "onFavDelete: d5alt el else  >- ezan el it != pressed line item  "
                                )
                            }


                        }
                        FavDraftOrder.draft_order.line_items = currentDraftOrderItems
                        productInfoViewModel.updateDraftOrder(
                            favDraftOrderId,
                            UpdateDraftOrderRequest(FavDraftOrder.draft_order)
                        )
                        binding.progressBar4.visibility = View.VISIBLE
                        binding.rvFavProducts.visibility = View.INVISIBLE
                        delay(1000) // to give time for changes ( delating and updating the list ) to take action
                        // to call the list again after its modified
                        binding.progressBar4.visibility = View.GONE
                        binding.rvFavProducts.visibility = View.VISIBLE
                        productInfoViewModel.getSpecificDraftOrder(favDraftOrderId)
                    } else {

                        FavDraftOrder.draft_order.line_items.get(0).sku =
                            "emptySKU" // make sku of remainig item to be Aa
                        productInfoViewModel.updateDraftOrder(
                            favDraftOrderId,
                            UpdateDraftOrderRequest(FavDraftOrder.draft_order)
                        )
                        binding.progressBar4.visibility = View.VISIBLE
                        binding.rvFavProducts.visibility = View.INVISIBLE
                        delay(1000)
                        binding.progressBar4.visibility = View.VISIBLE
                        binding.rvFavProducts.visibility = View.INVISIBLE
                        productInfoViewModel.getSpecificDraftOrder(favDraftOrderId)
                    }
                }
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
            .show()


    }


}