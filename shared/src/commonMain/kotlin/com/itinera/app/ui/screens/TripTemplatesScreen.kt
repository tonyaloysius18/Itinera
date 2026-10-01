package com.itinera.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BeachAccess
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Grass
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Terrain
import androidx.compose.material.icons.filled.Water
import androidx.compose.material.icons.filled.Waves
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.itinera.app.i18n.LocalStrings
import com.itinera.app.model.BudgetTier
import com.itinera.app.model.Continent
import com.itinera.app.model.DestinationType
import com.itinera.app.model.PaceTag
import com.itinera.app.model.TripTemplate
import com.itinera.app.ui.components.PlaneLoader
import com.itinera.app.ui.components.TopBar
import com.itinera.app.ui.theme.itinera
import kotlin.math.abs

@Composable
internal fun Continent.label(): String {
    val s = LocalStrings.current
    return when (this) {
        Continent.EUROPE -> s.continentEurope
        Continent.ASIA -> s.continentAsia
        Continent.AFRICA -> s.continentAfrica
        Continent.NORTH_AMERICA -> s.continentNorthAmerica
        Continent.SOUTH_AMERICA -> s.continentSouthAmerica
        Continent.OCEANIA -> s.continentOceania
    }
}

@Composable
internal fun DestinationType.label(): String {
    val s = LocalStrings.current
    return when (this) {
        DestinationType.BEACH -> s.destinationBeach
        DestinationType.MOUNTAIN -> s.destinationMountain
        DestinationType.DESERT -> s.destinationDesert
        DestinationType.CITY -> s.destinationCity
        DestinationType.COUNTRYSIDE -> s.destinationCountryside
        DestinationType.ISLAND -> s.destinationIsland
        DestinationType.LAKE -> s.destinationLake
    }
}

internal fun DestinationType.icon(): ImageVector = when (this) {
    DestinationType.BEACH -> Icons.Filled.BeachAccess
    DestinationType.MOUNTAIN -> Icons.Filled.Terrain
    DestinationType.DESERT -> Icons.Filled.WbSunny
    DestinationType.CITY -> Icons.Filled.LocationCity
    DestinationType.COUNTRYSIDE -> Icons.Filled.Grass
    DestinationType.ISLAND -> Icons.Filled.Waves
    DestinationType.LAKE -> Icons.Filled.Water
}

@Composable
internal fun BudgetTier.label(): String {
    val s = LocalStrings.current
    return when (this) {
        BudgetTier.BUDGET -> s.budgetBudget
        BudgetTier.MID_RANGE -> s.budgetMidRange
        BudgetTier.LUXURY -> s.budgetLuxury
    }
}

@Composable
internal fun PaceTag.label(): String {
    val s = LocalStrings.current
    return when (this) {
        PaceTag.RELAXED -> s.paceRelaxed
        PaceTag.BALANCED -> s.paceBalanced
        PaceTag.PACKED -> s.pacePacked
    }
}

/** Deterministic accent colour per template, cycling through the same four accents trip cards use. */
@Composable
internal fun templateAccent(template: TripTemplate): Color {
    val palette = listOf(
        MaterialTheme.itinera.accentBlue,
        MaterialTheme.itinera.accentGreen,
        MaterialTheme.itinera.accentCoral,
        MaterialTheme.itinera.accentPurple,
    )
    return palette[abs(template.id.hashCode()) % palette.size]
}

/** Small tonal pill chip — same recipe as the pills used on trip detail (filled, no border). */
@Composable
internal fun MetaPill(label: String, icon: ImageVector? = null, accent: Boolean = false) {
    val tint = if (accent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
    Surface(
        shape = CircleShape,
        color = if (accent) MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f),
    ) {
        Row(Modifier.padding(horizontal = 10.dp, vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
                Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(13.dp))
                Spacer(Modifier.width(5.dp))
            }
            Text(label, style = MaterialTheme.typography.labelSmall, color = tint)
        }
    }
}

@Composable
private fun DiscoverSearchField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val s = LocalStrings.current
    val onSurface = MaterialTheme.colorScheme.onSurface
    Surface(modifier = modifier, shape = RoundedCornerShape(22.dp), color = onSurface.copy(alpha = 0.06f)) {
        Row(
            Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Filled.Search, null, tint = onSurface.copy(alpha = 0.45f), modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(10.dp))
            Box(Modifier.weight(1f)) {
                if (value.isEmpty()) {
                    Text(
                        s.searchTemplates,
                        style = MaterialTheme.typography.bodyMedium,
                        color = onSurface.copy(alpha = 0.4f),
                    )
                }
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyMedium.copy(color = onSurface),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun tonalChipColors() = FilterChipDefaults.filterChipColors(
    containerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f),
    labelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
    iconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
    selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.16f),
    selectedLabelColor = MaterialTheme.colorScheme.primary,
    selectedLeadingIconColor = MaterialTheme.colorScheme.primary,
)

