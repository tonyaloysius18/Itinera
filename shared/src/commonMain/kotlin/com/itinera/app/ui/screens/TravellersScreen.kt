package com.itinera.app.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.itinera.app.i18n.LocalStrings
import com.itinera.app.model.Traveller
import com.itinera.app.openEmail
import com.itinera.app.openPhone
import com.itinera.app.model.fullName
import com.itinera.app.ui.components.TopBar
import com.itinera.app.ui.components.TravellerAvatar
import com.itinera.app.ui.components.painter
import kotlin.random.Random

private val AVATAR_COLORS = listOf(
    Color(0xFF5C7CFA), Color(0xFF51CF66), Color(0xFFFF922B),
    Color(0xFFE64980), Color(0xFF22B8CF), Color(0xFFBE4BDB),
    Color(0xFFFAB005), Color(0xFF20C997),
)

/**
 * ⬅ CHANGED — was AVATAR_COLORS[colorIndex % size], but colorIndex is assigned
 * two different ways: `travellers.size % 8` when added here, and
 * `maxOf { colorIndex } + 1` in reconcileMembersToTravellers. Those collide,
 * which is how two travellers ended up the same purple.
 *
 * Hashing the id removes the shared counter entirely and is stable across
 * devices, so everyone sees the same colours.
 */
