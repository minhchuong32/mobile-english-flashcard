package com.example.englishflashcard.data.api

import com.example.englishflashcard.model.*
import retrofit2.Response
import retrofit2.http.*

interface StudySessionApiService {
    @GET("api/v1/study-sessions/review-cards")
    suspend fun getAllReviewCards(): Response<ReviewCardsResponse>

    @GET("api/v1/study-sessions/daily-plan/{setId}")
    suspend fun getDailyPlan(
        @Path("setId") setId: String
    ): Response<DailyPlanResponse>

    @GET("api/v1/study-sessions/studied-today/{setId}")
    suspend fun getCardsStudiedToday(
        @Path("setId") setId: String
    ): Response<StudiedTodayResponse>

    @GET("api/v1/study-sessions/learning-cards/{setId}")
    suspend fun getLearningCards(
        @Path("setId") setId: String
    ): Response<LearningCardsResponse>

    @POST("api/v1/study-sessions/start")
    suspend fun startSession(
        @Body request: StartSessionRequest
    ): Response<StartSessionResponse>

    @POST("api/v1/study-sessions/{sessionId}/answers")
    suspend fun saveAnswer(
        @Path("sessionId") sessionId: String,
        @Body request: SaveAnswerRequest
    ): Response<SaveAnswerResponse>

    @POST("api/v1/study-sessions/{sessionId}/complete")
    suspend fun completeSession(
        @Path("sessionId") sessionId: String
    ): Response<CompleteSessionResponse>
}
