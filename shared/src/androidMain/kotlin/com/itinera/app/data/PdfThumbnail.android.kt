package com.itinera.app.data

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import com.itinera.app.AndroidApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File

/**
 * PdfRenderer needs a seekable file descriptor, so the bytes go to a temp file
 * in cacheDir first — it can't read from a stream or a byte array.
 *
 * Depends on AndroidApp.context, which must live in shared/src/androidMain
 * (NOT androidApp/) or this file can't see it.
 */
actual suspend fun renderPdfFirstPage(bytes: ByteArray, targetWidthPx: Int): ByteArray? =
    withContext(Dispatchers.IO) {
        val ctx = AndroidApp.context ?: return@withContext null
        var tmp: File? = null
        try {
            tmp = File.createTempFile("pdfthumb", ".pdf", ctx.cacheDir).apply { writeBytes(bytes) }
            ParcelFileDescriptor.open(tmp, ParcelFileDescriptor.MODE_READ_ONLY).use { pfd ->
                PdfRenderer(pfd).use { renderer ->
                    if (renderer.pageCount == 0) return@withContext null
                    renderer.openPage(0).use { page ->
                        val scale = targetWidthPx.toFloat() / page.width
                        val h = (page.height * scale).toInt().coerceAtLeast(1)
                        val bmp = Bitmap.createBitmap(targetWidthPx, h, Bitmap.Config.ARGB_8888)
                        // PDFs are transparent where nothing is drawn; without this
                        // a text page renders as marks on nothing.
                        bmp.eraseColor(Color.WHITE)
                        page.render(bmp, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                        ByteArrayOutputStream().use { out ->
                            bmp.compress(Bitmap.CompressFormat.PNG, 100, out)
                            bmp.recycle()
                            out.toByteArray()
                        }
                    }
                }
            }
        } catch (_: Exception) {
            null
        } finally {
            tmp?.delete()
        }
    }