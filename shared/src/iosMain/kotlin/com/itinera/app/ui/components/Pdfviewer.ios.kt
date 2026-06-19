package com.itinera.app.ui.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.interop.UIKitView
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.convert
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.Foundation.create
import platform.PDFKit.PDFDocument
import platform.PDFKit.PDFView

@OptIn(ExperimentalComposeUiApi::class, ExperimentalForeignApi::class)
@Composable
actual fun PdfViewer(bytes: ByteArray, modifier: Modifier) {
    UIKitView(
        factory = {
            val pdfView = PDFView()
            val data = bytes.toNSData()
            if (data != null) {
                pdfView.document = PDFDocument(data = data)
            }
            pdfView.autoScales = true
            pdfView
        },
        modifier = modifier.fillMaxSize(),
    )
}

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
private fun ByteArray.toNSData(): NSData? {
    if (isEmpty()) return null
    return usePinned { pinned ->
        NSData.create(bytes = pinned.addressOf(0), length = size.convert())
    }
}