package com.example.englishflashcard.model

data class UserStudyProgress(
    val _id: String = "",
    val userId: String = "",
    val setId: String = "",
    val totalLearned: Int = 0,
    val totalMemorized: Int = 0,
    val newWordsToday: Int = 0,
    val dailyNewWordLimit: Int = 20,
    val lastStudiedAt: String? = null,
    val dailyResetAt: String? = null,
    val nextReviewAt: String? = null
)
data class StudyProgress(
    val totalLearned: Int = 0,
    val totalMemorized: Int = 0
)
