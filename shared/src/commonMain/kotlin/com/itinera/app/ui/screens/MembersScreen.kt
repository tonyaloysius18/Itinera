package com.itinera.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.RemoveCircleOutline
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.itinera.app.data.rememberFileSharer
import com.itinera.app.i18n.LocalStrings
import com.itinera.app.model.Trip
import com.itinera.app.ui.components.PlaneLoader
import kotlinx.coroutines.launch

/**
 * Shows everyone on a trip and (for the owner) lets them assign roles or remove members.
 * Non-owners see a read-only roster plus a "Leave trip" button. The owner is shown first
 * and cannot be changed or removed. The owner also gets an Invite button.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MembersScreen(
    trip: Trip,
    currentUid: String,
    blockedUserIds: Set<String> = emptySet(),
    onSetRole: (String, String) -> Unit,      // (memberUid, role)
    onRemoveMember: (String) -> Unit,          // (memberUid)
    onCreateInvite: suspend (String) -> String?,
    onReport: suspend (String, String, String) -> Boolean = { _, _, _ -> false }, // (uid, reason, details)
    onBlock: (String) -> Unit = {},            // (memberUid)
    onUnblock: (String) -> Unit = {},          // (memberUid)
    onMessage: (String) -> Unit = {},
    onLeaveTrip: () -> Unit,
    onBack: () -> Unit,
) {
    val s = LocalStrings.current
    val isOwner = trip.ownerId == currentUid
    var showInvite by remember { mutableStateOf(false) }
    var confirmLeave by remember { mutableStateOf(false) }

    // Moderation dialog targets (member uid being acted on), null when closed.
    var reportTarget by remember { mutableStateOf<String?>(null) }
    var blockTarget by remember { mutableStateOf<String?>(null) }
    var unblockTarget by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    // Order: owner first, then others by name.
    val ordered = remember(trip.members, trip.memberInfo) {
        trip.members.entries.sortedWith(
            compareByDescending<Map.Entry<String, String>> { it.value == "owner" }
                .thenBy { trip.memberInfo[it.key]?.name ?: it.key }
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
                title = { Text(s.travellers) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = s.back)
                    }
                },
            )
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {

            if (isOwner) {
                // Invite button at the top (owner only)
                Surface(
                    onClick = { showInvite = true },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    border = androidx.compose.foundation.BorderStroke(
                        0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                    ),
                ) {
                    Row(
                        Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(Icons.Filled.PersonAdd, null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(12.dp))
                        Text(
                            s.inviteToTrip,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                }
            }

            LazyColumn(
                Modifier.weight(1f).fillMaxWidth(),
                contentPadding = PaddingValues(vertical = 8.dp),
            ) {
                items(ordered, key = { it.key }) { entry ->
                    val uid = entry.key
                    val role = entry.value
                    val info = trip.memberInfo[uid]
                    val isThisOwner = role == "owner"
                    val isMe = uid == currentUid
                    val isBlocked = uid in blockedUserIds

                    // A blocked member's identity is hidden until they're unblocked.
                    val displayName = if (isBlocked) s.blockedUser else (
                        info?.name?.takeIf { it.isNotBlank() }
                            ?: info?.email?.takeIf { it.isNotBlank() }
                            ?: uid.take(8)
                    )

                    MemberRow(
                        name = displayName + if (isMe) " (${s.you})" else "",
                        email = if (isBlocked) "" else (info?.email ?: ""),
                        role = role,
                        canEdit = isOwner && !isThisOwner,   // owner edits everyone except the owner row
                        canModerate = !isMe,                 // report/block anyone but yourself
                        isBlocked = isBlocked,
                        onSetRole = { newRole -> onSetRole(uid, newRole) },
                        onRemove = { onRemoveMember(uid) },
                        onReport = { reportTarget = uid },
                        onBlock = { blockTarget = uid },
                        onUnblock = { unblockTarget = uid },
                    )
                }
            }

            // Leave trip (non-owners only) — pinned under the list
            if (!isOwner) {
                Surface(
                    onClick = { confirmLeave = true },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 70.dp, vertical = 12.dp),
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    border = androidx.compose.foundation.BorderStroke(
                        0.5.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.4f)
                    ),
                ) {
                    Row(
                        Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.Logout,
                            null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp),
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(
                            s.leaveTrip,
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                }
            }
        }

        if (confirmLeave) {
            AlertDialog(
                onDismissRequest = { confirmLeave = false },
                title = { Text(s.leaveTripQ) },
                text = { Text(s.leaveTripDesc) },
                confirmButton = {
                    TextButton(onClick = { confirmLeave = false; onLeaveTrip() }) {
                        Text(s.leaveTrip, color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = { TextButton(onClick = { confirmLeave = false }) { Text(s.cancel) } },
            )
        }

        if (showInvite) {
            InviteDialog(
                trip = trip,
                onCreateInvite = onCreateInvite,
                onDismiss = { showInvite = false },
            )
        }

        reportTarget?.let { uid ->
            ReportDialog(
                onDismiss = { reportTarget = null },
                onSubmit = { reason, details ->
                    val target = uid
                    reportTarget = null
                    scope.launch {
                        val ok = onReport(target, reason, details)
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
                    TextButton(onClick = { blockTarget = null; onBlock(uid) }) {
                        Text(s.block, color = MaterialTheme.colorScheme.error)
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
                    TextButton(onClick = { unblockTarget = null; onUnblock(uid) }) {
                        Text(s.unblock)
                    }
                },
                dismissButton = { TextButton(onClick = { unblockTarget = null }) { Text(s.cancel) } },
            )
        }
    }
}

@Composable
private fun ReportDialog(
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
                Text(s.reportSubmit, color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(s.cancel) } },
    )
}

@Composable
private fun InviteDialog(
    trip: Trip,
    onCreateInvite: suspend (String) -> String?,
    onDismiss: () -> Unit,
) {
    val s = LocalStrings.current
    val clipboard = LocalClipboardManager.current
    val sharer = rememberFileSharer()

    var code by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(true) }
    var failed by remember { mutableStateOf(false) }

    LaunchedEffect(trip.id) {
        loading = true; failed = false
        val result = onCreateInvite(trip.id)
        if (result != null) code = result else failed = true
        loading = false
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(s.inviteToTrip) },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text(
                    trip.title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                )
                Spacer(Modifier.height(16.dp))
                when {
                    loading -> { PlaneLoader(size = 72.dp); Spacer(Modifier.height(12.dp)) }
                    failed || code == null -> {
                        Text(s.inviteFailed, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error)
                    }
                    else -> {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            tonalElevation = 2.dp,
                        ) {
                            Text(
                                code!!,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                            )
                        }
                        Spacer(Modifier.height(12.dp))
                        Text(
                            s.inviteCodeHint,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            modifier = Modifier.padding(horizontal = 8.dp),
                        )
                    }
                }
            }
        },
        confirmButton = {
            if (!loading && code != null) {
                TextButton(onClick = { sharer.shareText("${s.inviteShareMessage} ${trip.title}\n\n${code!!}") }) {
                    Icon(Icons.Filled.Share, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(s.share)
                }
            }
        },
        dismissButton = {
            if (!loading && code != null) {
                TextButton(onClick = { clipboard.setText(AnnotatedString(code!!)) }) {
                    Icon(Icons.Filled.ContentCopy, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(s.copy)
                }
            } else {
                TextButton(onClick = onDismiss) { Text(s.cancel) }
            }
        },
    )
}

@Composable
private fun MemberRow(
    name: String,
    email: String,
    role: String,
    canEdit: Boolean,
    canModerate: Boolean,
    isBlocked: Boolean,
    onSetRole: (String) -> Unit,
    onRemove: () -> Unit,
    onReport: () -> Unit,
    onBlock: () -> Unit,
    onUnblock: () -> Unit,
) {
    val s = LocalStrings.current
    var menuOpen by remember { mutableStateOf(false) }
    var moderateOpen by remember { mutableStateOf(false) }
    var confirmRemove by remember { mutableStateOf(false) }

    val roleLabel = when (role) {
        "owner" -> s.roleOwner
        "editor" -> s.roleEditor
        else -> s.roleViewer
    }

    Row(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Avatar circle with initial
        Box(
            Modifier.size(42.dp).clip(CircleShape)
                .background(MaterialTheme.colorScheme.secondaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                name.firstOrNull()?.uppercase() ?: "?",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
            )
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
            if (email.isNotBlank()) {
                Text(
                    email,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                )
            }
        }

        if (canEdit) {
            // Role chip that opens a menu
            Box {
                AssistChip(
                    onClick = { menuOpen = true },
                    label = { Text(roleLabel) },
                )
                val shape: Shape = RoundedCornerShape(16.dp)
                DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }, shape = shape) {
                    DropdownMenuItem(
                        text = { Text(s.roleEditor) },
                        trailingIcon = { if (role == "editor") Icon(Icons.Filled.Check, null) },
                        onClick = { menuOpen = false; onSetRole("editor") },
                    )
                    DropdownMenuItem(
                        text = { Text(s.roleViewer) },
                        trailingIcon = { if (role == "viewer") Icon(Icons.Filled.Check, null) },
                        onClick = { menuOpen = false; onSetRole("viewer") },
                    )
                    HorizontalDivider()
                    DropdownMenuItem(
                        text = { Text(s.removeMember, color = MaterialTheme.colorScheme.error) },
                        leadingIcon = {
                            Icon(Icons.Filled.RemoveCircleOutline, null, tint = MaterialTheme.colorScheme.error)
                        },
                        onClick = { menuOpen = false; confirmRemove = true },
                    )
                }
            }
        } else {
            // Read-only role label
            Text(
                roleLabel,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            )
        }

        // Moderation overflow menu: report / block any member but yourself.
        if (canModerate) {
            Box {
                IconButton(onClick = { moderateOpen = true }) {
                    Icon(Icons.Filled.MoreVert, contentDescription = s.report)
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
                            leadingIcon = {
                                Icon(Icons.Filled.Block, null, tint = MaterialTheme.colorScheme.error)
                            },
                            onClick = { moderateOpen = false; onBlock() },
                        )
                    }
                }
            }
        }
    }

    if (confirmRemove) {
        AlertDialog(
            onDismissRequest = { confirmRemove = false },
            title = { Text(s.removeMemberQ) },
            text = { Text(s.removeMemberDesc) },
            confirmButton = {
                TextButton(onClick = { confirmRemove = false; onRemove() }) {
                    Text(s.remove, color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = { confirmRemove = false }) { Text(s.cancel) } },
        )
    }
}