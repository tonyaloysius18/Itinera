package com.itinera.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.itinera.app.data.NeraActivity
import com.itinera.app.data.NeraChatService
import com.itinera.app.data.StoredNeraMessage
import com.itinera.app.data.NeraEntitlement
import com.itinera.app.data.NeraItinerary
import com.itinera.app.data.NeraLeg
import com.itinera.app.data.NeraLink
import com.itinera.app.data.NeraTimetable
import com.itinera.app.data.NeraTransportOption
import com.itinera.app.data.NeraOffer
import com.itinera.app.data.PurchaseOutcome
import com.itinera.app.data.PurchaseService
import com.itinera.app.data.NeraService
import com.itinera.app.data.NeraException
import com.itinera.app.data.NeraFailure
import com.itinera.app.data.NeraTurn
import com.itinera.app.i18n.LocalStrings
import com.itinera.app.i18n.Strings
import com.itinera.app.model.TransportType
import com.itinera.app.model.label
import com.itinera.app.prefersReducedMotion
import com.itinera.app.resources.Res
import com.itinera.app.resources.nera_head
import com.itinera.app.ui.BackHandler
import com.itinera.app.ui.theme.itinera
import com.itinera.app.ui.components.NeraPaywall
import com.itinera.app.ui.components.NeraThinking
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.painterResource

/** One row in the Nera conversation. [id] is stable per item, used as the Firestore doc id when persisted. */
private sealed interface NeraItem {
    val id: String
    data class FromUser(val text: String, override val id: String = "msg_${kotlin.random.Random.nextLong()}") : NeraItem
    data class FromNera(val text: String, val quickReplies: List<String> = emptyList(), val links: List<NeraLink> = emptyList(), val timetables: List<NeraTimetable> = emptyList(), override val id: String = "msg_${kotlin.random.Random.nextLong()}") : NeraItem
    data class Draft(val message: String, val itinerary: NeraItinerary, val approved: Boolean = false, val links: List<NeraLink> = emptyList(), val timetables: List<NeraTimetable> = emptyList(), override val id: String = "msg_${kotlin.random.Random.nextLong()}") : NeraItem
    data class Problem(val text: String, override val id: String = "msg_${kotlin.random.Random.nextLong()}") : NeraItem
}

/** Turns a persisted turn back into a chat row when restoring a trip's Nera history. */
private fun StoredNeraMessage.toNeraItem(): NeraItem {
    val itin = itinerary
    return when {
        itin != null -> NeraItem.Draft(text, itin, approved, links, timetables, id)
        role == "user" -> NeraItem.FromUser(text, id)
        else -> NeraItem.FromNera(text, quickReplies, links, timetables, id)
    }
}

/** The other direction: what to persist for a chat row, or null for rows that never get saved (local errors). */
private fun NeraItem.toStored(seq: Int): StoredNeraMessage? = when (this) {
    is NeraItem.FromUser -> StoredNeraMessage(id = id, role = "user", text = text, seq = seq)
    is NeraItem.FromNera -> StoredNeraMessage(id = id, role = "assistant", text = text, quickReplies = quickReplies, links = links, timetables = timetables, seq = seq)
    is NeraItem.Draft -> StoredNeraMessage(id = id, role = "assistant", text = message, links = links, timetables = timetables, itinerary = itinerary, approved = approved, seq = seq)
    is NeraItem.Problem -> null
}

