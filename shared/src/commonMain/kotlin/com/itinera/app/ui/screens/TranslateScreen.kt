package com.itinera.app.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
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

/**
 * Comparison key for history de-duplication.
 *
 * Case, trailing punctuation and repeated spaces shouldn't make two entries:
 * "How are you today", "how are you today." and "how  are you today" are the
 * same lookup. Only used for comparing — what's stored and displayed is
 * whatever the user actually typed.
 */
private fun normalizeForHistory(text: String): String =
    text.trim()
        .lowercase()
        .replace(Regex("\\s+"), " ")
        .trimEnd('.', ',', '!', '?', ';', ':', '…', ' ')

@OptIn(ExperimentalTime::class, ExperimentalLayoutApi::class)
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
    var showingFullscreen by remember { mutableStateOf(false) }       // ⬅ ADD

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
        // ⬅ CHANGED — was an exact sourceText comparison, so "how are you today"
        // and "how are you today." were two entries.
        val key = normalizeForHistory(text)
        val samePair = TranslateHistoryStore.all().filter {
            it.sourceLang == sourceLang.code && it.targetLang == targetLang.code
        }
        if (samePair.any { normalizeForHistory(it.sourceText) == key }) return@LaunchedEffect

        // The save fires after a 2s pause, so pausing mid-sentence stored
        // "how are you" before you'd finished typing "how are you today".
        // Drop those partials — but never a favourite, which was deliberate.
        samePair
            .filter { !it.favorite && key.startsWith(normalizeForHistory(it.sourceText) + " ") }
            .forEach { TranslateHistoryStore.remove(it.id) }

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

    val favorites = remember(history) { history.filter { it.favorite } }
    val recents = remember(history) { history.filterNot { it.favorite } }

    Column(Modifier.fillMaxSize()) {
        TopBar(s.translate, onBack = onBack)

        LazyColumn(
            Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp),
        ) {
            item {
                Box(Modifier.fillMaxWidth()) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(Modifier.fillMaxWidth()) {
                            // ⬅ CHANGED — source is now the quieter half. You type the
                            // input once and read the output repeatedly, so the
                            // emphasis was the wrong way round.
                            FieldHalf(
                                langName = sourceLang.name,
                                onPickLang = { pickingSource = true },
                                modifier = Modifier.offset { IntOffset(0, topSlide.value.toInt()) },
                                trailing = {
                                    if (input.isNotEmpty()) {
                                        IconButton(onClick = { input = "" }) {
                                            Icon(
                                                Icons.Filled.Close,
                                                contentDescription = s.close,
                                                modifier = Modifier.size(20.dp),
                                            )
                                        }
                                    }
                                },
                            ) {
                                BasicTextField(
                                    value = input,
                                    onValueChange = { input = it },
                                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                                    ),
                                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                                    // ⬅ CHANGED — was 90.dp on both halves, so a short
                                    // phrase left most of the card empty.
                                    modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
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

                            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f))

                            // ⬅ ADD — tinted panel. The translation is what you read
                            // (or hold up to someone), so it leads.
                            Box(Modifier.background(MaterialTheme.colorScheme.primary.copy(alpha = 0.09f))) {
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
                                    Column(Modifier.fillMaxWidth().heightIn(min = 56.dp)) {
                                        when {
                                            loading -> Row(verticalAlignment = Alignment.CenterVertically) {
                                                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                                                Spacer(Modifier.width(10.dp))
                                                Text(
                                                    s.translating,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                                )
                                            }
                                            error != null -> Text(
                                                error!!,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.error,
                                            )
                                            result.isNotBlank() -> SelectionContainer {
                                                Text(
                                                    result,
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.Medium,
                                                )
                                            }
                                            else -> Text(
                                                s.translationLabel,
                                                style = MaterialTheme.typography.bodyLarge,
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                                            )
                                        }

                                        // ⬅ ADD — the actual use case is holding the phone
                                        // up to someone. A 16sp line at arm's length
                                        // doesn't work.
                                        if (result.isNotBlank() && !loading) {
                                            Spacer(Modifier.height(12.dp))
                                            Surface(
                                                shape = RoundedCornerShape(20.dp),
                                                color = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.clickable { showingFullscreen = true },
                                            ) {
                                                Row(
                                                    Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                ) {
                                                    Icon(
                                                        Icons.Filled.Fullscreen,
                                                        contentDescription = null,
                                                        tint = MaterialTheme.colorScheme.onPrimary,
                                                        modifier = Modifier.size(16.dp),
                                                    )
                                                    Spacer(Modifier.width(6.dp))
                                                    Text(
                                                        s.showLabel,
                                                        style = MaterialTheme.typography.labelLarge,
                                                        color = MaterialTheme.colorScheme.onPrimary,
                                                    )
                                                }
                                            }
                                        }
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
                        border = BorderStroke(3.dp, MaterialTheme.colorScheme.surfaceVariant),
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
                                contentDescription = s.swap,
                                modifier = Modifier.rotate(iconRotation),
                            )
                        }
                    }
                }
            }

            // ⬅ ADD — the phrases a traveller actually needs and can't improvise.
            // Only while the box is empty; they'd be noise otherwise.
            if (input.isBlank()) {
                item {
                    Column(Modifier.fillMaxWidth().padding(top = 20.dp)) {
                        Text(
                            s.commonPhrases,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        )
                        Spacer(Modifier.height(8.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            commonPhrases(s).forEach { phrase ->
                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = Color.Transparent,
                                    border = BorderStroke(
                                        0.5.dp,
                                        MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                                    ),
                                    modifier = Modifier.clickable { input = phrase },
                                ) {
                                    Text(
                                        phrase,
                                        style = MaterialTheme.typography.labelMedium,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ⬅ CHANGED — favourites and recents were one undifferentiated list.
            // Saved phrases are a phrasebook you built; recents are disposable.
            if (favorites.isNotEmpty()) {
                item {
                    Row(
                        Modifier.fillMaxWidth().padding(top = 24.dp, bottom = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            Icons.Filled.Star,
                            contentDescription = null,
                            tint = Color(0xFFE8B931),
                            modifier = Modifier.size(15.dp),
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            s.savedLabel,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
                items(favorites, key = { "fav-${it.id}" }) { entry ->
                    TranslateEntryCard(
                        entry = entry,
                        isSwipeOpen = openHistoryId == entry.id,
                        onSwipeOpenChange = { open -> openHistoryId = if (open) entry.id else null },
                        onDelete = {
                            TranslateHistoryStore.remove(entry.id)
                            history = TranslateHistoryStore.all()
                            openHistoryId = null
                        },
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
                    Spacer(Modifier.height(8.dp))
                }
            }

            if (recents.isNotEmpty()) {
                item {
                    Row(
                        Modifier.fillMaxWidth().padding(top = 20.dp, bottom = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            s.recentLabel,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.weight(1f),
                        )
                        TextButton(onClick = {
                            TranslateHistoryStore.clearNonFavorites()
                            history = TranslateHistoryStore.all()
                        }) { Text(s.clear) }
                    }
                }
                items(recents, key = { it.id }) { entry ->
                    TranslateEntryCard(
                        entry = entry,
                        isSwipeOpen = openHistoryId == entry.id,
                        onSwipeOpenChange = { open -> openHistoryId = if (open) entry.id else null },
                        onDelete = {
                            TranslateHistoryStore.remove(entry.id)
                            history = TranslateHistoryStore.all()
                            openHistoryId = null
                        },
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
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }

    if (showingFullscreen && result.isNotBlank()) {
        ShowTranslationOverlay(
            sourceText = input.trim(),
            resultText = result,
            fromLang = sourceLang.name,
            toLang = targetLang.name,
            onCopy = { clipboard.setText(AnnotatedString(result)) },
            onShare = { sharer.shareText(result) },
            onDismiss = { showingFullscreen = false },
        )
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
// ─────────────────────────────────────────────────────────────────────────────
// Common phrases, entry card, fullscreen show
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Starters a traveller needs and can't improvise. The allergy one especially —
 * that's the phrase you can't afford to get wrong or paraphrase.
 */
private fun commonPhrases(s: com.itinera.app.i18n.Strings): List<String> = listOf(
    s.phraseWhereIs,
    s.phraseHowMuch,
    s.phraseTableForTwo,
    s.phraseAllergicTo,
    s.phraseBillPlease,
    s.phraseHelpMe,
)

/** History row + swipe-to-delete, so both sections render identically. */
@Composable
private fun TranslateEntryCard(
    entry: TranslationEntry,
    isSwipeOpen: Boolean,
    onSwipeOpenChange: (Boolean) -> Unit,
    onDelete: () -> Unit,
    onTap: () -> Unit,
    onToggleFavorite: () -> Unit,
) {
    SwipeableHistoryCard(
        isSwipeOpen = isSwipeOpen,
        onSwipeOpenChange = onSwipeOpenChange,
        onDelete = onDelete,
    ) {
        HistoryRow(entry = entry, onTap = onTap, onToggleFavorite = onToggleFavorite)
    }
}

/**
 * Fullscreen result, for holding the phone up to someone.
 *
 * NOTE — keeping the screen awake and lifting brightness needs an expect/actual
 * (FLAG_KEEP_SCREEN_ON + WindowManager.LayoutParams.screenBrightness on Android;
 * UIApplication.isIdleTimerDisabled + UIScreen.brightness on iOS). Left out
 * deliberately so this change stays in one file — see the follow-up commit.
 */
@Composable
private fun ShowTranslationOverlay(
    sourceText: String,
    resultText: String,
    fromLang: String,
    toLang: String,
    onCopy: () -> Unit,
    onShare: () -> Unit,
    onDismiss: () -> Unit,
) {
    val s = LocalStrings.current
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            Column(Modifier.fillMaxSize().padding(24.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "$fromLang → $toLang",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        modifier = Modifier.weight(1f),
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Filled.Close, contentDescription = s.close)
                    }
                }

                Column(
                    Modifier.weight(1f).fillMaxWidth(),
                    verticalArrangement = Arrangement.Center,
                ) {
                    if (sourceText.isNotBlank()) {
                        Text(
                            sourceText,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f),
                        )
                        Spacer(Modifier.height(16.dp))
                    }
                    SelectionContainer {
                        Text(
                            resultText,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Medium,
                            lineHeight = MaterialTheme.typography.headlineMedium.fontSize * 1.3,
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OverlayAction(Icons.Filled.ContentCopy, s.copy, Modifier.weight(1f), onCopy)
                    OverlayAction(Icons.Filled.Share, s.share, Modifier.weight(1f), onShare)
                }
            }
        }
    }
}

@Composable
private fun OverlayAction(
    icon: ImageVector,
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Surface(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        color = Color.Transparent,
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
    ) {
        Row(
            Modifier.padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(17.dp))
            Spacer(Modifier.width(8.dp))
            Text(label, style = MaterialTheme.typography.labelLarge, textAlign = TextAlign.Center)
        }
    }
}