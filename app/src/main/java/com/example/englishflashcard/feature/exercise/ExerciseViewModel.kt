package com.example.englishflashcard.feature.exercise

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.englishflashcard.data.repository.DeckRepository
import com.example.englishflashcard.data.repository.CardRepository
import com.example.englishflashcard.data.repository.SrsRepository
import com.example.englishflashcard.data.repository.AnalyticsRepository
import com.example.englishflashcard.model.Card
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Types of quiz questions
 */
enum class QuestionType {
    MULTIPLE_CHOICE,   // Choose the correct Vietnamese meaning
    FILL_IN_BLANK,     // Type the English word from Vietnamese hint
    TRUE_FALSE         // Is this term-definition pair correct?
}

/**
 * Represents a single quiz question
 */
data class QuizQuestion(
    val card: Card,
    val type: QuestionType,
    val options: List<String> = emptyList(),       // For multiple choice
    val displayedDefinition: String = "",           // For true/false (might be wrong)
    val isCorrectPair: Boolean = true               // For true/false
)

class ExerciseViewModel(
    private val deckRepository: DeckRepository,
    private val cardRepository: CardRepository,
    private val srsRepository: SrsRepository,
    private val analyticsRepository: AnalyticsRepository
) {
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    private var sessionId: String? = null
    private var startTimeMillis: Long = System.currentTimeMillis()
    private val pendingSaveJobs = mutableListOf<kotlinx.coroutines.Job>()

    // Quiz state
    var questions by mutableStateOf<List<QuizQuestion>>(emptyList())
        private set

    var currentIndex by mutableStateOf(0)
        private set

    var selectedAnswer by mutableStateOf<String?>(null)
        private set

    var trueFalseAnswer by mutableStateOf<Boolean?>(null)
        private set

    var typedAnswer by mutableStateOf("")

    var hasAnswered by mutableStateOf(false)
        private set

    var isCorrect by mutableStateOf(false)
        private set

    var correctCount by mutableStateOf(0)
        private set

    var wrongCount by mutableStateOf(0)
        private set

    var isFinished by mutableStateOf(false)
        private set

    var isLoading by mutableStateOf(false)
        private set

    var deckTitle by mutableStateOf("")
        private set

    val totalQuestions: Int get() = questions.size

    val currentQuestion: QuizQuestion? get() = questions.getOrNull(currentIndex)

    suspend fun loadExercise(deckId: String?) {
        isLoading = true
        val remoteDecks = deckRepository.getDecksRemote()
        var targetDeckId: String? = null
        
        if (deckId == "srs_review") {
            deckTitle = "Tổng ôn tập SRS"
            val allCards = mutableListOf<Card>()
            for (deck in remoteDecks) {
                val cards = cardRepository.getCardsBySetRemote(deck._id)
                allCards.addAll(cards)
            }
            
            // Lấy danh sách các thẻ cần ôn tập từ Server (những thẻ khác 'memorized' và khác 'new')
            val serverReviewCards = srsRepository.getAllReviewCardsRemote()

            if (serverReviewCards.isNotEmpty()) {
                questions = generateQuestions(serverReviewCards, allCards)
                targetDeckId = serverReviewCards.firstOrNull()?.setId
            }
        } else {
            val targetDeck = if (deckId != null) {
                remoteDecks.firstOrNull { it._id == deckId }
            } else {
                remoteDecks.firstOrNull()
            }

            if (targetDeck != null) {
                deckTitle = targetDeck.title
                
                var cards = srsRepository.getCardsStudiedTodayRemote(targetDeck._id)
                
                val allCards = mutableListOf<Card>()
                for (deck in remoteDecks) {
                    val c = cardRepository.getCardsBySetRemote(deck._id)
                    allCards.addAll(c)
                }

                if (cards.isNotEmpty()) {
                    questions = generateQuestions(cards, allCards)
                    targetDeckId = targetDeck._id
                }
            }
        }

        if (targetDeckId != null) {
            try {
                val session = srsRepository.startSessionRemote(targetDeckId, "quiz")
                sessionId = session?._id
            } catch (e: Exception) {
                e.printStackTrace()
            }
            startTimeMillis = System.currentTimeMillis()
        }

        isLoading = false
    }

    private fun generateQuestions(cardsToTest: List<Card>, poolForDistractors: List<Card>): List<QuizQuestion> {
        val allCardsToTest = cardsToTest.filter { it.term.isNotBlank() && it.definitionVi.isNotBlank() }
        val allPool = poolForDistractors.filter { it.term.isNotBlank() && it.definitionVi.isNotBlank() }
        if (allCardsToTest.isEmpty()) return emptyList()

        val result = mutableListOf<QuizQuestion>()

        allCardsToTest.forEach { card ->
            val srs = srsRepository.getCardSrs(card._id)
            val rating = srs?.rating ?: "good"

            val allowedTypes = if (allCardsToTest.size == 1) {
                listOf(QuestionType.MULTIPLE_CHOICE, QuestionType.TRUE_FALSE, QuestionType.FILL_IN_BLANK)
            } else {
                when (rating) {
                    "easy" -> listOf(QuestionType.MULTIPLE_CHOICE)
                    "good" -> listOf(QuestionType.MULTIPLE_CHOICE, QuestionType.TRUE_FALSE)
                    "hard" -> listOf(QuestionType.MULTIPLE_CHOICE, QuestionType.TRUE_FALSE, QuestionType.FILL_IN_BLANK)
                    "again" -> listOf(QuestionType.MULTIPLE_CHOICE, QuestionType.TRUE_FALSE, QuestionType.FILL_IN_BLANK)
                    else -> listOf(QuestionType.MULTIPLE_CHOICE, QuestionType.TRUE_FALSE)
                }
            }

            allowedTypes.forEach { type ->
                when (type) {
                    QuestionType.MULTIPLE_CHOICE -> {
                        val wrongOptions = allPool
                            .filter { it._id != card._id }
                            .shuffled()
                            .take(3)
                            .map { it.definitionVi }

                        val options = (wrongOptions + card.definitionVi).shuffled()
                        result.add(
                            QuizQuestion(
                                card = card,
                                type = type,
                                options = options
                            )
                        )
                    }

                    QuestionType.FILL_IN_BLANK -> {
                        result.add(
                            QuizQuestion(
                                card = card,
                                type = type
                            )
                        )
                    }

                    QuestionType.TRUE_FALSE -> {
                        val showCorrect = (0..1).random() == 0
                        val displayedDef = if (showCorrect) {
                            card.definitionVi
                        } else {
                            val wrongCard = allPool.filter { it._id != card._id }.shuffled().firstOrNull()
                            wrongCard?.definitionVi ?: card.definitionVi
                        }
                        result.add(
                            QuizQuestion(
                                card = card,
                                type = type,
                                displayedDefinition = displayedDef,
                                isCorrectPair = showCorrect || displayedDef == card.definitionVi
                            )
                        )
                    }
                }
            }
        }

        return result.shuffled()
    }

    fun selectMultipleChoice(answer: String) {
        if (hasAnswered) return
        selectedAnswer = answer
        val correct = currentQuestion?.card?.definitionVi ?: return
        isCorrect = answer == correct
        recordAnswer(isCorrect)
    }

    fun submitFillInBlank() {
        if (hasAnswered) return
        val card = currentQuestion?.card ?: return
        isCorrect = typedAnswer.trim().equals(card.term.trim(), ignoreCase = true)
        recordAnswer(isCorrect)
    }

    fun selectTrueFalse(answer: Boolean) {
        if (hasAnswered) return
        trueFalseAnswer = answer
        val expected = currentQuestion?.isCorrectPair ?: return
        isCorrect = (answer == expected)
        recordAnswer(isCorrect)
    }

    private val wrongCards = mutableListOf<Card>()

    private fun recordAnswer(correct: Boolean) {
        hasAnswered = true
        if (correct) correctCount++ else wrongCount++
        
        val card = currentQuestion?.card
        val currentTime = System.currentTimeMillis()
        val timeSpent = currentTime - startTimeMillis
        startTimeMillis = currentTime

        if (card != null) {
            if (!correct) {
                wrongCards.add(card)
            }
            srsRepository.saveCardSrs(
                cardId = card._id,
                rating = if (correct) "good" else "again",
                remembered = correct
            )

            sessionId?.let { sId ->
                val job = coroutineScope.launch {
                    try {
                        val qType = when (currentQuestion?.type) {
                            QuestionType.MULTIPLE_CHOICE -> "multiple_choice"
                            QuestionType.FILL_IN_BLANK -> "type_answer"
                            QuestionType.TRUE_FALSE -> "true_false"
                            else -> "quiz"
                        }
                        val uAnswer = when (currentQuestion?.type) {
                            QuestionType.MULTIPLE_CHOICE -> selectedAnswer ?: ""
                            QuestionType.FILL_IN_BLANK -> typedAnswer
                            QuestionType.TRUE_FALSE -> trueFalseAnswer?.toString() ?: ""
                            else -> ""
                        }
                        srsRepository.saveAnswerRemote(
                            sessionId = sId,
                            cardId = card._id,
                            questionType = qType,
                            userAnswer = uAnswer,
                            isCorrect = correct,
                            skipped = false,
                            difficulty = null,
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
            correctDelta = if (correct) 1 else 0,
            totalDelta = 0
        )
    }

    fun goNext() {
        if (currentIndex >= totalQuestions - 1) {
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
            return
        }
        currentIndex++
        resetAnswerState()
    }

    fun restartExercise() {
        val cardsToRestart = if (wrongCards.isNotEmpty()) {
            wrongCards.toList()
        } else {
            questions.map { it.card }
        }
        val pool = questions.map { it.card }
        wrongCards.clear()

        val testList = cardsToRestart

        questions = generateQuestions(testList, pool)
        currentIndex = 0
        correctCount = 0
        wrongCount = 0
        isFinished = false
        resetAnswerState()

        val targetDeckId = testList.firstOrNull()?.setId
        if (targetDeckId != null) {
            coroutineScope.launch {
                try {
                    val session = srsRepository.startSessionRemote(targetDeckId, "quiz")
                    sessionId = session?._id
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                startTimeMillis = System.currentTimeMillis()
            }
        }
    }

    private fun resetAnswerState() {
        selectedAnswer = null
        trueFalseAnswer = null
        typedAnswer = ""
        hasAnswered = false
        isCorrect = false
    }
}
