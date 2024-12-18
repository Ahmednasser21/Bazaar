package com.iti.itp.bazaar.mainActivity.shoppingCartFragment.view

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
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
import com.google.android.material.snackbar.Snackbar
import com.iti.itp.bazaar.auth.MyConstants
import com.iti.itp.bazaar.databinding.FragmentShoppingCartBinding
import com.iti.itp.bazaar.dto.AppliedDiscount
import com.iti.itp.bazaar.dto.Customer
import com.iti.itp.bazaar.dto.DraftOrder
import com.iti.itp.bazaar.dto.DraftOrderRequest
import com.iti.itp.bazaar.dto.LineItem
import com.iti.itp.bazaar.dto.PriceRuleDto
import com.iti.itp.bazaar.dto.UpdateDraftOrderRequest
import com.iti.itp.bazaar.mainActivity.DataState
import com.iti.itp.bazaar.network.responses.PriceRulesResponse
import com.iti.itp.bazaar.network.shopify.ShopifyRemoteDataSource
import com.iti.itp.bazaar.network.shopify.ShopifyRetrofitObj
import com.iti.itp.bazaar.repo.Repository
import com.iti.itp.bazaar.mainActivity.shoppingCartFragment.viewModel.ShoppingCartFragmentViewModel
import com.iti.itp.bazaar.mainActivity.shoppingCartFragment.viewModel.ShoppingCartFragmentViewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.abs

class ShoppingCartFragment : Fragment(), OnQuantityChangeListener {
    private lateinit var binding: FragmentShoppingCartBinding
    private lateinit var factory: ShoppingCartFragmentViewModelFactory
    private lateinit var shoppingCartViewModel: ShoppingCartFragmentViewModel
    private lateinit var firstDraftOrder: DraftOrder
    private lateinit var adapter: ItemAdapter
    private val currencyFormatter = NumberFormat.getNumberInstance(Locale.US)
    private lateinit var currencySharedPreferences: SharedPreferences
    private lateinit var draftOrderSharedPreferences: SharedPreferences
    private var customerId: String? = null
    private var draftOrderId: String? = null
    private lateinit var priceRules: PriceRulesResponse
    private var matchingRule: PriceRuleDto? = null
    private var appliedDiscountPercentage: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        factory = ShoppingCartFragmentViewModelFactory(
            Repository.getInstance(ShopifyRemoteDataSource(ShopifyRetrofitObj.productService))
        )
        shoppingCartViewModel =
            ViewModelProvider(this, factory)[ShoppingCartFragmentViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        draftOrderSharedPreferences = requireActivity().getSharedPreferences(
            MyConstants.MY_SHARED_PREFERANCE,
            Context.MODE_PRIVATE
        )
        currencySharedPreferences = requireActivity().applicationContext.getSharedPreferences(
            "currencySharedPrefs",
            Context.MODE_PRIVATE
        )
        binding = FragmentShoppingCartBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        draftOrderId = draftOrderSharedPreferences.getString(MyConstants.CART_DRAFT_ORDER_ID, "0")
        customerId = draftOrderSharedPreferences.getString(MyConstants.CUSOMER_ID, "0")
        observePriceRules()
        getPriceRules()
        setupUI()
        observeCartData()
    }

