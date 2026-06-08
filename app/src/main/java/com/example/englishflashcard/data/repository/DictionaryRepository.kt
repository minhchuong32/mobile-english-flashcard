package com.example.englishflashcard.data.repository

import com.example.englishflashcard.data.api.DictionaryApiService
import com.example.englishflashcard.model.DictionaryEntry

class DictionaryRepository(private val dictionaryApiService: DictionaryApiService) {
    suspend fun getDictionaryInfo(word: String): DictionaryEntry? {
        return try {
            val response = dictionaryApiService.getDictionaryData(word)
            if (response.isSuccessful) response.body()?.firstOrNull() else null
        } catch (e: Exception) {
            null
        }
    }
}
