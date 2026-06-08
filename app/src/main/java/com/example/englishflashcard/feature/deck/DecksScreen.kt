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
import androidx.compose.material.icons.filled.AirplanemodeActive
import androidx.compose.material.icons.filled.BusinessCenter
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.englishflashcard.data.api.*
import com.example.englishflashcard.data.repository.UserRepository
import com.example.englishflashcard.data.repository.DeckRepository
import com.example.englishflashcard.data.repository.AnalyticsRepository
import com.example.englishflashcard.data.repository.NotificationRepository
import com.example.englishflashcard.feature.home.MobileBottomNavBar
import com.example.englishflashcard.feature.home.MobileTopAppBar
import com.example.englishflashcard.model.FlashcardSet
import com.example.englishflashcard.model.DeckResponse
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

import androidx.compose.ui.platform.LocalContext
import com.example.englishflashcard.data.repository.CardRepository
import com.example.englishflashcard.data.repository.SrsRepository

// Reuse the design tokens from HomeScreen
private val EmeraldPrimary = Color(0xFF10B981)
private val EmeraldSurface = Color(0xFFF4FBF4)
private val EmeraldOnSurface = Color(0xFF1C1D1C)
private val EmeraldOnSurfaceVariant = Color(0xFF424942)
private val EmeraldSecondary = Color(0xFF064E3B)
private val EmeraldContainer = Color(0xFFEEF6EE)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DecksScreen(
    userRepository: UserRepository,
    deckRepository: DeckRepository,
    analyticsRepository: AnalyticsRepository,
    onHome: () -> Unit,
    onDecks: () -> Unit,
    onStats: () -> Unit,
    onProfile: () -> Unit,
    onCreateDeck: () -> Unit,
    onDeckDetail: (String) -> Unit,
    onLogout: () -> Unit
) {
    val user = userRepository.currentUser
    val stats = analyticsRepository.stats
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        deckRepository.fetchDecksRemote()
        analyticsRepository.fetchAnalyticsRemote()
    }

    val decks = deckRepository.getAllDecks()

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
                onHome = onHome,
                onDecks = onDecks,
                onStats = onStats,
                onProfile = onProfile,
                selectedItem = 1
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreateDeck,
                containerColor = EmeraldSecondary,
                contentColor = Color.White,
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Tạo bộ thẻ")
            }
        },
        containerColor = Color.White
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            SearchBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it }
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                StatsCard(
                    modifier = Modifier.weight(1f),
                    title = "Chuỗi học",
                    value = "${stats.streakDays} ngày",
                    icon = Icons.Filled.LocalFireDepartment,
                    containerColor = EmeraldSecondary,
                    contentColor = Color.White
                )
                StatsCard(
                    modifier = Modifier.weight(1f),
                    title = "Tổng từ",
                    value = "${stats.learnedCards} từ",
                    icon = Icons.Filled.School,
                    containerColor = Color(0xFFE0E7FF),
                    contentColor = Color(0xFF1E3A8A)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Bộ từ của bạn",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = EmeraldSecondary
            )

            Spacer(modifier = Modifier.height(16.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(decks.filter { it.title.contains(searchQuery, ignoreCase = true) }) { deck ->
                    DeckCard(deck = deck, onClick = { onDeckDetail(deck._id) })
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBar(query: String, onQueryChange: (String) -> Unit) {
    TextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(16.dp)),
        placeholder = { Text("Tìm kiếm bộ từ vựng...", color = Color.Gray) },
        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = Color.Gray) },
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color(0xFFF3F4F6),
            unfocusedContainerColor = Color(0xFFF3F4F6),
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent
        ),
        singleLine = true
    )
}

@Composable
fun StatsCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    icon: ImageVector,
    containerColor: Color,
    contentColor: Color
) {
    Card(
        modifier = modifier.height(100.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(contentColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = contentColor, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(text = title, fontSize = 12.sp, color = contentColor.copy(alpha = 0.8f))
                Text(text = value, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = contentColor)
            }
        }
    }
}

@Composable
fun DeckCard(deck: FlashcardSet, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.8f)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF3F4F6)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(36.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = when (deck.title) {
                        "IELTS Core" -> Color(0xFFECFDF5)
                        "Basic English" -> Color(0xFFF0FDF4)
                        "Travel Vocabulary" -> Color(0xFFFFFBEB)
                        else -> Color(0xFFF3F4F6)
                    }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = when (deck.title) {
                                "IELTS Core" -> Icons.Filled.MenuBook
                                "Basic English" -> Icons.Filled.Star
                                "Travel Vocabulary" -> Icons.Filled.AirplanemodeActive
                                "Business English" -> Icons.Filled.BusinessCenter
                                else -> Icons.Filled.ContentCopy
                            },
                            contentDescription = null,
                            tint = when (deck.title) {
                                "IELTS Core" -> Color(0xFF059669)
                                "Basic English" -> Color(0xFF16A34A)
                                "Travel Vocabulary" -> Color(0xFFD97706)
                                "Business English" -> Color(0xFF0D9488)
                                else -> Color.Gray
                            },
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Icon(Icons.Filled.MoreVert, contentDescription = null, tint = Color.LightGray)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = deck.title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = deck.description,
                fontSize = 12.sp,
                color = Color.Gray,
                maxLines = 2,
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                deck.tags.forEach { tag ->
                    TagItem(tag)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.MenuBook, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "${deck.totalCards} từ", fontSize = 12.sp, color = Color.Gray)
            }
        }
    }
}

@Composable
fun TagItem(tag: String) {
    Surface(
        color = EmeraldContainer,
        shape = RoundedCornerShape(4.dp)
    ) {
        Text(
            text = tag,
            fontSize = 10.sp,
            color = EmeraldSecondary,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            fontWeight = FontWeight.Medium
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DecksScreenPreview() {
    val context = LocalContext.current
    val retrofit = Retrofit.Builder()
        .baseUrl("https://example.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    val authApiService = retrofit.create(AuthApiService::class.java)
    val deckApiService = retrofit.create(DeckApiService::class.java)
    val cardApiService = retrofit.create(CardApiService::class.java)
    val userApiService = retrofit.create(UserApiService::class.java)
    val dictionaryApiService = retrofit.create(DictionaryApiService::class.java)
    val sessionApiService = retrofit.create(StudySessionApiService::class.java)
    val srsRepository = SrsRepository(context, sessionApiService)
    val userRepository = UserRepository(context, userApiService)
    val analyticsRepository = AnalyticsRepository(userApiService)
    val notificationRepository = NotificationRepository()
    val deckRepository = DeckRepository(deckApiService, analyticsRepository, notificationRepository)
    val cardRepository = CardRepository(cardApiService, deckRepository)
    
    DecksScreen(
        userRepository = userRepository,
        deckRepository = deckRepository,
        analyticsRepository = analyticsRepository,
        onHome = {},
        onDecks = {},
        onStats = {},
        onProfile = {},
        onCreateDeck = {},
        onDeckDetail = {},
        onLogout = {}
    )
}
