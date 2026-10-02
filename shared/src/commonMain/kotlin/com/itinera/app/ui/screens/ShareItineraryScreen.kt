package com.itinera.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.ui.semantics.Role
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.itinera.app.data.SHARE_BASE_URL
import com.itinera.app.data.rememberFileSharer
import com.itinera.app.i18n.LocalStrings
import com.itinera.app.model.Activity
import com.itinera.app.model.BudgetTier
import com.itinera.app.model.CommunityRules
import com.itinera.app.model.PaceTag
import com.itinera.app.model.SharedItinerary
import com.itinera.app.model.Trip
import com.itinera.app.model.buildSharedItinerary
import com.itinera.app.model.sharedLegDates
import com.itinera.app.ui.components.TopBar
import kotlinx.datetime.daysUntil
import kotlinx.coroutines.launch

/**
 * Preview, sanitize and publish a trip as a shareable link. The owner sees exactly what will be public, can remove
 * individual places, and can stop sharing later. Nothing personal is ever included (see [buildSharedItinerary]).
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ShareItineraryScreen(
    trip: Trip,
    activities: List<Activity>,
    onBack: () -> Unit,
    /** The link this trip is already shared under, if any. */
    loadExistingLinkId: suspend () -> String?,
    /** Publishes the copy built by the given builder; returns the new share id, or null on failure. */
    onPublish: suspend (build: (id: String) -> SharedItinerary) -> String?,
    onUnpublish: suspend (id: String) -> Boolean,
    onMessage: (String) -> Unit,
    /** The owner's home city (profile), so journeys to and from home are left out of what is shared. */
    homeCity: String = "",
    /** The signed-in user's id, attached only when listing in Community so people can report or block the author. */
    authorUid: String = "",
    /** Review status and copy count of an existing share, so the owner can see if it has been listed. */
    loadStatus: suspend (id: String) -> SharedItinerary? = { null },
) {
    val s = LocalStrings.current
    val scope = rememberCoroutineScope()
    val clipboard = LocalClipboardManager.current
    val sharer = rememberFileSharer()

    var title by remember { mutableStateOf(trip.title) }
    var description by remember { mutableStateOf("") }
    var budget by remember { mutableStateOf(BudgetTier.MID_RANGE) }
    var pace by remember { mutableStateOf(PaceTag.BALANCED) }
    var excluded by remember { mutableStateOf(setOf<String>()) }
    var shareId by remember { mutableStateOf<String?>(null) }
    var loaded by remember { mutableStateOf(false) }
    var busy by remember { mutableStateOf(false) }
    var listInCommunity by remember { mutableStateOf(false) }
    var showPrivacy by remember { mutableStateOf(false) }
    var existing by remember { mutableStateOf<SharedItinerary?>(null) }

    LaunchedEffect(Unit) {
        shareId = runCatching { loadExistingLinkId() }.getOrNull()
        shareId?.let { existing = runCatching { loadStatus(it) }.getOrNull() }
        loaded = true
    }

    val byDate = remember(activities) { activities.groupBy { it.date }.toList().sortedBy { it.first } }
    val included = activities.count { it.id !in excluded }

    Column(Modifier.fillMaxSize().imePadding()) {
        TopBar(s.shareItinerary, onBack = onBack)
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Surface(shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Icon(Icons.Filled.Link, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                        Text(s.shareIntro, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                    }
                    Row(
                        Modifier.fillMaxWidth().clickable { showPrivacy = !showPrivacy }.padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(Icons.Filled.VisibilityOff, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(s.detailsLabel.ifBlank { s.shareItinerary }, style = MaterialTheme.typography.labelLarge, modifier = Modifier.weight(1f))
                        Icon(if (showPrivacy) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore, s.detailsLabel)
                    }
                    AnimatedVisibility(showPrivacy) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(s.shareIncluded, style = MaterialTheme.typography.bodySmall)
                            Text(s.shareNeverShared, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            val currentId = shareId
            if (!loaded) {
                Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            } else if (currentId != null) {
                val link = SHARE_BASE_URL + currentId
                Surface(shape = RoundedCornerShape(14.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f)) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(s.shareLinkReady, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        existing?.takeIf { it.listRequested }?.let { e ->
                            val label = when {
                                e.feed -> "${s.shareStatusListed} · ${s.usedByN.replace("%d", e.copyCount.toString())}"
                                e.status == "rejected" || e.status == "taken_down" -> s.shareStatusRejected
                                else -> s.shareStatusPending
                            }
                            Text(label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                        }
                        Text(link, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary, maxLines = 2, overflow = TextOverflow.Ellipsis)
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedButton(
                                onClick = { clipboard.setText(AnnotatedString(link)); onMessage(s.shareLinkCopied) },
                                modifier = Modifier.weight(1f),
                            ) {
                                Icon(Icons.Filled.ContentCopy, null, Modifier.size(16.dp)); Spacer(Modifier.width(6.dp)); Text(s.shareCopyLink, maxLines = 1)
                            }
                            Button(onClick = { sharer.shareText("${trip.title}\n$link") }, modifier = Modifier.weight(1f)) {
                                Icon(Icons.Filled.Share, null, Modifier.size(16.dp)); Spacer(Modifier.width(6.dp)); Text(s.share, maxLines = 1)
                            }
                        }
                        OutlinedButton(
                            onClick = {
                                if (!busy) scope.launch {
                                    busy = true
                                    if (onUnpublish(currentId)) { shareId = null; onMessage(s.shareStopped) } else onMessage(s.shareFailed)
                                    busy = false
                                }
                            },
                            enabled = !busy,
                            modifier = Modifier.fillMaxWidth(),
                        ) { Text(s.shareStopSharing) }
                    }
                }
            } else {
                OutlinedTextField(
                    value = title, onValueChange = { title = it.take(80) }, label = { Text(s.shareTitleLabel) },
                    singleLine = true, shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth(),
                    supportingText = { Text("${title.length}/80", modifier = Modifier.fillMaxWidth(), textAlign = androidx.compose.ui.text.style.TextAlign.End) },
                )
                OutlinedTextField(
                    value = description, onValueChange = { description = it.take(300) }, label = { Text(s.shareDescriptionLabel) },
                    minLines = 2, maxLines = 4, shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth(),
                    supportingText = { Text("${description.length}/300", modifier = Modifier.fillMaxWidth(), textAlign = androidx.compose.ui.text.style.TextAlign.End) },
                )
                Text(s.filterBudget, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    BudgetTier.values().forEach { b -> FilterChip(selected = budget == b, onClick = { budget = b }, label = { Text(b.label()) }, colors = shareChipColors(), border = null, leadingIcon = if (budget == b) ({ Icon(Icons.Filled.CheckCircle, null, Modifier.size(16.dp)) }) else null) }
                }
                Text(s.filterPace, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PaceTag.values().forEach { p -> FilterChip(selected = pace == p, onClick = { pace = p }, label = { Text(p.label()) }, colors = shareChipColors(), border = null, leadingIcon = if (pace == p) ({ Icon(Icons.Filled.CheckCircle, null, Modifier.size(16.dp)) }) else null) }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text("${s.shareStopsLabel} · $included/${activities.size}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                    androidx.compose.material3.TextButton(onClick = {
                        excluded = if (included == activities.size) activities.map { it.id }.toSet() else emptySet()
                    }) { Text(if (included == activities.size) s.deselectAll else s.selectAll) }
                }
                if (included == 0) Text(s.shareNeedsStops, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                // Number days exactly as the published copy does: relative to the first day that still has a place
                // or a journey on it, so the preview and the shared page always agree.
                val firstDate = (activities.filter { it.id !in excluded }.map { it.date } + sharedLegDates(trip, homeCity)).minOrNull()
                byDate.forEach { (date, dayStops) ->
                    val dayNo = if (firstDate != null) firstDate.daysUntil(date) + 1 else 1
                    Surface(shape = RoundedCornerShape(18.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.04f)) {
                        Column {
                            val allSelected = dayStops.all { it.id !in excluded }
                            Row(
                                Modifier.fillMaxWidth().toggleable(value = allSelected, role = Role.Checkbox, onValueChange = { selected ->
                                    val ids = dayStops.map { it.id }.toSet()
                                    excluded = if (selected) excluded - ids else excluded + ids
                                }).padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text("${s.day} $dayNo", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary, modifier = Modifier.weight(1f))
                                Text("${dayStops.count { it.id !in excluded }}/${dayStops.size}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Checkbox(checked = allSelected, onCheckedChange = null)
                            }
                            dayStops.sortedBy { it.time.ifBlank { "99:99" } }.forEachIndexed { index, a ->
                                val off = a.id in excluded
                                val rowColor by animateColorAsState(if (off) MaterialTheme.colorScheme.surface.copy(alpha = 0.5f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.03f))
                                if (index > 0) HorizontalDivider(Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f))
                                Row(
                                    Modifier.fillMaxWidth().background(rowColor).toggleable(value = !off, role = Role.Checkbox, onValueChange = {
                                        excluded = if (off) excluded - a.id else excluded + a.id
                                    }).padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Checkbox(checked = !off, onCheckedChange = null)
                                    Spacer(Modifier.width(4.dp))
                                    Column(Modifier.weight(1f).padding(vertical = 8.dp)) {
                                        Text(a.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = if (off) 0.4f else 1f))
                                        if (a.location.isNotBlank()) Text(a.location, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    }
                                    if (a.time.isNotBlank()) Text(a.time, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(start = 8.dp))
                                }
                            }
                        }
                    }
                }
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(s.listInCommunity, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium)
                        Text(s.listInCommunityHint, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f))
                    }
                    Spacer(Modifier.width(12.dp))
                    Switch(checked = listInCommunity, onCheckedChange = { listInCommunity = it })
                }

            }
            Spacer(Modifier.height(24.dp))
        }
        if (loaded && shareId == null) {
            Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 8.dp) {
                Column(Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 16.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("${s.shareStopsLabel}: $included · ${budget.label()} · ${pace.label()}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Button(
                        onClick = {
                            if (busy) return@Button
                            if (included == 0) { onMessage(s.shareNeedsStops); return@Button }
                            if (listInCommunity && included < 4) { onMessage(s.shareNeedsMoreStops); return@Button }
                            if (listInCommunity) {
                                // Same text rules the server applies, so the person hears about a problem now rather than
                                // after the share page says "Not approved".
                                if (!CommunityRules.isAcceptable(title)) { onMessage(s.shareInvalidTitle); return@Button }
                                if (!CommunityRules.isAcceptable(description)) { onMessage(s.shareInvalidDescription); return@Button }
                                if (activities.any { it.id !in excluded && !(CommunityRules.isAcceptable(it.title) && CommunityRules.isAcceptable(it.location)) }) {
                                    onMessage(s.shareInvalidPlace); return@Button
                                }
                            }
                            scope.launch {
                                busy = true
                                val id = onPublish { newId ->
                                    buildSharedItinerary(newId, trip, activities, title, description, budget, pace, excluded, 0L, homeCity, listInCommunity, authorUid)
                                }
                                if (id != null) { shareId = id; existing = runCatching { loadStatus(id) }.getOrNull() } else onMessage(s.shareFailed)
                                busy = false
                            }
                        },
                        enabled = !busy && included > 0 && title.isNotBlank(),
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(26.dp),
                    ) {
                        if (busy) CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                        else Text(s.shareCreateLink)
                    }
                }
            }
        }

    }
}

@Composable
private fun shareChipColors() = FilterChipDefaults.filterChipColors(
    containerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f),
    selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.16f),
    selectedLabelColor = MaterialTheme.colorScheme.primary,
    selectedLeadingIconColor = MaterialTheme.colorScheme.primary,
)
