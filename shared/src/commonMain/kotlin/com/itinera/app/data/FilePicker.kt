package com.itinera.app.data

import androidx.compose.runtime.Composable

/** A file the user picked: its bytes, original name, and mime type. */
data class PickedFile(
    val bytes: ByteArray,
    val fileName: String,
    val mimeType: String,
)

expect class FilePicker {
    /** Opens the system file picker. Returns the picked file, or null if cancelled. */
    suspend fun pickFile(): PickedFile?
}

@Composable
expect fun rememberFilePicker(): FilePicker