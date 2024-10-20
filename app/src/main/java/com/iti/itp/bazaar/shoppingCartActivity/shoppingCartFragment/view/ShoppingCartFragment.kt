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
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.iti.itp.bazaar.databinding.FragmentShoppingCartBinding
import com.iti.itp.bazaar.dto.AppliedDiscount
import com.iti.itp.bazaar.dto.order.Customer
import com.iti.itp.bazaar.dto.DraftOrder
import com.iti.itp.bazaar.dto.order.LineItem
import com.iti.itp.bazaar.dto.UpdateDraftOrderRequest
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
            is DataState.OnFailed -> handleError(state.msg.message)
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

    private fun handleError(message: String?) {
        binding.apply {
            progressBar.visibility = View.GONE
            itemsRv.visibility = View.GONE
            tvTotalPriceValue.visibility = View.GONE
            btnProceedToCheckout.visibility = View.GONE
        }
        Toast.makeText(requireContext(), message ?: "Failed to load cart", Toast.LENGTH_SHORT).show()
    }

    private fun handleSuccess(data: ReceivedOrdersResponse) {
        binding.apply {
            progressBar.visibility = View.GONE
            if (data.draft_orders.isNotEmpty()) {
                itemsRv.visibility = View.VISIBLE
                tvTotalPriceValue.visibility = View.VISIBLE
                btnProceedToCheckout.visibility = View.VISIBLE
                firstDraftOrder = data.draft_orders[0]
                updateCartUI()
            } else {
                showEmptyCart()
            }
        }
    }

    private fun showEmptyCart() {
        binding.apply {
            itemsRv.visibility = View.GONE
            tvTotalPriceValue.visibility = View.GONE
            btnProceedToCheckout.visibility = View.GONE
        }
        Toast.makeText(requireContext(), "Your cart is empty", Toast.LENGTH_SHORT).show()
    }

    private fun updateCartUI() {
        val totalPrice = calculateTotalPrice()
        binding.tvTotalPriceValue.text = currencyFormatter.format(totalPrice)
        adapter.submitList(firstDraftOrder.line_items?.toMutableList())

        val swipeToDeleteCallback = SwipeToDelete(
            adapter = adapter,
            onDelete = { deletedItem ->
                // Remove the item from the local list
                val updatedLineItems = firstDraftOrder.line_items?.toMutableList() ?: mutableListOf()
                updatedLineItems.removeAll { it.id == deletedItem.id }

                // Update the UI immediately
                firstDraftOrder = firstDraftOrder.copy(line_items = updatedLineItems)
                adapter.submitList(updatedLineItems)

                // Recalculate and update the total price
                val newTotalPrice = calculateTotalPrice()
                binding.tvTotalPriceValue.text = currencyFormatter.format(newTotalPrice)

                // If the cart is now empty, show the empty cart UI
                if (updatedLineItems.isEmpty()) {
                    showEmptyCart()
                }

                // Perform the API call to sync with the server
                viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                    try {
                        val updateRequest = UpdateDraftOrderRequest(
                            DraftOrder(
                                line_items = updatedLineItems.map { item ->
                                    LineItem(
                                        id = item.id,
                                        product_id = item.product_id,
                                        title = item.title ?: "",
                                        price = item.price,
                                        quantity = item.quantity ?: 1,
                                        sku = item.sku
                                    )
                                },
                                applied_discount = AppliedDiscount(null),
                                customer = Customer(firstDraftOrder.customer?.id ?: 0),
                                use_customer_default_address = true
                            )
                        )

                        shoppingCartViewModel.updateDraftOrder(firstDraftOrder.id, updateRequest)

                        withContext(Dispatchers.Main) {
                            Toast.makeText(requireContext(), "Item deleted successfully", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                requireContext(),
                                "Failed to sync deletion with server: ${e.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                            Log.e("ShoppingCart", "Error syncing deletion", e)
                        }
                    }
                }

                Log.i("TAG", "updateCartUI: item deleted")
            }
        )

        ItemTouchHelper(swipeToDeleteCallback).attachToRecyclerView(binding.itemsRv)
    }

    private fun calculateTotalPrice(): Double {
        return firstDraftOrder.line_items?.sumOf { item ->
            val unitPrice = item.price.toDouble()
            unitPrice * (item.quantity ?: 1)
        } ?: 0.0
    }

    override fun onQuantityChanged(item: ReceivedLineItem, newQuantity: Int, newPrice: Double) {
        val updatedLineItems = firstDraftOrder.line_items?.map {
            if (it.id == item.id) {
                it.copy(
                    quantity = newQuantity,
                    price = newPrice.toString()
                )
            } else {
                it
            }
        }?.toMutableList()

        firstDraftOrder = firstDraftOrder.copy(line_items = updatedLineItems)
        updateCartUI()
    }

    private fun updateDraftOrderToAPI() {
        if (firstDraftOrder.line_items.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Cart is empty", Toast.LENGTH_SHORT).show()
            return
        }

        val updatedLineItems = firstDraftOrder.line_items?.map { item ->
            // Calculate the updated price based on quantity
            val basePrice = item.price.toDouble()
            val quantity = item.quantity ?: 1
            val totalPrice = (basePrice * quantity).toString()

            LineItem(
                variant_id = item.variant_id,
                product_id = item.product_id,
                quantity = quantity,
                price = totalPrice,  // Use the calculated total price
                title = item.title ?: "",
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

        val updateRequest = firstDraftOrder.customer?.let {
            Customer(it.id)
        }?.let {
            DraftOrder(
                line_items = updatedLineItems,
                customer = it,
                use_customer_default_address = true,
                applied_discount = null
            )
        }?.let {
            UpdateDraftOrderRequest(
                draft_order = it
            )
        }

        // Show loading state
        binding.btnProceedToCheckout.isEnabled = false
        binding.progressBar.visibility = View.VISIBLE

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                shoppingCartViewModel.updateDraftOrder(firstDraftOrder.id, updateRequest!!)

                withContext(Dispatchers.Main) {
                    handleUpdateSuccess()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    handleUpdateError(e)
                }
            }
        }
    }

    private fun handleUpdateSuccess() {
        binding.apply {
            btnProceedToCheckout.isEnabled = true
            progressBar.visibility = View.GONE
        }
        Toast.makeText(requireContext(), "Cart updated successfully", Toast.LENGTH_SHORT).show()
        Navigation.findNavController(requireView())
            .navigate(ShoppingCartFragmentDirections.actionShoppingCartFragmentToChooseAddressFragment())
    }

    private fun handleUpdateError(e: Exception) {
        binding.apply {
            btnProceedToCheckout.isEnabled = true
            progressBar.visibility = View.GONE
        }
        Log.e("ShoppingCart", "Failed to update cart: ${e.message}", e)
        Toast.makeText(
            requireContext(),
            "Failed to update cart: ${e.message ?: "Unknown error"}",
            Toast.LENGTH_SHORT
        ).show()
    }
}