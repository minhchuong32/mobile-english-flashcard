# Báo cáo dự án Mobile English Flashcard

Tài liệu này tóm tắt các chức năng chính, logic code và luồng hoạt động của dự án `Mobile-English-FlashCard` để phục vụ báo cáo.

---

## 1. Tổng quan dự án

Ứng dụng được xây dựng bằng **Android + Jetpack Compose** theo hướng **MVVM** và **feature-first**.

Mục tiêu của app:

- Hỗ trợ học từ vựng tiếng Anh bằng flashcard.
- Quản lý deck và card.
- Luyện tập theo SRS (Spaced Repetition System).
- Đăng nhập, đăng ký, quên mật khẩu, reset mật khẩu.
- Tra từ điển, thống kê học tập, thông báo nhắc học.

Mô hình xử lý dữ liệu:

`UI (Compose) -> ViewModel -> Repository -> API / Local data`

---

## 2. Cấu trúc chính của dự án

- `MainActivity.kt`: điểm khởi động ứng dụng, xin quyền thông báo, lấy FCM token, dựng UI Compose.
- `navigation/AppNavGraph.kt`: khai báo toàn bộ luồng điều hướng.
- `feature/auth/`: đăng nhập, đăng ký, quên mật khẩu, OTP, reset mật khẩu.
- `feature/deck/`: tạo deck, xem deck, thêm card, xem chi tiết deck.
- `feature/flashcard/`: màn học flashcard.
- `feature/exercise/`: màn luyện tập.
- `feature/profile/`: hồ sơ người dùng.
- `feature/analytic/`: thống kê và báo cáo học tập.
- `feature/notification/`: xử lý thông báo nhắc học.
- `data/repository/`: lớp trung gian giữa UI và dữ liệu.

---

## 3. Luồng khởi động ứng dụng

### `MainActivity.kt`

Luồng chính khi app mở:

1. Xin quyền thông báo nếu thiết bị hỗ trợ.
2. Lấy FCM token từ Firebase.
3. Lưu token vào repository và đồng bộ lên server.
4. Tạo giao diện Compose chính bằng `setContent {}`.
5. Gọi `AppNavGraph` để điều hướng giữa các màn hình.

### Ý nghĩa

- `rememberNavController()` tạo controller điều hướng.
- `AppModule` cung cấp các repository singleton.
- `EnglishFlashCardTheme` bao toàn bộ UI bằng theme chung.

---

## 4. Navigation và luồng điều hướng

### Vai trò của `AppNavGraph`

`AppNavGraph` là nơi khai báo tất cả route của app. Đây là “bản đồ điều hướng” giữa các màn hình.

### Các khái niệm chính

- `NavHost`: chứa các destination.
- `NavController`: điều hướng qua lại giữa màn hình.
- `composable(...)`: khai báo từng màn hình.
- `popUpTo(...)`: xóa hoặc cắt back stack khi chuyển màn.

### Luồng navigation chính

#### 4.1. Luồng đăng nhập

- Màn đầu tiên: `Screen.Login`
- Từ Login:
  - `onGoRegister` -> `Screen.Register`
  - `onForgotPasswordClick` -> `Screen.ForgotPassword`
  - `onLoginSuccess` -> xóa cache SRS -> vào `Screen.Home`

#### 4.2. Luồng đăng ký

- Người dùng nhập username, email, mật khẩu.
- App gọi API đăng ký.
- Nếu server yêu cầu OTP, user đi theo luồng xác thực.
- Sau khi xong:
  - nếu có `resetToken` -> `Screen.ResetPassword`
  - nếu không -> `Screen.Home`

#### 4.3. Luồng quên mật khẩu

- `Screen.ForgotPassword` -> nhập email -> gửi OTP.
- `Screen.VerifyForgotOtp` -> nhập OTP để xác thực.
- Nếu OTP đúng -> chuyển sang `Screen.ResetPassword`.
- Sau khi đổi mật khẩu xong -> quay về `Screen.Login`.

#### 4.4. Luồng chính sau đăng nhập

- `Screen.Home`: màn shell chính.
- Từ đây người dùng có thể đi tới:
  - `Screen.Decks`
  - `Screen.Profile`
  - `Screen.DeckExplorer`

#### 4.5. Luồng màn hình có tham số

