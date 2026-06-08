package com.example.englishflashcard.feature.exercise

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.englishflashcard.data.repository.DeckRepository
import com.example.englishflashcard.data.repository.CardRepository
import com.example.englishflashcard.data.repository.SrsRepository
import com.example.englishflashcard.data.repository.AnalyticsRepository

private val Green = Color(0xFF10B981)
private val DarkGreen = Color(0xFF275A42)
private val Red = Color(0xFFEF4444)
private val Blue = Color(0xFF3B82F6)
private val Orange = Color(0xFFF59E0B)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseScreen(
    deckRepository: DeckRepository,
    cardRepository: CardRepository,
    srsRepository: SrsRepository,
    analyticsRepository: AnalyticsRepository,
    deckId: String? = null,
    onBack: () -> Unit
) {
    val vm = remember { ExerciseViewModel(deckRepository, cardRepository, srsRepository, analyticsRepository) }

    LaunchedEffect(deckId) { vm.loadExercise(deckId) }

    if (vm.isFinished) {
        ResultScreen(vm, onBack)
        return
    }

    Scaffold(
        topBar = {
            Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.Close, null, tint = Color.Gray)
                    }
                    Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                        val progress = if (vm.totalQuestions > 0) (vm.currentIndex + 1f) / vm.totalQuestions else 0f
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                            color = Green,
                            trackColor = Color(0xFFE2E8F0)
                        )
                        Spacer(Modifier.height(4.dp))
                        Text("${vm.currentIndex + 1}/${vm.totalQuestions}", fontSize = 12.sp, color = Color.Gray)
                    }
                    Spacer(Modifier.width(48.dp))
                }
            }
        },
        containerColor = Color(0xFFF8FAFC)
    ) { padding ->
        if (vm.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = DarkGreen)
            }
        } else if (vm.questions.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                val message = if (deckId == "srs_review") {
                    "Không có bài tập ôn"
                } else {
                    "Không có từ vựng nào cần luyện tập hiện tại"
                }
                Text(
                    text = message,
                    color = Color.Gray,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    modifier = Modifier.padding(24.dp),
                    fontSize = 16.sp
                )
            }
        } else {
            val q = vm.currentQuestion ?: return@Scaffold
            Column(
                Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Question type badge
                val (typeLabel, typeColor, typeIcon) = when (q.type) {
                    QuestionType.MULTIPLE_CHOICE -> Triple("Trắc nghiệm", Blue, Icons.Default.HelpOutline)
                    QuestionType.FILL_IN_BLANK -> Triple("Điền từ", Orange, Icons.Default.Edit)
                    QuestionType.TRUE_FALSE -> Triple("Đúng / Sai", DarkGreen, Icons.Default.CheckCircle)
                }
                Surface(color = typeColor.copy(alpha = 0.1f), shape = RoundedCornerShape(20.dp)) {
                    Row(Modifier.padding(horizontal = 12.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(typeIcon, null, tint = typeColor, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(typeLabel, color = typeColor, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }

                Spacer(Modifier.height(24.dp))

                // Term card
                Card(
                    Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        when (q.type) {
                            QuestionType.MULTIPLE_CHOICE -> {
                                Text("Nghĩa của từ này là gì?", color = Color.Gray, fontSize = 14.sp)
                                Spacer(Modifier.height(12.dp))
                                Text(q.card.term, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = DarkGreen)
                                if (q.card.pronunciation.isNotBlank()) {
                                    Text(q.card.pronunciation, color = Color.Gray, fontSize = 14.sp, modifier = Modifier.padding(top = 4.dp))
                                }
                            }
                            QuestionType.FILL_IN_BLANK -> {
                                Text("Nhập từ tiếng Anh cho nghĩa:", color = Color.Gray, fontSize = 14.sp)
                                Spacer(Modifier.height(12.dp))
                                Text(q.card.definitionVi, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = DarkGreen, textAlign = TextAlign.Center)
                            }
                            QuestionType.TRUE_FALSE -> {
                                Text("Cặp từ này đúng hay sai?", color = Color.Gray, fontSize = 14.sp)
                                Spacer(Modifier.height(16.dp))
                                Text(q.card.term, fontSize = 26.sp, fontWeight = FontWeight.Bold, color = DarkGreen)
                                Spacer(Modifier.height(8.dp))
                                Text("=", fontSize = 20.sp, color = Color.Gray)
                                Spacer(Modifier.height(8.dp))
                                Text(q.displayedDefinition, fontSize = 22.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1E293B), textAlign = TextAlign.Center)
                            }
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))

                // Answer area
                when (q.type) {
                    QuestionType.MULTIPLE_CHOICE -> MultipleChoiceOptions(vm, q)
                    QuestionType.FILL_IN_BLANK -> FillInBlankInput(vm)
                    QuestionType.TRUE_FALSE -> TrueFalseButtons(vm)
                }

                // Feedback + Next
                if (vm.hasAnswered) {
                    Spacer(Modifier.height(16.dp))
                    FeedbackBanner(vm.isCorrect, q)
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = { vm.goNext() },
                        Modifier.fillMaxWidth().height(52.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Green),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text(if (vm.currentIndex >= vm.totalQuestions - 1) "Xem kết quả" else "Câu tiếp theo", fontWeight = FontWeight.Bold, color = White)
                    }
                }

                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun MultipleChoiceOptions(vm: ExerciseViewModel, q: QuizQuestion) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        q.options.forEach { option ->
            val correctAnswer = q.card.definitionVi
            val bgColor by animateColorAsState(
                targetValue = when {
                    !vm.hasAnswered -> Color.White
                    option == correctAnswer -> Color(0xFFDCFCE7)
                    option == vm.selectedAnswer && !vm.isCorrect -> Color(0xFFFEE2E2)
                    else -> Color.White
                }, animationSpec = tween(300), label = "bg"
            )
            val borderColor by animateColorAsState(
                targetValue = when {
                    !vm.hasAnswered && vm.selectedAnswer == option -> Blue
                    !vm.hasAnswered -> Color(0xFFE2E8F0)
                    option == correctAnswer -> Green
                    option == vm.selectedAnswer && !vm.isCorrect -> Red
                    else -> Color(0xFFE2E8F0)
                }, animationSpec = tween(300), label = "border"
            )

            Card(
                Modifier.fillMaxWidth().clickable(enabled = !vm.hasAnswered) { vm.selectMultipleChoice(option) }
                    .border(2.dp, borderColor, RoundedCornerShape(14.dp)),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = bgColor),
                elevation = CardDefaults.cardElevation(1.dp)
            ) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    if (vm.hasAnswered && option == correctAnswer) {
                        Icon(Icons.Default.CheckCircle, null, tint = Green, modifier = Modifier.size(22.dp))
                    } else if (vm.hasAnswered && option == vm.selectedAnswer && !vm.isCorrect) {
                        Icon(Icons.Default.Cancel, null, tint = Red, modifier = Modifier.size(22.dp))
                    } else {
                        Box(Modifier.size(22.dp).border(2.dp, Color(0xFFCBD5E1), CircleShape))
                    }
                    Spacer(Modifier.width(12.dp))
                    Text(option, fontSize = 16.sp, color = Color(0xFF1E293B))
                }
            }
        }
    }
}

