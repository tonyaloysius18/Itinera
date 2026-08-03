package com.itinera.app.ui.components

import androidx.compose.runtime.Composable

/**
 * While this composable is in the composition, forces the screen to max brightness
 * (for showing scannable barcodes at the gate); restores the previous level on dispose.
 */
@Composable
expect fun KeepMaxBrightness()