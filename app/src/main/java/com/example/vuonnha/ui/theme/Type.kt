package com.example.vuonnha.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.sp

private val base = Typography()

val Typography = Typography(
    headlineMedium = base.headlineMedium.copy(fontFamily = FontFamily.SansSerif, fontSize = 28.sp, lineHeight = 34.sp, fontWeight = FontWeight.Medium, letterSpacing = (-0.5).sp),
    headlineSmall = base.headlineSmall.copy(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Medium, letterSpacing = (-0.3).sp),
    titleLarge = base.titleLarge.copy(fontWeight = FontWeight.Medium),
    titleMedium = base.titleMedium.copy(fontWeight = FontWeight.Medium),
    bodyLarge = base.bodyLarge.copy(lineHeight = 24.sp),
    bodyMedium = base.bodyMedium.copy(lineHeight = 21.sp),
    bodySmall = base.bodySmall.copy(fontSize = 13.sp, lineHeight = 19.sp),
    labelLarge = base.labelLarge.copy(fontSize = 14.sp, fontWeight = FontWeight.Medium),
    titleSmall = base.titleSmall.copy(fontSize = 16.sp, lineHeight = 22.sp, fontWeight = FontWeight.Medium),
)
