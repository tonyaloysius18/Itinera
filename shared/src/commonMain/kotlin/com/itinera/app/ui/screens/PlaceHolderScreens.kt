package com.itinera.app.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.itinera.app.i18n.LocalStrings
import com.itinera.app.ui.components.EmptyState
import com.itinera.app.ui.components.TopBar

@Composable
fun NotificationsScreen(onBack: () -> Unit) {
    val s = LocalStrings.current
    Column(Modifier.fillMaxSize()) {
        TopBar(title = s.notifications, onBack = onBack)
        EmptyState(
            icon = Icons.Filled.Notifications,
            title = s.comingSoon,
            subtitle = s.notificationsSoonSubtitle,
            modifier = Modifier.fillMaxSize(),
        )
    }
}



