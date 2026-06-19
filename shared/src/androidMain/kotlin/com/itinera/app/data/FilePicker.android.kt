package com.itinera.app.data

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.CompletableDeferred

actual class FilePicker(
    private val context: Context,
    private val launchAndAwait: suspend () -> Uri?,
) {
    actual suspend fun pickFile(): PickedFile? {
        val uri = launchAndAwait() ?: return null
        val resolver = context.contentResolver

        val bytes = resolver.openInputStream(uri)?.use { it.readBytes() } ?: return null

        // original display name
        var name = "document"
        resolver.query(uri, null, null, null, null)?.use { c ->
            val idx = c.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (idx >= 0 && c.moveToFirst()) name = c.getString(idx)
        }

        val mime = resolver.getType(uri) ?: "application/octet-stream"
        return PickedFile(bytes, name, mime)
    }
}

@Composable
actual fun rememberFilePicker(): FilePicker {
    val context = LocalContext.current
    val deferredHolder = remember { mutableStateOf<CompletableDeferred<Uri?>?>(null) }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        deferredHolder.value?.complete(uri)
        deferredHolder.value = null
    }

    return remember {
        FilePicker(context) {
            val deferred = CompletableDeferred<Uri?>()
            deferredHolder.value = deferred
            launcher.launch(arrayOf("*/*"))   // any file type
            deferred.await()
        }
    }
}