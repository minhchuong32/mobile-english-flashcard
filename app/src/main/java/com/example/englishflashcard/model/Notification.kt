package com.example.englishflashcard.model

data class NotificationItem(
    val id: String,
    val title: String,
    val message: String,
    val createdAt: Long = System.currentTimeMillis()
)