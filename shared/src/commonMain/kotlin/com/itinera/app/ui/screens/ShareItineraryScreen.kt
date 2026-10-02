package com.itinera.app.ui.screens

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.itinera.app.data.SHARE_BASE_URL
import com.itinera.app.data.rememberFileSharer
import com.itinera.app.i18n.LocalStrings
import com.itinera.app.model.Activity
import com.itinera.app.model.BudgetTier
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

    LaunchedEffect(Unit) {
        shareId = runCatching { loadExistingLinkId() }.getOrNull()
        loaded = true
    }

    val byDate = remember(activities) { activities.groupBy { it.date }.toList().sortedBy { it.first } }
    val included = activities.count { it.id !in excluded }

    Column(Modifier.fillMaxSize()) {
        TopBar(s.shareItinerary, onBack = onBack)
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            // What is and isn't shared
            Surface(shape = RoundedCornerShape(14.dp), color = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(s.shareIntro, style = MaterialTheme.typography.bodyMedium)
                    Text(s.shareIncluded, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f))
                    Text(s.shareNeverShared, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f))
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
                    singleLine = true, modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = description, onValueChange = { description = it.take(300) }, label = { Text(s.shareDescriptionLabel) },
                    minLines = 2, modifier = Modifier.fillMaxWidth(),
                )
                Text(s.filterBudget, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    BudgetTier.values().forEach { b -> FilterChip(selected = budget == b, onClick = { budget = b }, label = { Text(b.label()) }) }
                }
                Text(s.filterPace, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PaceTag.values().forEach { p -> FilterChip(selected = pace == p, onClick = { pace = p }, label = { Text(p.label()) }) }
                }

                Text("${s.shareStopsLabel} ($included)", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium)
                // Number days exactly as the published copy does: relative to the first day that still has a place
                // or a journey on it, so the preview and the shared page always agree.
                val firstDate = (activities.filter { it.id !in excluded }.map { it.date } + sharedLegDates(trip, homeCity)).minOrNull()
                byDate.forEach { (date, dayStops) ->
                    val dayNo = if (firstDate != null) firstDate.daysUntil(date) + 1 else 1
                    Text("${s.day} $dayNo", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                    dayStops.sortedBy { it.time.ifBlank { "99:99" } }.forEach { a ->
                        val off = a.id in excluded
                        Row(
                            Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.04f), RoundedCornerShape(10.dp)).padding(start = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(Modifier.weight(1f).padding(vertical = 8.dp)) {
                                Text(
                                    a.title, style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis,
                                    textDecoration = if (off) TextDecoration.LineThrough else null,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = if (off) 0.4f else 1f),
                                )
                                if (a.location.isNotBlank()) Text(a.location, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                            IconButton(onClick = { excluded = if (off) excluded - a.id else excluded + a.id }) {
                                Icon(if (off) Icons.Filled.Add else Icons.Filled.Close, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                            }
                        }
                    }
                }
                Spacer(Modifier.height(4.dp))
                Button(
                    onClick = {
                        if (busy) return@Button
                        if (included == 0) { onMessage(s.shareNeedsStops); return@Button }
                        scope.launch {
                            busy = true
                            val id = onPublish { newId ->
                                buildSharedItinerary(newId, trip, activities, title, description, budget, pace, excluded, 0L, homeCity)
                            }
                            if (id != null) shareId = id else onMessage(s.shareFailed)
                            busy = false
                        }
                    },
                    enabled = !busy,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(26.dp),
                ) {
                    if (busy) CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                    else Text(s.shareCreateLink)
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}
