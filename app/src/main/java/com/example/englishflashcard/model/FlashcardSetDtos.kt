package com.example.englishflashcard.model

data class CreateDeckRequest(
    val title: String,
    val description: String,
    val isPublic: Boolean = true,
    val tags: List<String> = emptyList()
)

data class CreateCardRequest(
    val flashcardSetId: String,
    val term: String,
    val pronunciation: String,
    val partOfSpeech: String = "",
    val definitionVi: String,
    val exampleSentence: String,
    val note: String,
    val tags: List<String>
)

data class DeckResponse(
    val sets: List<FlashcardSet>,
    val pagination: PaginationInfo
)

data class PaginationInfo(
    val totalItems: Int,
    val totalPages: Int,
    val currentPage: Int,
    val limit: Int
)

data class CardResponse(
    val cards: List<Card>,
    val pagination: PaginationInfo
)
