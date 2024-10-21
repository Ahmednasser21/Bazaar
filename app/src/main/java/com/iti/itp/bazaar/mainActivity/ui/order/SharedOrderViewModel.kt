package com.iti.itp.bazaar.mainActivity.ui.order

import androidx.lifecycle.ViewModel
import com.iti.itp.bazaar.dto.OrderAddress
import com.iti.itp.bazaar.dto.OrderAppliedDiscount
import com.iti.itp.bazaar.dto.OrderCustomer
import com.iti.itp.bazaar.dto.OrderLineItem
import com.iti.itp.bazaar.dto.PartialOrder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class SharedOrderViewModel : ViewModel() {
    private val _partialOrder = MutableStateFlow(PartialOrder())
    val partialOrder: StateFlow<PartialOrder> = _partialOrder

    fun updateCustomer(customer: OrderCustomer) {
        _partialOrder.value = _partialOrder.value.copy(customer = customer)
    }

    fun updateLineItems(lineItems: List<OrderLineItem>) {
        _partialOrder.value = _partialOrder.value.copy(line_items = lineItems)
    }

    fun updateAppliedDiscount(appliedDiscount: OrderAppliedDiscount) {
        _partialOrder.value = _partialOrder.value.copy(applied_discount = appliedDiscount)
    }

    fun updateBillingAddress(billingAddress: OrderAddress) {
        _partialOrder.value = _partialOrder.value.copy(billing_address = billingAddress)
    }

    fun updateShippingAddress(shippingAddress: OrderAddress) {
        _partialOrder.value = _partialOrder.value.copy(shipping_address = shippingAddress)
    }

    fun updatePaymentGateway(paymentGatewayNames: List<String>) {
        _partialOrder.value = _partialOrder.value.copy(payment_gateway_names = paymentGatewayNames)
    }
}