/**
 * Chat with Nera. Nera asks for anything missing (party, dates, budget), then proposes a
 * draft itinerary. The draft only becomes a real trip when the traveller taps Approve.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NeraChatScreen(
    service: NeraService,
    chatService: NeraChatService,
    purchases: PurchaseService,
    uid: String,
    tripId: String? = null,       // null starts a fresh trip; set, restores and continues that trip's saved chat
    seed: NeraItinerary? = null,  // the trip's current real activities/legs, so Nera knows what's already there
    travellerName: String = "",   // first name from the profile, for a personal greeting; blank if unknown
    homeCity: String = "",        // home city from the profile, used for travel legs; blank if unknown
    onBack: () -> Unit,
    // Creates (tripId null) or updates (tripId set) the trip and returns its id; pending is every
    // turn not yet persisted (only non-empty for a brand-new trip, whose chat wasn't saved as it went).
    onApprove: (NeraItinerary, pending: List<StoredNeraMessage>) -> String,
) {
    val s = LocalStrings.current
    val welcomeText = travellerName.takeIf { it.isNotBlank() }
        ?.let { s.neraWelcomeNamed.replace("%s", it) }
        ?: s.neraWelcome
    val items = remember {
        mutableStateListOf<NeraItem>().apply {
            // A fresh trip has no history to restore, so greet right away; an existing trip's
            // history (or lack of it) is loaded below, once we know whether there's anything to show.
            if (tripId == null) add(NeraItem.FromNera(welcomeText, listOf(s.neraSuggest1, s.neraSuggest2, s.neraSuggest3)))
        }
    }
    var boundTripId by remember { mutableStateOf(tripId) }   // set once a brand-new trip gets created on Approve
    var historyLoading by remember { mutableStateOf(tripId != null) }
    var input by remember { mutableStateOf("") }
    var sending by remember { mutableStateOf(false) }
    var approving by remember { mutableStateOf(false) }   // debounces Approve while it's being applied
    // Where a non-paying user stands with the free trips; null = no info (paywall not enforced, or already paid).
    var freeTier by remember { mutableStateOf<NeraEntitlement?>(null) }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val uriHandler = LocalUriHandler.current

    // Paywall (only ever shown when the RevenueCat key for this platform is configured).
    var showPaywall by remember { mutableStateOf(false) }
    var offer by remember { mutableStateOf<NeraOffer?>(null) }
    var offerLoading by remember { mutableStateOf(false) }
    var paywallBusy by remember { mutableStateOf(false) }
    var paywallMessage by remember { mutableStateOf<String?>(null) }

    fun openPaywall() {
        if (!purchases.isAvailable) return
        paywallMessage = null
        showPaywall = true
        if (offer == null) scope.launch {
            offerLoading = true
            offer = purchases.loadOffer(uid)
            offerLoading = false
        }
    }

    // Opening the chat tells us how many free trips are left, before the first message.
    LaunchedEffect(Unit) {
        val status = service.status()
        freeTier = if (status?.enforced == true) status.entitlement?.takeIf { it.status != "paid" } else null
    }

    // Continuing an existing trip: restore its saved conversation instead of starting over.
    LaunchedEffect(Unit) {
        if (tripId != null) {
            val stored = runCatching { chatService.loadHistory(tripId) }.getOrDefault(emptyList())
            if (stored.isEmpty()) {
                items.add(NeraItem.FromNera(welcomeText, listOf(s.neraSuggest1, s.neraSuggest2, s.neraSuggest3)))
            } else {
                items.addAll(stored.map { it.toNeraItem() })
            }
            historyLoading = false
        }
    }

    /** Asks the server (which asks RevenueCat) whether the purchase has registered; a few tries, then gives up. */
    suspend fun serverConfirmsUnlock(): Boolean {
        repeat(3) {
            val status = service.status(refresh = true)
            if (status != null && (!status.enforced || status.entitlement?.status == "paid")) return true
            delay(1500)
        }
        return false
    }

    /** Finishes a successful purchase or restore: close the paywall and tell the user where they stand. */
    suspend fun finishUnlock() {
        if (serverConfirmsUnlock()) {
            freeTier = null
            showPaywall = false
            items.add(NeraItem.FromNera(s.neraUnlocked))
        } else {
            paywallMessage = s.neraConfirming
        }
    }

    val latestDraftIndex = items.indexOfLast { it is NeraItem.Draft }
    // Once the chat itself has proposed a draft, that supersedes the trip's on-open snapshot —
    // otherwise Nera would keep reverting to what the trip looked like before this conversation.
    val currentDraft = (items.getOrNull(latestDraftIndex) as? NeraItem.Draft)?.itinerary ?: seed

    // Keep the newest message in view as the conversation grows.
    LaunchedEffect(items.size, sending) {
        val last = items.size - 1 + if (sending) 1 else 0
        if (last >= 0) listState.animateScrollToItem(last)
    }

    // Best-effort: save one turn to the trip's chat history. No-op until a trip exists (boundTripId null).
    fun persist(item: NeraItem) {
        val tid = boundTripId ?: return
        val stored = item.toStored(items.indexOf(item)) ?: return
        scope.launch { runCatching { chatService.appendMessage(tid, stored) } }
    }

    fun send(text: String) {
        val message = text.trim()
        if (message.isEmpty() || sending || historyLoading) return
        items.add(NeraItem.FromUser(message))
        persist(items.last())
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
                val reply = service.send(history, currentDraft, homeCity)
                freeTier = reply.entitlement?.takeIf { it.status != "paid" }
                val itinerary = reply.itinerary
                if (reply.type == "itinerary" && itinerary != null) {
                    items.add(NeraItem.Draft(reply.message, itinerary, links = reply.links, timetables = reply.timetables))
                } else {
                    val text = when (reply.fallback) {
                        "lost" -> s.neraLostThread
                        "noDraft" -> s.neraNoDraft
                        else -> reply.message
                    }
                    items.add(NeraItem.FromNera(text, reply.quickReplies, reply.links, reply.timetables))
                }
                persist(items.last())
            } catch (e: NeraException) {
                items.add(NeraItem.Problem(when (e.failure) {
                    NeraFailure.NOT_CONFIGURED -> s.neraErrNotSetUp
                    NeraFailure.SIGN_IN -> s.neraErrSignIn
                    NeraFailure.NETWORK -> s.neraErrNetwork
                    NeraFailure.BAD_REPLY -> s.neraErrBadReply
                    NeraFailure.QUOTA -> s.neraErrQuota
                    NeraFailure.MONTHLY_QUOTA -> s.neraErrMonthlyQuota
                    NeraFailure.GENERIC -> s.neraErrGeneric
                }))
            } finally {
                sending = false
            }
        }
    }

    BackHandler(enabled = showPaywall) { showPaywall = false }

    Box(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
    Column(Modifier.fillMaxSize().imePadding()) {
        NeraChatHeader(s = s, onBack = onBack)
        freeTier?.let { tier ->
            FreeTierBanner(
                text = if (tier.status == "limit") s.neraFreeTripsUsed
                else s.neraFreeTripsLeft
                    .replaceFirst("%s", (tier.tripsLeft ?: 0).toString())
                    .replaceFirst("%s", (tier.freeTrips ?: 0).toString()),
                canUnlock = purchases.isAvailable,
                onClick = ::openPaywall,
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )
        }

        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .widthIn(max = 720.dp)
                .align(Alignment.CenterHorizontally),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                start = 16.dp,
                top = 16.dp,
                end = 16.dp,
                bottom = 20.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            itemsIndexed(items, key = { _, item -> item.id }) { index, item ->
                when (item) {
                    is NeraItem.FromUser -> Bubble(item.text, fromUser = true)
                    is NeraItem.FromNera -> {
                        AnimatedNeraResponse(item.id) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Bubble(item.text, fromUser = false)
                                item.timetables.forEach { TimetableCard(it, s) }
                                if (item.links.isNotEmpty()) LinkButtons(item.links)
                                if (index == items.lastIndex && !sending && item.quickReplies.isNotEmpty()) {
                                    QuickReplies(item.quickReplies, onPick = ::send)
                                }
                            }
                        }
                    }
                    is NeraItem.Draft -> {
                        AnimatedNeraResponse(item.id) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                if (item.message.isNotBlank()) Bubble(item.message, fromUser = false)
                                item.timetables.forEach { TimetableCard(it, s) }
                                if (item.links.isNotEmpty()) LinkButtons(item.links)
                                DraftCard(
                                    itinerary = item.itinerary,
                                    s = s,
                                    isLatest = index == latestDraftIndex,
                                    actionsEnabled = !sending && !approving && !item.approved,
                                    onApprove = {
                                        // Free trips used up: approving a draft needs the one-time unlock (when it can be bought here).
                                        if (freeTier?.status == "limit" && purchases.isAvailable) openPaywall()
                                        else if (!approving) {
                                            approving = true
                                            val idx = items.indexOfFirst { it.id == item.id }
                                            if (idx >= 0) items[idx] = item.copy(approved = true)
                                            val tidBefore = boundTripId
                                            // A trip that already existed had its turns saved as they happened; only a
                                            // brand-new one needs its whole (so-far unsaved) chat written in one go.
                                            val pending = if (tidBefore == null) items.mapIndexedNotNull { i, it -> it.toStored(i) } else emptyList()
                                            boundTripId = onApprove(item.itinerary, pending)
                                            if (tidBefore != null) scope.launch { runCatching { chatService.markApproved(tidBefore, item.id) } }
                                            approving = false
                                        }
                                    },
                                    onChange = {
                                        items.add(NeraItem.FromNera(
                                            s.neraChangePrompt,
                                            listOf(s.neraChange1, s.neraChange2, s.neraChange3, s.neraChange4),
                                        ))
                                    },
                                )
                            }
                        }
                    }
                    is NeraItem.Problem -> AnimatedNeraResponse(item.id) {
                        Bubble(item.text, fromUser = false, isError = true)
                    }
                }
            }
            if (sending || historyLoading) {
                item {
                    NeraThinking(
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .semantics { liveRegion = LiveRegionMode.Polite },
                        size = 50.dp,
                    )
                }
            }
        }

        Surface(
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 10.dp,
            tonalElevation = 0.dp,
        ) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .widthIn(max = 720.dp)
                    .align(Alignment.CenterHorizontally)
                    .navigationBarsPadding()
                    .padding(horizontal = 12.dp, vertical = 12.dp),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it.take(2000) },
                    modifier = Modifier.weight(1f),
                    placeholder = {
                        Text(
                            s.neraInputHint,
                            autoSize = TextAutoSize.StepBased(
                                minFontSize = 12.sp,
                                maxFontSize = 16.sp,
                                stepSize = 0.5.sp,
                            ),
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Ellipsis,
                        )
                    },
                    shape = RoundedCornerShape(24.dp),
                    minLines = 1,
                    maxLines = 4,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(onSend = { send(input) }),
                )
                FilledIconButton(
                    onClick = { send(input) },
                    enabled = input.isNotBlank() && !sending && !historyLoading,
                    modifier = Modifier.size(52.dp),
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = s.neraSend)
                }
            }
        }
    }

    if (showPaywall) {
        NeraPaywall(
            offer = offer,
            loading = offerLoading,
            busy = paywallBusy,
            message = paywallMessage,
            onPurchase = {
                val chosen = offer
                if (chosen != null && !paywallBusy) scope.launch {
                    paywallBusy = true
                    paywallMessage = null
                    when (purchases.purchase(chosen)) {
                        PurchaseOutcome.SUCCESS -> finishUnlock()
                        PurchaseOutcome.CANCELLED -> Unit
                        PurchaseOutcome.FAILED -> paywallMessage = s.neraPurchaseFailed
                    }
                    paywallBusy = false
                }
            },
            onRestore = {
                if (!paywallBusy) scope.launch {
                    paywallBusy = true
                    paywallMessage = null
                    if (purchases.restore(uid)) finishUnlock() else paywallMessage = s.neraRestoreNone
                    paywallBusy = false
                }
            },
            onTerms = { uriHandler.openUri(TERMS_URL) },
            onPrivacy = { uriHandler.openUri(PRIVACY_URL) },
            onClose = { showPaywall = false },
        )
    }
}
}

