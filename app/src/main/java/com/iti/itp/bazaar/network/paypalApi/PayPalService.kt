package com.iti.itp.bazaar.network.paypalApi

import org.json.JSONObject
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.FieldMap
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Header
import retrofit2.http.HeaderMap
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Path

interface PayPalService {
    @FormUrlEncoded
    @POST("v1/oauth2/token")
    fun fetchAccessToken(
        @HeaderMap headers: Map<String, String>,
        @FieldMap body: Map<String, String>
    ): Call<AccessTokenResponse>

    @POST("v2/checkout/orders")
    fun createOrder(
        @Header("Authorization") auth: String,
        @Body orderRequest: OrderRequest
    ): Call<OrderResponse>

    @POST("v2/checkout/orders/{orderID}/capture")
    fun captureOrder(
        @Path("orderID") orderID: String,
        @Header("Authorization") authHeader: String,
        @Body body: EmptyBody // Use your defined EmptyBody class here
    ): Call<CaptureResponse>

}