package com.itinera.app.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.itinera.app.data.NeraActivity
import com.itinera.app.data.NeraItinerary
import com.itinera.app.data.NeraService
import com.itinera.app.data.NeraException
import com.itinera.app.data.NeraFailure
import com.itinera.app.data.NeraTurn
import com.itinera.app.i18n.LocalStrings
import com.itinera.app.i18n.Strings
import com.itinera.app.model.label
import com.itinera.app.resources.Res
import com.itinera.app.resources.nera_head
import com.itinera.app.ui.components.NeraThinking
import com.itinera.app.ui.components.TopBar
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.painterResource

/** One row in the Nera conversation. */
private sealed interface NeraItem {
    data class FromUser(val text: String) : NeraItem
    data class FromNera(val text: String, val quickReplies: List<String> = emptyList()) : NeraItem
    data class Draft(val message: String, val itinerary: NeraItinerary) : NeraItem
    data class Problem(val text: String) : NeraItem
}

/**
 * Chat with Nera. Nera asks for anything missing (party, dates, budget), then proposes a
 * draft itinerary. The draft only becomes a real trip when the traveller taps Approve.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NeraChatScreen(
    service: NeraService,
    onBack: () -> Unit,
    onApprove: (NeraItinerary) -> Unit,
) {
    val s = LocalStrings.current
    val items = remember {
        mutableStateListOf<NeraItem>(
            NeraItem.FromNera(s.neraWelcome, listOf(s.neraSuggest1, s.neraSuggest2, s.neraSuggest3)),
        )
    }
    var input by remember { mutableStateOf("") }
    var sending by remember { mutableStateOf(false) }
    var approved by remember { mutableStateOf(false) }
    var trialDaysLeft by remember { mutableStateOf<Int?>(null) }   // null = no trial info (not enforced, or paid)
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    val latestDraftIndex = items.indexOfLast { it is NeraItem.Draft }
    val currentDraft = (items.getOrNull(latestDraftIndex) as? NeraItem.Draft)?.itinerary

    // Keep the newest message in view as the conversation grows.
    LaunchedEffect(items.size, sending) {
        val last = items.size - 1 + if (sending) 1 else 0
        if (last >= 0) listState.animateScrollToItem(last)
    }

    fun send(text: String) {
        val message = text.trim()
        if (message.isEmpty() || sending) return
        items.add(NeraItem.FromUser(message))
        input = ""
        sending = true
        // Draft cards are sent as their intro text; the draft itself travels separately.
        val history = items.mapNotNull {
            when (it) {
                is NeraItem.FromUser -> NeraTurn("user", it.text)
                is NeraItem.FromNera -> NeraTurn("assistant", it.text)
                is NeraItem.Draft -> NeraTurn("assistant", it.message.ifBlank { "Here is a draft itinerary." })
                is NeraItem.Problem -> null
            }
        }
        scope.launch {
            try {
                val reply = service.send(history, currentDraft)
                val trial = reply.entitlement
                trialDaysLeft = if (trial?.status == "trial") trial.daysLeft else null
                val itinerary = reply.itinerary
                if (reply.type == "itinerary" && itinerary != null) {
                    items.add(NeraItem.Draft(reply.message, itinerary))
                } else {
                    items.add(NeraItem.FromNera(reply.message, reply.quickReplies))
                }
            } catch (e: NeraException) {
                // The trial ending is not a fault: show it as an ordinary message from Nera, not a red error.
                if (e.failure == NeraFailure.TRIAL_ENDED) {
                    items.add(NeraItem.FromNera(s.neraErrTrialEnded))
                } else items.add(NeraItem.Problem(when (e.failure) {
                    NeraFailure.NOT_CONFIGURED -> s.neraErrNotSetUp
                    NeraFailure.SIGN_IN -> s.neraErrSignIn
                    NeraFailure.NETWORK -> s.neraErrNetwork
                    NeraFailure.BAD_REPLY -> s.neraErrBadReply
                    NeraFailure.QUOTA -> s.neraErrQuota
                    NeraFailure.TRIAL_ENDED -> s.neraErrTrialEnded   // handled above; keeps the when exhaustive
                    NeraFailure.GENERIC -> s.neraErrGeneric
                }))
            } finally {
                sending = false
            }
        }
    }

    Column(Modifier.fillMaxSize().imePadding()) {
        TopBar("Nera", onBack = onBack)
        trialDaysLeft?.let { days ->
            Text(
                s.neraTrialDaysLeft.replace("%s", days.toString()),
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 4.dp),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
            )
        }

        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            itemsIndexed(items) { index, item ->
                when (item) {
                    is NeraItem.FromUser -> Bubble(item.text, fromUser = true)
                    is NeraItem.FromNera -> {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Bubble(item.text, fromUser = false)
                            if (index == items.lastIndex && !sending && item.quickReplies.isNotEmpty()) {
                                QuickReplies(item.quickReplies, onPick = ::send)
                            }
                        }
                    }
                    is NeraItem.Draft -> {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (item.message.isNotBlank()) Bubble(item.message, fromUser = false)
                            DraftCard(
                                itinerary = item.itinerary,
                                s = s,
                                isLatest = index == latestDraftIndex,
                                actionsEnabled = !sending && !approved,
                                onApprove = { approved = true; onApprove(item.itinerary) },
                                onChange = {
                                    items.add(NeraItem.FromNera(
                                        s.neraChangePrompt,
                                        listOf(s.neraChange1, s.neraChange2, s.neraChange3, s.neraChange4),
                                    ))
                                },
                            )
                        }
                    }
                    is NeraItem.Problem -> Bubble(item.text, fromUser = false, isError = true)
                }
            }
            if (sending) {
                item { NeraThinking() }
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
        Row(
            Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedTextField(
                value = input,
                onValueChange = { input = it.take(2000) },
                modifier = Modifier.weight(1f),
                placeholder = { Text(s.neraInputHint) },
                shape = RoundedCornerShape(24.dp),
                maxLines = 4,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = { send(input) }),
            )
            Spacer(Modifier.padding(start = 8.dp))
            FilledIconButton(onClick = { send(input) }, enabled = input.isNotBlank() && !sending) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = s.neraSend)
            }
        }
    }
}

@Composable
private fun Bubble(text: String, fromUser: Boolean, isError: Boolean = false) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = if (fromUser) Arrangement.End else Arrangement.Start) {
        if (!fromUser) {
            Image(
                painter = painterResource(Res.drawable.nera_head),
                contentDescription = null,
                modifier = Modifier.padding(end = 8.dp, top = 2.dp).size(32.dp),
            )
        }
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = when {
                fromUser -> MaterialTheme.colorScheme.primary
                isError -> MaterialTheme.colorScheme.errorContainer
                else -> MaterialTheme.colorScheme.surfaceVariant
            },
            modifier = Modifier.widthIn(max = if (fromUser) 320.dp else 280.dp),
        ) {
            Text(
                text,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                style = MaterialTheme.typography.bodyLarge,
                color = when {
                    fromUser -> MaterialTheme.colorScheme.onPrimary
                    isError -> MaterialTheme.colorScheme.onErrorContainer
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                },
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun QuickReplies(options: List<String>, onPick: (String) -> Unit) {
    FlowRow(
        modifier = Modifier.padding(start = 40.dp),   // line up under the message text, past the avatar
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        options.forEach { option ->
            SuggestionChip(onClick = { onPick(option) }, label = { Text(option) })
        }
    }
}

/** "★ 4.6 (9,012) · $$" from whatever Nera got from place search; null when there is nothing to show. */
private fun ratingLine(a: NeraActivity, s: Strings): String? {
    val parts = buildList {
        if (a.rating > 0) {
            val count = if (a.ratingCount > 0) " (${a.ratingCount.withThousands()})" else ""
            add("★ ${a.rating}$count")
        }
        if (a.price.isNotBlank()) add(if (a.price == "free") s.neraFree else a.price)
    }
    return parts.takeIf { it.isNotEmpty() }?.joinToString(" · ")
}

