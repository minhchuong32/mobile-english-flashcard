package com.example.englishflashcard.model

data class CreatedByProfile(
    val fullName: String = "",
    val avatarUrl: String = "",
    val bio: String = ""
)

data class CreatedByInfo(
    val _id: String = "",
    val username: String = "",
    val email: String = "",
    val profile: CreatedByProfile = CreatedByProfile()
)

data class FlashcardSet(
    val _id: String = "",
    val createdBy: CreatedByInfo = CreatedByInfo(), // ← object, không phải String
    val title: String = "",
    val description: String = "",
    val thumbnailUrl: String = "",
    val tags: List<String> = emptyList(),
    val isPublic: Boolean = true,
    val totalCards: Int = 0,
    val enrollCount: Int = 0,
    val cards: List<Card> = emptyList()
)

data class FlashcardSetsResponse(
    val sets: List<FlashcardSet> = emptyList()
)
