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