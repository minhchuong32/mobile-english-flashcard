package com.example.englishflashcard.data.repository

import androidx.compose.runtime.mutableStateListOf
import com.example.englishflashcard.model.NotificationItem
import java.util.UUID

class NotificationRepository {
    private val notifications = mutableStateListOf<NotificationItem>()

    fun addNotification(title: String, message: String) {
        notifications.add(
            NotificationItem(
                id = UUID.randomUUID().toString(),
                title = title,
                message = message
            )
        )
    }

    fun getNotifications(): List<NotificationItem> = notifications.toList()
}
