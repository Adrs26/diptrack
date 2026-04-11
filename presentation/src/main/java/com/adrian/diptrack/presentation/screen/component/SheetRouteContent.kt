package com.adrian.diptrack.presentation.screen.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.adrian.diptrack.data.common.ModeState
import java.util.Locale

@Composable
fun SheetRouteContent(
    currentMode: ModeState,
    originName: String,
    destinationName: String,
    distanceKilometers: Double,
    onChangeDestinationClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val destinationModifier = if (currentMode is ModeState.Wait) {
        Modifier
            .clickable { onChangeDestinationClick() }
            .fillMaxWidth()
    } else {
        Modifier.fillMaxWidth()
    }

    Column(
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(MaterialTheme.shapes.small)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(MaterialTheme.shapes.small)
                        .background(MaterialTheme.colorScheme.surface)
                )
            }
            Text(
                text = originName,
                modifier = Modifier
                    .padding(start = 16.dp)
                    .weight(1f),
                style = MaterialTheme.typography.bodyMedium
            )
            Box(
                modifier = Modifier
                    .border(
                        border = BorderStroke(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outline
                        ),
                        shape = MaterialTheme.shapes.medium
                    )
            ) {
                Text(
                    text = "${String.format(Locale.US, "%.1f", distanceKilometers)} km",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = 12.sp)
                )
            }
        }
        Column(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            repeat(5) {
                Box(
                    Modifier
                        .size(4.dp)
                        .clip(MaterialTheme.shapes.small)
                        .background(MaterialTheme.colorScheme.secondary)
                )
            }
        }
        Row(
            modifier = destinationModifier,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(MaterialTheme.shapes.small)
                    .background(Color.Red),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(MaterialTheme.shapes.small)
                        .background(MaterialTheme.colorScheme.surface)
                )
            }
            Text(
                text = destinationName,
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .weight(1f),
                style = MaterialTheme.typography.bodyMedium
            )
            if (currentMode is ModeState.Wait) {
                Icon(
                    imageVector = Icons.AutoMirrored.Default.KeyboardArrowRight,
                    contentDescription = null,
                )
            }
        }
    }
}