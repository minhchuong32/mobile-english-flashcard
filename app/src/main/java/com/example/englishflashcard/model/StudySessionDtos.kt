package com.example.englishflashcard.model

data class StartSessionRequest(
    val setId: String,
    val sessionType: String
)

data class StartSessionResponse(
    val message: String,
    val session: StudySession
)

data class SaveAnswerRequest(
    val cardId: String,
    val questionType: String,
    val userAnswer: String,
    val isCorrect: Boolean,
    val skipped: Boolean,
    val difficulty: String?,
    val timeSpentMs: Long
)

data class SaveAnswerResponse(
    val message: String,
    val answer: SessionAnswer
)

data class CompleteSessionResponse(
    val message: String,
    val session: StudySession
)

data class UserStudyProgressSummary(
    val totalLearned: Int,
    val totalMemorized: Int,
    val totalReviewing: Int,
    val newWordsToday: Int,
    val dailyNewWordLimit: Int
)

data class DailyPlanSummary(
    val newCardsToLearn: Int,
    val cardsToReview: Int,
    val newCards: List<Card>,
    val reviewCards: List<Card>
)

data class DailyPlanResponse(
    val progress: UserStudyProgressSummary,
    val dailyPlan: DailyPlanSummary
)

data class ReviewCardsResponse(
    val cards: List<Card>
)

data class StudiedTodayResponse(
    val cards: List<Card>
)

data class LearningCardsResponse(
    val cards: List<Card>
)
