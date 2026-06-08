package com.example.englishflashcard.feature.main

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.englishflashcard.data.repository.UserRepository
import com.example.englishflashcard.data.repository.DeckRepository
import com.example.englishflashcard.data.repository.AnalyticsRepository
import com.example.englishflashcard.data.repository.SrsRepository
import com.example.englishflashcard.feature.deck.DeckExplorerContent
import com.example.englishflashcard.feature.home.HomeContent
import com.example.englishflashcard.feature.profile.ProfileContent
import com.example.englishflashcard.feature.profile.ProfileViewModel
import com.example.englishflashcard.feature.analytic.AnalyticsScreen
import com.example.englishflashcard.feature.analytic.AnalyticsViewModel
import com.example.englishflashcard.ui.theme.EmeraldPrimary
import com.example.englishflashcard.ui.theme.EmeraldSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainShellScreen(
    userRepository: UserRepository,
    deckRepository: DeckRepository,
    analyticsRepository: AnalyticsRepository,
    srsRepository: SrsRepository,
    onStudyFlashcard: () -> Unit,
    onStudyExercise: (String?) -> Unit,
    onStudyDeck: (String) -> Unit,
    onDeckDetail: (String) -> Unit,
    onCreateDeck: () -> Unit,
    onLogout: () -> Unit,
    initialTab: Int = 0
) {
    var selectedTab by rememberSaveable { mutableIntStateOf(initialTab) }
    val user = userRepository.currentUser

    val profileViewModel = remember { ProfileViewModel(userRepository) }
    val analyticsViewModel = remember { AnalyticsViewModel(analyticsRepository) }

    LaunchedEffect(Unit) {
        userRepository.getProfileRemote()
        deckRepository.fetchDecksRemote()
        analyticsRepository.fetchAnalyticsRemote()
    }

    LaunchedEffect(selectedTab) {
        if (selectedTab == 2) {
            analyticsViewModel.fetchAnalytics()
        }
        if (selectedTab == 3) {
            profileViewModel.fetchProfile()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Filled.School,
                            contentDescription = null,
                            tint = Color(0xFF275A42),
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "EduLingo",
                            color = Color(0xFF275A42),
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { /* Handle hamburger menu if needed */ }) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu", tint = Color(0xFF275A42))
                    }
                },
                actions = {
                    Box(
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE2E8F0))
                            .clickable { selectedTab = 3 },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = user?.name?.firstOrNull()?.uppercase() ?: "U",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    icon = {
                        Icon(
                            imageVector = if (selectedTab == 0) Icons.Filled.School else Icons.Outlined.School,
                            contentDescription = null
                        )
                    },
                    label = { Text("Học") },
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF275A42),
                        selectedTextColor = Color(0xFF275A42),
                        indicatorColor = Color(0xFFEEF6EE)
                    )
                )
                NavigationBarItem(
                    icon = {
                        Icon(
                            imageVector = if (selectedTab == 1) Icons.Filled.MenuBook else Icons.Outlined.MenuBook,
                            contentDescription = null
                        )
                    },
                    label = { Text("Bộ từ") },
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF275A42),
                        selectedTextColor = Color(0xFF275A42),
                        indicatorColor = Color(0xFFEEF6EE)
                    )
                )
                NavigationBarItem(
                    icon = {
                        Icon(
                            imageVector = if (selectedTab == 2) Icons.Filled.BarChart else Icons.Outlined.BarChart,
                            contentDescription = null
                        )
                    },
                    label = { Text("Thống kê") },
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = EmeraldPrimary,
                        indicatorColor = EmeraldSecondary
                    )
                )
                NavigationBarItem(
                    icon = {
                        Icon(
                            imageVector = if (selectedTab == 3) Icons.Filled.Person else Icons.Outlined.Person,
                            contentDescription = null
                        )
                    },
                    label = { Text("Cá nhân") },
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF275A42),
                        selectedTextColor = Color(0xFF275A42),
                        indicatorColor = Color(0xFFEEF6EE)
                    )
                )
            }
        },
        floatingActionButton = {
            if (selectedTab == 1) {
                FloatingActionButton(
                    onClick = onCreateDeck,
                    containerColor = Color(0xFF275A42),
                    contentColor = Color.White,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Tạo bộ thẻ mới", modifier = Modifier.size(28.dp))
                }
            }
        },
        containerColor = Color(0xFFF8FAFC)
    ) { paddingValues ->
        AnimatedContent(
            targetState = selectedTab,
            transitionSpec = {
                if (targetState > initialState) {
                    slideInHorizontally(
                        animationSpec = tween(durationMillis = 350),
                        initialOffsetX = { fullWidth -> fullWidth }
                    ) + fadeIn(
                        animationSpec = tween(durationMillis = 350)
                    ) togetherWith slideOutHorizontally(
                        animationSpec = tween(durationMillis = 350),
                        targetOffsetX = { fullWidth -> -fullWidth }
                    ) + fadeOut(
                        animationSpec = tween(durationMillis = 350)
                    )
                } else {
                    slideInHorizontally(
                        animationSpec = tween(durationMillis = 350),
                        initialOffsetX = { fullWidth -> -fullWidth }
                    ) + fadeIn(
                        animationSpec = tween(durationMillis = 350)
                    ) togetherWith slideOutHorizontally(
                        animationSpec = tween(durationMillis = 350),
                        targetOffsetX = { fullWidth -> fullWidth }
                    ) + fadeOut(
                        animationSpec = tween(durationMillis = 350)
                    )
                }
            },
            label = "tab_transition",
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) { targetTab ->
            when (targetTab) {
                0 -> HomeContent(
                    userRepository = userRepository,
                    deckRepository = deckRepository,
                    srsRepository = srsRepository,
                    analyticsRepository = analyticsRepository,
                    onStudyFlashcard = onStudyFlashcard,
                    onStudyExercise = onStudyExercise,
                    onStudyDeck = onStudyDeck,
                    modifier = Modifier.fillMaxSize()
                )
                1 -> DeckExplorerContent(
                    userRepository = userRepository,
                    deckRepository = deckRepository,
                    analyticsRepository = analyticsRepository,
                    onStudyDeck = onStudyDeck,
                    onDeckDetail = onDeckDetail,
                    modifier = Modifier.fillMaxSize()
                )
                2 -> AnalyticsScreen(
                    viewModel = analyticsViewModel,
                    modifier = Modifier.fillMaxSize()
                )
                3 -> ProfileContent(
                    userRepository = userRepository,
                    viewModel = profileViewModel,
                    onLogout = onLogout,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