/** Single-row quick filter for destination type — the one filter people reach for most, so it stays on-screen. */
@Composable
private fun TypeChipsRow(selected: DestinationType?, onSelect: (DestinationType?) -> Unit) {
    val s = LocalStrings.current
    val colors = tonalChipColors()
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
    ) {
        item {
            FilterChip(
                selected = selected == null,
                onClick = { onSelect(null) },
                label = { Text(s.allFilter) },
                colors = colors,
                border = null,
            )
        }
        items(DestinationType.values().toList()) { type ->
            FilterChip(
                selected = selected == type,
                onClick = { onSelect(if (selected == type) null else type) },
                label = { Text(type.label()) },
                leadingIcon = { Icon(type.icon(), null, modifier = Modifier.size(16.dp)) },
                colors = colors,
                border = null,
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun <T> SheetFilterGroup(
    label: String,
    options: List<T>,
    selected: T?,
    optionLabel: @Composable (T) -> String,
    onSelect: (T?) -> Unit,
) {
    val colors = tonalChipColors()
    Column(Modifier.padding(horizontal = 20.dp)) {
        Text(
            label,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 8.dp),
        )
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            options.forEach { option ->
                FilterChip(
                    selected = selected == option,
                    onClick = { onSelect(if (selected == option) null else option) },
                    label = { Text(optionLabel(option)) },
                    colors = colors,
                    border = null,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FiltersSheet(
    continent: Continent?,
    budget: BudgetTier?,
    pace: PaceTag?,
    onContinent: (Continent?) -> Unit,
    onBudget: (BudgetTier?) -> Unit,
    onPace: (PaceTag?) -> Unit,
    onClear: () -> Unit,
    onDismiss: () -> Unit,
) {
    val s = LocalStrings.current
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    ) {
        Column(Modifier.padding(bottom = 24.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
            SheetFilterGroup(s.filterContinent, Continent.values().toList(), continent, { it.label() }, onContinent)
            SheetFilterGroup(s.filterBudget, BudgetTier.values().toList(), budget, { it.label() }, onBudget)
            SheetFilterGroup(s.filterPace, PaceTag.values().toList(), pace, { it.label() }, onPace)
            Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = onClear,
                    enabled = continent != null || budget != null || pace != null,
                    modifier = Modifier.weight(1f),
                ) { Text(s.clear) }
                Button(onClick = onDismiss, modifier = Modifier.weight(1f)) { Text(s.done) }
            }
        }
    }
}

@Composable
private fun ActiveFilterChip(label: String, onRemove: () -> Unit) {
    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f),
        modifier = Modifier.clip(CircleShape).clickable(onClick = onRemove),
    ) {
        Row(Modifier.padding(start = 12.dp, end = 8.dp, top = 6.dp, bottom = 6.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(4.dp))
            Icon(Icons.Filled.Close, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
        }
    }
}

@Composable
fun TripTemplatesScreen(
    templates: List<TripTemplate>,
    isLoading: Boolean,
    onBack: () -> Unit,
    onOpenTemplate: (String) -> Unit,
) {
    val s = LocalStrings.current

    var query by remember { mutableStateOf("") }
    var showFilters by remember { mutableStateOf(false) }

    var selectedContinent by remember { mutableStateOf<Continent?>(null) }
    var selectedType by remember { mutableStateOf<DestinationType?>(null) }
    var selectedBudget by remember { mutableStateOf<BudgetTier?>(null) }
    var selectedPace by remember { mutableStateOf<PaceTag?>(null) }

    val filtered = remember(templates, query, selectedContinent, selectedType, selectedBudget, selectedPace) {
        templates.filter { t ->
            (query.isBlank() || t.title.contains(query.trim(), ignoreCase = true) ||
                t.country.contains(query.trim(), ignoreCase = true)) &&
                (selectedContinent == null || t.continent == selectedContinent) &&
                (selectedType == null || selectedType in t.destinationTypes) &&
                (selectedBudget == null || t.budgetTier == selectedBudget) &&
                (selectedPace == null || t.paceTag == selectedPace)
        }
    }
    val sheetFilterCount = listOfNotNull(selectedContinent, selectedBudget, selectedPace).size

    Column(Modifier.fillMaxSize()) {
        TopBar(s.discoverTemplates, onBack = onBack)

        when {
            isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                PlaneLoader(size = 130.dp)
            }

            else -> {
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    DiscoverSearchField(value = query, onValueChange = { query = it }, modifier = Modifier.weight(1f))
                    Box {
                        Surface(
                            shape = RoundedCornerShape(22.dp),
                            color = if (sheetFilterCount > 0) MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)
                            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f),
                            modifier = Modifier.size(44.dp).clip(RoundedCornerShape(22.dp)).clickable { showFilters = true },
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Filled.Tune,
                                    contentDescription = null,
                                    tint = if (sheetFilterCount > 0) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
                                    modifier = Modifier.size(20.dp),
                                )
                            }
                        }
                        if (sheetFilterCount > 0) {
                            Box(
                                Modifier.align(Alignment.TopEnd).size(18.dp).clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    sheetFilterCount.toString(),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimary,
                                )
                            }
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
                TypeChipsRow(selected = selectedType, onSelect = { selectedType = it })

                if (sheetFilterCount > 0) {
                    Spacer(Modifier.height(10.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp),
                    ) {
                        selectedContinent?.let { c -> item { ActiveFilterChip(c.label()) { selectedContinent = null } } }
                        selectedBudget?.let { b -> item { ActiveFilterChip(b.label()) { selectedBudget = null } } }
                        selectedPace?.let { p -> item { ActiveFilterChip(p.label()) { selectedPace = null } } }
                    }
                }

                if (filtered.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                Modifier.size(64.dp).clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    Icons.Filled.Explore,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(28.dp),
                                )
                            }
                            Spacer(Modifier.height(14.dp))
                            Text(
                                s.noTemplatesMatch,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp),
                    ) {
                        items(filtered, key = { it.id }) { template ->
                            TemplateCard(
                                template = template,
                                daysWord = s.templateDurationDaysN.replace("%d", template.durationDays.toString()),
                                onClick = { onOpenTemplate(template.id) },
                            )
                        }
                    }
                }
            }
        }
    }

    if (showFilters) {
        FiltersSheet(
            continent = selectedContinent,
            budget = selectedBudget,
            pace = selectedPace,
            onContinent = { selectedContinent = it },
            onBudget = { selectedBudget = it },
            onPace = { selectedPace = it },
            onClear = { selectedContinent = null; selectedBudget = null; selectedPace = null },
            onDismiss = { showFilters = false },
        )
    }
}

