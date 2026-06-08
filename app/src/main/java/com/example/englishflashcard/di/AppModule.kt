package com.example.englishflashcard.di

import android.content.Context
import com.example.englishflashcard.data.api.*
import com.example.englishflashcard.data.repository.*
import com.example.englishflashcard.model.CreatedByInfo
import com.franmontiel.persistentcookiejar.PersistentCookieJar
import com.franmontiel.persistentcookiejar.cache.SetCookieCache
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object AppModule {
    // private const val BASE_URL = "http://172.20.10.4:8080/"
    // private const val BASE_URL = "http://10.0.2.2:8080/" // localhost for emulator
    private const val BASE_URL = "http://192.168.1.7:8080/"

    private lateinit var applicationContext: Context

    fun initialize(context: Context) {
        applicationContext = context.applicationContext
    }

    private val cookieJar by lazy {
        PersistentCookieJar(SetCookieCache(), SharedPrefsCookiePersistor(applicationContext))
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .cookieJar(cookieJar)
            .addInterceptor(loggingInterceptor)
            .build()
    }

    private val gson = GsonBuilder()
        .registerTypeAdapter(CreatedByInfo::class.java, CreatedByInfoDeserializer())
        .create()

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    val authApiService: AuthApiService by lazy { retrofit.create(AuthApiService::class.java) }
    val deckApiService: DeckApiService by lazy { retrofit.create(DeckApiService::class.java) }
    val cardApiService: CardApiService by lazy { retrofit.create(CardApiService::class.java) }
    val userApiService: UserApiService by lazy { retrofit.create(UserApiService::class.java) }
    val dictionaryApiService: DictionaryApiService by lazy { retrofit.create(DictionaryApiService::class.java) }
    val studySessionApiService: StudySessionApiService by lazy { retrofit.create(StudySessionApiService::class.java) }

    val srsRepository: SrsRepository by lazy {
        SrsRepository(applicationContext, studySessionApiService)
    }

    val userRepository: UserRepository by lazy {
        UserRepository(applicationContext, userApiService)
    }

    val analyticsRepository: AnalyticsRepository by lazy {
        AnalyticsRepository(userApiService)
    }

    val notificationRepository: NotificationRepository by lazy {
        NotificationRepository()
    }

    val dictionaryRepository: DictionaryRepository by lazy {
        DictionaryRepository(dictionaryApiService)
    }

    val deckRepository: DeckRepository by lazy {
        DeckRepository(deckApiService, analyticsRepository, notificationRepository)
    }

    val cardRepository: CardRepository by lazy {
        CardRepository(cardApiService, deckRepository)
    }
}
