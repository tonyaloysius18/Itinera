package com.itinera.app.ui

import androidx.compose.runtime.Composable

/**
 * Android implementation of BackHandler using the system activity's back press dispatcher.
 */
@Composable
actual fun BackHandler(enabled: Boolean, onBack: () -> Unit) {
    androidx.activity.compose.BackHandler(enabled = enabled, onBack = onBack)
}
