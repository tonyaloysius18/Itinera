package com.itinera.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.itinera.app.data.RememberedAccount
import com.itinera.app.i18n.LocalStrings
import com.itinera.app.model.UserProfile
import com.itinera.app.ui.components.TopBar


private val AccentRed = Color(0xFFE03131)

@Composable
fun AccountScreen(
    profile: UserProfile,
    accounts: List<RememberedAccount> = emptyList(),
    currentUid: String = "",
    onSwitchAccount: (RememberedAccount) -> Unit = {},
    onForgetAccount: (RememberedAccount) -> Unit = {},
    onAddAccount: () -> Unit,
    onLogOut: () -> Unit,
    onDeleteAccount: () -> Unit,
    onBack: () -> Unit,
) {
    val s = LocalStrings.current
    var showDeleteDialog by remember { mutableStateOf(false) }
    var pendingForget by remember { mutableStateOf<RememberedAccount?>(null) }

    // ⬅ FIX — the active row rendered `profile` unconditionally. After the
    // account is deleted, profile resets to UserProfile() while this screen is
    // still on-screen, so you got a card with an avatar and two blank lines.
    val hasActiveAccount = currentUid.isNotBlank() &&
            (profile.fullName.isNotBlank() || profile.email.isNotBlank())

    // ⬅ FIX — also drop the current account from the "other" list, dedupe by
    // uid, and skip entries with nothing to show. A deleted account can linger
    // in the remembered list until AccountStore catches up.
    val otherAccounts = remember(accounts, currentUid) {
        accounts
            .filter { it.uid != currentUid && it.uid.isNotBlank() }
            .filter { it.name.isNotBlank() || it.email.isNotBlank() }
            .distinctBy { it.uid }
    }

    Column(Modifier.fillMaxSize()) {
        TopBar(title = s.account, onBack = onBack)

        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
        ) {
            if (hasActiveAccount) {
                SectionLabel(s.signedInAs)
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 2.dp,
                ) {
                    Row(
                        Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        ProfileAvatar(profile, size = 46.dp)
                        Spacer(Modifier.width(14.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                profile.fullName.ifBlank { profile.email },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            if (profile.email.isNotBlank()) {
                                Text(
                                    profile.email,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }
                        Box(
                            Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                Icons.Filled.Check,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(16.dp),
                            )
                        }
                    }
                }
            }

            // ⬅ CHANGED — was one card mixing the active account, the others and
            // "Add another". Switching accounts and adding one are different
            // actions, and the others were only removable by long-press, which
            // nothing indicated.
            if (otherAccounts.isNotEmpty()) {
                SectionLabel(s.otherAccounts, hint = s.tapToSwitch)
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 2.dp,
                ) {
                    Column {
                        otherAccounts.forEachIndexed { index, account ->
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .clickable { onSwitchAccount(account) }
                                    .padding(start = 16.dp, end = 8.dp, top = 12.dp, bottom = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Box(
                                    Modifier.size(40.dp).clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.secondaryContainer),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    if (account.photoUrl.isNotBlank()) {
                                        AsyncImage(
                                            model = account.photoUrl,
                                            contentDescription = null,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.matchParentSize(),
                                        )
                                    } else {
                                        Text(
                                            (account.name.firstOrNull()
                                                ?: account.email.firstOrNull() ?: '?').uppercase(),
                                            style = MaterialTheme.typography.titleMedium,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                                        )
                                    }
                                }
                                Spacer(Modifier.width(14.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        account.name.ifBlank { account.email },
                                        style = MaterialTheme.typography.bodyLarge,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                    if (account.email.isNotBlank() && account.name.isNotBlank()) {
                                        Text(
                                            account.email,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                        )
                                    }
                                }
                                // ⬅ ADD — removing was long-press only, with nothing
                                // saying so.
                                Box(
                                    Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .clickable { pendingForget = account },
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(
                                        Icons.Filled.Close,
                                        contentDescription = s.removeAccount,
                                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                                        modifier = Modifier.size(17.dp),
                                    )
                                }
                            }
                            if (index < otherAccounts.lastIndex) ThinDivider()
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            Surface(
                modifier = Modifier.fillMaxWidth().clickable(onClick = onAddAccount),
                shape = RoundedCornerShape(16.dp),
                color = Color.Transparent,
                border = androidx.compose.foundation.BorderStroke(
                    0.5.dp,
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.45f),
                ),
            ) {
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        Icons.Filled.PersonAdd,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(Modifier.width(14.dp))
                    Text(s.addAnotherAccount, style = MaterialTheme.typography.bodyLarge)
                }
            }

            Spacer(Modifier.height(32.dp))

            // ⬅ CHANGED — both actions were full-width solid red, which made
            // logging out look as consequential as deleting everything. Log out
            // is routine and reversible; only one of these deserves the colour.
            Surface(
                modifier = Modifier.fillMaxWidth().clickable(onClick = onLogOut),
                shape = RoundedCornerShape(14.dp),
                color = Color.Transparent,
                border = androidx.compose.foundation.BorderStroke(
                    0.5.dp,
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                ),
            ) {
                Row(
                    Modifier.fillMaxWidth().padding(vertical = 14.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.Logout,
                        contentDescription = null,
                        modifier = Modifier.size(19.dp),
                    )
                    Spacer(Modifier.width(9.dp))
                    Text(s.logOut, fontWeight = FontWeight.Medium)
                }
            }

            Spacer(Modifier.height(12.dp))

            Row(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .clickable { showDeleteDialog = true }
                    .padding(vertical = 14.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    Icons.Filled.DeleteForever,
                    contentDescription = null,
                    tint = AccentRed,
                    modifier = Modifier.size(19.dp),
                )
                Spacer(Modifier.width(9.dp))
                Text(s.deleteAccount, color = AccentRed, fontWeight = FontWeight.Medium)
            }

            Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.safeDrawing))
            Spacer(Modifier.height(24.dp))
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            icon = {
                Icon(Icons.Filled.DeleteForever, contentDescription = null, tint = AccentRed)
            },
            title = { Text(s.deleteAccount) },
            // ⬅ CHANGED — spells out what goes. "Are you sure?" doesn't say that
            // the trips and everything shared with travellers go too.
            text = { Text(s.deleteAccountLong) },
            shape = RoundedCornerShape(20.dp),
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    onDeleteAccount()
                }) { Text(s.delete, color = AccentRed) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text(s.cancel) }
            },
        )
    }

    pendingForget?.let { account ->
        AlertDialog(
            onDismissRequest = { pendingForget = null },
            title = { Text(s.removeAccount) },
            text = {
                Column {
                    Text(s.removeAccountConfirm)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        account.email.ifBlank { account.name },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    )
                }
            },
            shape = RoundedCornerShape(20.dp),
            confirmButton = {
                TextButton(onClick = {
                    // Clear the dialog first: if onForgetAccount throws, the old
                    // order left it stuck open with no way out.
                    pendingForget = null
                    onForgetAccount(account)
                }) { Text(s.remove, color = AccentRed) }
            },
            dismissButton = { TextButton(onClick = { pendingForget = null }) { Text(s.cancel) } },
        )
    }
}

@Composable
private fun SectionLabel(label: String, hint: String = "") {
    Row(
        Modifier.fillMaxWidth().padding(start = 4.dp, end = 4.dp, top = 18.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            modifier = Modifier.weight(1f),
        )
        if (hint.isNotBlank()) {
            Text(
                hint,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
            )
        }
    }
}

@Composable
private fun ThinDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(start = 70.dp),
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
    )
}