package com.itinera.app.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsTransit
import androidx.compose.material.icons.filled.Hotel
import androidx.compose.material.icons.filled.LocalActivity
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.itinera.app.i18n.LocalStrings
import com.itinera.app.model.ExpenseCategory

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
 * Mid-saturation values, chosen to hold contrast against both `surface` and
 * `surfaceVariant` in light *and* dark.
 *
 * Deliberately not pulled from `colorScheme`: these are categorical, and
 * Material's roles carry semantics (error, primary) that misread when applied
 * to "food" or "transport".
 */
val ExpenseCategory.color: Color
    get() = when (this) {
        ExpenseCategory.ACCOMMODATION -> Color(0xFF7F77DD)
        ExpenseCategory.FOOD -> Color(0xFF1D9E75)
        ExpenseCategory.TRANSPORT -> Color(0xFFBA7517)
        ExpenseCategory.SHOPPING -> Color(0xFFD4537E)
        ExpenseCategory.ACTIVITIES -> Color(0xFFD85A30)
        ExpenseCategory.OTHER -> Color(0xFF888780)
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