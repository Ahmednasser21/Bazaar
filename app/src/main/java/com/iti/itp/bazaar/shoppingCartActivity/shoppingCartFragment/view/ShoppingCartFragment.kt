package com.iti.itp.bazaar.shoppingCartActivity.shoppingCartFragment.view

import ReceivedDraftOrder
import ReceivedLineItem
import ReceivedOrdersResponse
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.iti.itp.bazaar.databinding.FragmentShoppingCartBinding
import com.iti.itp.bazaar.dto.AppliedDiscount
import com.iti.itp.bazaar.dto.Customer
import com.iti.itp.bazaar.dto.DraftOrder
import com.iti.itp.bazaar.dto.LineItem
import com.iti.itp.bazaar.dto.UpdateDraftOrder
import com.iti.itp.bazaar.dto.UpdateDraftOrderRequest
import com.iti.itp.bazaar.dto.UpdateLineItem
import com.iti.itp.bazaar.mainActivity.ui.DataState
import com.iti.itp.bazaar.network.shopify.ShopifyRemoteDataSource
import com.iti.itp.bazaar.network.shopify.ShopifyRetrofitObj
import com.iti.itp.bazaar.repo.Repository
import com.iti.itp.bazaar.shoppingCartActivity.shoppingCartFragment.viewModel.ShoppingCartFragmentViewModel
import com.iti.itp.bazaar.shoppingCartActivity.shoppingCartFragment.viewModel.ShoppingCartFragmentViewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.NumberFormat
import java.util.Locale

class ShoppingCartFragment : Fragment(), OnQuantityChangeListener {
    private lateinit var binding: FragmentShoppingCartBinding
    private lateinit var factory: ShoppingCartFragmentViewModelFactory
    private lateinit var shoppingCartViewModel: ShoppingCartFragmentViewModel
    private lateinit var firstDraftOrder: ReceivedDraftOrder
    private lateinit var adapter: ItemAdapter
    private val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.US)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        factory = ShoppingCartFragmentViewModelFactory(
            Repository.getInstance(ShopifyRemoteDataSource(ShopifyRetrofitObj.productService))
        )
        shoppingCartViewModel = ViewModelProvider(this, factory)[ShoppingCartFragmentViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentShoppingCartBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        observeCartData()
    }

    private fun setupUI() {
        binding.btnProceedToCheckout.setOnClickListener {
            updateDraftOrderToAPI()
        }

        adapter = ItemAdapter(this)
        binding.itemsRv.apply {
            adapter = this@ShoppingCartFragment.adapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun observeCartData() {
        lifecycleScope.launch(Dispatchers.IO) {
            shoppingCartViewModel.getAllDraftOrders()
            shoppingCartViewModel.allDraftOrders.collect { state ->
                withContext(Dispatchers.Main) {
                    handleCartState(state)
                }
            }
        }
    }

    private fun handleCartState(state: DataState) {
        when (state) {
            is DataState.Loading -> showLoading()
            is DataState.OnFailed -> handleError()
            is DataState.OnSuccess<*> -> handleSuccess(state.data as ReceivedOrdersResponse)
        }
    }

    private fun showLoading() {
        binding.apply {
            progressBar.visibility = View.VISIBLE
            itemsRv.visibility = View.GONE
            tvTotalPriceValue.visibility = View.GONE
            btnProceedToCheckout.visibility = View.GONE
        }
    }

    private fun handleError() {
        binding.apply {
            progressBar.visibility = View.GONE
            itemsRv.visibility = View.GONE
            tvTotalPriceValue.visibility = View.GONE
            btnProceedToCheckout.visibility = View.GONE
        }
        Toast.makeText(requireContext(), "Failed to load cart", Toast.LENGTH_SHORT).show()
    }

    private fun handleSuccess(data: ReceivedOrdersResponse) {
        binding.apply {
            progressBar.visibility = View.GONE
            itemsRv.visibility = View.VISIBLE
            tvTotalPriceValue.visibility = View.VISIBLE
            btnProceedToCheckout.visibility = View.VISIBLE

        }

        // Check if draft_orders is not empty
        if (data.draft_orders.isNotEmpty()) {
            firstDraftOrder = data.draft_orders[0]
            updateCartUI()
        } else {
            // Handle the case where there are no draft orders
            Toast.makeText(requireContext(), "No items in the cart", Toast.LENGTH_SHORT).show()
            // Optionally hide the cart UI or show a message
            binding.itemsRv.visibility = View.GONE
            binding.tvTotalPriceValue.visibility = View.GONE
            binding.btnProceedToCheckout.visibility = View.GONE
        }
    }


    private fun updateCartUI() {
        val totalPrice = calculateTotalPrice()
        binding.tvTotalPriceValue.text = currencyFormatter.format(totalPrice)
        adapter.submitList(firstDraftOrder.line_items?.toMutableList())
    }

    private fun calculateTotalPrice(): Double {
        return firstDraftOrder.line_items?.sumOf { item ->
            // Calculate unit price by dividing the total price by quantity
            val unitPrice = item.price.toDouble() / (item.quantity ?: 1)
            unitPrice * (item.quantity ?: 1)
        } ?: 0.0
    }

    override fun onQuantityChanged(item: ReceivedLineItem, newQuantity: Int, newPrice: Double) {
        val updatedLineItems = firstDraftOrder.line_items?.map {
            if (it.id == item.id) {
                // Calculate the unit price from the original item
                val unitPrice = it.price.toDouble() / (it.quantity ?: 1)
                it.copy(
                    quantity = newQuantity,
                    price = (unitPrice * newQuantity).toString(),
                    title = it.title ?: "Unknown Title",
                    variant_title = it.variant_title ?: "Default Variant Title"
                )
            } else {
                it
            }
        }?.toMutableList()

        firstDraftOrder.line_items = updatedLineItems
        updateCartUI()
    }

    private fun updateDraftOrderToAPI() {
        val updatedLineItems = firstDraftOrder.line_items?.map { item ->
            LineItem(
                variant_id = item.variant_id,
                product_id = item.product_id,
                quantity = item.quantity ?: 1,
                title = item.title!!,
                price = item.price,
                variant_title = item.variant_title,
                sku = item.sku,
                vendor = item.vendor,
                requires_shipping = item.requires_shipping,
                taxable = item.taxable,
                gift_card = item.gift_card,
                fulfillment_service = item.fulfillment_service,
                grams = item.grams,
                properties = item.properties,
                custom = item.custom,
                admin_graphql_api_id = item.admin_graphql_api_id
            )
        } ?: emptyList()

        val updateRequest = UpdateDraftOrderRequest(
            draft_order = DraftOrder(
                applied_discount = AppliedDiscount(null),
                customer = Customer(84986816),
                use_customer_default_address = true,
                line_items = updatedLineItems
            )
        )

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                shoppingCartViewModel.updateDraftOrder(firstDraftOrder.id, updateRequest)
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Cart updated successfully", Toast.LENGTH_SHORT).show()
                    Navigation.findNavController(requireView())
                        .navigate(ShoppingCartFragmentDirections.actionShoppingCartFragmentToChooseAddressFragment())
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e("ShoppingCart", "Failed to update cart", e)
                    Toast.makeText(requireContext(), "Failed to update cart", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}