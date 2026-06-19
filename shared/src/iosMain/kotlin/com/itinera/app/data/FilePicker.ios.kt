package com.itinera.app.data

import androidx.compose.runtime.Composable
import kotlin.coroutines.resume
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlinx.coroutines.suspendCancellableCoroutine

actual class FilePicker {
    @OptIn(ExperimentalEncodingApi::class)
    actual suspend fun pickFile(): PickedFile? = suspendCancellableCoroutine { cont ->
        val provider = IosFilePicker.provider
        if (provider == null) {
            cont.resume(null)
            return@suspendCancellableCoroutine
        }
        provider.pick { base64, name, mime ->
            if (base64 == null || name == null) {
                cont.resume(null)
            } else {
                val bytes = Base64.decode(base64)
                cont.resume(PickedFile(bytes, name, mime ?: "application/octet-stream"))
            }
        }
    }
}

@Composable
actual fun rememberFilePicker(): FilePicker = FilePicker()

/** Swift implements this; passes the file as base64 + name + mime. */
fun interface FilePickerProvider {
    fun pick(onResult: (base64: String?, name: String?, mime: String?) -> Unit)
}

object IosFilePicker {
    var provider: FilePickerProvider? = null
}