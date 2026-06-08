package com.example.englishflashcard.data.api

import com.example.englishflashcard.model.DictionaryEntry
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface DictionaryApiService {
    @GET("https://api.dictionaryapi.dev/api/v2/entries/en/{word}")
    suspend fun getDictionaryData(@Path("word") word: String): Response<List<DictionaryEntry>>
}
