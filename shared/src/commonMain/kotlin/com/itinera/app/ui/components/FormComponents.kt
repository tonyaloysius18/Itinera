package com.itinera.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

/*
 * Shared form pieces for AddLegScreen and AddPlaceScreen.
 *
 * These started as private composables inside AddLegScreen.kt. Kotlin's
 * top-level `private` is file-scoped, so AddPlaceScreen couldn't see them —
 * hence this file. DELETE the private copies of SectionLabel, PlainTextInput,
 * PickerRow and InlineTimeSlot from AddLegScreen.kt when you drop this in.
 */

@Composable
internal fun SectionLabel(label: String) {
    Text(
        label.uppercase(),
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
        modifier = Modifier.padding(start = 2.dp, top = 22.dp, bottom = 8.dp),
    )
}

/** Borderless input, so fields inside a card don't each draw their own outline. */
@Composable
internal fun PlainTextInput(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    textStyle: TextStyle,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true,
) {
    val onSurface = MaterialTheme.colorScheme.onSurface
    Box(modifier) {
        if (value.isEmpty()) {
            Text(placeholder, style = textStyle, color = onSurface.copy(alpha = 0.4f))
        }
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = singleLine,
            textStyle = textStyle.copy(color = onSurface),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

/**
 * Read-only row that opens a picker.
 *
 * Replaces the OutlinedTextField + `enabled = false` + outer `.clickable`
 * pattern, which needed a four-line disabled-colours block at every call site
 * and made screen readers announce the fields as disabled.
 */
@Composable
internal fun PickerRow(
    icon: ImageVector,
    value: String,
    placeholder: String,
    onClick: () -> Unit,
    trailingHint: String = "",
) {
    val onSurface = MaterialTheme.colorScheme.onSurface
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = onSurface.copy(alpha = 0.55f), modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(16.dp))
        Text(
            value.ifBlank { placeholder },
            style = MaterialTheme.typography.titleMedium,
            color = if (value.isBlank()) onSurface.copy(alpha = 0.4f) else onSurface,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        if (trailingHint.isNotBlank()) {
            Text(
                trailingHint,
                style = MaterialTheme.typography.labelMedium,
                color = onSurface.copy(alpha = 0.35f),
            )
            Spacer(Modifier.width(8.dp))
        }
        Icon(
            Icons.Filled.ChevronRight,
            contentDescription = null,
            tint = onSurface.copy(alpha = 0.3f),
            modifier = Modifier.size(20.dp),
        )
    }
}

/** Half-width time slot inside a card. */
@Composable
internal fun InlineTimeSlot(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    emptyText: String = "—",
    onClick: () -> Unit,
) {
    val onSurface = MaterialTheme.colorScheme.onSurface
    Row(
        modifier.clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (leadingIcon != null) {
            Icon(leadingIcon, contentDescription = null, tint = onSurface.copy(alpha = 0.55f), modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(16.dp))
        }
        Column {
            Text(label, style = MaterialTheme.typography.labelMedium, color = onSurface.copy(alpha = 0.5f))
            Spacer(Modifier.height(1.dp))
            Text(
                value.ifBlank { emptyText },
                style = MaterialTheme.typography.titleMedium,
                color = if (value.isBlank()) onSurface.copy(alpha = 0.4f) else onSurface,
            )
        }
    }
}
