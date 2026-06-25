package com.itinera.app.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember

/** The set of destinations in the app. Parameters carry the needed id. */
sealed interface Screen {
    data object Login : Screen

    data object CreateAccount : Screen

    data object Currency : Screen
    data object Home : Screen

    data object EditProfile : Screen

    data object Appearance : Screen

    data class TripDetail(val tripId: String) : Screen
    data class AddLeg(val tripId: String) : Screen

    data class EditLeg(val tripId: String, val legId: String) : Screen

    data class EditPlace(val tripId: String, val activityId: String) : Screen

    data class AddPlace(val tripId: String) : Screen

    data class TripDocuments(val tripId: String) : Screen
    data class DocViewer(val docId: String) : Screen

    data class Members(val tripId: String) : Screen

    data object Split : Screen

    data class TripExpenses(val tripId: String) : Screen
    data class AddExpense(val tripId: String, val expenseId: String? = null) : Screen
    data class Travellers(val tripId: String) : Screen
    data class Checklist(val tripId: String) : Screen
    data object Calendar : Screen
    data object Settings : Screen

    data object Account : Screen

    data object Notifications : Screen

    data object BackupStatus : Screen

    data object Help : Screen

    data object About : Screen

    data object ArchivedTrips : Screen
    data object LanguagePicker : Screen

    data object ExportTrips : Screen

}

/**
 * Minimal back-stack navigator held in Compose state. Hand-rolled on purpose so
 * the skeleton builds with no navigation library to version-match. Swap for
 * Compose Navigation Multiplatform later if you want deep links / transitions.
 */
class Navigator(start: Screen) {
    private val stack = mutableStateListOf(start)
    val current: Screen get() = stack.last()

    fun push(screen: Screen) { stack.add(screen) }
    fun replace(screen: Screen) { stack[stack.lastIndex] = screen }

    /** Reset the whole stack to a single root (used for login / log out). */
    fun resetTo(screen: Screen) {
        stack.clear()
        stack.add(screen)
    }

    fun back(): Boolean {
        if (stack.size <= 1) return false
        stack.removeAt(stack.lastIndex)
        return true
    }
}

@Composable
fun rememberNavigator(start: Screen): Navigator = remember { Navigator(start) }
