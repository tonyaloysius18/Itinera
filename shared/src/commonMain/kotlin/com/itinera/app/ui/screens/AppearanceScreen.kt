package com.itinera.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brightness6
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.itinera.app.i18n.LocalStrings
import com.itinera.app.ui.components.TopBar
import com.itinera.app.ui.theme.ThemeMode   // adjust import to where you put the enum

@Composable
fun AppearanceScreen(
    selected: ThemeMode,
    onSelect: (ThemeMode) -> Unit,
    onBack: () -> Unit,
) {
    val s = LocalStrings.current
    Column(Modifier.fillMaxSize().padding(horizontal = 16.dp)) {

        TopBar(s.appearance, onBack = onBack, modifier = Modifier.offset(x = (-15).dp))
        Spacer(Modifier.height(8.dp))
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp,
        ) {
            Column {
                AppearanceRow(Icons.Filled.Brightness6, s.systemDefault, s.matchYourPhone, selected == ThemeMode.SYSTEM) { onSelect(ThemeMode.SYSTEM) }
                HorizontalDivider(Modifier.padding(start = 56.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                AppearanceRow(Icons.Filled.LightMode, s.light, s.alwaysLight, selected == ThemeMode.LIGHT) { onSelect(ThemeMode.LIGHT) }
                HorizontalDivider(Modifier.padding(start = 56.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                AppearanceRow(Icons.Filled.DarkMode, s.dark, s.alwaysDark, selected == ThemeMode.DARK) { onSelect(ThemeMode.DARK) }
            }
        }
    }
}

@Composable
private fun AppearanceRow(icon: ImageVector, title: String, subtitle: String, selected: Boolean, onClick: () -> Unit) {
    val primary = MaterialTheme.colorScheme.primary
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, null, tint = primary, modifier = Modifier.size(24.dp))
        Spacer(Modifier.width(16.dp))
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
        }
        if (selected) Icon(Icons.Filled.CheckCircle, null, tint = primary)
        else Icon(Icons.Outlined.Circle, null, tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f))
    }
}