@file:Suppress("UNUSED_VALUE", "UNUSED_VARIABLE", "DEPRECATION", "UNUSED_PARAMETER")

package com.example.fitness.ui

import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle

// --- 機能模組引用 ---
import com.example.fitness.activity.ActivityLogRepository
import com.example.fitness.auth.SessionManager
import com.example.fitness.auth.SessionRepository
import com.example.fitness.chat.ChatScreen
import com.example.fitness.coach.UserRole
import com.example.fitness.data.DietRecord
import com.example.fitness.data.FirebaseDietRecordRepository // 引用 FirebaseRepo
import com.example.fitness.data.FirebaseTrainingRecordRepository
import com.example.fitness.data.TrainingPlan
import com.example.fitness.data.TrainingPlanRepository
import com.example.fitness.firebase.AuthRepository
import com.example.fitness.food.FoodRecognitionScreenOptimized
import com.example.fitness.inbody.InBodyRepository
import com.example.fitness.inbody.InBodyScreen
import com.example.fitness.network.GeminiClient
import com.example.fitness.running.CardioRepository
import com.example.fitness.running.CardioScreen
import com.example.fitness.ui.template.WorkoutTemplateMarketScreen
import com.example.fitness.ui.theme.TechColors
import com.example.fitness.ui.theme.glassEffect
import com.example.fitness.ui.theme.neonGlowBorder
import com.example.fitness.user.UserProfileRepository
import com.example.fitness.user.UserRoleProfileRepository
import com.example.fitness.ui.settings.ProfileBasicSettingsScreen
import com.example.fitness.ui.settings.WaterGoalDialog
import com.example.fitness.ui.settings.WaterIntakeCard
import com.example.fitness.nutrition.AdvancedNutritionRepository
import com.example.fitness.user.FirebaseUserProfileFirestoreRepository
import com.example.fitness.user.UserProfile

// --- Google & Firebase 引用 ---
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.firebase.auth.FirebaseAuth

// --- AI 引用 ---
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.generationConfig

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.UUID
import kotlin.math.max
import kotlin.math.roundToInt

// ★★★ Gemini AI 服務 (用於飲食分析) ★★★
object GeminiFoodAIService {
    private const val API_KEY = "AIzaSyC2KzjIq3UgiSYLvB5SGu2Eb0e6QdjgVBw"
    private const val MODEL_NAME = "gemini-2.5-flash"

    suspend fun analyze(text: String): DietRecord? {
        try {
            val generativeModel = GenerativeModel(
                modelName = MODEL_NAME,
                apiKey = API_KEY,
                generationConfig = generationConfig {
                    responseMimeType = "application/json"
                }
            )

            val prompt = """
                你是一位專業營養師。請分析這段食物描述："$text"。
                請回傳一個純 JSON 物件，必須嚴格遵守以下格式與型別：
                {
                  "food_name": "食物名稱(String)",
                  "calories": 熱量大卡(Int),
                  "protein": 蛋白質克數(Double),
                  "carbs": 碳水化合物克數(Double),
                  "fat": 脂肪克數(Double),
                  "meal_type": "早餐" 或 "午餐" 或 "晚餐" 或 "點心"
                }
                數值請依據台灣常見份量估算。如果無法辨識，回傳 null。
            """.trimIndent()

            val response = generativeModel.generateContent(prompt)
            val responseText = response.text ?: return null

            val jsonString = extractJson(responseText) ?: return null
            val json = JSONObject(jsonString)

            return DietRecord(
                id = UUID.randomUUID().toString(),
                userId = FirebaseAuth.getInstance().currentUser?.uid ?: "",
                date = LocalDate.now(),
                timestamp = System.currentTimeMillis(),
                mealType = json.optString("meal_type", "點心"),
                foodName = json.optString("food_name", text),
                calories = json.optInt("calories", 0),
                proteinG = json.optDouble("protein", 0.0),
                carbsG = json.optDouble("carbs", 0.0),
                fatG = json.optDouble("fat", 0.0)
            )
        } catch (e: Exception) {
            Log.e("GeminiAI", "Analysis failed: ${e.message}")
            return null
        }
    }

    private fun extractJson(text: String): String? {
        val startIndex = text.indexOf('{')
        val endIndex = text.lastIndexOf('}')
        if (startIndex != -1 && endIndex != -1 && endIndex > startIndex) {
            return text.substring(startIndex, endIndex + 1)
        }
        return null
    }
}

