package com.iti.itp.bazaar.shoppingCartActivity.paymentMethods

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.snackbar.Snackbar
import com.iti.itp.bazaar.auth.MyConstants
import com.iti.itp.bazaar.databinding.FragmentPaymentMethodsBinding
import com.iti.itp.bazaar.dto.*
import com.iti.itp.bazaar.dto.order.Order
import com.iti.itp.bazaar.mainActivity.MainActivity
import com.iti.itp.bazaar.mainActivity.ui.DataState
import com.iti.itp.bazaar.mainActivity.ui.order.SharedOrderViewModel
import com.iti.itp.bazaar.network.exchangeCurrencyApi.CurrencyRemoteDataSource
import com.iti.itp.bazaar.network.exchangeCurrencyApi.ExchangeRetrofitObj
import com.iti.itp.bazaar.network.shopify.ShopifyRemoteDataSource
import com.iti.itp.bazaar.network.shopify.ShopifyRetrofitObj
import com.iti.itp.bazaar.repo.CurrencyRepository
import com.iti.itp.bazaar.repo.Repository
import com.iti.itp.bazaar.shoppingCartActivity.cashOnDeliveryFragment.viewModel.CashOnDeliveryViewModel
import com.iti.itp.bazaar.shoppingCartActivity.cashOnDeliveryFragment.viewModel.CashOnDeliveryViewModelFactory
import com.stripe.android.PaymentConfiguration
import com.stripe.android.paymentsheet.PaymentSheet
import com.stripe.android.paymentsheet.PaymentSheetResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

class PaymentMethods : Fragment() {
    companion object {
        private const val TAG = "PaymentMethods"
    }

