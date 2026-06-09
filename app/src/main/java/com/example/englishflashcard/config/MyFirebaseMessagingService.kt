package com.example.englishflashcard.config

// Ghi log để debug quá trình nhận token và thông báo từ Firebase.
import android.util.Log
// AppModule cung cấp repository dùng chung cho toàn app.
import com.example.englishflashcard.di.AppModule
// Helper hiển thị notification tùy theo nội dung nhận được.
import com.example.englishflashcard.feature.notification.StudyNotificationHelper
// Service của Firebase dùng để nhận token mới và message đẩy.
import com.google.firebase.messaging.FirebaseMessagingService
// Dữ liệu message từ Firebase.
import com.google.firebase.messaging.RemoteMessage
// Coroutine để gọi API cập nhật token ở background thread.
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

// Service xử lý FCM khi app nhận token mới hoặc nhận message.
class MyFirebaseMessagingService : FirebaseMessagingService() {

    // Được gọi khi Firebase tạo hoặc refresh token cho thiết bị.
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // Ghi log token mới để kiểm tra.
        Log.d("FCM", "Refreshed token: $token")
        
        // Lấy repository người dùng từ AppModule.
        val userRepository = AppModule.userRepository
        
        // Lưu token vào máy để dùng lại khi cần.
        userRepository.fcmToken = token
        // Log thêm một lần để dễ debug.
        Log.d("FCM_1", "Refreshed token: $token")
        // Nếu người dùng đã đăng nhập thì đồng bộ token lên server.
        val userId = userRepository.currentUser?._id
        if (userId != null) {
            // Chạy network request ở luồng nền.
            CoroutineScope(Dispatchers.IO).launch {
                // Cập nhật token FCM của user trên server.
                val response = userRepository.updateFcmToken(userId, token)
                // Log kết quả để kiểm tra việc sync thành công hay chưa.
                Log.d("FCM", "Token updated on server: ${response?.message}")
            }
        }
    }

    // Được gọi khi app nhận message push từ Firebase.
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        // Log nơi gửi message đến.
        Log.d("FCM", "From: ${remoteMessage.from}")

        // Nếu message có data payload thì xử lý theo dữ liệu tùy biến.
        if (remoteMessage.data.isNotEmpty()) {
            // Ghi log toàn bộ data để debug.
            Log.d("FCM", "Message data payload: ${remoteMessage.data}")
            // Từ vựng cần hiển thị trên notification.
            val word = remoteMessage.data["word"]
            // Phiên âm của từ.
            val ipa = remoteMessage.data["ipa"]
            // Nghĩa của từ.
            val meaning = remoteMessage.data["meaning"]

            // Chỉ hiển thị notification nếu có đủ dữ liệu chính.
            if (word != null && meaning != null) {
                // Hiển thị notification từ vựng cho người dùng.
                StudyNotificationHelper.showWordNotification(this, word, ipa, meaning)
            }
        }

        // Nếu message có notification payload thì hiển thị nhắc học.
        remoteMessage.notification?.let {
            // Log nội dung notification để debug.
            Log.d("FCM", "Message Notification Body: ${it.body}")
            // Hiển thị thông báo nhắc học với tiêu đề/nội dung mặc định nếu thiếu.
            StudyNotificationHelper.showStudyReminder(
                this,
                it.title ?: "Nhắc học flashcard",
                it.body ?: "Đến giờ ôn bài rồi!"
            )
        }
    }
}