// ★★★ 熱量計算工具 ★★★
object CalorieCalculator {
    fun calculateWorkoutCalories(durationMinutes: Long, bodyWeightKg: Float, intensityFactor: Double = 5.0): Double {
        val durationHours = durationMinutes / 60.0
        return intensityFactor * bodyWeightKg * durationHours
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Suppress("ASSIGNED_BUT_NEVER_ACCESSED_VARIABLE", "UNUSED_VALUE", "UNUSED_VARIABLE")
@Composable
fun HomeScreen(
    repository: TrainingPlanRepository,
    activityRepository: ActivityLogRepository,
    modifier: Modifier = Modifier,
    trainingRecordRepository: FirebaseTrainingRecordRepository = remember { FirebaseTrainingRecordRepository() },
    isPro: Boolean = false,
    onShowPaywall: () -> Unit = {}
) {
    var selected by remember { mutableStateOf("plans") }
    var workoutInitialDialog by remember { mutableStateOf<String?>(null) }
    var selectedWorkoutPart by remember { mutableStateOf<String?>(null) }
    var selectedWorkoutPlanId by remember { mutableStateOf<String?>(null) }

    val ctx = LocalContext.current
    val appCtx = remember(ctx) { ctx.applicationContext }

    val authRepository = remember { AuthRepository() }
    val sessionManager = remember(appCtx) { SessionManager(authRepository, SessionRepository(appCtx)) }

    val inBodyRepo = remember(appCtx) { InBodyRepository(appCtx) }
    val cardioRepo = remember { CardioRepository() }
    val userProfileRepo = remember { UserProfileRepository(appCtx) }
    val roleRepo = remember { UserRoleProfileRepository() }

    var myRole by remember { mutableStateOf(UserRole.TRAINEE) }

    val firestoreProfileRepo = remember { FirebaseUserProfileFirestoreRepository() }
    val lifecycleOwner = LocalLifecycleOwner.current
    val remoteProfile by remember(firestoreProfileRepo, lifecycleOwner) {
        firestoreProfileRepo.observeMyProfile()
    }.collectAsStateWithLifecycle(initialValue = UserProfile(), lifecycleOwner = lifecycleOwner)

    var myNickname by remember { mutableStateOf("Trainee") }

    LaunchedEffect(Unit) {
        myRole = roleRepo.getMyRole().getOrDefault(UserRole.TRAINEE)
        val fbUser = FirebaseAuth.getInstance().currentUser
        if (fbUser != null && !fbUser.displayName.isNullOrBlank()) {
            myNickname = fbUser.displayName!!
        }
        if (myNickname == "Trainee" && myRole == UserRole.COACH) {
            myNickname = "Coach"
        }
    }

    val firebaseAuth = remember { FirebaseAuth.getInstance() }
    val currentUser = firebaseAuth.currentUser

    val displayNickname = remember(remoteProfile.nickname, myNickname, currentUser?.displayName) {
        when {
            remoteProfile.nickname.isNotBlank() -> remoteProfile.nickname
            !currentUser?.displayName.isNullOrBlank() -> currentUser?.displayName!!
            else -> myNickname
        }
    }

    val userWeight by userProfileRepo.weightKg.collectAsState(initial = 70f)
    val userHeight by userProfileRepo.heightCm.collectAsState(initial = 170f)
    val userTdee by userProfileRepo.tdee.collectAsState(initial = 2000f)
    val proteinGoal by userProfileRepo.proteinGoalGrams.collectAsState(initial = 120f)

    var showProfileDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val selectTab: (String) -> Unit = { tab -> selected = tab }
    val cardShape = MaterialTheme.shapes.extraLarge

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            if (selected == "plans") {
                CenterAlignedTopAppBar(
                    modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars),
                    navigationIcon = {
                        IconButton(onClick = {
                            scope.launch {
                                sessionManager.logout()
                                val intent = Intent(ctx, com.example.fitness.MainActivity::class.java).apply {
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                }
                                ctx.startActivity(intent)
                            }
                        }) {
                            Icon(Icons.Default.ExitToApp, contentDescription = "登出")
                        }
                    },
                    title = {
                        Text("Fitness", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, letterSpacing = 0.4.sp)
                    },
                    actions = {
                        IconButton(onClick = { selected = "profile_basic_settings" }) {
                            Icon(imageVector = Icons.Outlined.Settings, contentDescription = "設定")
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = MaterialTheme.colorScheme.primary,
                        navigationIconContentColor = Color.White,
                        actionIconContentColor = Color.White
                    )
                )
            }
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    // 讓「廣告 + 底部按鈕」整組避開系統導覽列/手勢列，避免被吃掉或間距怪
                    .navigationBarsPadding()
            ) {
                BannerAd(
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                NavigationBar(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp),
                    containerColor = Color.Transparent
                ) {
                    val items = listOf(
                        Triple("plans", Icons.Default.Home, "首頁"),
                        Triple("calendar", Icons.Default.DateRange, "日曆"),
                        Triple("food", Icons.Default.PhotoCamera, "辨識"),
                        Triple("chat", Icons.Filled.ChatBubbleOutline, "AI"),
                        Triple("analytics", Icons.Filled.Analytics, "分析")
                    )
                    items.forEach { (route, icon, label) ->
                        NavigationBarItem(
                            selected = selected == route,
                            onClick = { selectTab(route) },
                            icon = { Icon(icon, contentDescription = label) },
                            label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                            alwaysShowLabel = false,
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                unselectedIconColor = Color.Gray,
                                unselectedTextColor = Color.Gray,
                                indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                            )
                        )
                    }
                }
            }
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(TechColors.DarkBlue, TechColors.DeepPurple)))
        ) {
            Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    when (selected) {
                        "plans" -> {
                            HomeDashboardContent(
                                activityRepository, userProfileRepo, displayNickname, myRole, userTdee, proteinGoal,
                                selectTab, showProfileDialog, { showProfileDialog = it },
                                userWeight, userHeight, snackbarHostState, scope, cardShape
                            )
                        }
                        "workout" -> {
                            Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
                                ScreenHeader(title = "Workout", subtitle = "選擇部位、查看/開始計畫", onBack = { selectTab("plans") })
                                Spacer(modifier = Modifier.height(12.dp))

                                SectionCard(title = "快速操作") {
                                    PrimaryActionRow(
                                        primaryText = "選擇訓練部位", primaryIcon = Icons.Default.FitnessCenter,
                                        onPrimary = { selected = "workout_select" },
                                        secondaryText = "建立新計畫", secondaryIcon = Icons.Default.Add,
                                        onSecondary = { selected = "create_workout" }
                                    )
                                }
                                Spacer(modifier = Modifier.height(12.dp))

                                SectionCard(title = "教練 / 學員") {
                                    val isCoach = myRole == UserRole.COACH
                                    PrimaryActionRow(
                                        primaryText = "教練中心", primaryIcon = Icons.Default.Upload,
                                        onPrimary = {
                                            if (isPro) selected = "coach_center" else onShowPaywall()
                                        },
                                        secondaryText = "學員中心", secondaryIcon = Icons.Default.Download,
                                        onSecondary = { selected = "trainee_import" },
                                        primaryEnabled = isCoach,
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    if (!isCoach) {
                                        Text("教練中心僅提供給教練帳號使用。", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    } else {
                                        Text("提醒：教練發佈的計畫只會出現在『學員中心』頁面。", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                                Spacer(modifier = Modifier.height(12.dp))

                                SectionCard(title = "我的訓練計畫") {
                                    TrainingPlanScreen(
                                        repository = repository,
                                        activityRepository = activityRepository,
                                        trainingRecordRepository = trainingRecordRepository,
                                        modifier = Modifier.fillMaxSize(),
                                        onRecordCreated = { selectTab("activity") },
                                        initialDialog = workoutInitialDialog,
                                        onInitialDialogConsumed = { workoutInitialDialog = null }
                                    )
                                }
                            }
                        }
                        "workout_select" -> PartSelectionScreen(onSelect = { selectedWorkoutPart = it; selected = "workout_part_actions" }, onCancel = { selected = "workout" })
                        "workout_part_actions" -> { workoutInitialDialog = selectedWorkoutPart; selected = "workout" }
                        "bodyphotos" -> BodyPhotoScreen(onDone = { selectTab("plans") })
                        "create_workout" -> WorkoutCreateScreen(repository = repository, onDone = { selectTab("plans") })
                        "activity" -> ActivityLogScreen(
                            activityRepository = activityRepository,
                            onBack = { selectTab("workout") }
                        )
                        "food" -> FoodRecognitionScreenOptimized(activityRepository = activityRepository)
                        "foodlog" -> FoodLogScreen(onBack = { selectTab("workout") })
                        "calendar" -> CalendarScreen(
                            repository = repository,
                            onOpenPlan = {
                                selectedWorkoutPlanId = it
                                selected = "workout_plan_detail"
                            }
                        )
                        "settings" -> SettingsScreen(onDone = { selectTab("workout") })
                        "chat" -> ChatScreen()
                        "inbody" -> InBodyScreen(repository = inBodyRepo, onDone = { selectTab("workout") })
                        "cardio" -> CardioScreen(repository = cardioRepo, activityRepository = activityRepository, userWeightKg = userWeight, onDone = { selectTab("workout") })
                        "analytics" -> AnalyticsScreen(repository, activityRepository, inBodyRepo, onOpenPartAnalysis = { selected = "part_analysis" }, onOpenInBodyAnalysis = { selected = "inbody_analysis" })
                        "coach_center" -> com.example.fitness.coach.ui.CoachCenterScreen(authRepository = com.example.fitness.coach.CoachAuthRepository(LocalContext.current.applicationContext), trainingPlanRepository = repository, onBack = { selected = "workout" }, onNavigateToCreatePlan = { selected = "coach_plan_create" })
                        "coach_plan_create" -> {
                            val appCtx = LocalContext.current.applicationContext
                            val repo = remember(appCtx) { com.example.fitness.coach.local.CoachLocalPlanRepository(appCtx) }
                            com.example.fitness.coach.ui.CoachPlanCreateScreen(onBack = { selected = "coach_center" }, onSave = { scope.launch { repo.add(it) } }, coachLocalRepo = repo)
                        }
                        "trainee_import" -> com.example.fitness.coach.ui.TraineeImportScreen(trainingPlanRepository = repository, onBack = { selected = "workout" })
                        "inbody_analysis" -> com.example.fitness.inbody.InBodyAnalysisScreen(inBodyRepository = inBodyRepo, onBack = { selected = "analytics" })
                        "part_analysis" -> PartAnalysisScreen(repository = repository, activityRepository = activityRepository, inBodyRepo = inBodyRepo, onDone = { selected = "analytics" })
                        "workout_plan_detail" -> {
                            val pid = selectedWorkoutPlanId
                            if (!pid.isNullOrBlank()) {
                                TrainingPlanDetailScreen(repository = repository, planId = pid, onBack = { selected = "calendar" })
                            } else {
                                SideEffect { selected = "calendar" }
                            }
                        }
                        "social" -> com.example.fitness.ui.social.SocialCenterScreen(onBack = { selectTab("plans") })
                        "messages" -> com.example.fitness.ui.messaging.ChatRoomListScreen(
                            isCoach = myRole == com.example.fitness.coach.UserRole.COACH,
                            onChatRoomSelected = { chatRoomId -> selected = "chat_room" },
                            onBack = { selectTab("plans") }
                        )
                        "templates" -> WorkoutTemplateMarketScreen(onBack = { selectTab("plans") })
                        "advanced_settings" -> com.example.fitness.ui.settings.AdvancedSettingsScreen(onBack = { selectTab("workout") })
                        "training_history" -> TrainingHistoryScreen(
                            trainingRecordRepository = trainingRecordRepository,
                            modifier = Modifier.fillMaxSize(),
                            onBack = { selectTab("workout") }
                        )
                        "profile_basic_settings" -> ProfileBasicSettingsScreen(
                            onBack = { selectTab("workout") }
                        )
                        else -> TrainingPlanScreen(repository = repository, activityRepository = activityRepository, trainingRecordRepository = trainingRecordRepository, onRecordCreated = { selectTab("activity") })
                    }
                }
            }
        }
    }
}

