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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
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
    // Moderation (Apple UGC safety): report/block an app member on a shared trip.
    blockedUserIds: Set<String> = emptySet(),
    onReport: suspend (String, String, String) -> Boolean = { _, _, _ -> false },
    onBlock: (String) -> Unit = {},
    onUnblock: (String) -> Unit = {},
    onMessage: (String) -> Unit = {},
) {
    val s = LocalStrings.current
    var showAdd by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<Traveller?>(null) }
    var pendingDelete by remember { mutableStateOf<Traveller?>(null) }

    // Moderation dialog targets (member uid being acted on), null when closed.
    var reportTarget by remember { mutableStateOf<String?>(null) }
    var blockTarget by remember { mutableStateOf<String?>(null) }
    var unblockTarget by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

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
                        // moderation only on real app members (they have a userId)
                        moderatable = true,
                        blockedUserIds = blockedUserIds,
                        onReport = { uid -> reportTarget = uid },
                        onBlock = { uid -> blockTarget = uid },
                        onUnblock = { uid -> unblockTarget = uid },
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

    reportTarget?.let { uid ->
        ReportMemberDialog(
            onDismiss = { reportTarget = null },
            onSubmit = { reason, details ->
                reportTarget = null
                scope.launch {
                    val ok = onReport(uid, reason, details)
                    onMessage(if (ok) s.reportSubmitted else s.reportFailed)
                }
            },
        )
    }

    blockTarget?.let { uid ->
        AlertDialog(
            onDismissRequest = { blockTarget = null },
            title = { Text(s.blockMemberQ) },
            text = { Text(s.blockMemberDesc) },
            confirmButton = {
                TextButton(onClick = { blockTarget = null; onBlock(uid); onMessage(s.memberBlocked) }) {
                    Text(s.block, color = Color(0xFFE03131))
                }
            },
            dismissButton = { TextButton(onClick = { blockTarget = null }) { Text(s.cancel) } },
        )
    }

    unblockTarget?.let { uid ->
        AlertDialog(
            onDismissRequest = { unblockTarget = null },
            title = { Text(s.unblockMemberQ) },
            text = { Text(s.unblockMemberDesc) },
            confirmButton = {
                TextButton(onClick = { unblockTarget = null; onUnblock(uid); onMessage(s.memberUnblocked) }) {
                    Text(s.unblock)
                }
            },
            dismissButton = { TextButton(onClick = { unblockTarget = null }) { Text(s.cancel) } },
        )
    }
}

@Composable
private fun ReportMemberDialog(
    onDismiss: () -> Unit,
    onSubmit: (reason: String, details: String) -> Unit,
) {
    val s = LocalStrings.current
    val reasons = listOf(
        "spam" to s.reportReasonSpam,
        "harassment" to s.reportReasonHarassment,
        "inappropriate" to s.reportReasonInappropriate,
        "other" to s.reportReasonOther,
    )
    var selected by remember { mutableStateOf(reasons.first().first) }
    var details by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(s.reportMember) },
        text = {
            Column(Modifier.fillMaxWidth()) {
                Text(
                    s.reportReason,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                )
                Spacer(Modifier.height(8.dp))
                reasons.forEach { (key, label) ->
                    Row(
                        Modifier.fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { selected = key }
                            .padding(vertical = 8.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(selected = selected == key, onClick = { selected = key })
                        Spacer(Modifier.width(6.dp))
                        Text(label, style = MaterialTheme.typography.bodyLarge)
                    }
                }
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = details,
                    onValueChange = { details = it },
                    label = { Text(s.reportDetailsHint) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 4,
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onSubmit(selected, details) }) {
                Text(s.reportSubmit, color = Color(0xFFE03131))
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(s.cancel) } },
    )
}

// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun SectionHeader(label: String)
                          //count: Int)
{
    Row(
        Modifier.fillMaxWidth().padding(start = 2.dp, end = 2.dp, bottom = 10.dp, top = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            label.uppercase(),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
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
    moderatable: Boolean = false,
    blockedUserIds: Set<String> = emptySet(),
    onReport: (String) -> Unit = {},
    onBlock: (String) -> Unit = {},
    onUnblock: (String) -> Unit = {},
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Column {
            travellers.forEachIndexed { index, t ->
                val isMe = t.userId == currentUid && currentUid.isNotBlank()
                // A member (has userId) that isn't me can be reported/blocked.
                val canModerate = moderatable && !isMe && t.userId.isNotBlank()
                TravellerRow(
                    traveller = t,
                    isMe = isMe,
                    canEdit = canEdit,
                    onEdit = { onEdit(t) },
                    onLongPressDelete = { onLongPressDelete(t) },
                    canModerate = canModerate,
                    isBlocked = t.userId in blockedUserIds,
                    onReport = { onReport(t.userId) },
                    onBlock = { onBlock(t.userId) },
                    onUnblock = { onUnblock(t.userId) },
                )
                if (index < travellers.lastIndex) {
                    HorizontalDivider(
                        modifier = Modifier.padding(start = 88.dp),
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
    canModerate: Boolean = false,
    isBlocked: Boolean = false,
    onReport: () -> Unit = {},
    onBlock: () -> Unit = {},
    onUnblock: () -> Unit = {},
) {
    val s = LocalStrings.current
    val onSurface = MaterialTheme.colorScheme.onSurface
    var moderateOpen by remember { mutableStateOf(false) }

    Row(
        Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = { if (canEdit) onEdit() },
                onLongClick = { if (canEdit && !traveller.isOwner) onLongPressDelete() },
            )
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TravellerAvatarImage(
            traveller = traveller,
            size = 56.dp,
        )

        Spacer(Modifier.width(16.dp))

        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    traveller.fullName,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false),
                )
                if (isMe) {
                    Spacer(Modifier.width(8.dp))
                    Badge(s.you, accent = true)
                }
                // ⬅ ADD — isOwner existed on Traveller and nothing showed it.
                if (traveller.isOwner) {
                    Spacer(Modifier.width(8.dp))
                    Badge(s.owner, accent = false)
                }
            }

            if (traveller.phone.isNotBlank()) {
                Text(
                    traveller.phone,
                    style = MaterialTheme.typography.bodyMedium,
                    color = onSurface.copy(alpha = 0.6f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            if (traveller.email.isNotBlank()) {
                Text(
                    traveller.email,
                    style = MaterialTheme.typography.bodyMedium,
                    color = onSurface.copy(alpha = 0.6f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            if (traveller.phone.isBlank() && traveller.email.isBlank()) {
                Text(
                    s.noContactDetails,
                    style = MaterialTheme.typography.bodyMedium,
                    color = onSurface.copy(alpha = 0.35f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        // ⬅ ADD — email and phone were shown as text but weren't actionable.
        // Phone wins when both are present; the row itself still opens edit.
        val contactIcon = when {
            traveller.phone.isNotBlank() -> Icons.Filled.Phone
            traveller.email.isNotBlank() -> Icons.Filled.MailOutline
            else -> null
        }
        if (contactIcon != null) {
            Spacer(Modifier.width(12.dp))
            Box(
                Modifier
                    .size(40.dp)
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
                    modifier = Modifier.size(20.dp),
                )
            }
        }

        // Moderation menu: report/block any app member but yourself (Apple UGC safety).
        if (canModerate) {
            Spacer(Modifier.width(4.dp))
            Box {
                IconButton(onClick = { moderateOpen = true }) {
                    Icon(Icons.Filled.MoreVert, contentDescription = s.reportMember, tint = onSurface.copy(alpha = 0.65f))
                }
                DropdownMenu(
                    expanded = moderateOpen,
                    onDismissRequest = { moderateOpen = false },
                    shape = RoundedCornerShape(16.dp),
                ) {
                    DropdownMenuItem(
                        text = { Text(s.reportMember) },
                        leadingIcon = { Icon(Icons.Filled.Flag, null) },
                        onClick = { moderateOpen = false; onReport() },
                    )
                    if (isBlocked) {
                        DropdownMenuItem(
                            text = { Text(s.unblock) },
                            leadingIcon = { Icon(Icons.Filled.LockOpen, null) },
                            onClick = { moderateOpen = false; onUnblock() },
                        )
                    } else {
                        DropdownMenuItem(
                            text = { Text(s.block, color = MaterialTheme.colorScheme.error) },
                            leadingIcon = { Icon(Icons.Filled.Block, null, tint = MaterialTheme.colorScheme.error) },
                            onClick = { moderateOpen = false; onBlock() },
                        )
                    }
                }
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
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = if (accent) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
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
    showPersonPlaceholder: Boolean = false,
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
        } else if (
            showPersonPlaceholder &&
            traveller.firstName.isBlank() &&
            traveller.surname.isBlank()
        ) {
            Icon(
                imageVector = Icons.Filled.Person,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.fillMaxSize(0.5f),
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

@OptIn(ExperimentalMaterial3Api::class)
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
    val stateKey = initial?.id ?: "new"
    var firstName by remember(stateKey) { mutableStateOf(initial?.firstName ?: "") }
    var surname by remember(stateKey) { mutableStateOf(initial?.surname ?: "") }
    var email by remember(stateKey) { mutableStateOf(initial?.email ?: "") }
    var phone by remember(stateKey) { mutableStateOf(initial?.phone ?: "") }
    var avatarId by remember(stateKey) { mutableStateOf(initial?.avatarId ?: "") }
    var showAvatarPicker by remember(stateKey) { mutableStateOf(false) }
    var showContactDetails by remember(stateKey) {
        mutableStateOf(initial?.email?.isNotBlank() == true || initial?.phone?.isNotBlank() == true)
    }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val focusManager = LocalFocusManager.current

    val canSave = firstName.isNotBlank() && surname.isNotBlank()

    fun String.toTitleCase(): String =
        split(" ").joinToString(" ") { word ->
            word.replaceFirstChar { c ->
                if (c.isLowerCase()) c.titlecase() else c.toString()
            }
        }

    fun saveTraveller() {
        if (canSave) {
            focusManager.clearFocus()
            onConfirm(
                firstName.trim().toTitleCase(),
                surname.trim().toTitleCase(),
                email.trim(),
                phone.trim(),
                avatarId,
            )
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .imePadding()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(bottom = 20.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    if (initial == null) s.addTraveller else s.editTraveller,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f),
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Filled.Close, contentDescription = s.close)
                }
            }

            Spacer(Modifier.height(16.dp))

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClickLabel = s.chooseAvatar) { showAvatarPicker = true },
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.72f),
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TravellerAvatarImage(
                        traveller = (initial ?: Traveller(id = "new", firstName = firstName)).copy(
                            firstName = firstName,
                            surname = surname,
                            avatarId = avatarId,
                        ),
                        size = 64.dp,
                        showPersonPlaceholder = true,
                    )
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            s.profilePhoto,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Spacer(Modifier.height(3.dp))
                        Text(
                            "${s.optional} · ${s.chooseAvatar}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.72f),
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            Icons.Filled.Edit,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
            }

            Spacer(Modifier.height(18.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                OutlinedTextField(
                    value = firstName,
                    onValueChange = { firstName = it },
                    label = { Text(s.firstName) },
                    singleLine = true,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Next) },
                    ),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                )
                OutlinedTextField(
                    value = surname,
                    onValueChange = { surname = it },
                    label = { Text(s.surname) },
                    singleLine = true,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Next) },
                    ),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                )
            }

            Spacer(Modifier.height(12.dp))

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClickLabel = s.contact) {
                        showContactDetails = !showContactDetails
                    },
                shape = RoundedCornerShape(14.dp),
                color = Color.Transparent,
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        s.contact,
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        s.optional,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                    )
                    Spacer(Modifier.width(6.dp))
                    Icon(
                        if (showContactDetails) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.62f),
                    )
                }
            }

            if (showContactDetails) {
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text(s.email) },
                    singleLine = true,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next,
                    ),
                    keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Next) },
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                )
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text(s.phone) },
                    singleLine = true,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = KeyboardType.Phone,
                        imeAction = ImeAction.Done,
                    ),
                    keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                        onDone = { focusManager.clearFocus() },
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                )
            }

            Spacer(Modifier.height(18.dp))

            Button(
                onClick = { saveTraveller() },
                enabled = canSave,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(16.dp),
            ) {
                Text(if (initial == null) s.addTraveller else s.saveChanges)
            }
        }
    }

    if (showAvatarPicker) {
        AvatarPickerDialog(
            selectedId = avatarId,
            onPick = { avatarId = it; showAvatarPicker = false },
            onDismiss = { showAvatarPicker = false },
        )
    }
}
