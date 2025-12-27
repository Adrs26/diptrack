package com.adrian.diptrack.presentation.screen.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.AccessTimeFilled
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.DepartureBoard
import androidx.compose.material.icons.outlined.DirectionsBus
import androidx.compose.material.icons.outlined.Store
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.adrian.diptrack.domain.common.ModeState
import com.adrian.diptrack.presentation.theme.Blue800
import com.adrian.diptrack.presentation.theme.Charcoal

@Composable
fun SheetModeContent(
    currentMode: ModeState,
    etaMinutes: Int,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.AccessTimeFilled,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Bus akan tiba ke lokasi dalam",
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 14.sp)
            )
            Text(
                text = "$etaMinutes menit",
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 14.sp)
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 32.dp, horizontal = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ApplicationModeIcon(
                icon = Icons.AutoMirrored.Filled.DirectionsWalk,
                currentMode = currentMode
            )
            LinearProgress(
                status = 2,
                modifier = Modifier.weight(1f)
            )
            ApplicationModeIcon(
                icon = Icons.Outlined.DepartureBoard,
                currentMode = currentMode
            )
            LinearProgress(
                status = if (currentMode == ModeState.Wait) 3 else 2,
                modifier = Modifier.weight(1f)
            )
            ApplicationModeIcon(
                icon = Icons.Outlined.DirectionsBus,
                currentMode = currentMode
            )
            LinearProgress(
                status = when (currentMode) {
                    ModeState.Idle -> 1
                    ModeState.Wait -> 1
                    ModeState.Drive -> 3
                    ModeState.Arrive -> 2
                },
                modifier = Modifier.weight(1f)
            )
            ApplicationModeIcon(
                icon = Icons.Outlined.Store,
                currentMode = currentMode
            )
        }
    }
}

@Composable
private fun ApplicationModeIcon(
    icon: ImageVector,
    currentMode: ModeState,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(36.dp)
            .drawBehind {
                val strokeWidth = 1.dp.toPx()
                drawCircle(
                    color = changeIconColor(icon, currentMode),
                    radius = size.minDimension / 2 - strokeWidth / 2,
                    style = Stroke(width = strokeWidth)
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = changeIconColor(icon, currentMode)
        )
        if (changeIconColor(icon, currentMode) == MaterialTheme.colorScheme.primary) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                modifier = Modifier
                    .size(12.dp)
                    .align(Alignment.TopEnd)
                    .background(
                        color = MaterialTheme.colorScheme.surface,
                        shape = MaterialTheme.shapes.small
                    ),
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun LinearProgress(
    status: Int,
    modifier: Modifier = Modifier
) {
    val progress = rememberInfiniteTransition()
        .animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1500, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            )
        )

    LinearProgressIndicator(
        progress = {
            when (status) {
                1 -> 0f
                2 -> 1f
                else -> progress.value
            }
        },
        modifier = modifier.height(4.dp),
        color = MaterialTheme.colorScheme.primary,
        trackColor = MaterialTheme.colorScheme.outlineVariant
    )
}

private fun changeIconColor(
    icon: ImageVector,
    currentMode: ModeState
): Color {
    return when(icon) {
        Icons.Outlined.DirectionsBus -> {
            when(currentMode) {
                ModeState.Idle -> Charcoal
                ModeState.Wait -> Charcoal
                ModeState.Drive -> Blue800
                ModeState.Arrive -> Blue800
            }
        }
        Icons.Outlined.Store -> {
            when(currentMode) {
                ModeState.Idle -> Charcoal
                ModeState.Wait -> Charcoal
                ModeState.Drive -> Charcoal
                ModeState.Arrive -> Blue800
            }
        }
        else -> Blue800
    }
}