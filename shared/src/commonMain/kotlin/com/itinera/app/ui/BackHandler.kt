package com.itinera.app.ui

import androidx.compose.runtime.Composable

/**
 * Interface for intercepting the system back press.
 */
@Composable
expect fun BackHandler(enabled: Boolean = true, onBack: () -> Unit)
