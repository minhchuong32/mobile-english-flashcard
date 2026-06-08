package com.example.englishflashcard.data.api

import com.example.englishflashcard.model.CreateDeckRequest
import com.example.englishflashcard.model.DeckResponse
import com.example.englishflashcard.model.FlashcardSet
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface DeckApiService {
    @GET("api/v1/flashcard-sets")
    suspend fun getDecks(
        @Query("mine") mine: Boolean? = null
    ): Response<DeckResponse>

    @POST("api/v1/flashcard-sets")
    suspend fun createDeck(@Body request: CreateDeckRequest): Response<FlashcardSet>
}
