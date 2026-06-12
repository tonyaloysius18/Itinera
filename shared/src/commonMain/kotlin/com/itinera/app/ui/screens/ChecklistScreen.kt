package com.itinera.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.outlined.CheckBoxOutlineBlank
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.itinera.app.i18n.LocalStrings
import com.itinera.app.model.ChecklistItem
import com.itinera.app.ui.components.Progress
import com.itinera.app.ui.components.TopBar

@Composable
fun ChecklistScreen(
    items: List<ChecklistItem>,
    onBack: () -> Unit,
    onToggle: (String) -> Unit,
    onAdd: (String, String) -> Unit,
) {
    val s = LocalStrings.current
    val doneCount = items.count { it.done }
    val pct = if (items.isEmpty()) 0 else (doneCount * 100 / items.size)

    var showAddDialog by remember { mutableStateOf(false) }

    // existing groups in this checklist, used to offer them in the dialog
    val existingGroups = items.map { it.group }.distinct()


    Column(Modifier.fillMaxSize()) {
        TopBar(s.beforeYouGo, onBack = onBack)
        Column(Modifier.padding(horizontal = 16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("$doneCount / ${items.size} ${s.done}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                Text("$pct%", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            }
            Spacer(Modifier.height(6.dp))
            Progress(if (items.isEmpty()) 0f else doneCount.toFloat() / items.size)
        }
        Spacer(Modifier.height(16.dp))
        Column(Modifier.weight(1f).padding(horizontal = 16.dp)) {
            items.groupBy { it.group }.forEach { (group, groupItems) ->
                Text(group, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), modifier = Modifier.padding(vertical = 6.dp))
                groupItems.forEach { item ->
                    Row(
                        Modifier.fillMaxWidth().clickable { onToggle(item.id) }.padding(vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        if (item.done) {
                            Icon(Icons.Filled.CheckBox, null, tint = Color(0xFF1D9E75))
                        } else {
                            Icon(Icons.Outlined.CheckBoxOutlineBlank, null, tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                        }
                        Spacer(Modifier.width(10.dp))
                        Text(
                            item.text,
                            style = MaterialTheme.typography.bodyLarge,
                            textDecoration = if (item.done) TextDecoration.LineThrough else null,
                            color = if (item.done) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
            }
        }
        Box(
            modifier = Modifier.fillMaxWidth().padding(16.dp).padding(bottom = 60.dp),
            contentAlignment = Alignment.Center
        ) {
            Button(
                onClick = { showAddDialog = true },                  // ⬅ was TODO
                shape = androidx.compose.foundation.shape.CircleShape,
                contentPadding = PaddingValues(horizontal = 25.dp, vertical = 8.dp)
            ) {
                Icon(Icons.Filled.Add, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(s.addItem)
            }
        }
    }

    if (showAddDialog) {                                             // ⬅ the dialog
        AddChecklistItemDialog(
            existingGroups = existingGroups,
            onDismiss = { showAddDialog = false },
            onAdd = { text, group ->
                onAdd(text, group)
                showAddDialog = false
            },
        )
    }
}

// ---- Add-item dialog ----                                         // ⬅ new composable
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddChecklistItemDialog(
    existingGroups: List<String>,
    onDismiss: () -> Unit,
    onAdd: (String, String) -> Unit,
) {

    val s = LocalStrings.current

    // offer existing groups + a few sensible defaults, de-duplicated
    val groupOptions = (existingGroups + listOf(s.documents, s.bookings, s.packing, s.transport, s.money, s.gadget, s.other))
        .distinct()

    var text by remember { mutableStateOf("") }
    var group by remember { mutableStateOf(groupOptions.firstOrNull() ?: "Other") }
    var userPicked by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }

    val textFieldShape = RoundedCornerShape(12.dp)





    // auto-suggest a group from the text until the user picks one manually
    LaunchedEffect(text) {
        if (!userPicked) group = suggestGroup(text, groupOptions)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = { if (text.isNotBlank()) onAdd(text.trim(), group) }, enabled = text.isNotBlank()) {
                Text(s.add)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(s.cancel) } },
        title = { Text(s.addItem) },
        text = {
            Column {
                OutlinedTextField(
                    value = text,
                    onValueChange = { input ->
                        text = input.replaceFirstChar { if (it.isLowerCase()) it.uppercase() else it.toString() }
                    },
                    label = { Text(s.needToAdd) },
                    singleLine = true,
                    //keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                    modifier = Modifier.fillMaxWidth(),
                    shape = textFieldShape,
                )
                Spacer(Modifier.height(12.dp))
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                    OutlinedTextField(
                        value = group,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(s.section) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        shape = textFieldShape,
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        groupOptions.forEach { g ->
                            DropdownMenuItem(
                                text = { Text(g) },
                                onClick = { group = g; userPicked = true; expanded = false },
                            )
                        }
                    }
                }
            }
        },
    )
}

// keyword auto-suggest → returns a group name that exists in options, else "Other"
private fun suggestGroup(text: String, options: List<String>): String {
    val t = text.lowercase()
    fun has(vararg w: String) = w.any { t.contains(it) }
    val guess = when {
        has("passport", "visa", "insurance", "document", "licence", "id") -> "Documents"
        has("hotel", "hostel", "book", "reserve", "Airbnb", "flight", "ticket", "reservation") -> "Bookings"
        has("pack", "clothes", "baby bag", "backpack", "sunscreen", "shoes", "suitcase", "handbag", "bag") -> "Packing"
        has("bus", "train", "transfer", "taxi", "metro", "car", "taxi", "shuttle", "subway", "train") -> "Transport"
        has("cash", "money", "card", "currency", "atm", "exchange", "credit card", "debit card", "credit") -> "Money"
        has(
            "drone",
            "camera",
            "hard disk",
            "laptop",
            "charger",
            "trimmer",
            "tripod",
            "mobile",
            "phone",
            "tablet",
            "computer",
            "printer",
            "scanner",
            "projector",
            "monitor",
            "headphones",
            "speaker",
            "microphone",
            "mouse",
            "keyboard"
        ) -> "Gadget"
        else -> "Other"
    }
    // only return the guess if that section is actually offered; else first option
    return if (options.contains(guess)) guess else options.firstOrNull() ?: "Other"
}