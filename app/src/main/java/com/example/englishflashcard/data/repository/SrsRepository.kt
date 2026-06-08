package com.example.englishflashcard.data.repository

import android.content.Context
import com.example.englishflashcard.data.api.StudySessionApiService
import com.example.englishflashcard.model.*

class SrsRepository(
    private val context: Context,
    private val studySessionApiService: StudySessionApiService
) {
    private val sharedPrefs by lazy {
        context.getSharedPreferences("english_flashcard_srs", Context.MODE_PRIVATE)
    }

    fun clearCache() {
        sharedPrefs.edit().clear().apply()
    }

    fun saveCardSrs(cardId: String, rating: String, remembered: Boolean) {
        val lastStudiedTime = System.currentTimeMillis()
        sharedPrefs.edit()
            .putString("srs_$cardId", "$rating;$lastStudiedTime;$remembered")
            .apply()
    }

    fun getCardSrs(cardId: String): CardSrsState? {
        val data = sharedPrefs.getString("srs_$cardId", null) ?: return null
        val parts = data.split(";")
        if (parts.size < 3) return null
        return CardSrsState(
            cardId = cardId,
            rating = parts[0],
            lastStudiedTimeMillis = parts[1].toLongOrNull() ?: 0L,
            remembered = parts[2].toBooleanStrictOrNull() ?: false
        )
    }

    fun getRememberedCardsCountToday(): Int {
        val todayStart = getStartOfTodayMillis()
        var count = 0
        sharedPrefs.all.forEach { (key, value) ->
            if (key.startsWith("srs_") && value is String) {
                val parts = value.split(";")
                if (parts.size >= 3) {
                    val lastStudiedTime = parts[1].toLongOrNull() ?: 0L
                    val remembered = parts[2].toBooleanStrictOrNull() ?: false
                    if (remembered && lastStudiedTime >= todayStart) {
                        count++
                    }
                }
            }
        }
        return count
    }

    fun getStudiedCardsCount(): Int {
        var count = 0
        sharedPrefs.all.forEach { (key, value) ->
            if (key.startsWith("srs_") && value is String) {
                count++
            }
        }
        return count
    }

    fun isCardRememberedToday(cardId: String): Boolean {
        val srs = getCardSrs(cardId) ?: return false
        val todayStart = getStartOfTodayMillis()
        return srs.remembered && srs.lastStudiedTimeMillis >= todayStart
    }

    private fun getStartOfTodayMillis(): Long {
        val cal = java.util.Calendar.getInstance()
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
        cal.set(java.util.Calendar.MINUTE, 0)
        cal.set(java.util.Calendar.SECOND, 0)
        cal.set(java.util.Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    suspend fun startSessionRemote(setId: String, sessionType: String): StudySession? {
        return try {
            val response = studySessionApiService.startSession(StartSessionRequest(setId, sessionType))
            if (response.isSuccessful) response.body()?.session else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun saveAnswerRemote(
        sessionId: String,
        cardId: String,
        questionType: String,
        userAnswer: String,
        isCorrect: Boolean,
        skipped: Boolean,
        difficulty: String?,
        timeSpentMs: Long
    ): Boolean {
        return try {
            val response = studySessionApiService.saveAnswer(
                sessionId,
                SaveAnswerRequest(cardId, questionType, userAnswer, isCorrect, skipped, difficulty, timeSpentMs)
            )
            response.isSuccessful
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun completeSessionRemote(sessionId: String): StudySession? {
        return try {
            val response = studySessionApiService.completeSession(sessionId)
            if (response.isSuccessful) response.body()?.session else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun getAllReviewCardsRemote(): List<Card> {
        return try {
            val response = studySessionApiService.getAllReviewCards()
            if (response.isSuccessful) response.body()?.cards ?: emptyList() else emptyList()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun getDailyPlanRemote(setId: String): DailyPlanResponse? {
        return try {
            val response = studySessionApiService.getDailyPlan(setId)
            if (response.isSuccessful) response.body() else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun getCardsStudiedTodayRemote(setId: String): List<Card> {
        return try {
            val response = studySessionApiService.getCardsStudiedToday(setId)
            if (response.isSuccessful) response.body()?.cards ?: emptyList() else emptyList()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun getLearningCardsRemote(setId: String): List<Card> {
        return try {
            val response = studySessionApiService.getLearningCards(setId)
            if (response.isSuccessful) response.body()?.cards ?: emptyList() else emptyList()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}
