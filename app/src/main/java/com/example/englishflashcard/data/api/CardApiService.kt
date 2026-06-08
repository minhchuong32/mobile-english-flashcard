package com.example.englishflashcard.data.api

import com.example.englishflashcard.model.Card
import com.example.englishflashcard.model.CreateCardRequest
import retrofit2.Response
import retrofit2.http.*

interface CardApiService {
    @POST("api/v1/flashcard-sets/{setId}/cards")
    suspend fun createCard(
        @Path("setId") setId: String,
        @Body request: CreateCardRequest
    ): Response<Card>

    @GET("api/v1/flashcard-sets/{setId}/cards")
    suspend fun getCards(
        @Path("setId") setId: String
    ): Response<List<Card>>

    @GET("api/v1/cards/{id}")
    suspend fun getCardDetail(
        @Path("id") id: String
    ): Response<Card>

    @PUT("api/v1/cards/{id}")
    suspend fun updateCard(
        @Path("id") id: String,
        @Body card: Card
    ): Response<Card>

    @DELETE("api/v1/cards/{id}")
    suspend fun deleteCard(
        @Path("id") id: String
    ): Response<Unit>
}
