package com.iti.itp.bazaar.search.view

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import androidx.recyclerview.widget.GridLayoutManager
import com.example.productinfoform_commerce.productInfo.viewModel.ProuductIfonViewModelFactory
import com.example.productinfoform_commerce.productInfo.viewModel.ProductInfoViewModel
import com.iti.itp.bazaar.auth.MyConstants
import com.iti.itp.bazaar.databinding.FragmentSearchBinding
import com.iti.itp.bazaar.dto.AppliedDiscount
import com.iti.itp.bazaar.dto.Customer
import com.iti.itp.bazaar.dto.DraftOrder
import com.iti.itp.bazaar.dto.DraftOrderRequest
import com.iti.itp.bazaar.dto.LineItem
import com.iti.itp.bazaar.mainActivity.ui.DataState
import com.iti.itp.bazaar.mainActivity.ui.products.OnFavouriteClickListener
import com.iti.itp.bazaar.mainActivity.ui.products.OnProductClickListener
import com.iti.itp.bazaar.mainActivity.ui.products.ProductsAdapter
import com.iti.itp.bazaar.network.exchangeCurrencyApi.CurrencyRemoteDataSource
import com.iti.itp.bazaar.network.exchangeCurrencyApi.ExchangeRetrofitObj
import com.iti.itp.bazaar.network.products.Products
import com.iti.itp.bazaar.network.responses.ProductResponse
import com.iti.itp.bazaar.network.shopify.ShopifyRemoteDataSource
import com.iti.itp.bazaar.network.shopify.ShopifyRetrofitObj
import com.iti.itp.bazaar.repo.CurrencyRepository
import com.iti.itp.bazaar.repo.Repository
import com.iti.itp.bazaar.search.viewModel.SearchViewModel
import com.iti.itp.bazaar.search.viewModel.SearchViewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class SearchFragment : Fragment(), OnFavouriteClickListener, OnProductClickListener {

    private lateinit var binding: FragmentSearchBinding
    private lateinit var vmFactory: SearchViewModelFactory
    private lateinit var searchViewModel: SearchViewModel
    private lateinit var searchAdapter: ProductsAdapter
    private lateinit var productInfoViewModel: ProductInfoViewModel
    private lateinit var draftViewModelFactory: ProuductIfonViewModelFactory

    private lateinit var sharedPreferences: SharedPreferences
    var draftOrderId: Long = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        vmFactory =
            SearchViewModelFactory(Repository.getInstance(ShopifyRemoteDataSource(ShopifyRetrofitObj.productService)))
        searchViewModel =
            ViewModelProvider(requireActivity(), vmFactory).get(SearchViewModel::class.java)

        // instance of draft_shared viewModel (which is ProductViewModel)
        draftViewModelFactory = ProuductIfonViewModelFactory(
            Repository.getInstance(
                ShopifyRemoteDataSource(ShopifyRetrofitObj.productService)
            ), CurrencyRepository(CurrencyRemoteDataSource(ExchangeRetrofitObj.service))
        )
        productInfoViewModel =
            ViewModelProvider(this, draftViewModelFactory).get(ProductInfoViewModel::class.java)

        // sharedPref To Store FavDraftOrderId
        sharedPreferences =
            requireContext().getSharedPreferences(
                MyConstants.MY_SHARED_PREFERANCE,
                Context.MODE_PRIVATE
            )
        draftOrderId =
            (sharedPreferences.getString(MyConstants.FAV_DRAFT_ORDERS_ID, "0") ?: "0").toLong()

        //////////////////////////////////////////////
        searchAdapter = ProductsAdapter(false, this, this)
        binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.rvSearchProuducts.apply {
            adapter = searchAdapter
            layoutManager = GridLayoutManager(requireContext(), 2)
        }
        searchViewModel.getAllProducts()
        binding.svSearchbar.setOnClickListener {
            binding.svSearchbar.isIconified = false
            binding.svSearchbar.requestFocusFromTouch ()
        }
        lifecycleScope.launch {
            searchViewModel.searchStateFlow.collectLatest { result ->
                when (result) {
                    DataState.Loading -> {}
                    is DataState.OnFailed -> {}
                    is DataState.OnSuccess<*> -> {
                        val allProuducts = result.data as ProductResponse
                        searchAdapter.submitList(allProuducts.products)
                        binding.svSearchbar.setOnQueryTextListener(object :
                            SearchView.OnQueryTextListener {
                            override fun onQueryTextSubmit(query: String?): Boolean {
                                return false
                            }

                            override fun onQueryTextChange(newText: String?): Boolean {
                                lifecycleScope.launch {
                                    if (newText.isNullOrBlank()) {
                                        binding.emptySearchAnimation.visibility = View.GONE
                                        binding.rvSearchProuducts.visibility = View.VISIBLE
                                        searchAdapter.submitList(allProuducts.products)
                                    } else {
                                        val filteredList = withContext(Dispatchers.Main) {
                                            allProuducts.products.filter { item ->
                                                extractProductName(item.title).startsWith(
                                                    newText.trim(),
                                                    ignoreCase = true
                                                )
                                            }
                                        }
                                        if (filteredList.isEmpty()) {
                                            binding.rvSearchProuducts.visibility = View.INVISIBLE
                                            binding.emptySearchAnimation.visibility = View.VISIBLE
                                        } else {
                                            binding.emptySearchAnimation.visibility = View.GONE
                                            binding.rvSearchProuducts.visibility = View.VISIBLE
                                        }
                                        searchAdapter.submitList(filteredList)
                                    }
                                }
                                return true
                            }
                        })

                    }
                }

            }
        }


    }

    private fun extractProductName(fullName: String): String {
        val delimiter = "|"
        val parts = fullName.split(delimiter)
        return if (parts.size > 1) parts[1].trim() else ""
    }


    private fun draftOrderRequest(prduct: Products): DraftOrderRequest {
        val draftOrderRequest = DraftOrderRequest(
            draft_order = DraftOrder(
                line_items = listOf(
                    LineItem(
                        id = prduct.id,
                        product_id = prduct.id,
                        sku = "${prduct.id.toString()}##${prduct.image?.src}",
                        title = prduct.title, price = prduct.variants[0].price, quantity = 1
                    )
                ),
                use_customer_default_address = true,
                applied_discount = AppliedDiscount(
                    null,
                    value_type = null,
                    value = null,
                    amount = null,
                    title = null
                ),
                customer = Customer(8220771385648)
            )

        )
        return draftOrderRequest
    }

    override fun onProductClick(id: Long) {
        val action = SearchFragmentDirections.actionSearchFragmentToProuductnfoFragment(id)
        Navigation.findNavController(binding.root).navigate(action)
    }

