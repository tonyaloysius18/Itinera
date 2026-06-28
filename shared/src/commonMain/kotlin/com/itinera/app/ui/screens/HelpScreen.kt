package com.itinera.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.itinera.app.i18n.LocalStrings
import com.itinera.app.ui.components.TopBar

@Composable
fun HelpScreen(onBack: () -> Unit) {
    val s = LocalStrings.current   // only used for the screen title

    var searching by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }

    val filtered = remember(query) {
        val q = query.trim()
        if (q.isBlank()) HelpContent.faqs
        else HelpContent.faqs.filter {
            it.question.contains(q, ignoreCase = true) || it.answer.contains(q, ignoreCase = true)
        }
    }

    Column(Modifier.fillMaxSize()) {
        TopBar(
            title = s.help,
            onBack = onBack,
            trailing = {
                IconButton(onClick = {
                    searching = !searching
                    if (!searching) query = ""   // closing the search clears it
                }) {
                    Icon(
                        if (searching) Icons.Filled.Close else Icons.Filled.Search,
                        contentDescription = if (searching) s.close else s.search,
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                }
            },
        )

        // Search box, revealed under the bar when the search icon is tapped
        AnimatedVisibility(visible = searching) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text(s.search) },
                singleLine = true,
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { query = "" }) {
                            Icon(Icons.Filled.Close, contentDescription = s.close)
                        }
                    }
                },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
            )
        }

        LazyColumn(
            Modifier.fillMaxSize().padding(horizontal = 16.dp),
        ) {
            // Intro line — only when not actively searching, to keep results clean
            if (!searching) {
                item {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        HelpContent.intro,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                        modifier = Modifier.padding(vertical = 8.dp),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                    )
                    Spacer(Modifier.height(8.dp))
                }
            } else {
                item { Spacer(Modifier.height(8.dp)) }
            }

            if (filtered.isEmpty()) {
                item {
                    Text(
                        s.noResults,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                        modifier = Modifier.padding(vertical = 24.dp),
                    )
                }
            } else {
                items(filtered, key = { it.question }) { faq ->
                    FaqItem(faq)
                    Spacer(Modifier.height(10.dp))
                }
            }

            item {
                Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.safeDrawing))
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun FaqItem(faq: HelpContent.Faq) {
    var expanded by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
    ) {
        Column(Modifier.animateContentSize()) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    faq.question,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f),
                )
                Spacer(Modifier.width(12.dp))
                Icon(
                    if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                )
            }
            AnimatedVisibility(visible = expanded) {
                Text(
                    faq.answer,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
                    textAlign = TextAlign.Justify,
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                )
            }
        }
    }
}