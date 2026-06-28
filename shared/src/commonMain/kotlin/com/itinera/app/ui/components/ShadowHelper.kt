package com.itinera.app.ui.components

import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope

expect fun DrawScope.drawPillShadow(
    path: Path,
    radius: Float,
    dy: Float,
    isLightMode: Boolean
)