@Composable
private fun FillInBlankInput(vm: ExerciseViewModel) {
    OutlinedTextField(
        value = vm.typedAnswer,
        onValueChange = { if (!vm.hasAnswered) vm.typedAnswer = it },
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text("Nhập từ tiếng Anh...") },
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = DarkGreen, unfocusedBorderColor = Color(0xFFE2E8F0),
            focusedContainerColor = Color.White, unfocusedContainerColor = Color.White
        ),
        singleLine = true,
        enabled = !vm.hasAnswered
    )
    if (!vm.hasAnswered) {
        Spacer(Modifier.height(12.dp))
        Button(
            onClick = { vm.submitFillInBlank() },
            Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Blue),
            shape = RoundedCornerShape(14.dp),
            enabled = vm.typedAnswer.isNotBlank()
        ) { Text("Kiểm tra", fontWeight = FontWeight.Bold, color = White) }
    }
}

@Composable
private fun TrueFalseButtons(vm: ExerciseViewModel) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        listOf(true to "Đúng" to Green, false to "Sai" to Red).forEach { (pair, color) ->
            val (answer, label) = pair
            val bgColor by animateColorAsState(
                targetValue = when {
                    !vm.hasAnswered -> Color.White
                    vm.trueFalseAnswer == answer && vm.isCorrect -> Color(0xFFDCFCE7)
                    vm.trueFalseAnswer == answer && !vm.isCorrect -> Color(0xFFFEE2E2)
                    else -> Color.White
                }, animationSpec = tween(300), label = "tf"
            )
            Card(
                Modifier.weight(1f).height(80.dp).clickable(enabled = !vm.hasAnswered) { vm.selectTrueFalse(answer) },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = bgColor),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                    Icon(
                        if (answer) Icons.Default.CheckCircle else Icons.Default.Cancel,
                        null, tint = color, modifier = Modifier.size(28.dp)
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(label, fontWeight = FontWeight.Bold, color = color)
                }
            }
        }
    }
}