// ------------------------------------
// Helper Functions & UI Components
// ------------------------------------

@Composable
fun HomeDashboardContent(
    activityRepository: ActivityLogRepository,
    userProfileRepo: UserProfileRepository,
    displayNickname: String,
    myRole: UserRole,
    userTdee: Float,
    proteinGoal: Float,
    selectTab: (String) -> Unit,
    showProfileDialog: Boolean,
    onShowProfileDialogChange: (Boolean) -> Unit,
    userWeight: Float,
    userHeight: Float,
    snackbarHostState: SnackbarHostState,
    scope: kotlinx.coroutines.CoroutineScope,
    cardShape: androidx.compose.ui.graphics.Shape
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val dietRecords by FirebaseDietRecordRepository.records.collectAsStateWithLifecycle(initialValue = emptyList(), lifecycleOwner = lifecycleOwner)
    val trainingLogs by activityRepository.logs.collectAsStateWithLifecycle(initialValue = emptyList(), lifecycleOwner = lifecycleOwner)
    val today = LocalDate.now(ZoneId.systemDefault())

    // 篩選今日飲食和運動記錄
    val todayFoodLogs = remember(dietRecords, today) {
        dietRecords.filter { it.date == today }
    }

    val todayExerciseLogs = remember(trainingLogs, today) {
        trainingLogs.filter {
            !it.type.startsWith("飲食") &&
                    it.start.atZone(ZoneId.systemDefault()).toLocalDate() == today
        }
    }

    // 計算營養攝取
    val todayConsumedCalories = todayFoodLogs.sumOf { it.calories.toDouble() }
    val todayProtein = todayFoodLogs.sumOf { it.proteinG ?: 0.0 }
    val todayCarbs = todayFoodLogs.sumOf { it.carbsG ?: 0.0 }
    val todayFat = todayFoodLogs.sumOf { it.fatG ?: 0.0 }

    // 計算運動消耗
    val todayExerciseCalories = todayExerciseLogs.sumOf { it.calories ?: 0.0 }

    // 計算淨卡路里和剩餘量
    val netCalories = todayConsumedCalories - todayExerciseCalories
    val remainingCalories = userTdee - netCalories

    // 計算營養目標
    val carbsGoal = (userTdee * 0.5f / 4f)
    val fatGoal = (userTdee * 0.25f / 9f)

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Box(modifier = Modifier.fillMaxWidth().glassEffect(cornerRadius = 24.dp).padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(56.dp).border(2.dp, MaterialTheme.colorScheme.primary, CircleShape).clip(CircleShape).background(Color.White.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) {
                        Text(displayNickname.take(1).uppercase(), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Hi, $displayNickname", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                        Text(if (myRole == UserRole.COACH) "Coach" else "Trainee", style = MaterialTheme.typography.labelMedium, color = Color.White.copy(alpha = 0.7f))
                    }
                }
            }
        }
        item {
            Box(modifier = Modifier.fillMaxWidth().glassEffect(cornerRadius = 24.dp).padding(16.dp)) {
                Column {
                    Text("訓練摘要", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White)
                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        SummaryStat("今日攝取", "%.0f".format(todayConsumedCalories), "kcal", Icons.Default.Restaurant, modifier = Modifier.weight(1f))
                        SummaryStat("今日消耗", "%.0f".format(todayExerciseCalories), "kcal", Icons.Default.LocalFireDepartment, modifier = Modifier.weight(1f))
                    }
                }
            }
        }
        item {
            NutritionProgressCard(
                consumedCalories = todayConsumedCalories.toFloat(),
                targetCalories = userTdee,
                consumedProtein = todayProtein.toFloat(),
                targetProtein = proteinGoal,
                consumedCarbs = todayCarbs.toFloat(),
                targetCarbs = carbsGoal,
                consumedFat = todayFat.toFloat(),
                targetFat = fatGoal,
                remainingCalories = remainingCalories.toFloat()
            )
        }
        item {
            val shortcuts = listOf(
                ShortcutItem("Workout", Icons.Default.FitnessCenter) { selectTab("workout") },
                ShortcutItem("訓練紀錄", Icons.Default.History) { selectTab("training_history") },
                ShortcutItem("飲食紀錄", Icons.Default.Restaurant) { selectTab("foodlog") },
                ShortcutItem("社交中心", Icons.Default.People) { selectTab("social") },
                ShortcutItem("訊息", Icons.Default.Message) { selectTab("messages") },
                ShortcutItem("訓練模板", Icons.Default.LibraryBooks) { selectTab("templates") },
                ShortcutItem("Cardio", Icons.Default.DirectionsRun) { selectTab("cardio") },
                ShortcutItem("InBody", Icons.Default.MonitorWeight) { selectTab("inbody") },
                ShortcutItem("體態相簿", Icons.Default.PhotoLibrary) { selectTab("bodyphotos") },
                ShortcutItem("進階設定", Icons.Default.Tune) { selectTab("advanced_settings") },
            )
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                shortcuts.chunked(2).forEach { row ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        row.forEach { item -> ShortcutCard(item = item, modifier = Modifier.weight(1f), shape = cardShape) }
                        if (row.size == 1) Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }

    if (showProfileDialog) {
        AlertDialog(
            onDismissRequest = { onShowProfileDialogChange(false) },
            confirmButton = { Button(onClick = { onShowProfileDialogChange(false) }) { Text("確定") } },
            text = { Text("Profile Settings Placeholder") }
        )
    }
}

