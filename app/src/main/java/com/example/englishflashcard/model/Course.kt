package com.example.englishflashcard.model

data class Course(
    val _id: String = "",
    val teacherId: String = "",
    val title: String = "",
    val description: String = "",
    val thumbnailUrl: String = "",
    val images: List<String> = emptyList(),
    val introVideoUrl: String = "",
    val category: String = "",
    val level: String = "beginner", // "beginner", "intermediate", "advanced"
    val tags: List<String> = emptyList(),
    val price: Double = 0.0,
    val discountPrice: Double? = null,
    val currency: String = "VND",
    val status: String = "draft", // "draft", "pending", "published", "rejected", "archived"
    val reviewNote: String = "",
    val totalLessons: Int = 0,
    val totalEnrollments: Int = 0,
    val totalRevenue: Double = 0.0,
    val averageRating: Double = 0.0,
    val totalRatings: Int = 0,
    val views: Int = 0,
    val publishedAt: String? = null
)
