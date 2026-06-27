package com.itinera.app.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.itinera.app.data.Compass
import com.itinera.app.data.cardinal
import com.itinera.app.i18n.LocalStrings
import com.itinera.app.ui.components.TopBar
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin

@Composable
fun CompassScreen(
    compass: Compass,
    onBack: () -> Unit,
) {
    val s = LocalStrings.current
    val heading by compass.heading.collectAsState()
    val available by compass.available.collectAsState()

    DisposableEffect(Unit) {
        compass.start()
        onDispose { compass.stop() }
    }

    // Smooth rotation with 359°→0° wrap handling (shortest path).
    var unwrapped by remember { mutableStateOf(0f) }
    LaunchedEffect(heading) {
        val current = unwrapped
        val diff = ((heading - (current % 360f) + 540f) % 360f) - 180f
        unwrapped = current + diff
    }
    val dialAngle by animateFloatAsState(
        targetValue = -unwrapped,   // counter-rotate so N tracks magnetic north
        animationSpec = tween(200),
        label = "dialRotation",
    )

    Column(Modifier.fillMaxSize()) {
        TopBar(s.compass, onBack = onBack)

        Column(
            Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            val deg = (((heading % 360f) + 360f) % 360f).roundToInt()
            Text("$deg°", style = MaterialTheme.typography.displayLarge, fontWeight = FontWeight.Bold)
            Text(
                cardinal(heading),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold,
            )

            Spacer(Modifier.height(40.dp))

            val onSurface = MaterialTheme.colorScheme.onSurface
            val primary = MaterialTheme.colorScheme.primary
            val faint = onSurface.copy(alpha = 0.25f)
            val dialSize = 300.dp

            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(dialSize)) {

                // Rotating dial: tick marks (Canvas) + cardinal letters (Text), both rotated together
                Box(
                    Modifier.fillMaxSize().graphicsLayer { rotationZ = dialAngle },
                    contentAlignment = Alignment.Center,
                ) {
                    Canvas(Modifier.fillMaxSize()) {
                        val r = min(size.width, size.height) / 2f
                        val cx = size.width / 2f
                        val cy = size.height / 2f
                        for (a in 0 until 360 step 15) {
                            val rad = a * kotlin.math.PI.toFloat() / 180f
                            val outer = r * 0.95f
                            val major = a % 90 == 0
                            val inner = when {
                                major -> r * 0.80f
                                a % 45 == 0 -> r * 0.85f
                                else -> r * 0.89f
                            }
                            drawLine(
                                color = if (major) primary else faint,
                                start = Offset(cx + sin(rad) * outer, cy - cos(rad) * outer),
                                end = Offset(cx + sin(rad) * inner, cy - cos(rad) * inner),
                                strokeWidth = if (major) 7f else 3f,
                            )
                        }
                    }
                    // Cardinal letters positioned around the dial
                    CardinalLabel("N", 0f, dialSize, color = primary)
                    CardinalLabel("E", 90f, dialSize, color = onSurface)
                    CardinalLabel("S", 180f, dialSize, color = onSurface)
                    CardinalLabel("W", 270f, dialSize, color = onSurface)
                }

                // Fixed pointer at top — the direction the device is facing
                Canvas(Modifier.fillMaxSize()) {
                    val cx = size.width / 2f
                    drawCircle(primary, radius = 12f, center = Offset(cx, size.height * 0.04f))
                }
            }

            Spacer(Modifier.height(40.dp))

            if (!available) {
                Text(
                    s.compassCalibrate,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                )
            }
        }
    }
}

/** A cardinal letter placed at [angleDeg] around a circle of [dialSize]. */
@Composable
private fun CardinalLabel(text: String, angleDeg: Float, dialSize: androidx.compose.ui.unit.Dp, color: Color) {
    val rad = angleDeg * kotlin.math.PI.toFloat() / 180f
    val radiusFraction = 0.66f
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text,
            color = color,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.offset(
                x = (dialSize * radiusFraction / 2) * sin(rad),
                y = -(dialSize * radiusFraction / 2) * cos(rad),
            ),
        )
    }
}