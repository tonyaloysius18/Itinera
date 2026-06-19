package com.itinera.app.data

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import java.io.File

actual class FileSharer(private val context: Context) {
    actual fun share(bytes: ByteArray, fileName: String, mimeType: String) {
        try {
            val dir = File(context.cacheDir, "shared").apply { mkdirs() }
            val file = File(dir, fileName.ifBlank { "document" })
            file.writeBytes(bytes)

            val authority = "${context.packageName}.itinerafileprovider"
            val uri = FileProvider.getUriForFile(context, authority, file)

            val send = Intent(Intent.ACTION_SEND).apply {
                type = mimeType.ifBlank { "*/*" }
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            val chooser = Intent.createChooser(send, null).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
        } catch (e: Exception) {
            println("SHARE FAILED: ${e.message}")
        }
    }
}

@Composable
actual fun rememberFileSharer(): FileSharer {
    val context = LocalContext.current
    return remember { FileSharer(context) }
}