@Composable
private fun DateSwitcherRow(
    date: LocalDate,
    onDateChange: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.glassEffect(16.dp).padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = { onDateChange(date.minusDays(1)) }) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = Color.White)
        }
        Text(
            text = date.format(DateTimeFormatter.ofPattern("yyyy/MM/dd")),
            style = MaterialTheme.typography.titleMedium,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
        IconButton(onClick = { onDateChange(date.plusDays(1)) }) {
            Icon(Icons.Default.ArrowForward, contentDescription = null, tint = Color.White)
        }
    }
}

@Composable
fun FoodLogScreen(
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null
) {
    // 單一資料源：Firestore snapshot
    val dietRepository = FirebaseDietRecordRepository

    var currentDate by remember { mutableStateOf(LocalDate.now()) }
    val lifecycleOwner = LocalLifecycleOwner.current
    val records by dietRepository.records.collectAsStateWithLifecycle(
        initialValue = emptyList(),
        lifecycleOwner = lifecycleOwner
    )

    val filteredRecords = remember(records, currentDate) {
        records.filter { it.date == currentDate }
    }

    var showAddDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    fun deleteRecord(record: DietRecord) {
        scope.launch {
            dietRepository.deleteRecord(record.id)
        }
    }

    val totalCalories = filteredRecords.sumOf { it.calories }
    val totalProtein = filteredRecords.sumOf { it.proteinG ?: 0.0 }
    val totalCarbs = filteredRecords.sumOf { it.carbsG ?: 0.0 }
    val totalFat = filteredRecords.sumOf { it.fatG ?: 0.0 }

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
            // 1. Title Bar
            Box(modifier = Modifier.fillMaxWidth().glassEffect(cornerRadius = 24.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (onBack != null) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                        }
                    } else {
                        Spacer(Modifier.width(8.dp))
                    }
                    Column(modifier = Modifier.weight(1f).padding(horizontal = 4.dp)) {
                        Text(
                            "飲食日誌",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = TechColors.NeonBlue
                        )
                        Text(
                            "AI 智慧營養分析",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(0.8f)
                        )
                    }
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Food", tint = TechColors.NeonBlue)
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // 2. Date Selector
            DateSwitcherRow(
                date = currentDate,
                onDateChange = { newDate: LocalDate -> currentDate = newDate },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))

            // 3. Summary Card
            Box(modifier = Modifier.fillMaxWidth().glassEffect(16.dp).padding(16.dp)) {
                Column {
                    Text("當日總結", style = MaterialTheme.typography.titleMedium, color = TechColors.NeonBlue)
                    Spacer(Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "$totalCalories",
                                style = MaterialTheme.typography.headlineSmall,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                            Text("Kcal", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "${"%.1f".format(totalProtein)}g",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color(0xFF4ECDC4)
                            )
                            Text("蛋白質", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "${"%.1f".format(totalFat)}g",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color(0xFF95E77E)
                            )
                            Text("脂肪", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "${"%.1f".format(totalCarbs)}g",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color(0xFFA78BFA)
                            )
                            Text("碳水", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // 4. List of Records
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 20.dp)
            ) {
                val grouped = filteredRecords.groupBy { it.mealType }
                val mealOrder = listOf("早餐", "午餐", "晚餐", "點心")

                mealOrder.forEach { type ->
                    val mealRecords = grouped[type]
                    if (!mealRecords.isNullOrEmpty()) {
                        item {
                            Text(
                                type,
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White.copy(0.8f),
                                modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                            )
                        }
                        items(mealRecords) { record ->
                            DietRecordCard(record, onDelete = { deleteRecord(record) })
                        }
                    }
                }

                if (filteredRecords.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(30.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("尚無紀錄，點擊右上角 + 新增", color = Color.Gray)
                        }
                    }
                }
            }
        }

        if (showAddDialog) {
            SmartFoodEntryDialog(
                currentUserId = currentUserId,
                onDismiss = { showAddDialog = false },
                onConfirm = { newRecord ->
                    scope.launch {
                        // date 由本頁選擇的日曆決定；userId 由 FirebaseDietRecordRepository 強制補齊
                        val recordToSave = newRecord.copy(date = currentDate)
                        dietRepository.addRecord(recordToSave)
                        showAddDialog = false
                    }
                }
            )
        }
    }
}

