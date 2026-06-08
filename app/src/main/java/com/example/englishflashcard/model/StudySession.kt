package com.example.englishflashcard.model

data class StudySession(
    val _id: String = "",
    val userId: String = "",
    val progressId: String = "",
    val setId: String = "",
    val startTime: String = "",
    val endTime: String? = null,
    val cardsStudied: Int = 0,
    val newCardsLearned: Int = 0,
    val correctAnswers: Int = 0,
    val skippedCards: Int = 0,
    val sessionType: String = "flashcard", // "flashcard", "quiz", "listen_fill", "listen_choose", "mixed"
    val isCompleted: Boolean = false
)

data class SessionAnswer(
    val _id: String = "",
    val sessionId: String = "",
    val cardId: String = "",
    val questionType: String = "", // "flashcard", "multiple_choice", "listen_fill", "listen_choose", "type_answer"
    val userAnswer: String = "",
    val isCorrect: Boolean = false,
    val skipped: Boolean = false,
    val difficulty: String? = null, // "again", "hard", "good", "easy"
    val timeSpentMs: Long = 0
)

enum class StudyMode {
    FLASHCARD,
    EXERCISE
}

data class StudyStats(
    val learnedCards: Int = 0,
    val streakDays: Int = 0,
    val correctAnswers: Int = 0,
    val totalAnswers: Int = 0
)