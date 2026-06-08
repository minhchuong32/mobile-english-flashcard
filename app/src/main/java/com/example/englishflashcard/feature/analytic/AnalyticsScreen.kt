package com.example.englishflashcard.feature.analytic

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.englishflashcard.model.AnalyticsResponse
import com.example.englishflashcard.model.StudyHistoryItem
import com.example.englishflashcard.ui.theme.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

@Composable
fun AnalyticsScreen(
    viewModel: AnalyticsViewModel,
    modifier: Modifier = Modifier
) {
    val analyticsData = viewModel.analyticsData
    val isLoading = viewModel.isLoading

    if (isLoading) {
        Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = EmeraldPrimary)
        }
    } else if (analyticsData == null || (analyticsData.overview.totalLearned == 0 && analyticsData.studyHistory.isEmpty())) {
        EmptyAnalyticsState(modifier)
    } else {
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(EmeraldSurface)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            HeroStreakCard(
                currentStreak = analyticsData.overview.currentStreak,
                longestStreak = analyticsData.overview.longestStreak
            )

            QuickStatsRow(
                totalDays = analyticsData.overview.totalDaysStudied,
                level = analyticsData.overview.estimatedLevel
            )

            ActivityChartSection(studyHistory = analyticsData.studyHistory)

            VocabularyProgressSection(
                totalLearned = analyticsData.overview.totalLearned,
                totalMemorized = analyticsData.overview.totalMemorized
            )

            StudyInsightsSection(
                studyHistory = analyticsData.studyHistory,
                overview = analyticsData.overview
            )
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun EmptyAnalyticsState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.BarChart,
            contentDescription = null,
            modifier = Modifier.size(100.dp),
            tint = Color.LightGray
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Chưa có dữ liệu thống kê",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = EmeraldSecondary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Hãy hoàn thành vài bài học để xem thống kê chi tiết.",
            textAlign = TextAlign.Center,
            color = Color.Gray
        )
    }
}

@Composable
fun HeroStreakCard(currentStreak: Int, longestStreak: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(OrangeStreak.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "🔥", fontSize = 32.sp)
            }
            
            Spacer(modifier = Modifier.width(20.dp))
            
            Column {
                Text(
                    text = "$currentStreak ngày liên tiếp",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = OrangeStreak
                )
                Text(
                    text = "Chuỗi dài nhất: $longestStreak ngày",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
fun QuickStatsRow(totalDays: Int, level: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        StatCard(
            modifier = Modifier.weight(1f),
            icon = "📚",
            value = "$totalDays",
            label = "Ngày đã học",
            color = Color(0xFF3B82F6)
        )
        StatCard(
            modifier = Modifier.weight(1f),
            icon = "🎯",
            value = level,
            label = "Trình độ",
            color = EmeraldPrimary
        )
    }
}

@Composable
fun StatCard(modifier: Modifier, icon: String, value: String, label: String, color: Color) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = icon, fontSize = 24.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun ActivityChartSection(studyHistory: List<StudyHistoryItem>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Hoạt động 7 ngày qua",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Simple Bar Chart
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                val last7Days = getLast7Days()
                val historyMap = studyHistory.associateBy { 
                    it.date.substring(0, 10) // Assuming format YYYY-MM-DD...
                }
                
                last7Days.forEach { date ->
                    val studied = historyMap[date]?.cardsStudied ?: 0
                    val maxStudied = studyHistory.maxOfOrNull { it.cardsStudied }?.coerceAtLeast(10) ?: 10
                    val heightFactor = (studied.toFloat() / maxStudied).coerceIn(0.05f, 1f)
                    
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        if (studied > 0) {
                            Text(
                                text = "$studied",
                                fontSize = 10.sp,
                                color = EmeraldPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxHeight(heightFactor)
                                .width(12.dp)
                                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                .background(if (studied > 0) EmeraldPrimary else Color.LightGray.copy(alpha = 0.3f))
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = getDayOfWeek(date),
                            fontSize = 10.sp,
                            color = Color.Gray
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            val totalWeekly = studyHistory.take(7).sumOf { it.cardsStudied }
            Text(
                text = "Tổng cộng: $totalWeekly thẻ trong tuần này",
                style = MaterialTheme.typography.bodyMedium,
                color = EmeraldSecondary,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun VocabularyProgressSection(totalLearned: Int, totalMemorized: Int) {
    if (totalLearned == 0) return

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Tiến độ từ vựng",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            val progress = if (totalLearned > 0) totalMemorized.toFloat() / totalLearned else 0f
            val percent = (progress * 100).toInt()
            
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text(text = "Đã ghi nhớ", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                Text(text = "$percent%", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = EmeraldPrimary)
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .clip(RoundedCornerShape(6.dp)),
                color = EmeraldPrimary,
                trackColor = EmeraldPrimary.copy(alpha = 0.1f)
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text(text = "$totalLearned", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                    Text(text = "Từ đã học", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "$totalMemorized", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = EmeraldPrimary)
                    Text(text = "Từ đã ghi nhớ", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
            }
        }
    }
}

@Composable
fun StudyInsightsSection(studyHistory: List<StudyHistoryItem>, overview: com.example.englishflashcard.model.OverviewStats) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Phân tích học tập",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            val totalInHistory = studyHistory.sumOf { it.cardsStudied }
            val avgPerDay = if (studyHistory.isNotEmpty()) totalInHistory / studyHistory.size else 0
            
            InsightRow(
                icon = Icons.Default.TrendingUp,
                label = "Trung bình mỗi ngày",
                value = "$avgPerDay thẻ",
                color = Color(0xFF8B5CF6)
            )
            Divider(modifier = Modifier.padding(vertical = 12.dp), color = Color.LightGray.copy(alpha = 0.3f))
            InsightRow(
                icon = Icons.Default.CalendarMonth,
                label = "Số ngày học tuần này",
                value = "${studyHistory.size}/7 ngày",
                color = Color(0xFFEC4899)
            )
            Divider(modifier = Modifier.padding(vertical = 12.dp), color = Color.LightGray.copy(alpha = 0.3f))
            InsightRow(
                icon = Icons.Default.CheckCircle,
                label = "Độ chính xác",
                value = "${(overview.accuracy).toInt()}%",
                color = Color(0xFFF59E0B)
            )
        }
    }
}

@Composable
fun InsightRow(icon: ImageVector, label: String, value: String, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = label, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
        Text(text = value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
    }
}

private fun getLast7Days(): List<String> {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    return (0..6).reversed().map {
        LocalDate.now().minusDays(it.toLong()).format(formatter)
    }
}

private fun getDayOfWeek(dateString: String): String {
    return try {
        val date = LocalDate.parse(dateString)
        date.dayOfWeek.name.take(1) // M, T, W, T, F, S, S
    } catch (e: Exception) {
        ""
    }
}
