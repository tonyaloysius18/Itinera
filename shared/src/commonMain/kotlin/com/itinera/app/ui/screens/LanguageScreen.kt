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

@Composable
fun LanguageScreen(
    selected: Language,
    onSelect: (Language) -> Unit,
    onBack: () -> Unit,
) {
    val s = LocalStrings.current

    Surface(                                                    // ⬅ ADDED: themed full-screen backdrop
        modifier = Modifier.fillMaxSize(),                      // ⬅ ADDED
        color = MaterialTheme.colorScheme.background,           // ⬅ ADDED: paints under status bar = no light band
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .statusBarsPadding()                            // ⬅ ADDED back: header clears the notch
                .padding(horizontal = 16.dp)
        ) {
            TopBar(s.language, onBack = onBack, modifier = Modifier.offset(x = (-15).dp))
            Column(
                Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {

                // "System default" card
                Spacer(Modifier.height(8.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),         // ⬅ CHANGED: removed .padding(horizontal = 16.dp)
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 2.dp,
                ) {
                    LanguageRow(
                        title = s.followPhone,
                        subtitle = Language.SYSTEM.englishName,
                        selected = selected == Language.SYSTEM,
                        onClick = { onSelect(Language.SYSTEM) },
                    )
                }

                // Section header
                Text(
                    s.chooseLanguage,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                    modifier = Modifier.padding(start = 8.dp, top = 24.dp, bottom = 8.dp),  // ⬅ CHANGED: start 24→8 (outer padding now handles indent)
                )

                // Main language list card
                Surface(
                    modifier = Modifier.fillMaxWidth(),         // ⬅ CHANGED: removed .padding(horizontal = 16.dp)
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 2.dp,
                ) {
                    Column {
                        val langs = Language.entries
                            .filter { it != Language.SYSTEM }
                            .sortedWith(
                                compareByDescending<Language> { it == Language.ENGLISH }   // English first
                                    .thenBy { it.nativeName }                              // then A→Z
                            )
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

                        Spacer(Modifier.navigationBarsPadding())
                    }
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