- `Screen.DeckDetail(deckId)`
- `Screen.AddCard(deckId)`
- `Screen.FlashcardStudy(mode, deckId?)`
- `Screen.Exercise(deckId?)`

Các màn này lấy dữ liệu từ route arguments để hiển thị đúng nội dung theo deck.

---

## 5. Báo cáo từng chức năng chính

## 5.1. Chức năng đăng nhập

### Màn hình liên quan

- `LoginScreen`

### Luồng hoạt động

1. Người dùng nhập email/username và mật khẩu.
2. `AuthViewModel.login()` kiểm tra dữ liệu đầu vào.
3. `AuthRepository.login()` gọi API.
4. Nếu thành công:
   - tải lại deck từ server,
   - xóa cache SRS,
   - chuyển sang Home.

### Logic code chính

- Tìm user theo `email` hoặc `username`.
- Kiểm tra tài khoản đã xác minh chưa.
- So sánh mật khẩu bằng `bcrypt.compare()`.
- Tạo `accessToken` và `refreshToken` bằng JWT.
- Trả về `redirect_url` theo role.

### Kết quả

- Đăng nhập thành công -> vào giao diện chính.
- Đăng nhập thất bại -> hiển thị lỗi.

---

## 5.2. Chức năng đăng ký

### Màn hình liên quan

- `RegisterScreen`

### Luồng hoạt động

1. Người dùng nhập username, email, mật khẩu.
2. `AuthViewModel.registerRemote()` validate dữ liệu.
3. `AuthRepository.register()` gọi API.
4. Backend kiểm tra user đã tồn tại hay chưa.
5. Nếu hợp lệ:
   - hash mật khẩu,
   - tạo OTP,
   - lưu user với `isVerified = false`,
   - gửi OTP qua email.

### Logic code chính

- Kiểm tra trùng email/username.
- Hash password trước khi lưu.
- Hash OTP trước khi lưu DB.
- Gửi mail OTP cho user.

### Kết quả

- User được tạo mới nhưng chưa thể đăng nhập ngay nếu chưa xác thực.

---

## 5.3. Chức năng quên mật khẩu

### Màn hình liên quan

- `ForgotPasswordScreen`
- `VerifyForgotOtpScreen`
- `ResetPasswordScreen`

### Luồng hoạt động

1. User nhập email.
2. Hệ thống tạo OTP mới.
3. OTP được gửi qua email.
4. User nhập OTP để xác thực.
5. Nếu đúng, backend trả về `resetToken`.
6. User dùng `resetToken` để đặt mật khẩu mới.

### Logic code chính

- `forgotPassword(email)`:
  - tìm user theo email,
  - tạo OTP mới,
  - hash OTP,
  - lưu vào user,
  - gửi email.

- `verifyOtp(email, otpCode)`:
  - kiểm tra OTP tồn tại,
  - kiểm tra hết hạn,
  - so sánh OTP.
  - nếu là luồng quên mật khẩu -> tạo `resetToken`.

- `resetPassword(resetToken, newPassword)`:
  - verify JWT token,
  - kiểm tra `purpose = reset_password`,
  - hash mật khẩu mới,
  - lưu lại mật khẩu mới.

### Kết quả

- User có thể đặt lại mật khẩu mà không cần mật khẩu cũ.

---

## 5.4. Chức năng quản lý deck

### Màn hình liên quan

- `DecksScreen`
- `DeckDetailScreen`
- `CreateDeckScreen`
- `DeckExplorerScreen`

### Luồng hoạt động

1. Người dùng vào danh sách deck.
2. Chọn deck để xem chi tiết.
3. Có thể tạo deck mới.
4. Có thể duyệt deck ở màn khám phá.

### Logic code chính

- `DeckRepository` quản lý dữ liệu deck.
- `DeckDetailScreen` hiển thị thông tin deck và card trong deck.
- `CreateDeckScreen` dùng để tạo deck mới.

### Kết quả

- Người dùng quản lý bộ học của mình theo từng chủ đề.

---

## 5.5. Chức năng quản lý card

### Màn hình liên quan

- `AddCardScreen`

### Luồng hoạt động

1. User mở deck detail.
2. Chọn thêm card vào deck.
3. Nhập nội dung thẻ.
4. Lưu card vào repository / backend.

### Logic code chính

- `CardRepository` chịu trách nhiệm thêm/sửa/xóa card.
- `DictionaryRepository` hỗ trợ tra nghĩa từ khi tạo card.

