package com.itinera.app.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.itinera.app.i18n.LocalStrings
import com.itinera.app.ui.theme.itinera
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * A row that slides left to reveal Edit and Delete, in the style of the trip cards on
 * the home screen. [isOpen]/[onOpenChange] let the parent keep only one row open at a
 * time. With [enabled] false (viewers) the row doesn't move.
 */
@Composable
fun SwipeRevealRow(
    enabled: Boolean,
    isOpen: Boolean,
    onOpenChange: (Boolean) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val s = LocalStrings.current
    val density = LocalDensity.current
    val panelWidth = 112.dp
    val panelPx = with(density) { panelWidth.toPx() }
    val scope = rememberCoroutineScope()
    val offsetX = remember { Animatable(0f) }
    val progress = (-offsetX.value / panelPx).coerceIn(0f, 1f)

    LaunchedEffect(isOpen) {
        val target = if (isOpen) -panelPx else 0f
        if (offsetX.value != target) offsetX.animateTo(target, spring(stiffness = Spring.StiffnessMedium))
    }

    Box(modifier.fillMaxWidth()) {
        if (enabled) {
            // Behind: the actions, pinned to the right edge
            Row(
                Modifier.matchParentSize(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    Modifier.width(panelWidth),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    RevealAction(Icons.Filled.Edit, s.edit, MaterialTheme.itinera.actionEdit, progress) {
                        onOpenChange(false); onEdit()
                    }
                    RevealAction(Icons.Filled.Delete, s.delete, MaterialTheme.itinera.actionDelete, progress) {
                        onOpenChange(false); onDelete()
                    }
                }
            }
        }

        // Front: the row itself, dragged horizontally
        Box(
            Modifier
                .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                .then(
                    if (!enabled) Modifier
                    else Modifier.pointerInput(Unit) {
                        detectHorizontalDragGestures(
                            onDragStart = { if (!isOpen) onOpenChange(false) },
                            onHorizontalDrag = { change, dragAmount ->
                                change.consume()
                                scope.launch {
                                    offsetX.snapTo((offsetX.value + dragAmount).coerceIn(-panelPx, 0f))
                                }
                            },
                            onDragEnd = {
                                scope.launch {
                                    val open = offsetX.value < -panelPx / 2
                                    offsetX.animateTo(
                                        if (open) -panelPx else 0f,
                                        spring(
                                            dampingRatio = if (open) Spring.DampingRatioLowBouncy else Spring.DampingRatioNoBouncy,
                                            stiffness = Spring.StiffnessMediumLow,
                                        ),
                                    )
                                    onOpenChange(open)
                                }
                            },
                        )
                    },
                ),
        ) {
            content()
        }
    }
}

@Composable
private fun RevealAction(icon: ImageVector, label: String, bg: Color, progress: Float, onClick: () -> Unit) {
    Box(
        Modifier
            .size(44.dp)
            .graphicsLayer {
                scaleX = progress
                scaleY = progress
                alpha = progress
            }
            .clip(CircleShape)
            .background(bg)
            .clickable(
                enabled = progress > 0.9f,
                onClick = onClick,
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = label, tint = Color.White, modifier = Modifier.size(18.dp))
    }
}
