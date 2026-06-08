package com.example.englishflashcard.model
data class CardSrsState(
    val cardId: String,
    val rating: String,
    val lastStudiedTimeMillis: Long,
    val remembered: Boolean
)
data class Card(
    val _id: String = "",
    val setId: String = "",
    val term: String = "",
    val pronunciation: String = "",
    val partOfSpeech: String = "",
    val definitionVi: String = "",
    val exampleSentence: String = "",
    val exampleSentenceVi: String = "",
    val collocations: List<String> = emptyList(),
    val relatedWords: List<String> = emptyList(),
    val note: String = "",
    val imageUrl: String = "",
    val audioUrl: String = "",
    val masteryStatus: String = "learning", // "mastered" or "learning"
    val tags: List<String> = emptyList(),
    val orderIndex: Int = 0,
    val isActive: Boolean = true
)