@Composable
private fun FeedbackBanner(correct: Boolean, q: QuizQuestion) {
    Card(
        Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = if (correct) Color(0xFFDCFCE7) else Color(0xFFFEE2E2))
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    if (correct) Icons.Default.CheckCircle else Icons.Default.Cancel,
                    null, tint = if (correct) Green else Red, modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    if (correct) "Chính xác!" else "Chưa đúng rồi",
                    fontWeight = FontWeight.Bold, fontSize = 16.sp,
                    color = if (correct) DarkGreen else Red
                )
            }
            if (!correct) {
                Spacer(Modifier.height(8.dp))
                Text("Đáp án: ${q.card.term} = ${q.card.definitionVi}", fontSize = 14.sp, color = Color(0xFF1E293B))
            }
        }
    }
}

@Composable
private fun ResultScreen(vm: ExerciseViewModel, onBack: () -> Unit) {
    val total = vm.totalQuestions
    val pct = if (total > 0) (vm.correctCount * 100) / total else 0
    val (emoji, msg) = when {
        pct >= 90 -> "🏆" to "Xuất sắc!"
        pct >= 70 -> "🎉" to "Giỏi lắm!"
        pct >= 50 -> "💪" to "Khá tốt!"
        else -> "📚" to "Cần luyện thêm!"
    }

    Box(Modifier.fillMaxSize().background(Color(0xFFF8FAFC)), contentAlignment = Alignment.Center) {
        Card(
            Modifier.padding(32.dp).fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(6.dp)
        ) {
            Column(Modifier.padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(emoji, fontSize = 64.sp)
                Spacer(Modifier.height(16.dp))
                Text(msg, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = DarkGreen)
                Text(vm.deckTitle, color = Color.Gray, fontSize = 14.sp, modifier = Modifier.padding(top = 4.dp))

                Spacer(Modifier.height(24.dp))
                // Score ring
                Box(Modifier.size(120.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        progress = { pct / 100f }, modifier = Modifier.fillMaxSize(),
                        color = if (pct >= 70) Green else Orange, trackColor = Color(0xFFE2E8F0), strokeWidth = 10.dp
                    )
                    Text("$pct%", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = DarkGreen)
                }

                Spacer(Modifier.height(24.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    StatChip("✅ Đúng", "${vm.correctCount}", Green)
                    StatChip("❌ Sai", "${vm.wrongCount}", Red)
                    StatChip("📝 Tổng", "$total", Blue)
                }

                Spacer(Modifier.height(32.dp))

                if (vm.correctCount < total) {
                    Button(
                        onClick = { vm.restartExercise() }, Modifier.fillMaxWidth().height(52.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DarkGreen),
                        shape = RoundedCornerShape(14.dp)
                    ) { Text("Làm lại", fontWeight = FontWeight.Bold) }
                    Spacer(Modifier.height(12.dp))
                }

                OutlinedButton(
                    onClick = onBack, Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(14.dp)
                ) { Text("Quay lại", fontWeight = FontWeight.Bold, color = DarkGreen) }
            }
        }
    }
}

@Composable
private fun StatChip(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = color)
        Text(label, fontSize = 12.sp, color = Color.Gray)
    }
}
