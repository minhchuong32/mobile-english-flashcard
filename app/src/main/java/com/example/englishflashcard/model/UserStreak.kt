package com.example.englishflashcard.model

data class UserStreak(
    val _id: String = "",
    val userId: String = "",
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val lastStudyDate: String? = null,
    val studyHistory: List<StudyHistoryItem> = emptyList(),
    val totalDaysStudied: Int = 0,
    val totalCardsAllTime: Int = 0
)
