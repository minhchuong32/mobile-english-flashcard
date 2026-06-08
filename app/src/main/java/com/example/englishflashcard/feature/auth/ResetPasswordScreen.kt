package com.example.englishflashcard.feature.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch


private val EmeraldPrimary = Color(0xFF10B981)
private val EmeraldSurface = Color(0xFFF4FBF4)
private val EmeraldOnSurface = Color(0xFF1C1D1C)
private val EmeraldOnSurfaceVariant = Color(0xFF424942)
private val EmeraldOutline = Color(0xFFD4DCD5)
private val EmeraldSecondary = Color(0xFF064E3B)
@Composable
fun ResetPasswordScreen(
    viewModel: AuthViewModel,
    onSuccess: () -> Unit
) {

    var passwordVisible by remember {
        mutableStateOf(false)
    }

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

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 40.dp)
            ) {

                Icon(
                    imageVector = Icons.Filled.School,
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
                shape = RoundedCornerShape(24.dp)
            ) {

                Column(
                    modifier = Modifier.padding(40.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Text(
                        text = "Đặt lại mật khẩu",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = EmeraldOnSurface
                    )

                    Text(
                        text = "Nhập mật khẩu mới cho tài khoản của bạn",
                        color = EmeraldOnSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(
                            top = 8.dp,
                            bottom = 32.dp
                        )
                    )

                    FormField(
                        label = "Mật khẩu mới",
                        value = viewModel.newPassword,
                        onValueChange = {
                            viewModel.newPassword = it
                        },
                        placeholder = "••••••••",
                        leadingIcon = Icons.Outlined.Lock,
                        isPassword = true,
                        passwordVisible = passwordVisible,
                        onTogglePassword = {
                            passwordVisible = !passwordVisible
                        }
                    )

                    Button(
                        onClick = {

                            scope.launch {

                                if (viewModel.resetPassword()) {
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
                                color = Color.White
                            )

                        } else {

                            Text(
                                "Đặt lại mật khẩu",
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    if (viewModel.message.isNotBlank()) {

                        Text(
                            text = viewModel.message,
                            color = messageColor,
                            modifier = Modifier.padding(top = 16.dp)
                        )
                    }
                }
            }
        }
    }
}