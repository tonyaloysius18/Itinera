package com.itinera.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.text.style.TextOverflow
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
    /** Optional small "New"-style chip shown inside the pill after the label (e.g. to flag a feature added in this release). */
    badge: String? = null,
) {
    val dark = MaterialTheme.itinera.isDark
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(50),
        color = tint.copy(alpha = if (dark) 0.20f else 0.10f),
        border = BorderStroke(1.dp, tint.copy(alpha = 0.45f)),
    ) {
        Row(
            // A badge makes the pill wider, so the padding, gap and icon tighten a little to keep the label readable.
            Modifier.padding(horizontal = if (badge != null) 10.dp else 14.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(if (badge != null) 4.dp else 6.dp),
        ) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(if (badge != null) 16.dp else 18.dp))
            // With a badge the pill is wider; the label gives way (smaller, then ellipsis) before the badge is squeezed.
            Text(
                label,
                color = tint,
                style = if (badge != null) MaterialTheme.typography.labelMedium else MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false),
            )
            if (badge != null) {
                Surface(shape = RoundedCornerShape(50), color = tint) {
                    Text(
                        badge,
                        color = MaterialTheme.colorScheme.onPrimary,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        softWrap = false,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                    )
                }
            }
        }
    }
}
