package com.itinera.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.itinera.app.resources.Res
import com.itinera.app.ui.components.PlatformStartupVideo
import org.jetbrains.compose.resources.ExperimentalResourceApi

// Android Studio should auto-import your generated Res
// Example:
// import itinera.composeapp.generated.resources.Res

@OptIn(ExperimentalResourceApi::class)
@Composable
fun StartupVideoScreen(
    onFinished: () -> Unit
) {

    val videoUri =
        Res.getUri(
            "files/itinera_startup.mp4"
        )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {

        PlatformStartupVideo(
            uri = videoUri,
            modifier = Modifier.fillMaxSize(),
            onFinished = onFinished
        )
    }
}