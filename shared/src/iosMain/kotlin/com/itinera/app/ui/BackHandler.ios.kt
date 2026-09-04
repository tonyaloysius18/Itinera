package com.itinera.app.ui

import androidx.compose.runtime.Composable

/**
 * iOS implementation of BackHandler (no-op as there is no hardware back button).
 */
@Composable
actual fun BackHandler(enabled: Boolean, onBack: () -> Unit) {
    // No-op on iOS
}
