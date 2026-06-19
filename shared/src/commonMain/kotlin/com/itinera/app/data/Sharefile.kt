package com.itinera.app.data

import androidx.compose.runtime.Composable

expect class FileSharer {
    /** Writes the bytes to a temp file and opens the native share sheet. */
    fun share(bytes: ByteArray, fileName: String, mimeType: String)
}

@Composable
expect fun rememberFileSharer(): FileSharer