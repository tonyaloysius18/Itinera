package com.itinera.app.data

import io.ktor.client.HttpClient
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.content.PartData
import io.ktor.utils.io.core.buildPacket
import io.ktor.utils.io.core.writeFully
import kotlinx.io.readByteArray
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

private const val CLOUD_NAME = "dnyaivctq"
private const val UPLOAD_PRESET = "itinera_profile"

suspend fun uploadBytesToStorage(
    httpClient: HttpClient,
    bytes: ByteArray,
): String {
    val parts = listOf(
        // text field: upload_preset
        PartData.FormItem(
            UPLOAD_PRESET,
            {},
            Headers.build {
                append(HttpHeaders.ContentDisposition, "form-data; name=\"upload_preset\"")
            },
        ),
        // binary field: file
        PartData.BinaryItem(
            { buildPacket { writeFully(bytes) }.readByteArray() }.let { provider ->
                // wrap as Input provider
                { kotlinx.io.Buffer().apply { write(bytes) } }
            },
            {},
            Headers.build {
                append(HttpHeaders.ContentType, "image/jpeg")
                append(HttpHeaders.ContentDisposition, "form-data; name=\"file\"; filename=\"profile.jpg\"")
            },
        ),
    )

    val response = httpClient.post("https://api.cloudinary.com/v1_1/$CLOUD_NAME/image/upload") {
        setBody(MultiPartFormDataContent(parts))
    }

    val body = response.bodyAsText()
    println("CLOUDINARY RESPONSE: $body")
    val json = Json.parseToJsonElement(body).jsonObject
    return json["secure_url"]?.jsonPrimitive?.content
        ?: throw Exception("Cloudinary upload failed: $body")
}

/**
 * Generic document upload for any file type (pdf / jpg / png / docx / ...).
 *
 * Uses the /auto/ endpoint so Cloudinary picks the right resource type per file
 * (PDFs & images -> image, docx & others -> raw). Preserves the original filename
 * and content type so the stored asset keeps its extension.
 *
 * NOTE: docx (a "raw" file) only succeeds if the unsigned preset allows raw/auto
 * resource types. PDF/JPG/PNG work even on an image-only preset.
 */
suspend fun uploadFileToStorage(
    httpClient: HttpClient,
    bytes: ByteArray,
    fileName: String,
    mimeType: String,
): String {
    val safeName = fileName.ifBlank { "document" }
    val safeMime = mimeType.ifBlank { "application/octet-stream" }

    val parts = listOf(
        // text field: upload_preset
        PartData.FormItem(
            UPLOAD_PRESET,
            {},
            Headers.build {
                append(HttpHeaders.ContentDisposition, "form-data; name=\"upload_preset\"")
            },
        ),
        // binary field: file — same provider pattern as the profile upload above
        PartData.BinaryItem(
            { kotlinx.io.Buffer().apply { write(bytes) } },
            {},
            Headers.build {
                append(HttpHeaders.ContentType, safeMime)
                append(HttpHeaders.ContentDisposition, "form-data; name=\"file\"; filename=\"$safeName\"")
            },
        ),
    )

    // /auto/ lets Cloudinary detect image vs raw per file.
    val response = httpClient.post("https://api.cloudinary.com/v1_1/$CLOUD_NAME/auto/upload") {
        setBody(MultiPartFormDataContent(parts))
    }

    val body = response.bodyAsText()
    println("CLOUDINARY DOC RESPONSE: $body")
    val json = Json.parseToJsonElement(body).jsonObject
    return json["secure_url"]?.jsonPrimitive?.content
        ?: throw Exception("Cloudinary upload failed: $body")
}