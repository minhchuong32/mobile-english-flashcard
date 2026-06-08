package com.example.englishflashcard.data.api

import com.example.englishflashcard.model.AnalyticsResponse
import com.example.englishflashcard.model.FcmTokenRequest
import com.example.englishflashcard.model.FcmTokenResponse
import com.example.englishflashcard.model.ProfileResponse
import com.example.englishflashcard.model.UpdateProfileRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT

interface UserApiService {
    @GET("api/v1/user/profile")
    suspend fun getProfile(): Response<ProfileResponse>

    @PUT("api/v1/user/profile")
    suspend fun updateProfile(@Body request: UpdateProfileRequest): Response<ProfileResponse>

    @GET("api/v1/user/analytics")
    suspend fun getUserAnalytics(): Response<AnalyticsResponse>

    @POST("api/v1/fcm-token")
    suspend fun updateFcmToken(@Body request: FcmTokenRequest): Response<FcmTokenResponse>
}
