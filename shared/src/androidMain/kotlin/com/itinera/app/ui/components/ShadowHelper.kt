package com.itinera.app.ui.components

import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.nativeCanvas

actual fun DrawScope.drawPillShadow(
    path: Path,
    radius: Float,
    dy: Float,
    isLightMode: Boolean
) {
    drawContext.canvas.nativeCanvas.save()
    val frameworkPaint = android.graphics.Paint().apply {
        isAntiAlias = true
        color = android.graphics.Color.TRANSPARENT
        setShadowLayer(
            radius,
            0f,
            dy,
            if (!isLightMode) android.graphics.Color.argb(70, 0, 0, 0)
            else android.graphics.Color.argb(35, 0, 0, 0)
        )
    }
    drawContext.canvas.nativeCanvas.drawPath(path.asAndroidPath(), frameworkPaint)
    drawContext.canvas.nativeCanvas.restore()
}
