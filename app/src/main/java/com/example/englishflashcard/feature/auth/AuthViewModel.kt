package com.example.englishflashcard.feature.auth

// Hỗ trợ cú pháp `by` cho state của Compose.
import androidx.compose.runtime.getValue
// Tạo state có thể quan sát để UI tự cập nhật khi giá trị đổi.
import androidx.compose.runtime.mutableStateOf
// Hỗ trợ gán giá trị cho state dùng `by`.
import androidx.compose.runtime.setValue
// Repository xử lý API xác thực (login/register/otp/reset password).
import com.example.englishflashcard.data.repository.AuthRepository
// Repository quản lý deck; dùng để tải lại dữ liệu sau khi login thành công.
import com.example.englishflashcard.data.repository.DeckRepository

// ViewModel tự quản state cho các màn hình auth.
class AuthViewModel(
    // Inject repository deck để lấy dữ liệu học sau khi đăng nhập.
    private val deckRepository: DeckRepository,
    // Inject repository auth để gọi API xác thực.
    private val authRepository: AuthRepository
) {
    // Tên người dùng dùng ở màn đăng ký.
    var name by mutableStateOf("")
    // Email dùng cho đăng ký/quên mật khẩu/xác thực OTP.
    var email by mutableStateOf("")
    // Identifier có thể là email hoặc username khi login.
    var identifier by mutableStateOf("")
    // Mật khẩu cho login/register.
    var password by mutableStateOf("")
    // Mã OTP 6 chữ số.
    var otp by mutableStateOf("")
    // Cờ đánh dấu đã gửi OTP thành công hay chưa.
    var isOtpSent by mutableStateOf(false)

    // Thông báo hiển thị lên UI (lỗi hoặc thành công).
    var message by mutableStateOf("")
    // Cờ loading để disable nút và hiện progress.
    var isLoading by mutableStateOf(false)
    // Cờ xác định thông báo hiện tại là lỗi hay không.
    var isError by mutableStateOf(false)
    // Token tạm để gọi API reset password.
    var resetToken by mutableStateOf("")
    // Mật khẩu mới ở màn reset password.
    var newPassword by mutableStateOf("")

    // Xử lý đăng nhập; trả về true nếu thành công.
    suspend fun login(): Boolean {
        // Chuẩn hóa input: bỏ khoảng trắng đầu/cuối.
        val normalizedIdentifier = identifier.trim()
        // Chuẩn hóa mật khẩu: bỏ khoảng trắng đầu/cuối.
        val normalizedPassword = password.trim()

        // Validate: thiếu email/username thì báo lỗi và dừng.
        if (normalizedIdentifier.isBlank()) {
            message = "Vui lòng nhập email hoặc tên đăng nhập"
            isError = true
            return false
        }

        // Validate: thiếu mật khẩu thì báo lỗi và dừng.
        if (normalizedPassword.isBlank()) {
            message = "Vui lòng nhập mật khẩu"
            isError = true
            return false
        }

        // Bật trạng thái loading trước khi gọi API.
        isLoading = true
        // Xóa message cũ trước khi xử lý yêu cầu mới.
        message = ""
        // Reset trạng thái lỗi về false trước khi gọi API.
        isError = false

        // Dùng try/finally để luôn tắt loading dù thành công hay lỗi.
        return try {
            // Gọi API login qua repository.
            val (success, result) = authRepository.login(
                identifier = normalizedIdentifier,
                password = normalizedPassword
            )

            // Nếu login thành công thì tải dữ liệu deck từ server.
            if (success) {
                // Không để lỗi fetch deck làm fail luồng login.
                runCatching { deckRepository.fetchDecksRemote() }
                // Đặt lại cờ OTP vì đăng nhập không còn trong luồng OTP.
                isOtpSent = false
            }

            // Cập nhật thông báo trả về để UI hiển thị.
            message = result
            // Đảo cờ lỗi theo kết quả success.
            isError = !success
            // Trả về kết quả cho UI quyết định điều hướng.
            success
        } finally {
            // Luôn tắt loading khi kết thúc request.
            isLoading = false
        }
    }

    // Xử lý đăng ký tài khoản từ server.
    suspend fun registerRemote(): Boolean {
        // Chuẩn hóa các input trước khi validate/gọi API.
        val normalizedName = name.trim()
        val normalizedEmail = email.trim()
        val normalizedPassword = password.trim()

        // Validate tên đăng nhập.
        if (normalizedName.isBlank()) {
            message = "Vui lòng nhập tên đăng nhập"
            isError = true
            return false
        }

        // Validate email.
        if (normalizedEmail.isBlank()) {
            message = "Vui lòng nhập email"
            isError = true
            return false
        }

        // Validate mật khẩu.
        if (normalizedPassword.isBlank()) {
            message = "Vui lòng nhập mật khẩu"
            isError = true
            return false
        }

        // Bật loading trước khi gọi API.
        isLoading = true
        // Xóa thông báo cũ.
        message = ""
        // Reset trạng thái lỗi.
        isError = false

        // Gọi API đăng ký và luôn tắt loading ở finally.
        return try {
            val (success, result) = authRepository.register(
                username = normalizedName,
                email = normalizedEmail,
                password = normalizedPassword
            )

            // Ghi thông báo phản hồi từ server.
            message = result
            // Cập nhật cờ lỗi dựa trên kết quả.
            isError = !success

            // Nếu đăng ký thành công thì chuyển sang bước OTP.
            if (success) {
                isOtpSent = true
                // Xóa OTP cũ để người dùng nhập mã mới.
                otp = ""
            }

            // Trả về true/false cho UI.
            success
        } finally {
            // Luôn tắt loading.
            isLoading = false
        }
    }

    // Xác thực OTP và nhận reset token (nếu backend trả về).
    suspend fun verifyOtp(): Boolean {

        // OTP hợp lệ theo rule hiện tại phải đủ 6 ký tự.
        if (otp.length != 6) {
            message = "OTP không hợp lệ"
            isError = true
            return false
        }

        // Bật loading khi bắt đầu gọi API.
        isLoading = true

        return try {

            // Gọi API verify OTP bằng email + mã OTP.
            val result =
                authRepository.verifyOtp(
                    email,
                    otp
                )

            // Hiển thị thông báo server trả về.
            message = result.message
            // Đánh dấu lỗi nếu verify thất bại.
            isError = !result.success

            // Nếu có resetToken thì lưu lại cho bước đổi mật khẩu.
            result.resetToken?.let {
                resetToken = it
            }

            // Trả về kết quả verify.
            result.success

        } finally {
            // Luôn tắt loading.
            isLoading = false
        }
    }

    // Gửi yêu cầu quên mật khẩu (gửi OTP/link theo backend).
    suspend fun forgotPassword(): Boolean {

        // Validate email trước khi gọi API.
        if (email.isBlank()) {
            message = "Vui lòng nhập email"
            isError = true
            return false
        }

        // Bật loading trong lúc request.
        isLoading = true

        return try {

            // Gọi API quên mật khẩu.
            val (success, result) =
                authRepository.forgotPassword(email)

            // Cập nhật message để UI thông báo cho người dùng.
            message = result
            // Cập nhật cờ lỗi theo success.
            isError = !success

            // Trả về kết quả cho màn hình quyết định điều hướng.
            success

        } finally {
            // Luôn tắt loading sau request.
            isLoading = false
        }
    }

    // Đặt lại mật khẩu bằng resetToken đã xác thực trước đó.
    suspend fun resetPassword(): Boolean {

        // Validate mật khẩu mới tối thiểu 6 ký tự.
        if (newPassword.length < 6) {
            message = "Mật khẩu phải từ 6 ký tự"
            isError = true
            return false
        }

        // Bật loading khi gọi API.
        isLoading = true

        return try {

            // Gọi API reset password với token + mật khẩu mới.
            val (success, result) =
                authRepository.resetPassword(
                    resetToken,
                    newPassword
                )

            // Cập nhật thông báo trả về từ server.
            message = result
            // Đánh dấu lỗi nếu thao tác thất bại.
            isError = !success

            // Trả kết quả để UI xử lý tiếp.
            success

        } finally {
            // Luôn tắt loading sau khi hoàn tất.
            isLoading = false
        }
    }
}