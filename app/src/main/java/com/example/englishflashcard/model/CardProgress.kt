package com.example.englishflashcard.model

data class CardProgress(
    val _id: String = "",
    val userId: String = "",
    val cardId: String = "",
    val progressId: String = "",
    val status: String = "new", // "new", "learning", "review", "memorized"
    val difficulty: String = "good", // "again", "hard", "good", "easy"
    val skipped: Boolean = false,
    val reviewCount: Int = 0,
    val consecutiveCorrect: Int = 0,
    val interval: Int = 1,
    val easeFactor: Double = 2.5,
    val nextReviewAt: String? = null,
    val firstLearnedAt: String? = null,
    val lastReviewedAt: String? = null
)
