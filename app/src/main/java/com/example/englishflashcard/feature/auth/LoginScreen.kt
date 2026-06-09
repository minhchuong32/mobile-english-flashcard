package com.example.englishflashcard.feature.auth


import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

// Design System Tokens: Academic Emerald (EduLingo)
private val EmeraldPrimary = Color(0xFF10B981)
private val EmeraldSurface = Color(0xFFF4FBF4)
private val EmeraldOnSurface = Color(0xFF1C1D1C)
private val EmeraldOnSurfaceVariant = Color(0xFF424942)
private val EmeraldOutline = Color(0xFFD4DCD5)
private val EmeraldSecondary = Color(0xFF064E3B)

@Composable
fun LoginScreen(
    viewModel: AuthViewModel? = null,
    onGoRegister: () -> Unit = {},
    onLoginSuccess: () -> Unit = {},
    onForgotPasswordClick: () -> Unit = {}
) {
    // State local cho ô "email/tên đăng nhập" khi không truyền ViewModel (ví dụ @Preview).
// remember giúp giữ giá trị qua các lần recomposition.
    var localIdentifier by remember { mutableStateOf("") }

// State local cho ô mật khẩu (fallback khi không có ViewModel).
    var localPassword by remember { mutableStateOf("") }

// State local để bật/tắt hiện mật khẩu (icon con mắt).
    var passwordVisible by remember { mutableStateOf(false) }

// Giá trị hiển thị cho ô identifier:
// - nếu có viewModel -> dùng state trong viewModel
// - nếu không có -> dùng state local
    val identifier = viewModel?.identifier ?: localIdentifier

// Tương tự cho mật khẩu: ưu tiên dữ liệu trong viewModel.
    val password = viewModel?.password ?: localPassword

// Thông báo trả về từ viewModel; nếu null thì dùng chuỗi rỗng để tránh crash/null UI.
    val message = viewModel?.message.orEmpty()

// Màu thông báo:
// - lỗi -> đỏ
// - không lỗi -> màu xanh chủ đề
    val messageColor = if (viewModel?.isError == true) Color.Red else EmeraldSecondary

// Trạng thái loading của nút đăng nhập; mặc định false khi không có viewModel.
    val isLoading = viewModel?.isLoading ?: false

// Tạo CoroutineScope gắn với composable hiện tại,
// dùng để gọi hàm suspend (ví dụ login()) trong onClick.
    val scope = rememberCoroutineScope()


    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(EmeraldSurface),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 480.dp)
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
                    tint = EmeraldSecondary,
                    modifier = Modifier.size(32.dp)
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
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(32.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(40.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Chào mừng trở lại",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldOnSurface
                    )
                    Text(
                        text = "Tiếp tục hành trình chinh phục ngôn ngữ của bạn",
                        color = EmeraldOnSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp, bottom = 32.dp),
                        textAlign = TextAlign.Center
                    )

                    LoginField(
                        label = "Email hoặc tên đăng nhập",
                        value = identifier,
                        onValueChange = {
                            if (viewModel != null) viewModel.identifier = it else localIdentifier = it
                        },
                        placeholder = "testuser@example.com",
                        leadingIcon = Icons.Outlined.Email,
                        keyboardType = KeyboardType.Text
                    )

                    LoginField(
                        label = "Mật khẩu",
                        value = password,
                        onValueChange = {
                            if (viewModel != null) viewModel.password = it else localPassword = it
                        },
                        placeholder = "••••••••",
                        leadingIcon = Icons.Outlined.Lock,
                        isPassword = true,
                        passwordVisible = passwordVisible,
                        onTogglePassword = { passwordVisible = !passwordVisible }
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Text(
                            text = "Quên mật khẩu?",
                            color = EmeraldSecondary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            modifier = Modifier
                                .padding(bottom = 24.dp)
                                .clickable { onForgotPasswordClick() }
                        )
                    }

                    Button(
                        onClick = {
                            scope.launch {
                                if (viewModel?.login() == true) onLoginSuccess()
                            }
                        },
                        enabled = !isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
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
                                text = "Đăng nhập",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color.White
                            )
                        }
                    }



                    Row(
                        modifier = Modifier.padding(top = 40.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Chưa có tài khoản? ", color = EmeraldOnSurfaceVariant)
                        Text(
                            text = "Đăng ký ngay",
                            color = EmeraldSecondary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable { onGoRegister() }
                        )
                    }

                    if (message.isNotBlank()) {
                        Text(
                            text = message,
                            color = messageColor,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 16.dp)
                        )
                    }
                }
            }

            Text(
                text = "© 2026 EduLingo. Phát triển bởi tình yêu ngôn ngữ.",
                modifier = Modifier.padding(top = 48.dp),
                color = EmeraldOnSurfaceVariant.copy(alpha = 0.5f),
                fontSize = 12.sp
            )
        }
    }
}

@Composable
fun LoginField(
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
    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp)) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(label) },
            placeholder = {
                Text(
                    placeholder,
                    color = EmeraldOnSurfaceVariant.copy(alpha = 0.4f)
                )
            },
            leadingIcon = {
                Icon(
                    leadingIcon,
                    contentDescription = null,
                    tint = EmeraldOnSurfaceVariant
                )
            },
            trailingIcon = if (isPassword) {
                {
                    IconButton(onClick = onTogglePassword) {
                        Icon(
                            imageVector = if (passwordVisible) {
                                Icons.Filled.Visibility
                            } else {
                                Icons.Filled.VisibilityOff
                            },
                            contentDescription = null,
                            tint = EmeraldOnSurfaceVariant
                        )
                    }
                }
            } else null,
            visualTransformation = if (isPassword && !passwordVisible) {
                PasswordVisualTransformation()
            } else {
                VisualTransformation.None
            },
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = EmeraldPrimary,
                unfocusedBorderColor = EmeraldOutline,
                focusedLabelColor = EmeraldPrimary,
                unfocusedLabelColor = EmeraldOnSurfaceVariant,
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black
            )
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun LoginScreenPreview() {
    MaterialTheme {
        LoginScreen()
    }
}
