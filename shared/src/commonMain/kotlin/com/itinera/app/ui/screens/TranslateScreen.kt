package com.itinera.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.itinera.app.data.FileSharer
import com.itinera.app.data.TranslateHistoryStore
import com.itinera.app.data.TranslateLang
import com.itinera.app.data.TranslationEntry
import com.itinera.app.data.Translator
import com.itinera.app.data.rememberFileSharer
import com.itinera.app.data.translateLanguages
import com.itinera.app.i18n.LocalStrings
import com.itinera.app.ui.components.TopBar
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

private fun langName(code: String): String =
    translateLanguages.firstOrNull { it.code == code }?.name ?: code

@OptIn(ExperimentalTime::class)
@Composable
fun TranslateScreen(
    translator: Translator,
    onBack: () -> Unit,
) {
    val s = LocalStrings.current
    val clipboard = LocalClipboardManager.current
    val sharer: FileSharer = rememberFileSharer()

    var sourceLang by remember { mutableStateOf(translateLanguages.first { it.code == "en" }) }
    var targetLang by remember { mutableStateOf(translateLanguages.first { it.code == "fr" }) }
    var input by remember { mutableStateOf("") }
    var result by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    var pickingSource by remember { mutableStateOf(false) }
    var pickingTarget by remember { mutableStateOf(false) }

    var history by remember { mutableStateOf(TranslateHistoryStore.all()) }
    var openHistoryId by remember { mutableStateOf<String?>(null) }   // which history card is swiped open

    // ── Swap animation (mirrors the currency screen) ──
    val density = LocalDensity.current
    val slidePx = with(density) { 150.dp.toPx() }   // approx one half's height
    var rotated by remember { mutableStateOf(false) }
    var swapTrigger by remember { mutableStateOf(0) }
    val topSlide = remember { Animatable(0f) }
    val bottomSlide = remember { Animatable(0f) }
    val iconRotation by animateFloatAsState(
        targetValue = if (rotated) 180f else 0f,
        animationSpec = tween(400),
        label = "swapIconRotation",
    )
    LaunchedEffect(swapTrigger) {
        if (swapTrigger == 0) return@LaunchedEffect
        topSlide.snapTo(slidePx)
        bottomSlide.snapTo(-slidePx)
        launch { topSlide.animateTo(0f, tween(400)) }
        bottomSlide.animateTo(0f, tween(400))
    }

    // Live translate: debounce 450ms; save to history on success.
    LaunchedEffect(input, sourceLang.code, targetLang.code) {
        val text = input.trim()
        if (text.isBlank()) {
            result = ""; error = null; loading = false
            return@LaunchedEffect
        }
        delay(450)
        loading = true; error = null
        try {
            result = translator.translate(text, sourceLang.code, targetLang.code)
        } catch (e: Exception) {
            error = s.translateError
            result = ""
        }
        loading = false
    }

    // Save to history only after a longer pause (2s idle), so partial words while
    // typing don't pile up. Keyed on the settled result + input.
    LaunchedEffect(result, input, sourceLang.code, targetLang.code) {
        val text = input.trim()
        val r = result
        if (text.isBlank() || r.isBlank()) return@LaunchedEffect
        delay(2000)
        TranslateHistoryStore.add(
            TranslationEntry(
                id = Clock.System.now().toEpochMilliseconds().toString(),
                sourceText = text,
                resultText = r,
                sourceLang = sourceLang.code,
                targetLang = targetLang.code,
                createdAt = Clock.System.now().toEpochMilliseconds(),
            )
        )
        history = TranslateHistoryStore.all()
    }

    Column(Modifier.fillMaxSize()) {
        TopBar(s.translate, onBack = onBack)

        LazyColumn(
            Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp),
        ) {
            item {
                // ── One connected card: source half / divider / target half ──
                // Swap button overlaid centered on the seam.
                Box(Modifier.fillMaxWidth()) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(Modifier.fillMaxWidth()) {
                            // Source half (editable)
                            FieldHalf(
                                langName = sourceLang.name,
                                onPickLang = { pickingSource = true },
                                modifier = Modifier.offset { IntOffset(0, topSlide.value.toInt()) },
                                trailing = {
                                    if (input.isNotEmpty()) {
                                        IconButton(onClick = { input = "" }) {
                                            Icon(Icons.Filled.Close, contentDescription = s.close)
                                        }
                                    }
                                },
                            ) {
                                BasicTextField(
                                    value = input,
                                    onValueChange = { input = it },
                                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                                        color = MaterialTheme.colorScheme.onSurface,
                                    ),
                                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                                    modifier = Modifier.fillMaxWidth().heightIn(min = 90.dp),
                                    decorationBox = { inner ->
                                        if (input.isEmpty()) {
                                            Text(
                                                s.translateEnterText,
                                                style = MaterialTheme.typography.bodyLarge,
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                                            )
                                        }
                                        inner()
                                    },
                                )
                            }

                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                            )

                            // Target half (result)
                            FieldHalf(
                                langName = targetLang.name,
                                onPickLang = { pickingTarget = true },
                                modifier = Modifier.offset { IntOffset(0, bottomSlide.value.toInt()) },
                                trailing = {
                                    if (result.isNotBlank()) {
                                        Row {
                                            IconButton(onClick = { clipboard.setText(AnnotatedString(result)) }) {
                                                Icon(Icons.Filled.ContentCopy, contentDescription = s.copy, modifier = Modifier.size(20.dp))
                                            }
                                            IconButton(onClick = { sharer.shareText(result) }) {
                                                Icon(Icons.Filled.Share, contentDescription = s.share, modifier = Modifier.size(20.dp))
                                            }
                                        }
                                    }
                                },
                            ) {
                                Box(Modifier.fillMaxWidth().heightIn(min = 90.dp)) {
                                    when {
                                        loading -> Row(verticalAlignment = Alignment.CenterVertically) {
                                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                                            Spacer(Modifier.width(10.dp))
                                            Text(s.translating, style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                        }
                                        error != null -> Text(error!!, style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.error)
                                        result.isNotBlank() -> SelectionContainer {
                                            Text(result, style = MaterialTheme.typography.bodyLarge)
                                        }
                                        else -> Text(s.translationLabel, style = MaterialTheme.typography.bodyLarge,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                                    }
                                }
                            }
                        }
                    }

                    // Swap button, centered over the seam between the two halves
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 4.dp,
                        border = androidx.compose.foundation.BorderStroke(
                            3.dp, MaterialTheme.colorScheme.surfaceVariant,
                        ),
                        modifier = Modifier.align(Alignment.Center),
                    ) {
                        IconButton(onClick = {
                            val t = sourceLang; sourceLang = targetLang; targetLang = t
                            if (result.isNotBlank()) input = result
                            rotated = !rotated
                            swapTrigger++
                        }) {
                            Icon(
                                Icons.Filled.SwapVert,
                                contentDescription = "Swap languages",
                                modifier = Modifier.rotate(iconRotation),
                            )
                        }
                    }
                }
            }

            // ── History / favorites ──
            if (history.isNotEmpty()) {
                item {
                    Row(
                        Modifier.fillMaxWidth().padding(top = 24.dp, bottom = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(s.history, style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                        TextButton(onClick = {
                            TranslateHistoryStore.clearNonFavorites()
                            history = TranslateHistoryStore.all()
                        }) { Text(s.clear) }
                    }
                }
                items(history, key = { it.id }) { entry ->
                    SwipeableHistoryCard(
                        isSwipeOpen = openHistoryId == entry.id,
                        onSwipeOpenChange = { open -> openHistoryId = if (open) entry.id else null },
                        onDelete = {
                            TranslateHistoryStore.remove(entry.id)
                            history = TranslateHistoryStore.all()
                            openHistoryId = null
                        },
                    ) {
                        HistoryRow(
                            entry = entry,
                            onTap = {
                                sourceLang = translateLanguages.first { it.code == entry.sourceLang }
                                targetLang = translateLanguages.first { it.code == entry.targetLang }
                                input = entry.sourceText
                            },
                            onToggleFavorite = {
                                TranslateHistoryStore.toggleFavorite(entry.id)
                                history = TranslateHistoryStore.all()
                            },
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }

    if (pickingSource) {
        LangPickerDialog(current = sourceLang, onPick = { sourceLang = it; pickingSource = false }, onDismiss = { pickingSource = false })
    }
    if (pickingTarget) {
        LangPickerDialog(current = targetLang, onPick = { targetLang = it; pickingTarget = false }, onDismiss = { pickingTarget = false })
    }
}

/** A half of the joined translate card: a language pill (tappable) + trailing action + content. */
@Composable
private fun FieldHalf(
    langName: String,
    onPickLang: () -> Unit,
    modifier: Modifier = Modifier,
    trailing: @Composable () -> Unit = {},
    content: @Composable () -> Unit,
) {
    Column(modifier.fillMaxWidth().padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // language pill / chip button
            Surface(
                shape = RoundedCornerShape(50),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
                modifier = Modifier.clickable { onPickLang() },
            ) {
                Row(
                    Modifier.padding(start = 14.dp, end = 8.dp, top = 6.dp, bottom = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        langName,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Icon(
                        Icons.Filled.ArrowDropDown,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
            Spacer(Modifier.weight(1f))
            trailing()
        }
        Spacer(Modifier.height(10.dp))
        content()
    }
}

@Composable
private fun HistoryRow(
    entry: TranslationEntry,
    onTap: () -> Unit,
    onToggleFavorite: () -> Unit,
) {
    val s = LocalStrings.current
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        modifier = Modifier.fillMaxWidth().clickable { onTap() },
    ) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    "${langName(entry.sourceLang)} → ${langName(entry.targetLang)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                )
                Spacer(Modifier.height(2.dp))
                Text(entry.sourceText, style = MaterialTheme.typography.bodyMedium, maxLines = 1)
                Text(
                    entry.resultText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                )
            }
            IconButton(onClick = onToggleFavorite) {
                Icon(
                    if (entry.favorite) Icons.Filled.Star else Icons.Filled.StarBorder,
                    contentDescription = s.favorite,
                    tint = if (entry.favorite) Color(0xFFE8B931) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}

/**
 * Wraps a history row with swipe-to-reveal-delete + slide-out animation,
 * matching the Weather / World Clock / expense cards. Tap (reload) and the
 * favorite star stay on the row itself.
 */
@Composable
private fun SwipeableHistoryCard(
    isSwipeOpen: Boolean,
    onSwipeOpenChange: (Boolean) -> Unit,
    onDelete: () -> Unit,
    content: @Composable () -> Unit,
) {
    val s = LocalStrings.current
    val density = LocalDensity.current
    val actionWidth = 80.dp
    val gap = 15.dp
    val panelWidth = actionWidth + gap
    val panelPx = with(density) { panelWidth.toPx() }
    val scope = rememberCoroutineScope()
    val offsetX = remember { Animatable(0f) }
    val exitOffsetX = remember { Animatable(0f) }

    val progress = ((-offsetX.value - with(density) { gap.toPx() }) /
            (panelPx - with(density) { gap.toPx() })).coerceIn(0f, 1f)

    LaunchedEffect(isSwipeOpen) {
        if (!isSwipeOpen && offsetX.value != 0f) offsetX.animateTo(0f, spring(stiffness = Spring.StiffnessMedium))
    }

    fun animateOutThenDelete() {
        scope.launch {
            val slide = with(density) { (panelWidth + 600.dp).toPx() }
            exitOffsetX.animateTo(-slide, tween(durationMillis = 300))
            onDelete()
        }
    }

    Box(
        Modifier
            .fillMaxWidth()
            .offset { IntOffset(exitOffsetX.value.roundToInt(), 0) },
    ) {
        if (offsetX.value != 0f) {
            Row(
                Modifier.matchParentSize().clip(RoundedCornerShape(14.dp)),
                horizontalArrangement = Arrangement.End,
            ) {
                Column(Modifier.width(panelWidth).fillMaxHeight().padding(start = gap)) {
                    Column(
                        Modifier.fillMaxHeight().clickable(
                            onClick = { animateOutThenDelete() },
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() },
                        ),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Box(
                            Modifier.size(52.dp)
                                .graphicsLayer { scaleX = progress; scaleY = progress; alpha = progress }
                                .clip(CircleShape)
                                .background(Color(0xFFB23B3B)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(Icons.Outlined.Delete, contentDescription = s.delete, tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                        Spacer(Modifier.height(5.dp))
                        Text(s.delete, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f), style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }

        Box(
            Modifier
                .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onHorizontalDrag = { _, dragAmount ->
                            scope.launch {
                                offsetX.snapTo((offsetX.value + dragAmount).coerceIn(-panelPx, 0f))
                            }
                        },
                        onDragEnd = {
                            scope.launch {
                                if (offsetX.value < -panelPx / 2) {
                                    offsetX.animateTo(-panelPx, spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMediumLow))
                                    onSwipeOpenChange(true)
                                } else {
                                    offsetX.animateTo(0f, spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMedium))
                                    onSwipeOpenChange(false)
                                }
                            }
                        },
                    )
                },
        ) {
            content()
        }
    }
}

@Composable
private fun LangPickerDialog(
    current: TranslateLang,
    onPick: (TranslateLang) -> Unit,
    onDismiss: () -> Unit,
) {
    val s = LocalStrings.current
    var query by remember { mutableStateOf("") }
    val filtered = remember(query) {
        if (query.isBlank()) translateLanguages
        else translateLanguages.filter { it.name.contains(query.trim(), ignoreCase = true) }
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text(s.close) } },
        title = { Text(s.languageLabel) },
        text = {
            Column {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    label = { Text(s.searchLanguage) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                )
                Spacer(Modifier.height(8.dp))
                LazyColumn(Modifier.heightIn(max = 320.dp)) {
                    items(filtered, key = { it.code }) { lang ->
                        Row(
                            Modifier.fillMaxWidth().clickable { onPick(lang) }
                                .padding(vertical = 12.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(lang.name, Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
                            if (lang.code == current.code) Text("✓", color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        },
    )
}