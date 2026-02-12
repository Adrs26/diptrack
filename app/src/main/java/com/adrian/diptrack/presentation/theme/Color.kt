package com.adrian.diptrack.presentation.theme

import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

val SoftWhite = Color(0xFFF8FCFB)
val SoftBlack = Color(0xFF121212)
val Blue800 = Color(0xFF1565C0)
val Blue400 = Color(0xFF42A5F5)
val Charcoal = Color(0xFF36454F)
val LightOutline = Color(0xFFC4CAD0)

val ColorScheme = lightColorScheme(
    primary = Blue800,
    onPrimary = SoftWhite,
    inversePrimary = Blue400,
    surface = SoftWhite,
    onSurface = SoftBlack,
    outline = LightOutline,
    outlineVariant = Color.LightGray
)