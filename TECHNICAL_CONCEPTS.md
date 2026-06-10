# Giải Thích Chi Tiết Các Khái Niệm Kỹ Thuật trong Mobile English Flashcard

Tài liệu này cung cấp giải thích sâu về các khái niệm, thành phần, công nghệ, và pattern được sử dụng trong dự án Mobile English Flashcard.

---

## Mục Lục

1. [Jetpack Compose & UI Framework](#1-jetpack-compose--ui-framework)
2. [Architecture Patterns (MVVM, Repository)](#2-architecture-patterns-mvvm-repository)
3. [Networking (Retrofit, OkHttp, Gson)](#3-networking-retrofit-okhttp-gson)
4. [Firebase & Push Notifications](#4-firebase--push-notifications)
5. [Navigation & Routing](#5-navigation--routing)
6. [Dependency Injection & AppModule](#6-dependency-injection--appmodule)
7. [Kotlin Coroutines & Async Programming](#7-kotlin-coroutines--async-programming)
8. [Local Storage & State Management](#8-local-storage--state-management)
9. [Build System & Dependencies](#9-build-system--dependencies)
10. [Project Structure & Organization](#10-project-structure--organization)

---

## 1. Jetpack Compose & UI Framework

### 1.1 Jetpack Compose là gì?

**Jetpack Compose** là framework khai báo (declarative) để xây dựng giao diện người dùng Android.

#### Đặc điểm chính:

- **Declarative**: mô tả UI là gì (dùng state) thay vì lệnh làm gì từng bước
- **Composable**: UI được xây từ các hàm `@Composable` nhỏ, tái sử dụng được
- **Reactive**: UI tự động cập nhật khi state thay đổi
- **Type-safe**: kiểm tra kiểu dữ liệu tại compile time

#### So sánh Imperative vs Declarative:

```kotlin
// === Cách cũ (Imperative - XML + Java) ===
// activity_login.xml
<Button
    android:id="@+id/loginButton"
    android:text="Login" />

// LoginActivity.kt
val button = findViewById<Button>(R.id.loginButton)
button.setOnClickListener {
    // gọi API, hiển thị lỗi, chuyển màn hình
    loginViewModel.login(username, password)
}

// === Cách mới (Declarative - Compose) ===
@Composable
fun LoginScreen(viewModel: AuthViewModel) {
    val loginState by viewModel.loginState.collectAsState()
    
    Button(onClick = { viewModel.login(username, password) }) {
        Text("Login")
    }
    
    // UI tự cập nhật khi loginState thay đổi
    when (loginState) {
        is LoginState.Success -> { /* navigate */ }
        is LoginState.Error -> { /* show error */ }
        is LoginState.Loading -> { /* show loading */ }
    }
}
```

#### Ưu điểm:

- Code UI ngắn gọn hơn
- Dễ preview và test UI
- Tự động handle recomposition hiệu quả
- Tích hợp tốt với ViewModel và reactive streams

### 1.2 Composable

`@Composable` là annotation đánh dấu hàm là một UI component.

```kotlin
// Hàm này nhận input (data) và trả về UI
@Composable
fun LoginScreen(viewModel: AuthViewModel, onNavigateToRegister: () -> Unit) {
    // Các composable khác
    Column(modifier = Modifier.fillMaxSize()) {
        TextField(...)
        Button(onClick = onNavigateToRegister) { Text("Register") }
    }
}

// Hàm không được gọi trực tiếp từ code thường:
// LoginScreen(viewModel, {}) ❌ SAI

// Chỉ được gọi từ các composable khác hoặc từ setContent:
setContent {
    LoginScreen(viewModel, {}) ✓ ĐÚNG
}
```

**Quy tắc quan trọng:**
- Hàm `@Composable` có thể được gọi lại bất kỳ lúc nào (recomposition)
- Không được có side effects trực tiếp (dùng `LaunchedEffect`, `remember` để xử lý)
- Phải là pure function (cùng input -> cùng output)

### 1.3 Composition & Recomposition

#### Composition

Là quá trình Compose tạo cây giao diện (UI tree) từ các hàm `@Composable`.

```
App starts
    ↓
MainActivity.onCreate() -> setContent {
    ↓
Compose runtime reads LoginScreen() composable
    ↓
Creates UI tree: Column -> TextField, Button, ...
    ↓
Renders on screen
```

#### Recomposition

Khi state thay đổi, Compose sẽ gọi lại các composable để vẽ lại (nhưng chỉ những phần cần cập nhật).

```kotlin
// Ví dụ:
@Composable
fun Counter() {
    var count by remember { mutableStateOf(0) }
    
    Column {
        Text("Count: $count") // ← Cần recompose khi count thay đổi
        Button(onClick = { count++ }) { 
            Text("Increment") // ← Không cần recompose (onClick không phụ thuộc state)
        }
        OtherComponent() // ← Có thể không recompose (không phụ thuộc count)
    }
}

// Khi người dùng click Button:
// 1. count được cập nhật từ 0 -> 1
// 2. Compose phát hiện count thay đổi
// 3. Recompose chỉ Text("Count: $count") để hiển thị "Count: 1"
// 4. Các phần khác không bị recompose
```

**Lợi ích:**
- Hiệu suất tốt: không vẽ lại toàn bộ UI
- Code UI đơn giản: chỉ cần mô tả state -> UI

### 1.4 Remember & State Persistence

`remember` giúp giữ object/state qua các lần recomposition.

```kotlin
@Composable
fun CounterExample() {
    // ❌ SAI: textState được tạo lại mỗi lần recomposition
    var textState = mutableStateOf("")
    Button(onClick = { textState.value = "Clicked" }) {
        Text(textState.value)
    }
    // Khi recomposition, text sẽ luôn trống

    // ✓ ĐÚNG: textState được giữ qua recompositions
    var textState by remember { mutableStateOf("") }
    Button(onClick = { textState.value = "Clicked" }) {
        Text(textState.value)
    }
    // Khi recomposition, text vẫn giữ giá trị cũ
}
```

#### Difference between `remember` và `rememberSaveable`:

```kotlin
@Composable
fun LoginScreen(viewModel: AuthViewModel) {
    // ❌ SAI: mất dữ liệu khi device rotate hoặc back to this screen
    var username by remember { mutableStateOf("") }
    
    // ✓ ĐÚNG: tự động restore sau device rotate
    var username by rememberSaveable { mutableStateOf("") }
}
```

### 1.5 Modifier & Layout

Modifier dùng để thay đổi style, size, event handling của UI component.

```kotlin
@Composable
fun StyledButton() {
    Button(
        modifier = Modifier
            .fillMaxWidth()           // Chiếm toàn bộ chiều rộng
            .height(48.dp)            // Chiều cao 48 dp
            .padding(16.dp)           // Padding 16 dp
            .clip(RoundedCornerShape(8.dp))  // Bo góc
            .background(Color.Blue),
        onClick = { /* handle click */ }
    ) {
        Text("Click Me")
    }
}
```

---

## 2. Architecture Patterns (MVVM, Repository)

### 2.1 MVVM Architecture

**MVVM** (Model-View-ViewModel) tách biệt code thành 3 phần:

```
┌─────────────────────────────────────┐
│         VIEW (UI)                   │
│   LoginScreen, RegisterScreen, ...  │
│   ├─ State từ ViewModel             │
│   ├─ Call ViewModel methods         │
│   └─ No business logic              │
└──────────┬──────────────────────────┘
           │ observe state
           ↓
┌─────────────────────────────────────┐
│      VIEWMODEL                      │
│   AuthViewModel, DeckViewModel, ... │
│   ├─ Hold UI state                  │
│   ├─ Call Repository methods        │
│   ├─ Transform data for UI          │
│   └─ Handle user events             │
└──────────┬──────────────────────────┘
           │ call methods
           ↓
┌─────────────────────────────────────┐
│      REPOSITORY                     │
│   AuthRepository, DeckRepository... │
│   ├─ Fetch from API / DB            │
│   ├─ Cache data                     │
│   ├─ No UI logic                    │
│   └─ Can be swapped for testing     │
└──────────┬──────────────────────────┘
           │ call services
           ↓
┌─────────────────────────────────────┐
│      DATA SOURCES                   │
│   API Services, Local Database      │
│   ├─ Raw data                       │
│   └─ No app logic                   │
└─────────────────────────────────────┘
```

#### Ưu điểm MVVM:

1. **Separation of Concerns**: UI, logic, data tách biệt
2. **Testability**: có thể test ViewModel mà không cần UI
3. **Reusability**: cùng ViewModel có thể dùng với nhiều UI khác nhau
4. **Maintainability**: dễ bảo trì, mỗi phần có trách nhiệm riêng

### 2.2 Repository Pattern

Repository là lớp trung gian quản lý nguồn dữ liệu.

```kotlin
// ❌ SAI: ViewModel biết chi tiết nguồn dữ liệu
class AuthViewModel {
    fun login(email: String, password: String) {
        // Call API trực tiếp
        val response = AuthApiService.login(email, password)
        // Handle response
    }
}

// ✓ ĐÚNG: ViewModel gọi Repository, Repository quản lý nguồn dữ liệu
interface AuthRepository {
    suspend fun login(email: String, password: String): Result<User>
    suspend fun register(data: RegisterData): Result<User>
}

class AuthRepositoryImpl(
    private val authApiService: AuthApiService,
    private val localDatabase: UserDatabase
) : AuthRepository {
    override suspend fun login(email: String, password: String): Result<User> {
        return try {
            // Có thể fetch từ API
            val response = authApiService.login(email, password)
            if (response.isSuccessful) {
                val user = response.body()
                // Cache vào local DB
                localDatabase.saveUser(user)
                Result.Success(user)
            } else {
                // Hoặc fallback từ cache
                val cachedUser = localDatabase.getUser(email)
                if (cachedUser != null) Result.Success(cachedUser)
                else Result.Error(response.message())
            }
        } catch (e: Exception) {
            Result.Error(e.message)
        }
    }
}

class AuthViewModel(private val authRepository: AuthRepository) {
    fun login(email: String, password: String) {
        viewModelScope.launch {
            // ViewModel chỉ biết Repository interface, không biết implementation
            val result = authRepository.login(email, password)
            when (result) {
                is Result.Success -> { /* update UI */ }
                is Result.Error -> { /* show error */ }
            }
        }
    }
}
```

#### Ưu điểm Repository:

1. **Flexibility**: có thể swap API ↔ Cache ↔ Mock mà không cần thay code khác
2. **Testing**: dễ test bằng mock repository
3. **Caching**: tập trung logic cache ở một chỗ
4. **Resilience**: có thể fallback từ cache khi API fail

### 2.3 Singleton Pattern (AppModule)

AppModule là singleton chứa toàn bộ instances của services, repositories, v.v.

```kotlin
// Di container dùng singleton pattern
object AppModule {
    // Context của app
    private lateinit var appContext: Context
    
    // Chỉ tạo một lần (lazy initialization)
    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .cookieJar(cookieJar)
            .addInterceptor(loggingInterceptor)
            .build()
    }
    
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    
    // API services
    val authApiService: AuthApiService by lazy {
        retrofit.create(AuthApiService::class.java)
    }
    
    // Repositories
    val authRepository: AuthRepository by lazy {
        AuthRepositoryImpl(authApiService)
    }
    
    fun initialize(context: Context) {
        appContext = context.applicationContext
    }
}

// Sử dụng:
val authService = AppModule.authApiService  // Lần 1: tạo instance
val authService2 = AppModule.authApiService // Lần 2: trả về instance cũ
```

---

## 3. Networking (Retrofit, OkHttp, Gson)

### 3.1 Retrofit: HTTP Client Builder

**Retrofit** là framework tạo HTTP client từ interface Kotlin.

```kotlin
// === Bước 1: Định nghĩa API interface ===
interface AuthApiService {
    // Decorator: định nghĩa HTTP method, URL endpoint
    @POST("api/auth/login")
    // @Headers: set headers cho request này
    @Headers("Content-Type: application/json")
    // suspend: Kotlin coroutine function (chạy trên background)
    suspend fun login(
        // @Body: serialize object thành JSON body
        @Body request: LoginRequest
    ): Response<LoginResponse>
    
    @GET("api/users/{userId}")
    // @Path: parameter trong URL path
    suspend fun getUser(
        @Path("userId") userId: String
    ): Response<UserResponse>
    
    @GET("api/decks")
    // @Query: parameter trong query string (?key=value)
    suspend fun searchDecks(
        @Query("search") query: String,
        @Query("limit") limit: Int = 20
    ): Response<List<DeckResponse>>
}

// === Bước 2: Tạo Retrofit instance ===
val retrofit = Retrofit.Builder()
    .baseUrl("http://192.168.1.7:8080/")  // Base URL (prefix cho toàn bộ requests)
    .client(okHttpClient)                 // OkHttp client
    .addConverterFactory(GsonConverterFactory.create())  // JSON converter
    .build()

// === Bước 3: Tạo API service ===
val authService = retrofit.create(AuthApiService::class.java)

// === Bước 4: Gọi API (từ coroutine) ===
viewModelScope.launch {
    try {
        val response = authService.login(LoginRequest("user@email.com", "123456"))
        if (response.isSuccessful) {
            val loginResponse = response.body()
            println("Login thành công: ${loginResponse?.accessToken}")
        } else {
            println("Login thất bại: ${response.code()}")
        }
    } catch (e: Exception) {
        println("Error: ${e.message}")
    }
}
```

### 3.2 OkHttp: HTTP Client

**OkHttp** là HTTP client thực tế (Retrofit sử dụng OkHttp dưới nước).

```kotlin
val okHttpClient = OkHttpClient.Builder()
    // === Cookie Management ===
    // PersistentCookieJar giữ cookie giữa các requests và giữa các lần mở app
    .cookieJar(PersistentCookieJar(
        SetCookieCache(),
        SharedPrefsCookiePersistor(context)
    ))
    
    // === Logging ===
    // HttpLoggingInterceptor in toàn bộ request/response để debug
    .addInterceptor(HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY  // In body (toàn bộ request/response)
    })
    
    // === Custom Interceptor ===
    .addInterceptor { chain ->
        val originalRequest = chain.request()
        
        // Thêm token vào header
        val requestWithToken = originalRequest.newBuilder()
            .header("Authorization", "Bearer $accessToken")
            .build()
        
        chain.proceed(requestWithToken)
    }
    
    .build()
```

#### Interceptor Pattern:

```
Request -> Interceptor 1 -> Interceptor 2 -> ... -> Server
                                                        ↓
Response <- Interceptor 1 <- Interceptor 2 <- ... <- Server
```

### 3.3 Gson: JSON Serialization

**Gson** convert JSON string ↔ Kotlin object.

```kotlin
// === Cách sử dụng cơ bản ===
data class LoginRequest(
    val email: String,
    val password: String
)

// Object -> JSON string
val request = LoginRequest("user@email.com", "123456")
val gson = Gson()
val jsonString = gson.toJson(request)
// Output: {"email":"user@email.com","password":"123456"}

// JSON string -> Object
val jsonString = """{"email":"user@email.com","password":"123456"}"""
val request = gson.fromJson(jsonString, LoginRequest::class.java)

// === Custom deserializer (khi JSON format khác) ===
class CreatedByInfoDeserializer : JsonDeserializer<CreatedByInfo> {
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): CreatedByInfo {
        val obj = json.asJsonObject
        
        // Xử lý JSON tùy ý
        val id = obj.get("id")?.asString ?: ""
        val username = obj.get("name")?.asString ?: ""
        
        return CreatedByInfo(id, username)
    }
}

// Dùng custom deserializer:
val gson = GsonBuilder()
    .registerTypeAdapter(CreatedByInfo::class.java, CreatedByInfoDeserializer())
    .create()
```

---

## 4. Firebase & Push Notifications

### 4.1 Firebase Cloud Messaging (FCM)

**FCM** là dịch vụ push notification của Google.

```
┌──────────────┐
│ User Device  │
│   App        │
└────┬─────────┘
     │ 1. Request FCM token
     ↓
┌──────────────────────────────────────┐
│ Firebase Cloud Messaging             │
│ - Tạo token duy nhất cho device này  │
│ - token = "abc123def456xyz789..."   │
└────┬─────────────────────────────────┘
     │ 2. Gửi token
     ↓
┌──────────────────────────────────────┐
│ Backend Server                       │
│ - Lưu token vào DB: user.fcmToken   │
└──────────────────────────────────────┘

Sau đó, khi server muốn gửi thông báo:

┌──────────────────────────────────────┐
│ Backend Server                       │
│ - Lấy user.fcmToken                 │
│ - Gọi FCM API: send(token, message) │
└────┬─────────────────────────────────┘
     │
     ↓
┌──────────────────────────────────────┐
│ Firebase Cloud Messaging             │
│ - Nhận request send thông báo       │
└────┬─────────────────────────────────┘
     │ Gửi thông báo
     ↓
┌──────────────┐
│ User Device  │
│   Nhận thông báo  │
└──────────────┘
```

#### Cách implement FCM trong app:

```kotlin
// === MainActivity.kt ===
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    
    // 1. Xin quyền thông báo
    askNotificationPermission()
    
    // 2. Lấy FCM token
    FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
        if (!task.isSuccessful) {
            Log.e("FCM", "Fetching token failed", task.exception)
            return@addOnCompleteListener
        }
        
        // 3. Lưu token
        val token = task.result
        Log.d("FCM", "Token: $token")
        
        // 4. Đồng bộ token lên server
        viewModelScope.launch {
            userRepository.syncFcmToken(token)
        }
    }
}

// === Request permission ===
private fun askNotificationPermission() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        if (ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.POST_NOTIFICATIONS
        ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}
```

#### Push Notification Handler:

```kotlin
// Lớp tự động receive notification từ FCM
class MyFirebaseMessagingService : FirebaseMessagingService() {
    
    // Gọi khi nhận message từ FCM
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d("FCM", "Message from: ${remoteMessage.from}")
        
        // Kiểm tra message có data không
        if (remoteMessage.data.isNotEmpty()) {
            val title = remoteMessage.data["title"]
            val body = remoteMessage.data["body"]
            
            // Hiển thị notification
            showNotification(title, body)
        }
    }
    
    // Gọi khi token bị refresh
    override fun onNewToken(token: String) {
        Log.d("FCM", "New token: $token")
        
        // Gửi token mới lên server
        sendTokenToServer(token)
    }
    
    private fun showNotification(title: String?, body: String?) {
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
        
        NotificationManagerCompat.from(this).notify(1, notification)
    }
}
```

---

## 5. Navigation & Routing

### 5.1 Navigation Compose

**Navigation Compose** quản lý routing giữa các màn hình.

```kotlin
// === 1. Định nghĩa Routes ===
sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Home : Screen("home")
    
    // Route với tham số
    data class DeckDetail(val deckId: String) : Screen("deck_detail/{deckId}") {
        fun createRoute(deckId: String) = "deck_detail/$deckId"
    }
}

// === 2. Tạo NavHost ===
@Composable
fun AppNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Login.route  // Màn hình đầu tiên
    ) {
        // === Login screen ===
        composable(Screen.Login.route) {
            LoginScreen(
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                },
                onLoginSuccess = {
                    // popUpTo: xóa tất cả screens trước Login khỏi back stack
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) {
                            inclusive = true  // Xóa cả Login
                        }
                    }
                }
            )
        }
        
        // === Register screen ===
        composable(Screen.Register.route) {
            RegisterScreen(
                onBack = {
                    navController.popBackStack()  // Quay lại (back)
                }
            )
        }
        
        // === Home screen ===
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToDeckDetail = { deckId ->
                    navController.navigate(Screen.DeckDetail(deckId).createRoute(deckId))
                }
            )
        }
        
        // === Deck Detail screen (có tham số) ===
        composable(
            route = Screen.DeckDetail("").route,
            arguments = listOf(
                navArgument("deckId") {
                    type = NavType.StringType
                    nullable = false
                }
            )
        ) { backStackEntry ->
            val deckId = backStackEntry.arguments?.getString("deckId") ?: ""
            DeckDetailScreen(deckId)
        }
    }
}

// === 3. Setup navigation ===
setContent {
    val navController = rememberNavController()
    AppNavGraph(navController)
}
```

### 5.2 Back Stack Management

```
┌─────────┐
│ LOGIN   │ ← startDestination (khởi đầu)
├─────────┤
│ REGISTER│ ← navigate(Screen.Register)
├─────────┤
│ HOME    │ ← navigate(Screen.Home, popUpTo = Login)
├─────────┤
│ DETAIL  │ ← navigate(Screen.Detail)
└─────────┘

Back button:
DETAIL -> HOME -> REGISTER -> LOGIN -> exit app

Với popUpTo(Login, inclusive = true):
navigat(Home) { popUpTo(Login) { inclusive = true } }

Kết quả:
┌─────────┐
│ HOME    │ ← Back button -> EXIT APP
└─────────┘
```

---

## 6. Dependency Injection & AppModule

### 6.1 Dependency Injection Pattern

**DI** là cách cung cấp dependency thay vì object tự tạo.

```kotlin
// ❌ SAI: AuthViewModel tự tạo repository (tight coupling)
class AuthViewModel {
    private val authRepository = AuthRepositoryImpl(...)
    
    fun login() {
        authRepository.login(...)
    }
}

// ✓ ĐÚNG: Repository được inject vào (loose coupling)
class AuthViewModel(private val authRepository: AuthRepository) {
    fun login() {
        authRepository.login(...)
    }
}

// === Tạo ViewModel với dependency ===
val authRepository = AuthRepositoryImpl(authApiService)
val authViewModel = AuthViewModel(authRepository)
```

#### Ưu điểm:

1. **Testability**: có thể pass mock repository vào constructor
2. **Flexibility**: dễ swap implementation
3. **Separation of Concerns**: object không cần biết cách tạo dependency

### 6.2 AppModule: Service Locator Pattern

```kotlin
object AppModule {
    // === Step 1: initialize khi app start ===
    fun initialize(context: Context) {
        AppContext = context.applicationContext
    }
    
    // === Step 2: Lazy-initialized singletons ===
    
    // HTTP Client
    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .cookieJar(PersistentCookieJar(...))
            .addInterceptor(HttpLoggingInterceptor())
            .build()
    }
    
    // Retrofit
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    
    // === Step 3: API Services ===
    val authApiService: AuthApiService by lazy {
        retrofit.create(AuthApiService::class.java)
    }
    
    val deckApiService: DeckApiService by lazy {
        retrofit.create(DeckApiService::class.java)
    }
    
    // === Step 4: Repositories ===
    val authRepository: AuthRepository by lazy {
        AuthRepositoryImpl(authApiService)
    }
    
    val deckRepository: DeckRepository by lazy {
        DeckRepositoryImpl(deckApiService, analyticsRepository)
    }
    
    val analyticsRepository: AnalyticsRepository by lazy {
        AnalyticsRepositoryImpl(userApiService)
    }
}

// === Usage ===
val authService = AppModule.authApiService
val deckRepo = AppModule.deckRepository
```

#### `by lazy` giải thích:

```kotlin
// === Cách cũ (eager initialization) ===
object AppModule {
    val retrofit: Retrofit = Retrofit.Builder().build()  // Tạo ngay khi app start
}

// === Cách mới (lazy initialization) ===
object AppModule {
    val retrofit: Retrofit by lazy {  // Chỉ tạo khi lần đầu access
        Retrofit.Builder().build()
    }
}

// === So sánh ===
// Eager:
// App start -> tạo toàn bộ -> có thể lâu

// Lazy:
// App start -> nhanh (không tạo gì)
// Lần 1 access AppModule.retrofit -> tạo retrofit -> trả về
// Lần 2 access AppModule.retrofit -> không tạo, trả về instance cũ
```

---

## 7. Kotlin Coroutines & Async Programming

### 7.1 suspend function

`suspend` là keyword đánh dấu hàm có thể bị tạm dừng mà không block thread.

```kotlin
// ❌ SAI: blocking thread (UI hang)
fun login(email: String, password: String): LoginResponse {
    // Thread bị block (chờ response từ server)
    return authApiService.login(email, password)  // Network call
}

// ✓ ĐÚNG: suspend function (không block thread)
suspend fun login(email: String, password: String): LoginResponse {
    // Coroutine bị suspend (chờ response)
    // Thread được giải phóng để làm việc khác
    return authApiService.login(email, password)  // Network call
}

// === Retrofit tự động tạo suspend function ===
interface AuthApiService {
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>
}
```

### 7.2 Coroutine Scopes

```kotlin
// === viewModelScope ===
// Scope này tự động hủy coroutine khi ViewModel bị clear
class AuthViewModel(private val authRepository: AuthRepository) : ViewModel() {
    fun login(email: String, password: String) {
        // viewModelScope.launch chạy coroutine trong scope của ViewModel
        viewModelScope.launch {
            val response = authRepository.login(email, password)
            // Update UI state
            _loginState.value = response
        }
        
        // Khi ViewModel destroy, coroutine tự động cancel
    }
}

// === lifecycleScope ===
// Scope này tự động hủy coroutine khi Lifecycle (Activity/Fragment) destroy
class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        lifecycleScope.launch {
            // Coroutine chạy trong scope của Activity lifecycle
            someAsyncWork()
        }
        
        // Khi Activity destroy, coroutine tự động cancel
    }
}

// === Global scope (không tự động cancel) ===
GlobalScope.launch {  // ❌ Tránh dùng
    // Coroutine tiếp tục chạy ngay cả khi Activity destroy
    // Có thể gây memory leak
}
```

### 7.3 withContext & Dispatcher

```kotlin
// === Chuyển giữa các thread ===
class UserRepository(private val userApiService: UserApiService) {
    suspend fun getUser(userId: String): User {
        // Mặc định chạy trên Dispatchers.Main.immediate (UI thread)
        
        // Chuyển sang IO thread để làm network call
        return withContext(Dispatchers.IO) {
            val response = userApiService.getUser(userId)
            response.body()!!  // Đây chạy trên IO thread
        }
        // Tự động switch về Main thread sau khi xong
    }
}

// === Dispatcher types ===
- Dispatchers.Main: Android UI thread
- Dispatchers.IO: thread pool cho I/O operations (network, file, DB)
- Dispatchers.Default: thread pool cho CPU-intensive work
- Dispatchers.Unconfined: không switch thread (ít dùng)
```

### 7.4 Flow & StateFlow

```kotlin
// === StateFlow: observable state ===
class AuthViewModel(private val authRepository: AuthRepository) : ViewModel() {
    // StateFlow luôn có giá trị hiện tại
    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState.asStateFlow()  // Read-only
    
    fun login(email: String, password: String) {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading
            
            try {
                val response = authRepository.login(email, password)
                _loginState.value = LoginState.Success(response)
            } catch (e: Exception) {
                _loginState.value = LoginState.Error(e.message ?: "")
            }
        }
    }
}

// === UI quan sát state thay đổi ===
@Composable
fun LoginScreen(viewModel: AuthViewModel) {
    // collectAsState: collect StateFlow trong Compose
    val loginState by viewModel.loginState.collectAsState()
    
    when (loginState) {
        is LoginState.Idle -> { }
        is LoginState.Loading -> { LoadingSpinner() }
        is LoginState.Success -> { navigate() }
        is LoginState.Error -> { showError(message) }
    }
}
```

---

## 8. Local Storage & State Management

### 8.1 SharedPreferences

```kotlin
// === Lưu key-value đơn giản ===
class UserRepository(private val context: Context) {
    private val pref = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
    
    // Lưu token
    fun saveAccessToken(token: String) {
        pref.edit().putString("access_token", token).apply()
    }
    
    // Lấy token
    fun getAccessToken(): String? {
        return pref.getString("access_token", null)
    }
    
    // Xóa token
    fun clearAccessToken() {
        pref.edit().remove("access_token").apply()
    }
}
```

### 8.2 StateFlow (In-Memory State)

```kotlin
// === StateFlow giữ state trong memory ===
class SrsRepository(context: Context) {
    // Để lưu trữ dữ liệu SRS (spaced repetition) trong app lifetime
    private val _cachedCards = MutableStateFlow<List<Card>>(emptyList())
    val cachedCards: StateFlow<List<Card>> = _cachedCards.asStateFlow()
    
    fun setCachedCards(cards: List<Card>) {
        _cachedCards.value = cards
    }
    
    fun clearCache() {
        _cachedCards.value = emptyList()
    }
}
```

---

## 9. Build System & Dependencies

### 9.1 Gradle Build Configuration

```kotlin
// build.gradle.kts
android {
    compileSdk = 36  // API level để compile
    
    defaultConfig {
        applicationId = "com.example.englishflashcard"
        minSdk = 24      // Minimum API level (Android 7.0+)
        targetSdk = 36   // Target API level (cho latest features)
        versionCode = 1
        versionName = "1.0"
    }
    
    buildFeatures {
        compose = true   // Enable Jetpack Compose
    }
}

dependencies {
    // === Jetpack Compose ===
    implementation(platform("androidx.compose:compose-bom:2026.05.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.navigation:navigation-compose:2.9.8")
    
    // === Networking ===
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    
    // === Firebase ===
    implementation("com.google.firebase:firebase-messaging-ktx:24.1.0")
    
    // === Testing ===
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.7.0")
}
```

---

## 10. Project Structure & Organization

### 10.1 Feature-First Architecture

```
app/src/main/java/com/example/englishflashcard/

feature/                     ← Organized by feature (not by layer)
├── auth/
│   ├── AuthViewModel.kt
│   ├── LoginScreen.kt
│   ├── RegisterScreen.kt
│   └── ...
├── deck/
│   ├── DeckViewModel.kt
│   ├── DecksScreen.kt
│   ├── DeckDetailScreen.kt
│   └── ...
├── flashcard/
│   ├── FlashcardViewModel.kt
│   ├── FlashcardScreen.kt
│   └── ...
└── ...

data/                        ← Shared across features
├── api/
│   ├── AuthApiService.kt
│   ├── DeckApiService.kt
│   └── ...
├── repository/
│   ├── AuthRepository.kt
│   ├── DeckRepository.kt
│   └── ...
└── ...

di/
├── AppModule.kt             ← Dependency Injection container

navigation/
├── AppNavGraph.kt           ← Navigation routing
├── Screen.kt                ← Screen definitions
└── ...

MainActivity.kt             ← Entry point
```

#### Ưu điểm Feature-First:

1. **Cohesion**: tất cả code liên quan đến một feature ở một chỗ
2. **Scalability**: dễ thêm feature mới hoặc remove feature
3. **Team**: team khác nhau có thể làm feature khác nhau song song
4. **Modularization**: dễ build feature như module riêng

---

## Kết Luận

Dự án Mobile English Flashcard sử dụng:

- **Jetpack Compose**: modern, declarative UI framework
- **MVVM + Repository**: clean architecture, dễ test
- **Retrofit + OkHttp + Gson**: professional networking
- **Firebase FCM**: push notifications
- **Navigation Compose**: screen routing
- **Kotlin Coroutines**: async programming
- **Feature-First**: organized code structure

Cách tổ chức này tạo ra ứng dụng:
- Dễ bảo trì
- Dễ mở rộng
- Dễ test
- Dễ collaborate giữa các developer

---