@Composable
fun SmartFoodEntryDialog(
    currentUserId: String,
    onDismiss: () -> Unit,
    onConfirm: (DietRecord) -> Unit
) {
    var foodDescription by remember { mutableStateOf("") }
    var isAnalyzing by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    var foodName by remember { mutableStateOf("") }
    var calories by remember { mutableStateOf("") }
    var protein by remember { mutableStateOf("") }
    var carbs by remember { mutableStateOf("") }
    var fat by remember { mutableStateOf("") }
    var selectedMealType by remember { mutableStateOf("早餐") }

    val mealTypes = listOf("早餐", "午餐", "晚餐", "點心")
    val scope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = TechColors.DarkBlue.copy(alpha = 0.95f),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.AutoAwesome, null, tint = TechColors.NeonBlue)
                Spacer(Modifier.width(8.dp))
                Text("AI 飲食分析", color = TechColors.NeonBlue, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {

                // AI Input Area
                OutlinedTextField(
                    value = foodDescription,
                    onValueChange = { foodDescription = it },
                    label = { Text("今天吃了什麼？ (例如：雞腿便當)", color = Color.White.copy(0.7f)) },
                    singleLine = false,
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = TechColors.NeonBlue, unfocusedBorderColor = Color.White.copy(0.3f)),
                    trailingIcon = {
                        if (isAnalyzing) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = TechColors.NeonBlue, strokeWidth = 2.dp)
                        } else {
                            IconButton(onClick = {
                                if (foodDescription.isNotBlank()) {
                                    isAnalyzing = true
                                    errorMessage = null
                                    scope.launch {
                                        val result = GeminiFoodAIService.analyze(foodDescription)
                                        if (result != null) {
                                            foodName = result.foodName
                                            calories = result.calories.toString()
                                            protein = (result.proteinG ?: 0.0).toString()
                                            carbs = (result.carbsG ?: 0.0).toString()
                                            fat = (result.fatG ?: 0.0).toString()
                                            selectedMealType = result.mealType
                                        } else {
                                            errorMessage = "分析失敗。請檢查網路連線或 API Key，或者試著簡化描述。"
                                        }
                                        isAnalyzing = false
                                    }
                                }
                            }) {
                                Icon(Icons.Default.Send, null, tint = TechColors.NeonBlue)
                            }
                        }
                    }
                )

                if (errorMessage != null) {
                    Text(text = errorMessage!!, color = Color(0xFFFF6B6B), style = MaterialTheme.typography.bodySmall)
                }

                HorizontalDivider(color = Color.White.copy(0.1f))

                Text("營養素分析 (可手動修正)", color = Color.White.copy(0.7f), fontSize = 12.sp)

                OutlinedTextField(
                    value = foodName,
                    onValueChange = { foodName = it },
                    label = { Text("食物名稱") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = Color.Gray, unfocusedBorderColor = Color.White.copy(0.2f))
                )

                OutlinedTextField(
                    value = calories,
                    onValueChange = { if (it.all { char -> char.isDigit() }) calories = it },
                    label = { Text("熱量 (kcal)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TechColors.NeonBlue, unfocusedTextColor = TechColors.NeonBlue, focusedBorderColor = TechColors.NeonBlue, unfocusedBorderColor = Color.White.copy(0.3f))
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val nutrientColors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = Color.Gray, unfocusedBorderColor = Color.White.copy(0.2f))
                    val decimalKeyboard = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    OutlinedTextField(value = protein, onValueChange = { protein = it }, label = { Text("P(蛋)") }, modifier = Modifier.weight(1f), colors = nutrientColors, keyboardOptions = decimalKeyboard)
                    OutlinedTextField(value = carbs, onValueChange = { carbs = it }, label = { Text("C(碳)") }, modifier = Modifier.weight(1f), colors = nutrientColors, keyboardOptions = decimalKeyboard)
                    OutlinedTextField(value = fat, onValueChange = { fat = it }, label = { Text("F(脂)") }, modifier = Modifier.weight(1f), colors = nutrientColors, keyboardOptions = decimalKeyboard)
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    mealTypes.forEach { type ->
                        FilterChip(
                            selected = selectedMealType == type,
                            onClick = { selectedMealType = type },
                            label = { Text(type) },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = TechColors.NeonBlue.copy(0.3f), selectedLabelColor = TechColors.NeonBlue, labelColor = Color.White)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (foodName.isNotBlank() && calories.isNotBlank()) {
                        val record = DietRecord(
                            id = UUID.randomUUID().toString(),
                            userId = currentUserId,
                            date = LocalDate.now(),
                            timestamp = System.currentTimeMillis(),
                            mealType = selectedMealType,
                            foodName = foodName,
                            calories = calories.toIntOrNull() ?: 0,
                            proteinG = protein.toDoubleOrNull() ?: 0.0,
                            carbsG = carbs.toDoubleOrNull() ?: 0.0,
                            fatG = fat.toDoubleOrNull() ?: 0.0
                        )
                        onConfirm(record)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = TechColors.NeonBlue)
            ) { Text("儲存", color = Color.Black, fontWeight = FontWeight.Bold) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消", color = Color.White.copy(0.7f)) } }
    )
}

@Composable
fun DietRecordCard(record: DietRecord, onDelete: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxWidth().glassEffect(12.dp).padding(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(record.foodName, style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("${record.calories} kcal", color = TechColors.NeonBlue, style = MaterialTheme.typography.bodyMedium)
                    if (record.proteinG != null) Text("P: ${"%.1f".format(record.proteinG)}g", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
                }
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFFF6B6B))
            }
        }
    }
}


