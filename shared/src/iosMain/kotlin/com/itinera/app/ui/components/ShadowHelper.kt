package com.itinera.app.ui.components

import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope

actual fun DrawScope.drawPillShadow(
    path: Path,
    radius: Float,
    dy: Float,
    isLightMode: Boolean
) {
    // Basic fallback for iOS - you could implement Skia-based blur here if needed
}