### Kết quả

- Card mới được gắn vào deck tương ứng.

---

## 5.6. Chức năng học flashcard

### Màn hình liên quan

- `FlashcardScreen`

### Luồng hoạt động

1. User chọn học flashcard.
2. App lấy danh sách card theo mode hoặc deck.
3. Hiển thị card để học từng thẻ.
4. Ghi nhận hành vi học để phục vụ SRS và analytics.

### Logic code chính

- `FlashcardViewModel` quản lý state học.
- `SrsRepository` hỗ trợ dữ liệu ôn tập.
- `AnalyticsRepository` ghi nhận hoạt động học.

### Kết quả

- Người học xem và lật thẻ theo trình tự.

---

## 5.7. Chức năng luyện tập / exercise

### Màn hình liên quan

- `ExerciseScreen`

### Luồng hoạt động

1. User chọn một deck để luyện tập.
2. App lấy danh sách card cần ôn.
3. Người dùng trả lời bài tập.
4. Kết quả được ghi nhận và cập nhật trạng thái học.

### Logic code chính

- `ExerciseViewModel` quản lý phiên luyện tập.
- `SrsRepository` cập nhật lịch ôn.
- `AnalyticsRepository` ghi lại kết quả.

### Kết quả

- Hỗ trợ học có kiểm tra và theo dõi tiến độ.

---

## 5.8. Chức năng hồ sơ người dùng

### Màn hình liên quan

- `ProfileScreen`

### Luồng hoạt động

1. User mở màn profile.
2. App tải thông tin cá nhân.
3. Cho phép đăng xuất.

### Logic code chính

- `ProfileViewModel` quản lý dữ liệu cá nhân.
- `UserRepository.logout()` xóa trạng thái đăng nhập.

### Kết quả

- Người dùng xem thông tin và thoát tài khoản.

---

## 5.9. Chức năng thông báo

### Thành phần liên quan

- `StudyNotificationHelper`
- `FirebaseMessaging`

### Luồng hoạt động

1. App xin quyền thông báo.
2. Lấy FCM token của thiết bị.
3. Lưu token vào repository.
4. Đồng bộ token lên server.

### Logic code chính

- `MainActivity` xử lý xin quyền và lấy token.
- `NotificationRepository` và helper xử lý thông báo nhắc học.

### Kết quả

- App có thể gửi nhắc học cho người dùng.

---

## 5.10. Chức năng thống kê / analytics

### Màn hình liên quan

- `AnalyticsScreen`

### Luồng hoạt động

1. App ghi nhận hoạt động học.
2. Dữ liệu được xử lý bởi repository.
3. Màn analytics hiển thị thống kê.

### Logic code chính

- `AnalyticsRepository` lưu/đọc dữ liệu thống kê.
- `AnalyticsViewModel` cung cấp state cho UI.

### Kết quả

- Người dùng biết tiến độ học, số phiên học, hiệu suất.

---

## 6. Các khái niệm quan trọng trong project

### 6.1. Jetpack Compose

**Jetpack Compose** là framework khai báo UI mới của Android, thay thế XML layouts truyền thống.

- **Ưu điểm**: code UI ngắn gọn, dễ tái sử dụng, tự động cập nhật UI khi state thay đổi.
- **Cách dùng**: định nghĩa UI bằng hàm Kotlin thay vì viết XML.
- **Ví dụ**: 
  ```kotlin
  @Composable
  fun LoginScreen() {
      Column(modifier = Modifier.fillMaxSize()) {
          Text("Hello")
          Button(onClick = { }) { Text("Click") }
      }
  }
  ```

### 6.2. Composable

`@Composable` là hàm tạo UI trong Jetpack Compose. Mỗi hàm `@Composable` là một thành phần giao diện nhỏ có thể tái sử dụng.

- Hàm này được gọi bởi Compose runtime và trả về cây giao diện.
- Khi state bên trong thay đổi, hàm sẽ được gọi lại để vẽ lại UI.
- Không được gọi trực tiếp từ code thường - chỉ từ các composable khác hoặc từ `setContent {}`.

### 6.3. Composition

Là quá trình Compose tạo cây giao diện (UI tree) từ các hàm `@Composable` và theo dõi state của chúng.