// ------------------------------------
// UI Components
// ------------------------------------

@Composable
fun ScreenHeader(title: String, subtitle: String? = null, onBack: (() -> Unit)? = null) {
    Box(modifier = Modifier.fillMaxWidth().glassEffect(cornerRadius = 24.dp)) {
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
            if (onBack != null) IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White) } else Spacer(Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f).padding(horizontal = 4.dp)) {
                Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = TechColors.NeonBlue)
                if (!subtitle.isNullOrBlank()) Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(0.8f))
            }
        }
    }
}

@Composable
fun SectionCard(title: String, modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Box(modifier = modifier.fillMaxWidth().glassEffect(cornerRadius = 24.dp)) {
        Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
            Spacer(Modifier.height(10.dp))
            content()
        }
    }
}

@Composable
fun PrimaryActionRow(primaryText: String, primaryIcon: ImageVector, onPrimary: () -> Unit, secondaryText: String, secondaryIcon: ImageVector, onSecondary: () -> Unit, primaryEnabled: Boolean = true) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Button(onClick = onPrimary, enabled = primaryEnabled, modifier = Modifier.weight(1f).height(50.dp).then(if(primaryEnabled) Modifier.neonGlowBorder(16.dp) else Modifier), shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(containerColor = if(primaryEnabled) TechColors.NeonBlue.copy(0.2f) else Color.Gray.copy(0.3f))) {
            Icon(primaryIcon, null, tint = if(primaryEnabled) TechColors.NeonBlue else Color.Gray); Spacer(Modifier.width(8.dp)); Text(primaryText, color = if(primaryEnabled) TechColors.NeonBlue else Color.Gray)
        }
        OutlinedButton(onClick = onSecondary, modifier = Modifier.weight(1f).height(50.dp).neonGlowBorder(16.dp), shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.outlinedButtonColors(contentColor = TechColors.NeonBlue)) {
            Icon(secondaryIcon, null); Spacer(Modifier.width(8.dp)); Text(secondaryText)
        }
    }
}

