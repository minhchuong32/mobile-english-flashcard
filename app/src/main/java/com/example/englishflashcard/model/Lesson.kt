package com.example.englishflashcard.model

data class Lesson(
    val _id: String = "",
    val courseId: String = "",
    val title: String = "",
    val description: String = "",
    val orderIndex: Int = 0,
    val type: String = "video", // "video", "flashcard", "quiz", "reading", "mixed"
    val videoUrl: String = "",
    val videoDuration: Int = 0,
    val flashcardSetId: String? = null,
    val attachments: List<Attachment> = emptyList(),
    val isFree: Boolean = false,
    val isActive: Boolean = true
)

data class Attachment(
    val name: String = "",
    val url: String = ""
)
