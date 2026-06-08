package com.example.englishflashcard.model

data class Revenue(
    val _id: String = "",
    val period: String = "daily", // "daily", "monthly"
    val periodKey: String = "",
    val scope: String = "platform", // "platform", "teacher"
    val teacherId: String? = null,
    val totalOrders: Int = 0,
    val totalGrossRevenue: Double = 0.0,
    val totalPlatformFee: Double = 0.0,
    val totalTeacherPayout: Double = 0.0,
    val totalRefunded: Double = 0.0,
    val netRevenue: Double = 0.0,
    val topCourses: List<TopCourseItem> = emptyList()
)

data class TopCourseItem(
    val courseId: String = "",
    val title: String = "",
    val orders: Int = 0,
    val revenue: Double = 0.0
)