//    override fun onFavProductClick() {
//        //        lifecycleScope.launch {
////
////            if (draftOrderId==0L )
////            {
////                ProductInfoViewModel.createOrder(draftOrderRequest(prduct))
////                Log.d("TAG", "onFavClick: click favorite fragment")
////                delay(2000) // tb be able to retrive my draftOrderId
////                ProductInfoViewModel.getAllDraftOrders()
////                ProductInfoViewModel.allDraftOrders.collectLatest { result->
////                    when(result){
////                        DataState.Loading -> {}
////                        is DataState.OnFailed ->{}
////                        is DataState.OnSuccess<*> ->{
////                            val draftOrder = (result.data as ReceivedOrdersResponse)
////                            val draftOrderId =draftOrder.draft_orders.get(draftOrder.draft_orders.size-1).id
////                             sharedPreferences.edit().putString(MyConstants.FAV_DRAFT_ORDERS_ID,"$draftOrderId").apply()
////                        }
////                    }
////
////                }
////
////            }
////            else {
////
////                ProductInfoViewModel.getSpecificDraftOrder(draftOrderId)
////                ProductInfoViewModel.specificDraftOrders.collectLatest { result->
////                    when (result){
////                        DataState.Loading -> {}
////                        is DataState.OnFailed ->{}
////                        is DataState.OnSuccess<*> ->{
////                            var OldDraftOrderReq = result.data as  DraftOrderRequest
////                            var currentDraftOrderItems : MutableList<LineItem> = mutableListOf() // to store my previous liked products
////                            OldDraftOrderReq.draft_order.line_items.forEach{
////                                currentDraftOrderItems.add(it) // getting the old list of line_items
////                            }
////                            // now i want to add this list to my new liked item
////                            currentDraftOrderItems.add(draftOrderRequest(prduct).draft_order.line_items.get(0))
////                            var updatedDraftOrder = draftOrderRequest(prduct).draft_order
////                            updatedDraftOrder.line_items =currentDraftOrderItems
////                            ProductInfoViewModel.updateDraftOrder(draftOrderId,UpdateDraftOrderRequest(updatedDraftOrder) )
////                        }
////                    }
////
////
////                }
////
////
////            }
////
////            lifecycleScope.launch(Dispatchers.IO) {
////                ProductInfoViewModel.createdOrder.collectLatest { result ->
////                    when (result ){
////                        DataState.Loading -> {}
////                        is DataState.OnFailed -> {}
////                        is DataState.OnSuccess<*> -> {
////                            val x = result.data as ReceivedDraftOrder
////
////                            Log.d("TAG", "onFavClick: w id el draft odre is ->${x.line_items?.get(0)?.title} ")
////
////                        }
////                    }
////
////
////
////                }
////
////            }
////
////
////
////        }
//
//    }

    override fun onFavProductClick(product: Products) {
    }


}