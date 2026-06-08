package com.example.englishflashcard.model
data class LoginRequest(
    val identifier: String,
    val password: String
)

data class LoginResponse(
    val message: String,
    val redirect_url: String? = null
)

data class RegisterResponse(
    val message: String,
)

data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String,
)

data class VerifyOtpRequest(
    val email: String,
    val otpCode: String
)

data class VerifyOtpResult(
    val success: Boolean,
    val message: String,
    val resetToken: String? = null
)
data class VerifyOtpResponse(
    val message: String,
    val resetToken: String? = null
)


data class ForgotPasswordRequest(
    val email: String
)

data class ForgotPasswordResponse(
    val message: String
)

data class ResetPasswordRequest(
    val resetToken: String,
    val newPassword: String
)

data class ResetPasswordResponse(
    val message: String
)