private fun Int.withThousands(): String =
    toString().reversed().chunked(3).joinToString(",").reversed()

@Composable
private fun DraftCard(
    itinerary: NeraItinerary,
    s: Strings,
    isLatest: Boolean,
    actionsEnabled: Boolean,
    onApprove: () -> Unit,
    onChange: () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Column {
                Text(itinerary.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                val dates = itinerary.days.mapNotNull { runCatching { LocalDate.parse(it.date) }.getOrNull() }
                val range = if (dates.isNotEmpty()) "${dates.first().label()} – ${dates.last().label()} · " else ""
                Text(
                    "$range${s.neraDaysDraft.replace("%s", itinerary.days.size.toString())}",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                )
            }

            itinerary.days.forEachIndexed { n, day ->
                val date = runCatching { LocalDate.parse(day.date) }.getOrNull()
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        buildString {
                            append(s.neraDay.replace("%s", (n + 1).toString()))
                            if (date != null) append(" · ${date.label()}")
                            if (day.theme.isNotBlank()) append(" · ${day.theme}")
                        },
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    day.activities.forEach { a ->
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(
                                a.time.ifBlank { "—" },
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                modifier = Modifier.widthIn(min = 44.dp),
                            )
                            Column {
                                Text(a.title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                                ratingLine(a, s)?.let {
                                    Text(
                                        it,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.primary,
                                    )
                                }
                                if (a.location.isNotBlank()) {
                                    Text(
                                        a.location,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                    )
                                }
                                if (a.note.isNotBlank()) {
                                    Text(a.note, style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                }
            }

            if (isLatest) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(onClick = onChange, enabled = actionsEnabled, modifier = Modifier.weight(1f)) {
                        Text(s.neraMakeChanges)
                    }
                    Button(onClick = onApprove, enabled = actionsEnabled, modifier = Modifier.weight(1f)) {
                        Text(s.neraApprove)
                    }
                }
            } else {
                Text(
                    s.neraSuperseded,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                )
            }
        }
    }
}