@Composable
private fun AnimatedNeraResponse(
    itemId: String,
    content: @Composable () -> Unit,
) {
    val reduceMotion = remember { prefersReducedMotion() }
    var visible by rememberSaveable(itemId) { mutableStateOf(false) }
    LaunchedEffect(itemId) { visible = true }

    AnimatedVisibility(
        visible = visible,
        enter = if (reduceMotion) {
            fadeIn(animationSpec = tween(0))
        } else {
            fadeIn(animationSpec = tween(240)) +
                slideInVertically(animationSpec = tween(280)) { fullHeight ->
                    (fullHeight / 10).coerceAtMost(40)
                }
        },
    ) {
        content()
    }
}

@Composable
private fun NeraChatHeader(s: Strings, onBack: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        shape = RoundedCornerShape(20.dp),
        // Solid, un-tinted fill with a hairline border and soft shadow, so the bar reads as a
        // distinct card instead of washing into the page behind it.
        color = if (MaterialTheme.itinera.isDark) MaterialTheme.colorScheme.surfaceContainerHigh
        else MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
        shadowElevation = 4.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 68.dp)
                .padding(horizontal = 6.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = s.back,
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            }
            Image(
                painter = painterResource(Res.drawable.nera_head),
                contentDescription = null,
                modifier = Modifier
                    .padding(start = 2.dp)
                    .size(44.dp),
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    "Nera",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Icon(
                        Icons.Filled.AutoAwesome,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp),
                    )
                    Text(
                        s.planWithNera,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }
    }
}

