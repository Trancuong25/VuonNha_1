package com.example.vuonnha.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color

private val Light = lightColorScheme(
    primary = Green40, onPrimary = Paper, primaryContainer = Green90, onPrimaryContainer = Green10,
    secondary = Sage40, secondaryContainer = Sage90,
    tertiary = Water40, tertiaryContainer = Water90,
    background = Paper, surface = Color.White, surfaceVariant = PaperVariant,
    surfaceContainerLowest = Color.White, surfaceContainerLow = Color(0xFFF9FBF8), surfaceContainer = Paper,
    surfaceContainerHigh = PaperVariant, surfaceContainerHighest = PaperVariant, surfaceTint = Green40,
    onBackground = Color(0xFF23372E), onSurface = Color(0xFF23372E),
    outline = Color(0xFF829B8E), outlineVariant = Color(0xFFDCE7E0),
)
private val Dark = darkColorScheme(
    primary = Green80, onPrimary = Green20, primaryContainer = Green30, onPrimaryContainer = Green90,
    secondary = Sage80, secondaryContainer = Sage30,
    tertiary = Water80, tertiaryContainer = Water30,
    background = Ink, surface = Color(0xFF19251E), surfaceVariant = InkVariant,
    surfaceContainerLowest = Ink, surfaceContainerLow = Color(0xFF19251E), surfaceContainer = Color(0xFF1D2B22),
    surfaceContainerHigh = InkVariant, surfaceContainerHighest = Color(0xFF2D3D33), surfaceTint = Green80,
    onSurface = Color(0xFFE4ECE5), onSurfaceVariant = Color(0xFFB9C9BD),
    outline = Color(0xFF82978A), outlineVariant = Color(0xFF35493B),
)

private val AppShapes = Shapes(
    small = RoundedCornerShape(12.dp), medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp), extraLarge = RoundedCornerShape(28.dp),
)

// Tắt dynamic color: giữ nhận diện thương hiệu nhất quán trên mọi máy thay vì đổi màu theo hình nền.
@Composable
fun VuonNhaTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (darkTheme) Dark else Light,
        typography = Typography, shapes = AppShapes, content = content
    )
}
