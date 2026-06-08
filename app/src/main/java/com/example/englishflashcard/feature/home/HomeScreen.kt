package com.example.englishflashcard.feature.home

import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.HorizontalDivider
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.englishflashcard.data.repository.UserRepository
import com.example.englishflashcard.data.repository.SrsRepository
import com.example.englishflashcard.data.repository.AnalyticsRepository
import com.example.englishflashcard.data.repository.DeckRepository
import com.example.englishflashcard.model.FlashcardSet

// Design System Tokens: Academic Emerald
private val EmeraldPrimary = Color(0xFF10B981)
private val EmeraldSurface = Color(0xFFF4FBF4)
private val EmeraldOnSurface = Color(0xFF1C1D1C)
private val EmeraldOnSurfaceVariant = Color(0xFF424942)
private val EmeraldOutline = Color(0xFFD4DCD5)
private val EmeraldSecondary = Color(0xFF064E3B)
private val EmeraldContainer = Color(0xFFEEF6EE)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    userRepository: UserRepository,
    deckRepository: DeckRepository,
    srsRepository: SrsRepository,
    analyticsRepository: AnalyticsRepository,
    onStudyFlashcard: () -> Unit,
    onStudyExercise: (String?) -> Unit,
    onCreateDeck: () -> Unit,
    onStudyDeck: (String) -> Unit,
    onDecks: () -> Unit,
    onStats: () -> Unit,
    onProfile: () -> Unit,
    onLogout: () -> Unit
) {
    val user = userRepository.currentUser

    Scaffold(
        topBar = {
            MobileTopAppBar(
                userName = user?.name ?: "bạn",
                onProfile = onProfile,
                onLogout = onLogout
            )
        },
        bottomBar = {
            MobileBottomNavBar(
                onHome = { /* Already on Home */ },
                onDecks = onDecks,
                onStats = onStats,
                onProfile = onProfile,
                selectedItem = 0
            )
        },
        containerColor = EmeraldSurface
    ) { padding ->
        HomeContent(
            userRepository = userRepository,
            deckRepository = deckRepository,
            srsRepository = srsRepository,
            analyticsRepository = analyticsRepository,
            onStudyFlashcard = onStudyFlashcard,
            onStudyExercise = onStudyExercise,
            onStudyDeck = onStudyDeck,
            modifier = Modifier.padding(padding)
        )
    }
}

@Composable
fun HomeContent(
    userRepository: UserRepository,
    deckRepository: DeckRepository,
    srsRepository: SrsRepository,
    analyticsRepository: AnalyticsRepository,
    onStudyFlashcard: () -> Unit,
    onStudyExercise: (String?) -> Unit,
    onStudyDeck: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val user = userRepository.currentUser
    val stats = analyticsRepository.stats
    val recommendedDeck = deckRepository.getAllDecks().firstOrNull { it.isPublic && it.createdBy?._id != user?._id }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.padding(vertical = 16.dp)) {
            Text(
                text = "Chào bạn, ${user?.name ?: "bạn"} 👋",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = EmeraldOnSurface
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.LocalFireDepartment,
                    contentDescription = null,
                    tint = Color(0xFFF59E0B),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${stats.streakDays} ngày streak",
                    color = Color(0xFFF59E0B),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }

        val rememberedToday = stats.correctAnswers
        val totalStudied = stats.totalAnswers
        DailyPlanCard(
            correctAnswers = rememberedToday,
            totalAnswers = totalStudied,
            onStudyNow = onStudyFlashcard
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (recommendedDeck != null) {
            SectionHeader(title = "Bộ từ vựng gợi ý")
            RecommendedVocabCard(
                deck = recommendedDeck,
                onClick = { onStudyDeck(recommendedDeck._id) }
            )
            Spacer(modifier = Modifier.height(24.dp))
        }

        SectionHeader(title = "Phương thức học nhanh", showMore = true)
        StudyModesList(
            onFlashcard = onStudyFlashcard,
            onSrsReview = { onStudyExercise("srs_review") }
        )

        Spacer(modifier = Modifier.height(32.dp))
    }
}
@Composable
fun MobileTopAppBar(
    userName: String,
    onProfile: () -> Unit,
    onLogout: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp),
        color = Color.White
        // Removed shadowElevation to match the flat look in the image
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Menu Icon
            IconButton(onClick = { /* Open Drawer */ }) {
                Icon(
                    imageVector = Icons.Filled.Menu,
                    contentDescription = "Menu",
                    tint = EmeraldOnSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Logo and Title
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.School,
                    contentDescription = null,
                    tint = EmeraldSecondary, // Darker green as in image
                    modifier = Modifier.size(28.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "EduLingo",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = EmeraldSecondary
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Profile
            Box {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(EmeraldContainer)
                        .clickable {
                            menuExpanded = true
                        },
                    contentAlignment = Alignment.Center
                ) {
                    // Try to simulate the image's profile pic with an icon if no image
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = null,
                        tint = EmeraldSecondary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = {
                        menuExpanded = false
                    }
                ) {
                    DropdownMenuItem(
                        text = { Text("Cài đặt tài khoản") },
                        leadingIcon = { Icon(Icons.Filled.Settings, contentDescription = null) },
                        onClick = {
                            menuExpanded = false
                            onProfile()
                        }
                    )
                    HorizontalDivider()
                    DropdownMenuItem(
                        text = { Text("Đăng xuất") },
                        leadingIcon = { Icon(Icons.Filled.Logout, contentDescription = null) },
                        onClick = {
                            menuExpanded = false
                            onLogout()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun DailyPlanCard(
    correctAnswers: Int,
    totalAnswers: Int,
    onStudyNow: () -> Unit
) {
    val progress = if (totalAnswers == 0) 0f else correctAnswers.toFloat() / totalAnswers.toFloat()

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = EmeraldSecondary),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                text = "KẾ HOẠCH HÔM NAY",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Từ đã nhớ / Đã học",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "$correctAnswers/$totalAnswers",
                        color = Color.White,
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Black
                    )
                }
                Icon(
                    imageVector = Icons.Filled.Star,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.1f),
                    modifier = Modifier.size(80.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = Color.White,
                trackColor = Color.White.copy(alpha = 0.2f)
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = onStudyNow,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    text = "HỌC NGAY",
                    color = EmeraldSecondary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        }
    }
}

@Composable
fun RecommendedVocabCard(deck: FlashcardSet, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F7FF)),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color(0xFFDCE9FE))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFDCE9FE)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Filled.Language,
                        contentDescription = null,
                        tint = Color(0xFF2563EB),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = deck.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E3A8A)
                )
                Text(
                    text = deck.description,
                    fontSize = 13.sp,
                    color = Color(0xFF3B82F6),
                    maxLines = 1
                )
            }

            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = Color(0xFF3B82F6),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun StudyModesList(
    onFlashcard: () -> Unit,
    onSrsReview: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        StudyModeItem(
            "Flashcard",
            "Thẻ ghi nhớ",
            Icons.Filled.ContentCopy,
            Color(0xFF10B981),
            onClick = onFlashcard
        )
        StudyModeItem(
            "Ôn tập",
            "Ôn tập những từ đã học",
            Icons.Filled.School,
            Color(0xFF8B5CF6),
            onClick = onSrsReview
        )
    }
}

