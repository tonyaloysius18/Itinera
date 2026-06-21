package com.itinera.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.itinera.app.i18n.Language
import com.itinera.app.i18n.LocalStrings
import com.itinera.app.ui.components.TopBar
import androidx.compose.runtime.remember

@Composable
fun LanguageScreen(
    selected: Language,
    onSelect: (Language) -> Unit,
    onBack: () -> Unit,
) {
    val s = LocalStrings.current

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // Header + fixed cards keep the horizontal inset
            TopBar(
                s.language,
                onBack = onBack,
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .offset(x = (-15).dp),
            )

            // ── Fixed (non-scrolling) section ──

            // "System default" card
            Spacer(Modifier.height(8.dp))
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
            ) {
                LanguageRow(
                    title = s.followPhone,
                    subtitle = s.systemDefault,
                    selected = selected == Language.SYSTEM,
                    onClick = { onSelect(Language.SYSTEM) },
                )
            }

            // Section header
            Text(
                s.chooseLanguage,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                modifier = Modifier.padding(start = 24.dp, top = 24.dp, bottom = 8.dp),
            )

            // ── Scrollable section: ONLY the language list, bled to the bottom edge ──
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                // round only the TOP corners so the card merges with the bottom edge
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
            ) {
                Column(
                    Modifier.verticalScroll(rememberScrollState())
                ) {
                    val langs = remember {
                        Language.entries
                            .filter { it != Language.SYSTEM }
                            .sortedWith(
                                compareByDescending<Language> { it == Language.ENGLISH }
                                    .thenBy { it.nativeName }
                            )
                    }
                    langs.forEachIndexed { index, lang ->
                        LanguageRow(
                            title = lang.nativeName,
                            subtitle = lang.englishName,
                            selected = selected == lang,
                            onClick = { onSelect(lang) },
                        )
                        if (index < langs.lastIndex) {
                            HorizontalDivider(
                                modifier = Modifier.padding(start = 16.dp),
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                            )
                        }
                    }

                    // keeps the LAST item clear of the gesture bar, inside the scroll
                    Spacer(Modifier.navigationBarsPadding())
                }
            }
        }
    }
}

@Composable
private fun LanguageRow(title: String, subtitle: String, selected: Boolean, onClick: () -> Unit) {
    val s = LocalStrings.current
    val primary = MaterialTheme.colorScheme.primary
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                color = if (selected) primary else MaterialTheme.colorScheme.onSurface,
            )
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
            )
        }
        if (selected) {
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
        } else {
            Surface(
                modifier = Modifier.size(24.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surface,
                border = androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f)),
            ) {}
        }
    }
}