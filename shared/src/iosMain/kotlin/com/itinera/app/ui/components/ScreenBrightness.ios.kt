package com.itinera.app.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import platform.UIKit.UIScreen

@Composable
actual fun KeepMaxBrightness() {
    DisposableEffect(Unit) {
        val prev = UIScreen.mainScreen.brightness
        UIScreen.mainScreen.brightness = 1.0
        onDispose { UIScreen.mainScreen.brightness = prev }
    }
}