package com.example.englishflashcard.model

data class Order(
    val _id: String = "",
    val userId: String = "",
    val courseId: String = "",
    val originalPrice: Double = 0.0,
    val discountPrice: Double? = null,
    val finalPrice: Double = 0.0,
    val currency: String = "VND",
    val platformFee: Double = 0.0,
    val teacherRevenue: Double = 0.0,
    val status: String = "pending", // "pending", "paid", "failed", "refunded"
    val paymentMethod: String = "",
    val paymentGatewayId: String = "",
    val paidAt: String? = null,
    val refundedAt: String? = null,
    val refundReason: String = ""
)
