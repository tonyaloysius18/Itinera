package com.itinera.app.ui.components

import android.R.attr.keepScreenOn
import android.R.attr.repeatMode
import android.R.attr.resizeMode
import androidx.annotation.OptIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView

@OptIn(UnstableApi::class)
@Composable
actual fun PlatformStartupVideo(
    uri: String,
    modifier: Modifier,
    onFinished: () -> Unit
) {
    val context = LocalContext.current

    val currentOnFinished = rememberUpdatedState(onFinished)

    val player = remember(uri) {
        ExoPlayer.Builder(context).build().apply {

            setMediaItem(
                MediaItem.fromUri(uri)
            )

            // Startup animation should only play once
            repeatMode = Player.REPEAT_MODE_OFF

            // Mute the startup video
            volume = 0f

            // Start automatically
            playWhenReady = true

            prepare()
        }
    }

    DisposableEffect(player) {

        val listener = object : Player.Listener {

            override fun onPlaybackStateChanged(
                playbackState: Int
            ) {
                if (playbackState == Player.STATE_ENDED) {
                    currentOnFinished.value()
                }
            }

            override fun onPlayerError(
                error: PlaybackException
            ) {
                // Don't leave the user stuck on the splash
                currentOnFinished.value()
            }
        }

        player.addListener(listener)

        onDispose {
            player.removeListener(listener)
            player.stop()
            player.release()
        }
    }

    AndroidView(
        modifier = modifier,

        factory = { context ->

            PlayerView(context).apply {

                this.player = player

                // Hide video playback controls
                useController = false

                // Fill the entire screen
                resizeMode =
                    AspectRatioFrameLayout.RESIZE_MODE_ZOOM

                // Keep startup animation screen awake
                keepScreenOn = true
            }
        },

        update = { playerView ->
            playerView.player = player
        }
    )
}