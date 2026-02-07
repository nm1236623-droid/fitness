package com.example.fitness.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import com.example.fitness.designsystem.DsProvideTokens

// Enhanced color palette for professional app design
private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF6366F1),        // Indigo - modern & professional
    secondary = Color(0xFF8B5CF6),      // Purple - energetic
    tertiary = Color(0xFF06B6D4),       // Cyan - fresh
    background = Color(0xFF0F172A),     // Slate 900 - deep & elegant
    surface = Color(0xFF1E293B),        // Slate 800 - layered depth
    surfaceVariant = Color(0xFF334155), // Slate 700
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color(0xFFF1F5F9),   // Slate 100
    onSurface = Color(0xFFF1F5F9),
    primaryContainer = Color(0xFF312E81), // Indigo 900
    onPrimaryContainer = Color(0xFFE0E7FF), // Indigo 100
    secondaryContainer = Color(0xFF581C87), // Purple 900
    onSecondaryContainer = Color(0xFFF3E8FF), // Purple 100
    outline = Color(0xFF475569),        // Slate 600
    error = Color(0xFFEF4444),          // Red 500
    errorContainer = Color(0xFF7F1D1D), // Red 900
    onErrorContainer = Color(0xFFFEE2E2) // Red 100
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF6366F1),        // Indigo
    secondary = Color(0xFF8B5CF6),      // Purple
    tertiary = Color(0xFF06B6D4),       // Cyan
    background = Color(0xFFF8FAFC),     // Slate 50
    surface = Color.White,
    surfaceVariant = Color(0xFFF1F5F9), // Slate 100
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color(0xFF0F172A),   // Slate 900
    onSurface = Color(0xFF1E293B),      // Slate 800
    primaryContainer = Color(0xFFE0E7FF), // Indigo 100
    onPrimaryContainer = Color(0xFF312E81), // Indigo 900
    secondaryContainer = Color(0xFFF3E8FF), // Purple 100
    onSecondaryContainer = Color(0xFF581C87), // Purple 900
    outline = Color(0xFFCBD5E1),        // Slate 300
    error = Color(0xFFEF4444),          // Red 500
    errorContainer = Color(0xFFFEE2E2), // Red 100
    onErrorContainer = Color(0xFF7F1D1D) // Red 900
)

@Composable
fun FitnessTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = com.example.fitness.ui.theme.Shapes
    ) {
        DsProvideTokens(darkTheme = darkTheme) {
            content()
        }
    }
}

@Composable
fun FitnessTheme(
    themeMode: ThemeMode,
    colorScheme: ColorScheme,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current

    val darkTheme = when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.AUTO -> isSystemInDarkTheme()
    }

    val baseScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val primary = remember(colorScheme) { colorScheme.toPrimaryColor().copy(alpha = 1f) }

    // 用選到的 primary 生成「一整套」配色，確保全 App 的按鈕/Tab/Icon/Progress 都一致
    val appliedScheme = remember(baseScheme, primary) {
        baseScheme.withPrimary(primary = primary)
    }

    MaterialTheme(
        colorScheme = appliedScheme,
        typography = Typography,
        shapes = com.example.fitness.ui.theme.Shapes
    ) {
        DsProvideTokens(darkTheme = darkTheme) {
            content()
        }
    }
}

/**
 * 以 primary 為主，推導出容器色與 on* 色，避免 UI 出現低對比/看不清楚。
 * 規則：
 * - onPrimary/onPrimaryContainer 依 luminance 自動黑/白
 * - container 用 primary 的 alpha 混合 (簡單且穩定)
 */
private fun androidx.compose.material3.ColorScheme.withPrimary(
    primary: Color,
): androidx.compose.material3.ColorScheme {
    val onPrimary = if (primary.luminance() > 0.5f) Color.Black else Color.White

    val primaryContainer = primary.copy(alpha = 0.22f).compositeOver(background)
    val onPrimaryContainer = if (primaryContainer.luminance() > 0.5f) Color.Black else Color.White

    return copy(
        primary = primary,
        onPrimary = onPrimary,
        primaryContainer = primaryContainer,
        onPrimaryContainer = onPrimaryContainer,
        // secondary/tertiary 先沿用 base，以免整站全變同一顏色導致層級不清
        // 如果你想更強烈的「全站同色」，可再把 secondary/tertiary 也指向 primary。
    )
}

/**
 * 將設定中的 ColorScheme 映射為主色。
 * 這裡與 ThemeManager.getPrimaryColor() 保持一致，但不依賴 ThemeManager 的 mutable state。
 */
private fun ColorScheme.toPrimaryColor(): Color = when (this) {
    ColorScheme.NEON_BLUE -> TechColors.NeonBlue
    ColorScheme.PURPLE -> Color(0xFF9C27B0)
    ColorScheme.GREEN -> Color(0xFF4CAF50)
    ColorScheme.ORANGE -> Color(0xFFFF9800)
    ColorScheme.RED -> Color(0xFFF44336)
    ColorScheme.CUSTOM -> TechColors.NeonBlue
}
