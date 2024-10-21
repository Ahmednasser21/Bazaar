package com.iti.itp.bazaar.shoppingCartActivity.paypalFragment

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.iti.itp.bazaar.databinding.FragmentPayPalBinding
import com.stripe.android.PaymentConfiguration
import com.stripe.android.paymentsheet.PaymentSheet
import com.stripe.android.paymentsheet.PaymentSheetResult
import org.json.JSONObject

class PayPalFragment : Fragment() {

    private lateinit var binding: FragmentPayPalBinding
    private val TAG = "PayPalFragment"
    private val publishableKey = "pk_test_51QC3haAoIOlEetPWyhWCPqSmPmwTG0YQxPf00Lhdj8hOFwNT3hasxVsFQNPUe1qF2zARW0CLt3DfwQmBg48HTR0200ikDsaiWf"
    private val secretKey = "sk_test_51QC3haAoIOlEetPWK9JXmOYd8NQEdrdYR6JDHGoOntH3Xl5tOSmeAjJFFR8M1So5cWwDiS6q7GoozFL33zLA4mnx001VQzYprI"

    private var customerId: String? = null
    private var ephemeralKey: String? = null
    private var clientSecret: String? = null
    private lateinit var paymentSheet: PaymentSheet

    override fun onAttach(context: Context) {
        super.onAttach(context)
        PaymentConfiguration.init(context, publishableKey)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPayPalBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        paymentSheet = PaymentSheet(this) { result: PaymentSheetResult ->
            when (result) {
                is PaymentSheetResult.Completed -> {
                    Toast.makeText(requireContext(), "Payment success", Toast.LENGTH_SHORT).show()
                }
                is PaymentSheetResult.Canceled -> {
                    Toast.makeText(requireContext(), "Payment canceled", Toast.LENGTH_SHORT).show()
                }
                is PaymentSheetResult.Failed -> {
                    Toast.makeText(requireContext(), "Payment failed: ${result.error?.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.proceed.setOnClickListener {
            createCustomer()
        }
    }

    private fun createCustomer() {
        val request = object : StringRequest(
            Method.POST,
            "https://api.stripe.com/v1/customers",
            Response.Listener { response ->
                try {
                    val jsonResponse = JSONObject(response)
                    customerId = jsonResponse.getString("id")
                    Toast.makeText(requireContext(), "Customer ID: $customerId", Toast.LENGTH_SHORT).show()
                    getEphemeralKey()
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing customer response", e)
                }
            },
            Response.ErrorListener { error ->
                Log.e(TAG, "Error creating customer", error)
            }
        ) {
            override fun getHeaders(): MutableMap<String, String> {
                return mutableMapOf("Authorization" to "Bearer $secretKey")
            }
        }

        val requestQueue = Volley.newRequestQueue(requireContext())
        requestQueue.add(request)
    }

    private fun getEphemeralKey() {
        val request = object : StringRequest(
            Method.POST,
            "https://api.stripe.com/v1/ephemeral_keys",
            Response.Listener { response ->
                try {
                    val jsonResponse = JSONObject(response)
                    ephemeralKey = jsonResponse.getString("id")
                    getClientSecret(customerId!!)
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing ephemeral key response", e)
                }
            },
            Response.ErrorListener { error ->
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

        val requestQueue = Volley.newRequestQueue(requireContext())
        requestQueue.add(request)
    }

    private fun getClientSecret(customerId: String) {
        val request = object : StringRequest(
            Method.POST,
            "https://api.stripe.com/v1/payment_intents",
            Response.Listener { response ->
                try {
                    val jsonResponse = JSONObject(response)
                    clientSecret = jsonResponse.getString("client_secret")
                    Toast.makeText(requireContext(), "Client Secret: $clientSecret", Toast.LENGTH_SHORT).show()
                    paymentFlow()
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing payment intent response", e)
                }
            },
            Response.ErrorListener { error ->
                Log.e(TAG, "Error creating payment intent", error)
            }
        ) {
            override fun getHeaders(): MutableMap<String, String> {
                return mutableMapOf("Authorization" to "Bearer $secretKey")
            }

            override fun getParams(): MutableMap<String, String>? {
                return mutableMapOf(
                    "customer" to customerId,
                    "amount" to "1000",  // amount in cents (e.g., $10.00)
                    "currency" to "usd",
                    "automatic_payment_methods[enabled]" to "true"
                )
            }
        }

        val requestQueue = Volley.newRequestQueue(requireContext())
        requestQueue.add(request)
    }

    private fun paymentFlow() {
        clientSecret?.let {
            paymentSheet.presentWithPaymentIntent(it, PaymentSheet.Configuration("Your Merchant Name"))
        } ?: run {
            Toast.makeText(requireContext(), "Client secret is null", Toast.LENGTH_SHORT).show()
        }
    }
}
