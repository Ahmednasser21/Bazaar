package com.iti.itp.bazaar.shoppingCartActivity.cashOnDeliveryFragment.view

import ReceivedDiscount
import ReceivedDraftOrder
import ReceivedLineItem
import ReceivedOrdersResponse
import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.iti.itp.bazaar.auth.MyConstants
import com.iti.itp.bazaar.databinding.FragmentCashOnDeliveryBinding
import com.iti.itp.bazaar.dto.AppliedDiscount
import com.iti.itp.bazaar.dto.Customer
import com.iti.itp.bazaar.dto.DraftOrder
import com.iti.itp.bazaar.dto.DraftOrderRequest
import com.iti.itp.bazaar.dto.LineItem
import com.iti.itp.bazaar.dto.OrderAddress
import com.iti.itp.bazaar.dto.OrderAppliedDiscount
import com.iti.itp.bazaar.dto.OrderCustomer
import com.iti.itp.bazaar.dto.OrderLineItem
import com.iti.itp.bazaar.dto.PartialOrder
import com.iti.itp.bazaar.dto.PartialOrder2
import com.iti.itp.bazaar.dto.PriceRuleDto
import com.iti.itp.bazaar.dto.UpdateDraftOrderRequest
import com.iti.itp.bazaar.dto.order.Order
import com.iti.itp.bazaar.mainActivity.ui.DataState
import com.iti.itp.bazaar.mainActivity.ui.order.SharedOrderViewModel
import com.iti.itp.bazaar.network.exchangeCurrencyApi.CurrencyRemoteDataSource
import com.iti.itp.bazaar.network.exchangeCurrencyApi.ExchangeRetrofitObj
import com.iti.itp.bazaar.network.responses.ExchangeRateResponse
import com.iti.itp.bazaar.network.responses.PriceRulesResponse
import com.iti.itp.bazaar.network.shopify.ShopifyRemoteDataSource
import com.iti.itp.bazaar.network.shopify.ShopifyRetrofitObj
import com.iti.itp.bazaar.repo.CurrencyRepository
import com.iti.itp.bazaar.repo.Repository
import com.iti.itp.bazaar.shoppingCartActivity.cashOnDeliveryFragment.viewModel.CashOnDeliveryViewModel
import com.iti.itp.bazaar.shoppingCartActivity.cashOnDeliveryFragment.viewModel.CashOnDeliveryViewModelFactory
import com.stripe.param.CreditNoteCreateParams.Line
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.abs

