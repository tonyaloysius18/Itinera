package com.itinera.app.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsTransit
import androidx.compose.material.icons.filled.Hotel
import androidx.compose.material.icons.filled.LocalActivity
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.itinera.app.i18n.LocalStrings
import com.itinera.app.model.ExpenseCategory
import com.itinera.app.ui.theme.itinera

/**
 * Category presentation lives here, not in Models.kt, so the model package stays
 * free of Compose imports — the same separation you already keep for TripAccent.
 *
 * ── Dependency note ──────────────────────────────────────────────────────────
 * The icons below come from the extended set. Add to commonMain:
 *
 *     implementation(compose.materialIconsExtended)
 *
 * Core-only swaps if you'd rather not take that artifact:
 *   Hotel             -> Icons.Filled.Home
 *   Restaurant        -> Icons.Filled.Favorite
 *   DirectionsTransit -> Icons.Filled.Send
 *   ShoppingBag       -> Icons.Filled.ShoppingCart
 *   LocalActivity     -> Icons.Filled.Star
 *   MoreHoriz         -> Icons.Filled.MoreVert
 * The colour coding carries the meaning either way; icons are reinforcement.
 */

/**
 * Categorical, so deliberately not pulled from `colorScheme`: Material's roles
 * carry semantics (error, primary) that misread when applied to "food" or
 * "transport". They resolve through [itinera] instead, which holds a hue-matched
 * variant per mode — each lightened just far enough to clear 4.5:1 on the dark
 * surface without drifting off its hue.
 */
val ExpenseCategory.color: Color
    @Composable get() = when (this) {
        ExpenseCategory.ACCOMMODATION -> MaterialTheme.itinera.categoryAccommodation
        ExpenseCategory.FOOD -> MaterialTheme.itinera.categoryFood
        ExpenseCategory.TRANSPORT -> MaterialTheme.itinera.categoryTransport
        ExpenseCategory.SHOPPING -> MaterialTheme.itinera.categoryShopping
        ExpenseCategory.ACTIVITIES -> MaterialTheme.itinera.categoryActivities
        ExpenseCategory.OTHER -> MaterialTheme.itinera.categoryOther
    }

val ExpenseCategory.icon: ImageVector
    get() = when (this) {
        ExpenseCategory.ACCOMMODATION -> Icons.Filled.Hotel
        ExpenseCategory.FOOD -> Icons.Filled.Restaurant
        ExpenseCategory.TRANSPORT -> Icons.Filled.DirectionsTransit
        ExpenseCategory.SHOPPING -> Icons.Filled.ShoppingBag
        ExpenseCategory.ACTIVITIES -> Icons.Filled.LocalActivity
        ExpenseCategory.OTHER -> Icons.Filled.MoreHoriz
    }

@Composable
fun ExpenseCategory.label(): String {
    val s = LocalStrings.current
    return when (this) {
        ExpenseCategory.ACCOMMODATION -> s.catAccommodation
        ExpenseCategory.FOOD -> s.catFood
        ExpenseCategory.TRANSPORT -> s.catTransport
        ExpenseCategory.SHOPPING -> s.catShopping
        ExpenseCategory.ACTIVITIES -> s.catActivities
        ExpenseCategory.OTHER -> s.catOther
    }
}