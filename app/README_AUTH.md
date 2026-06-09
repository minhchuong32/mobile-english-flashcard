README - Authentication (Auth)

Mục đích
-- Tài liệu ngắn gọn về luồng xác thực (đăng ký, đăng nhập, quên mật khẩu, reset) trong dự án Mobile-English-FlashCard.

Files tham khảo (client):
- API: `app/src/main/java/com/example/englishflashcard/data/api/AuthApiService.kt`
- Repository: `app/src/main/java/com/example/englishflashcard/data/repository/AuthRepository.kt`
- ViewModel: `app/src/main/java/com/example/englishflashcard/feature/auth/AuthViewModel.kt`
- Screens: `feature/auth/*` (LoginScreen, RegisterScreen, ForgotPasswordScreen, VerifyForgotOtpScreen, ResetPasswordScreen)
- Navigation: `AppNavGraph.kt`, `Screen.kt`

Tổng quan luồng hoạt động (ngắn gọn)
- Client gửi request tương ứng từ các screens -> ViewModel -> Repository -> AuthApiService -> Server.
- Server xử lý: kiểm tra user, lưu OTP (khi cần), gửi email, trả về token (access/refresh/reset) hoặc message.

Endpoints chính & logic (mô tả ngắn theo mã nguồn server mẫu)
1) POST /api/auth/login
   - Mục đích: Đăng nhập bằng email hoặc username + password.
   - Logic chính (tóm tắt):
     const loginUser = async (identifier, password) => {
       // Tìm user theo email hoặc username
       const user = await User.findOne({ $or: [{ email: identifier }, { username: identifier }] });
       // Nếu không tồn tại -> lỗi UNAUTHORIZED
       // Nếu chưa verified -> lỗi ACCOUNT_NOT_VERIFIED
       // So sánh mật khẩu (bcrypt)
       // Cập nhật lastLogin và lưu
       // Tạo accessToken (15m) và refreshToken (7d)
       // Trả về { accessToken, refreshToken, redirect_url }
     };

2) POST /api/auth/register
   - Mục đích: Tạo tài khoản mới và gửi OTP xác thực tới email.
   - Logic chính (tóm tắt):
     const registerUser = async (userData) => {
       // Kiểm tra user tồn tại theo email hoặc username -> nếu có lỗi USER_ALREADY_EXISTS
       // Hash mật khẩu, tạo OTP (code + expiresAt) và hash OTP
       // Tạo user mới với isVerified = false, lưu otp.hashed
       // Gửi email chứa mã OTP
       // Trả message: yêu cầu kiểm tra email để lấy OTP
     };

3) POST /api/auth/forgot-password
   - Mục đích: Bắt đầu luồng quên mật khẩu bằng cách gửi mã OTP tới email.
   - Logic chính (tóm tắt):
     const forgotPassword = async (email) => {
       // Tìm user theo email -> nếu không có lỗi EMAIL_NOT_FOUND
       // Tạo OTP, hash OTP và lưu vào user.otp, cập nhật expiresAt
       // Gửi email chứa OTP để xác thực reset
     };

4) POST /api/auth/verify-otp
   - Mục đích: Xác thực mã OTP (dùng cho cả đăng ký và quên mật khẩu).
   - Logic chính (tóm tắt):
     const verifyOtp = async (email, otpCode) => {
       // Tìm user theo email -> nếu không có lỗi USER_NOT_FOUND
       // Kiểm tra tồn tại user.otp và thời hạn (expiresAt)
       // So sánh otpCode với mã đã hash (bcrypt.compare)
       // Nếu là luồng đăng ký (user.isVerified === false): đánh dấu isVerified = true, xóa otp, lưu và trả message thành công
       // Nếu là luồng quên mật khẩu (user đã verified): tạo resetToken JWT (purpose = "reset_password", ~10m) và trả lại resetToken
     };

5) POST /api/auth/reset-password
   - Mục đích: Thiết lập mật khẩu mới sau khi đã xác thực OTP và nhận resetToken.
   - Logic chính (tóm tắt):
     const resetPassword = async (resetToken, newPassword) => {
       // Verify resetToken (JWT) và kiểm tra purpose === "reset_password" (để lọc loại token dùng để reset pass) 
   - -> nếu sai hoặc hết hạn lỗi INVALID_TOKEN
       // Tìm user theo decoded.id -> nếu không có lỗi USER_NOT_FOUND
       // Hash mật khẩu mới, xóa user.otp và lưu user
     };

Tokens và nơi lưu (tóm tắt client-side)
- accessToken: JWT short-lived (~15 phút) dùng để xác thực request API; nên lưu tạm an toàn (ví dụ EncryptedSharedPreferences) và gửi trong header `Authorization: Bearer <token>`.
- refreshToken: JWT long-lived (~7 ngày) để cấp lại accessToken (nếu server hỗ trợ refresh); lưu an toàn (EncryptedSharedPreferences / Keystore) hoặc server-side cookie theo yêu cầu bảo mật.
- resetToken: JWT mục đích đặc biệt (purpose = reset_password), thời hạn ngắn (~10 phút); KHÔNG lưu lâu dài trên client — chỉ dùng tạm để gọi endpoint reset-password.

Luồng ngắn (client-side)
- Register: RegisterScreen -> AuthViewModel.register() -> AuthRepository.register() -> AuthApiService.register() -> server gửi OTP -> navigate VerifyOtp
- Verify OTP (đăng ký): VerifyForgotOtpScreen -> AuthViewModel.verifyOtp() -> server trả success -> chuyển về Login
- Forgot password: ForgotPasswordScreen -> gửi email -> server gửi OTP -> VerifyOtp -> server trả resetToken -> ResetPasswordScreen (gửi resetToken + newPassword)
- Login: LoginScreen -> AuthViewModel.login() -> AuthRepository.login() -> AuthApiService.login() -> nếu success lưu token + navigate Home

Lưu ý client (best practices)
- Lưu token an toàn: EncryptedSharedPreferences / Keystore. Tránh lưu plain text.
- Thêm `AuthInterceptor` (OkHttp) để tự động thêm header `Authorization: Bearer <accessToken>` cho các request cần xác thực.
- Xử lý 401: nếu backend hỗ trợ refresh token, gọi endpoint refresh; nếu không, force logout và yêu cầu đăng nhập lại.
- Khi `verify-otp` trả `resetToken`, giữ token tạm (in-memory) và dùng ngay cho reset-password — tránh lưu resetToken lâu trên storage.
- Map các mã lỗi server (UNAUTHORIZED, USER_ALREADY_EXISTS, INVALID_OTP, OTP_EXPIRED, EMAIL_NOT_FOUND, INVALID_TOKEN, USER_NOT_FOUND, ACCOUNT_NOT_VERIFIED) sang thông báo người dùng thân thiện.

Gợi ý cải tiến
- Scope ViewModel theo NavGraph hoặc dùng `hiltViewModel()` để quản lý lifecycle tốt hơn.
- Thêm `AuthInterceptor` vào `AppModule.kt` và xử lý refresh token ở tầng mạng (network layer).
- Mã hóa/điền thêm các kiểm tra bảo mật như rate-limit OTP request, giới hạn thử OTP, log suspicious activities.