/** Image-led card: title sits on the photo, so the card is one tall visual unit instead of image + text block. */
@Composable
private fun TemplateCard(template: TripTemplate, daysWord: String, onClick: () -> Unit) {
    val accent = templateAccent(template)
    val primaryType = template.destinationTypes.firstOrNull()
    val shape = RoundedCornerShape(20.dp)

    Box(Modifier.fillMaxWidth().height(230.dp).clip(shape).clickable(onClick = onClick)) {
        if (template.coverImageUrl.isNotBlank()) {
            AsyncImage(
                model = template.coverImageUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize(),
            )
        } else {
            Box(Modifier.matchParentSize().background(accent.copy(alpha = 0.18f)))
            Icon(
                primaryType?.icon() ?: Icons.Filled.Map,
                contentDescription = null,
                tint = accent.copy(alpha = 0.55f),
                modifier = Modifier.align(Alignment.Center).size(48.dp),
            )
        }
        Box(
            Modifier.matchParentSize().background(
                Brush.verticalGradient(
                    0f to Color.Black.copy(alpha = 0.25f),
                    0.4f to Color.Transparent,
                    1f to Color.Black.copy(alpha = 0.8f),
                )
            )
        )

        Surface(
            shape = CircleShape,
            color = Color.Black.copy(alpha = 0.4f),
            modifier = Modifier.align(Alignment.TopEnd).padding(12.dp),
        ) {
            Row(Modifier.padding(horizontal = 10.dp, vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Schedule, null, tint = Color.White, modifier = Modifier.size(13.dp))
                Spacer(Modifier.width(4.dp))
                Text(daysWord, color = Color.White, style = MaterialTheme.typography.labelMedium)
            }
        }

        Column(Modifier.align(Alignment.BottomStart).padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Place, null, tint = Color.White.copy(alpha = 0.85f), modifier = Modifier.size(13.dp))
                Spacer(Modifier.width(4.dp))
                Text(
                    template.country,
                    color = Color.White.copy(alpha = 0.85f),
                    style = MaterialTheme.typography.labelMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Spacer(Modifier.height(2.dp))
            Text(
                template.title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            if (template.shortDescription.isNotBlank()) {
                Spacer(Modifier.height(4.dp))
                Text(
                    template.shortDescription,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.8f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                if (primaryType != null) {
                    Surface(shape = CircleShape, color = Color.White.copy(alpha = 0.22f)) {
                        Row(Modifier.padding(horizontal = 9.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(primaryType.icon(), null, tint = Color.White, modifier = Modifier.size(12.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(primaryType.label(), color = Color.White, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
                Surface(shape = CircleShape, color = Color.White.copy(alpha = 0.22f)) {
                    Text(
                        "${template.budgetTier.label()} · ${template.paceTag.label()}",
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                    )
                }
            }
        }
    }
}
