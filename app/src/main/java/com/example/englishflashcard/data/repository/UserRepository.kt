package com.example.englishflashcard.data.repository

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.englishflashcard.data.api.AuthApiService
import com.example.englishflashcard.data.api.UserApiService
import com.example.englishflashcard.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class UserRepository(
    private val context: Context,
    private val userApiService: UserApiService
) {
    private val prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

    private val users = mutableStateListOf(
        User(
            _id = "u1",
            name = "Demo User",
            email = "demo@gmail.com",
            password = "123456"
        )
    )

    var currentUser by mutableStateOf<User?>(null)
        private set

    var fcmToken: String?
        get() = prefs.getString("fcm_token", null)
        set(value) = prefs.edit().putString("fcm_token", value).apply()

    suspend fun updateFcmToken(userId: String, token: String): FcmTokenResponse? {
        return try {
            val response = userApiService.updateFcmToken(FcmTokenRequest(userId, token))
            if (response.isSuccessful) {
                fcmToken = token // Save locally that we have this token
                response.body()
            } else null
        } catch (e: Exception) {
            null
        }
    }

    fun syncFcmToken() {
        val token = fcmToken
        val userId = currentUser?._id
        if (token != null && userId != null) {
            CoroutineScope(Dispatchers.IO).launch {
                updateFcmToken(userId, token)
            }
        }
    }

    suspend fun getProfileRemote(): ProfileResponse? {
        return try {
            val response = userApiService.getProfile()
            if (response.isSuccessful) {
                val body = response.body()
                body?.data?.let { data ->
                    currentUser = User(
                        _id = data.userId,
                        username = data.username,
                        email = data.email,
                        role = data.role,
                        profile = data.profile,
                        name = data.profile.fullName.ifBlank { data.username }
                    )
                    syncFcmToken()
                }
                body
            } else null
        } catch (e: Exception) {
            null
        }
    }

    suspend fun updateProfileRemote(fullName: String, bio: String, phoneNumber: String): ProfileResponse? {
        return try {
            val response = userApiService.updateProfile(UpdateProfileRequest(fullName, bio, phoneNumber))
            if (response.isSuccessful) {
                val body = response.body()
                body?.data?.let { data ->
                    currentUser = User(
                        _id = data.userId,
                        username = data.username,
                        email = data.email,
                        role = data.role,
                        profile = data.profile,
                        name = data.profile.fullName.ifBlank { data.username }
                    )
                }
                body
            } else null
        } catch (e: Exception) {
            null
        }
    }


    fun logout() {
        currentUser = null
    }
}
