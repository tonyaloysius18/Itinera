package com.itinera.app.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.itinera.app.i18n.LocalStrings
import com.itinera.app.model.Traveller
import com.itinera.app.model.fullName
import com.itinera.app.ui.components.TopBar
import kotlin.random.Random

private val AVATAR_COLORS = listOf(
    Color(0xFF5C7CFA), Color(0xFF51CF66), Color(0xFFFF922B),
    Color(0xFFE64980), Color(0xFF22B8CF), Color(0xFFBE4BDB),
    Color(0xFFFAB005), Color(0xFF20C997),
)

private fun colorFor(index: Int): Color = AVATAR_COLORS[index % AVATAR_COLORS.size]

private fun initials(first: String, last: String): String {
    val f = first.trim().take(1)
    val l = last.trim().take(1)
    val combined = (f + l).uppercase()
    return combined.ifBlank { "?" }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TravellersScreen(
    travellers: List<Traveller>,
    onBack: () -> Unit,
    onAdd: (Traveller) -> Unit,
    onUpdate: (Traveller) -> Unit,
    onDelete: (String) -> Unit,
    canEdit: Boolean = true,

    ) {
    val s = LocalStrings.current
    var showAdd by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<Traveller?>(null) }
    var pendingDelete by remember { mutableStateOf<Traveller?>(null) }

    Column(Modifier.fillMaxSize()) {
        TopBar(
            title = s.travellers,
            onBack = onBack,
            trailing = {
                if (canEdit) {
                IconButton(onClick = { showAdd = true }) {
                    Icon(Icons.Filled.PersonAdd, contentDescription = s.addTraveller, tint = MaterialTheme.colorScheme.primary)
                    }
                }
            },
        )

        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentPadding = PaddingValues(vertical = 8.dp),
        ) {
            items(travellers, key = { it.id }) { t ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .combinedClickable(
                            onClick = { if (canEdit) editing = t },
                            onLongClick = { if (canEdit && !t.isOwner) pendingDelete = t },
                        )
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // avatar
                    Box(
                        Modifier.size(44.dp).clip(CircleShape).background(colorFor(t.colorIndex)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            initials(t.firstName, t.surname),
                            color = Color.White,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(t.fullName, style = MaterialTheme.typography.bodyLarge)
                            if (t.isOwner) {
                                Spacer(Modifier.width(8.dp))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                ) {
                                    Text(
                                        s.you,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    )
                                }
                            }
                        }
                        val sub = listOf(t.email, t.phone).filter { it.isNotBlank() }.joinToString(" · ")
                        if (sub.isNotBlank()) {
                            Text(
                                sub,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAdd) {
        TravellerDialog(
            initial = null,
            onDismiss = { showAdd = false },
            onConfirm = { firstName, surname, email, phone ->
                onAdd(
                    Traveller(
                        id = "trav_${Random.nextLong()}",
                        firstName = firstName,
                        surname = surname,
                        email = email,
                        phone = phone,
                        colorIndex = travellers.size % AVATAR_COLORS.size,
                    ),
                )
                showAdd = false
            },
        )
    }

    editing?.let { t ->
        TravellerDialog(
            initial = t,
            onDismiss = { editing = null },
            onConfirm = { firstName, surname, email, phone ->
                onUpdate(t.copy(firstName = firstName, surname = surname, email = email, phone = phone))
                editing = null
            },
        )
    }

    pendingDelete?.let { t ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text(s.deleteTravellerQ) },
            text = { Text(t.fullName) },
            confirmButton = {
                TextButton(onClick = { onDelete(t.id); pendingDelete = null }) {
                    Text(s.delete, color = Color(0xFFE03131))
                }
            },
            dismissButton = { TextButton(onClick = { pendingDelete = null }) { Text(s.cancel) } },
        )
    }
}

@Composable
private fun TravellerDialog(
    initial: Traveller?,
    onDismiss: () -> Unit,
    onConfirm: (firstName: String, surname: String, email: String, phone: String) -> Unit,
) {
    val s = LocalStrings.current
    var firstName by remember { mutableStateOf(initial?.firstName ?: "") }
    var surname by remember { mutableStateOf(initial?.surname ?: "") }
    var email by remember { mutableStateOf(initial?.email ?: "") }
    var phone by remember { mutableStateOf(initial?.phone ?: "") }

    val canSave = firstName.isNotBlank() && surname.isNotBlank()

    fun String.toTitleCase(): String =
        split(" ").joinToString(" ") { word ->
            word.replaceFirstChar { c ->
                if (c.isLowerCase()) c.titlecase() else c.toString()
            }
        }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) s.addTraveller else s.editTraveller) },
        shape = RoundedCornerShape(16.dp),
        text = {
            Column {
                OutlinedTextField(
                    value = firstName,
                    onValueChange = { firstName = it.toTitleCase() },
                    label = { Text(s.name) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                )
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = surname,
                    onValueChange = { surname = it.toTitleCase() },
                    label = { Text(s.surname) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                )
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text(s.email) },
                    singleLine = true,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                )
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text(s.phone) },
                    singleLine = true,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { if (canSave) onConfirm(firstName.trim(), surname.trim(), email.trim(), phone.trim()) },
                enabled = canSave,
            ) { Text(if (initial == null) s.add else s.save) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(s.cancel) } },
    )
}