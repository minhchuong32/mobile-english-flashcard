package com.example.englishflashcard

// Các import cần cho xin quyền thông báo, lấy token FCM và dựng UI bằng Compose.
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.remember
import androidx.core.content.ContextCompat
import androidx.navigation.compose.rememberNavController
import com.example.englishflashcard.data.repository.AuthRepository
import com.example.englishflashcard.di.AppModule
import com.example.englishflashcard.navigation.AppNavGraph
import com.example.englishflashcard.ui.theme.EnglishFlashCardTheme
import com.google.firebase.messaging.FirebaseMessaging

// Màn hình khởi động của ứng dụng: xin quyền, lấy FCM token và dựng giao diện chính.
class MainActivity : ComponentActivity() {

    // Launcher dùng để xin quyền thông báo từ người dùng.
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        // Nếu người dùng đồng ý cấp quyền.
        if (isGranted) {
            Log.d("FCM", "Notification permission granted")
        // Nếu người dùng từ chối cấp quyền.
        } else {
            Log.w("FCM", "Notification permission denied")
        }
    }

    // Hàm được gọi khi Activity được tạo lần đầu.
    override fun onCreate(savedInstanceState: Bundle?) {
        // Gọi onCreate của lớp cha trước để hệ thống khởi tạo Activity đúng cách.
        super.onCreate(savedInstanceState)

        // Kiểm tra và xin quyền thông báo ngay khi app mở.
        askNotificationPermission()
        
        // Lấy token FCM để Firebase biết thiết bị này là ai.
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            // Nếu việc lấy token thất bại thì ghi log lỗi và dừng xử lý tiếp.
            if (!task.isSuccessful) {
                Log.w("FCM", "Fetching FCM registration token failed", task.exception)
                return@addOnCompleteListener
            }

            // Lấy token vừa nhận được từ Firebase.
            val token = task.result
            Log.d("FCM", "FCM Token: $token")

            // Lưu token vào repository và đồng bộ lên server nếu cần.
            val userRepository = AppModule.userRepository
            userRepository.fcmToken = token
            userRepository.syncFcmToken()
        }

        // Cho phép giao diện vẽ sát mép màn hình, nhìn hiện đại hơn.
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            )
        )

        // Dựng giao diện Compose chính và điều hướng giữa các màn hình.
        setContent {
            // Dùng theme của app để thống nhất màu sắc, font và style.
            EnglishFlashCardTheme {
                // Tạo navController để chuyển qua lại giữa các màn hình.
                val navController = rememberNavController()

                // Tạo repository cho phần xác thực và giữ nó không tạo lại khi Compose recompose.
                val authRepository = remember {
                    AuthRepository(AppModule.authApiService)
                }

                // Box là khung chứa các thành phần giao diện bên trong.
                Box(
                    modifier = androidx.compose.ui.Modifier
                        // Cho Box chiếm toàn bộ màn hình.
                        .fillMaxSize()
                        // Chừa khoảng trống phía trên để không bị thanh trạng thái che mất nội dung.
                        .statusBarsPadding()
                ) {
                    // Đưa các repository và navController vào hệ thống điều hướng chính của app.
                    AppNavGraph(
                        // Truyền controller để điều hướng giữa các màn hình.
                        navController = navController,
                        // Repository quản lý dữ liệu người dùng.
                        userRepository = AppModule.userRepository,
                        // Repository xác thực: đăng nhập, đăng ký, quên mật khẩu.
                        authRepository = authRepository,
                        // Repository quản lý thẻ học.
                        cardRepository = AppModule.cardRepository,
                        // Repository cho SRS (hệ thống lặp lại ngắt quãng).
                        srsRepository = AppModule.srsRepository,
                        // Repository thống kê và phân tích.
                        analyticsRepository = AppModule.analyticsRepository,
                        // Repository thông báo.
                        notificationRepository = AppModule.notificationRepository,
                        // Repository quản lý bộ thẻ.
                        deckRepository = AppModule.deckRepository,
                        // Repository tra từ điển.
                        dictionaryRepository = AppModule.dictionaryRepository
                    )
                }
            }
        }

    }


    // Hàm kiểm tra và xin quyền thông báo trên Android 13 trở lên.
    private fun askNotificationPermission() {
        // Chỉ cần xin quyền này trên Android 13 (API 33) trở lên.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Kiểm tra xem app đã có quyền gửi thông báo chưa.
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
            ) {
                // Đã có quyền: ứng dụng có thể gửi thông báo.
            } else if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                // Nếu cần, có thể hiển thị giải thích trước khi xin quyền.
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                // Xin quyền trực tiếp từ người dùng.
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}
