package com.example.englishflashcard.feature.deck

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.englishflashcard.data.repository.DeckRepository
import com.example.englishflashcard.data.repository.CardRepository
import com.example.englishflashcard.model.Card
import com.example.englishflashcard.model.FlashcardSet

import com.example.englishflashcard.data.repository.SrsRepository

private val EmeraldPrimary = Color(0xFF10B981)
private val EmeraldSecondary = Color(0xFF064E3B)
private val EmeraldContainer = Color(0xFFEEF6EE)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeckDetailScreen(
    deckRepository: DeckRepository,
    cardRepository: CardRepository,
    srsRepository: SrsRepository,
    deckId: String,
    onBack: () -> Unit,
    onAddCard: () -> Unit,
    onStudyFlashcards: (String) -> Unit,
    onStudyExercise: (String) -> Unit
) {
    val deck = deckRepository.getDeck(deckId) ?: return

    var totalLearned by remember { mutableStateOf(0) }
    var totalMemorized by remember { mutableStateOf(0) }
    var needReview by remember { mutableStateOf(0) }

    // Trigger fetch every time we enter the screen
    LaunchedEffect(Unit) {
        cardRepository.fetchCardsRemote(deckId)
        try {
            val plan = srsRepository.getDailyPlanRemote(deckId)
            if (plan != null) {
                totalLearned = plan.progress.totalLearned
                totalMemorized = plan.progress.totalMemorized
                needReview = plan.progress.totalReviewing
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(deck.title, fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddCard,
                containerColor = EmeraldSecondary,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Thêm từ")
            }
        },
        containerColor = Color(0xFFF9FAFB)
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            item {
                DeckInfoHeader(deck)
            }
            
            item {
                Spacer(Modifier.height(8.dp))
                DeckStatisticsBar(
                    totalWords = deck.cards.size,
                    learned = totalLearned,
                    memorized = totalMemorized,
                    needReview = needReview
                )
                Spacer(Modifier.height(8.dp))
            }

            item {
                StudyActionsRow(
                    onStudyFlashcards = { onStudyFlashcards(deckId) },
                    onStudyExercise = { onStudyExercise(deckId) }
                )
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Danh sách từ vựng",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color(0xFF374151)
                    )
                    
                    Surface(
                        color = Color.White,
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.FilterList,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = Color.Gray
                            )
                            Spacer(Modifier.width(4.dp))
                            Text("Tất cả từ", fontSize = 12.sp, color = Color.Gray)
                        }
                    }
                }
            }

            items(deck.cards) { card ->
                VocabCardItem(card)
            }
            
            item { Spacer(Modifier.height(80.dp)) }
        }
    }
}

@Composable
fun StudyActionsRow(
    onStudyFlashcards: () -> Unit,
    onStudyExercise: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Button(
            onClick = onStudyFlashcards,
            modifier = Modifier
                .weight(1f)
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = EmeraldSecondary),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Style, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Học thẻ", fontWeight = FontWeight.Bold)
        }
        
        OutlinedButton(
            onClick = onStudyExercise,
            modifier = Modifier
                .weight(1f)
                .height(48.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = EmeraldSecondary),
            border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldSecondary),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Quiz, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Luyện tập", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun DeckInfoHeader(deck: FlashcardSet) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        color = Color(0xFFE0F2FE).copy(alpha = 0.5f),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                modifier = Modifier.size(80.dp),
                color = EmeraldSecondary,
                shape = RoundedCornerShape(16.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Filled.MenuBook, contentDescription = null, tint = Color.White, modifier = Modifier.size(40.dp))
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Bộ từ: ${deck.title}",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 22.sp,
                color = EmeraldSecondary
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = deck.description,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                fontSize = 14.sp,
                color = Color.Gray,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }
}

@Composable
fun DeckStatisticsBar(
    totalWords: Int,
    learned: Int,
    memorized: Int,
    needReview: Int
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        color = Color.White,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB)),
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            StatItem(value = totalWords.toString(), label = "Tổng số từ", valueColor = Color(0xFF1F2937))
            StatItem(value = learned.toString(), label = "Đã học", valueColor = Color(0xFF1F2937))
            StatItem(value = memorized.toString(), label = "Đã nhớ", valueColor = Color(0xFF1F2937))
            StatItem(value = needReview.toString(), label = "Cần ôn tập", valueColor = Color(0xFFE53E3E))
        }
    }
}

@Composable
fun StatItem(value: String, label: String, valueColor: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = valueColor
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 13.sp,
            color = Color(0xFF6B7280)
        )
    }
}

@Composable
fun VocabCardItem(card: Card) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = card.term,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = EmeraldSecondary
                    )
                    Spacer(Modifier.width(8.dp))
                    Surface(
                        color = Color(0xFFF3F4F6),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = card.partOfSpeech,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            color = Color.Gray
                        )
                    }
                }
            }
            
            Text(
                text = card.pronunciation,
                fontSize = 13.sp,
                color = Color.LightGray
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = card.definitionVi,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                color = Color.Black
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (card.masteryStatus == "mastered") Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                        contentDescription = null,
                        tint = if (card.masteryStatus == "mastered") EmeraldPrimary else Color.LightGray,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Text(
                    text = if (card.masteryStatus == "mastered") "Thành thạo" else "Đang học",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }
    }
}
