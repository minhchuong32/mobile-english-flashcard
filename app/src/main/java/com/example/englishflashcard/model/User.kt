package com.example.englishflashcard.model

data class User(
    val _id: String = "",
    val username: String = "",
    val email: String = "",
    val password: String = "",
    val googleId: String? = null,
    val profile: UserProfile = UserProfile(),
    val isVerified: Boolean = false,
    val role: String = "user", // "user", "teacher", "manager", "admin"
    val teacherProfile: TeacherProfile? = null,
    // Keep name for backward compatibility in local mock setup
    val name: String = ""
)

data class UserProfile(
    val fullName: String = "",
    val avatarUrl: String = "default-avatar.png",
    val bio: String = "",
    val phoneNumber: String = "",
    val learningGoal: String = "",
    val level: String = ""
)

data class TeacherProfile(
    val bio: String = "",
    val expertise: List<String> = emptyList(),
    val bankAccount: String = "",
    val bankName: String = "",
    val revenueShare: Double = 0.7,
    val isApproved: Boolean = false
)

data class ProfileResponse(
    val message: String,
    val data: ProfileData
)

data class UpdateProfileRequest(
    val fullName: String,
    val bio: String,
    val phoneNumber: String
)


// Profile response structures
data class ProfileData(
    val userId: String,
    val username: String,
    val email: String,
    val role: String,
    val profile: UserProfile,
    val streak: UserStreak,
    val studyProgress: StudyProgress,
    val createdAt: String,
    val updatedAt: String
)

data class FcmTokenRequest(
    val userId: String,
    val token: String
)

data class FcmTokenResponse(
    val message: String
)