- Khi app khởi động hoặc state thay đổi, Compose sẽ tạo lại cây giao diện.
- Mỗi lần composition, Compose ghi nhớ các object (via `remember`) để không mất dữ liệu.

### 6.4. Recomposition

Khi state đổi, Compose chỉ vẽ lại những phần cần cập nhật (thay vì vẽ lại toàn bộ màn hình).

- **Hiệu suất**: tránh vẽ lại các phần không cần thiết.
- **Ví dụ**: nếu chỉ counter thay đổi, chỉ component hiển thị counter được vẽ lại, không phải toàn bộ màn hình.

### 6.5. `remember` và `rememberSaveable`

`remember` giúp giữ object/state qua các lần recomposition. Nó giống như một bộ nhớ tạm cho composable.

- **`remember`**: giữ object trong composition scope hiện tại.
- **`rememberSaveable`**: giữ object và tự động restore khi device rotate hoặc process death.
- **Ví dụ**:
  ```kotlin
  val state = remember { mutableStateOf("") }
  val viewModel = remember { MyViewModel() } // tạo một lần, giữ qua recompositions
  ```

### 6.6. ViewModel

Chứa state và logic xử lý của màn hình. ViewModel tồn tại lâu hơn composable (theo lifecycle Android).

- **Trách nhiệm**: quản lý state, xử lý user events, gọi repository.
- **Thời gian sống**: từ khi màn hình hiển thị đến khi user rời màn hình.
- **Ví dụ**: `AuthViewModel` quản lý trạng thái đăng nhập và xử lý logic đăng nhập/đăng ký.

### 6.7. Repository

Là lớp trung gian giữa ViewModel và nguồn dữ liệu (API, Database, Cache).

- **Trách nhiệm**: gọi API, lưu/lấy dữ liệu từ database, cache dữ liệu.
- **Lợi ích**: tách biệt logic dữ liệu ra khỏi ViewModel, dễ test.
- **Ví dụ**: `AuthRepository` gọi `AuthApiService` để login/register, `DeckRepository` quản lý dữ liệu deck.

### 6.8. StateFlow / LiveData / mutableStateOf

Đây là các cách UI quan sát state và tự cập nhật khi dữ liệu đổi.

- **`StateFlow`** (Kotlin Coroutine): stream dữ liệu reactive, emit giá trị mới khi state thay đổi.
- **`LiveData`** (AndroidX): giống StateFlow nhưng aware lifecycle, tự unsubscribe khi Activity destroy.
- **`mutableStateOf`** (Compose): dùng trong @Composable để tạo state cục bộ của UI.
- **Ví dụ**:
  ```kotlin
  val username = MutableStateFlow("")
  username.collect { newValue -> 
      println("Username changed to $newValue")
  }
  ```

---

## 6.9. Retrofit

**Retrofit** là thư viện tạo HTTP client từ interface Java/Kotlin. Nó chuyển đổi interface khai báo (với annotation) thành các hàm gọi API thực tế.

- **Cách dùng**: 
  1. Định nghĩa interface với các endpoint.
  2. Retrofit tạo implementation động (dynamic proxy).
  3. Gọi hàm interface -> Retrofit tạo HTTP request và parse response.

- **Ví dụ trong project**:
  ```kotlin
  interface AuthApiService {
      @POST("api/auth/login")
      suspend fun login(@Body request: LoginRequest): Response<LoginResponse>
  }
  
  // Dùng:
  val retrofit = Retrofit.Builder()
      .baseUrl("http://192.168.1.7:8080/")
      .addConverterFactory(GsonConverterFactory.create())
      .build()
  val authService = retrofit.create(AuthApiService::class.java)
  val response = authService.login(loginRequest)
  ```

- **Thành phần chính**:
  - `@POST`, `@GET`, `@PUT`, `@DELETE`: định nghĩa HTTP method.
  - `@Body`: body của request (được convert thành JSON).
  - `@Path`, `@Query`: parameter trong URL.
  - `suspend`: Coroutine function - tự động chạy trên background.

### 6.10. OkHttp

**OkHttp** là HTTP client của Square. Retrofit sử dụng OkHttp dưới nước để gửi request thực tế.

- **Chức năng**: gửi HTTP request, quản lý connection, cache, retry, v.v.
- **Trong project**:
  - `cookieJar`: lưu cookie từ server (dùng `PersistentCookieJar` lưu vào SharedPreferences).
  - `HttpLoggingInterceptor`: in log request/response để debug.
  - `Interceptor`: chặn request/response để modify hoặc log.

