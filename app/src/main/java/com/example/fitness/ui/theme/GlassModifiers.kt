package com.example.fitness.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

/**
 * 高科技玻璃感 UI 顏色常量
 */
object TechColors {
    // 深色科技感漸層背景
    val DarkBlue = Color(0xFF0A0E27)      // 深藍
    val DeepPurple = Color(0xFF1A0B2E)     // 深紫
    
    // 霓虹藍色
    val NeonBlue = Color(0xFF00F0FF)       // 霓虹藍
    val NeonBlueDim = Color(0xFF00F0FF).copy(alpha = 0.6f)  // 半透明霓虹藍
    
    // 霓虹綠色
    val NeonGreen = Color(0xFF00FF88)      // 霓虹綠
    
    // 霓虹紫色
    val NeonPurple = Color(0xFFB826FF)     // 霓虹紫
    
    // 霓虹橙色
    val NeonOrange = Color(0xFFFF6B35)    // 霓虹橙
    
    // 霓虹黃色
    val NeonYellow = Color(0xFFFFD700)    // 霓虹黃
    
    // 霓虹紅色
    val NeonRed = Color(0xFFFF1744)       // 霓虹紅
    
    // 霓虹灰色
    val NeonGray = Color(0xFF9E9E9E)      // 霓虹灰
    
    // 玻璃感背景漸層
    val GlassGradient = listOf(
        Color.White.copy(0.1f),
        Color.White.copy(0.05f)
    )
}

/**
 * 玻璃感 UI 標準修飾符
 * 
 * 提供統一的玻璃質感效果，包含：
 * - 陰影效果
 * - 半透明邊框
 * - 漸層背景
 * - 圓角裁剪
 */
fun Modifier.glassEffect(cornerRadius: Dp = 24.dp) = this.then(
    Modifier
        .shadow(8.dp, RoundedCornerShape(cornerRadius))
        .border(1.dp, Color.White.copy(0.3f), RoundedCornerShape(cornerRadius))
        .background(
            Brush.linearGradient(TechColors.GlassGradient),
            shape = RoundedCornerShape(cornerRadius)
        )
        .clip(RoundedCornerShape(cornerRadius))
)

/**
 * Theme-aware 的玻璃效果：用 MaterialTheme.colorScheme 產生更一致的結果。
 *
 * 注意：Modifier 擴充本身不需要是 @Composable。
 * 呼叫端可傳入 MaterialTheme.colorScheme 的顏色（在 composable 內取得）。
 */
fun Modifier.glassEffectThemed(
    scheme: ColorScheme,
    cornerRadius: Dp = 24.dp
) = this.then(
    Modifier
        .shadow(8.dp, RoundedCornerShape(cornerRadius))
        .border(
            1.dp,
            scheme.outline.copy(alpha = 0.6f),
            RoundedCornerShape(cornerRadius)
        )
        .background(
            Brush.linearGradient(
                listOf(
                    scheme.surface.copy(alpha = 0.18f),
                    scheme.surface.copy(alpha = 0.08f)
                )
            ),
            shape = RoundedCornerShape(cornerRadius)
        )
        .clip(RoundedCornerShape(cornerRadius))
)

/**
 * 霓虹邊框發光效果修飾符
 * 用於重要操作按鈕
 */
fun Modifier.neonGlowBorder(
    cornerRadius: Dp = 16.dp,
    borderWidth: Dp = 2.dp,
    glowColor: Color = TechColors.NeonBlue
) = this.then(
    Modifier
        .shadow(
            elevation = 12.dp,
            shape = RoundedCornerShape(cornerRadius),
            ambientColor = glowColor.copy(alpha = 0.5f),
            spotColor = glowColor.copy(alpha = 0.8f)
        )
        .border(
            width = borderWidth,
            brush = Brush.linearGradient(
                listOf(
                    glowColor.copy(alpha = 0.8f),
                    glowColor.copy(alpha = 0.4f),
                    glowColor.copy(alpha = 0.8f)
                )
            ),
            shape = RoundedCornerShape(cornerRadius)
        )
)

/**
 * Theme-aware 的霓虹邊框：預設用 colorScheme.primary。
 *
 * 注意：這個函數不是 composable，呼叫端請傳入 MaterialTheme.colorScheme。
 */
fun Modifier.neonGlowBorderThemed(
    scheme: ColorScheme,
    cornerRadius: Dp = 16.dp,
    borderWidth: Dp = 2.dp,
    glowColor: Color = scheme.primary
) = neonGlowBorder(
    cornerRadius = cornerRadius,
    borderWidth = borderWidth,
    glowColor = glowColor
)

/**
 * 便利版本：在 Composable 中直接呼叫即可（自動讀取 MaterialTheme.colorScheme）。
 */
@Composable
fun Modifier.glassEffectThemedM3(
    cornerRadius: Dp = 24.dp
) = glassEffectThemed(
    scheme = MaterialTheme.colorScheme,
    cornerRadius = cornerRadius
)

/**
 * 便利版本：在 Composable 中直接呼叫即可（自動讀取 MaterialTheme.colorScheme）。
 */
@Composable
fun Modifier.neonGlowBorderThemedM3(
    cornerRadius: Dp = 16.dp,
    borderWidth: Dp = 2.dp,
    glowColor: Color = MaterialTheme.colorScheme.primary
) = neonGlowBorderThemed(
    scheme = MaterialTheme.colorScheme,
    cornerRadius = cornerRadius,
    borderWidth = borderWidth,
    glowColor = glowColor
)
