package com.itinera.app.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun PlatformStartupVideo(
    uri: String,
    modifier: Modifier = Modifier,
    onFinished: () -> Unit
)