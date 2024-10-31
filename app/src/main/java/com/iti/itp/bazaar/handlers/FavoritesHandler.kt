package com.iti.itp.bazaar.handlers

import android.content.SharedPreferences
import androidx.lifecycle.viewModelScope
import com.example.productinfoform_commerce.productInfo.viewModel.ProductInfoViewModel
import com.iti.itp.bazaar.auth.MyConstants
import com.iti.itp.bazaar.dto.*
import com.iti.itp.bazaar.mainActivity.ui.DataState
import com.iti.itp.bazaar.network.products.Products
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FavoritesHandler(
    private val viewModel: ProductInfoViewModel,
    private val sharedPreferences: SharedPreferences
) {
    private val favDraftOrderId: String = sharedPreferences.getString(MyConstants.FAV_DRAFT_ORDERS_ID, "") ?: ""
    private val customerId: String = sharedPreferences.getString(MyConstants.CUSOMER_ID, "0") ?: "0"
    private var cachedDraftOrder: DraftOrderRequest? = null

    suspend fun initialize() {
        if (cachedDraftOrder == null) {
            viewModel.getSpecificDraftOrder(favDraftOrderId.toLong())
            viewModel.specificDraftOrders.collect { state ->
                when (state) {
                    is DataState.OnSuccess<*> -> {
                        cachedDraftOrder = state.data as DraftOrderRequest
                    }
                    else -> {} // Handle other states if needed
                }
            }
        }
    }

    fun addProductToFavorites(
        product: Products,
        onAdded: () -> Unit,
        onAlreadyExists: () -> Unit
    ) {
        viewModel.viewModelScope.launch {
            try {
                // Use cached draft order if available, otherwise fetch it
                val draftOrder = cachedDraftOrder ?: run {
                    viewModel.getSpecificDraftOrder(favDraftOrderId.toLong())
                    val result = viewModel.specificDraftOrders.first { it is DataState.OnSuccess<*> }
                    (result as DataState.OnSuccess<*>).data as DraftOrderRequest
                }

                val isInFavorites = isProductInFavorites(draftOrder, product.id.toString())

                if (isInFavorites) {
                    withContext(Dispatchers.Main) {
                        onAlreadyExists()
                    }
                    return@launch
                }

                // Add to favorites
                val updatedLineItems = draftOrder.draft_order.line_items + LineItem(
                    sku = "${product.id}##${product.image?.src}",
                    id = product.id,
                    variant_title = product.variants[0].title,
                    product_id = product.id,
                    title = product.title,
                    price = product.variants[0].price,
                    quantity = 1
                )

                val updateRequest = UpdateDraftOrderRequest(
                    DraftOrder(
                        line_items = updatedLineItems,
                        applied_discount = null,
                        customer = Customer(customerId.toLong()),
                        use_customer_default_address = true
                    )
                )

                viewModel.updateDraftOrder(favDraftOrderId.toLong(), updateRequest)

                // Update cache
                cachedDraftOrder = DraftOrderRequest(updateRequest.draft_order)

                withContext(Dispatchers.Main) {
                    onAdded()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    // Handle error case
                    e.printStackTrace()
                }
            }
        }
    }

     fun isProductInFavorites(draftOrder: DraftOrderRequest, productId: String): Boolean {
        return draftOrder.draft_order.line_items.any { lineItem ->
            val skuParts = lineItem.sku?.split("##")
            skuParts?.firstOrNull() == productId
        }
    }

    fun removeFromFavorites(
        product: Products,
        onRemoved: () -> Unit
    ) {
        viewModel.viewModelScope.launch {
            try {
                val draftOrder = cachedDraftOrder ?: run {
                    viewModel.getSpecificDraftOrder(favDraftOrderId.toLong())
                    val result = viewModel.specificDraftOrders.first { it is DataState.OnSuccess<*> }
                    (result as DataState.OnSuccess<*>).data as DraftOrderRequest
                }

                // Remove from favorites
                val updatedLineItems = draftOrder.draft_order.line_items.filter { lineItem ->
                    val skuParts = lineItem.sku?.split("##")
                    skuParts?.firstOrNull() != product.id.toString()
                }

                val updateRequest = UpdateDraftOrderRequest(
                    DraftOrder(
                        line_items = updatedLineItems,
                        applied_discount = null,
                        customer = Customer(customerId.toLong()),
                        use_customer_default_address = true
                    )
                )

                viewModel.updateDraftOrder(favDraftOrderId.toLong(), updateRequest)

                // Update cache
                cachedDraftOrder = DraftOrderRequest(updateRequest.draft_order)

                withContext(Dispatchers.Main) {
                    onRemoved()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    // Handle error case
                    e.printStackTrace()
                }
            }
        }
    }
}