private fun colorFor(id: String): Color {
    if (id.isBlank()) return AVATAR_COLORS[0]
    val h = id.fold(0) { acc, c -> acc * 31 + c.code }
    return AVATAR_COLORS[((h % AVATAR_COLORS.size) + AVATAR_COLORS.size) % AVATAR_COLORS.size]
}

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
    currentUid: String = "",
    /** Opens the share/invite flow. Optional — omit and the prompt is hidden. */
    onInvite: (() -> Unit)? = null,
) {
    val s = LocalStrings.current
    var showAdd by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<Traveller?>(null) }
    var pendingDelete by remember { mutableStateOf<Traveller?>(null) }

    // ⬅ ADD — someone with a userId has joined the trip: they see the plans and
    // their expenses sync. Someone without is a name for splitting. On screen
    // those were indistinguishable.
    val (members, manual) = travellers.partition { it.userId.isNotBlank() }

    Column(Modifier.fillMaxSize()) {
        TopBar(
            title = s.travellers,
            onBack = onBack,
            trailing = {
                Row(verticalAlignment = Alignment.CenterVertically) {
//                    Text(
//                        "${travellers.size}",
//                        style = MaterialTheme.typography.labelMedium,
//                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f),
//                    )
                    if (canEdit) {
                        IconButton(onClick = { showAdd = true }) {
                            Icon(
                                Icons.Filled.PersonAdd,
                                contentDescription = s.addTraveller,
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                }
            },
        )

        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 32.dp),
        ) {
            if (members.isNotEmpty()) {
                item(key = "hdr-members") { SectionHeader(s.inTheApp)
                    //members.size)
                    }
                item(key = "card-members") {
                    TravellerCard(
                        travellers = members,
                        currentUid = currentUid,
                        canEdit = canEdit,
                        onEdit = { editing = it },
                        onLongPressDelete = { pendingDelete = it },
                    )
                    Spacer(Modifier.height(18.dp))
                }
            }

            if (manual.isNotEmpty()) {
                item(key = "hdr-manual") { SectionHeader(s.addedByYou)
                    //manual.size)
                    }
                item(key = "card-manual") {
                    TravellerCard(
                        travellers = manual,
                        currentUid = currentUid,
                        canEdit = canEdit,
                        onEdit = { editing = it },
                        onLongPressDelete = { pendingDelete = it },
                    )
                    Spacer(Modifier.height(14.dp))
                }

                // ⬅ ADD — the natural next action for a typed-in name.
                // createTripInvite() already exists in the repository.
                if (onInvite != null && canEdit) {
                    item(key = "invite") {
                        Surface(
                            modifier = Modifier.fillMaxWidth().clickable { onInvite() },
                            shape = RoundedCornerShape(12.dp),
                            color = Color.Transparent,
                            border = BorderStroke(
                                0.5.dp,
                                MaterialTheme.colorScheme.outline.copy(alpha = 0.45f),
                            ),
                        ) {
                            Row(
                                Modifier.padding(horizontal = 13.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(
                                    Icons.Filled.PersonAdd,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp),
                                )
                                Spacer(Modifier.width(11.dp))
                                Column {
                                    Text(s.inviteToTripTitle, style = MaterialTheme.typography.bodyMedium)
                                    Text(
                                        s.inviteToTripSubtitle,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                                    )
                                }
                            }
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
            onConfirm = { firstName, surname, email, phone, avatarId ->
                onAdd(
                    Traveller(
                        id = "trav_${Random.nextLong()}",
                        firstName = firstName,
                        surname = surname,
                        email = email,
                        phone = phone,
                        colorIndex = travellers.size % AVATAR_COLORS.size,
                        avatarId = avatarId,
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
            onConfirm = { firstName, surname, email, phone, avatarId ->
                onUpdate(
                    t.copy(
                        firstName = firstName,
                        surname = surname,
                        email = email,
                        phone = phone,
                        avatarId = avatarId,
                    )
                )
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

// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun SectionHeader(label: String)
                          //count: Int)
{
    Row(
        Modifier.fillMaxWidth().padding(start = 2.dp, end = 2.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
            modifier = Modifier.weight(1f),
        )
//        Text(
//            "$count",
//            style = MaterialTheme.typography.labelSmall,
//            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
//        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TravellerCard(
    travellers: List<Traveller>,
    currentUid: String,
    canEdit: Boolean,
    onEdit: (Traveller) -> Unit,
    onLongPressDelete: (Traveller) -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Column {
            travellers.forEachIndexed { index, t ->
                TravellerRow(
                    traveller = t,
                    isMe = t.userId == currentUid && currentUid.isNotBlank(),
                    canEdit = canEdit,
                    onEdit = { onEdit(t) },
                    onLongPressDelete = { onLongPressDelete(t) },
                )
                if (index < travellers.lastIndex) {
                    HorizontalDivider(
                        modifier = Modifier.padding(start = 65.dp),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TravellerRow(
    traveller: Traveller,
    isMe: Boolean,
    canEdit: Boolean,
    onEdit: () -> Unit,
    onLongPressDelete: () -> Unit,
) {
    val s = LocalStrings.current
    val onSurface = MaterialTheme.colorScheme.onSurface

    Row(
        Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = { if (canEdit) onEdit() },
                onLongClick = { if (canEdit && !traveller.isOwner) onLongPressDelete() },
            )
            .padding(horizontal = 13.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TravellerAvatarImage(
            traveller = traveller,
            size = 40.dp,
        )

        Spacer(Modifier.width(12.dp))

        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    traveller.fullName,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false),
                )
                if (isMe) {
                    Spacer(Modifier.width(6.dp))
                    Badge(s.you, accent = true)
                }
                // ⬅ ADD — isOwner existed on Traveller and nothing showed it.
                if (traveller.isOwner) {
                    Spacer(Modifier.width(6.dp))
                    Badge(s.owner, accent = false)
                }
            }

            val sub = listOf(traveller.email, traveller.phone)
                .filter { it.isNotBlank() }
                .joinToString(" · ")
            Text(
                // ⬅ CHANGED — a row with no contact details used to be a line
                // shorter than its neighbours, which read as a rendering glitch.
                sub.ifBlank { s.noContactDetails },
                style = MaterialTheme.typography.bodySmall,
                color = onSurface.copy(alpha = if (sub.isBlank()) 0.35f else 0.6f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        // ⬅ ADD — email and phone were shown as text but weren't actionable.
        // Phone wins when both are present; the row itself still opens edit.
        val contactIcon = when {
            traveller.phone.isNotBlank() -> Icons.Filled.Phone
            traveller.email.isNotBlank() -> Icons.Filled.MailOutline
            else -> null
        }
        if (contactIcon != null) {
            Spacer(Modifier.width(10.dp))
            Box(
                Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(onSurface.copy(alpha = 0.07f))
                    .clickable {
                        if (traveller.phone.isNotBlank()) openPhone(traveller.phone)
                        else openEmail(traveller.email)
                    },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    contactIcon,
                    contentDescription = if (traveller.phone.isNotBlank()) s.phone else s.email,
                    tint = onSurface.copy(alpha = 0.65f),
                    modifier = Modifier.size(16.dp),
                )
            }
        }
    }
}

@Composable
private fun Badge(label: String, accent: Boolean) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = if (accent) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else Color.Transparent,
        border = if (accent) null
        else BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)),
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = if (accent) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
        )
    }
}

/**
 * Illustrated avatar when the traveller has picked one, coloured initials
 * otherwise. Initials stay the default: a list of ten cartoons where nobody has
 * chosen is harder to scan than initials, since the pictures say nothing about
 * who's who until they're deliberate.
 */
@Composable
internal fun TravellerAvatarImage(
    traveller: Traveller,
    size: Dp,
    modifier: Modifier = Modifier,
) {
    val avatar = TravellerAvatar.fromId(traveller.avatarId)
    Box(
        modifier.size(size).clip(CircleShape).background(
            if (avatar == null) colorFor(traveller.id)
            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f)
        ),
        contentAlignment = Alignment.Center,
    ) {
        if (avatar != null) {
            Image(
                painter = avatar.painter(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize(),
            )
        } else {
            Text(
                initials(traveller.firstName, traveller.surname),
                color = Color.White,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

/** Avatar with an edit badge, for the traveller dialog. */
@Composable
private fun EditableAvatar(
    traveller: Traveller,
    onClick: () -> Unit,
) {
    val s = LocalStrings.current
    Box(Modifier.size(84.dp).clickable { onClick() }) {
        TravellerAvatarImage(traveller = traveller, size = 84.dp)
        Box(
            Modifier
                .align(Alignment.BottomEnd)
                .size(28.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary)
                .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Filled.Edit,
                contentDescription = s.chooseAvatar,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(15.dp),
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AvatarPickerDialog(
    selectedId: String,
    onPick: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val s = LocalStrings.current
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(s.chooseAvatar) },
        shape = RoundedCornerShape(20.dp),
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text(s.cancel) } },
        text = {
            Column {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    // "None" restores initials — without it, picking an avatar
                    // would be irreversible.
                    Box(
                        Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f))
                            .border(
                                width = if (selectedId.isBlank()) 2.dp else 0.dp,
                                color = if (selectedId.isBlank()) MaterialTheme.colorScheme.primary
                                else Color.Transparent,
                                shape = CircleShape,
                            )
                            .clickable { onPick("") },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            Icons.Filled.Person,
                            contentDescription = s.noAvatar,
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f),
                            modifier = Modifier.size(26.dp),
                        )
                    }

                    TravellerAvatar.entries.forEach { avatar ->
                        val selected = avatar.id == selectedId
                        Box(
                            Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .border(
                                    width = if (selected) 2.dp else 0.dp,
                                    color = if (selected) MaterialTheme.colorScheme.primary
                                    else Color.Transparent,
                                    shape = CircleShape,
                                )
                                .clickable { onPick(avatar.id) },
                        ) {
                            Image(
                                painter = avatar.painter(),
                                contentDescription = avatar.id,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.matchParentSize().clip(CircleShape),
                            )
                        }
                    }
                }
            }
        },
    )
}

@Composable
private fun TravellerDialog(
    initial: Traveller?,
    onDismiss: () -> Unit,
    onConfirm: (
        firstName: String,
        surname: String,
        email: String,
        phone: String,
        avatarId: String,
    ) -> Unit,
) {
    val s = LocalStrings.current
    var firstName by remember { mutableStateOf(initial?.firstName ?: "") }
    var surname by remember { mutableStateOf(initial?.surname ?: "") }
    var email by remember { mutableStateOf(initial?.email ?: "") }
    var phone by remember { mutableStateOf(initial?.phone ?: "") }
    var avatarId by remember { mutableStateOf(initial?.avatarId ?: "") }
    var showAvatarPicker by remember { mutableStateOf(false) }

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
                // ⬅ ADD — tap the avatar to choose an illustration.
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    EditableAvatar(
                        traveller = (initial ?: Traveller(id = "new", firstName = firstName)).copy(
                            firstName = firstName,
                            surname = surname,
                            avatarId = avatarId,
                        ),
                        onClick = { showAvatarPicker = true },
                    )
                }
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = firstName,
                    onValueChange = { firstName = it },   // ⬅ CHANGED — cased on save
                    label = { Text(s.name) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                )
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = surname,
                    onValueChange = { surname = it },     // ⬅ CHANGED — cased on save
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
                // ⬅ CHANGED — cased once here. Per keystroke it fought anyone
                // typing "van der Berg" or "d'Souza".
                onClick = {
                    if (canSave) onConfirm(
                        firstName.trim().toTitleCase(),
                        surname.trim().toTitleCase(),
                        email.trim(),
                        phone.trim(),
                        avatarId,
                    )
                },
                enabled = canSave,
            ) { Text(if (initial == null) s.add else s.save) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(s.cancel) } },
    )

    if (showAvatarPicker) {
        AvatarPickerDialog(
            selectedId = avatarId,
            onPick = { avatarId = it; showAvatarPicker = false },
            onDismiss = { showAvatarPicker = false },
        )
    }
}