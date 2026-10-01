package com.itinera.app.data

import com.russhwolf.settings.Settings
import com.russhwolf.settings.set

/**
 * Device-local "New" badges for features introduced in a release. A badge shows until the user opens the feature it
 * points at, then never again on that device. To announce a later feature, add a new id here; to re-announce a
 * reworked one, bump its suffix (e.g. discover_templates_v2).
 */
object FeatureBadges {
    const val DISCOVER_TEMPLATES = "discover_templates_v1"

    private val settings: Settings = Settings()
    private fun key(id: String) = "feature_badge_seen_$id"

    /** True until [markSeen] has been called for [id] on this device. */
    fun isNew(id: String): Boolean = !settings.getBoolean(key(id), false)

    fun markSeen(id: String) {
        settings[key(id)] = true
    }
}
