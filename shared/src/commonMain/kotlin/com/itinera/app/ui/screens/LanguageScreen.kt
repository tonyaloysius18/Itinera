package com.itinera.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.itinera.app.i18n.Language
import com.itinera.app.i18n.LocalStrings
import com.itinera.app.ui.components.TopBar

/*
 * ─── New string keys (EN + FR) ──────────────────────────────────────────────
 *   allLanguages     "All languages"      / "Toutes les langues"
 *   whereYouAre      "Where you are"      / "Où vous êtes"
 *   searchLanguages  "Search languages"   / "Rechercher une langue"
 *
 * Reused: language, followPhone, systemDefault, close, selected.
 * `chooseLanguage`, `search` and `suggestedLabel` are no longer used here.
 */

@Composable
fun LanguageScreen(
    selected: Language,
    onSelect: (Language) -> Unit,
    onBack: () -> Unit,
    /**
     * The language of the country the user is currently in. Shown alongside
     * English in the second card. Optional — omit and only English appears.
     *
     * Derive from the device region rather than its language: someone with an
     * English phone standing in Munich wants German offered, not English twice.
     */
    localLanguage: Language? = null,
    /** What "Follow phone" resolves to, e.g. "Français". Blank hides it. */
    systemLanguageName: String = "",
) {
    val s = LocalStrings.current
    var query by remember { mutableStateOf("") }

    // ⬅ CHANGED — untrimmed before, so a trailing space from autocorrect
    // silently emptied the list.
    val q = query.trim()

    val quickPicks = remember(localLanguage) {
        (listOf(Language.ENGLISH) + listOfNotNull(localLanguage)).distinct()
    }

    val allLangs = remember(q, quickPicks) {
        Language.entries
            .filter { it != Language.SYSTEM }
            // ⬅ ADD — the second card owns these, so don't list them twice.
            // While searching the cards are hidden, so everything is in scope.
            .filter { q.isNotBlank() || it !in quickPicks }
            .filter {
                q.isBlank() ||
                        it.nativeName.contains(q, ignoreCase = true) ||
                        it.englishName.contains(q, ignoreCase = true)
            }
            // ⬅ CHANGED — was a three-tier sort (English / European / rest) that
            // was real but invisible, so Deutsch after Français looked arbitrary.
            // Priority languages have their own card now; this is plainly A–Z.
            .sortedBy { it.englishName }
    }

    // The A–Z list always opens at the top. Scrolling to the selection was
    // disorienting: you'd land mid-alphabet with no sense of where you were.
    val listState = rememberLazyListState()

    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(Modifier.fillMaxSize().statusBarsPadding()) {
            // ⬅ CHANGED — was .padding(horizontal = 16.dp).offset(x = (-15).dp),
            // a near-equal offset fighting TopBar's own padding.
            TopBar(s.language, onBack = onBack)

            // The two cards are choices you make once; hide them while searching
            // so results aren't split across three places.
            if (q.isBlank()) {
                Spacer(Modifier.height(12.dp))

                // ── Card 1: system default ──
                Surface(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 2.dp,
                ) {
                    LanguageRow(
                        title = s.followPhone,
                        subtitle = listOf(s.systemDefault, systemLanguageName)
                            .filter { it.isNotBlank() }.joinToString(" · "),
                        selected = selected == Language.SYSTEM,
                        showRing = true,
                        onClick = { onSelect(Language.SYSTEM) },
                    )
                }

                Spacer(Modifier.height(20.dp))

                // ── Card 2: English + where you are ──
                Surface(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 2.dp,
                ) {
                    Column {
                        quickPicks.forEachIndexed { index, lang ->
                            LanguageRow(
                                title = lang.nativeName,
                                subtitle = lang.englishName,
                                selected = selected == lang,
                                showRing = true,
                                trailingLabel = if (lang == localLanguage && lang != Language.ENGLISH)
                                    s.whereYouAre else "",
                                onClick = { onSelect(lang) },
                            )
                            if (index < quickPicks.lastIndex) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(start = 16.dp),
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))
            } else {
                Spacer(Modifier.height(8.dp))
            }

            // ── Search ──
            LanguageSearchField(
                value = query,
                onValueChange = { query = it },
                onClear = { query = "" },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            )

            Spacer(Modifier.height(12.dp))

            // ── All languages, A–Z. Top-rounded so it merges with the bottom edge.
            Surface(
                modifier = Modifier.fillMaxWidth().weight(1f).padding(horizontal = 16.dp),
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
            ) {
                // ⬅ CHANGED — was Column(verticalScroll), which composed all ~40
                // languages on entry and recomposed every one on each keystroke.
                LazyColumn(state = listState) {
                    item(key = "hdr-all") {
                        Text(
                            s.allLanguages.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 4.dp),
                        )
                    }

                    items(allLangs, key = { it.name }) { lang ->
                        LanguageRow(
                            title = lang.nativeName,
                            subtitle = lang.englishName,
                            selected = selected == lang,
                            showRing = false,
                            onClick = { onSelect(lang) },
                        )
                        HorizontalDivider(
                            modifier = Modifier.padding(start = 16.dp),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                        )
                    }

                    item(key = "bottom-inset") { Spacer(Modifier.navigationBarsPadding()) }
                }
            }
        }
    }
}