### 6.11. Gson

**Gson** là thư viện convert JSON string ↔ Kotlin object.

- **Cách dùng**: `Gson().toJson(obj)` hoặc `Gson().fromJson(jsonString, MyClass::class.java)`.
- **Trong Retrofit**: `GsonConverterFactory` tự động convert response JSON thành object.
- **Custom deserializer**: khi JSON format phức tạp, custom deserializer để parse đúng.
  ```kotlin
  GsonBuilder()
      .registerTypeAdapter(CreatedByInfo::class.java, CreatedByInfoDeserializer())
      .create()
  ```

### 6.12. Firebase Cloud Messaging (FCM)

**FCM** là dịch vụ push notification của Google. App nhận token FCM duy nhất, server dùng token này để gửi thông báo.

- **Luồng**:
  1. App lấy FCM token từ `FirebaseMessaging.getInstance().token`.
  2. Lưu token vào repository (cache và database).
  3. Đồng bộ token lên server qua API.
  4. Server lưu token của user.
  5. Khi cần gửi thông báo, server gọi FCM API với token để gửi message.

- **Trong project** (MainActivity.kt):
  ```kotlin
  FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
      val token = task.result
      userRepository.fcmToken = token
      userRepository.syncFcmToken() // gửi token lên server
  }
  ```

---

## 6.13. Navigation & Route

**Navigation** là cơ chế chuyển qua lại giữa các màn hình trong app.

- **NavController**: quản lý back stack và điều hướng.
- **NavHost**: container chứa tất cả destination (màn hình).
- **composable()**: khai báo một destination trong NavHost.
- **Route**: string đại diện cho một màn hình (ví dụ: `"login"`, `"deck/{deckId}"`).

### Screen sealed class

Trong project, routes được định nghĩa bằng **sealed class**:

```kotlin
sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    data class DeckDetail(val deckId: String) : Screen("deck_detail/{deckId}")
}

// Dùng:
navController.navigate(Screen.Login.route)
navController.navigate(Screen.DeckDetail("123").route) // "deck_detail/123"
```

### popUpTo

`popUpTo` xóa các destination khỏi back stack:
```kotlin
navController.navigate(Screen.Home.route) {
    popUpTo(Screen.Login.route) { inclusive = true } // xóa Login khỏi back stack
}
```

### Argument

Routes có thể nhận tham số động:
```kotlin
composable("deck/{deckId}", arguments = listOf(navArgument("deckId") { type = NavType.StringType })) { 
    val deckId = it.arguments?.getString("deckId")
}
```

---

## 6.14. Dependency Injection (DI) & AppModule

**Dependency Injection** là pattern cung cấp dependency thay vì component tự tạo.

- **Ưu điểm**: dễ test, tái sử dụng, quản lý lifecycle.
- **Trong project**: dùng **object singleton** `AppModule` thay vì framework như Hilt hay Dagger.

```kotlin
object AppModule {
    // Tạo một lần và giữ lâu dài (singleton)
    private val retrofit: Retrofit by lazy { /* tạo Retrofit */ }
    val authApiService: AuthApiService by lazy { retrofit.create(...) }
    val authRepository: AuthRepository by lazy { AuthRepository(...) }
}

// Dùng:
val authService = AppModule.authApiService
```

- **`by lazy`**: chỉ tạo khi cần (lazy initialization).

---

## 6.15. Coroutine & suspend

**Coroutine** là cách Kotlin xử lý async operation mà không cần callback.

- **`suspend`**: hàm có thể bị tạm dừng mà không block thread.
- **`launch`**, **`async`**: launch scope để chạy coroutine.
- **`withContext`**: chuyển context (ví dụ: từ Main sang IO).

```kotlin
// Retrofit suspend function
suspend fun login(request: LoginRequest): Response<LoginResponse>

// Gọi từ ViewModel
viewModelScope.launch {
    val response = authRepository.login(request)
    // cập nhật UI state
}
```

---

## 6.16. Model & Data Class

**Data class** là Kotlin class đặc biệt auto-generate `equals()`, `hashCode()`, `copy()`, v.v.