    private fun setupUI() {
        binding.btnProceedToCheckout.setOnClickListener {
            if (firstDraftOrder.line_items.size <= 1) {
                Snackbar.make(requireView(), "Your cart is empty", 2000).show()
            } else {
                updateDraftOrderToAPI()
            }
        }

        adapter = ItemAdapter(this)
        binding.itemsRv.apply {
            adapter = this@ShoppingCartFragment.adapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun observeCartData() {
        lifecycleScope.launch(Dispatchers.IO) {
            shoppingCartViewModel.getSpecificDraftOrder(draftOrderId?.toLong() ?: 0)
            shoppingCartViewModel.specificDraftOrder.collect { state ->
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
            is DataState.OnSuccess<*> -> handleSuccess(state.data as DraftOrderRequest)
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
        Toast.makeText(requireContext(), message ?: "Failed to load cart", Toast.LENGTH_SHORT)
            .show()
    }

    private fun handleSuccess(data: DraftOrderRequest) {
        binding.apply {
            progressBar.visibility = View.GONE
            if (data.draft_order.line_items.isNotEmpty()) {
                itemsRv.visibility = View.VISIBLE
                tvTotalPriceValue.visibility = View.VISIBLE
                btnProceedToCheckout.visibility = View.VISIBLE
                firstDraftOrder = data.draft_order
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


    private fun calculateDiscountedTotal(): Double {
        val currencyRate = currencySharedPreferences.getFloat("currency", 1F)
        return firstDraftOrder.line_items?.sumOf { item ->
            val basePrice = item.price.toDoubleOrNull() ?: 0.0
            val quantity = item.quantity ?: 1
            val itemTotal = basePrice * quantity * currencyRate
            val discountAmount = (itemTotal * appliedDiscountPercentage) / 100
            itemTotal - discountAmount
        } ?: 0.0
    }


    private fun calculateDiscountedPrice(basePrice: Double): Double {
        return if (appliedDiscountPercentage > 0) {
            val discountAmount = (basePrice * appliedDiscountPercentage) / 100
            basePrice - discountAmount
        } else {
            basePrice
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun updateCartUI() {
        val currencyRate = currencySharedPreferences.getFloat("currency", 1F)
        val baseTotal = calculateTotalPrice()
        binding.tvTotalPriceValue.text = baseTotal.toString()

        binding.couponsImageButton.setOnClickListener {
            val couponCode = binding.couponsEditText.text.toString().trim()

            // Find matching price rule
            matchingRule = priceRules.priceRules.find { it.title == couponCode }

            if (matchingRule != null) {
                // Store the discount percentage
                appliedDiscountPercentage = abs(matchingRule?.value?.toDouble() ?: 0.0)

                // Recalculate total with discounted items
                val discountedTotal = calculateDiscountedTotal()

                binding.tvTotalPriceValue.text = currencyFormatter.format(discountedTotal)
                binding.couponsEditText.isEnabled = false
                binding.couponsImageButton.isEnabled = false

                // Refresh the RecyclerView to show discounted prices
                adapter.setDiscount(appliedDiscountPercentage)
                adapter.notifyDataSetChanged()
            } else {
                // Show invalid coupon code message
                Snackbar.make(requireView(), "Invalid coupon code", Snackbar.LENGTH_SHORT).show()
                binding.couponsEditText.text?.clear()
            }
        }

        if (firstDraftOrder.line_items.isEmpty() || firstDraftOrder.line_items.size == 1) {
            binding.emptyBoxAnimation.visibility = View.VISIBLE
            binding.itemsRv.visibility = View.INVISIBLE
            binding.couponsConstraint.visibility = View.INVISIBLE
            binding.couponsCardView.visibility = View.INVISIBLE
            binding.couponsEditText.visibility = View.INVISIBLE
            binding.couponsImageButton.visibility = View.INVISIBLE
        } else {
            binding.emptyBoxAnimation.visibility = View.GONE
            binding.itemsRv.visibility = View.VISIBLE
            binding.couponsConstraint.visibility = View.VISIBLE
            binding.couponsCardView.visibility = View.VISIBLE
            binding.couponsEditText.visibility = View.VISIBLE
            binding.couponsImageButton.visibility = View.VISIBLE
        }

        val newList = firstDraftOrder.line_items?.map { oldItem ->
            val basePrice = oldItem.price.toDoubleOrNull() ?: 0.0
            val displayPrice = basePrice * currencyRate

            LineItem(
                id = oldItem.id,
                variant_id = oldItem.variant_id,
                product_id = oldItem.product_id,
                title = oldItem.title,
                variant_title = oldItem.variant_title,
                sku = oldItem.sku,
                vendor = oldItem.vendor,
                quantity = oldItem.quantity,
                requires_shipping = oldItem.requires_shipping,
                taxable = oldItem.taxable,
                gift_card = oldItem.gift_card,
                fulfillment_service = oldItem.fulfillment_service,
                grams = oldItem.grams,
                tax_lines = oldItem.tax_lines,
                applied_discount = oldItem.applied_discount,
                name = oldItem.name,
                properties = oldItem.properties,
                custom = oldItem.custom,
                price = displayPrice.toString(), // Show converted price for display
                admin_graphql_api_id = oldItem.admin_graphql_api_id,
            )
        }
        adapter.submitList(newList)

        val swipeToDeleteCallback = SwipeToDelete(
            adapter = adapter,
            onDelete = { deletedItem ->
                // Remove the item from the local list
                val updatedLineItems =
                    firstDraftOrder.line_items?.toMutableList() ?: mutableListOf()
                updatedLineItems.removeAll { it.id == deletedItem.id }

                // Update the UI immediately
                firstDraftOrder = firstDraftOrder.copy(line_items = updatedLineItems)
                adapter.submitList(updatedLineItems)

                if (updatedLineItems.isEmpty() || updatedLineItems.size == 1) {
                    binding.emptyBoxAnimation.visibility = View.VISIBLE
                    binding.itemsRv.visibility = View.INVISIBLE
                    binding.couponsConstraint.visibility = View.INVISIBLE
                    binding.couponsCardView.visibility = View.INVISIBLE
                    binding.couponsEditText.visibility = View.INVISIBLE
                    binding.couponsImageButton.visibility = View.INVISIBLE
                } else {
                    binding.emptyBoxAnimation.visibility = View.GONE
                    binding.itemsRv.visibility = View.VISIBLE
                    binding.couponsConstraint.visibility = View.VISIBLE
                    binding.couponsCardView.visibility = View.VISIBLE
                    binding.couponsEditText.visibility = View.VISIBLE
                    binding.couponsImageButton.visibility = View.VISIBLE
                }

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
                                line_items = updatedLineItems,
                                applied_discount = AppliedDiscount(
                                    null,
                                    matchingRule?.valueType,
                                    matchingRule?.value,
                                    matchingRule?.value,
                                    matchingRule?.title
                                ),
                                customer = firstDraftOrder.customer,
                                use_customer_default_address = firstDraftOrder.use_customer_default_address
                            )
                        )

                        shoppingCartViewModel.updateDraftOrder(
                            draftOrderId?.toLong() ?: 0,
                            updateRequest
                        )

                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                requireContext(),
                                "Item deleted successfully",
                                Toast.LENGTH_SHORT
                            ).show()
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
        val currencyRate = currencySharedPreferences.getFloat("currency", 1F)
        return firstDraftOrder.line_items?.sumOf { item ->
            val basePrice = item.price.toDoubleOrNull() ?: 0.0
            val quantity = item.quantity ?: 1
            basePrice * quantity * currencyRate
        } ?: 0.0
    }

    override fun onQuantityChanged(item: LineItem, newQuantity: Int, newPrice: Double) {
        val currencyRate = currencySharedPreferences.getFloat("currency", 1F)
        val basePriceInEGP = newPrice / currencyRate // Convert back to base price in EGP
        binding.tvTotalPriceValue.text = newPrice.toString()

        val updatedLineItems = firstDraftOrder.line_items?.map { lineItem ->
            if (lineItem.id == item.id) {
                // Store the original base price, not the discounted one
                lineItem.copy(
                    quantity = newQuantity,
                    price = basePriceInEGP.toString()
                )
            } else {
                lineItem
            }
        }

        firstDraftOrder = firstDraftOrder.copy(line_items = updatedLineItems ?: listOf())
        updateCartUI()
        binding.tvTotalPriceValue.text = currencyFormatter.format(calculateDiscountedTotal())
    }

    private fun updateDraftOrderToAPI() {
        if (firstDraftOrder.line_items.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Cart is empty", Toast.LENGTH_SHORT).show()
            return
        }

        val updatedLineItems = firstDraftOrder.line_items?.map { item ->
            // Get the base price in EGP
            val basePrice = item.price.toDoubleOrNull() ?: 0.0
            val quantity = item.quantity ?: 1

            // Calculate the discounted price if a discount is applied
            val finalPrice = calculateDiscountedPrice(basePrice)
            LineItem(
                variant_id = item.variant_id,
                product_id = item.product_id,
                quantity = quantity,
                price = finalPrice.toString(), // Send original EGP price to API
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
                shoppingCartViewModel.updateDraftOrder(draftOrderId?.toLong() ?: 0, updateRequest!!)

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
        Snackbar.make(requireView(), "Cart updated successfully", Snackbar.LENGTH_SHORT).show()

        // Use the discounted total price when navigating
        val finalTotal = if (appliedDiscountPercentage > 0) {
            calculateDiscountedTotal()
        } else {
            calculateTotalPrice()
        }

        Navigation.findNavController(requireView())
            .navigate(
                ShoppingCartFragmentDirections.actionNavCartToChooseAddressFragment2(
                    "$finalTotal EGP"
                )
            )
    }

    private fun handleUpdateError(e: Exception) {
        binding.apply {
            btnProceedToCheckout.isEnabled = true
            progressBar.visibility = View.GONE
        }
        Log.e("ShoppingCart", "Failed to update cart: ${e.message}", e)
        Snackbar.make(
            requireView(),
            "Failed to update cart: ${e.message ?: "Unknown error"}",
            Snackbar.LENGTH_SHORT
        ).show()
    }

    private fun getPriceRules() {
        lifecycleScope.launch {
            shoppingCartViewModel.getPriceRules()
        }
    }

    private fun observePriceRules() {
        lifecycleScope.launch {
            shoppingCartViewModel.priceRules.collect {
                when (it) {
                    DataState.Loading -> {}
                    is DataState.OnFailed -> {}
                    is DataState.OnSuccess<*> -> {
                        priceRules = it.data as PriceRulesResponse
                    }
                }
            }
        }
    }
}