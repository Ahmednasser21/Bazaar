package com.iti.itp.bazaar.search.view

import ReceivedDraftOrder
import ReceivedOrdersResponse
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
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
import com.example.productinfoform_commerce.productInfo.viewModel.prouductInfoViewModel
import com.iti.itp.bazaar.auth.MyConstants
import com.iti.itp.bazaar.databinding.FragmentSearchBinding
import com.iti.itp.bazaar.dto.AppliedDiscount
import com.iti.itp.bazaar.dto.Customer
import com.iti.itp.bazaar.dto.DraftOrder
import com.iti.itp.bazaar.dto.DraftOrderRequest
import com.iti.itp.bazaar.dto.LineItem
import com.iti.itp.bazaar.dto.UpdateDraftOrderRequest
import com.iti.itp.bazaar.mainActivity.ui.DataState
import com.iti.itp.bazaar.network.exchangeCurrencyApi.CurrencyRemoteDataSource
import com.iti.itp.bazaar.network.exchangeCurrencyApi.ExchangeRetrofitObj
import com.iti.itp.bazaar.network.products.Products
import com.iti.itp.bazaar.network.responses.ProductResponse
import com.iti.itp.bazaar.network.shopify.ShopifyRemoteDataSource
import com.iti.itp.bazaar.network.shopify.ShopifyRetrofitObj
import com.iti.itp.bazaar.repo.CurrencyRepository
import com.iti.itp.bazaar.repo.Repository
import com.iti.itp.bazaar.search.OnCardClickListner
import com.iti.itp.bazaar.search.OnSearchProductFavClick
import com.iti.itp.bazaar.search.viewModel.SearchViewModel
import com.iti.itp.bazaar.search.viewModel.SearchViewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class SearchFragment : Fragment() ,OnCardClickListner , OnSearchProductFavClick{

lateinit var binding : FragmentSearchBinding
lateinit var vmFactory : SearchViewModelFactory
lateinit var searchViewModel: SearchViewModel
lateinit var searshAdapter: SearchAdapter
// shared viewModel to be used in the draft_Orders
    lateinit var ProductInfoViewModel : prouductInfoViewModel
    lateinit var DraftvmFActory : ProuductIfonViewModelFactory

    lateinit var sharedPreferences: SharedPreferences
    var draftOrderId:Long  = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        vmFactory = SearchViewModelFactory(Repository.getInstance(ShopifyRemoteDataSource(ShopifyRetrofitObj.productService)))
        searchViewModel = ViewModelProvider(requireActivity() ,vmFactory).get(SearchViewModel::class.java)

        // instance of draft_shared viewModel (which is ProductViewModel)
        DraftvmFActory = ProuductIfonViewModelFactory( Repository.getInstance(
            ShopifyRemoteDataSource(ShopifyRetrofitObj.productService)
        ) , CurrencyRepository(CurrencyRemoteDataSource(ExchangeRetrofitObj.service))
        )
        ProductInfoViewModel = ViewModelProvider(this , DraftvmFActory).get(prouductInfoViewModel::class.java)

        // sharedPref To Store FavDraftOrderId
        sharedPreferences =
            requireContext().getSharedPreferences(MyConstants.MY_SHARED_PREFERANCE, Context.MODE_PRIVATE)
        draftOrderId = (sharedPreferences.getString(MyConstants.FAV_DRAFT_ORDERS_ID,"0")?:"0").toLong()

        //////////////////////////////////////////////
        searshAdapter= SearchAdapter(this,this)
        binding = FragmentSearchBinding.inflate(inflater,container , false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.rvSearchProuducts.apply {
            adapter = searshAdapter
            layoutManager = GridLayoutManager(requireContext() , 2)
        }
        searchViewModel.getAllProducts()
        lifecycleScope.launch {
            searchViewModel.searchStateFlow.collectLatest { result->
                when(result){
                    DataState.Loading -> {}
                    is DataState.OnFailed -> {}
                    is DataState.OnSuccess<*> -> {
                        val allProuducts = result.data as ProductResponse
                     //   searshAdapter.submitList(allProuducts.products)
                        binding.svSearchbar .setOnQueryTextListener(object :
                            SearchView.OnQueryTextListener {
                            override fun onQueryTextSubmit(query: String?): Boolean {
                                return false
                            }
                            override fun onQueryTextChange(newText: String?): Boolean {
                                lifecycleScope.launch {
                                    if (newText.isNullOrEmpty()) {
                                        searshAdapter.submitList(emptyList())
                                    } else {
                                        val filteredList = withContext(Dispatchers.Default) {
                                            allProuducts.products.filter { item ->
                                                item.title.contains(newText, ignoreCase = true)
                                            }
                                        }
                                        searshAdapter.submitList(filteredList)
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


    override fun onCardClick(prduct: Products) {
        // navigate to product info Screen
       val action = SearchFragmentDirections.actionSearchFragmentToProuductnfoFragment(prduct.id)
        Navigation.findNavController(binding.root).navigate(action)
    }

    override fun onFavClick(prduct: Products) {

//        lifecycleScope.launch {
//
//            if (draftOrderId==0L )
//            {
//                ProductInfoViewModel.createOrder(draftOrderRequest(prduct))
//                Log.d("TAG", "onFavClick: click favorite fragment")
//                delay(2000) // tb be able to retrive my draftOrderId
//                ProductInfoViewModel.getAllDraftOrders()
//                ProductInfoViewModel.allDraftOrders.collectLatest { result->
//                    when(result){
//                        DataState.Loading -> {}
//                        is DataState.OnFailed ->{}
//                        is DataState.OnSuccess<*> ->{
//                            val draftOrder = (result.data as ReceivedOrdersResponse)
//                            val draftOrderId =draftOrder.draft_orders.get(draftOrder.draft_orders.size-1).id
//                             sharedPreferences.edit().putString(MyConstants.FAV_DRAFT_ORDERS_ID,"$draftOrderId").apply()
//                        }
//                    }
//
//                }
//
//            }
//            else {
//
//                ProductInfoViewModel.getSpecificDraftOrder(draftOrderId)
//                ProductInfoViewModel.specificDraftOrders.collectLatest { result->
//                    when (result){
//                        DataState.Loading -> {}
//                        is DataState.OnFailed ->{}
//                        is DataState.OnSuccess<*> ->{
//                            var OldDraftOrderReq = result.data as  DraftOrderRequest
//                            var currentDraftOrderItems : MutableList<LineItem> = mutableListOf() // to store my previous liked products
//                            OldDraftOrderReq.draft_order.line_items.forEach{
//                                currentDraftOrderItems.add(it) // getting the old list of line_items
//                            }
//                            // now i want to add this list to my new liked item
//                            currentDraftOrderItems.add(draftOrderRequest(prduct).draft_order.line_items.get(0))
//                            var updatedDraftOrder = draftOrderRequest(prduct).draft_order
//                            updatedDraftOrder.line_items =currentDraftOrderItems
//                            ProductInfoViewModel.updateDraftOrder(draftOrderId,UpdateDraftOrderRequest(updatedDraftOrder) )
//                        }
//                    }
//
//
//                }
//
//
//            }
//
//            lifecycleScope.launch(Dispatchers.IO) {
//                ProductInfoViewModel.createdOrder.collectLatest { result ->
//                    when (result ){
//                        DataState.Loading -> {}
//                        is DataState.OnFailed -> {}
//                        is DataState.OnSuccess<*> -> {
//                            val x = result.data as ReceivedDraftOrder
//
//                            Log.d("TAG", "onFavClick: w id el draft odre is ->${x.line_items?.get(0)?.title} ")
//
//                        }
//                    }
//
//
//
//                }
//
//            }
//
//
//
//        }


    }


private fun draftOrderRequest(prduct: Products ):DraftOrderRequest{
    val draftOrderRequest = DraftOrderRequest(
        draft_order = DraftOrder(
            line_items = listOf(
                LineItem(
                    id = prduct.id,
                    product_id = prduct.id,
                    sku = "${prduct.id.toString()}##${prduct.image?.src}",
                    title = prduct.title, price = prduct.variants[0].price, quantity = 1)
            ),
            use_customer_default_address = true,
            applied_discount = AppliedDiscount(),
            customer = Customer(8220771385648)
        )

    )
    return draftOrderRequest
}


}