class CashOnDeliveryFragment : Fragment() {
    private lateinit var binding: FragmentCashOnDeliveryBinding
    private lateinit var factory: CashOnDeliveryViewModelFactory
    private lateinit var cashOnDeliveryViewModel: CashOnDeliveryViewModel
    private lateinit var currencySharedPreferences: SharedPreferences
    private var isApplyingDiscount = false
    private var currentConversionRate = 1.0
    private lateinit var draftOrderSharedPreferences: SharedPreferences
    private val sharedOrderViewModel by activityViewModels<SharedOrderViewModel>()
    private var customerId: String? = null
    private var draftOrderId: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        draftOrderSharedPreferences = requireActivity().getSharedPreferences(
            MyConstants.MY_SHARED_PREFERANCE,
            Context.MODE_PRIVATE
        )
        factory = CashOnDeliveryViewModelFactory(
            Repository.getInstance(ShopifyRemoteDataSource(ShopifyRetrofitObj.productService)),
            CurrencyRepository(CurrencyRemoteDataSource(ExchangeRetrofitObj.service))
        )
        cashOnDeliveryViewModel =
            ViewModelProvider(this, factory)[CashOnDeliveryViewModel::class.java]
        currencySharedPreferences =
            requireContext().getSharedPreferences("currencySharedPrefs", Context.MODE_PRIVATE)
        binding = FragmentCashOnDeliveryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        customerId = draftOrderSharedPreferences.getString(MyConstants.CUSOMER_ID, "0")
        sharedOrderViewModel.updateCustomer(OrderCustomer(customerId?.toLong() ?: 0))
        draftOrderId = draftOrderSharedPreferences.getString(MyConstants.CART_DRAFT_ORDER_ID, "0")
        setupUI()
        observeData()
        binding.btnPlaceOrder.setOnClickListener {
            createOrder()
            observeOrderResult()
        }
    }

    private fun setupUI() {
        binding.tvValidate.setOnClickListener {
            val couponCode = binding.etCoupons.text.toString()
            if (couponCode.isNotEmpty()) {
                validateCoupon(couponCode)
            } else {
                Snackbar.make(requireView(), "Please enter a coupon code", Snackbar.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private fun observeData() {
        lifecycleScope.launch {
            launch { observeCoupons() }
            launch { observeDraftOrders() }
            launch { observeCurrency() }
        }
    }

    private suspend fun observeCoupons() {
        cashOnDeliveryViewModel.getCoupons()
        cashOnDeliveryViewModel.coupons.collect { state ->
            when (state) {
                is DataState.Loading -> {
                    binding.tvValidate.isEnabled = false
                    binding.etCoupons.isEnabled = false
                    if (!isApplyingDiscount) {
                        Snackbar.make(requireView(), "Loading coupons", Snackbar.LENGTH_SHORT)
                            .show()
                    }
                }

                is DataState.OnFailed -> {
                    binding.tvValidate.isEnabled = true
                    binding.etCoupons.isEnabled = true
                    if (!isApplyingDiscount) {
                        Snackbar.make(
                            requireView(),
                            "Failed to fetch coupons",
                            Snackbar.LENGTH_SHORT
                        ).show()
                    }
                }

                is DataState.OnSuccess<*> -> {
                    binding.tvValidate.isEnabled = true
                    binding.etCoupons.isEnabled = true
                }
            }
        }
    }

    private suspend fun observeDraftOrders() {
        cashOnDeliveryViewModel.getSpecificDraftOrder(draftOrderId?.toLong() ?: 0)
        cashOnDeliveryViewModel.specificDraftOrder.collect { state ->
            when (state) {
                is DataState.Loading -> {
                    if (!isApplyingDiscount) {
                        Snackbar.make(requireView(), "Loading orders", Snackbar.LENGTH_SHORT).show()
                    }
                }

                is DataState.OnFailed -> {
                    if (!isApplyingDiscount) {
                        Snackbar.make(
                            requireView(),
                            "Failed to fetch orders",
                            Snackbar.LENGTH_SHORT
                        ).show()
                    }
                }

                is DataState.OnSuccess<*> -> {
                    val orderResponse = state.data as DraftOrderRequest
                    if (orderResponse.draft_order.line_items.isNotEmpty()) {
                        // Calculate subtotal and total
                        val subtotal = orderResponse.draft_order.line_items.sumOf { lineItem ->
                            lineItem.price.toDoubleOrNull()?.let { price ->
                                price * (lineItem.quantity ?: 1)
                            } ?: 0.0
                        }
                        // Assuming applied_discount is a ReceivedDiscount that contains the discount value
                        val discountAmount =
                            orderResponse.draft_order.applied_discount?.amount?.toDoubleOrNull()
                                ?: 0.0
                        val total = subtotal - discountAmount

                        // Update UI with calculated values
                        binding.tvSubTotalPrice.text =
                            String.format("%.2f", subtotal * currentConversionRate)
                        binding.tvGrandTotal.text =
                            String.format("%.2f", total * currentConversionRate)

                        if (!isApplyingDiscount) {
                            updateUIWithDraftOrder(
                                ReceivedDraftOrder(
                                    id = draftOrderId?.toLong() ?: 0,
                                    line_items = orderResponse.draft_order.line_items.map {
                                        ReceivedLineItem(
                                            product_id = it.product_id,
                                            price = it.price
                                        )
                                    },
                                    subtotal_price = subtotal.toString(),
                                    total_price = total.toString(),
                                    applied_discount = ReceivedDiscount(
                                        null,
                                        value = null,
                                        title = null,
                                        amount = null,
                                        value_type = null
                                    )
                                )
                            )
                        } else {
                            isApplyingDiscount = false
                        }
                    }
                    val draft = orderResponse.draft_order
                    sharedOrderViewModel.updateLineItems(draft.line_items.map {
                        OrderLineItem(
                            variant_id = it.variant_id ?: 0,
                            quantity = it.quantity ?: 0,
                            name = it.name ?: "",
                            title = it.title ?: "",
                            price = it.price ?: ""
                        )
                    })
                    sharedOrderViewModel.updateAppliedDiscount(
                        OrderAppliedDiscount(
                            description = draft.applied_discount?.description ?: "",
                            value = draft.applied_discount?.value ?: "",
                            value_type = draft.applied_discount?.value_type ?: "",
                            amount = draft.applied_discount?.amount ?: "",
                            title = draft.applied_discount?.title ?: ""
                        )
                    )
                }
            }
        }
    }


    private suspend fun observeCurrency() {
        val currency = currencySharedPreferences.getString("currency", "EGP")
        if (currency == "USD") {
            cashOnDeliveryViewModel.exchangeCurrency("EGP", "USD")
        }
        cashOnDeliveryViewModel.currency.collect { state ->
            when (state) {
                is DataState.Loading -> {}
                is DataState.OnFailed -> {}
                is DataState.OnSuccess<*> -> {
                    val data = state.data as ExchangeRateResponse
                    currentConversionRate = data.conversion_rate
                    if (!isApplyingDiscount) {
                        changeAllTextViewsCurrency(data.conversion_rate)
                    }
                }
            }
        }
    }

    private fun validateCoupon(couponCode: String) {
        when (val currentState = cashOnDeliveryViewModel.coupons.value) {
            is DataState.OnSuccess<*> -> {
                val priceRules = currentState.data as PriceRulesResponse
                val matchingPriceRule = priceRules.priceRules.find { it.title == couponCode }

                if (matchingPriceRule != null) {
                    applyCoupon(matchingPriceRule)
                } else {
                    Snackbar.make(requireView(), "Invalid coupon code", Snackbar.LENGTH_SHORT)
                        .show()
                }
            }

            is DataState.Loading -> {
                Snackbar.make(
                    requireView(),
                    "Please wait while loading coupons",
                    Snackbar.LENGTH_SHORT
                ).show()
            }

            is DataState.OnFailed -> {
                Snackbar.make(
                    requireView(),
                    "Failed to validate coupon. Please try again",
                    Snackbar.LENGTH_SHORT
                ).show()
                cashOnDeliveryViewModel.getCoupons()
            }
        }
    }

    private fun applyCoupon(priceRule: PriceRuleDto) {
        val subTotalPrice = binding.tvSubTotalPrice.text.toString().toDoubleOrNull() ?: 0.0
        val baseSubTotalPrice = subTotalPrice / currentConversionRate

        val baseDiscountAmount = when (priceRule.valueType) {
            "percentage" -> {
                val percentage = abs(priceRule.value.toDouble())
                if (percentage > 100) {
                    Snackbar.make(
                        requireView(),
                        "Invalid discount percentage",
                        Snackbar.LENGTH_SHORT
                    ).show()
                    return
                }
                baseSubTotalPrice * percentage / 100
            }

            "fixed_amount" -> {
                val amount = abs(priceRule.value.toDouble())
                if (amount > baseSubTotalPrice) {
                    Snackbar.make(
                        requireView(),
                        "Discount amount exceeds total",
                        Snackbar.LENGTH_SHORT
                    ).show()
                    return
                }
                amount
            }

            else -> 0.0
        }

        if (baseDiscountAmount <= 0) {
            Snackbar.make(requireView(), "Invalid discount amount", Snackbar.LENGTH_SHORT).show()
            return
        }

        isApplyingDiscount = true
        updatePricesAfterDiscount(baseDiscountAmount)
        updateDraftOrderWithDiscount(priceRule.title, baseDiscountAmount)
    }

    private fun updatePricesAfterDiscount(baseDiscountAmount: Double) {
        val subTotalPrice = binding.tvSubTotalPrice.text.toString().toDoubleOrNull() ?: 0.0
        val baseSubTotalPrice = subTotalPrice / currentConversionRate

        val totalSubPriceAfterDiscount = baseSubTotalPrice - baseDiscountAmount
        val totalGrandPriceAfterDiscount = totalSubPriceAfterDiscount

        binding.tvDiscountValue.text =
            String.format("%.2f", baseDiscountAmount * currentConversionRate)
        binding.tvSubTotalPrice.text =
            String.format("%.2f", totalSubPriceAfterDiscount * currentConversionRate)
        binding.tvGrandTotal.text =
            String.format("%.2f", totalGrandPriceAfterDiscount * currentConversionRate)

        binding.tvValidate.isClickable = false
        binding.etCoupons.isEnabled = false
    }

    private fun updateDraftOrderWithDiscount(couponCode: String, baseDiscountAmount: Double) {
        val currentDraftOrderState =
            (cashOnDeliveryViewModel.draftOrders.value as? DataState.OnSuccess<*>)?.data as? ReceivedOrdersResponse
        val currentDraftOrder = currentDraftOrderState?.draft_orders?.firstOrNull()

        if (currentDraftOrder != null) {
            val updatedLineItems = currentDraftOrder.line_items?.map { item ->
                val itemPrice = item.price?.toDoubleOrNull() ?: 0.0
                val quantity = item.quantity ?: 1
                val itemTotal = itemPrice * quantity
                val itemDiscountShare =
                    (itemTotal / (currentDraftOrder.subtotal_price?.toDoubleOrNull()
                        ?: 1.0)) * baseDiscountAmount

                LineItem(
                    product_id = item.product_id,
                    title = item.title,
                    price = item.price,
                    quantity = item.quantity,
                    sku = item.sku,
                    applied_discount = AppliedDiscount(couponCode, itemDiscountShare.toString())
                )
            }

            val updateRequest = UpdateDraftOrderRequest(
                DraftOrder(
                    line_items = updatedLineItems!!,
                    applied_discount = AppliedDiscount(couponCode, baseDiscountAmount.toString()),
                    customer = Customer(currentDraftOrder.customer?.id ?: 0),
                    use_customer_default_address = true
                )
            )

            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    cashOnDeliveryViewModel.updateDraftOrder(currentDraftOrder.id, updateRequest)
                    withContext(Dispatchers.Main) {
                        Snackbar.make(
                            requireView(),
                            "Discount applied successfully",
                            Snackbar.LENGTH_SHORT
                        ).show()
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        isApplyingDiscount = false
                        Snackbar.make(
                            requireView(),
                            "Failed to apply discount: ${e.message}",
                            Snackbar.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        } else {
            isApplyingDiscount = false
            Snackbar.make(requireView(), "No draft order available", Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun updateUIWithDraftOrder(draftOrder: ReceivedDraftOrder) {

        val subTotal = draftOrder.subtotal_price?.toDoubleOrNull() ?: 0.0
        val total = draftOrder.subtotal_price?.toDoubleOrNull() ?: 0.0
        val discount = draftOrder.applied_discount?.amount?.toDoubleOrNull() ?: 0.0

        binding.tvSubTotalPrice.text = String.format("%.2f", subTotal * currentConversionRate)
        binding.tvDiscountValue.text = String.format("%.2f", discount * currentConversionRate)
        binding.tvGrandTotal.text = String.format("%.2f", total * currentConversionRate)

        if (discount > 0) {
            binding.tvValidate.isClickable = false
            binding.etCoupons.isEnabled = false
            binding.etCoupons.setText(draftOrder.applied_discount?.description)
        }
    }

    @SuppressLint("SetTextI18n", "DefaultLocale")
    private fun changeAllTextViewsCurrency(conversionRate: Double) {
        val subTotal = binding.tvSubTotalPrice.text.toString().toDoubleOrNull() ?: 0.0
        val discount = binding.tvDiscountValue.text.toString().toDoubleOrNull() ?: 0.0
        val grandTotal = binding.tvGrandTotal.text.toString().toDoubleOrNull() ?: 0.0

        // Convert from current rate to base rate, then to new rate
        val baseSubTotal = subTotal / currentConversionRate
        val baseDiscount = discount / currentConversionRate
        val baseGrandTotal = grandTotal / currentConversionRate

        binding.tvSubTotalPrice.text = String.format("%.2f", baseSubTotal * conversionRate)
        binding.tvDiscountValue.text = String.format("%.2f", baseDiscount * conversionRate)
        binding.tvGrandTotal.text = String.format("%.2f", baseGrandTotal * conversionRate)
    }

    private fun observeOrderResult() {
        lifecycleScope.launch {
            cashOnDeliveryViewModel.placedOrder.collect {
                when (it) {
                    DataState.Loading -> {}
                    is DataState.OnFailed -> {}
                    is DataState.OnSuccess<*> -> {
                        clearingDraftOrderAfterPlacingOrder()

                    }
                }
            }
        }
    }

    private fun createOrder() {
        lifecycleScope.launch {
            sharedOrderViewModel.partialOrder.collect {
                Log.i("input", "createOrder: $it")
                cashOnDeliveryViewModel.createOrder(
                    PartialOrder2(
                        PartialOrder(
                            customer = it.customer,
                            payment_gateway_names = it.payment_gateway_names,
                            applied_discount = it.applied_discount,
                            shipping_address = it.shipping_address,
                            fulfillment_status = it.fulfillment_status,
                            billing_address = it.billing_address,
                            line_items = it.line_items
                        )
                    )
                )
            }
        }
    }

    private suspend fun clearingDraftOrderAfterPlacingOrder() {

        cashOnDeliveryViewModel.getSpecificDraftOrder(draftOrderId?.toLong() ?: 0)
        cashOnDeliveryViewModel.specificDraftOrder.collect { state ->
            when (state) {
                is DataState.Loading -> {}
                is DataState.OnFailed -> {}
                is DataState.OnSuccess<*> -> {
                    val orderResponse = state.data as DraftOrderRequest
                    if (orderResponse.draft_order.line_items.isNotEmpty()) {
                        val restOfLineItems =
                            listOf(orderResponse.draft_order.line_items[0]).toMutableList()

                        val draftOrderItem = DraftOrder(
                            restOfLineItems,
                            applied_discount = orderResponse.draft_order.applied_discount,
                            customer = orderResponse.draft_order.customer,
                            use_customer_default_address = orderResponse.draft_order.use_customer_default_address
                        )
                        Log.i("draftItem", "clearingDraftOrderAfterPlacingOrder: ${draftOrderItem}")
                        cashOnDeliveryViewModel.updateDraftOrder(
                            draftOrderId?.toLong()?: 0,
                            UpdateDraftOrderRequest(draftOrderItem)
                        )
                    }
                }
            }
        }
    }
}