@Composable
private fun FreeTierBanner(
    text: String,
    canUnlock: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .fillMaxWidth()
            .widthIn(max = 688.dp)
            .clickable(enabled = canUnlock, onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp)
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Icon(Icons.Filled.Lock, contentDescription = null, modifier = Modifier.size(18.dp))
            Text(
                text,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.labelLarge,
            )
        }
    }
}

// Same site as the Terms link on the sign-up screen (GitHub Pages, served from docs/).
private const val TERMS_URL = "https://tonyaloysius18.github.io/Itinera/terms.html"
private const val PRIVACY_URL = "https://tonyaloysius18.github.io/Itinera/privacy-policy.html"

@Composable
private fun Bubble(text: String, fromUser: Boolean, isError: Boolean = false) {
    val bubbleShape = if (fromUser) {
        RoundedCornerShape(topStart = 22.dp, topEnd = 6.dp, bottomStart = 22.dp, bottomEnd = 22.dp)
    } else {
        RoundedCornerShape(topStart = 6.dp, topEnd = 22.dp, bottomStart = 22.dp, bottomEnd = 22.dp)
    }
    Row(
        Modifier
            .fillMaxWidth()
            .then(if (isError) Modifier.semantics { liveRegion = LiveRegionMode.Assertive } else Modifier),
        horizontalArrangement = if (fromUser) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Top,
    ) {
        if (!fromUser) {
            Image(
                painter = painterResource(Res.drawable.nera_head),
                contentDescription = null,
                modifier = Modifier
                    .padding(end = 8.dp)
                    .size(32.dp),
            )
        }
        Surface(
            shape = bubbleShape,
            color = when {
                fromUser -> MaterialTheme.colorScheme.primary
                isError -> MaterialTheme.colorScheme.errorContainer
                else -> MaterialTheme.colorScheme.surface
            },
            border = if (!fromUser && !isError) androidx.compose.foundation.BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.outlineVariant,
            ) else null,
            shadowElevation = if (!fromUser && !isError) 1.dp else 0.dp,
            modifier = Modifier.widthIn(max = if (fromUser) 328.dp else 560.dp),
        ) {
            Text(
                text,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                style = MaterialTheme.typography.bodyLarge,
                color = when {
                    fromUser -> MaterialTheme.colorScheme.onPrimary
                    isError -> MaterialTheme.colorScheme.onErrorContainer
                    else -> MaterialTheme.colorScheme.onSurface
                },
            )
        }
    }
}

