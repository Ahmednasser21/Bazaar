package com.iti.itp.bazaar.network.shopify

import com.iti.itp.bazaar.dto.CustomerAddress
import com.iti.itp.bazaar.dto.CustomerAddressResponse
import com.iti.itp.bazaar.network.responses.CouponsCountResponse
import com.iti.itp.bazaar.network.responses.DiscountCodesResponse
import com.iti.itp.bazaar.network.responses.PriceRulesCountResponse
import com.iti.itp.bazaar.network.responses.PriceRulesResponse
import com.iti.itp.bazaar.network.responses.ProductResponse
import com.iti.itp.bazaar.network.responses.SmartCollectionsResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ProductService {
    @GET("admin/api/2022-01/smart_collections.json")
    suspend fun getVendors():SmartCollectionsResponse

    @GET("admin/api/2022-01/products.json")
    suspend fun getVendorProducts(@Query("vendor") name: String): ProductResponse
    ///
    @GET("admin/api/2022-01/products.json")
    suspend fun getProductDetails(@Query("ids") id: Long): ProductResponse

    @POST("admin/api/2024-10/customers/{customer_id}/addresses.json")
    suspend fun createCustomerAddress(
        @Path("customer_id") customerId: Long,
        @Body address: CustomerAddress
    ): Response<CustomerAddressResponse>

    @GET("admin/api/2024-10/price_rules.json")
    suspend fun getPriceRules(): PriceRulesResponse

    @GET("/admin/api/2024-10/price_rules/count.json")
    suspend fun getPriceRulesCount(): PriceRulesCountResponse

    @GET("/admin/api/2024-10/discount_codes/count.json")
    suspend fun getCouponsCounts(): CouponsCountResponse

    @GET("admin/api/2024-10/price_rules/{price_rule_id}/discount_codes.json")
    suspend fun getDiscountCodes(@Path("price_rule_id") priceRuleId: Long): DiscountCodesResponse

    @GET("admin/api/2022-01/collections/{id}/products.json")
    suspend fun getCollectionProducts (@Path("id") id:Long ) : ProductResponse

}
