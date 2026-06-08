package com.example.englishflashcard.feature.profile

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.englishflashcard.data.repository.UserRepository
import com.example.englishflashcard.model.ProfileData

class ProfileViewModel(
    private val userRepository: UserRepository
) {
    var profileData by mutableStateOf<ProfileData?>(null)
    var isLoading by mutableStateOf(false)
    var isSaving by mutableStateOf(false)
    var message by mutableStateOf("")

    // Editable fields
    var fullName by mutableStateOf("")
    var bio by mutableStateOf("")
    var phoneNumber by mutableStateOf("")

    suspend fun fetchProfile() {
        isLoading = true
        val response = userRepository.getProfileRemote()
        isLoading = false
        if (response != null) {
            profileData = response.data
            fullName = response.data.profile.fullName
            bio = response.data.profile.bio
            phoneNumber = response.data.profile.phoneNumber
        } else {
            message = "Không thể tải thông tin profile"
        }
    }

    suspend fun updateProfile() {
        isSaving = true
        val response = userRepository.updateProfileRemote(fullName, bio, phoneNumber)
        isSaving = false
        if (response != null) {
            profileData = response.data
            message = "Cập nhật thành công"
        } else {
            message = "Cập nhật thất bại"
        }
    }

    fun hasChanges(): Boolean {
        val data = profileData?.profile ?: return false
        return fullName != data.fullName || bio != data.bio || phoneNumber != data.phoneNumber
    }
}