@Composable
private fun LanguageSearchField(
    value: String,
    onValueChange: (String) -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val s = LocalStrings.current
    val onSurface = MaterialTheme.colorScheme.onSurface
    // ⬅ CHANGED — search was behind an icon toggle. With 40 languages it's the
    // fastest route, and hiding it cost a tap plus a layout shift.
    Surface(modifier = modifier, shape = RoundedCornerShape(22.dp), color = onSurface.copy(alpha = 0.06f)) {
        Row(
            Modifier.padding(horizontal = 14.dp, vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Filled.Search, null, tint = onSurface.copy(alpha = 0.45f), modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(10.dp))
            Box(Modifier.weight(1f)) {
                if (value.isEmpty()) {
                    Text(
                        s.searchLanguages,
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
            if (value.isNotEmpty()) {
                Icon(
                    Icons.Filled.Close,
                    contentDescription = s.close,
                    tint = onSurface.copy(alpha = 0.5f),
                    modifier = Modifier.size(18.dp).clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() },
                    ) { onClear() },
                )
            }
        }
    }
}

/**
 * @param showRing draw an empty circle when unselected. On the two top cards
 * this reads as a radio group you're choosing between. In the 40-row A–Z list
 * it would be 39 hollow rings marking one choice, so there it's off.
 */
@Composable
private fun LanguageRow(
    title: String,
    subtitle: String,
    selected: Boolean,
    showRing: Boolean,
    onClick: () -> Unit,
    trailingLabel: String = "",
) {
    val s = LocalStrings.current
    val primary = MaterialTheme.colorScheme.primary
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .background(if (selected) primary.copy(alpha = 0.10f) else Color.Transparent)
            // ⬅ CHANGED — was 14.dp vertical. Still well over the touch target
            // with two lines, and worth a lot of scroll across 40 items.
            .padding(horizontal = 16.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                color = if (selected) primary else MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (subtitle.isNotBlank()) {
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        if (trailingLabel.isNotBlank() && !selected) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color.Transparent,
                border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
            ) {
                Text(
                    trailingLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp),
                )
            }
            Spacer(Modifier.width(10.dp))
        }

        when {
            selected -> {
                Spacer(Modifier.width(10.dp))
                Surface(modifier = Modifier.size(24.dp), shape = CircleShape, color = primary) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Filled.Check,
                            contentDescription = s.selected,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(16.dp),
                        )
                    }
                }
            }
            showRing -> {
                Spacer(Modifier.width(10.dp))
                Surface(
                    modifier = Modifier.size(24.dp),
                    shape = CircleShape,
                    color = Color.Transparent,
                    border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f)),
                ) {}
            }
        }
    }
}

/**
 * Maps English country names to the language most useful there.
 *
 * Deliberately partial — covers common destinations rather than pretending to
 * be authoritative, and returns null for anything it doesn't know, so a miss
 * just means no suggestion.
 *
 * ⚠️ Multilingual countries are omitted on purpose. Offering German for
 * Switzerland or French for Belgium is a coin flip, and a confidently wrong
 * suggestion is worse than none.
 */
fun languageForCountry(country: String): Language? = when (country.trim().lowercase()) {
    "france" -> Language.FRENCH
    "germany", "austria" -> Language.GERMAN
    "spain", "mexico", "argentina", "colombia", "chile" -> Language.SPANISH
    "italy" -> Language.ITALIAN
    "portugal", "brazil" -> Language.PORTUGUESE
    "netherlands" -> Language.DUTCH
    "poland" -> Language.POLISH
    "russia" -> Language.RUSSIAN
    "ukraine" -> Language.UKRAINIAN
    "hungary" -> Language.HUNGARIAN
    "romania" -> Language.ROMANIAN
    "greece" -> Language.GREEK
    "bulgaria" -> Language.BULGARIAN
    "czechia", "czech republic" -> Language.CZECH
    "slovakia" -> Language.SLOVAK
    "slovenia" -> Language.SLOVENIAN
    "serbia" -> Language.SERBIAN
    "albania" -> Language.ALBANIAN
    "north macedonia" -> Language.MACEDONIAN
    "moldova" -> Language.MOLDAVIAN
    "norway" -> Language.NORWEGIAN
    "finland" -> Language.FINNISH
    "sweden" -> Language.SWEDISH
    "denmark" -> Language.DANISH
    "estonia" -> Language.ESTONIAN
    "latvia" -> Language.LATVIAN
    "lithuania" -> Language.LITHUANIAN
    else -> null
}