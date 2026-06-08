package com.example.englishflashcard.model

data class AnalyticsResponse(
    val overview: OverviewStats,
    val studyHistory: List<StudyHistoryItem>
)

data class OverviewStats(
    val totalLearned: Int = 0,
    val totalMemorized: Int = 0,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val totalDaysStudied: Int = 0,
    val accuracy: Double = 0.0,
    val estimatedLevel: String = "",
    val todayLearned: Int = 0,
    val todayRemembered: Int = 0
)

data class StudyHistoryItem(
    val date: String = "",
    val cardsStudied: Int = 0,
    val setsStudied: Int = 0,
    val _id: String = ""
)
