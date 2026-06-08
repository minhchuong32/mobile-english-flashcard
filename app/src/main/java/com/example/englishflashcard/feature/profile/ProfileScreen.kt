package com.example.englishflashcard.feature.profile

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.englishflashcard.data.repository.UserRepository
import com.example.englishflashcard.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    userRepository: UserRepository,
    onBack: () -> Unit
) {
    val viewModel = remember { ProfileViewModel(userRepository) }

    LaunchedEffect(Unit) {
        viewModel.fetchProfile()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Trang cá nhân", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        containerColor = EmeraldSurface
    ) { padding ->
        ProfileContent(
            userRepository = userRepository,
            viewModel = viewModel,
            onLogout = onBack,
            modifier = Modifier.padding(padding)
        )
    }
}

@Composable
fun ProfileContent(
    userRepository: UserRepository,
    viewModel: ProfileViewModel,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    if (viewModel.isLoading) {
        Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = EmeraldPrimary)
        }
    } else {
        Column(
            modifier = modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            HeroSection(viewModel)
            Spacer(Modifier.height(24.dp))
            ProfileFormSection(viewModel)
            Spacer(Modifier.height(24.dp))
            TextButton(
                onClick = { userRepository.logout(); onLogout() },
                colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)
            ) {
                Icon(Icons.Default.ExitToApp, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Đăng xuất tài khoản")
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
fun StatsContent(
    viewModel: ProfileViewModel,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    if (viewModel.isLoading) {
        Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = EmeraldPrimary)
        }
    } else {
        Column(
            modifier = modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            InsightsSection(viewModel)
            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
fun HeroSection(viewModel: ProfileViewModel) {
    val profile = viewModel.profileData?.profile
    val progress = 0.7f // Giả định tiến độ học tập trong ngày

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(120.dp)) {
            // Vòng tròn tiến độ
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawArc(
                    color = Color.LightGray.copy(alpha = 0.3f),
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                )
                drawArc(
                    color = EmeraldPrimary,
                    startAngle = -90f,
                    sweepAngle = 360f * progress,
                    useCenter = false,
                    style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                )
            }
            // Avatar Placeholder
            Surface(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape),
                color = EmeraldSecondary
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.padding(20.dp),
                    tint = Color.White
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        Text(
            text = profile?.fullName ?: "Chưa cập nhật",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(8.dp))

        Surface(
            color = EmeraldPrimary.copy(alpha = 0.1f),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = "Level ${profile?.level ?: "N/A"} • Goal: ${profile?.learningGoal ?: "N/A"}",
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                color = EmeraldSecondary,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun InsightsSection(viewModel: ProfileViewModel) {
    val streak = viewModel.profileData?.streak
    val progress = viewModel.profileData?.studyProgress

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatCard(
            modifier = Modifier.weight(1f),
            title = "Streak",
            value = "${streak?.currentStreak ?: 0}",
            unit = "Ngày",
            icon = Icons.Default.Whatshot,
            iconColor = Color(0xFFFF9800)
        )
        StatCard(
            modifier = Modifier.weight(1f),
            title = "Kho thẻ",
            value = "${streak?.totalCardsAllTime ?: 0}",
            unit = "Thẻ",
            icon = Icons.Default.AutoStories,
            iconColor = EmeraldPrimary
        )
        StatCard(
            modifier = Modifier.weight(1f),
            title = "Đã thuộc",
            value = "${progress?.totalMemorized ?: 0}",
            unit = "Thẻ",
            icon = Icons.Default.Bolt,
            iconColor = Color(0xFFFFD600)
        )
    }
}

@Composable
fun StatCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    unit: String,
    icon: ImageVector,
    iconColor: Color
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(24.dp))
            Spacer(Modifier.height(8.dp))
            Text(text = value, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
            Text(text = unit, fontSize = 12.sp, color = Color.Gray)
            Spacer(Modifier.height(4.dp))
            Text(text = title, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = Color.Gray)
        }
    }
}

@Composable
fun ProfileFormSection(viewModel: ProfileViewModel) {
    val scope = rememberCoroutineScope()
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text("Thông tin cá nhân", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            
            Spacer(Modifier.height(20.dp))

            ProfileField(
                label = "Họ và tên",
                value = viewModel.fullName,
                onValueChange = { viewModel.fullName = it },
                icon = Icons.Default.Badge
            )

            ProfileField(
                label = "Email",
                value = viewModel.profileData?.email ?: "",
                onValueChange = {},
                icon = Icons.Default.Email,
                readOnly = true
            )

            ProfileField(
                label = "Số điện thoại",
                value = viewModel.phoneNumber,
                onValueChange = { viewModel.phoneNumber = it },
                icon = Icons.Default.Phone
            )

            Text(
                text = "Giới thiệu bản thân",
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            OutlinedTextField(
                value = viewModel.bio,
                onValueChange = { viewModel.bio = it },
                modifier = Modifier.fillMaxWidth().height(100.dp),
                placeholder = { Text("Viết gì đó về mục tiêu của bạn...") },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = EmeraldPrimary,
                    unfocusedBorderColor = Color.LightGray.copy(alpha = 0.5f)
                )
            )

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = { scope.launch { viewModel.updateProfile() } },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled = viewModel.hasChanges() && !viewModel.isSaving,
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldSecondary),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (viewModel.isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                } else {
                    Text("Lưu thay đổi", fontWeight = FontWeight.Bold)
                }
            }
            
            if (viewModel.message.isNotBlank()) {
                Text(
                    text = viewModel.message,
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    textAlign = TextAlign.Center,
                    color = if (viewModel.message.contains("thành công")) EmeraldPrimary else Color.Red,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
fun ProfileField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    icon: ImageVector,
    readOnly: Boolean = false
) {
    Column(modifier = Modifier.padding(bottom = 20.dp)) {
        Text(text = label, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, modifier = Modifier.padding(bottom = 8.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(icon, contentDescription = null, tint = EmeraldPrimary) },
            trailingIcon = if (readOnly) {
                { Icon(Icons.Default.Lock, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(18.dp)) }
            } else null,
            readOnly = readOnly,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = EmeraldPrimary,
                unfocusedBorderColor = Color.LightGray.copy(alpha = 0.5f),
                disabledContainerColor = Color.Gray.copy(alpha = 0.1f)
            ),
            singleLine = true
        )
    }
}
