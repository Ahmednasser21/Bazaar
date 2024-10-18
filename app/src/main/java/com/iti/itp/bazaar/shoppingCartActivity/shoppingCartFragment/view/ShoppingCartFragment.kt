package com.iti.itp.bazaar.shoppingCartActivity.shoppingCartFragment.view

import ReceivedOrdersResponse
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.iti.itp.bazaar.databinding.FragmentShoppingCartBinding
import com.iti.itp.bazaar.mainActivity.ui.DataState
import com.iti.itp.bazaar.network.shopify.ShopifyRemoteDataSource
import com.iti.itp.bazaar.network.shopify.ShopifyRetrofitObj
import com.iti.itp.bazaar.repo.Repository
import com.iti.itp.bazaar.shoppingCartActivity.shoppingCartFragment.viewModel.ShoppingCartFragmentViewModel
import com.iti.itp.bazaar.shoppingCartActivity.shoppingCartFragment.viewModel.ShoppingCartFragmentViewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class ShoppingCartFragment : Fragment() {
    private lateinit var binding:FragmentShoppingCartBinding
    private lateinit var factory:ShoppingCartFragmentViewModelFactory
    private lateinit var shoppingCartViewModel:ShoppingCartFragmentViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        factory = ShoppingCartFragmentViewModelFactory(
            Repository.getInstance(ShopifyRemoteDataSource(ShopifyRetrofitObj.productService))
        )
        shoppingCartViewModel = ViewModelProvider(this,factory)[ShoppingCartFragmentViewModel::class.java]
        binding = FragmentShoppingCartBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnProceedToCheckout.setOnClickListener{
            Navigation.findNavController(view).navigate(ShoppingCartFragmentDirections.actionShoppingCartFragmentToChooseAddressFragment())
        }

        lifecycleScope.launch(Dispatchers.IO){
            shoppingCartViewModel.getAllDraftOrders()
            shoppingCartViewModel.allDraftOrders.collect{state ->
                withContext(Dispatchers.Main){
                    when(state){
                        is DataState.Loading -> {
                            binding.progressBar.visibility = View.VISIBLE
                            binding.itemsRv.visibility = View.GONE
                            binding.tvTotalPriceValue.visibility = View.GONE
                            binding.btnProceedToCheckout.visibility = View.GONE
                        }
                        is DataState.OnFailed -> {
                            binding.progressBar.visibility = View.GONE
                            binding.itemsRv.visibility = View.GONE
                            binding.tvTotalPriceValue.visibility = View.GONE
                            binding.btnProceedToCheckout.visibility = View.GONE
                        }
                        is DataState.OnSuccess<*> ->{
                            binding.progressBar.visibility = View.GONE
                            binding.itemsRv.visibility = View.VISIBLE
                            binding.tvTotalPriceValue.visibility = View.VISIBLE
                            binding.btnProceedToCheckout.visibility = View.VISIBLE
                            val data = state.data as  ReceivedOrdersResponse
                            val firstDraftOrder = data.draft_orders[0]
                            val adapter = ItemAdapter()
                            binding.itemsRv.apply {
                                this.adapter = adapter
                                this.layoutManager = LinearLayoutManager(requireContext())
                            }
                            adapter.submitList(firstDraftOrder.line_items?.toMutableList())
                        }
                    }
                }
            }
            }

        }

    }