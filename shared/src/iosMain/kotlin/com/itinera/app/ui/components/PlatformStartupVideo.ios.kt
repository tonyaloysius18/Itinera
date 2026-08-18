package com.itinera.app.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.interop.UIKitView
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.readValue
import platform.AVFoundation.AVLayerVideoGravityResizeAspectFill
import platform.AVFoundation.AVPlayer
import platform.AVFoundation.AVPlayerItemDidPlayToEndTimeNotification
import platform.AVFoundation.AVPlayerLayer
import platform.AVFoundation.currentItem
import platform.AVFoundation.pause
import platform.AVFoundation.play
import platform.AVFoundation.volume
import platform.CoreGraphics.CGRectZero
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSOperationQueue
import platform.Foundation.NSURL
import platform.QuartzCore.CATransaction
import platform.QuartzCore.kCATransactionDisableActions
import platform.UIKit.UIView

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun PlatformStartupVideo(
    uri: String,
    modifier: Modifier,
    onFinished: () -> Unit
) {

    val currentOnFinished =
        rememberUpdatedState(onFinished)

    val player = remember(uri) {

        val url = NSURL(string = uri)

        AVPlayer(
            uRL = url
        ).apply {
            volume = 0f
        }
    }

    val playerLayer = remember(player) {

        AVPlayerLayer().apply {

            this.player = player

            // Same behavior as Android ZOOM:
            // fill screen while maintaining aspect ratio
            videoGravity =
                AVLayerVideoGravityResizeAspectFill
        }
    }

    DisposableEffect(player) {

        val observer =
            NSNotificationCenter.defaultCenter
                .addObserverForName(
                    name =
                        AVPlayerItemDidPlayToEndTimeNotification,

                    `object` =
                        player.currentItem,

                    queue =
                        NSOperationQueue.mainQueue

                ) { _ ->

                    currentOnFinished.value()
                }

        player.play()

        onDispose {

            player.pause()

            NSNotificationCenter
                .defaultCenter
                .removeObserver(observer)
        }
    }

    UIKitView(
        modifier = modifier,

        factory = {

            val container =
                object : UIView(
                    frame = CGRectZero.readValue()
                ) {

                    override fun layoutSubviews() {
                        super.layoutSubviews()

                        CATransaction.begin()

                        CATransaction.setValue(
                            true,
                            kCATransactionDisableActions
                        )

                        playerLayer.setFrame(bounds)

                        CATransaction.commit()
                    }
                }

            container.layer.addSublayer(
                playerLayer
            )

            container
        },

        update = {
            playerLayer.player = player
        }
    )
}