```kotlin
data class LoginRequest(
    val email: String,
    val password: String
)

// Dùng:
val req1 = LoginRequest("user@gmail.com", "123")
val req2 = req1.copy(email = "newemail@gmail.com") // tạo bản copy với field khác
```

---

## 6.17. Sealed Class

**Sealed class** là class chỉ có các subclass cố định được biết trước. Dùng cho enum phức tạp.

```kotlin
sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    data class DeckDetail(val deckId: String) : Screen("deck_detail/{deckId}")
}

// Dùng when để ensure exhaustive:
when (screen) {
    is Screen.Login -> { }
    is Screen.Register -> { }
    is Screen.DeckDetail -> { }
}
```

---

## 6.18. Lifecycle & Context

**Context** là context Android để access tài nguyên (SharedPreferences, file system, v.v.).

- **applicationContext**: context của cả app, tồn tại lâu nhất.
- **Activity context**: context của Activity, bị destroy khi Activity destroy (gây memory leak).
- **Trong project**: dùng `applicationContext` ở DI layer để tránh memory leak.

---

## 6.19. SharedPreferences

**SharedPreferences** là cách lưu trữ key-value đơn giản trên device.

- **Dùng cho**: lưu token, user preferences, cấu hình, v.v.
- **Ví dụ**:
  ```kotlin
  val pref = context.getSharedPreferences("app_pref", Context.MODE_PRIVATE)
  pref.edit().putString("fcm_token", token).apply()
  val token = pref.getString("fcm_token", "")
  ```

---

## 6.20. AndroidManifest.xml & Permissions

**AndroidManifest.xml** khai báo meta-data của app: activity, service, permission, v.v.

- **Permission**: app phải khai báo quyền cần dùng trong manifest.
- **Runtime permission**: trên Android 6.0+, user phải cấp quyền runtime (ví dụ: notification, camera).
- **Trong project**: khai báo `POST_NOTIFICATIONS` permission, request runtime ở MainActivity.

---

## 7. Các Pattern & Best Practice trong project

### 7.1. MVVM (Model-View-ViewModel)

**MVVM** là kiến trúc phần mềm tách biệt UI, logic, và dữ liệu thành 3 tầng.

- **View** (UI): Composable, hiển thị dữ liệu từ ViewModel, gửi user events.
- **ViewModel**: chứa logic màn hình, quản lý state, gọi Repository.
- **Model**: định nghĩa dữ liệu (data class, API models).

**Luồng dữ liệu**:
```
UI (Composable) 
  ↓ (observe state)
ViewModel (UI state, events) 
  ↓ (call methods)
Repository (fetch data, caching) 
  ↓ (API calls, DB access)
API / Local Database
```

**Ưu điểm**:
- UI không phụ thuộc logic phức tạp.
- Dễ test ViewModel riêng biệt.
- Dễ tái sử dụng ViewModel với nhiều UI khác nhau.

### 7.2. Repository Pattern

**Repository** là lớp trung gian quản lý nguồn dữ liệu (API, Database, Cache).

- **Lợi ích**: 
  - ViewModel không cần biết dữ liệu từ API hay Database.
  - Dễ swap implementation (ví dụ: dùng mock API cho testing).
  - Tập trung logic caching, refresh, v.v. ở một chỗ.

- **Ví dụ**: `AuthRepository` có method `login()`, nhưng ViewModel không cần biết `login()` gọi `AuthApiService` hay load từ cache.

### 7.3. Singleton Pattern

**Singleton** là object chỉ tạo một lần và tái sử dụng khắp app.

- **Trong project**: `AppModule` là singleton chứa toàn bộ services, repositories.
- **Dùng `by lazy`**: chỉ tạo khi lần đầu access (lazy initialization).

### 7.4. Builder Pattern

**Builder** là pattern xây dựng object phức tạp từng bước.

- **Ví dụ**:
  ```kotlin
  val retrofit = Retrofit.Builder()
      .baseUrl(BASE_URL)
      .client(okHttpClient)
      .addConverterFactory(GsonConverterFactory.create(gson))
      .build()
  ```

### 7.5. Sealed Class & Sealed Hierarchy

**Sealed class** giới hạn subclass, dùng cho enum phức tạp hoặc state.

- **Ví dụ**: `sealed class Screen` định nghĩa tất cả route của app.
- **Ưu điểm**: `when` expression buộc handle tất cả case.

### 7.6. Feature-First Architecture

