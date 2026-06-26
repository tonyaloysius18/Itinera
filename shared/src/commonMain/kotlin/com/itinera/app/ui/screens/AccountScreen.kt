package com.itinera.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.clickable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.itinera.app.i18n.LocalStrings
import com.itinera.app.model.UserProfile
import com.itinera.app.ui.components.TopBar
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage

private val AccentRed = Color(0xFFE03131)

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun AccountScreen(
    profile: UserProfile,
    accounts: List<com.itinera.app.data.RememberedAccount> = emptyList(),
    currentUid: String = "",
    onSwitchAccount: (com.itinera.app.data.RememberedAccount) -> Unit = {},
    onForgetAccount: (com.itinera.app.data.RememberedAccount) -> Unit = {},
    onAddAccount: () -> Unit,
    onLogOut: () -> Unit,
    onDeleteAccount: () -> Unit,
    onBack: () -> Unit,          // ⬅ ADD
) {
    val s = LocalStrings.current
    val primary = MaterialTheme.colorScheme.primary
    var showDeleteDialog by remember { mutableStateOf(false) }

    var pendingForget by remember { mutableStateOf<com.itinera.app.data.RememberedAccount?>(null) }

    Column(Modifier.fillMaxSize()) {
        TopBar(title = s.account, onBack = onBack)

        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
        ) {
            Spacer(Modifier.height(8.dp))
            Text(
                s.signedInAs,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                modifier = Modifier.padding(start = 4.dp, bottom = 8.dp),
            )

            // Accounts list. For now this is just the active account.
            // PHASE 2: render every remembered account here and switch on tap.
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
            ) {
                Column {
                    AccountRow(profile = profile, active = true)

                    // Other remembered accounts on this device — tap to switch
                    accounts.filter { it.uid != currentUid }.forEach { account ->
                        ThinDivider()
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .combinedClickable(
                                    onClick = { onSwitchAccount(account) },
                                    onLongClick = { pendingForget = account },
                                )
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            // avatar: initial in a circle
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
                                        (account.name.firstOrNull() ?: account.email.firstOrNull() ?: '?').uppercase(),
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
                                    fontWeight = FontWeight.Medium,
                                )
                                if (account.email.isNotBlank()) {
                                    Text(
                                        account.email,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                    )
                                }
                            }
                        }
                    }

                    ThinDivider()
                    // Add another account
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clickable(onClick = onAddAccount)
                            .padding(horizontal = 16.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(Icons.Filled.PersonAdd, null, tint = primary, modifier = Modifier.size(22.dp))
                        Spacer(Modifier.width(14.dp))
                        Text(s.addAnotherAccount, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }

            Spacer(Modifier.height(28.dp))

            // Log out
            Button(
                onClick = onLogOut,
                modifier = Modifier.fillMaxWidth().height(52.dp).padding(horizontal = 45
                    .dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AccentRed, contentColor = Color.White),
            ) {
                Icon(Icons.AutoMirrored.Filled.Logout, null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text(s.logOut, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            }

            Spacer(Modifier.height(12.dp))

            // Delete account
            Button(
                onClick = { showDeleteDialog = true },
                modifier = Modifier.fillMaxWidth().height(52.dp).padding(horizontal = 45.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AccentRed, contentColor = Color.White),
            ) {
                Text(s.deleteAccount, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            }

            Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.safeDrawing))
            Spacer(Modifier.height(24.dp))
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(s.deleteAccount) },
            text = { Text(s.deleteAccountConfirm) },
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
    if (pendingForget != null) {
        AlertDialog(
            onDismissRequest = { pendingForget = null },
            title = { Text(s.removeAccount) },
            text = { Text(s.removeAccountConfirm) },
            confirmButton = {
                TextButton(onClick = {
                    onForgetAccount(pendingForget!!)
                    pendingForget = null
                }) { Text(s.remove, color = AccentRed) }
            },
            dismissButton = { TextButton(onClick = { pendingForget = null }) { Text(s.cancel) } },
        )
    }
}

@Composable
private fun AccountRow(profile: UserProfile, active: Boolean) {
    Row(
        Modifier.fillMaxWidth().padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ProfileAvatar(profile, size = 44.dp)
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(profile.fullName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(
                profile.email,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            )
        }
        if (active) {
            Icon(Icons.Filled.Check, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
        }
    }
}

@Composable
private fun ThinDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(start = 52.dp),
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
    )
}