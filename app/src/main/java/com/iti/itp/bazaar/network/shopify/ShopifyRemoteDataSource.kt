package com.iti.itp.bazaar.network.shopify

import ReceivedDraftOrder
import ReceivedOrdersResponse
import com.iti.itp.bazaar.dto.AddAddressResponse
import com.iti.itp.bazaar.dto.AddedAddressRequest
import com.iti.itp.bazaar.dto.AddedCustomerAddressResponse
import com.iti.itp.bazaar.dto.AddressRequest
import com.iti.itp.bazaar.dto.CustomerAddress
import com.iti.itp.bazaar.dto.CustomerAddressResponse
import com.iti.itp.bazaar.dto.CustomerRequest
import com.iti.itp.bazaar.dto.DraftOrderRequest
import com.iti.itp.bazaar.dto.ListOfAddresses
import com.iti.itp.bazaar.dto.PartialOrder
import com.iti.itp.bazaar.dto.PartialOrder2
import com.iti.itp.bazaar.dto.SingleCustomerResponse
import com.iti.itp.bazaar.dto.UpdateCustomerRequest
import com.iti.itp.bazaar.dto.UpdateDraftOrderRequest
import com.iti.itp.bazaar.dto.cutomerResponce.CustomerByEmailResponce
import com.iti.itp.bazaar.dto.cutomerResponce.CustomerResponse
import com.iti.itp.bazaar.dto.order.Order
import com.iti.itp.bazaar.network.responses.CouponsCountResponse
import com.iti.itp.bazaar.network.responses.DiscountCodesResponse
import com.iti.itp.bazaar.network.responses.OrdersResponse
import com.iti.itp.bazaar.network.responses.PriceRulesCountResponse
import com.iti.itp.bazaar.network.responses.PriceRulesResponse
import com.iti.itp.bazaar.network.responses.ProductResponse
import com.iti.itp.bazaar.network.responses.SmartCollectionsResponse
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.Response

class ShopifyRemoteDataSource(private val productService: ProductService) {

    suspend fun getVendors(): SmartCollectionsResponse{
       return productService.getVendors()
    }

    suspend fun getVendorProducts(vendorName:String):ProductResponse{
        return productService.getVendorProducts(vendorName)
    }

    suspend fun addAddress(customerId: Long, address:AddAddressResponse):AddAddressResponse{
        return productService.addAddress(customerId, address)
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
    suspend fun getCollectionProducts(id:Long):ProductResponse{
        return productService.getCollectionProducts(id)
    }

    suspend fun getAddressForCustomer(customerId:Long):ListOfAddresses{
        return productService.getAddressForCustomer(customerId)
    }
    suspend fun getProductDetails(id: Long):ProductResponse{
        return productService.getProductDetails (id)
    }

    suspend fun postCustomer (customer : CustomerRequest): CustomerResponse {
        return productService.postCustomer (customer)
    }

    suspend fun updateCustomerAddress(customerId:Long, addressId:Long, customerAddress: CustomerAddressResponse):CustomerAddressResponse{
        return productService.updateCustomerAddress(customerId, addressId,customerAddress)
    }
    suspend fun getAllProducts () : ProductResponse{
        return productService.getAllProducts()
    }

    suspend fun createDraftOrder(draftOrderRequest:DraftOrderRequest):ReceivedDraftOrder{
        return productService.createDraftOrder(draftOrderRequest)
    }

    suspend fun updateDraftOrder(draftOrderId: Long, updateDraftOrderRequest:UpdateDraftOrderRequest):UpdateDraftOrderRequest{
        return productService.updateDraftOrder(draftOrderId, updateDraftOrderRequest)
    }

    suspend fun getAllDraftOrders():ReceivedOrdersResponse{
        return productService.getAllDraftOrders()
    }
    suspend fun getSpecificDraftOrder(draftOrderId: Long): DraftOrderRequest{
        return productService.getSpecificDraftOrder(draftOrderId)
    }
    suspend fun deleteSpecificDraftOrder (draftOrderId: Long){
        productService.deleteSpecificDraftOrder(draftOrderId)
    }

    suspend fun getCustomerById(customerId: Long):SingleCustomerResponse{
        return productService.getCustomerById(customerId)
    }

    suspend fun deleteAddressOfSpecificCustomer(customerId: Long, addressId: Long){
        return productService.deleteAddressOfSpecificCustomer(customerId, addressId)
    }

    suspend fun getCustomerByEmail(email : String) : CustomerByEmailResponce{
        return productService.getCustomerByEmail (email)
    }
    suspend fun updateCustomerById (customerId : Long , updateCustomerRequest: UpdateCustomerRequest) :
            CustomerResponse{

        return productService.updateCustomerById(customerId,updateCustomerRequest)
    }

    suspend fun getOrdersByCustomerID(query:String): OrdersResponse {
        return productService.getOrdersByCustomerId("customer_id:${query}")
    }

    suspend fun createOrder(partialOrder2: PartialOrder2):Order{
        return productService.createOrder(partialOrder2)
    }


}