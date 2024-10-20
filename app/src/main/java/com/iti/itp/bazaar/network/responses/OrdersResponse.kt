package com.iti.itp.bazaar.network.responses

import com.iti.itp.bazaar.dto.order.Order

data class OrdersResponse(
    val orders: List<Order>
)