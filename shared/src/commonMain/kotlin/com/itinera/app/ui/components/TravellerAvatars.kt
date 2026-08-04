package com.itinera.app.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import com.itinera.app.resources.Res
import com.itinera.app.resources.avatar_adventurer
import com.itinera.app.resources.avatar_backpacker
import com.itinera.app.resources.avatar_beachgoer
import com.itinera.app.resources.avatar_birdwatcher
import com.itinera.app.resources.avatar_explorer
import com.itinera.app.resources.avatar_hiker
import com.itinera.app.resources.avatar_navigator
import com.itinera.app.resources.avatar_photographer
import com.itinera.app.resources.avatar_planner
import com.itinera.app.resources.avatar_wanderer
import com.itinera.app.resources.avatar_braided
import com.itinera.app.resources.avatar_curious
import com.itinera.app.resources.avatar_curly
import com.itinera.app.resources.avatar_searched
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

enum class TravellerAvatar(val id: String, val resource: DrawableResource) {
    BACKPACKER("backpacker", Res.drawable.avatar_backpacker),
    EXPLORER("explorer", Res.drawable.avatar_explorer),
    NAVIGATOR("navigator", Res.drawable.avatar_navigator),
    WANDERER("wanderer", Res.drawable.avatar_wanderer),
    BIRDWATCHER("birdwatcher", Res.drawable.avatar_birdwatcher),
    ADVENTURER("adventurer", Res.drawable.avatar_adventurer),
    PLANNER("planner", Res.drawable.avatar_planner),
    HIKER("hiker", Res.drawable.avatar_hiker),
    BEACHGOER("beachgoer", Res.drawable.avatar_beachgoer),
    PHOTOGRAPHER("photographer", Res.drawable.avatar_photographer),
    BRAIDED("braided", Res.drawable.avatar_braided),
    CURIOUS("curious", Res.drawable.avatar_curious),
    CURLY("curly", Res.drawable.avatar_curly),
    SEARCHED("searched", Res.drawable.avatar_searched);


    companion object {
        /** Null for an unknown or empty id, so the row falls back to initials. */
        fun fromId(id: String?): TravellerAvatar? =
            entries.firstOrNull { it.id == id }
    }
}

@Composable
fun TravellerAvatar.painter(): Painter = painterResource(resource)