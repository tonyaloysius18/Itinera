package com.itinera.app.data

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import io.ktor.client.request.get
import io.ktor.http.HttpHeaders

private const val FeedbackEndpoint =
    "https://script.google.com/macros/s/AKfycbz2C0QFiyURPCmgemqBQd8WBtdN-LVW6g_UphqB-kM7e8GTLuGxuETMBsRWKd8JdP44dQ/exec"


enum class FeedbackCategory(
    val wireValue: String
) {
    PROBLEM("problem"),
    SUGGESTION("suggestion"),
    GENERAL("general"),
}


data class FeedbackAttachment(
    val bytes: ByteArray,
    val fileName: String,
    val mimeType: String = "image/jpeg",
)


data class FeedbackSubmission(
    val category: FeedbackCategory,
    val message: String,
    val includeAppDetails: Boolean,
    val appVersion: String,
    val platform: String,
    val attachments: List<FeedbackAttachment>,
)


@Serializable
internal data class FeedbackPayload(
    val idToken: String,
    val category: String,
    val message: String,
    val includeAppDetails: Boolean,
    val appVersion: String,
    val platform: String,
    val attachments: List<FeedbackAttachmentPayload>,
)


@Serializable
internal data class FeedbackAttachmentPayload(
    val fileName: String,
    val mimeType: String,
    val contentBase64: String,
)


@Serializable
private data class FeedbackResponse(
    val ok: Boolean = false,
    val error: String? = null,
)


/**
 * Sends feedback from Itinera to the configured
 * Google Apps Script feedback endpoint.
 *
 * The current Firebase user's ID token is included
 * so the backend can verify the sender.
 */
class FeedbackService(
    private val client: HttpClient = HttpClient {

        install(ContentNegotiation) {

            json(
                Json {
                    ignoreUnknownKeys = true
                }
            )
        }
    },
) {

    suspend fun send(
        submission: FeedbackSubmission
    ) {

        val user =
            Firebase.auth.currentUser
                ?: error(
                    "Sign in before sending feedback."
                )

        val authToken =
            user.getIdToken(
                forceRefresh = false
            )
                ?: error(
                    "Could not authenticate this feedback request."
                )

        check(
            !FeedbackEndpoint.contains(
                "REPLACE_WITH_DEPLOYMENT_ID"
            )
        ) {
            "The feedback endpoint has not been configured."
        }


        println(
            "ITINERA: Sending feedback..."
        )


        // -----------------------------------------------------
        // POST FEEDBACK
        // -----------------------------------------------------

        val initialResponse =
            client.post(
                FeedbackEndpoint
            ) {

                contentType(
                    ContentType.Application.Json
                )

                setBody(
                    submission.toPayload(
                        authToken
                    )
                )
            }


        println(
            "ITINERA: FEEDBACK INITIAL HTTP = ${initialResponse.status.value}"
        )


        val redirectLocation =
            initialResponse.headers[
                HttpHeaders.Location
            ]


        println(
            "ITINERA: FEEDBACK REDIRECT = $redirectLocation"
        )


        // -----------------------------------------------------
        // GOOGLE APPS SCRIPT MAY RETURN 302
        // -----------------------------------------------------

        val finalResponse =
            if (
                initialResponse.status.value in 300..399 &&
                redirectLocation != null
            ) {

                println(
                    "ITINERA: Following Google Apps Script redirect..."
                )

                client.get(
                    redirectLocation
                )

            } else {

                initialResponse
            }


        // -----------------------------------------------------
        // FINAL RESPONSE
        // -----------------------------------------------------

        val responseText =
            finalResponse.bodyAsText()


        println(
            "ITINERA: FEEDBACK FINAL HTTP = ${finalResponse.status.value}"
        )

        println(
            "ITINERA: FEEDBACK RESPONSE = $responseText"
        )


        if (
            !finalResponse.status.isSuccess()
        ) {

            error(
                "Feedback service returned HTTP " +
                        "${finalResponse.status.value}: " +
                        responseText
            )
        }


        // -----------------------------------------------------
        // PARSE JSON
        // -----------------------------------------------------

        val result =
            try {

                Json {
                    ignoreUnknownKeys = true
                }.decodeFromString<FeedbackResponse>(
                    responseText
                )

            } catch (e: Exception) {

                println(
                    "ITINERA: Could not parse feedback response."
                )

                println(
                    "ITINERA: Raw response = $responseText"
                )

                throw IllegalStateException(
                    "The feedback server returned an invalid response.",
                    e
                )
            }


        if (!result.ok) {

            error(
                result.error
                    ?: "Feedback delivery failed."
            )
        }


        println(
            "ITINERA: Feedback sent successfully."
        )
    }
}


/**
 * Converts the UI-level FeedbackSubmission into
 * the JSON payload expected by Google Apps Script.
 */
@OptIn(
    ExperimentalEncodingApi::class
)
internal fun FeedbackSubmission.toPayload(
    idToken: String
): FeedbackPayload {

    return FeedbackPayload(

        idToken = idToken,

        category =
            category.wireValue,

        message =
            message.trim(),

        includeAppDetails =
            includeAppDetails,

        appVersion =
            appVersion,

        platform =
            platform,

        attachments =
            attachments.mapIndexed {
                    index,
                    attachment ->

                FeedbackAttachmentPayload(

                    fileName =
                        attachment.fileName
                            .ifBlank {
                                "itinera-feedback-${index + 1}.jpg"
                            },

                    mimeType =
                        attachment.mimeType,

                    contentBase64 =
                        Base64.Default.encode(
                            attachment.bytes
                        )
                )
            }
    )
}