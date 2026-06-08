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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.widget.Toast
import com.example.englishflashcard.data.repository.DictionaryRepository
import com.example.englishflashcard.data.repository.CardRepository
import com.example.englishflashcard.model.Card
import com.example.englishflashcard.model.DictionaryEntry
import com.example.englishflashcard.model.CreateCardRequest
import kotlinx.coroutines.launch
import androidx.compose.ui.platform.LocalContext
import java.util.Locale

private val EmeraldSecondary = Color(0xFF064E3B)
private val EmeraldPrimary = Color(0xFF10B981)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCardScreen(
    dictionaryRepository: DictionaryRepository,
    cardRepository: CardRepository,
    deckId: String,
    onBack: () -> Unit
) {
    var word by remember { mutableStateOf("") }
    var pronunciation by remember { mutableStateOf("") }
    var partOfSpeech by remember { mutableStateOf("") }
    var meaningVi by remember { mutableStateOf("") }
    var example by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    val selectedTags = remember { mutableStateListOf("Tính từ") }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Thêm từ mới", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        if (word.isNotBlank()) {
                            scope.launch {
                                val info = dictionaryRepository.getDictionaryInfo(word)
                                info?.let {
                                    pronunciation = it.phonetic ?: it.phonetics.firstOrNull { p -> !p.text.isNullOrBlank() }?.text ?: ""
                                    example = it.meanings.firstOrNull()?.definitions?.firstOrNull()?.example ?: ""
                                }
                            }
                        }
                    }) {
                        Icon(Icons.Default.AutoFixHigh, contentDescription = "Gợi ý AI", tint = EmeraldPrimary)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            var isSaving by remember { mutableStateOf(false) }
            
            Button(
                onClick = {
                    if (word.isBlank() || meaningVi.isBlank()) return@Button
                    isSaving = true
                    scope.launch {
                            val success = cardRepository.addCardRemote(
                                deckId = deckId,
                                term = word,
                                pronunciation = pronunciation,
                                definitionVi = meaningVi,
                                example = example,
                                note = note,
                                tags = selectedTags.toList(),
                                partOfSpeech = partOfSpeech
                            )
                            isSaving = false
                            if (success) {
                                Toast.makeText(context, "Đã lưu từ vựng", Toast.LENGTH_SHORT).show()
                                onBack()
                            } else {
                                Toast.makeText(context, "Lỗi khi lưu từ vựng. Vui lòng kiểm tra lại thông tin.", Toast.LENGTH_LONG).show()
                            }
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
                    Text("Lưu vào bộ từ", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        },
        containerColor = Color.White
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            AddCardField("Từ vựng (Word)", word, { word = it }, Icons.Default.Edit)
            
            AddCardField(
                label = "Phiên âm (Pronunciation)",
                value = pronunciation,
                onValueChange = { pronunciation = it },
                icon = null
            )

            AddCardField("Từ loại (e.g. noun, verb)", partOfSpeech, { partOfSpeech = it }, null)
            
            AddCardField("Nghĩa tiếng Việt (Meaning)", meaningVi, { meaningVi = it }, Icons.Default.Translate)
            
            AddCardField("Ví dụ & Cụm từ", example, { example = it }, null, minLines = 2)
            
            AddCardField("Ghi chú (Note)", note, { note = it }, Icons.Default.StickyNote2)
            
            Spacer(Modifier.height(16.dp))
            
            Text("Từ loại (Category)", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
            Spacer(Modifier.height(8.dp))
            
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TagSelectionItem("Tính từ", selectedTags.contains("Tính từ")) {
                    if (selectedTags.contains("Tính từ")) selectedTags.remove("Tính từ") else selectedTags.add("Tính từ")
                }
                TagSelectionItem("Học thuật", selectedTags.contains("Học thuật")) {
                    if (selectedTags.contains("Học thuật")) selectedTags.remove("Học thuật") else selectedTags.add("Học thuật")
                }
                TagSelectionItem("Cảm xúc", selectedTags.contains("Cảm xúc")) {
                    if (selectedTags.contains("Cảm xúc")) selectedTags.remove("Cảm xúc") else selectedTags.add("Cảm xúc")
                }
            }
            
            Spacer(Modifier.height(100.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCardField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    icon: ImageVector?,
    iconColor: Color = Color.Gray,
    iconContainerColor: Color = Color.Transparent,
    minLines: Int = 1,
    onIconClick: (() -> Unit)? = null
) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(text = label, fontSize = 14.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(4.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            trailingIcon = icon?.let {
                {
                    Surface(
                        modifier = Modifier
                            .padding(8.dp)
                            .size(32.dp)
                            .clickable(enabled = onIconClick != null) { onIconClick?.invoke() },
                        color = iconContainerColor,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(it, contentDescription = null, tint = iconColor, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black,
                focusedBorderColor = EmeraldPrimary,
                unfocusedBorderColor = Color(0xFFE5E7EB),
                focusedContainerColor = Color(0xFFF9FAFB),
                unfocusedContainerColor = Color(0xFFF9FAFB)
            ),
            shape = RoundedCornerShape(12.dp),
            minLines = minLines
        )
    }
}

@Composable
fun TagSelectionItem(text: String, isSelected: Boolean, onClick: () -> Unit = {}) {
    Surface(
        modifier = Modifier.clickable { onClick() },
        color = if (isSelected) EmeraldSecondary else Color(0xFFF3F4F6),
        shape = RoundedCornerShape(16.dp)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
            color = if (isSelected) Color.White else Color.Gray,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
