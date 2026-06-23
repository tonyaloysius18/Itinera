package com.itinera.app.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Loading indicator: a shaded folded-paper plane flying around a circle, with a
 * fading dotted trail streaming from its tail (comet tail). Drop-in replacement
 * for CircularProgressIndicator.
 *
 * @param size overall diameter
 * @param color base tint (defaults to primary)
 * @param periodMillis time for one full orbit
 */
@Composable
fun PlaneLoader(
    modifier: Modifier = Modifier,
    size: Dp = 120.dp,
    color: Color = MaterialTheme.colorScheme.primary,
    periodMillis: Int = 2000,
) {
    val transition = rememberInfiniteTransition(label = "planeLoader")
    val angle by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = periodMillis, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "angle",
    )

    Box(modifier = modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(Modifier.size(size)) {
            val cx = this.size.width / 2f
            val cy = this.size.height / 2f
            val orbitR = (this.size.minDimension / 2f) * 0.70f

            fun posAt(deg: Float): Offset {
                val rad = deg * (PI.toFloat() / 180f)
                return Offset(cx + orbitR * cos(rad), cy + orbitR * sin(rad))
            }

            val pSize = this.size.minDimension * 0.24f   // plane length

            // The plane is centred on the orbit and points along the tangent.
            // Its tail sits a little BEHIND the centre (smaller angle). Convert the
            // plane's half-length into an angular offset so the trail begins exactly
            // at the tail, not under the plane.
            val halfPlaneAngle = (pSize / 2f) / orbitR * (180f / PI.toFloat())

            // ---- Trailing dotted tail (starts at the tail, curves along the orbit) ----
            val trailCount = 16
            val trailGapDeg = 7.5f       // spacing between dots (bigger = larger gaps)
            val baseDot = this.size.minDimension * 0.018f
            for (k in 0 until trailCount) {
                val tDeg = angle - halfPlaneAngle - k * trailGapDeg
                val p = posAt(tDeg)
                val frac = k.toFloat() / trailCount
                val alpha = (1f - frac) * 0.9f
                val dotR = baseDot * (1f - frac * 0.5f)
                drawCircle(color = color.copy(alpha = alpha), radius = dotR, center = p)
            }

            // ---- The folded-paper plane (shaded for a 3D-ish look) ----
            val planePos = posAt(angle)
            val h = pSize
            val w = pSize * 0.8f

            withTransform({
                translate(left = planePos.x - cx, top = planePos.y - cy)
                rotate(degrees = angle + 180f, pivot = Offset(cx, cy))
            }) {
                val nose = Offset(cx, cy - h / 2f)
                val leftBack = Offset(cx - w / 2f, cy + h / 2f)
                val rightBack = Offset(cx + w / 2f, cy + h / 2f)
                val tailNotch = Offset(cx, cy + h * 0.22f)

                val lightShade = color
                val darkShade = Color(
                    red = color.red * 0.6f,
                    green = color.green * 0.6f,
                    blue = color.blue * 0.6f,
                    alpha = 1f,
                )

                // Left wing (lighter — catches the light)
                val leftWing = Path().apply {
                    moveTo(nose.x, nose.y)
                    lineTo(leftBack.x, leftBack.y)
                    lineTo(tailNotch.x, tailNotch.y)
                    close()
                }
                drawPath(leftWing, color = lightShade)

                // Right wing (darker — in shadow → the folded-paper / 3D feel)
                val rightWing = Path().apply {
                    moveTo(nose.x, nose.y)
                    lineTo(rightBack.x, rightBack.y)
                    lineTo(tailNotch.x, tailNotch.y)
                    close()
                }
                drawPath(rightWing, color = darkShade)

                // Centre crease for definition
                drawLine(
                    color = darkShade.copy(alpha = 0.9f),
                    start = nose,
                    end = tailNotch,
                    strokeWidth = pSize * 0.04f,
                )
            }
        }
    }
}