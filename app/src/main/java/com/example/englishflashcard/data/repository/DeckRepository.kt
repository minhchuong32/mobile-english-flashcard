package com.example.englishflashcard.data.repository

import androidx.compose.runtime.mutableStateListOf
import com.example.englishflashcard.data.api.DeckApiService
import com.example.englishflashcard.model.CreateDeckRequest
import com.example.englishflashcard.model.FlashcardSet

class DeckRepository(
    private val deckApiService: DeckApiService,
    private val analyticsRepository: AnalyticsRepository,
    private val notificationRepository: NotificationRepository
) {
    private val localDecks = mutableStateListOf<FlashcardSet>()
    private val remoteDecks = mutableStateListOf<FlashcardSet>()

    suspend fun fetchDecksRemote() {
        try {
            // Tải thông tin thống kê thật
            analyticsRepository.fetchAnalyticsRemote()

            // Lấy tất cả bộ thẻ: gồm bộ thẻ của mình và bộ thẻ public của người khác
            val response = deckApiService.getDecks()
            if (response.isSuccessful) {
                val deckResponse = response.body()
                println("API Success: Found ${deckResponse?.sets?.size ?: 0} decks")
                remoteDecks.clear()
                deckResponse?.sets?.let {
                    remoteDecks.addAll(it)
                }
            } else {
                println("API Error: ${response.code()} - ${response.errorBody()?.string()}")
            }
        } catch (e: Exception) {
            println("API Exception: ${e.message}")
            e.printStackTrace()
        }
    }

    fun getAllDecks(): List<FlashcardSet> {
        return localDecks + remoteDecks
    }

    fun getDecks(): List<FlashcardSet> = localDecks.toList()

    suspend fun getDecksRemote(): List<FlashcardSet> {
        fetchDecksRemote()
        return remoteDecks.toList()
    }

    fun getDeck(deckId: String): FlashcardSet? = getAllDecks().firstOrNull { it._id == deckId }

    fun createDeck(title: String, description: String): FlashcardSet {
        val deck = FlashcardSet(
            _id = "deck-${localDecks.size + 1}",
            title = title.trim(),
            description = description.trim(),
            cards = emptyList()
        )
        localDecks.add(deck)
        notificationRepository.addNotification("Tạo bộ thẻ", "Bạn vừa tạo bộ thẻ: ${deck.title}")
        return deck
    }

    suspend fun createDeckRemote(title: String, description: String, tags: List<String>): FlashcardSet? {
        return try {
            val response = deckApiService.createDeck(CreateDeckRequest(title, description, tags = tags))
            if (response.isSuccessful) {
                val newDeck = response.body()
                if (newDeck != null) {
                    remoteDecks.add(0, newDeck)
                }
                newDeck
            } else null
        } catch (e: Exception) {
            null
        }
    }

    // Helper to update remote decks list from CardRepository if needed
    fun updateRemoteDeckCards(setId: String, cards: List<com.example.englishflashcard.model.Card>) {
        val index = remoteDecks.indexOfFirst { it._id == setId }
        if (index != -1) {
            remoteDecks[index] = remoteDecks[index].copy(cards = cards)
        }
    }

    fun updateRemoteDeckCard(id: String, updatedCard: com.example.englishflashcard.model.Card) {
        for (i in remoteDecks.indices) {
            val deck = remoteDecks[i]
            val cardIndex = deck.cards.indexOfFirst { it._id == id }
            if (cardIndex != -1) {
                val newCards = deck.cards.toMutableList()
                newCards[cardIndex] = updatedCard
                remoteDecks[i] = deck.copy(cards = newCards)
                break
            }
        }
    }

    fun removeRemoteDeckCard(id: String) {
        for (i in remoteDecks.indices) {
            val deck = remoteDecks[i]
            val cardIndex = deck.cards.indexOfFirst { it._id == id }
            if (cardIndex != -1) {
                val newCards = deck.cards.toMutableList()
                newCards.removeAt(cardIndex)
                remoteDecks[i] = deck.copy(cards = newCards)
                break
            }
        }
    }
    
    // For local decks
    fun addLocalCard(deckId: String, card: com.example.englishflashcard.model.Card): Boolean {
        val index = localDecks.indexOfFirst { it._id == deckId }
        if (index == -1) return false
        val oldDeck = localDecks[index]
        localDecks[index] = oldDeck.copy(cards = oldDeck.cards + card)
        return true
    }
}
