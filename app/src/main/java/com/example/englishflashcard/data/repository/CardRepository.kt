package com.example.englishflashcard.data.repository

import com.example.englishflashcard.data.api.CardApiService
import com.example.englishflashcard.model.Card
import com.example.englishflashcard.model.CreateCardRequest
import java.util.UUID

class CardRepository(
    private val cardApiService: CardApiService,
    private val deckRepository: DeckRepository
) {
    fun addCard(
        deckId: String,
        term: String,
        pronunciation: String,
        definitionVi: String,
        example: String,
        note: String,
        tags: List<String>
    ): Boolean {
        val card = Card(
            _id = UUID.randomUUID().toString(),
            term = term.trim(),
            pronunciation = pronunciation.trim(),
            definitionVi = definitionVi.trim(),
            exampleSentence = example.trim(),
            note = note.trim(),
            tags = tags
        )
        return deckRepository.addLocalCard(deckId, card)
    }

    suspend fun createCardRemote(setId: String, card: Card): Card? {
        return try {
            val response = cardApiService.createCard(setId, CreateCardRequest(
                flashcardSetId = setId,
                term = card.term,
                pronunciation = card.pronunciation,
                partOfSpeech = card.partOfSpeech,
                definitionVi = card.definitionVi,
                exampleSentence = card.exampleSentence,
                note = card.note,
                tags = card.tags
            ))
            if (response.isSuccessful) {
                val newCard = response.body()
                if (newCard != null) {
                    val deck = deckRepository.getDeck(setId)
                    if (deck != null) {
                        deckRepository.updateRemoteDeckCards(setId, deck.cards + newCard)
                    }
                }
                newCard
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun getCardsBySetRemote(setId: String): List<Card> {
        return try {
            val response = cardApiService.getCards(setId)
            if (response.isSuccessful) {
                val remoteCards = response.body() ?: emptyList()
                deckRepository.updateRemoteDeckCards(setId, remoteCards)
                remoteCards
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun getCardDetailRemote(id: String): Card? {
        return try {
            val response = cardApiService.getCardDetail(id)
            if (response.isSuccessful) {
                response.body()
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun updateCardRemote(id: String, card: Card): Card? {
        return try {
            val response = cardApiService.updateCard(id, card)
            if (response.isSuccessful) {
                val updatedCard = response.body()
                if (updatedCard != null) {
                    deckRepository.updateRemoteDeckCard(id, updatedCard)
                }
                updatedCard
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun deleteCardRemote(id: String): Boolean {
        return try {
            val response = cardApiService.deleteCard(id)
            if (response.isSuccessful) {
                deckRepository.removeRemoteDeckCard(id)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun fetchCardsRemote(setId: String) {
        try {
            val response = cardApiService.getCards(setId)
            if (response.isSuccessful) {
                val newCards = response.body() ?: emptyList()
                deckRepository.updateRemoteDeckCards(setId, newCards)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun addCardRemote(
        deckId: String,
        term: String,
        pronunciation: String,
        definitionVi: String,
        example: String,
        note: String,
        tags: List<String>,
        partOfSpeech: String = ""
    ): Boolean {
        return try {
            val response = cardApiService.createCard(
                deckId,
                CreateCardRequest(
                    flashcardSetId = deckId,
                    term = term,
                    pronunciation = pronunciation,
                    partOfSpeech = partOfSpeech,
                    definitionVi = definitionVi,
                    exampleSentence = example,
                    note = note,
                    tags = tags
                )
            )
            if (response.isSuccessful) {
                val newCard = response.body()
                if (newCard != null) {
                    val deck = deckRepository.getDeck(deckId)
                    if (deck != null) {
                        deckRepository.updateRemoteDeckCards(deckId, deck.cards + newCard)
                    }
                }
                true
            } else {
                val errorBody = response.errorBody()?.string()
                println("API Error adding card: ${response.code()} - $errorBody")
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
