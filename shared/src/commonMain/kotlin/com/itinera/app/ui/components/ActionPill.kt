package com.itinera.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.itinera.app.ui.theme.itinera

/**
 * A pill-shaped action button: an icon and a label in one colour, on a light tint of that colour with a matching
 * outline. Used for the row actions on the Archived and Recently deleted screens (and the same look as the Undo pill).
 */
@Composable
fun ActionPill(
    label: String,
    icon: ImageVector,
    tint: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    /** Optional small "New"-style chip straddling the pill's top-right corner (e.g. to flag a feature added in this release). */
    badge: String? = null,
) {
    val dark = MaterialTheme.itinera.isDark
    // The badge straddles the pill's top-right corner (half in, half out), so it sits in a Box over the pill.
    Box(modifier = modifier) {
        Surface(
            onClick = onClick,
            shape = RoundedCornerShape(50),
            color = tint.copy(alpha = if (dark) 0.20f else 0.10f),
            border = BorderStroke(1.dp, tint.copy(alpha = 0.45f)),
        ) {
            Row(
                Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(18.dp))
                Text(label, color = tint, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold, maxLines = 1)
            }
        }
        if (badge != null) {
            Surface(
                shape = RoundedCornerShape(50),
                color = tint,
                border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.background),
                modifier = Modifier.align(Alignment.TopEnd).offset(x = 8.dp, y = (-9).dp),
            ) {
                Text(
                    badge,
                    color = MaterialTheme.colorScheme.onPrimary,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                )
            }
        }
    }
}