@Composable
fun StudyModeItem(
    title: String,
    desc: String,
    icon: ImageVector,
    iconColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(44.dp),
                shape = RoundedCornerShape(10.dp),
                color = iconColor.copy(alpha = 0.1f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = EmeraldOnSurface
                )
                Text(
                    text = desc,
                    fontSize = 12.sp,
                    color = EmeraldOnSurfaceVariant
                )
            }

            Button(
                onClick = onClick,
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldSecondary),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 12.dp),
                modifier = Modifier.height(32.dp)
            ) {
                Text(
                    "Bắt đầu",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun SectionHeader(title: String, showMore: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = EmeraldOnSurface
        )
        if (showMore) {
            Text(
                text = "Xem tất cả",
                color = EmeraldPrimary,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                modifier = Modifier.clickable { }
            )
        }
    }
}

@Composable
fun MobileBottomNavBar(
    onHome: () -> Unit,
    onDecks: () -> Unit,
    onStats: () -> Unit,
    onProfile: () -> Unit,
    selectedItem: Int = 0
) {
    NavigationBar(
        containerColor = Color.White,
        tonalElevation = 8.dp
    ) {
        NavigationBarItem(
            icon = { Icon(Icons.Filled.School, contentDescription = null) },
            label = { Text("Học") },
            selected = selectedItem == 0,
            onClick = onHome,
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color.White,
                selectedTextColor = EmeraldPrimary,
                indicatorColor = EmeraldSecondary
            )
        )
        NavigationBarItem(
            icon = { Icon(Icons.Filled.Layers, contentDescription = null) },
            label = { Text("Bộ từ") },
            selected = selectedItem == 1,
            onClick = onDecks,
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color.White,
                selectedTextColor = EmeraldPrimary,
                indicatorColor = EmeraldSecondary
            )
        )
        NavigationBarItem(
            icon = { Icon(Icons.Filled.BarChart, contentDescription = null) },
            label = { Text("Thống kê") },
            selected = selectedItem == 2,
            onClick = onStats,
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color.White,
                selectedTextColor = EmeraldPrimary,
                indicatorColor = EmeraldSecondary
            )
        )
        NavigationBarItem(
            icon = { Icon(Icons.Filled.Person, contentDescription = null) },
            label = { Text("Cá nhân") },
            selected = selectedItem == 3,
            onClick = onProfile,
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color.White,
                selectedTextColor = EmeraldPrimary,
                indicatorColor = EmeraldSecondary
            )
        )
    }
}
