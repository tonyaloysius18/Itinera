package com.itinera.app.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.TipsAndUpdates
import androidx.compose.material.icons.outlined.AddPhotoAlternate
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.rememberAsyncImagePainter
import com.itinera.app.data.FeedbackAttachment
import com.itinera.app.data.FeedbackCategory
import com.itinera.app.data.FeedbackService
import com.itinera.app.data.FeedbackSubmission
import com.itinera.app.data.compressFeedbackImage
import com.itinera.app.getPlatform
import com.itinera.app.i18n.LocalStrings
import com.itinera.app.ui.components.TopBar
import com.preat.peekaboo.image.picker.SelectionMode
import com.preat.peekaboo.image.picker.rememberImagePickerLauncher
import kotlinx.coroutines.launch

private const val FeedbackMessageLimit = 1000
private const val FeedbackImageLimit = 3
private const val FeedbackImageBytesLimit = 5 * 1024 * 1024

private data class SelectedFeedbackImage(
    val id: Int,
    val bytes: ByteArray,
)

@Composable
fun FeedbackScreen(
    onBack: () -> Unit,
    appVersion: String = "1.0.0",
) {
    val s = LocalStrings.current
    val scope = rememberCoroutineScope()
    val feedbackService = remember { FeedbackService() }
    val selectedImages = remember { mutableStateListOf<SelectedFeedbackImage>() }

    var category by remember { mutableStateOf(FeedbackCategory.PROBLEM) }
    var message by remember { mutableStateOf("") }
    var includeAppDetails by remember { mutableStateOf(true) }
    var sending by remember { mutableStateOf(false) }
    var processingImage by remember { mutableStateOf(false) }
    var messageError by remember { mutableStateOf<String?>(null) }
    var attachmentError by remember { mutableStateOf<String?>(null) }
    var sendError by remember { mutableStateOf<String?>(null) }
    var feedbackSent by remember { mutableStateOf(false) }
    var nextImageId by remember { mutableStateOf(0) }

    val imagePicker = rememberImagePickerLauncher(
        selectionMode = SelectionMode.Single,
        scope = scope,
        onResult = { results ->
            results.firstOrNull()?.let { sourceBytes ->
                scope.launch {
                    processingImage = true
                    attachmentError = null
                    try {
                        val compressed = compressFeedbackImage(sourceBytes)
                        val total = selectedImages.sumOf { it.bytes.size } + compressed.size
                        if (total > FeedbackImageBytesLimit) {
                            attachmentError = s.feedbackImagesTooLarge
                        } else if (selectedImages.size < FeedbackImageLimit) {
                            selectedImages += SelectedFeedbackImage(nextImageId++, compressed)
                        }
                    } catch (_: Exception) {
                        attachmentError = s.feedbackImagesTooLarge
                    } finally {
                        processingImage = false
                    }
                }
            }
        },
    )

    fun submit() {
        messageError = if (message.trim().length < 10) s.feedbackInvalidMessage else null
        sendError = null
        if (messageError != null || attachmentError != null) return

        val submission = FeedbackSubmission(
            category = category,
            message = message,
            includeAppDetails = includeAppDetails,
            appVersion = appVersion,
            platform = getPlatform().name,
            attachments = selectedImages.mapIndexed { index, image ->
                FeedbackAttachment(
                    bytes = image.bytes,
                    fileName = "itinera-feedback-${index + 1}.jpg",
                )
            },
        )

        sending = true
        scope.launch {
            try {
                feedbackService.send(submission)
                feedbackSent = true
            } catch (e: Exception) {

                println(
                    "ITINERA: FEEDBACK SEND FAILED — ${e.message}"
                )

                e.printStackTrace()

                // Temporary while debugging
                sendError =
                    e.message ?: s.feedbackSendFailed

            } finally {
                sending = false
            }
        }
    }

    Column(Modifier.fillMaxSize()) {
        TopBar(s.sendFeedback, onBack = onBack)

        if (feedbackSent) {
            FeedbackSuccess(
                onDone = onBack,
                modifier = Modifier.weight(1f),
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .imePadding(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    bottom = 24.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                item { FeedbackIntro() }

                item {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        FeedbackFieldLabel(s.feedbackTopic, required = true)
                        Row(
                            modifier = Modifier.fillMaxWidth().selectableGroup(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            FeedbackCategoryCard(
                                label = s.feedbackProblem,
                                icon = Icons.Filled.BugReport,
                                selected = category == FeedbackCategory.PROBLEM,
                                onClick = { category = FeedbackCategory.PROBLEM },
                                modifier = Modifier.weight(1f),
                            )
                            FeedbackCategoryCard(
                                label = s.feedbackSuggestion,
                                icon = Icons.Filled.Lightbulb,
                                selected = category == FeedbackCategory.SUGGESTION,
                                onClick = { category = FeedbackCategory.SUGGESTION },
                                modifier = Modifier.weight(1f),
                            )
                            FeedbackCategoryCard(
                                label = s.feedbackGeneral,
                                icon = Icons.AutoMirrored.Filled.Message,
                                selected = category == FeedbackCategory.GENERAL,
                                onClick = { category = FeedbackCategory.GENERAL },
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }

                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        FeedbackFieldLabel(s.yourFeedback, required = true)
                        OutlinedTextField(
                            value = message,
                            onValueChange = {
                                message = it.take(FeedbackMessageLimit)
                                if (messageError != null && message.trim().length >= 10) messageError = null
                            },
                            placeholder = { Text(s.feedbackPlaceholder) },
                            minLines = 5,
                            maxLines = 8,
                            isError = messageError != null,
                            supportingText = {
                                Row(Modifier.fillMaxWidth()) {
                                    Text(
                                        messageError.orEmpty(),
                                        color = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.weight(1f),
                                    )
                                    Text("${message.length} / $FeedbackMessageLimit")
                                }
                            },
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }

                item {
                    FeedbackAttachments(
                        images = selectedImages,
                        processing = processingImage,
                        error = attachmentError,
                        onAdd = {
                            if (selectedImages.size < FeedbackImageLimit && !processingImage) imagePicker.launch()
                        },
                        onRemove = { id ->
                            selectedImages.removeAll { it.id == id }
                            attachmentError = null
                        },
                    )
                }

                item {
                    AppDetailsToggle(
                        checked = includeAppDetails,
                        onCheckedChange = { includeAppDetails = it },
                    )
                }

                if (sendError != null) {
                    item { FeedbackError(sendError.orEmpty()) }
                }

                item {
                    Button(
                        onClick = ::submit,
                        enabled = !sending && !processingImage,
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier.fillMaxWidth().height(54.dp),
                    ) {
                        if (sending) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary,
                            )
                        } else {
                            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null)
                            Spacer(Modifier.size(9.dp))
                            Text(s.reviewAndSend, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                item {
                    Text(
                        s.feedbackSensitiveImageWarning,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                    )
                }

                item {
                    Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.safeDrawing))
                }
            }
        }
    }
}

@Composable
private fun FeedbackIntro() {
    val s = LocalStrings.current
    Surface(
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
        contentColor = MaterialTheme.colorScheme.onSurface,
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.16f), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Filled.TipsAndUpdates, contentDescription = null)
            }
            Spacer(Modifier.size(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    s.feedbackIntroTitle,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(Modifier.height(3.dp))
                Text(s.feedbackIntroSubtitle, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun FeedbackFieldLabel(label: String, required: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium,
        )
        if (required) {
            Text(
                " *",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.error,
            )
        }
    }
}

@Composable
private fun FeedbackCategoryCard(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .selectable(
                selected = selected,
                role = Role.RadioButton,
                onClick = onClick,
            ),
        shape = RoundedCornerShape(16.dp),
        color = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
        else MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
        ),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 6.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(7.dp),
        ) {
            Icon(
                imageVector = if (selected) Icons.Filled.Check else icon,
                contentDescription = null,
                tint = if (selected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                color = if (selected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun FeedbackAttachments(
    images: List<SelectedFeedbackImage>,
    processing: Boolean,
    error: String?,
    onAdd: () -> Unit,
    onRemove: (Int) -> Unit,
) {
    val s = LocalStrings.current
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            FeedbackFieldLabel(s.screenshots, required = false)
            Text(
                " · ${s.optional}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (images.isNotEmpty()) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                images.forEach { image ->
                    Box(
                        modifier = Modifier
                            .size(88.dp)
                            .clip(RoundedCornerShape(16.dp)),
                    ) {
                        Image(
                            painter = rememberAsyncImagePainter(image.bytes),
                            contentDescription = s.screenshots,
                            modifier = Modifier.fillMaxSize(),
                        )
                        IconButton(
                            onClick = { onRemove(image.id) },
                            modifier = Modifier.align(Alignment.TopEnd),
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.scrim.copy(alpha = 0.72f),
                                contentColor = androidx.compose.ui.graphics.Color.White,
                            ) {
                                Icon(
                                    Icons.Filled.Close,
                                    contentDescription = s.removePhoto,
                                    modifier = Modifier.padding(5.dp).size(16.dp),
                                )
                            }
                        }
                    }
                }
            }
        }

        if (images.size < FeedbackImageLimit) {
            Surface(
                onClick = onAdd,
                enabled = !processing,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                contentColor = MaterialTheme.colorScheme.primary,
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.34f),
                ),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                    ) {
                        Box(Modifier.size(46.dp), contentAlignment = Alignment.Center) {
                            if (processing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(22.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.onPrimary,
                                )
                            } else {
                                Icon(
                                    Icons.Outlined.AddPhotoAlternate,
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp),
                                )
                            }
                        }
                    }
                    Spacer(Modifier.size(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            s.addPhoto,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            s.feedbackImageLimit,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Icon(Icons.Filled.ChevronRight, contentDescription = null)
                }
            }
        }
        if (images.size == FeedbackImageLimit) {
            Text(
                s.feedbackImageLimit,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (error != null) {
            Text(error, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
        }
    }
}

@Composable
private fun AppDetailsToggle(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    val s = LocalStrings.current
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    s.includeAppDetails,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                )
                Spacer(Modifier.height(3.dp))
                Text(
                    s.appDetailsDescription,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.size(12.dp))
            Switch(checked = checked, onCheckedChange = onCheckedChange)
        }
    }
}

@Composable
private fun FeedbackError(message: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.errorContainer,
        contentColor = MaterialTheme.colorScheme.onErrorContainer,
    ) {
        Text(message, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(14.dp))
    }
}

@Composable
private fun FeedbackSuccess(
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val s = LocalStrings.current
    Column(
        modifier = modifier.fillMaxWidth().padding(horizontal = 28.dp).navigationBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f), RoundedCornerShape(24.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Filled.Check,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(38.dp),
            )
        }
        Spacer(Modifier.height(22.dp))
        Text(s.feedbackSent, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))
        Text(
            s.feedbackSentSubtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(26.dp))
        Button(
            onClick = onDone,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth().height(52.dp),
        ) {
            Text(s.done)
        }
    }
}
