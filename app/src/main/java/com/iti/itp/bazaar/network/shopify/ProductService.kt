package com.iti.itp.bazaar.network.shopify

import ReceivedDraftOrder
import ReceivedOrdersResponse
import com.iti.itp.bazaar.dto.AddAddressResponse
import com.iti.itp.bazaar.dto.AddedAddressRequest
import com.iti.itp.bazaar.dto.CustomerAddressResponse
import com.iti.itp.bazaar.dto.AddedCustomerAddressResponse
import com.iti.itp.bazaar.dto.CustomerRequest
import com.iti.itp.bazaar.dto.DraftOrderRequest
import com.iti.itp.bazaar.dto.ListOfAddresses
import com.iti.itp.bazaar.dto.SingleCustomerResponse
import com.iti.itp.bazaar.dto.UpdateDraftOrderRequest
import com.iti.itp.bazaar.dto.cutomerResponce.CustomerResponse
import com.iti.itp.bazaar.network.responses.CouponsCountResponse
import com.iti.itp.bazaar.network.responses.DiscountCodesResponse
import com.iti.itp.bazaar.network.responses.PriceRulesCountResponse
import com.iti.itp.bazaar.network.responses.PriceRulesResponse
import com.iti.itp.bazaar.network.responses.ProductResponse
import com.iti.itp.bazaar.network.responses.SmartCollectionsResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ProductService {
    @GET("admin/api/2022-01/smart_collections.json")
    suspend fun getVendors(): SmartCollectionsResponse

    @GET("admin/api/2022-01/products.json")
    suspend fun getVendorProducts(@Query("vendor") name: String): ProductResponse

    ///
    @GET("admin/api/2022-01/products.json")
    suspend fun getProductDetails(@Query("ids") id: Long): ProductResponse

    @POST("admin/api/2024-10/customers/{customerId}/addresses.json")
    suspend fun addAddress(
        @Path("customerId") customerId: Long,
        @Body address: AddAddressResponse
    ): AddAddressResponse

    @GET("admin/api/2024-10/price_rules.json")
    suspend fun getPriceRules(): PriceRulesResponse

    @GET("/admin/api/2024-10/price_rules/count.json")
    suspend fun getPriceRulesCount(): PriceRulesCountResponse

    @GET("/admin/api/2024-10/discount_codes/count.json")
    suspend fun getCouponsCounts(): CouponsCountResponse

    @GET("admin/api/2024-10/price_rules/{price_rule_id}/discount_codes.json")
    suspend fun getDiscountCodes(@Path("price_rule_id") priceRuleId: Long): DiscountCodesResponse

    @GET("admin/api/2022-01/collections/{id}/products.json")
    suspend fun getCollectionProducts(@Path("id") id: Long): ProductResponse

    @GET("/admin/api/2024-10/customers/{customer_id}/addresses.json")
    suspend fun getAddressForCustomer(
        @Path("customer_id") customerId: Long,
    ): ListOfAddresses

    @PUT("/admin/api/2024-10/customers/{customer_id}/addresses/{address_id}.json")
    suspend fun updateCustomerAddress(
        @Path("customer_id") customerId: Long,
        @Path("address_id") addressId: Long,
        @Body addressRequest: CustomerAddressResponse
    ): CustomerAddressResponse

    @POST("admin/api/2024-10/customers.json")
    suspend fun postCustomer(@Body customer: CustomerRequest): CustomerResponse

    /////////////////////////
    @POST("admin/api/2024-10/draft_orders.json")
    suspend fun createDraftOrder(
        @Body draftOrderRequest: DraftOrderRequest
    ): ReceivedDraftOrder

    @PUT("admin/api/2024-10/draft_orders/{draftOrderId}.json")
    suspend fun updateDraftOrder(
        @Path("draftOrderId") draftOrderId: Long,
        @Body updateDraftOrderRequest: UpdateDraftOrderRequest
    ): UpdateDraftOrderRequest

    @GET("admin/api/2024-10/draft_orders.json")
    suspend fun getAllDraftOrders():ReceivedOrdersResponse
/////////////////////////////
    @GET("admin/api/2024-10/products.json")
    suspend fun getAllProducts(): ProductResponse

    @GET("admin/api/2024-10/draft_orders/{draftOrderId}.json")
    suspend fun getSpecificDraftOrder(
        @Path("draftOrderId") draftOrderId: Long
    ): DraftOrderRequest


    @GET("/admin/api/2024-10/customers/{customer_id}.json")
    suspend fun getCustomerById(
        @Path("customer_id") customerId:Long
    ): SingleCustomerResponse

}
