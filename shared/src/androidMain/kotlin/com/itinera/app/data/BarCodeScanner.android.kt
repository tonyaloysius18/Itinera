package com.itinera.app.data

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

actual suspend fun renderPdfFirstPage(pdfBytes: ByteArray): ImageBitmap? {
    return try {
        val tmp = File.createTempFile("ticket_", ".pdf")
        tmp.writeBytes(pdfBytes)
        val pfd = ParcelFileDescriptor.open(tmp, ParcelFileDescriptor.MODE_READ_ONLY)
        val renderer = PdfRenderer(pfd)
        if (renderer.pageCount <= 0) {
            renderer.close(); pfd.close(); tmp.delete(); return null
        }
        val page = renderer.openPage(0)
        val scale = 3                                   // 3x for a crisp, decodable barcode
        val w = (page.width * scale).coerceAtLeast(1)
        val h = (page.height * scale).coerceAtLeast(1)
        val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        bmp.eraseColor(Color.WHITE)                     // PDFs are transparent; give white bg
        page.render(bmp, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
        page.close(); renderer.close(); pfd.close(); tmp.delete()
        bmp.asImageBitmap()
    } catch (e: Exception) {
        println("PDF RENDER FAILED: ${e.message}")
        null
    }
}

actual suspend fun detectBarcodesInImage(image: ImageBitmap): List<DetectedBarcode> {
    val bmp = image.asAndroidBitmap()
    val input = InputImage.fromBitmap(bmp, 0)
    val scanner = BarcodeScanning.getClient(BarcodeScannerOptions.Builder().build()) // all formats
    val w = bmp.width.toFloat()
    val h = bmp.height.toFloat()

    return suspendCancellableCoroutine { cont ->
        scanner.process(input)
            .addOnSuccessListener { barcodes ->
                val out = barcodes.mapNotNull { b ->
                    val box = b.boundingBox ?: return@mapNotNull null
                    DetectedBarcode(
                        rawValue = b.rawValue ?: b.displayValue ?: "",
                        left = box.left / w, top = box.top / h,
                        right = box.right / w, bottom = box.bottom / h,
                    )
                }
                scanner.close()
                cont.resume(out)
            }
            .addOnFailureListener { e ->
                scanner.close()
                cont.resumeWithException(e)
            }
        cont.invokeOnCancellation { scanner.close() }
    }
}