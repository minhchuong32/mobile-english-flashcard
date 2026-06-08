package com.example.englishflashcard.feature.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.School
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

@Composable
fun VerifyForgotOtpScreen(
    viewModel: AuthViewModel,
    onSuccess: () -> Unit,
    onBack: () -> Unit
) {

    val EmeraldPrimary = Color(0xFF10B981)
    val EmeraldSurface = Color(0xFFF4FBF4)
    val EmeraldOnSurface = Color(0xFF1C1D1C)
    val EmeraldOnSurfaceVariant = Color(0xFF424942)
    val EmeraldSecondary = Color(0xFF064E3B)

    val scope = rememberCoroutineScope()
    val isLoading = viewModel.isLoading
    val messageColor =
        if (viewModel.isError) Color.Red
        else EmeraldSecondary

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(EmeraldSurface),
        contentAlignment = Alignment.Center
    ) {

        Column(
            modifier = Modifier
                .widthIn(max = 520.dp)
                .verticalScroll(rememberScrollState())
                .padding(vertical = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // Logo
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 40.dp)
            ) {

                Icon(
                    imageVector = Icons.Outlined.School,
                    contentDescription = null,
                    tint = EmeraldPrimary,
                    modifier = Modifier.size(36.dp)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = "EduLingo",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = EmeraldSecondary
                )
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 2.dp
                )
            ) {

                Column(
                    modifier = Modifier.padding(40.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Text(
                        text = "Xác thực OTP",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = EmeraldOnSurface
                    )

                    Text(
                        text = "Nhập mã OTP đã gửi tới email của bạn",
                        color = EmeraldOnSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(
                            top = 8.dp,
                            bottom = 12.dp
                        )
                    )

                    Text(
                        text = viewModel.email,
                        color = EmeraldPrimary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 28.dp)
                    )

                    FormField(
                        label = "Mã OTP (6 chữ số)",
                        value = viewModel.otp,
                        onValueChange = {
                            if (
                                it.length <= 6 &&
                                it.all(Char::isDigit)
                            ) {
                                viewModel.otp = it
                            }
                        },
                        placeholder = "123456",
                        leadingIcon = Icons.Outlined.Lock,
                        keyboardType = KeyboardType.Number
                    )

                    Button(
                        onClick = {

                            scope.launch {

                                if (viewModel.verifyOtp()) {
                                    onSuccess()
                                }

                            }

                        },
                        enabled = !isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = EmeraldSecondary
                        )
                    ) {

                        if (isLoading) {

                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )

                        } else {

                            Text(
                                text = "Xác thực OTP",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color.White
                            )
                        }
                    }

                    TextButton(
                        onClick = onBack,
                        modifier = Modifier.padding(top = 12.dp)
                    ) {

                        Text(
                            text = "Quay lại",
                            color = EmeraldPrimary
                        )
                    }

                    if (viewModel.message.isNotBlank()) {

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = viewModel.message,
                            color = messageColor,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Text(
                text = "© 2026 EduLingo. Phát triển bởi tình yêu ngôn ngữ.",
                modifier = Modifier.padding(top = 48.dp),
                color = EmeraldOnSurfaceVariant.copy(alpha = 0.6f),
                fontSize = 12.sp
            )
        }
    }
}