Project được tổ chức theo **features** (chức năng) thay vì layers (tầng).

```
feature/
  ├── auth/        (toàn bộ auth screens, viewmodels)
  ├── deck/        (toàn bộ deck management)
  ├── flashcard/   (toàn bộ flashcard learning)
  └── ...
data/
  ├── api/         (API services)
  ├── repository/  (repositories)
  └── ...
```

**Ưu điểm**:
- Dễ tìm code liên quan đến một feature.
- Dễ build feature như module riêng (modularization).
- Team khác nhau có thể làm feature khác nhau song song.

### 7.7. Declarative UI (Compose)

Thay vì imperative UI (lệnh từng bước), Compose dùng **declarative** (mô tả state -> UI).

```kotlin
// Imperative (cũ):
button.setOnClickListener { count++ }
text.setText("Count: $count")

// Declarative (Compose):
val count = remember { mutableStateOf(0) }
Button(onClick = { count.value++ }) { 
    Text("Count: ${count.value}") 
}
```

**Ưu điểm**: UI tự cập nhật khi state đổi, không cần manual update.

### 7.8. Reactive Programming

Repository và ViewModel sử dụng **reactive streams** (`StateFlow`, observers) để dữ liệu tự động lan truyền từ dữ liệu → UI.

- **UI subscribe** state từ ViewModel.
- **Khi state đổi**, UI tự recompose (Compose handles this).
- **Lợi ích**: UI luôn sync với dữ liệu, không cần manual refresh.

---

## 8. Luồng backend auth tóm tắt

### `loginUser(identifier, password)`

- Tìm user theo email hoặc username.
- Kiểm tra user tồn tại.
- Kiểm tra đã verify chưa.
- So sánh mật khẩu bằng bcrypt.
- Cập nhật `lastLogin`.
- Tạo `accessToken` và `refreshToken`.
- Trả về thông tin redirect theo role.

### `registerUser(userData)`

- Kiểm tra trùng email/username.
- Hash password.
- Tạo OTP và hash OTP.
- Lưu user mới với trạng thái chưa verify.
- Gửi OTP qua email.

### `forgotPassword(email)`

- Tìm user theo email.
- Tạo OTP mới.
- Gửi OTP reset qua email.

### `verifyOtp(email, otpCode)`

- Kiểm tra OTP hợp lệ và còn hạn.
- Nếu là luồng đăng ký -> đánh dấu `isVerified = true`.
- Nếu là luồng quên mật khẩu -> tạo `resetToken`.

### `resetPassword(resetToken, newPassword)`

- Xác thực JWT reset token.
- Kiểm tra `purpose`.
- Hash mật khẩu mới.
- Lưu mật khẩu mới và xóa OTP cũ.

---

## 9. Access token, refresh token và reset token

- **Access token**: token ngắn hạn, dùng để gọi API.
- **Refresh token**: token dài hạn, dùng để xin access token mới.
- **Reset token**: token ngắn hạn cho riêng luồng quên mật khẩu.

### Reset token lưu ở đâu?

- Thường được giữ tạm trong `ViewModel` hoặc memory của app.
- Chỉ nên dùng một lần và xóa sau khi reset xong.

### Mục đích

- Bảo đảm chỉ người đã xác thực OTP mới đặt lại mật khẩu được.

---

## 10. Kết luận

Dự án được tổ chức rõ ràng theo các lớp:

- UI bằng Compose (Declarative UI)
- Logic màn hình trong ViewModel (MVVM pattern)
- Truy xuất dữ liệu qua Repository (Repository pattern)
- Điều hướng tập trung tại `AppNavGraph`
- Dependency Injection via `AppModule` (Singleton pattern)

Các thành phần chính:
- **Retrofit + OkHttp**: HTTP client, API calls, caching, interceptors
- **Gson**: JSON serialization/deserialization
- **Jetpack Compose**: Declarative UI framework
- **Navigation Compose**: App routing và screen management
- **Firebase Cloud Messaging**: Push notifications
- **Kotlin Coroutines**: Async/await, background tasks
- **StateFlow**: Reactive data streams

Cách tổ chức này giúp:

- Dễ mở rộng chức năng.
- Dễ bảo trì mã nguồn.
- Tách biệt UI và logic (separation of concerns).
- Dễ báo cáo luồng xử lý từng tính năng.
- Dễ testing từng component độc lập.

---
