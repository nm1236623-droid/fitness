package com.example.fitness.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.fitness.ui.theme.TechColors
import com.example.fitness.ui.theme.glassEffect
import com.example.fitness.user.FirebaseUserProfileFirestoreRepository
import com.example.fitness.user.UserProfile
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileBasicSettingsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    repo: FirebaseUserProfileFirestoreRepository = remember { FirebaseUserProfileFirestoreRepository() },
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val profileFlow = remember(repo) {
        repo.observeMyProfile()
            .catch { emit(UserProfile()) }
    }
    val remoteProfile by profileFlow.collectAsStateWithLifecycle(initialValue = UserProfile())

    // 表單狀態（與遠端同步）
    var nickname by remember(remoteProfile.nickname) { mutableStateOf(remoteProfile.nickname) }
    var ageText by remember(remoteProfile.age) { mutableStateOf(remoteProfile.age.takeIf { it > 0 }?.toString().orEmpty()) }
    var weightText by remember(remoteProfile.weightKg) { mutableStateOf(remoteProfile.weightKg.toString()) }
    var heightText by remember(remoteProfile.heightCm) { mutableStateOf(remoteProfile.heightCm.toString()) }
    var tdeeText by remember(remoteProfile.tdee) { mutableStateOf(remoteProfile.tdee.toString()) }
    var proteinGoalText by remember(remoteProfile.proteinGoalGrams) { mutableStateOf(remoteProfile.proteinGoalGrams.toString()) }

    fun validateAndBuild(): UserProfile? {
        val age = ageText.trim().toIntOrNull() ?: 0
        val weight = weightText.trim().toFloatOrNull() ?: return null
        val height = heightText.trim().toFloatOrNull() ?: return null
        val tdee = tdeeText.trim().toIntOrNull() ?: return null
        val protein = proteinGoalText.trim().toFloatOrNull() ?: return null

        if (weight <= 0f || height <= 0f || tdee <= 0 || protein < 0f) return null

        return UserProfile(
            nickname = nickname.trim(),
            age = age,
            weightKg = weight,
            heightCm = height,
            tdee = tdee,
            proteinGoalGrams = protein,
            avatarUri = remoteProfile.avatarUri,
        )
    }

    // 高對比（深色背景）用色
    val highContrastText = Color.White
    val highContrastLabel = Color.White.copy(alpha = 0.92f)
    val highContrastHint = Color.White.copy(alpha = 0.70f)
    val highContrastIcon = Color.White
    val highContrastBorder = Color.White.copy(alpha = 0.55f)
    val highContrastFocusedBorder = TechColors.NeonBlue
    val highContrastCursor = TechColors.NeonBlue

    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = highContrastText,
        unfocusedTextColor = highContrastText,
        disabledTextColor = highContrastHint,
        focusedContainerColor = Color.Transparent,
        unfocusedContainerColor = Color.Transparent,
        disabledContainerColor = Color.Transparent,
        cursorColor = highContrastCursor,
        focusedBorderColor = highContrastFocusedBorder,
        unfocusedBorderColor = highContrastBorder,
        focusedLabelColor = highContrastFocusedBorder,
        unfocusedLabelColor = highContrastLabel,
        focusedLeadingIconColor = highContrastIcon,
        unfocusedLeadingIconColor = highContrastIcon,
        focusedTrailingIconColor = highContrastIcon,
        unfocusedTrailingIconColor = highContrastIcon,
        focusedPlaceholderColor = highContrastHint,
        unfocusedPlaceholderColor = highContrastHint,
    )

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color.Transparent,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("個人資料基本設定", color = highContrastText, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = highContrastIcon)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(TechColors.DarkBlue, TechColors.DeepPurple)))
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(modifier = Modifier.fillMaxWidth().glassEffect(20.dp).padding(16.dp)) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("基本資料", style = MaterialTheme.typography.titleMedium, color = highContrastText, fontWeight = FontWeight.Bold)

                    OutlinedTextField(
                        value = nickname,
                        onValueChange = { nickname = it },
                        label = { Text("暱稱", color = highContrastLabel) },
                        leadingIcon = { Icon(Icons.Default.Person, null, tint = highContrastIcon) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = textFieldColors,
                        textStyle = MaterialTheme.typography.bodyLarge.copy(color = highContrastText),
                    )

                    OutlinedTextField(
                        value = ageText,
                        onValueChange = { ageText = it.filter { ch -> ch.isDigit() }.take(3) },
                        label = { Text("年齡", color = highContrastLabel) },
                        leadingIcon = { Icon(Icons.Default.Cake, null, tint = highContrastIcon) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = textFieldColors,
                        textStyle = MaterialTheme.typography.bodyLarge.copy(color = highContrastText),
                    )
                }
            }

            Box(modifier = Modifier.fillMaxWidth().glassEffect(20.dp).padding(16.dp)) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("身體數據", style = MaterialTheme.typography.titleMedium, color = highContrastText, fontWeight = FontWeight.Bold)

                    OutlinedTextField(
                        value = weightText,
                        onValueChange = { weightText = it },
                        label = { Text("體重 (kg)", color = highContrastLabel) },
                        leadingIcon = { Icon(Icons.Default.MonitorWeight, null, tint = highContrastIcon) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = textFieldColors,
                        textStyle = MaterialTheme.typography.bodyLarge.copy(color = highContrastText),
                    )

                    OutlinedTextField(
                        value = heightText,
                        onValueChange = { heightText = it },
                        label = { Text("身高 (cm)", color = highContrastLabel) },
                        leadingIcon = { Icon(Icons.Default.Height, null, tint = highContrastIcon) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = textFieldColors,
                        textStyle = MaterialTheme.typography.bodyLarge.copy(color = highContrastText),
                    )
                }
            }

            Box(modifier = Modifier.fillMaxWidth().glassEffect(20.dp).padding(16.dp)) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("營養目標", style = MaterialTheme.typography.titleMedium, color = highContrastText, fontWeight = FontWeight.Bold)

                    OutlinedTextField(
                        value = tdeeText,
                        onValueChange = { tdeeText = it.filter { ch -> ch.isDigit() }.take(5) },
                        label = { Text("每日熱量目標 (kcal)", color = highContrastLabel) },
                        leadingIcon = { Icon(Icons.Default.LocalFireDepartment, null, tint = highContrastIcon) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = textFieldColors,
                        textStyle = MaterialTheme.typography.bodyLarge.copy(color = highContrastText),
                    )

                    OutlinedTextField(
                        value = proteinGoalText,
                        onValueChange = { proteinGoalText = it },
                        label = { Text("蛋白質目標 (g)", color = highContrastLabel) },
                        leadingIcon = { Icon(Icons.Default.FitnessCenter, null, tint = highContrastIcon) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = textFieldColors,
                        textStyle = MaterialTheme.typography.bodyLarge.copy(color = highContrastText),
                    )
                }
            }

            Button(
                onClick = {
                    val newProfile = validateAndBuild()
                    if (newProfile == null) {
                        scope.launch { snackbarHostState.showSnackbar("請檢查輸入格式") }
                        return@Button
                    }

                    scope.launch {
                        val result = repo.saveMyProfile(newProfile)
                        if (result.isSuccess) {
                            snackbarHostState.showSnackbar("已儲存")
                        } else {
                            snackbarHostState.showSnackbar("儲存失敗：${result.exceptionOrNull()?.localizedMessage}")
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) {
                Icon(Icons.Default.Save, null)
                Spacer(Modifier.width(8.dp))
                Text("儲存")
            }

            Text(
                text = "提示：此頁面資料會保存到 Firestore 的 user_profiles/{uid}，並供首頁計算 TDEE / 目標使用。",
                style = MaterialTheme.typography.bodySmall,
                color = highContrastHint,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(Modifier.height(24.dp))
        }
    }
}
