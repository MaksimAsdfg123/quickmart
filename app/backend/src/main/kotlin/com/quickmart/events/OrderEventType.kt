package com.quickmart.events

enum class OrderEventType(
    val code: String,
) {
    CREATED("order.created"),
    CANCELLED("order.cancelled"),
    STATUS_CHANGED("order.status_changed"),
}