    private var totalAmount = 0.0
    private lateinit var binding: FragmentPaymentMethodsBinding
    private val publishableKey = "pk_test_51QC3haAoIOlEetPWyhWCPqSmPmwTG0YQxPf00Lhdj8hOFwNT3hasxVsFQNPUe1qF2zARW0CLt3DfwQmBg48HTR0200ikDsaiWf"
    private val secretKey = "sk_test_51QC3haAoIOlEetPWK9JXmOYd8NQEdrdYR6JDHGoOntH3Xl5tOSmeAjJFFR8M1So5cWwDiS6q7GoozFL33zLA4mnx001VQzYprI"
    private lateinit var factory: CashOnDeliveryViewModelFactory
    private lateinit var cashOnDeliveryViewModel: CashOnDeliveryViewModel
    private lateinit var draftOrderSharedPreferences: SharedPreferences
    private var customerId: String? = null
    private var ephemeralKey: String? = null
    private var clientSecret: String? = null
    private lateinit var paymentSheet: PaymentSheet
    private var draftOrderId: String? = null
    private val sharedOrderViewModel by activityViewModels<SharedOrderViewModel>()
    private var sharedCustomerId:String? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        PaymentConfiguration.init(context, publishableKey)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        paymentSheet = PaymentSheet(this) { result: PaymentSheetResult ->
            when (result) {
                is PaymentSheetResult.Completed -> {
                    Toast.makeText(requireContext(), "Payment success", Toast.LENGTH_SHORT).show()
                    createOrder()
                }
                is PaymentSheetResult.Canceled -> {
                    Toast.makeText(requireContext(), "Payment canceled", Toast.LENGTH_SHORT).show()
                }
                is PaymentSheetResult.Failed -> {
                    Toast.makeText(
                        requireContext(),
                        "Payment failed: ${result.error?.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

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
        cashOnDeliveryViewModel = ViewModelProvider(requireActivity(), factory)[CashOnDeliveryViewModel::class.java]
        binding = FragmentPaymentMethodsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        draftOrderId = draftOrderSharedPreferences.getString(MyConstants.CART_DRAFT_ORDER_ID, "0")
        sharedCustomerId = draftOrderSharedPreferences.getString(MyConstants.CUSOMER_ID, "0")
        binding.continueToPayment.setOnClickListener {
            when {
                binding.paymob.isChecked -> {
                    startPayWithStripe()
                    sharedOrderViewModel.updatePaymentGateway(listOf("Credit Card"))
                }
                binding.cashOnDelivery.isChecked -> {
                    sharedOrderViewModel.updatePaymentGateway(listOf("Cash On Delivery"))
                    val action = PaymentMethodsDirections.actionPaymentMethodsToCashOnDeliveryFragment()
                    Navigation.findNavController(view).navigate(action)
                }
            }
        }
        observeDraftOrders()
    }

    private fun startPayWithStripe() {
        if (clientSecret != null) {
            paymentFlow()
        } else {
            createCustomer()
        }
    }

    private fun createCustomer() {
        val request = object : StringRequest(
            Method.POST,
            "https://api.stripe.com/v1/customers",
            { response ->
                try {
                    val jsonResponse = JSONObject(response)
                    customerId = jsonResponse.getString("id")
                    Toast.makeText(requireContext(), "Customer ID: $customerId", Toast.LENGTH_SHORT).show()
                    getEphemeralKey()
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing customer response", e)
                }
            },
            { error ->
                Log.e(TAG, "Error creating customer", error)
            }
        ) {
            override fun getHeaders(): MutableMap<String, String> {
                return mutableMapOf("Authorization" to "Bearer $secretKey")
            }
        }

        Volley.newRequestQueue(requireContext()).add(request)
    }

    private fun getEphemeralKey() {
        val request = object : StringRequest(
            Method.POST,
            "https://api.stripe.com/v1/ephemeral_keys",
            { response ->
                try {
                    val jsonResponse = JSONObject(response)
                    ephemeralKey = jsonResponse.getString("id")
                    getClientSecret(customerId!!)
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing ephemeral key response", e)
                }
            },
            { error ->
                Log.e(TAG, "Error getting ephemeral key", error)
            }
        ) {
            override fun getHeaders(): MutableMap<String, String> {
                return mutableMapOf(
                    "Authorization" to "Bearer $secretKey",
                    "Stripe-Version" to "2024-09-30.acacia"
                )
            }

            override fun getParams(): MutableMap<String, String?> {
                return mutableMapOf("customer" to customerId)
            }
        }

        Volley.newRequestQueue(requireContext()).add(request)
    }

    private fun getClientSecret(customerId: String) {
        val request = object : StringRequest(
            Method.POST,
            "https://api.stripe.com/v1/payment_intents",
            { response ->
                try {
                    val jsonResponse = JSONObject(response)
                    clientSecret = jsonResponse.getString("client_secret")
                    Toast.makeText(requireContext(), "Client Secret: $clientSecret", Toast.LENGTH_SHORT).show()
                    paymentFlow()
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing payment intent response", e)
                }
            },
            { error ->
                Log.e(TAG, "Error creating payment intent", error)
            }
        ) {
            override fun getHeaders(): MutableMap<String, String> {
                return mutableMapOf("Authorization" to "Bearer $secretKey")
            }

            override fun getParams(): MutableMap<String, String> {
                return mutableMapOf(
                    "customer" to customerId,
                    "amount" to (totalAmount * 100).toInt().toString(),  // Convert to cents
                    "currency" to "usd",
                    "automatic_payment_methods[enabled]" to "true"
                )
            }
        }

        Volley.newRequestQueue(requireContext()).add(request)
    }

    private fun paymentFlow() {
        clientSecret?.let {
            paymentSheet.presentWithPaymentIntent(
                it,
                PaymentSheet.Configuration("Your Merchant Name")
            )
        } ?: run {
            Toast.makeText(requireContext(), "Client secret is null", Toast.LENGTH_SHORT).show()
        }
    }

    private fun createOrder() {
        lifecycleScope.launch {
            try {
                // Get the current value from the flow instead of collecting indefinitely
                val partialOrder = sharedOrderViewModel.partialOrder.value

                cashOnDeliveryViewModel.createOrder(
                    PartialOrder2(
                        PartialOrder(
                            customer = OrderCustomer(sharedCustomerId?.toLong()?:0),
                            payment_gateway_names = listOf("Credit Card"),
                            applied_discount = partialOrder.applied_discount,
                            shipping_address = partialOrder.shipping_address,
                            fulfillment_status = partialOrder.fulfillment_status,
                            billing_address = partialOrder.billing_address,
                            line_items = partialOrder.line_items
                        )
                    )
                )

                // Move the observation of order result here
                observeOrderResult()
            } catch (e: Exception) {
                Log.e(TAG, "Error creating order", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        requireContext(),
                        "Failed to create order: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun observeOrderResult() {
        lifecycleScope.launch {
            try {
                cashOnDeliveryViewModel.placedOrder.collect { state ->
                    when (state) {
                        is DataState.Loading -> {
                            // Show loading indicator if needed
                        }
                        is DataState.OnFailed -> {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(
                                    requireContext(),
                                    "Failed to place order",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                        is DataState.OnSuccess<*> -> {
                            withContext(Dispatchers.Main) {
                                showOrderSuccessDialog()
                                clearingDraftOrderAfterPlacingOrder()
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error observing order result", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        requireContext(),
                        "Error processing order: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
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
                        val restOfLineItems = listOf(orderResponse.draft_order.line_items[0]).toMutableList()
                        val draftOrderItem = DraftOrder(
                            restOfLineItems,
                            applied_discount = orderResponse.draft_order.applied_discount,
                            customer = orderResponse.draft_order.customer,
                            use_customer_default_address = orderResponse.draft_order.use_customer_default_address
                        )
                        Log.i("draftItem", "clearingDraftOrderAfterPlacingOrder: $draftOrderItem")
                        cashOnDeliveryViewModel.updateDraftOrder(
                            draftOrderId?.toLong() ?: 0,
                            UpdateDraftOrderRequest(draftOrderItem)
                        )
                    }
                }
            }
        }
    }

    private fun showOrderSuccessDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Successful Order")
            .setMessage("Order placed successfully")
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
                val intent = Intent(requireActivity(), MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
            }
            .create()
            .show()
    }

    private fun observeDraftOrders() {
        lifecycleScope.launch {
            cashOnDeliveryViewModel.getSpecificDraftOrder(draftOrderId?.toLong() ?: 0)
            cashOnDeliveryViewModel.specificDraftOrder.collect { state ->
                when (state) {
                    is DataState.Loading -> {}
                    is DataState.OnFailed -> {
                        Snackbar.make(requireView(), "Failed to fetch orders", Snackbar.LENGTH_SHORT).show()
                    }
                    is DataState.OnSuccess<*> -> {
                        val orderResponse = state.data as DraftOrderRequest
                        val draft = orderResponse.draft_order
                        totalAmount = draft.line_items.sumOf { it.price.toDouble() } * 0.021
                        sharedOrderViewModel.updateLineItems(draft.line_items.map {
                            OrderLineItem(
                                variant_id = it.variant_id ?: 0,
                                quantity = it.quantity ?: 0,
                                name = it.name ?: "",
                                title = it.title ?: "",
                                price = it.price
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
    }
}