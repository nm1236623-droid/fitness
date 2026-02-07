package com.example.fitness.ui.settings

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.fitness.localization.LocaleManager
import com.example.fitness.localization.SupportedLanguage
import com.example.fitness.ui.theme.ColorScheme
import com.example.fitness.ui.theme.TechColors
import com.example.fitness.ui.theme.ThemeManager
import com.example.fitness.ui.theme.ThemeMode
import com.example.fitness.ui.theme.glassEffect
import com.example.fitness.ui.theme.neonGlowBorder
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdvancedSettingsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val themeManager = remember { ThemeManager.getInstance(context) }
    val localeManager = remember { LocaleManager.getInstance(context) }
    val snackbarHostState = remember { SnackbarHostState() }

    val currentTheme by themeManager.themeMode.collectAsState(initial = ThemeMode.AUTO)
    val currentColorScheme by themeManager.colorScheme.collectAsState(initial = ColorScheme.NEON_BLUE)
    val currentLanguage by localeManager.currentLanguage.collectAsState(initial = SupportedLanguage.CHINESE_TRADITIONAL)

    var showThemeDialog by remember { mutableStateOf(false) }
    var showColorSchemeDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color.Transparent
    ) { paddingValues ->
        val cs = MaterialTheme.colorScheme
        val bg = Brush.verticalGradient(listOf(cs.background, cs.surface))

        Box(
            modifier = modifier
                .fillMaxSize()
                .background(bg)
                .padding(paddingValues)
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
                        .glassEffect(20.dp)
                        .padding(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBack, null, tint = cs.primary)
                        }
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text(
                                "進階設定",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = cs.primary
                            )
                            Text(
                                "個性化您的體驗",
                                style = MaterialTheme.typography.bodySmall,
                                color = cs.onBackground.copy(alpha = 0.7f)
                            )
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))

                // Theme Section
                SettingSection("外觀設定") {
                    SettingItem(
                        icon = Icons.Default.Palette,
                        title = "主題模式",
                        subtitle = currentTheme.toDisplayName(),
                        onClick = { showThemeDialog = true }
                    )

                    SettingItem(
                        icon = Icons.Default.ColorLens,
                        title = "顏色風格",
                        subtitle = currentColorScheme.toDisplayName(),
                        onClick = { showColorSchemeDialog = true }
                    )
                }

                Spacer(Modifier.height(16.dp))

                // Language Section
                SettingSection("語言設定") {
                    SettingItem(
                        icon = Icons.Default.Language,
                        title = "應用程式語言",
                        subtitle = currentLanguage.displayName,
                        onClick = { showLanguageDialog = true }
                    )
                }

                Spacer(Modifier.height(32.dp))
            }

            // Dialogs
            if (showThemeDialog) {
                ThemeSelectionDialog(
                    currentTheme = currentTheme,
                    onThemeSelected = { theme ->
                        scope.launch {
                            themeManager.setThemeMode(theme)
                            showThemeDialog = false
                        }
                    },
                    onDismiss = { showThemeDialog = false }
                )
            }

            if (showColorSchemeDialog) {
                ColorSchemeSelectionDialog(
                    currentScheme = currentColorScheme,
                    onSchemeSelected = { scheme ->
                        scope.launch {
                            themeManager.setColorScheme(scheme)
                            showColorSchemeDialog = false
                        }
                    },
                    onDismiss = { showColorSchemeDialog = false }
                )
            }

            if (showLanguageDialog) {
                LanguageSelectionDialog(
                    currentLanguage = currentLanguage,
                    onLanguageSelected = { language ->
                        scope.launch {
                            localeManager.setLanguage(language)
                            showLanguageDialog = false
                            snackbarHostState.showSnackbar("語言已更新，請重啟應用程式")
                        }
                    },
                    onDismiss = { showLanguageDialog = false }
                )
            }
        }
    }
}

private fun ThemeMode.toDisplayName(): String = when (this) {
    ThemeMode.LIGHT -> "LIGHT（淺色）"
    ThemeMode.DARK -> "DARK（深色）"
    ThemeMode.AUTO -> "SYSTEM（跟隨系統）"
}

