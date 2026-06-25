package com.itinera.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.outlined.CheckBoxOutlineBlank
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
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
            if (items.isEmpty()) {
                Column(
                    Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Icon(
                        Icons.Outlined.CheckBoxOutlineBlank,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f),
                        modifier = Modifier.size(52.dp),
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        s.noChecklistItems,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        s.noChecklistSubtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 24.dp),
                    )
                }
            } else {
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
    }
        Box(
            modifier = Modifier.fillMaxWidth().padding(16.dp).padding(bottom = 60.dp),
            contentAlignment = Alignment.Center
        ) {
            Button(
                onClick = { showAddDialog = true },
                modifier = Modifier.padding(bottom = 10.dp),
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
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
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

    fun String.toTitleCase(): String =
        split(" ").joinToString(" ") { word ->
            word.replaceFirstChar { c ->
                if (c.isLowerCase()) c.titlecase() else c.toString()
            }
        }

    // auto-suggest a group from the text until the user picks one manually
    LaunchedEffect(text) {
        if (!userPicked) group = suggestGroup(
            text,
            documents = s.documents,
            bookings = s.bookings,
            packing = s.packing,
            transport = s.transport,
            money = s.money,
            gadget = s.gadget,
            other = s.other,
        )
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
                        text = input.toTitleCase()
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
                        modifier = Modifier.menuAnchor(
                            type = ExposedDropdownMenuAnchorType.PrimaryNotEditable,
                            enabled = true
                        ).fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        groupOptions.forEach { g ->
                            DropdownMenuItem(
                                text = { Text(g) },
                                onClick = { group = g; userPicked = true; expanded = false },
                                shape = RoundedCornerShape(12.dp),
                            )
                        }
                    }
                }
            }
        },
    )
}

// keyword auto-suggest → returns a group name that exists in options, else "Other"
// keyword auto-suggest → returns one of the provided localized labels, else the "other" label
private fun suggestGroup(
    text: String,
    documents: String,
    bookings: String,
    packing: String,
    transport: String,
    money: String,
    gadget: String,
    other: String,
): String {
    val t = text.lowercase()
    fun has(vararg w: String) = w.any { t.contains(it) }
    return when {
        // Documents — passport / visa / insurance / id / licence
        has("passport", "visa", "insurance", "document", "licence", "license", "id",
            "passeport", "assurance", "papiers", "pièce",                          // FR
            "pasaporte", "seguro", "documento", "carnet",                          // ES
            "reisepass", "ausweis", "versicherung", "dokument",                    // DE
            "passaporto", "assicurazione", "documento", "patente",                 // IT
            "passaporte", "seguro", "documento", "carteira"                        // PT
        ) -> documents

        // Bookings — hotel / flight / ticket / reservation
        has("hotel", "hostel", "book", "reserve", "airbnb", "flight", "ticket", "reservation",
            "hôtel", "vol", "billet", "réservation", "auberge",                    // FR
            "vuelo", "billete", "reserva", "alojamiento",                          // ES
            "flug", "ticket", "buchung", "unterkunft", "reservierung",             // DE
            "volo", "biglietto", "prenotazione", "albergo",                        // IT
            "voo", "bilhete", "reserva", "hospedagem"                              // PT
        ) -> bookings

        // Packing — clothes / bag / shoes / suitcase
        has("pack", "clothes", "backpack", "sunscreen", "shoes", "suitcase", "handbag", "bag",
            "valise", "vêtements", "chaussures", "sac", "bagage",                  // FR
            "maleta", "ropa", "zapatos", "bolsa", "equipaje",                      // ES
            "koffer", "kleidung", "schuhe", "tasche", "gepäck",                    // DE
            "valigia", "vestiti", "scarpe", "borsa", "bagaglio",                   // IT
            "mala", "roupas", "sapatos", "bolsa", "bagagem"                        // PT
        ) -> packing

        // Transport — bus / train / taxi / car
        has("bus", "train", "transfer", "taxi", "metro", "car", "shuttle", "subway",
            "voiture", "métro", "navette", "transfert",                            // FR
            "tren", "coche", "autobús", "metro", "traslado",                       // ES
            "zug", "auto", "u-bahn", "transfer",                                   // DE
            "treno", "auto", "metropolitana", "navetta",                           // IT
            "trem", "carro", "ônibus", "metrô", "transporte"                       // PT
        ) -> transport

        // Money — cash / card / currency / atm
        has("cash", "money", "card", "currency", "atm", "exchange", "credit", "debit",
            "argent", "carte", "monnaie", "espèces", "change",                     // FR
            "dinero", "tarjeta", "efectivo", "cambio", "moneda",                   // ES
            "geld", "karte", "bargeld", "währung", "wechsel",                      // DE
            "soldi", "carta", "contanti", "valuta", "cambio",                      // IT
            "dinheiro", "cartão", "moeda", "câmbio"                                // PT
        ) -> money

        // Gadget — electronics
        has("drone", "camera", "hard disk", "laptop", "charger", "trimmer", "tripod",
            "mobile", "phone", "tablet", "computer", "printer", "scanner", "projector",
            "monitor", "headphones", "speaker", "microphone", "mouse", "keyboard",
            "appareil", "caméra", "chargeur", "téléphone", "ordinateur", "casque", // FR
            "cámara", "cargador", "teléfono", "ordenador", "auriculares",          // ES
            "kamera", "ladegerät", "telefon", "computer", "kopfhörer",             // DE
            "fotocamera", "caricabatterie", "telefono", "computer", "cuffie",      // IT
            "câmera", "carregador", "telefone", "computador", "fones"              // PT
        ) -> gadget

        else -> other
    }
}