/** "5h 48m" from minutes. */
private fun durationLabel(min: Int): String = if (min >= 60) "${min / 60}h ${(min % 60).toString().padStart(2, '0')}m" else "${min}m"

private fun changesLabel(n: Int, s: Strings): String = when (n) {
    0 -> s.transportDirect
    1 -> s.transportOneChange
    else -> s.transportChangesN.replace("%d", n.toString())
}

private fun modeIcon(modes: List<String>) = when (modes.firstOrNull()) {
    "bus" -> transportIcon(TransportType.BUS)
    "ferry" -> transportIcon(TransportType.FERRY)
    "flight" -> transportIcon(TransportType.FLIGHT)
    else -> transportIcon(TransportType.TRAIN)
}

/** The scheduled options Nera found for a route: times, duration, changes, operators. Booking stays on the provider's site. */
@Composable
private fun TimetableCard(t: NeraTimetable, s: Strings) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(start = 44.dp),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                "${t.from} → ${t.to}",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
            t.options.forEach { o -> TimetableRow(o, s) }
            Text(
                s.transportScheduleNote,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun TimetableRow(o: NeraTransportOption, s: Strings) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Icon(modeIcon(o.modes), contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
        Column(Modifier.weight(1f)) {
            Text("${o.depart} → ${o.arrive}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium)
            val operators = o.operators.joinToString(" · ")
            if (operators.isNotBlank()) {
                Text(operators, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
            }
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(durationLabel(o.durationMin), style = MaterialTheme.typography.bodyMedium)
            Text(changesLabel(o.changes, s), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

/** "Toulouse → Grenoble: trains (Omio)" in the app language; falls back to the server's English label. */
private fun linkLabel(link: NeraLink, s: Strings): String {
    val word = when (link.mode) {
        "all" -> s.transportCompareAll
        "train" -> s.transportTrains
        "bus" -> s.transportBuses
        "flight" -> s.transportFlights
        else -> return link.label
    }
    if (link.from.isBlank() || link.to.isBlank()) return link.label
    val provider = if (link.provider.isBlank()) "" else " (${link.provider})"
    return "${link.from} → ${link.to}: $word$provider"
}

/** Booking / comparison links from Nera (e.g. train, bus and flight pages for a route); each opens in the browser. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun LinkButtons(links: List<NeraLink>) {
    val s = LocalStrings.current
    val uriHandler = LocalUriHandler.current
    FlowRow(
        modifier = Modifier.padding(start = 44.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        links.forEach { link ->
            OutlinedButton(
                onClick = { runCatching { uriHandler.openUri(link.url) } },
                modifier = Modifier.heightIn(min = 44.dp),
                shape = RoundedCornerShape(16.dp),
                colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary,
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.45f),
                ),
            ) {
                Text("${linkLabel(link, s)} ↗")
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun QuickReplies(options: List<String>, onPick: (String) -> Unit) {
    FlowRow(
        modifier = Modifier.padding(start = 44.dp),   // line up under the message text, past the avatar
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        options.forEach { option ->
            OutlinedButton(
                onClick = { onPick(option) },
                modifier = Modifier.heightIn(min = 48.dp),
                shape = RoundedCornerShape(16.dp),
                colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary,
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.45f),
                ),
            ) {
                Text(option)
            }
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

/** A day's activities and travel legs, merged so they can be shown in one time-ordered list. */
private sealed interface DraftEntry {
    val sortTime: String   // blank sorts last, so an unset time never jumps ahead of timed entries
    data class Act(val activity: NeraActivity) : DraftEntry { override val sortTime get() = activity.time.ifBlank { "99:99" } }
    data class Leg(val leg: NeraLeg) : DraftEntry { override val sortTime get() = leg.time.ifBlank { "99:99" } }
}

private fun transportTypeOf(value: String): TransportType = when (value.lowercase()) {
    "train" -> TransportType.TRAIN
    "bus" -> TransportType.BUS
    "ferry" -> TransportType.FERRY
    "car" -> TransportType.CAR
    else -> TransportType.FLIGHT
}

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
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Filled.AutoAwesome,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(22.dp),
                    )
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        itinerary.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    val dates = itinerary.days.mapNotNull { runCatching { LocalDate.parse(it.date) }.getOrNull() }
                    val range = if (dates.isNotEmpty()) "${dates.first().label()} – ${dates.last().label()} · " else ""
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Icon(
                            Icons.Filled.CalendarMonth,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp),
                        )
                        Text(
                            "$range${s.neraDaysDraft.replace("%s", itinerary.days.size.toString())}",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            itinerary.days.forEachIndexed { n, day ->
                val date = runCatching { LocalDate.parse(day.date) }.getOrNull()
                // Legs (flights/trains) for this date, merged in among the day's activities by time — same
                // convention as the trip detail screen, so the outbound flight sits before sightseeing and the
                // return one after it, rather than always first or last.
                val legsToday = itinerary.legs.filter { it.date == day.date }
                val entries = (legsToday.map { DraftEntry.Leg(it) } + day.activities.map { DraftEntry.Act(it) })
                    .sortedBy { it.sortTime }
                Surface(
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    shape = RoundedCornerShape(18.dp),
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
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
                        entries.forEach { entry ->
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.Top,
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.surface,
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        MaterialTheme.colorScheme.outlineVariant,
                                    ),
                                ) {
                                    Text(
                                        entry.sortTime.takeUnless { it == "99:99" } ?: "—",
                                        modifier = Modifier
                                            .widthIn(min = 48.dp)
                                            .padding(horizontal = 8.dp, vertical = 6.dp),
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                                if (entry is DraftEntry.Leg) {
                                    val l = entry.leg
                                    Icon(
                                        transportIcon(transportTypeOf(l.transport)),
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(top = 5.dp).size(20.dp),
                                    )
                                    Text(
                                        "${l.fromCity} → ${l.toCity}",
                                        modifier = Modifier.weight(1f).padding(top = 5.dp),
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Medium,
                                    )
                                } else {
                                    val a = (entry as DraftEntry.Act).activity
                                    Column(
                                        modifier = Modifier.weight(1f).padding(top = 4.dp),
                                        verticalArrangement = Arrangement.spacedBy(2.dp),
                                    ) {
                                        Text(
                                            a.title,
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.Medium,
                                        )
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
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            )
                                        }
                                        if (a.note.isNotBlank()) {
                                            Text(
                                                a.note,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurface,
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (isLatest) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = onApprove,
                        enabled = actionsEnabled,
                        modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
                        shape = RoundedCornerShape(16.dp),
                    ) {
                        Icon(
                            Icons.Filled.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(s.neraApprove)
                    }
                    OutlinedButton(
                        onClick = onChange,
                        enabled = actionsEnabled,
                        modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
                        shape = RoundedCornerShape(16.dp),
                    ) {
                        Text(s.neraMakeChanges)
                    }
                }
            } else {
                Text(
                    s.neraSuperseded,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
