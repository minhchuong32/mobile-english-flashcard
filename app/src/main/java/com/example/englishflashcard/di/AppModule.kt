package com.example.englishflashcard.di

// Context Android để lưu dữ liệu dùng chung như cookie đăng nhập.
import android.content.Context
// Import toàn bộ API interface của app.
import com.example.englishflashcard.data.api.*
// Import toàn bộ repository của app.
import com.example.englishflashcard.data.repository.*
// Model dùng để custom parse JSON.
import com.example.englishflashcard.model.CreatedByInfo
// Thư viện lưu cookie giữa các lần mở app.
import com.franmontiel.persistentcookiejar.PersistentCookieJar
// Cache tạm cho cookie.
import com.franmontiel.persistentcookiejar.cache.SetCookieCache
// Lưu cookie vào SharedPreferences để không mất khi thoát app.
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor
// Cấu hình Gson để parse JSON.
import com.google.gson.GsonBuilder
// HTTP client của OkHttp.
import okhttp3.OkHttpClient
// Interceptor để in log request/response.
import okhttp3.logging.HttpLoggingInterceptor
// Thư viện tạo client API từ interface.
import retrofit2.Retrofit
// Converter để Retrofit dùng Gson.
import retrofit2.converter.gson.GsonConverterFactory

// Module khởi tạo dùng chung cho toàn bộ app.
object AppModule {
    // private const val BASE_URL = "http://172.20.10.4:8080/"
    // private const val BASE_URL = "http://10.0.2.2:8080/" // localhost for emulator
    // Địa chỉ backend hiện tại của ứng dụng.
    private const val BASE_URL = "http://192.168.1.7:8080/"

    // Lưu application context để dùng an toàn trong cả vòng đời app.
    private lateinit var applicationContext: Context

    // Gọi 1 lần khi app khởi động để cung cấp context cho AppModule.
    fun initialize(context: Context) {
        applicationContext = context.applicationContext
    }

    // Cookie jar để giữ phiên đăng nhập và các cookie từ server.
    private val cookieJar by lazy {
        PersistentCookieJar(SetCookieCache(), SharedPrefsCookiePersistor(applicationContext))
    }

    // Ghi log đầy đủ request/response để dễ debug API.
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    // OkHttp client dùng chung cho mọi request mạng.
    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .cookieJar(cookieJar)
            .addInterceptor(loggingInterceptor)
            .build()
    }

    // Gson dùng để chuyển JSON từ server thành object Kotlin.
    private val gson = GsonBuilder()
        .registerTypeAdapter(CreatedByInfo::class.java, CreatedByInfoDeserializer())
        .create()

    // Retrofit là nền tảng sinh các API service từ BASE_URL và OkHttpClient.
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    // API cho chức năng xác thực: đăng nhập, đăng ký, quên mật khẩu.
    val authApiService: AuthApiService by lazy { retrofit.create(AuthApiService::class.java) }
    // API cho chức năng bộ thẻ (deck).
    val deckApiService: DeckApiService by lazy { retrofit.create(DeckApiService::class.java) }
    // API cho chức năng thẻ học (card).
    val cardApiService: CardApiService by lazy { retrofit.create(CardApiService::class.java) }
    // API cho người dùng.
    val userApiService: UserApiService by lazy { retrofit.create(UserApiService::class.java) }
    // API tra từ điển.
    val dictionaryApiService: DictionaryApiService by lazy { retrofit.create(DictionaryApiService::class.java) }
    // API cho buổi học / study session.
    val studySessionApiService: StudySessionApiService by lazy { retrofit.create(StudySessionApiService::class.java) }

    // Repository cho SRS (spaced repetition system).
    val srsRepository: SrsRepository by lazy {
        SrsRepository(applicationContext, studySessionApiService)
    }

    // Repository quản lý dữ liệu người dùng.
    val userRepository: UserRepository by lazy {
        UserRepository(applicationContext, userApiService)
    }

    // Repository xử lý thống kê và analytics.
    val analyticsRepository: AnalyticsRepository by lazy {
        AnalyticsRepository(userApiService)
    }

    // Repository hỗ trợ logic thông báo.
    val notificationRepository: NotificationRepository by lazy {
        NotificationRepository()
    }

    // Repository cho tra cứu từ điển.
    val dictionaryRepository: DictionaryRepository by lazy {
        DictionaryRepository(dictionaryApiService)
    }

    // Repository quản lý bộ thẻ, có liên kết analytics và notification.
    val deckRepository: DeckRepository by lazy {
        DeckRepository(deckApiService, analyticsRepository, notificationRepository)
    }

    // Repository quản lý thẻ học, phụ thuộc vào deckRepository.
    val cardRepository: CardRepository by lazy {
        CardRepository(cardApiService, deckRepository)
    }
}
