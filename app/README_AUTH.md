README - Authentication (Auth) 

Files tham khảo (client):
- API: `app/src/main/java/com/example/englishflashcard/data/api/AuthApiService.kt`
- Repository: `app/src/main/java/com/example/englishflashcard/data/repository/AuthRepository.kt`
- ViewModel: `app/src/main/java/com/example/englishflashcard/feature/auth/AuthViewModel.kt`
- Screens: `feature/auth/*` (LoginScreen, RegisterScreen, ForgotPasswordScreen, VerifyForgotOtpScreen, ResetPasswordScreen)
- Navigation: `AppNavGraph.kt`, `Screen.kt`

Endpoints chính & tóm tắt hành vi
- POST /api/auth/register: tạo user, lưu hashed OTP, gửi OTP tới email. (Response: success message)
- POST /api/auth/verify-otp: xác thực OTP.
  - Nếu user chưa verified -> mark verified, xóa OTP, trả message thành công.
  - Nếu user đã verified (quên mật khẩu flow) -> trả `resetToken` (JWT, ~10m) để dùng cho reset-password.
- POST /api/auth/login: login bằng email hoặc username + password.
  - Kiểm tra tồn tại user, isVerified, so sánh password.
  - Trả về `accessToken` (15m), `refreshToken` (7d) và `redirect_url`.
- POST /api/auth/forgot-password: tạo OTP reset, lưu hashed OTP và gửi email.
- POST /api/auth/reset-password: xác thực `resetToken` (purpose = reset_password) và cập nhật mật khẩu mới.


Luồng ngắn (client-side)
- Register: RegisterScreen -> AuthViewModel.register() -> AuthRepository.register() -> AuthApiService.register() -> server gửi OTP -> navigate VerifyOtp
- Verify OTP: VerifyForgotOtpScreen -> AuthViewModel.verifyOtp() -> AuthRepository.verifyOtp() -> server trả success hoặc resetToken -> nếu success goto Login, nếu resetToken lưu tạm và goto ResetPassword
- Login: LoginScreen -> AuthViewModel.login() -> AuthRepository.login() -> AuthApiService.login() -> nếu success lưu token + navigate Home
- Forgot -> Reset: ForgotScreen -> forgot-password -> server gửi OTP -> VerifyOtp -> server trả resetToken -> ResetPassword -> reset-password -> success -> goto Login

Lưu ý client (best practices)
- Lưu token an toàn: EncryptedSharedPreferences / Keystore. Không lưu plain text.
- Thêm `AuthInterceptor` (OkHttp) để tự động thêm header `Authorization: Bearer <accessToken>` cho request bảo mật.
- Xử lý 401: nếu server có refresh endpoint (không có trong mẫu backend), gọi refresh; nếu không, force logout.
- Khi verify-otp trả `resetToken`, giữ token tạm thời (in-memory) và dùng ngay cho reset-password (không lưu lâu dài).
- Map mã lỗi server (UNAUTHORIZED, USER_ALREADY_EXISTS, INVALID_OTP, OTP_EXPIRED, EMAIL_NOT_FOUND, INVALID_TOKEN, USER_NOT_FOUND, ACCOUNT_NOT_VERIFIED) sang thông báo người dùng thân thiện.

Ví dụ nhanh (pseudo)
POST /api/auth/login { identifier, password } -> { accessToken, refreshToken, redirect_url }
POST /api/auth/register { username, email, password } -> 200 (OTP gửi email)
POST /api/auth/verify-otp { email, otp } -> 200 (hoặc { resetToken })
POST /api/auth/reset-password { resetToken, newPassword } -> 200

Gợi ý cải tiến
- Scope ViewModel theo NavGraph hoặc dùng `hiltViewModel()` thay vì `remember { AuthViewModel(...) }` để quản lý lifecycle tốt hơn.
- Thêm `AuthInterceptor` vào `AppModule.kt` và xử lý refresh token trung tâm.
- Encode route params khi điều hướng (Uri.encode) để tránh lỗi với ký tự đặc biệt.


