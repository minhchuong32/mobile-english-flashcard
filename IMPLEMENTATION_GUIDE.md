# Hướng Dẫn Sử Dụng Các Thành Phần Trong Project

Tài liệu này hướng dẫn cách các thành phần chính trong project hoạt động với nhau qua các ví dụ thực tế.

---

## Mục Lục

1. [Luồng Đăng Nhập (Complete Flow)](#luồng-đăng-nhập-complete-flow)
2. [Luồng Tạo & Xem Deck](#luồng-tạo--xem-deck)
3. [Luồng Học Flashcard & SRS](#luồng-học-flashcard--srs)
4. [Quản Lý State trong Compose](#quản-lý-state-trong-compose)
5. [Calling APIs & Error Handling](#calling-apis--error-handling)
6. [Firebase Notifications Flow](#firebase-notifications-flow)

---

## Luồng Đăng Nhập (Complete Flow)

### Bước 1: User nhập email/password và click Login

```kotlin
@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onLoginSuccess: () -> Unit,
    onGoRegister: () -> Unit,
    onForgotPasswordClick: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    
    // Lấy trạng thái từ ViewModel
    val loginState by viewModel.loginState.collectAsState()
    val isLoading = loginState is AuthState.Loading
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        // Input fields
        TextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") }
        )
        
        TextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = if (isPasswordVisible) 
                VisualTransformation.None 
            else 
                PasswordVisualTransformation()
        )
        
        // Login button
        Button(
            onClick = {
                // === User click -> call ViewModel method ===
                viewModel.login(email, password)
            },
            enabled = !isLoading && email.isNotEmpty() && password.isNotEmpty()
        ) {
            if (isLoading) {
                CircularProgressIndicator()
            } else {
                Text("Login")
            }
        }
        
        // Handle login state changes
        LaunchedEffect(loginState) {
            when (loginState) {
                is AuthState.Success -> {
                    // Đăng nhập thành công -> navigate
                    onLoginSuccess()
                }
                is AuthState.Error -> {
                    // Hiển thị lỗi
                    val error = (loginState as AuthState.Error).message
                    showErrorDialog(error)
                }
                else -> { }
            }
        }
    }
}
```

### Bước 2: ViewModel xử lý login

```kotlin
class AuthViewModel(
    private val authRepository: AuthRepository,
    private val deckRepository: DeckRepository
) : ViewModel() {
    
    // State cho UI
    private val _loginState = MutableStateFlow<AuthState>(AuthState.Idle)
    val loginState: StateFlow<AuthState> = _loginState.asStateFlow()
    
    fun login(email: String, password: String) {
        viewModelScope.launch {
            try {
                // === 1. Set loading state ===
                _loginState.value = AuthState.Loading
                
                // === 2. Call repository (không biết repository dùng API hay cache) ===
                val loginResponse = authRepository.login(email, password)
                
                // === 3. Đăng nhập thành công -> load dữ liệu user ===
                // ViewModel có thể thực hiện thêm logic sau đăng nhập
                // ví dụ: tải lại deck từ server, clear cache SRS
                
                // === 4. Update state to success ===
                _loginState.value = AuthState.Success(loginResponse.user)
                
            } catch (e: Exception) {
                // === Handle error ===
                _loginState.value = AuthState.Error(e.message ?: "Unknown error")
                Log.e("AuthViewModel", "Login error", e)
            }
        }
    }
}

// === Auth state sealed class ===
sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val user: User) : AuthState()
    data class Error(val message: String) : AuthState()
}
```

### Bước 3: Repository gọi API

```kotlin
class AuthRepositoryImpl(
    private val authApiService: AuthApiService
) : AuthRepository {
    
    override suspend fun login(
        email: String,
        password: String
    ): LoginResponse = withContext(Dispatchers.IO) {
        try {
            // === Tạo request object ===
            val request = LoginRequest(
                email = email,
                password = password
            )
            
            // === Gọi API service ===
            val response = authApiService.login(request)
            
            // === Check response ===
            if (response.isSuccessful) {
                val body = response.body() ?: throw Exception("Empty response")
                
                // === Cache data locally (nếu cần) ===
                // saveLoginResponseLocally(body)
                
                return@withContext body
            } else {
                // API trả về lỗi (4xx, 5xx)
                throw HttpException(response.code(), response.message())
            }
        } catch (e: Exception) {
            Log.e("AuthRepository", "Login failed", e)
            throw e
        }
    }
}
```

### Bước 4: API Service định nghĩa endpoint

```kotlin
interface AuthApiService {
    @Headers("Content-Type: application/json")
    @POST("api/auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<LoginResponse>
}

// === Request/Response models ===
data class LoginRequest(
    val email: String,
    val password: String
)

data class LoginResponse(
    val accessToken: String,
    val refreshToken: String,
    val user: User
)

data class User(
    val id: String,
    val email: String,
    val username: String
)
```

### Bước 5: Retrofit tạo HTTP request

```
Retrofit intercepts authApiService.login() call:

1. Serialize LoginRequest thành JSON
   {
       "email": "user@example.com",
       "password": "password123"
   }

2. Create HTTP POST request
   POST /api/auth/login HTTP/1.1
   Host: 192.168.1.7:8080
   Content-Type: application/json
   
   {"email":"user@example.com","password":"password123"}

3. Pass to OkHttpClient
   - OkHttpClient.cookieJar: add cookies from previous requests
   - HttpLoggingInterceptor: log request/response
   - Custom interceptor: add Authorization header

4. Send request to server

5. Server responds:
   HTTP/1.1 200 OK
   Content-Type: application/json
   
   {
       "accessToken": "jwt_token_here",
       "refreshToken": "refresh_token_here",
       "user": {
           "id": "123",
           "email": "user@example.com",
           "username": "username"
       }
   }

6. Deserialize response JSON thành LoginResponse object
   Gson.fromJson(responseBody, LoginResponse::class.java)

7. Return Response<LoginResponse> to authApiService
```

### Bước 6: Flow hoàn chỉnh

```
┌──────────────────────────────────────────┐
│ UI (LoginScreen)                         │
│ Button click -> call viewModel.login()   │
└────┬─────────────────────────────────────┘
     │
     ↓
┌──────────────────────────────────────────┐
│ ViewModel (AuthViewModel)                │
│ 1. Set state = Loading                   │
│ 2. Call authRepository.login()           │
│ 3. Catch result/error                    │
│ 4. Set state = Success/Error             │
└────┬─────────────────────────────────────┘
     │
     ↓
┌──────────────────────────────────────────┐
│ Repository (AuthRepositoryImpl)           │
│ 1. Create LoginRequest                   │
│ 2. Call authApiService.login()           │
│ 3. Handle response                       │
│ 4. Return to ViewModel                   │
└────┬─────────────────────────────────────┘
     │
     ↓
┌──────────────────────────────────────────┐
│ API Service (AuthApiService - interface) │
│ - Define API endpoint                    │
│ - Retrofit creates implementation        │
└────┬─────────────────────────────────────┘
     │
     ↓
┌──────────────────────────────────────────┐
│ Retrofit + OkHttp                        │
│ 1. Serialize request to JSON             │
│ 2. Add interceptors (cookies, headers)   │
│ 3. Send HTTP POST to server              │
│ 4. Parse response JSON                   │
└────┬─────────────────────────────────────┘
     │
     ↓
┌──────────────────────────────────────────┐
│ Backend Server                           │
│ GET /api/auth/login                      │
│ - Check user exists                      │
│ - Verify password                        │
│ - Create JWT tokens                      │
│ - Return response                        │
└────┬─────────────────────────────────────┘
     │
     ↓ (response back through same chain)
     
UI receives state change -> recompose with result
```

---

## Luồng Tạo & Xem Deck

### User interaction

```kotlin
@Composable
fun DecksScreen(
    viewModel: DeckViewModel,
    onNavigateToDeckDetail: (String) -> Unit,
    onNavigateToCreateDeck: () -> Unit
) {
    val decks by viewModel.decks.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    
    LaunchedEffect(Unit) {
        // === Load decks khi screen hiển thị lần đầu ===
        viewModel.loadDecks()
    }
    
    Column {
        Button(onClick = onNavigateToCreateDeck) {
            Text("New Deck")
        }
        
        when {
            isLoading -> CircularProgressIndicator()
            decks.isEmpty() -> Text("No decks. Create one!")
            else -> {
                LazyColumn {
                    items(decks) { deck ->
                        DeckItem(
                            deck = deck,
                            onClick = {
                                // === Navigate to detail ===
                                onNavigateToDeckDetail(deck.id)
                            }
                        )
                    }
                }
            }
        }
    }
}
```

### ViewModel load decks

```kotlin
class DeckViewModel(
    private val deckRepository: DeckRepository
) : ViewModel() {
    
    private val _decks = MutableStateFlow<List<Deck>>(emptyList())
    val decks: StateFlow<List<Deck>> = _decks.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    fun loadDecks() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                
                // === Call repository ===
                val deckList = deckRepository.getDecks()
                
                // === Update state ===
                _decks.value = deckList
                
            } finally {
                _isLoading.value = false
            }
        }
    }
}
```

### Repository fetch từ API

```kotlin
class DeckRepositoryImpl(
    private val deckApiService: DeckApiService,
    private val analyticsRepository: AnalyticsRepository
) : DeckRepository {
    
    override suspend fun getDecks(): List<Deck> = withContext(Dispatchers.IO) {
        // === Call API ===
        val response = deckApiService.getDecks()
        
        if (response.isSuccessful) {
            val decks = response.body() ?: emptyList()
            
            // === Optional: Track analytics ===
            analyticsRepository.trackDeckLoaded(decks.size)
            
            return@withContext decks
        } else {
            throw Exception("Failed to load decks")
        }
    }
}
```

### API interface

```kotlin
interface DeckApiService {
    @GET("api/decks")
    suspend fun getDecks(): Response<List<Deck>>
    
    @GET("api/decks/{deckId}")
    suspend fun getDeckDetail(
        @Path("deckId") deckId: String
    ): Response<DeckDetail>
    
    @POST("api/decks")
    suspend fun createDeck(
        @Body request: CreateDeckRequest
    ): Response<Deck>
}

data class Deck(
    val id: String,
    val title: String,
    val description: String,
    val cardCount: Int
)

data class DeckDetail(
    val id: String,
    val title: String,
    val cards: List<Card>
)

data class CreateDeckRequest(
    val title: String,
    val description: String
)
```

---

## Luồng Học Flashcard & SRS

### UI render flashcard

```kotlin
@Composable
fun FlashcardScreen(
    deckId: String,
    viewModel: FlashcardViewModel
) {
    val card by viewModel.currentCard.collectAsState()
    val isFlipped by viewModel.isCardFlipped.collectAsState()
    val progress by viewModel.progress.collectAsState()
    
    LaunchedEffect(deckId) {
        // === Load flashcards khi screen mở ===
        viewModel.loadCards(deckId)
    }
    
    Column {
        // === Show progress ===
        Text("${progress.current} / ${progress.total}")
        
        // === Show flashcard ===
        if (card != null) {
            FlashcardView(
                card = card!!,
                isFlipped = isFlipped,
                onFlip = { viewModel.flipCard() },
                onCorrect = {
                    // === User said "Correct" ===
                    viewModel.markCardAsCorrect()
                },
                onIncorrect = {
                    // === User said "Incorrect" ===
                    viewModel.markCardAsIncorrect()
                }
            )
        }
    }
}

@Composable
fun FlashcardView(
    card: Card,
    isFlipped: Boolean,
    onFlip: () -> Unit,
    onCorrect: () -> Unit,
    onIncorrect: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
            .clickable { onFlip() }
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (isFlipped) {
                // === Mặt sau (meaning) ===
                Text(card.meaning, fontSize = 24.sp)
            } else {
                // === Mặt trước (word) ===
                Text(card.word, fontSize = 32.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
    
    Row {
        Button(onClick = onIncorrect) { Text("Incorrect") }
        Button(onClick = onCorrect) { Text("Correct") }
    }
}
```

### ViewModel quản lý SRS state

```kotlin
class FlashcardViewModel(
    private val srsRepository: SrsRepository,
    private val analyticsRepository: AnalyticsRepository
) : ViewModel() {
    
    private val _currentCard = MutableStateFlow<Card?>(null)
    val currentCard: StateFlow<Card?> = _currentCard.asStateFlow()
    
    private val _isCardFlipped = MutableStateFlow(false)
    val isCardFlipped: StateFlow<Boolean> = _isCardFlipped.asStateFlow()
    
    private val _progress = MutableStateFlow(Progress(0, 0))
    val progress: StateFlow<Progress> = _progress.asStateFlow()
    
    private var cardQueue: List<Card> = emptyList()
    private var currentIndex = 0
    
    fun loadCards(deckId: String) {
        viewModelScope.launch {
            try {
                // === Load cards từ SRS repository ===
                cardQueue = srsRepository.getCardsToLearn(deckId)
                
                if (cardQueue.isNotEmpty()) {
                    _currentCard.value = cardQueue[0]
                    _progress.value = Progress(1, cardQueue.size)
                }
            } catch (e: Exception) {
                Log.e("FlashcardViewModel", "Failed to load cards", e)
            }
        }
    }
    
    fun flipCard() {
        _isCardFlipped.value = !_isCardFlipped.value
    }
    
    fun markCardAsCorrect() {
        viewModelScope.launch {
            if (currentIndex >= cardQueue.size) return@launch
            
            val card = cardQueue[currentIndex]
            
            // === Update SRS: next review date ===
            srsRepository.updateCardSuccess(card.id)
            
            // === Track analytics ===
            analyticsRepository.trackCardReview(card.id, isCorrect = true)
            
            // === Move to next card ===
            moveToNextCard()
        }
    }
    
    fun markCardAsIncorrect() {
        viewModelScope.launch {
            if (currentIndex >= cardQueue.size) return@launch
            
            val card = cardQueue[currentIndex]
            
            // === Update SRS: reset interval ===
            srsRepository.updateCardFailure(card.id)
            
            // === Track analytics ===
            analyticsRepository.trackCardReview(card.id, isCorrect = false)
            
            // === Move to next card ===
            moveToNextCard()
        }
    }
    
    private suspend fun moveToNextCard() {
        currentIndex++
        _isCardFlipped.value = false  // Reset flip state
        
        if (currentIndex < cardQueue.size) {
            _currentCard.value = cardQueue[currentIndex]
            _progress.value = Progress(currentIndex + 1, cardQueue.size)
        } else {
            // === Hết card ===
            _currentCard.value = null
        }
    }
}

data class Progress(val current: Int, val total: Int)
```

### SRS Repository update schedules

```kotlin
class SrsRepository(
    private val context: Context,
    private val studySessionApiService: StudySessionApiService
) {
    
    suspend fun updateCardSuccess(cardId: String) = withContext(Dispatchers.IO) {
        try {
            // === Calculate next review date using SRS algorithm ===
            val nextReviewDate = calculateNextReviewDateSuccess(cardId)
            
            // === Update locally ===
            // localDatabase.updateCard(cardId, nextReviewDate)
            
            // === Sync with server ===
            studySessionApiService.updateCardProgress(
                cardId = cardId,
                isCorrect = true,
                nextReviewDate = nextReviewDate
            )
        } catch (e: Exception) {
            Log.e("SrsRepository", "Update failed", e)
        }
    }
    
    suspend fun updateCardFailure(cardId: String) = withContext(Dispatchers.IO) {
        // === Reset interval ===
        val nextReviewDate = calculateNextReviewDateFailure(cardId)
        
        studySessionApiService.updateCardProgress(
            cardId = cardId,
            isCorrect = false,
            nextReviewDate = nextReviewDate
        )
    }
    
    // === SRS algorithm ===
    private fun calculateNextReviewDateSuccess(cardId: String): LocalDateTime {
        // Spaced repetition algorithm:
        // 1st review: 1 day
        // 2nd review: 3 days
        // 3rd review: 7 days
        // 4th review: 14 days
        // etc.
        return LocalDateTime.now().plusDays(1)
    }
    
    private fun calculateNextReviewDateFailure(cardId: String): LocalDateTime {
        // Reset: review again tomorrow
        return LocalDateTime.now().plusDays(1)
    }
}
```

---

## Quản Lý State trong Compose

### Local state (within composable)

```kotlin
@Composable
fun Counter() {
    // === Tạo state local ===
    var count by remember { mutableStateOf(0) }
    
    Column {
        Text("Count: $count")
        Button(onClick = { count++ }) {
            Text("Increment")
        }
    }
    
    // Lưu ý: state này chỉ tồn tại trong composable scope
    // Khi composable destroy, state sẽ bị mất
}
```

### Lifted state (shared between composables)

```kotlin
@Composable
fun ParentScreen() {
    // === State được lift lên parent ===
    var text by remember { mutableStateOf("") }
    
    Column {
        // === Truyền state + callback xuống child ===
        TextInputField(
            value = text,
            onValueChange = { text = it }
        )
        DisplayText(text)
    }
}

@Composable
fun TextInputField(value: String, onValueChange: (String) -> Unit) {
    TextField(
        value = value,
        onValueChange = onValueChange
    )
}

@Composable
fun DisplayText(text: String) {
    Text("You entered: $text")
}
```

### State từ ViewModel (hoisted to ViewModel)

```kotlin
// === ViewModel chứa state ===
class InputViewModel : ViewModel() {
    private val _text = MutableStateFlow("")
    val text: StateFlow<String> = _text.asStateFlow()
    
    fun updateText(newText: String) {
        _text.value = newText
    }
}

// === Composable read state từ ViewModel ===
@Composable
fun InputScreen(viewModel: InputViewModel) {
    val text by viewModel.text.collectAsState()
    
    Column {
        TextField(
            value = text,
            onValueChange = { viewModel.updateText(it) }
        )
        Text("You entered: $text")
    }
}
```

### Persistence (rememberSaveable)

```kotlin
@Composable
fun PersistentInputScreen() {
    // ❌ SAI: Mất input khi device rotate
    var text by remember { mutableStateOf("") }
    
    // ✓ ĐÚNG: Input được restore khi device rotate
    var text by rememberSaveable { mutableStateOf("") }
    
    TextField(
        value = text,
        onValueChange = { text = it }
    )
}
```

---

## Calling APIs & Error Handling

### Success flow

```kotlin
try {
    // === 1. Call API ===
    val response = authApiService.login(LoginRequest(email, password))
    
    // === 2. Check if successful (2xx status code) ===
    if (response.isSuccessful) {
        // === 3. Extract body ===
        val loginResponse = response.body()
        
        if (loginResponse != null) {
            // === 4. Update UI state ===
            _loginState.value = AuthState.Success(loginResponse.user)
        } else {
            _loginState.value = AuthState.Error("Empty response")
        }
    } else {
        // === Handle HTTP error (4xx, 5xx) ===
        val errorMessage = response.errorBody()?.string() ?: response.message()
        _loginState.value = AuthState.Error("API error: $errorMessage")
    }
} catch (e: IOException) {
    // === Network error ===
    _loginState.value = AuthState.Error("Network error: ${e.message}")
} catch (e: Exception) {
    // === Other errors ===
    _loginState.value = AuthState.Error("Error: ${e.message}")
}
```

### Show error to user

```kotlin
@Composable
fun LoginScreen(viewModel: AuthViewModel) {
    val loginState by viewModel.loginState.collectAsState()
    
    LaunchedEffect(loginState) {
        when (loginState) {
            is AuthState.Error -> {
                val error = (loginState as AuthState.Error).message
                // === Show toast or snackbar ===
                showErrorMessage(error)
            }
            else -> { }
        }
    }
}
```

### Retry logic

```kotlin
class AuthViewModel(private val authRepository: AuthRepository) : ViewModel() {
    
    fun login(email: String, password: String, maxRetries: Int = 3) {
        viewModelScope.launch {
            var attempt = 0
            while (attempt < maxRetries) {
                try {
                    attempt++
                    val response = authRepository.login(email, password)
                    _loginState.value = AuthState.Success(response.user)
                    return@launch  // Success, exit
                } catch (e: Exception) {
                    if (attempt == maxRetries) {
                        // === Final attempt failed ===
                        _loginState.value = AuthState.Error("Max retries exceeded")
                    } else {
                        // === Retry after delay ===
                        delay(1000 * attempt)  // exponential backoff
                    }
                }
            }
        }
    }
}
```

---

## Firebase Notifications Flow

### 1. Get FCM token on app start

```kotlin
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // === Request notification permission ===
        askNotificationPermission()
        
        // === Get FCM token ===
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.e("FCM", "Failed", task.exception)
                return@addOnCompleteListener
            }
            
            val token = task.result
            Log.d("FCM", "Token: $token")
            
            // === Save token locally ===
            AppModule.userRepository.fcmToken = token
            
            // === Sync with server ===
            viewModelScope.launch {
                AppModule.userRepository.syncFcmToken()
            }
        }
        
        // === Setup UI ===
        setContent {
            // ...
        }
    }
}
```

### 2. Receive notifications

```kotlin
// Trong AndroidManifest.xml:
<service
    android:name=".notification.MyFirebaseMessagingService"
    android:exported="false">
    <intent-filter>
        <action android:name="com.google.firebase.MESSAGING_EVENT" />
    </intent-filter>
</service>

// MyFirebaseMessagingService.kt:
class MyFirebaseMessagingService : FirebaseMessagingService() {
    
    // === Called when app receives message ===
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d("FCM", "Message from: ${remoteMessage.from}")
        
        // === Check message has data ===
        if (remoteMessage.data.isNotEmpty()) {
            val title = remoteMessage.data["title"]
            val body = remoteMessage.data["body"]
            
            // === Show notification ===
            showNotification(title, body)
        }
    }
    
    // === Called when token refreshed ===
    override fun onNewToken(token: String) {
        Log.d("FCM", "New token: $token")
        
        // === Send new token to server ===
        sendTokenToServer(token)
    }
    
    private fun showNotification(title: String?, body: String?) {
        val notificationId = 123
        
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            // === Intent when user tap notification ===
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        
        // === Show notification ===
        NotificationManagerCompat.from(this).notify(notificationId, notification)
    }
}
```

### 3. Server sends notification

```
Server (backend):

1. User schedules study session at 9:00 AM
   user.fcmToken = "abc123def456xyz789..."
   user.studyTime = "09:00"

2. Job scheduler (e.g., cron) checks at 9:00 AM
   if (user.studyTime == now) {
       callFcmApi(user.fcmToken, {
           title: "Time to study!",
           body: "Your flashcard lesson is ready"
       })
   }

3. FCM receives request
   - Forward message to device with token "abc123def456xyz789..."

4. Device receives message
   - MyFirebaseMessagingService.onMessageReceived() called
   - Show notification to user

5. User taps notification
   - App opens
   - Navigate to study screen
```

---

## Dependency Injection Flow

### 1. Initialize AppModule

```kotlin
class EnglishFlashCardApp : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // === Initialize AppModule with context ===
        AppModule.initialize(this)
    }
}
```

### 2. Create dependencies

```kotlin
object AppModule {
    private lateinit var appContext: Context
    
    fun initialize(context: Context) {
        appContext = context.applicationContext
    }
    
    // === Create HTTP client ===
    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .cookieJar(PersistentCookieJar(
                SetCookieCache(),
                SharedPrefsCookiePersistor(appContext)
            ))
            .addInterceptor(HttpLoggingInterceptor())
            .build()
    }
    
    // === Create Retrofit ===
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    
    // === Create API services ===
    val authApiService: AuthApiService by lazy {
        retrofit.create(AuthApiService::class.java)
    }
    
    // === Create repositories ===
    val authRepository: AuthRepository by lazy {
        AuthRepositoryImpl(authApiService)
    }
}
```

### 3. Use in MainActivity

```kotlin
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            val authRepository = remember {
                AuthRepository(AppModule.authApiService)
            }
            
            AppNavGraph(
                navController = rememberNavController(),
                authRepository = authRepository,
                deckRepository = AppModule.deckRepository,
                // ... other repositories
            )
        }
    }
}
```

---

Tất cả các luồng này làm việc cùng nhau để tạo ra một ứng dụng flashcard hoàn chỉnh!

