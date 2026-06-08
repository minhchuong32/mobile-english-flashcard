package com.example.englishflashcard.config

import android.util.Log
import com.example.englishflashcard.di.AppModule
import com.example.englishflashcard.feature.notification.StudyNotificationHelper
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "Refreshed token: $token")
        
        // Access repository from DI
        val userRepository = AppModule.userRepository
        
        // Save token locally
        userRepository.fcmToken = token
        Log.d("FCM_1", "Refreshed token: $token")
        // If user is logged in, send token to server
        val userId = userRepository.currentUser?._id
        if (userId != null) {
            CoroutineScope(Dispatchers.IO).launch {
                val response = userRepository.updateFcmToken(userId, token)
                Log.d("FCM", "Token updated on server: ${response?.message}")
            }
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d("FCM", "From: ${remoteMessage.from}")

        // Check if message contains a data payload.
        if (remoteMessage.data.isNotEmpty()) {
            Log.d("FCM", "Message data payload: ${remoteMessage.data}")
            val word = remoteMessage.data["word"]
            val ipa = remoteMessage.data["ipa"]
            val meaning = remoteMessage.data["meaning"]

            if (word != null && meaning != null) {
                StudyNotificationHelper.showWordNotification(this, word, ipa, meaning)
            }
        }

        // Check if message contains a notification payload.
        remoteMessage.notification?.let {
            Log.d("FCM", "Message Notification Body: ${it.body}")
            StudyNotificationHelper.showStudyReminder(
                this,
                it.title ?: "Nhắc học flashcard",
                it.body ?: "Đến giờ ôn bài rồi!"
            )
        }
    }
}
