package com.itinera.app.data

import com.russhwolf.settings.Settings
import com.russhwolf.settings.set
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/** A device-local record of an account the user has signed into before. */
@Serializable
data class RememberedAccount(
    val uid: String = "",
    val email: String = "",
    val name: String = "",
    val photoUrl: String = "",
    val method: String = "password",   // "google" | "password"
)

/**
 * Device-local store of the accounts that have signed in on this device, so the
 * user can see and switch between them. This is NOT tied to any one user's cloud
 * data — it spans accounts and lives only on the device (multiplatform-settings:
 * SharedPreferences on Android, NSUserDefaults on iOS).
 *
 * Note: stores only display info (name/email/photo/method) + uid. No passwords or
 * tokens are stored; switching to an account still requires authentication.
 */
class AccountStore {

    private val settings: Settings = Settings()
    private val json = Json { ignoreUnknownKeys = true }
    private val key = "remembered_accounts"

    /** All remembered accounts (most-recently-used first is not guaranteed; insertion order). */
    fun all(): List<RememberedAccount> {
        val raw = settings.getStringOrNull(key) ?: return emptyList()
        return try {
            json.decodeFromString<List<RememberedAccount>>(raw)
        } catch (e: Exception) {
            emptyList()
        }
    }

    /** Add or update an account (keyed by uid). Call after every successful sign-in. */
    fun remember(account: RememberedAccount) {
        if (account.uid.isBlank()) return
        val current = all().filterNot { it.uid == account.uid }
        val updated = current + account
        settings[key] = json.encodeToString(updated)
    }

    /** Remove an account from the remembered list (e.g. on full sign-out/forget). */
    fun forget(uid: String) {
        val updated = all().filterNot { it.uid == uid }
        settings[key] = json.encodeToString(updated)
    }

    /** Clear all remembered accounts. */
    fun clear() {
        settings.remove(key)
    }


}