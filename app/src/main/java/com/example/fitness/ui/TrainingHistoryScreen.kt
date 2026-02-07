package com.example.fitness.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.fitness.data.FirebaseTrainingRecordRepository
import com.example.fitness.data.TrainingRecord
import com.example.fitness.ui.theme.TechColors
import com.example.fitness.ui.theme.glassEffect
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun TrainingHistoryScreen(
    trainingRecordRepository: FirebaseTrainingRecordRepository,
    modifier: Modifier = Modifier,
    onBack: () -> Unit
) {
    // 左上箭頭與系統返回鍵：一律回主頁（由呼叫端決定要切到哪個 tab）
    BackHandler(enabled = true) {
        onBack()
    }

    var currentDate by remember { mutableStateOf(LocalDate.now()) }

    // 改成只訂閱「當天」資料，避免全量 + filter 造成回看缺紀錄
    val dailyRecords by trainingRecordRepository
        .observeRecordsForDate(currentDate)
        .collectAsStateWithLifecycle(initialValue = emptyList())

    Column(modifier = modifier.fillMaxSize().padding(12.dp)) {
        ScreenHeader(title = "訓練日誌", subtitle = "依日期查看訓練紀錄", onBack = onBack)
        Spacer(Modifier.height(12.dp))

        // 日期選擇列（風格沿用 FoodLog 的前後日切換）
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .glassEffect(16.dp)
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { currentDate = currentDate.minusDays(1) }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = MaterialTheme.colorScheme.onSurface)
            }

            Text(
                text = currentDate.format(DateTimeFormatter.ofPattern("yyyy/MM/dd")),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
                    .clickable { currentDate = LocalDate.now() }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            )

            IconButton(onClick = { currentDate = currentDate.plusDays(1) }) {
                Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = MaterialTheme.colorScheme.onSurface)
            }
        }

        Spacer(Modifier.height(12.dp))

        // 當日摘要
        val totalCalories = dailyRecords.sumOf { it.caloriesBurned }.toFloat()
        val totalDurationMin = (dailyRecords.sumOf { it.durationInSeconds } / 60f)

        Box(modifier = Modifier.fillMaxWidth().glassEffect(16.dp).padding(12.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                "當日總結", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    SummaryStat(
                        title = "熱量",
                        value = totalCalories.toInt().toString(),
                        unit = "kcal",
                        icon = Icons.Default.LocalFireDepartment,
                        modifier = Modifier.weight(1f)
                    )
                    SummaryStat(
                        title = "時間",
                        value = String.format(Locale.US, "%.0f", totalDurationMin),
                        unit = "min",
                        icon = Icons.Default.FitnessCenter,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        if (dailyRecords.isEmpty()) {
            EmptyState(title = "這天尚無訓練紀錄", subtitle = "完成訓練後會自動出現在這裡。")
        } else {
            // 依課表名稱分組，讓同天多筆更清楚
            val grouped = remember(dailyRecords) {
                dailyRecords.groupBy { it.planName.ifBlank { "未命名訓練" } }
            }

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 20.dp)
            ) {
                grouped.forEach { (planName, records) ->
                    item {
                        Text(
                            planName,
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White.copy(alpha = 0.85f),
                            modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                        )
                    }
                    items(records) { rec ->
                        TrainingRecordCard(rec)
                    }
                }
            }
        }
    }
}

@Composable
private fun TrainingRecordCard(record: TrainingRecord) {
    var expanded by remember(record.id) { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                record.planName.ifBlank { "未命名訓練" },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                StatPill(label = "${record.durationInSeconds / 60} 分鐘")
                StatPill(label = "${record.caloriesBurned.toInt()} kcal")
                StatPill(label = "${record.exercises.size} 動作")
            }

            if (!record.notes.isNullOrBlank()) {
                Text("備註：${record.notes}", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.75f))
            }

            if (record.exercises.isNotEmpty()) {
                HorizontalDivider(color = Color.White.copy(alpha = 0.08f), thickness = 0.5.dp)

                val list = if (expanded) record.exercises else record.exercises.take(6)
                list.forEach { ex ->
                    Text(
                        text = "• ${ex.name}  ${ex.sets} 組 × ${ex.reps} 次" +
                            (ex.weight?.let { "  @ ${String.format(Locale.US, "%.1f", it)} kg" } ?: ""),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                }

                if (!expanded && record.exercises.size > 6) {
                    Text(
                        "…還有 ${record.exercises.size - 6} 個動作（點擊展開）",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                } else if (expanded && record.exercises.size > 6) {
                    Text(
                        "點擊可收合",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

@Composable
private fun StatPill(label: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(Color.White.copy(alpha = 0.08f))
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = Color.White)
    }
}
