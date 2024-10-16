package com.iti.itp.bazaar.network.shopify

import com.iti.itp.bazaar.dto.CustomerAddress
import com.iti.itp.bazaar.dto.CustomerAddressResponse
import com.iti.itp.bazaar.network.responses.CouponsCountResponse
import com.iti.itp.bazaar.network.responses.DiscountCodesResponse
import com.iti.itp.bazaar.network.responses.PriceRulesCountResponse
import com.iti.itp.bazaar.network.responses.PriceRulesResponse
import com.iti.itp.bazaar.network.responses.ProductResponse
import retrofit2.Response

class ShopifyRemoteDataSource(private val productService: ProductService) {

    suspend fun getVendors(fields:String): ProductResponse{
       return productService.getVendors(fields)
    }

    suspend fun getVendorProducts(vendorName:String):ProductResponse{
        return productService.getVendorProducts(vendorName)
    }

    suspend fun addAddress(customerId: Long, customerAddress: CustomerAddress): Response<CustomerAddressResponse> {
        return productService.createCustomerAddress(customerId,customerAddress)
    }

    suspend fun getPriceRules():PriceRulesResponse{
        return productService.getPriceRules()
    }

    suspend fun getPriceRulesCount(): PriceRulesCountResponse{
        return productService.getPriceRulesCount()
    }

    suspend fun getCouponsCount(): CouponsCountResponse{
        return productService.getCouponsCounts()
    }

    suspend fun getCoupons(priceRuleId:Long):DiscountCodesResponse{
        return productService.getDiscountCodes(priceRuleId)
    }
}