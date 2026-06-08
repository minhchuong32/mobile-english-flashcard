package com.example.englishflashcard.feature.deck

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.englishflashcard.data.repository.UserRepository
import com.example.englishflashcard.data.repository.DeckRepository
import com.example.englishflashcard.data.repository.AnalyticsRepository
import com.example.englishflashcard.model.FlashcardSet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeckExplorerScreen(
    userRepository: UserRepository,
    deckRepository: DeckRepository,
    analyticsRepository: AnalyticsRepository,
    onStudyDeck: (String) -> Unit,
    onDeckDetail: (String) -> Unit,
    onCreateDeck: () -> Unit,
    onHome: () -> Unit,
    onStats: () -> Unit,
    onProfile: () -> Unit,
    onBack: () -> Unit
) {
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
                    IconButton(onClick = onBack) {
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
                            .clickable(onClick = onProfile),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = userRepository.currentUser?.name?.firstOrNull()?.uppercase() ?: "U",
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
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreateDeck,
                containerColor = Color(0xFF275A42),
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tạo bộ thẻ mới", modifier = Modifier.size(28.dp))
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    icon = { Icon(Icons.Outlined.School, contentDescription = null) },
                    label = { Text("Học") },
                    selected = false,
                    onClick = onHome
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.MenuBook, contentDescription = null) },
                    label = { Text("Bộ từ") },
                    selected = true,
                    onClick = { /* Already here */ },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF275A42),
                        selectedTextColor = Color(0xFF275A42),
                        indicatorColor = Color(0xFFEEF6EE)
                    )
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Outlined.BarChart, contentDescription = null) },
                    label = { Text("Thống kê") },
                    selected = false,
                    onClick = onStats
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Outlined.Person, contentDescription = null) },
                    label = { Text("Cá nhân") },
                    selected = false,
                    onClick = onProfile
                )
            }
        },
        containerColor = Color(0xFFF8FAFC)
    ) { paddingValues ->
        DeckExplorerContent(
            userRepository = userRepository,
            deckRepository = deckRepository,
            analyticsRepository = analyticsRepository,
            onStudyDeck = onStudyDeck,
            onDeckDetail = onDeckDetail,
            modifier = Modifier.padding(paddingValues)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeckExplorerContent(
    userRepository: UserRepository,
    deckRepository: DeckRepository,
    analyticsRepository: AnalyticsRepository,
    onStudyDeck: (String) -> Unit,
    onDeckDetail: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQueries by remember { mutableStateOf("") }
    var selectedTab by remember { mutableStateOf(0) } // 0: My Decks, 1: Explore Public
    var isLoading by remember { mutableStateOf(false) }
    val allDecks = remember { mutableStateListOf<FlashcardSet>() }

    // Fetch Decks from API
    LaunchedEffect(Unit) {
        isLoading = true
        val remoteDecks = deckRepository.getDecksRemote()
        allDecks.clear()
        allDecks.addAll(remoteDecks)
        isLoading = false
    }

    // Stats from Repository
    val stats = analyticsRepository.stats
    val currentUserId = userRepository.currentUser?._id

    // Filters based on tab and search
    val myDecks = allDecks.filter {
        val isOwner = it.createdBy?._id == currentUserId || (currentUserId != null && it.createdBy == null)
        isOwner && (searchQueries.isBlank() || it.title.contains(searchQueries, ignoreCase = true))
    }

    val exploreDecks = allDecks.filter {
        val isNotOwner = it.createdBy?._id != currentUserId
        val isPublic = it.isPublic
        isPublic && isNotOwner && (searchQueries.isBlank() || it.title.contains(searchQueries, ignoreCase = true))
    }

    Column(
        modifier = modifier
            .fillMaxSize()
    ) {
        // Search Bar
        OutlinedTextField(
            value = searchQueries,
            onValueChange = { searchQueries = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            placeholder = { Text("Tìm kiếm bộ từ vựng...", color = Color.Gray) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
            shape = RoundedCornerShape(24.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
                focusedContainerColor = Color(0xFFF1F5F9),
                unfocusedContainerColor = Color(0xFFF1F5F9)
            ),
            singleLine = true
        )

        // Overview stats row matching exactly the image mockup
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Streak Card (Dark green)
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF275A42))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF387E5D)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.LocalFireDepartment,
                            contentDescription = null,
                            tint = Color(0xFFF59E0B),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Chuỗi học", color = Color(0xFFA7F3D0), fontSize = 12.sp)
                        Text("${stats.streakDays} ngày", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                }
            }

            // Total cards card (Soft Blue)
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFDCE9FC))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFC5DAF9)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.School,
                            contentDescription = null,
                            tint = Color(0xFF1E3A8A),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Tổng từ", color = Color(0xFF4B5563), fontSize = 12.sp)
                        Text("${stats.learnedCards} từ", color = Color(0xFF1E293B), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Capsule switcher for Mine vs Public
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            FilterChip(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                label = { Text("Bộ từ của bạn") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFFEEF6EE),
                    selectedLabelColor = Color(0xFF275A42),
                    containerColor = Color(0xFFF1F5F9),
                    labelColor = Color.Gray
                ),
                border = null,
                shape = RoundedCornerShape(20.dp)
            )
            FilterChip(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                label = { Text("Khám phá công khai") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFFEEF6EE),
                    selectedLabelColor = Color(0xFF275A42),
                    containerColor = Color(0xFFF1F5F9),
                    labelColor = Color.Gray
                ),
                border = null,
                shape = RoundedCornerShape(20.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Section Header Text
        Text(
            text = if (selectedTab == 0) "Bộ từ của bạn" else "Bộ từ gợi ý",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1E293B),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        // Grid Content
        Box(modifier = Modifier.fillMaxSize()) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = Color(0xFF275A42)
                )
            } else {
                val displayedList = if (selectedTab == 0) myDecks else exploreDecks
                if (displayedList.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Book,
                            contentDescription = null,
                            tint = Color.LightGray,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (selectedTab == 0) "Bạn chưa tạo bộ từ vựng nào" else "Không tìm thấy bộ từ vựng công khai nào",
                            color = Color.Gray,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(displayedList) { deck ->
                            DeckGridItem(
                                deck = deck,
                                onClick = { onDeckDetail(deck._id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DeckGridItem(
    deck: FlashcardSet,
    onClick: () -> Unit
) {
    val (iconBgColor, iconTintColor, icon) = when {
        deck.title.contains("IELTS", ignoreCase = true) -> Triple(Color(0xFFE0F2FE), Color(0xFF0284C7), Icons.Default.Book)
        deck.title.contains("Basic", ignoreCase = true) -> Triple(Color(0xFFDCFCE7), Color(0xFF16A34A), Icons.Default.Star)
        deck.title.contains("Travel", ignoreCase = true) -> Triple(Color(0xFFFEF3C7), Color(0xFFD97706), Icons.Default.Flight)
        deck.title.contains("Business", ignoreCase = true) -> Triple(Color(0xFFE2E8F0), Color(0xFF475569), Icons.Default.Work)
        else -> Triple(Color(0xFFF3E8FF), Color(0xFF9333EA), Icons.Default.MenuBook)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(iconBgColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTintColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
                IconButton(
                    onClick = { /* Menu Action */ },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Options",
                        tint = Color.Gray,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = deck.title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E293B),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = deck.description.ifBlank { "Không có mô tả cho bộ thẻ này." },
                fontSize = 12.sp,
                color = Color.Gray,
                maxLines = 3,
                minLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val tags = if (deck.tags.isEmpty()) listOf("Hàng ngày") else deck.tags
                tags.take(2).forEach { tag ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFFEEF2F6))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = tag,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF64748B)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = Color(0xFFF1F5F9))
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.MenuBook,
                    contentDescription = null,
                    tint = Color.Gray,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "${deck.totalCards} từ",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }
    }
}
