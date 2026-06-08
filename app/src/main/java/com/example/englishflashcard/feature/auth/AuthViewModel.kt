package com.example.englishflashcard.feature.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.englishflashcard.data.repository.AuthRepository
import com.example.englishflashcard.data.repository.DeckRepository

class AuthViewModel(
    private val deckRepository: DeckRepository,
    private val authRepository: AuthRepository
) {
    var name by mutableStateOf("")
    var email by mutableStateOf("")
    var identifier by mutableStateOf("")
    var password by mutableStateOf("")
    var otp by mutableStateOf("")
    var isOtpSent by mutableStateOf(false)

    var message by mutableStateOf("")
    var isLoading by mutableStateOf(false)
    var isError by mutableStateOf(false)
    var resetToken by mutableStateOf("")
    var newPassword by mutableStateOf("")

    suspend fun login(): Boolean {
        val normalizedIdentifier = identifier.trim()
        val normalizedPassword = password.trim()

        if (normalizedIdentifier.isBlank()) {
            message = "Vui lòng nhập email hoặc tên đăng nhập"
            isError = true
            return false
        }

        if (normalizedPassword.isBlank()) {
            message = "Vui lòng nhập mật khẩu"
            isError = true
            return false
        }

        isLoading = true
        message = ""
        isError = false

        return try {
            val (success, result) = authRepository.login(
                identifier = normalizedIdentifier,
                password = normalizedPassword
            )

            if (success) {
                runCatching { deckRepository.fetchDecksRemote() }
                isOtpSent = false
            }

            message = result
            isError = !success
            success
        } finally {
            isLoading = false
        }
    }

    suspend fun registerRemote(): Boolean {
        val normalizedName = name.trim()
        val normalizedEmail = email.trim()
        val normalizedPassword = password.trim()

        if (normalizedName.isBlank()) {
            message = "Vui lòng nhập tên đăng nhập"
            isError = true
            return false
        }

        if (normalizedEmail.isBlank()) {
            message = "Vui lòng nhập email"
            isError = true
            return false
        }

        if (normalizedPassword.isBlank()) {
            message = "Vui lòng nhập mật khẩu"
            isError = true
            return false
        }

        isLoading = true
        message = ""
        isError = false

        return try {
            val (success, result) = authRepository.register(
                username = normalizedName,
                email = normalizedEmail,
                password = normalizedPassword
            )

            message = result
            isError = !success

            if (success) {
                isOtpSent = true
                otp = ""
            }

            success
        } finally {
            isLoading = false
        }
    }

    suspend fun verifyOtp(): Boolean {

        if (otp.length != 6) {
            message = "OTP không hợp lệ"
            isError = true
            return false
        }

        isLoading = true

        return try {

            val result =
                authRepository.verifyOtp(
                    email,
                    otp
                )

            message = result.message
            isError = !result.success

            result.resetToken?.let {
                resetToken = it
            }

            result.success

        } finally {
            isLoading = false
        }
    }
    suspend fun forgotPassword(): Boolean {

        if (email.isBlank()) {
            message = "Vui lòng nhập email"
            isError = true
            return false
        }

        isLoading = true

        return try {

            val (success, result) =
                authRepository.forgotPassword(email)

            message = result
            isError = !success

            success

        } finally {
            isLoading = false
        }
    }

    suspend fun resetPassword(): Boolean {

        if (newPassword.length < 6) {
            message = "Mật khẩu phải từ 6 ký tự"
            isError = true
            return false
        }

        isLoading = true

        return try {

            val (success, result) =
                authRepository.resetPassword(
                    resetToken,
                    newPassword
                )

            message = result
            isError = !success

            success

        } finally {
            isLoading = false
        }
    }
}