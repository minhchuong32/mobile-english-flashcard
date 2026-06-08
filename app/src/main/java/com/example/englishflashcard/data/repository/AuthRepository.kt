package com.example.englishflashcard.data.repository

import com.example.englishflashcard.data.api.AuthApiService
import com.example.englishflashcard.model.*

class AuthRepository(
    private val apiService: AuthApiService
) {
    suspend fun login(identifier: String, password: String): Pair<Boolean, String> {
        return try {
            val response = apiService.login(
                LoginRequest(
                    identifier = identifier.trim(),
                    password = password
                )
            )

            if (response.isSuccessful) {
                true to (response.body()?.message ?: "Đăng nhập thành công")
            } else {
                false to (response.errorBody()?.string().orEmpty().ifBlank {
                    "HTTP ${response.code()} - Đăng nhập thất bại"
                })
            }
        } catch (e: Exception) {
            false to "${e.javaClass.simpleName}: ${e.message}"
        }
    }

    suspend fun register(username: String, email: String, password: String): Pair<Boolean, String> {
        return try {
            val response = apiService.register(
                RegisterRequest(
                    username = username.trim(),
                    email = email.trim(),
                    password = password
                )
            )

            if (response.isSuccessful) {
                true to (response.body()?.message ?: "Đăng ký thành công, vui lòng kiểm tra OTP")
            } else {
                false to (response.errorBody()?.string().orEmpty().ifBlank {
                    "HTTP ${response.code()} - Đăng ký thất bại"
                })
            }
        } catch (e: Exception) {
            false to "${e.javaClass.simpleName}: ${e.message}"
        }
    }

    suspend fun verifyOtp(
        email: String,
        otp: String
    ): VerifyOtpResult {

        return try {

            val response = apiService.verifyOtp(
                VerifyOtpRequest(
                    email = email,
                    otpCode = otp
                )
            )

            if (response.isSuccessful) {

                VerifyOtpResult(
                    success = true,
                    message = response.body()?.message
                        ?: "OTP hợp lệ",
                    resetToken = response.body()?.resetToken
                )

            } else {

                VerifyOtpResult(
                    success = false,
                    message = response.errorBody()?.string()
                        ?: "Xác thực OTP thất bại"
                )
            }

        } catch (e: Exception) {

            VerifyOtpResult(
                success = false,
                message = e.message ?: "Có lỗi xảy ra"
            )
        }
    }

    suspend fun forgotPassword(email: String): Pair<Boolean, String> {
        return try {

            val response = apiService.forgotPassword(
                ForgotPasswordRequest(email.trim())
            )

            if (response.isSuccessful) {
                true to (response.body()?.message
                    ?: "OTP đã được gửi")
            } else {
                false to (response.errorBody()?.string().orEmpty())
            }

        } catch (e: Exception) {
            false to (e.message ?: "Có lỗi xảy ra")
        }
    }

    suspend fun resetPassword(
        resetToken: String,
        newPassword: String
    ): Pair<Boolean, String> {
        return try {

            val response = apiService.resetPassword(
                ResetPasswordRequest(
                    resetToken = resetToken,
                    newPassword = newPassword
                )
            )

            if (response.isSuccessful) {
                true to (response.body()?.message
                    ?: "Đặt lại mật khẩu thành công")
            } else {
                false to (response.errorBody()?.string().orEmpty())
            }

        } catch (e: Exception) {
            false to (e.message ?: "Có lỗi xảy ra")
        }
    }
}
