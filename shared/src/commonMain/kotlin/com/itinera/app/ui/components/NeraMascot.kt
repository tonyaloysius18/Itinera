package com.itinera.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.itinera.app.prefersReducedMotion
import com.itinera.app.resources.Res
import com.itinera.app.resources.nera_blink
import com.itinera.app.resources.nera_idle
import com.itinera.app.resources.nera_thinking
import com.itinera.app.resources.nera_wave_a
import com.itinera.app.resources.nera_wave_b
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.painterResource
import kotlin.random.Random

private enum class NeraPose { IDLE, BLINK, WAVE_A, WAVE_B }

/** How many times Nera waves (with her "Plan with Nera" bubble) per visit to the screen; after that she just idles. */
private const val MAX_WAVES = 3

/**
 * Nera's entry button for the trip home header: a small mascot who bobs gently, blinks, and now and then waves
 * with a speech bubble saying [label]. With the system "reduce motion" setting on she stays still.
 *
 * The visible mascot is larger than the 48 dp touch target so she is readable without making the header taller.
 */
@Composable
fun NeraMascotButton(label: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val reduceMotion = remember { prefersReducedMotion() }
    var pose by remember { mutableStateOf(NeraPose.IDLE) }
    var bubbleVisible by remember { mutableStateOf(false) }

    LaunchedEffect(reduceMotion) {
        if (reduceMotion) return@LaunchedEffect
        delay(1200)
        var waves = 0
        while (true) {
            if (waves < MAX_WAVES) {
                waves++
                bubbleVisible = true
                repeat(3) {
                    pose = NeraPose.WAVE_A; delay(240)
                    pose = NeraPose.WAVE_B; delay(240)
                }
                pose = NeraPose.WAVE_A; delay(240)
                pose = NeraPose.IDLE
                delay(2400)                       // let the bubble be read
                bubbleVisible = false
            }
            repeat(4) {
                delay(Random.nextLong(1800, 3200))
                pose = NeraPose.BLINK; delay(140)
                pose = NeraPose.IDLE
            }
        }
    }

    val bob = if (reduceMotion) 0f else rememberInfiniteTransition(label = "nera-bob").animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2200, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "nera-bob-y",
    ).value

    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val pressScale by animateFloatAsState(if (pressed) 0.92f else 1f, label = "nera-press")

    val idle = painterResource(Res.drawable.nera_idle)
    val blink = painterResource(Res.drawable.nera_blink)
    val waveA = painterResource(Res.drawable.nera_wave_a)
    val waveB = painterResource(Res.drawable.nera_wave_b)

    Box(
        modifier = modifier
            .size(48.dp)
            .semantics { contentDescription = label }
            .clickable(interactionSource = interaction, indication = null, role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        NeraBubble(
            text = label,
            visible = bubbleVisible,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .wrapContentWidth(align = Alignment.End, unbounded = true)   // may extend left of the 48 dp box
                .offset(x = (-50).dp),
        )
        Image(
            painter = when (pose) {
                NeraPose.IDLE -> idle
                NeraPose.BLINK -> blink
                NeraPose.WAVE_A -> waveA
                NeraPose.WAVE_B -> waveB
            },
            contentDescription = null,
            modifier = Modifier
                .requiredSize(64.dp)
                .graphicsLayer {
                    translationY = -bob * 3f * density
                    rotationZ = (bob - 0.5f) * 3f
                    scaleX = pressScale; scaleY = pressScale
                },
        )
    }
}

@Composable
private fun NeraBubble(text: String, visible: Boolean, modifier: Modifier = Modifier) {
    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = fadeIn(tween(200)) + scaleIn(tween(220), initialScale = 0.8f, transformOrigin = TransformOrigin(1f, 0.5f)),
        exit = fadeOut(tween(200)) + scaleOut(tween(200), targetScale = 0.8f, transformOrigin = TransformOrigin(1f, 0.5f)),
    ) {
        val fill = MaterialTheme.colorScheme.surface
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = fill,
                shadowElevation = 6.dp,
            ) {
                Text(
                    text,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                )
            }
            // little tail pointing at the mascot
            Canvas(Modifier.size(width = 7.dp, height = 12.dp)) {
                drawPath(
                    Path().apply {
                        moveTo(0f, 0f); lineTo(size.width, size.height / 2f); lineTo(0f, size.height); close()
                    },
                    color = fill,
                )
            }
        }
    }
}

/** Nera "thinking" while a reply is on its way (chat screen). Bobs gently unless reduce motion is on. */
@Composable
fun NeraThinking(modifier: Modifier = Modifier) {
    val reduceMotion = remember { prefersReducedMotion() }
    val bob = if (reduceMotion) 0f else rememberInfiniteTransition(label = "nera-think").animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1100, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "nera-think-y",
    ).value
    Image(
        painter = painterResource(Res.drawable.nera_thinking),
        contentDescription = null,
        modifier = modifier
            .size(84.dp)
            .graphicsLayer { translationY = -bob * 4f * density },
    )
}
