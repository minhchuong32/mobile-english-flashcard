package com.example.englishflashcard.feature.deck

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import com.example.englishflashcard.data.repository.DictionaryRepository
import com.example.englishflashcard.data.repository.DeckRepository
import com.example.englishflashcard.data.repository.CardRepository
import com.example.englishflashcard.model.Card
import com.example.englishflashcard.model.CreateDeckRequest
import com.example.englishflashcard.model.CreateCardRequest
import com.example.englishflashcard.model.DictionaryEntry
import kotlinx.coroutines.launch
import java.util.UUID

private val EmeraldSecondary = Color(0xFF064E3B)
private val EmeraldPrimary = Color(0xFF10B981)
private val EmeraldSurface = Color(0xFFF9FAFB)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateDeckScreen(
    dictionaryRepository: DictionaryRepository,
    deckRepository: DeckRepository,
    cardRepository: CardRepository,
    onBack: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    
    // Form for new card
    var word by remember { mutableStateOf("") }
    var pronunciation by remember { mutableStateOf("") }
    var partOfSpeech by remember { mutableStateOf("") }
    var meaningVi by remember { mutableStateOf("") }
    var example by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    val selectedTags = remember { mutableStateListOf<String>() }
    
    val tempCards = remember { mutableStateListOf<Card>() }
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Tạo bộ thẻ", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            var isSaving by remember { mutableStateOf(false) }
            
            Button(
                onClick = {
                    if (title.isBlank()) return@Button
                    isSaving = true
                    scope.launch {
                        // 1. Create Deck on Server
                        val remoteDeck = deckRepository.createDeckRemote(title, description, emptyList())
                        
                        if (remoteDeck != null) {
                            // 1.5. Add current form word if it's not empty
                            val finalCards = tempCards.toMutableList()
                            if (word.isNotBlank() && meaningVi.isNotBlank()) {
                                finalCards.add(Card(
                                    _id = UUID.randomUUID().toString(),
                                    term = word,
                                    pronunciation = pronunciation,
                                    partOfSpeech = partOfSpeech,
                                    definitionVi = meaningVi,
                                    exampleSentence = example,
                                    note = note,
                                    tags = selectedTags.toList()
                                ))
                            }

                            if (finalCards.isEmpty()) {
                                Toast.makeText(context, "Vui lòng thêm ít nhất 1 từ vựng", Toast.LENGTH_SHORT).show()
                                isSaving = false
                                return@launch
                            }

                            // 2. Add each card to that deck on Server
                            var allCardsSuccess = true
                            finalCards.forEach { card ->
                                val success = cardRepository.addCardRemote(
                                    deckId = remoteDeck._id,
                                    term = card.term,
                                    pronunciation = card.pronunciation,
                                    definitionVi = card.definitionVi,
                                    example = card.exampleSentence,
                                    note = card.note,
                                    tags = card.tags,
                                    partOfSpeech = card.partOfSpeech
                                )
                                if (!success) allCardsSuccess = false
                            }

                            if (allCardsSuccess) {
                                Toast.makeText(context, "Đã tạo bộ thẻ thành công", Toast.LENGTH_SHORT).show()
                                onBack()
                            } else {
                                Toast.makeText(context, "Bộ thẻ đã tạo nhưng một số từ có lỗi khi lưu", Toast.LENGTH_LONG).show()
                                onBack()
                            }
                        } else {
                            Toast.makeText(context, "Lỗi kết nối máy chủ khi tạo bộ thẻ", Toast.LENGTH_SHORT).show()
                        }
                        isSaving = false
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldSecondary),
                shape = RoundedCornerShape(16.dp),
                enabled = !isSaving
            ) {
                if (isSaving) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Icon(Icons.Default.Save, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Lưu bộ thẻ", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        },
        containerColor = Color.White
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {
            // Deck Info Section
            Text("Thông tin bộ thẻ", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = EmeraldSecondary)
            Spacer(Modifier.height(12.dp))
            
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Tên bộ thẻ") },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                    focusedBorderColor = EmeraldPrimary
                )
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Mô tả") },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                    focusedBorderColor = EmeraldPrimary
                )
            )
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 24.dp))
            
            // Add Word Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Thêm từ mới", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = EmeraldSecondary)
                if (word.isNotBlank()) {
                    TextButton(onClick = {
                        scope.launch {
                            val info = dictionaryRepository.getDictionaryInfo(word)
                            info?.let {
                                pronunciation = it.phonetic ?: it.phonetics.firstOrNull { p -> !p.text.isNullOrBlank() }?.text ?: ""
                                example = it.meanings.firstOrNull()?.definitions?.firstOrNull()?.example ?: ""
                                // Note: Translation to Vietnamese would need a separate API (like Google Translate)
                                // but we provide pronunciation and English definition for now.
                            }
                        }
                    }) {
                        Icon(Icons.Default.AutoFixHigh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Gợi ý AI", fontSize = 12.sp)
                    }
                }
            }
            
            Spacer(Modifier.height(12.dp))
            
            AddCardField("Từ vựng (Word)", word, { word = it }, Icons.Default.Edit)
            AddCardField("Phiên âm (Pronunciation)", pronunciation, { pronunciation = it }, null)
            AddCardField("Từ loại (e.g. noun, verb)", partOfSpeech, { partOfSpeech = it }, null)
            AddCardField("Nghĩa tiếng Việt (Meaning)", meaningVi, { meaningVi = it }, Icons.Default.Translate)
            AddCardField("Ví dụ", example, { example = it }, null, minLines = 2)
            AddCardField("Ghi chú", note, { note = it }, Icons.Default.StickyNote2)
            
            Spacer(Modifier.height(16.dp))
            
            Button(
                onClick = {
                    if (word.isBlank() || meaningVi.isBlank()) return@Button
                    tempCards.add(
                        Card(
                            _id = UUID.randomUUID().toString(),
                            term = word,
                            pronunciation = pronunciation,
                            partOfSpeech = partOfSpeech,
                            definitionVi = meaningVi,
                            exampleSentence = example,
                            note = note,
                            tags = selectedTags.toList()
                        )
                    )
                    // Clear form
                    word = ""; pronunciation = ""; partOfSpeech = ""; meaningVi = ""; example = ""; note = ""
                    selectedTags.clear()
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Thêm từ vào danh sách tạm")
            }
            
            Spacer(Modifier.height(24.dp))
            
            // Temporary list of cards
            if (tempCards.isNotEmpty()) {
                Text("Danh sách từ đã thêm (${tempCards.size})", fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                tempCards.forEach { card ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF3F4F6))
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(card.term, fontWeight = FontWeight.Bold)
                                Text(card.definitionVi, fontSize = 12.sp, color = Color.Gray)
                            }
                            IconButton(onClick = { tempCards.remove(card) }) {
                                Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red, modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }
            }
            
            Spacer(Modifier.height(100.dp))
        }
    }
}
