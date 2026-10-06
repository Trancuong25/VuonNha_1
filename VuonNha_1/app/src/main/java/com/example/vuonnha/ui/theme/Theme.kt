package com.example.vuonnha.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val Light = lightColorScheme(
    primary = Green40, onPrimary = Paper, primaryContainer = Green90, onPrimaryContainer = Green10,
    secondary = Sage40, secondaryContainer = Sage90,
    tertiary = Water40, tertiaryContainer = Water90,
    background = Paper, surface = Paper, surfaceVariant = PaperVariant,
)
private val Dark = darkColorScheme(
    primary = Green80, onPrimary = Green20, primaryContainer = Green30, onPrimaryContainer = Green90,
    secondary = Sage80, secondaryContainer = Sage30,
    tertiary = Water80, tertiaryContainer = Water30,
    background = Ink, surface = Ink, surfaceVariant = InkVariant,
)

private val AppShapes = Shapes(
    small = RoundedCornerShape(12.dp), medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(28.dp), extraLarge = RoundedCornerShape(32.dp),
)

// Tắt dynamic color: giữ nhận diện thương hiệu nhất quán trên mọi máy thay vì đổi màu theo hình nền.
@Composable
fun VuonNhaTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (darkTheme) Dark else Light,
        typography = Typography, shapes = AppShapes, content = content
    )
}
