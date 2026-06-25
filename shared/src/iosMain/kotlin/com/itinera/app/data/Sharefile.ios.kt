package com.itinera.app.data

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.convert
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSURL
import platform.Foundation.create
import platform.Foundation.writeToFile
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication

actual class FileSharer {
    @OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
    actual fun share(bytes: ByteArray, fileName: String, mimeType: String) {
        val data = bytes.toNSData() ?: return
        val path = NSTemporaryDirectory() + fileName.ifBlank { "document" }
        data.writeToFile(path, atomically = true)
        val url = NSURL.fileURLWithPath(path)

        val activityVC = UIActivityViewController(
            activityItems = listOf(url),
            applicationActivities = null,
        )

        val root = UIApplication.sharedApplication.keyWindow?.rootViewController
        // iPad: a popover anchor would be required; iPhone presents fine as-is.
        root?.presentViewController(activityVC, animated = true, completion = null)
    }
actual fun shareText(text: String) {
        val activityVC = UIActivityViewController(
            activityItems = listOf(text),
            applicationActivities = null,
        )
        val root = UIApplication.sharedApplication.keyWindow?.rootViewController
        root?.presentViewController(activityVC, animated = true, completion = null)
    }
}

@Composable
actual fun rememberFileSharer(): FileSharer = remember { FileSharer() }

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
private fun ByteArray.toNSData(): NSData? {
    if (isEmpty()) return null
    return usePinned { pinned ->
        NSData.create(bytes = pinned.addressOf(0), length = size.convert())
    }
}