@Composable
private fun BannerAd(
    modifier: Modifier = Modifier
) {
    AndroidView(modifier = modifier.fillMaxWidth().height(50.dp), factory = { ctx -> AdView(ctx).apply { setAdSize(AdSize.BANNER); adUnitId = "ca-app-pub-3940256099942544/6300978111"; loadAd(AdRequest.Builder().build()) } })
}

@Composable
fun SummaryStat(title: String, value: String, unit: String, icon: ImageVector? = null, modifier: Modifier = Modifier) {
    Box(modifier = modifier.glassEffect(20.dp)) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (icon != null) Icon(icon, null, tint = Color.White, modifier = Modifier.size(18.dp))
                Text(title, style = MaterialTheme.typography.labelLarge, color = Color.White.copy(0.9f))
            }
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold, color = TechColors.NeonBlue)
                Spacer(Modifier.width(4.dp)); Text(unit, style = MaterialTheme.typography.labelMedium, color = Color.White.copy(0.8f))
            }
        }
    }
}

@Composable
fun EmptyState(title: String, subtitle: String) {
    Column(modifier = Modifier.fillMaxWidth().glassEffect(20.dp).padding(14.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TechColors.NeonBlue)
        Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(0.7f))
    }
}

data class ShortcutItem(val title: String, val icon: ImageVector, val onClick: () -> Unit)

@Composable
fun ShortcutCard(item: ShortcutItem, modifier: Modifier = Modifier, shape: androidx.compose.ui.graphics.Shape) {
    Box(modifier = modifier.glassEffect(20.dp).clickable(onClick = item.onClick)) {
        Row(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(modifier = Modifier.size(36.dp).clip(RoundedCornerShape(12.dp)).background(TechColors.NeonBlue.copy(0.2f)), contentAlignment = Alignment.Center) { Icon(item.icon, null, tint = TechColors.NeonBlue) }
            Column { Text(item.title, style = MaterialTheme.typography.titleMedium, color = Color.White); Text("快速開啟", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(0.7f)) }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FoodLogChipsRow(content: @Composable () -> Unit) { FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) { content() } }

@Composable
fun FoodLogEntryChipsRow(p: Double?, c: Double?, f: Double?) {
    FoodLogChipsRow {
        AssistChip(onClick = {}, label = { Text("P ${p?.let { "%.1f".format(it) } ?: "-"} g") })
        AssistChip(onClick = {}, label = { Text("C ${c?.let { "%.1f".format(it) } ?: "-"} g") })
        AssistChip(onClick = {}, label = { Text("F ${f?.let { "%.1f".format(it) } ?: "-"} g") })
    }
}

@Composable
fun TrainingPlanDetailScreen(repository: TrainingPlanRepository, planId: String, onBack: () -> Unit) {
    val plan by repository.getPlanFlow(planId).collectAsState(initial = null)
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        ScreenHeader(title = "計畫內容", subtitle = plan?.name, onBack = onBack)
        Spacer(Modifier.height(12.dp))
        val p = plan
        if (p == null) { EmptyState("找不到計畫", "此計畫可能已被刪除或尚未同步完成。"); return }
        Card(modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.extraLarge, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                Text(p.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                if (p.exercises.isEmpty()) Text("此計畫沒有動作。", style = MaterialTheme.typography.bodyMedium)
                else p.exercises.forEach { e -> Text("• ${e.name}", style = MaterialTheme.typography.bodyMedium); Spacer(Modifier.height(6.dp)) }
            }
        }
    }
}

@Composable
fun ActivityLogScreen(activityRepository: ActivityLogRepository, modifier: Modifier = Modifier, onBack: (() -> Unit)?) {
    val logs by activityRepository.logs.collectAsState()
    Column(modifier = modifier.fillMaxSize().padding(12.dp)) {
        ScreenHeader("訓練紀錄", "瀏覽歷史", onBack)
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(logs) { rec ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(rec.type, fontWeight = FontWeight.Bold)
                        Text(rec.start.toString())
                    }
                }
            }
        }
    }
}