private fun ColorScheme.toDisplayName(): String = when (this) {
    ColorScheme.NEON_BLUE -> "霓虹藍"
    ColorScheme.PURPLE -> "紫色"
    ColorScheme.GREEN -> "綠色"
    ColorScheme.ORANGE -> "橘色"
    ColorScheme.RED -> "紅色"
    ColorScheme.CUSTOM -> "自訂"
}

@Composable
fun SettingSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .glassEffect(24.dp)
            .padding(20.dp)
    ) {
        val cs = MaterialTheme.colorScheme
        Column {
            Text(
                title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = cs.onSurface
            )
            Spacer(Modifier.height(16.dp))
            content()
        }
    }
}

@Composable
fun SettingItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    val cs = MaterialTheme.colorScheme
    val titleColor = cs.onSurface
    val subtitleColor = cs.onSurface.copy(alpha = 0.7f)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            null,
            tint = cs.primary,
            modifier = Modifier.size(24.dp)
        )

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                style = MaterialTheme.typography.bodyLarge,
                color = titleColor,
                fontWeight = FontWeight.Medium
            )
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = subtitleColor
            )
        }

        Icon(
            Icons.Default.ChevronRight,
            null,
            tint = cs.onSurface.copy(alpha = 0.5f)
        )
    }
}

@Composable
fun ThemeSelectionDialog(
    currentTheme: ThemeMode,
    onThemeSelected: (ThemeMode) -> Unit,
    onDismiss: () -> Unit
) {
    val cs = MaterialTheme.colorScheme
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = cs.surface,
        title = {
            Text("選擇主題", fontWeight = FontWeight.Bold, color = cs.primary)
        },
        text = {
            Column {
                ThemeMode.values().forEach { theme ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onThemeSelected(theme) }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = currentTheme == theme,
                            onClick = { onThemeSelected(theme) },
                            colors = RadioButtonDefaults.colors(selectedColor = cs.primary)
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            theme.toDisplayName(),
                            color = cs.onSurface,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消", color = cs.onSurface.copy(alpha = 0.8f))
            }
        }
    )
}

@Composable
fun ColorSchemeSelectionDialog(
    currentScheme: ColorScheme,
    onSchemeSelected: (ColorScheme) -> Unit,
    onDismiss: () -> Unit
) {
    val cs = MaterialTheme.colorScheme
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = cs.surface,
        title = {
            Text("選擇顏色風格", fontWeight = FontWeight.Bold, color = cs.primary)
        },
        text = {
            Column {
                ColorScheme.values().forEach { scheme ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onSchemeSelected(scheme) }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Color preview
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(getColorForScheme(scheme))
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            scheme.toDisplayName(),
                            color = cs.onSurface,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.weight(1f)
                        )
                        if (currentScheme == scheme) {
                            Icon(
                                Icons.Default.Check,
                                null,
                                tint = cs.primary
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消", color = cs.onSurface.copy(alpha = 0.8f))
            }
        }
    )
}

@Composable
fun LanguageSelectionDialog(
    currentLanguage: SupportedLanguage,
    onLanguageSelected: (SupportedLanguage) -> Unit,
    onDismiss: () -> Unit
) {
    val cs = MaterialTheme.colorScheme
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = cs.surface,
        title = {
            Text("選擇語言", fontWeight = FontWeight.Bold, color = cs.primary)
        },
        text = {
            Column {
                SupportedLanguage.values().forEach { language ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onLanguageSelected(language) }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = currentLanguage == language,
                            onClick = { onLanguageSelected(language) },
                            colors = RadioButtonDefaults.colors(selectedColor = cs.primary)
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            language.displayName,
                            color = cs.onSurface,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消", color = cs.onSurface.copy(alpha = 0.8f))
            }
        }
    )
}

fun getColorForScheme(scheme: ColorScheme): Color {
    return when (scheme) {
        ColorScheme.NEON_BLUE -> Color(0xFF00D4FF)
        ColorScheme.PURPLE -> Color(0xFF9C27B0)
        ColorScheme.GREEN -> Color(0xFF4CAF50)
        ColorScheme.ORANGE -> Color(0xFFFF9800)
        ColorScheme.RED -> Color(0xFFF44336)
        ColorScheme.CUSTOM -> Color(0xFF00D4FF)
    }
}
