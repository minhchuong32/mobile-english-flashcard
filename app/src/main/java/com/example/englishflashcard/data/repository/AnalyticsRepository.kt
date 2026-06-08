package com.example.englishflashcard.data.repository

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.englishflashcard.data.api.UserApiService
import com.example.englishflashcard.model.AnalyticsResponse
import com.example.englishflashcard.model.StudyStats

class AnalyticsRepository(private val userApiService: UserApiService) {
    var stats by mutableStateOf(StudyStats())
        private set

    var analyticsData by mutableStateOf<AnalyticsResponse?>(null)
        private set

    var isLoading by mutableStateOf(false)
        private set

    fun recordStudy(correctDelta: Int, totalDelta: Int) {
        stats = stats.copy(
            correctAnswers = stats.correctAnswers + correctDelta,
            totalAnswers = stats.totalAnswers + totalDelta
        )
    }

    suspend fun fetchAnalyticsRemote() {
        isLoading = true
        try {
            val response = userApiService.getUserAnalytics()
            if (response.isSuccessful) {
                val analytics = response.body()
                analyticsData = analytics
                analytics?.let {
                    stats = stats.copy(
                        streakDays = it.overview.currentStreak,
                        learnedCards = it.overview.totalLearned,
                        correctAnswers = it.overview.totalMemorized,
                        totalAnswers = it.overview.totalLearned
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            isLoading = false
        }
    }
}
