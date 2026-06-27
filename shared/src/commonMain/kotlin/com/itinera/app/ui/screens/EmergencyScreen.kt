package com.itinera.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.LocalPolice
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.itinera.app.deviceRegion
import com.itinera.app.dial
import com.itinera.app.i18n.LocalStrings
import com.itinera.app.ui.components.TopBar

private val SosRed = Color(0xFFD32F2F)

@Composable
fun EmergencyScreen(onBack: () -> Unit) {
    val s = LocalStrings.current
    // default to the device region if we have data for it, else fall back to a common one
    val defaultCode = remember {
        val region = deviceRegion().uppercase()
        if (emergencyNumbers.containsKey(region)) region else "US"
    }
    var countryCode by remember { mutableStateOf(defaultCode) }
    var showPicker by remember { mutableStateOf(false) }

    val info = emergencyNumbers[countryCode]

    Column(Modifier.fillMaxSize()) {
        TopBar(s.emergency, onBack = onBack)

        Column(
            Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
        ) {
            Spacer(Modifier.height(4.dp))

            // Country selector
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
                modifier = Modifier.fillMaxWidth().clickable { showPicker = true },
            ) {
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(s.country, style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f))
                        Text(
                            info?.countryName ?: countryCode,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                    Text(s.change, color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.labelLarge)
                }
            }

            Spacer(Modifier.height(20.dp))

            if (info == null) {
                Text(
                    s.noEmergencyData,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                )
            } else {
                // General — big, prominent
                if (info.general.isNotBlank()) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = SosRed,
                        modifier = Modifier.fillMaxWidth().clickable { dial(info.general) },
                    ) {
                        Row(
                            Modifier.fillMaxWidth().padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(
                                Modifier.size(54.dp).background(Color.White.copy(alpha = 0.2f), CircleShape),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(Icons.Filled.Warning, null, tint = Color.White, modifier = Modifier.size(30.dp))
                            }
                            Spacer(Modifier.width(16.dp))
                            Column(Modifier.weight(1f)) {
                                Text(s.emergency, color = Color.White.copy(alpha = 0.85f),
                                    style = MaterialTheme.typography.labelLarge)
                                Text(info.general, color = Color.White,
                                    style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                            }
                            Icon(Icons.Filled.Phone, null, tint = Color.White, modifier = Modifier.size(26.dp))
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                }

                // Specific services — show each; if identical to general, still useful to list
                val services = buildList {
                    add(Triple(s.police, info.police, Icons.Filled.LocalPolice))
                    add(Triple(s.ambulance, info.ambulance, Icons.Filled.LocalHospital))
                    add(Triple(s.fire, info.fire, Icons.Filled.LocalFireDepartment))
                }.filter { it.second.isNotBlank() }

                services.forEach { (label, number, icon) ->
                    ServiceRow(label, number, icon) { dial(number) }
                    Spacer(Modifier.height(10.dp))
                }

                Spacer(Modifier.height(12.dp))
                Text(
                    s.emergencyDialerNote,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                )
            }
        }
    }

    if (showPicker) {
        CountryPickerDialog(
            current = countryCode,
            onPick = { countryCode = it; showPicker = false },
            onDismiss = { showPicker = false },
        )
    }
}

@Composable
private fun ServiceRow(label: String, number: String, icon: ImageVector, onCall: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        modifier = Modifier.fillMaxWidth().clickable { onCall() },
    ) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                Modifier.size(42.dp).background(SosRed.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, null, tint = SosRed, modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.width(14.dp))
            Text(label, Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
            Text(number, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.width(10.dp))
            Icon(Icons.Filled.Phone, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
private fun CountryPickerDialog(
    current: String,
    onPick: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val s = LocalStrings.current
    var query by remember { mutableStateOf("") }
    val all = remember {
        emergencyNumbers.entries.map { it.key to it.value.countryName }.sortedBy { it.second }
    }
    val filtered = remember(query) {
        if (query.isBlank()) all
        else all.filter { it.second.contains(query.trim(), ignoreCase = true) }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text(s.close) } },
        title = { Text(s.selectCountry) },
        text = {
            Column {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    label = { Text(s.searchCountry) },
                    singleLine = true,
                    trailingIcon = {
                        if (query.isNotEmpty()) {
                            IconButton(onClick = { query = "" }) {
                                Icon(Icons.Filled.Close, contentDescription = "Clear")
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                )
                Spacer(Modifier.height(8.dp))
                LazyColumn(Modifier.heightIn(max = 360.dp)) {
                    items(filtered, key = { it.first }) { (code, name) ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clickable { onPick(code) }
                                .padding(vertical = 12.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(name, Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
                            if (code == current) Text("✓", color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        },
    )
}