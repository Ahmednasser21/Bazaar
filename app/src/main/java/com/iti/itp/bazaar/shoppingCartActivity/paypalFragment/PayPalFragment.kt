//package com.iti.itp.bazaar.shoppingCartActivity.paypalFragment
//
//import android.app.Activity
//import android.content.Intent
//import android.os.Bundle
//import android.util.Base64
//import android.util.Log
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.Toast
//import androidx.fragment.app.Fragment
//import com.iti.itp.bazaar.databinding.FragmentPayPalBinding
//import com.iti.itp.bazaar.network.paypalApi.AccessTokenResponse
//import com.iti.itp.bazaar.network.paypalApi.Amount
//import com.iti.itp.bazaar.network.paypalApi.CaptureResponse
//import com.iti.itp.bazaar.network.paypalApi.EmptyBody
//import com.iti.itp.bazaar.network.paypalApi.ExperienceContext
//import com.iti.itp.bazaar.network.paypalApi.OrderRequest
//import com.iti.itp.bazaar.network.paypalApi.OrderResponse
//import com.iti.itp.bazaar.network.paypalApi.PayPalDetails
//import com.iti.itp.bazaar.network.paypalApi.PayPalRetrofit
//import com.iti.itp.bazaar.network.paypalApi.PaymentSource
//import com.iti.itp.bazaar.network.paypalApi.PurchaseUnit
//import com.paypal.android.corepayments.CoreConfig
//import com.paypal.android.corepayments.Environment
//import com.paypal.android.corepayments.PayPalSDKError
//import com.paypal.android.paypalwebpayments.PayPalWebCheckoutClient
//import com.paypal.android.paypalwebpayments.PayPalWebCheckoutFundingSource
//import com.paypal.android.paypalwebpayments.PayPalWebCheckoutListener
//import com.paypal.android.paypalwebpayments.PayPalWebCheckoutRequest
//import com.paypal.android.paypalwebpayments.PayPalWebCheckoutResult
//import org.json.JSONArray
//import org.json.JSONObject
//import retrofit2.Call
//import retrofit2.Callback
//import retrofit2.Response
//import java.util.UUID
//
//class PayPalFragment : Fragment() {
//    private val returnUrl = "com.iti.itp.bazaar://paypalpay"
//    var accessToken = ""
//    private lateinit var uniqueId: String
//    private var orderid = ""
//    private lateinit var binding: FragmentPayPalBinding
//    private val TAG = "PayPalFragment"
//    private val PAYPAL_REQUEST_CODE = 1234 // Define your request code
//
//
//    // PayPal Configuration
//    private val clientID = "AYF_7hasq1akGkaUz04HUmMoC-Iplw7jPfoLFuEFQzfqsB3rfGQCqUw5ZcWVGY5cO0LMGjFCooWunB5N"
//    private val secretID = "EEiwJFW8iAefjIcgWYfq9hWtLHOg2OCn-YFy-TFhlaGD2qzIkz4Hu3hmFnJPGrSNLefrQwREeaaMI9c5"
//
//
//
//    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
//        binding = FragmentPayPalBinding.inflate(inflater, container, false)
//        return binding.root
//    }
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//        fetchAccessToken()
//        binding.proceed.setOnClickListener {
//            if (accessToken.isNotEmpty()) {
//                startOrder()
//            } else {
//                Toast.makeText(requireContext(), "Please wait, fetching access token...", Toast.LENGTH_SHORT).show()
//            }
//        }
//    }
//
//
//    private fun handlerOrderID(orderID: String) {
//        val config = CoreConfig(clientID, environment = Environment.SANDBOX)
//        val payPalWebCheckoutClient = PayPalWebCheckoutClient(requireActivity(), config, returnUrl)
//
//        payPalWebCheckoutClient.listener = object : PayPalWebCheckoutListener {
//            override fun onPayPalWebSuccess(result: PayPalWebCheckoutResult) {
//                Log.d(TAG, "Payment successful: Order ID = ${result.orderId}")
//                // Capture the order here
//                captureOrder(result.orderId ?: orderID)
//            }
//
//            override fun onPayPalWebFailure(error: PayPalSDKError) {
//                Log.e(TAG, "Payment failed: ${error.message}")
//                Toast.makeText(requireContext(), "Payment failed: ${error.message}", Toast.LENGTH_SHORT).show()
//            }
//
//            override fun onPayPalWebCanceled() {
//                Log.d(TAG, "Payment was canceled")
//                Toast.makeText(requireContext(), "Payment was canceled", Toast.LENGTH_SHORT).show()
//            }
//        }
//
//        // Start the PayPal payment process
//        val payPalWebCheckoutRequest = PayPalWebCheckoutRequest(orderID, fundingSource = PayPalWebCheckoutFundingSource.PAYPAL)
//        payPalWebCheckoutClient.start(payPalWebCheckoutRequest)
//    }
//
//
//    private fun fetchAccessToken() {
//        val authString = "$clientID:$secretID"
//        val encodedAuthString = Base64.encodeToString(authString.toByteArray(), Base64.NO_WRAP)
//
//        val headers = mapOf(
//            "Authorization" to "Basic $encodedAuthString",
//            "Content-Type" to "application/x-www-form-urlencoded"
//        )
//
//        val body = mapOf("grant_type" to "client_credentials")
//
//        PayPalRetrofit.instance.fetchAccessToken(headers, body)
//            .enqueue(object : retrofit2.Callback<AccessTokenResponse> {
//                override fun onResponse(call: Call<AccessTokenResponse>, response: retrofit2.Response<AccessTokenResponse>) {
//                    if (response.isSuccessful) {
//                        accessToken = response.body()?.access_token ?: ""
//                        Log.d(TAG, "Access Token: $accessToken")
//                        Toast.makeText(requireContext(), "Access Token Fetched!", Toast.LENGTH_SHORT).show()
//                    } else {
//                        Log.e(TAG, "Error fetching access token: ${response.errorBody()?.string()}")
//                        Toast.makeText(requireContext(), "Error Occurred: ${response.code()}", Toast.LENGTH_SHORT).show()
//                    }
//                }
//
//                override fun onFailure(call: Call<AccessTokenResponse>, t: Throwable) {
//                    Log.e(TAG, "Error: ${t.message}")
//                    Toast.makeText(requireContext(), "Network Error Occurred!", Toast.LENGTH_SHORT).show()
//                }
//            })
//    }
//
//    private fun startOrder() {
//        uniqueId = UUID.randomUUID().toString()
//
//        val orderRequest = OrderRequest(
//            intent = "CAPTURE",
//            purchase_units = listOf(
//                PurchaseUnit(
//                    reference_id = uniqueId,
//                    amount = Amount(currency_code = "USD", value = "5.00")
//                )
//            ),
//            payment_source = PaymentSource(
//                paypal = PayPalDetails(
//                    experience_context = ExperienceContext(
//                        payment_method_preference = "IMMEDIATE_PAYMENT_REQUIRED",
//                        brand_name = "SH Developer",
//                        locale = "en-US",
//                        landing_page = "LOGIN",
//                        shipping_preference = "NO_SHIPPING",
//                        user_action = "PAY_NOW",
//                        return_url = returnUrl,
//                        cancel_url = "https://example.com/cancelUrl"
//                    )
//                )
//            )
//        )
//
//        PayPalRetrofit.instance.createOrder("Bearer $accessToken", orderRequest)
//            .enqueue(object : retrofit2.Callback<OrderResponse> {
//                override fun onResponse(call: Call<OrderResponse>, response: retrofit2.Response<OrderResponse>) {
//                    if (response.isSuccessful) {
//                        Log.d(TAG, "Order Response: ${response.body()}")
//                        response.body()?.id?.let { orderId ->
//                            handlerOrderID(orderId)
//                        } ?: run {
//                            Log.e(TAG, "Order ID is null")
//                            Toast.makeText(requireContext(), "Error: Order ID is null", Toast.LENGTH_SHORT).show()
//                        }
//                    } else {
//                        Log.e(TAG, "Order Error: ${response.errorBody()?.string()}")
//                        Toast.makeText(requireContext(), "Error creating order", Toast.LENGTH_SHORT).show()
//                    }
//                }
//
//                override fun onFailure(call: Call<OrderResponse>, t: Throwable) {
//                    Log.e(TAG, "Order Error: ${t.message}")
//                    Toast.makeText(requireContext(), "Network error occurred", Toast.LENGTH_SHORT).show()
//                }
//            })
//    }
//
//    private fun captureOrder(orderID: String) {
//        val call = PayPalRetrofit.instance.captureOrder(orderID, "Bearer $accessToken", EmptyBody())
//
//        call.enqueue(object : Callback<CaptureResponse> {
//            override fun onResponse(call: Call<CaptureResponse>, response: Response<CaptureResponse>) {
//                if (response.isSuccessful) {
//                    Log.d(TAG, "Capture Response: ${response.body()}")
//                    Toast.makeText(requireContext(), "Payment Successful", Toast.LENGTH_SHORT).show()
//                } else {
//                    Log.e(TAG, "Capture Error: ${response.errorBody()?.string()}")
//                }
//            }
//
//            override fun onFailure(call: Call<CaptureResponse>, t: Throwable) {
//                Log.e(TAG, "Network error: ${t.message}")
//            }
//        })
//    }
//
//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//
//        if (requestCode == PAYPAL_REQUEST_CODE) { // Define your request code
//            val orderId = data?.getStringExtra("orderId") // Extract orderId from Intent
//            if (orderId != null) {
//                captureOrder(orderId)
//            } else {
//                Log.e(TAG, "Order ID is null")
//                Toast.makeText(requireContext(), "Error: Order ID is null", Toast.LENGTH_SHORT).show()
//            }
//        }
//    }
//}