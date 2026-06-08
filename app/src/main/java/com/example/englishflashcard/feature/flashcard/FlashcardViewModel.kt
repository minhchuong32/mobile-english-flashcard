package com.example.englishflashcard.feature.flashcard

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.englishflashcard.data.repository.DeckRepository
import com.example.englishflashcard.data.repository.CardRepository
import com.example.englishflashcard.data.repository.SrsRepository
import com.example.englishflashcard.data.repository.AnalyticsRepository
import com.example.englishflashcard.model.Card
import com.example.englishflashcard.model.FlashcardSet
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FlashcardViewModel(
    private val deckRepository: DeckRepository,
    private val cardRepository: CardRepository,
    private val srsRepository: SrsRepository,
    private val analyticsRepository: AnalyticsRepository
) {
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    private var sessionId: String? = null
    private var startTimeMillis: Long = System.currentTimeMillis()
    private val pendingSaveJobs = mutableListOf<kotlinx.coroutines.Job>()

    var selectedDeck by mutableStateOf<FlashcardSet?>(null)
        private set

    var cardIndex by mutableStateOf(0)
        private set

    var showAnswer by mutableStateOf(false)
        private set

    var isFinished by mutableStateOf(false)
        private set

    var isLoading by mutableStateOf(false)
        private set

    // For exercise mode
    var typedAnswer by mutableStateOf("")
    var resultText by mutableStateOf("")
    var correctCount by mutableStateOf(0)
    var wrongCount by mutableStateOf(0)

    var alreadyFinishedToday by mutableStateOf(false)
        private set

    suspend fun loadData(deckId: String? = null) {
        isLoading = true
        alreadyFinishedToday = false
        val remoteDecks = deckRepository.getDecksRemote()
        val targetDeck = if (deckId != null) {
            remoteDecks.firstOrNull { it._id == deckId }
        } else {
            remoteDecks.firstOrNull()
        }
        if (targetDeck != null) {
            var maxCardsToStudy = 20
            try {
                val dailyPlanRes = srsRepository.getDailyPlanRemote(targetDeck._id)
                if (dailyPlanRes != null) {
                    val newWordsToday = dailyPlanRes.progress.newWordsToday
                    val limit = dailyPlanRes.progress.dailyNewWordLimit
                    maxCardsToStudy = (limit - newWordsToday).coerceAtLeast(0)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            if (maxCardsToStudy <= 0) {
                alreadyFinishedToday = true
                isFinished = true
                selectedDeck = targetDeck.copy(cards = emptyList())
            } else {
                val cards = srsRepository.getLearningCardsRemote(targetDeck._id)
                val limitedCards = if (cards.size > maxCardsToStudy) cards.take(maxCardsToStudy) else cards
                selectedDeck = targetDeck.copy(cards = limitedCards)

                try {
                    val session = srsRepository.startSessionRemote(targetDeck._id, "flashcard")
                    sessionId = session?._id
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                startTimeMillis = System.currentTimeMillis()
            }
        }
        isLoading = false
    }

    fun selectDeck(deck: FlashcardSet) {
        selectedDeck = deck
        resetSession()
        coroutineScope.launch {
            try {
                val session = srsRepository.startSessionRemote(deck._id, "flashcard")
                sessionId = session?._id
            } catch (e: Exception) {
                e.printStackTrace()
            }
            startTimeMillis = System.currentTimeMillis()
        }
    }

    fun currentCard(): Card? = selectedDeck?.cards?.getOrNull(cardIndex)

    fun currentTotal(): Int = selectedDeck?.cards?.size ?: 0

    fun flip() {
        showAnswer = !showAnswer
    }

    fun nextCard() {
        val cards = selectedDeck?.cards ?: emptyList()
        if (cards.isEmpty()) return
        if (cardIndex >= cards.size - 1) {
            isFinished = true
            sessionId?.let { sId ->
                coroutineScope.launch {
                    pendingSaveJobs.forEach { it.join() }
                    try {
                        srsRepository.completeSessionRemote(sId)
                        analyticsRepository.fetchAnalyticsRemote()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        } else {
            cardIndex++
            showAnswer = false
            typedAnswer = ""
        }
    }

    // Handles the SM-2 SRS inputs: "again", "hard", "good", "easy"
    fun submitDifficulty(difficulty: String) {
        val isCorrect = difficulty != "again"
        if (isCorrect) correctCount++ else wrongCount++

        val cardId = currentCard()?._id
        val currentTime = System.currentTimeMillis()
        val timeSpent = currentTime - startTimeMillis
        startTimeMillis = currentTime

        if (cardId != null) {
            srsRepository.saveCardSrs(cardId = cardId, rating = difficulty, remembered = isCorrect)

            sessionId?.let { sId ->
                val job = coroutineScope.launch {
                    try {
                        srsRepository.saveAnswerRemote(
                            sessionId = sId,
                            cardId = cardId,
                            questionType = "flashcard",
                            userAnswer = difficulty,
                            isCorrect = isCorrect,
                            skipped = false,
                            difficulty = difficulty,
                            timeSpentMs = timeSpent
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                pendingSaveJobs.add(job)
            }
        }

        analyticsRepository.recordStudy(
            correctDelta = 0,
            totalDelta = 1
        )
        
        nextCard()
    }

    fun checkExerciseAnswer() {
        val card = currentCard() ?: return
        val isCorrect = typedAnswer.trim().equals(card.definitionVi.trim(), ignoreCase = true)
        val cardId = card._id
        val currentTime = System.currentTimeMillis()
        val timeSpent = currentTime - startTimeMillis
        startTimeMillis = currentTime

        if (isCorrect) {
            correctCount++
            resultText = "Chính xác!"
        } else {
            wrongCount++
            resultText = "Sai rồi. Đáp án đúng: ${card.definitionVi}"
        }

        srsRepository.saveCardSrs(cardId = cardId, rating = if (isCorrect) "good" else "again", remembered = isCorrect)

        sessionId?.let { sId ->
            coroutineScope.launch {
                try {
                    srsRepository.saveAnswerRemote(
                        sessionId = sId,
                        cardId = cardId,
                        questionType = "type_answer",
                        userAnswer = typedAnswer,
                        isCorrect = isCorrect,
                        skipped = false,
                        difficulty = null,
                        timeSpentMs = timeSpent
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        analyticsRepository.recordStudy(
            correctDelta = if (isCorrect) 1 else 0,
            totalDelta = 1
        )

        nextCard()
    }

    fun resetSession() {
        cardIndex = 0
        showAnswer = false
        typedAnswer = ""
        resultText = ""
        correctCount = 0
        wrongCount = 0
        isFinished = false
        sessionId = null
    }
}
