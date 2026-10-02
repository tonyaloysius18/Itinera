package com.itinera.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.itinera.app.i18n.LocalStrings
import com.itinera.app.model.TripTemplate
import com.itinera.app.ui.components.PlaneLoader
import com.itinera.app.ui.components.TopBar

/**
 * Community feed: itineraries other travellers shared and a moderator approved. Opening one shows the usual template
 * screen (Use this template / Edit with Nera), and each copy counts towards its "Used by N".
 */
@Composable
fun CommunityFeedScreen(
    items: List<TripTemplate>,
    loading: Boolean,
    onLoad: (popular: Boolean) -> Unit,
    onBack: () -> Unit,
    onOpen: (String) -> Unit,
) {
    val s = LocalStrings.current
    var popular by remember { mutableStateOf(true) }
    LaunchedEffect(popular) { onLoad(popular) }

    Column(Modifier.fillMaxSize()) {
        TopBar(s.communityTab, onBack = onBack)
        FeedSwitch(communitySelected = true, onCurated = onBack, onCommunity = {})
        Spacer(Modifier.height(8.dp))
        val colors = tonalChipColors()
        Row(Modifier.padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = popular, onClick = { popular = true }, label = { Text(s.communityPopular) }, colors = colors, border = null)
            FilterChip(selected = !popular, onClick = { popular = false }, label = { Text(s.communityNewest) }, colors = colors, border = null)
        }
        Spacer(Modifier.height(8.dp))

        val snapshot = items.toList()
        when {
            loading && snapshot.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { PlaneLoader(size = 110.dp) }
            snapshot.isEmpty() -> Box(Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                Text(
                    s.communityEmpty,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center,
                )
            }
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp),
            ) {
                items(snapshot, key = { it.id }) { t ->
                    TemplateCard(
                        template = t,
                        daysWord = s.templateDurationDaysN.replace("%d", t.durationDays.toString()),
                        onClick = { onOpen(t.id) },
                    )
                }
            }
        }
    }
}
