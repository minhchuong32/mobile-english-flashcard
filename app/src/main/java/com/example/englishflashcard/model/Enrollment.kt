package com.example.englishflashcard.model

data class Enrollment(
    val _id: String = "",
    val userId: String = "",
    val courseId: String = "",
    val orderId: String? = null,
    val status: String = "active", // "active", "expired", "refunded"
    val progressPercent: Double = 0.0,
    val lessonsCompleted: Int = 0,
    val lastAccessedAt: String? = null,
    val completedAt: String? = null,
    val rating: Double? = null,
    val review: String = "",
    val reviewedAt: String? = null,
    val accessExpiresAt: String? = null
)
