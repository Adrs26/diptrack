package com.adrian.diptrack.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

@Composable
fun DipTrackTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = ColorScheme,
        shapes = Shape,
        typography = Typography,
        content = content
    )
}