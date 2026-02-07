package com.example.fitness.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.fitness.BuildConfig
import com.example.fitness.ai.GeminiTrainingPlanParser
import com.example.fitness.ai.UserTrainingContextRepository
import com.example.fitness.data.TrainingPlan
import com.example.fitness.data.TrainingPlanRepository
import com.example.fitness.inbody.FirebaseInBodyRepository
import com.example.fitness.network.GeminiClient
import com.example.fitness.ui.theme.TechColors
import com.example.fitness.ui.theme.glassEffect
import com.example.fitness.ui.theme.neonGlowBorder
import kotlinx.coroutines.launch
import java.time.ZoneId

// 定義目標選項
private enum class PlanGoal(val label: String) {
    BULK("增肌"),
    CUT("減脂"),
}

// 定義程度選項
private enum class ExperienceLevel(val label: String) {
    BEGINNER("新手"),
    INTERMEDIATE("中階"),
    ADVANCED("高階")
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WorkoutCreateScreen(
    repository: TrainingPlanRepository,
    onDone: () -> Unit,
) {
    val userCtxRepo = remember { UserTrainingContextRepository() }
    val userCtx by userCtxRepo.observeMyContext().collectAsState(initial = null)

    val profile = userCtx?.profile
    val latestInBody = userCtx?.latestInBody

    val weightKg = profile?.weightKg ?: 70f
    val heightCm = profile?.heightCm ?: 170f

    // 年齡：從 profile 自動帶入（允許空值時顯示空字串）
    var ageYearsInput by remember(profile?.age) {
        mutableStateOf(profile?.age?.takeIf { it > 0 }?.toString().orEmpty())
    }

    val muscleMassKg = latestInBody?.muscleMassKg
    val bodyFatPercent = latestInBody?.bodyFatPercent
    val inBodyWeightKg = latestInBody?.weightKg

    // State Variables
    var goal by remember { mutableStateOf(PlanGoal.BULK) }
    var daysPerWeek by remember { mutableStateOf(4) }
    var experience by remember { mutableStateOf(ExperienceLevel.BEGINNER) }

    var loading by remember { mutableStateOf(false) }
    var errorText by remember { mutableStateOf<String?>(null) }

    var previewPlan by remember { mutableStateOf<TrainingPlan?>(null) }
    var showSaveDialog by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    fun buildPrompt(): String {
        val zoneId = ZoneId.systemDefault()
        val now = java.time.ZonedDateTime.now(zoneId)
        val effectiveWeightKg = inBodyWeightKg ?: weightKg

        return buildString {
            appendLine("你是健身教練，請依照使用者資料『產生一份訓練計畫』。")
            appendLine("目標：${goal.label}")
            appendLine("每週訓練天數：$daysPerWeek 天")
            appendLine("使用者訓練程度：${experience.label}")
            appendLine("使用者資料：")
            // ★ 修改 2: 直接使用字串，若為空則在按鈕點擊時會被攔截，這裡不用擔心
            appendLine("- 年齡：$ageYearsInput 歲")
            appendLine("- 身高：${"%.1f".format(heightCm)} cm")
            appendLine("- 體重：${"%.1f".format(effectiveWeightKg)} kg")
            if (muscleMassKg != null) appendLine("- 肌肉量：${"%.1f".format(muscleMassKg)} kg")
            if (bodyFatPercent != null) appendLine("- 體脂：${"%.1f".format(bodyFatPercent)} %")
            appendLine("- 目前日期：${now.toLocalDate()}")
            appendLine()
            appendLine("請只回傳 JSON（不要任何額外文字、不要 code fence）。")
            appendLine()
            appendLine("JSON 格式如下（days 的長度請等於每週訓練天數）：")
            appendLine("{")
            appendLine("  \"name\": \"計畫名稱\",")
            appendLine("  \"days\": [")
            appendLine("    {")
            appendLine("      \"day\": 1,")
            appendLine("      \"focus\": \"胸+三頭\",")
            appendLine("      \"exercises\": [")
            appendLine("        { \"name\": \"動作名稱\", \"sets\": 4, \"reps\": 10, \"weightKg\": 20 }")
            appendLine("      ]")
            appendLine("    }")
            appendLine("  ]")
            appendLine("}")
            appendLine()
            appendLine("規則：")
            appendLine("- days 請從 day=1 開始，依序遞增")
            appendLine("- 每一天至少 5 個動作；整份計畫總動作數至少 12")
            appendLine("- sets/reps 需為整數")
            appendLine("- weightKg 可省略或為 0（若你無法判斷重量）")
            appendLine("- 動作名稱請用繁體中文（可附英文別名但以中文為主）")
        }
    }

    // Global Background
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(TechColors.DarkBlue, TechColors.DeepPurple)
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .glassEffect(cornerRadius = 20.dp)
                    .padding(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onDone) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回",
                            tint = TechColors.NeonBlue
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text(
                            "AI 訓練計畫",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = TechColors.NeonBlue
                        )
                        Text(
                            "AI 智能生成專屬課表",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Configuration Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .glassEffect(cornerRadius = 24.dp)
                    .padding(20.dp)
            ) {
                Column {
                    Text("設定目標", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                    Spacer(Modifier.height(12.dp))

                    // Goal Toggle Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        PlanGoal.entries.forEach { g ->
                            NeonToggleOption(
                                text = g.label,
                                isSelected = goal == g,
                                onClick = { goal = g },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Spacer(Modifier.height(20.dp))

                    Text("使用者資料", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                    Spacer(Modifier.height(8.dp))

                    // Stats Row
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        NeonStatChip("身高", "${heightCm.toInt()}cm")
                        NeonStatChip("體重", "${(inBodyWeightKg ?: weightKg).toInt()}kg")
                        bodyFatPercent?.let { NeonStatChip("體脂", "${it.toInt()}%") }
                    }

                    Spacer(Modifier.height(16.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        // ★ 修改 3: 更新輸入框邏輯，允許空值輸入
                        NeonOutlinedTextField(
                            value = ageYearsInput,
                            onValueChange = { v ->
                                // 只允許數字，長度限制 3 位
                                if (v.all { it.isDigit() } && v.length <= 3) {
                                    ageYearsInput = v
                                }
                            },
                            label = "年齡",
                            modifier = Modifier.weight(1f)
                        )

                        NeonOutlinedTextField(
                            value = daysPerWeek.toString(),
                            onValueChange = { v ->
                                val n = v.filter { it.isDigit() }.toIntOrNull()
                                if (n != null) daysPerWeek = n.coerceIn(1, 6)
                            },
                            label = "每週天數",
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(Modifier.height(20.dp))

                    // 訓練程度
                    Text("訓練程度", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                    Spacer(Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ExperienceLevel.entries.forEach { level ->
                            NeonToggleOption(
                                text = level.label,
                                isSelected = experience == level,
                                onClick = { experience = level },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // Error Message
                    errorText?.let {
                        Spacer(Modifier.height(12.dp))
                        Text(it, color = Color(0xFFFF6B6B), style = MaterialTheme.typography.bodySmall)
                    }

                    Spacer(Modifier.height(20.dp))

                    // Action Buttons
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Button(
                            onClick = {
                                if (BuildConfig.GEMINI_API_KEY.isBlank()) {
                                    errorText = "未設定 API Key"
                                    return@Button
                                }
                                // ★ 修改 4: 增加防呆檢查，確保年齡有輸入
                                if (ageYearsInput.isBlank()) {
                                    errorText = "請輸入年齡"
                                    return@Button
                                }

                                loading = true
                                errorText = null
                                previewPlan = null

                                scope.launch {
                                    try {
                                        val prompt = buildPrompt()
                                        val res = GeminiClient.ask(prompt)
                                        if (res.isSuccess) {
                                            val raw = res.getOrNull().orEmpty()
                                            val parsed = GeminiTrainingPlanParser.parseJson(raw)
                                            previewPlan = GeminiTrainingPlanParser.toTrainingPlan(parsed)
                                            showSaveDialog = true
                                        } else {
                                            errorText = "AI 服務呼叫失敗: ${res.exceptionOrNull()?.message}"
                                        }
                                    } catch (e: Exception) {
                                        errorText = "產生失敗：${e.message}"
                                    } finally {
                                        loading = false
                                    }
                                }
                            },
                            enabled = !loading,
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                                .neonGlowBorder(cornerRadius = 12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = TechColors.NeonBlue.copy(alpha = 0.2f),
                                contentColor = TechColors.NeonBlue
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            if (loading) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp), color = TechColors.NeonBlue, strokeWidth = 2.dp)
                                Spacer(Modifier.width(8.dp))
                                Text("AI 思考中...")
                            } else {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("生成計畫", fontWeight = FontWeight.Bold)
                            }
                        }

                        OutlinedButton(
                            onClick = { previewPlan = null; errorText = null },
                            enabled = !loading,
                            modifier = Modifier.height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White.copy(alpha = 0.7f)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.3f))
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null)
                        }
                    }
                }
            }


            // Preview Section
            previewPlan?.let { plan ->
                Spacer(Modifier.height(16.dp))
                Text("預覽計畫", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                Spacer(Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .glassEffect(cornerRadius = 24.dp)
                        .padding(20.dp)
                ) {
                    Column {
                        Text(plan.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = TechColors.NeonBlue)
                        Spacer(Modifier.height(12.dp))

                        val parsedDays = remember(plan) { GeminiTrainingPlanParser.extractDaysOrNull(plan) }

                        if (!parsedDays.isNullOrEmpty()) {
                            parsedDays.forEach { day ->
                                Surface(
                                    color = Color.White.copy(alpha = 0.05f),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(
                                            text = "Day ${day.day} ${day.focus?.let { "• $it" } ?: ""}",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                        Spacer(Modifier.height(8.dp))
                                        day.exercises.forEach { e ->
                                            Row(verticalAlignment = Alignment.Top) {
                                                Text("• ", color = TechColors.NeonBlue)
                                                Column {
                                                    Text(e.name, color = Color.White.copy(alpha = 0.9f), style = MaterialTheme.typography.bodyMedium)
                                                    Text(
                                                        "${e.sets}組 x ${e.reps}下 ${e.weight?.let { "@ ${it.toInt()}kg" } ?: ""}",
                                                        color = Color.White.copy(alpha = 0.5f),
                                                        style = MaterialTheme.typography.bodySmall
                                                    )
                                                }
                                            }
                                            Spacer(Modifier.height(6.dp))
                                        }
                                    }
                                }
                            }
                        } else {
                            plan.exercises.take(12).forEach { e ->
                                Text("• ${e.name}", color = Color.White.copy(alpha = 0.8f))
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(40.dp))
        }
    }

    // Save Dialog
    if (showSaveDialog && previewPlan != null) {
        AlertDialog(
            onDismissRequest = { showSaveDialog = false },
            containerColor = TechColors.DarkBlue.copy(alpha = 0.95f),
            titleContentColor = TechColors.NeonBlue,
            textContentColor = Color.White,
            title = { Text("儲存訓練計畫") },
            text = { Text("將『${previewPlan?.name}』儲存到我的訓練計畫？") },
            confirmButton = {
                Button(
                    onClick = {
                        previewPlan?.let { repository.addPlan(it) }
                        showSaveDialog = false
                        onDone()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = TechColors.NeonBlue,
                        contentColor = Color.Black
                    )
                ) { Text("儲存") }
            },
            dismissButton = {
                TextButton(
                    onClick = { showSaveDialog = false },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color.White.copy(alpha = 0.7f))
                ) { Text("取消") }
            }
        )
    }
}

// ... (Helper Components 保持不變) ...
@Composable
private fun NeonToggleOption(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(40.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(
                if (isSelected) TechColors.NeonBlue.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.05f)
            )
            .border(
                1.dp,
                if (isSelected) TechColors.NeonBlue else Color.Transparent,
                RoundedCornerShape(10.dp)
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (isSelected) TechColors.NeonBlue else Color.White.copy(alpha = 0.6f),
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun NeonStatChip(label: String, value: String) {
    Surface(
        color = TechColors.NeonBlue.copy(alpha = 0.1f),
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, TechColors.NeonBlue.copy(alpha = 0.3f))
    ) {
        Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
            Text(label, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.7f))
            Spacer(Modifier.width(4.dp))
            Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = TechColors.NeonBlue)
        }
    }
}

@Composable
private fun NeonOutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            focusedBorderColor = TechColors.NeonBlue,
            unfocusedBorderColor = TechColors.NeonBlue.copy(alpha = 0.5f),
            focusedLabelColor = TechColors.NeonBlue,
            unfocusedLabelColor = Color.White.copy(alpha = 0.7f),
            cursorColor = TechColors.NeonBlue,
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent
        )
    )
}