@Composable
fun NutritionProgressCard(
    consumedCalories: Float,
    targetCalories: Float,
    consumedProtein: Float,
    targetProtein: Float,
    consumedCarbs: Float = 0f,
    targetCarbs: Float = 0f,
    consumedFat: Float = 0f,
    targetFat: Float = 0f,
    remainingCalories: Float
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val nutritionRepo = remember { AdvancedNutritionRepository(context) }
    val waterGoal by nutritionRepo.waterGoal.collectAsState(initial = 2000.0)
    var showWaterGoalDialog by remember { mutableStateOf(false) }

    // 改成即時監聽 Firestore，避免「按了沒反應」
    val todayWaterIntake by nutritionRepo.observeTodayWaterIntake().collectAsState(initial = 0.0)

    // 移除一次性讀取
    // LaunchedEffect(Unit) { todayWaterIntake = nutritionRepo.getTodayWaterIntake() }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .glassEffect(cornerRadius = 16.dp)
            .padding(12.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "營養攝取",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 14.sp
                )
                Icon(
                    imageVector = Icons.Default.Restaurant,
                    contentDescription = null,
                    tint = TechColors.NeonBlue,
                    modifier = Modifier.size(18.dp)
                )
            }

            NutritionProgressBar(
                label = "卡路里",
                current = consumedCalories,
                target = targetCalories,
                unit = "kcal",
                icon = Icons.Default.LocalFireDepartment,
                color = TechColors.NeonBlue,
                showRemaining = true
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 4.dp),
                color = Color.White.copy(alpha = 0.1f),
                thickness = 0.5.dp
            )

            Text(
                "營養素",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White.copy(0.8f),
                fontSize = 11.sp
            )

            NutritionProgressBar(
                label = "蛋白質",
                current = consumedProtein,
                target = targetProtein,
                unit = "g",
                icon = Icons.Default.FitnessCenter,
                color = Color(0xFF00E676),
                showRemaining = false
            )

            if (targetCarbs > 0) {
                NutritionProgressBar(
                    label = "碳水化合物",
                    current = consumedCarbs,
                    target = targetCarbs,
                    unit = "g",
                    icon = Icons.Default.Grain,
                    color = Color(0xFFFFD600),
                    showRemaining = false
                )
            }

            if (targetFat > 0) {
                NutritionProgressBar(
                    label = "脂肪",
                    current = consumedFat,
                    target = targetFat,
                    unit = "g",
                    icon = Icons.Default.WaterDrop,
                    color = Color(0xFFFF6B6B),
                    showRemaining = false
                )
            }

            // ===== 水分追蹤（從進階設定移到營養攝取） =====
            Spacer(Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "水分追蹤",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(0.8f),
                    fontSize = 11.sp
                )
                TextButton(onClick = { showWaterGoalDialog = true }, contentPadding = PaddingValues(0.dp)) {
                    Text("設定目標", color = TechColors.NeonBlue, fontSize = 11.sp)
                }
            }

            WaterIntakeCard(
                consumed = todayWaterIntake.toInt(),
                goal = waterGoal.toInt(),
                onAddWater = { amountMl ->
                    scope.launch {
                        val result = nutritionRepo.logWaterIntake(amountMl.toDouble())
                        if (result.isFailure) {
                            Toast.makeText(
                                context,
                                "水分紀錄失敗：${result.exceptionOrNull()?.message ?: "未知錯誤"}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        // 成功時不需要手動加，listener 會自動推最新總量
                    }
                }
            )

            if (remainingCalories > 0) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(TechColors.NeonBlue.copy(alpha = 0.1f))
                        .border(0.5.dp, TechColors.NeonBlue.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                        .padding(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            tint = TechColors.NeonBlue,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            "還可 ${remainingCalories.toInt()} kcal",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White,
                            fontSize = 10.sp
                        )
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFFF6B6B).copy(alpha = 0.1f))
                        .border(0.5.dp, Color(0xFFFF6B6B).copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                        .padding(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = null,
                            tint = Color(0xFFFF6B6B),
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            "超標 ${(-remainingCalories).toInt()} kcal",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }
    }

    if (showWaterGoalDialog) {
        WaterGoalDialog(
            currentGoal = waterGoal.toInt(),
            onGoalSet = { goal ->
                scope.launch {
                    nutritionRepo.setWaterGoal(goal.toDouble())
                    showWaterGoalDialog = false
                }
            },
            onDismiss = { showWaterGoalDialog = false }
        )
    }
}

@Composable
fun NutritionProgressBar(
    label: String,
    current: Float,
    target: Float,
    unit: String,
    icon: ImageVector,
    color: Color,
    showRemaining: Boolean = true
) {
    val progress = (current / target).coerceIn(0f, 1f)
    val percentage = (progress * 100).toInt()

    val animatedProgress by androidx.compose.animation.core.animateFloatAsState(
        targetValue = progress,
        animationSpec = androidx.compose.animation.core.spring(
            dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy,
            stiffness = androidx.compose.animation.core.Spring.StiffnessLow
        ),
        label = "progress_animation"
    )

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    label,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = Color.White,
                    fontSize = 11.sp
                )
            }

            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    current.toInt().toString(),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = color,
                    fontSize = 12.sp
                )
                Text(
                    "/${target.toInt()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 10.sp
                )
                Text(
                    unit,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 9.sp,
                    modifier = Modifier.padding(start = 2.dp)
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(Color.White.copy(alpha = 0.1f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedProgress)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(6.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                color.copy(alpha = 0.7f),
                                color,
                                color.copy(alpha = 0.9f)
                            )
                        )
                    )
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedProgress)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(6.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.25f),
                                Color.Transparent
                            )
                        )
                    )
            )
        }
    }
}
