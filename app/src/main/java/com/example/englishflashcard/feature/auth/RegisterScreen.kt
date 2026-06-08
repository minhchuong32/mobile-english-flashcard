package com.example.englishflashcard.feature.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.School
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

@Composable
fun RegisterScreen(
    viewModel: AuthViewModel,
    onGoLogin: () -> Unit,
    onRegisterSuccess: () -> Unit
) {
    // Design System Tokens: Academic Emerald
    val EmeraldPrimary = Color(0xFF10B981)
    val EmeraldSurface = Color(0xFFF4FBF4)
    val EmeraldOnSurface = Color(0xFF1C1D1C)
    val EmeraldOnSurfaceVariant = Color(0xFF424942)
    val EmeraldOutline = Color(0xFFD4DCD5)
    val EmeraldSecondary = Color(0xFF064E3B)

    var passwordVisible by remember { mutableStateOf(false) }
    var termsAgreed by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val isLoading = viewModel.isLoading
    val isOtpSent = viewModel.isOtpSent
    val messageColor = if (viewModel.isError) Color.Red else EmeraldSecondary

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
            // Header Logo
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

            // Main Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(40.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Đăng ký tài khoản",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = EmeraldOnSurface
                    )
                    Text(
                        text = "Tham gia cùng hàng ngàn học viên ngay hôm nay",
                        color = EmeraldOnSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp, bottom = 32.dp),
                        textAlign = TextAlign.Center
                    )

                    if (!isOtpSent) {
                        // Registration Form
                        FormField(
                            label = "Họ và tên",
                            value = viewModel.name,
                            onValueChange = { viewModel.name = it },
                            placeholder = "Nguyễn Văn A",
                            leadingIcon = Icons.Outlined.Person
                        )

                        FormField(
                            label = "Địa chỉ Email",
                            value = viewModel.email,
                            onValueChange = { viewModel.email = it },
                            placeholder = "example@lexicon.edu.vn",
                            leadingIcon = Icons.Outlined.Email,
                            keyboardType = KeyboardType.Email
                        )

                        FormField(
                            label = "Mật khẩu",
                            value = viewModel.password,
                            onValueChange = { viewModel.password = it },
                            placeholder = "••••••••",
                            leadingIcon = Icons.Outlined.Lock,
                            isPassword = true,
                            passwordVisible = passwordVisible,
                            onTogglePassword = { passwordVisible = !passwordVisible }
                        )

                        Row(
                            verticalAlignment = Alignment.Top,
                            modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp)
                        ) {
                            Checkbox(
                                checked = termsAgreed,
                                onCheckedChange = { termsAgreed = it },
                                colors = CheckboxDefaults.colors(checkedColor = EmeraldPrimary)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Tôi đồng ý với Điều khoản dịch vụ và Chính sách bảo mật.",
                                fontSize = 14.sp,
                                color = EmeraldOnSurfaceVariant,
                                lineHeight = 20.sp
                            )
                        }

                        Button(
                            onClick = {
                                scope.launch {
                                    viewModel.registerRemote()
                                }
                            },
                            enabled = !isLoading && termsAgreed,
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldSecondary),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(
                                    text = "Đăng ký",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = Color.White
                                )
                            }
                        }
                    } else {
                        // OTP Verification Section
                        Text(
                            text = "Xác thực tài khoản",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldOnSurface,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        Text(
                            text = "Mã OTP đã được gửi đến ${viewModel.email}",
                            fontSize = 14.sp,
                            color = EmeraldOnSurfaceVariant,
                            modifier = Modifier.padding(bottom = 24.dp),
                            textAlign = TextAlign.Center
                        )

                        FormField(
                            label = "Mã OTP (6 chữ số)",
                            value = viewModel.otp,
                            onValueChange = {
                                if (it.length <= 6 && it.all { c -> c.isDigit() }) {
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
                                        onRegisterSuccess()
                                    }
                                }
                            },
                            enabled = !isLoading,
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldSecondary),
                            shape = RoundedCornerShape(12.dp)
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
                            onClick = {
                                scope.launch {
                                    viewModel.registerRemote()
                                }
                            },
                            modifier = Modifier.padding(top = 12.dp)
                        ) {
                            Text(
                                text = "Gửi lại mã OTP",
                                color = EmeraldPrimary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    // optional message from viewModel
                    if (viewModel.message.isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = viewModel.message,
                            color = messageColor,
                            fontSize = 12.sp
                        )
                    }

                    TextButton(
                        onClick = onGoLogin,
                        modifier = Modifier.padding(top = 16.dp)
                    ) {
                        Text(
                            text = "Đã có tài khoản? Đăng nhập",
                            color = EmeraldSecondary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Footer
            Text(
                text = "© 2026 EduLingo. Phát triển bởi tình yêu ngôn ngữ.",
                modifier = Modifier.padding(top = 48.dp),
                color = EmeraldOnSurfaceVariant.copy(alpha = 0.6f),
                fontSize = 12.sp
            )
        }
    }
}

@Composable
fun FormField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    leadingIcon: ImageVector,
    isPassword: Boolean = false,
    passwordVisible: Boolean = false,
    onTogglePassword: () -> Unit = {},
    keyboardType: KeyboardType = KeyboardType.Text
) {
    val EmeraldPrimary = Color(0xFF10B981)
    val EmeraldOutline = Color(0xFFD4DCD5)
    val EmeraldOnSurfaceVariant = Color(0xFF424942)

    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp)) {
        Text(
            text = label,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(placeholder, color = EmeraldOutline) },
            leadingIcon = { Icon(leadingIcon, contentDescription = null, tint = EmeraldOnSurfaceVariant) },
            trailingIcon = if (isPassword) {
                {
                    IconButton(onClick = onTogglePassword) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                            contentDescription = null
                        )
                    }
                }
            } else null,
            visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = EmeraldPrimary,
                unfocusedBorderColor = EmeraldOutline,
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black
            )
        )
    }
}
