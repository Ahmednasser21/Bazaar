package com.iti.itp.bazaar.mainActivity.ui.order

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.google.android.material.snackbar.Snackbar
import com.iti.itp.bazaar.databinding.FragmentOrderBinding
import com.iti.itp.bazaar.mainActivity.ui.DataState
import com.iti.itp.bazaar.network.responses.OrdersResponse
import com.iti.itp.bazaar.network.shopify.ShopifyRemoteDataSource
import com.iti.itp.bazaar.network.shopify.ShopifyRetrofitObj
import com.iti.itp.bazaar.repo.Repository
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

private const val TAG = "OrderFragment"

class OrderFragment : Fragment() {

    private lateinit var binding: FragmentOrderBinding
    private lateinit var orderViewModel: OrderViewModel
    private lateinit var ordersRecycler: RecyclerView
    private lateinit var ordersProg: ProgressBar
    private lateinit var emptyAnimation: LottieAnimationView
    private lateinit var orderAdapter: OrdersAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val factory = OrderViewModelFactory(
            Repository.getInstance(
                ShopifyRemoteDataSource(ShopifyRetrofitObj.productService)
            )
        )
        val customerID = arguments?.let { OrderFragmentArgs.fromBundle(it).customerID }
        orderViewModel = ViewModelProvider(requireActivity(), factory)[OrderViewModel::class.java]
        orderViewModel.getOrdersByCustomerID(customerID.toString())
        orderAdapter = OrdersAdapter()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentOrderBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initialiseUI()
        getOrderItems()

    }

    private fun initialiseUI() {
        ordersRecycler = binding.recOrders.apply {
            adapter = orderAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
        ordersProg = binding.progOrders
        emptyAnimation = binding.emptyOrdersAnimation
    }

    private fun getOrderItems() {
        lifecycleScope.launch {
            orderViewModel.ordersStateFlow.collectLatest { result ->
                when (result) {
                    is DataState.Loading -> {
                        ordersRecycler.visibility = View.INVISIBLE
                        ordersProg.visibility = View.VISIBLE
                    }

                    is DataState.OnSuccess<*> -> {
                        ordersProg.visibility = View.GONE
                        ordersRecycler.visibility = View.VISIBLE
                        val ordersResponse = result.data as OrdersResponse
                        val ordersList = ordersResponse.orders
                        Log.i(TAG, "getOrders:${ordersList}")
                        if (ordersList.isEmpty()) {
                            emptyAnimation.visibility = View.VISIBLE
                        } else {
                            emptyAnimation.visibility = View.GONE
                        }
                        orderAdapter.submitList(ordersList)

                    }

                    is DataState.OnFailed -> {
                        ordersProg.visibility = View.GONE
                        Snackbar.make(requireView(), "Failed to get data", Snackbar.LENGTH_SHORT)
                            .show()
                    }
                }
            }
        }
    }
}