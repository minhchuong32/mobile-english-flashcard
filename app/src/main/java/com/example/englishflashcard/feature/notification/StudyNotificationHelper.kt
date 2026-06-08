package com.example.englishflashcard.feature.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import com.example.englishflashcard.MainActivity
import com.example.englishflashcard.R

object StudyNotificationHelper {
    private const val CHANNEL_ID = "study_reminder_channel"
    private const val CHANNEL_NAME = "Nhắc nhở học tập"
    private const val NOTIFICATION_ID = 1001

    fun showStudyReminder(
        context: Context,
        title: String = "Nhắc học flashcard",
        message: String = "Đến giờ ôn bài rồi!"
    ) {
        showNotification(context, title, message, NOTIFICATION_ID)
    }

    fun showWordNotification(
        context: Context,
        word: String,
        ipa: String?,
        meaning: String
    ) {
        val title = word
        val message = if (!ipa.isNullOrBlank()) "[$ipa] $meaning" else meaning
        
        val bigText = StringBuilder()
        if (!ipa.isNullOrBlank()) {
            bigText.append("Phát âm: $ipa\n\n")
        }
        bigText.append("Ý nghĩa: $meaning")

        showNotification(
            context = context,
            title = title,
            message = message,
            notificationId = word.hashCode(),
            bigText = bigText.toString(),
            subText = "Từ vựng hôm nay"
        )
    }

    private fun showNotification(
        context: Context,
        title: String,
        message: String,
        notificationId: Int,
        bigText: String? = null,
        subText: String? = null
    ) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Kênh gửi các thông báo nhắc nhở học từ vựng"
                enableLights(true)
                lightColor = Color.GREEN
            }
            manager.createNotificationChannel(channel)
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context, 
            0, 
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val largeIcon = ContextCompat.getDrawable(context, R.drawable.ic_notification)?.toBitmap()

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_small)
            .setLargeIcon(largeIcon)
            .setContentTitle(title)
            .setContentText(message)
            .setSubText(subText)
            .setAutoCancel(true)
            .setColor(ContextCompat.getColor(context, R.color.emerald_primary))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setContentIntent(pendingIntent)

        if (bigText != null) {
            builder.setStyle(NotificationCompat.BigTextStyle().bigText(bigText))
        }

        manager.notify(notificationId, builder.build())
    }
}
