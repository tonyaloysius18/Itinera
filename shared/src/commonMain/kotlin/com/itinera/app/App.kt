package com.itinera.app

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.itinera.app.data.TripRepository
import com.itinera.app.i18n.Language
import com.itinera.app.i18n.LocalStrings
import com.itinera.app.i18n.stringsFor
import com.itinera.app.ui.Navigator
import com.itinera.app.ui.Screen
import com.itinera.app.ui.rememberNavigator
import com.itinera.app.ui.screens.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.lerp
import com.itinera.app.data.imageQueryForTrip
import com.itinera.app.ui.theme.ThemeMode
import kotlin.math.abs
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch

/**
 * App root. Owns the three pieces of global state — auth flag, chosen language,
 * and the repository — and routes the current screen. Changing the language swaps
 * the value provided to LocalStrings, which recomposes every screen instantly.
 */
@Composable
fun App() {
    val repository = remember { TripRepository() }
    var language by remember { mutableStateOf(Language.ENGLISH) }
    var themeMode by remember { mutableStateOf(ThemeMode.SYSTEM) }
    val navigator = rememberNavigator(Screen.Login)

    // SYSTEM falls back to English here; in production resolve the device locale
    // via an expect/actual platform call and map it to a Language.
    val activeStrings = remember(language) {
        stringsFor(if (language == Language.SYSTEM) Language.ENGLISH else language)
    }

    val darkTheme = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    val scope = rememberCoroutineScope()

    ItineraTheme(darkTheme = darkTheme) {
        CompositionLocalProvider(LocalStrings provides activeStrings) {
            AppContent(repository, navigator, language, themeMode, { language = it }, { themeMode = it })
        }
    }
}

private data class NavItem(
    val icon: ImageVector,
    val label: String,
    val screen: Screen,
)
private val topLevel = setOf(Screen.Home, Screen.Documents, Screen.Calendar, Screen.Currency, Screen.Settings)

@Composable
private fun AppContent(
    repository: TripRepository,
    navigator: Navigator,
    language: Language,
    themeMode: ThemeMode,
    onLanguageChange: (Language) -> Unit,
    onThemeChange: (ThemeMode) -> Unit,
) {
    val s = LocalStrings.current
    val current = navigator.current

    val scope = rememberCoroutineScope()

    if (current == Screen.Login) {
        LoginScreen(
            onAuthed = { navigator.resetTo(Screen.Home) },
            onCreateAccount = { navigator.push(Screen.CreateAccount) },
        )
        return
    }
    if (current == Screen.LanguagePicker) {
        LanguageScreen(
            selected = language,
            onSelect = onLanguageChange,
            onBack = { navigator.back() },
        )
        return
    }

    val showBottomBar = current in topLevel

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            if (showBottomBar) {
                val items = listOf(
                    NavItem(Icons.Filled.Flight, s.myTrips, Screen.Home),
                    NavItem(Icons.Filled.Description, s.documents, Screen.Documents),
                    NavItem(Icons.Filled.CalendarMonth, s.calendar, Screen.Calendar),
                    NavItem(Icons.Filled.CurrencyExchange, s.currencyUnits, Screen.Currency),
                    NavItem(Icons.Filled.Settings, s.settings, Screen.Settings),
                )
                Box(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    SlidingPillBar(current = current, items = items) { navigator.resetTo(it) }
                }
            }
        },
    ) { padding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .statusBarsPadding()
                .navigationBarsPadding(),
            color = MaterialTheme.colorScheme.background
        ) {
            when (val screen = current) {
                Screen.Login -> LoginScreen(
                    onAuthed = { navigator.resetTo(Screen.Home) },
                    onCreateAccount = { navigator.push(Screen.CreateAccount) },
                )

                Screen.CreateAccount -> CreateAccountScreen(
                    onBack = { navigator.back() },
                    onCreate = { navigator.resetTo(Screen.Login) },
                )

                is Screen.Home -> TripsHomeScreen(
                    trips = repository.activeTrips(),
                    onOpenTrip = { navigator.push(Screen.TripDetail(it)) },
                    onCreateTrip = { name ->                                    // ⬅ create + fetch image
                        val id = repository.addTrip(name)
                        scope.launch {
                            val trip = repository.tripById(id)
                            if (trip != null) {
                                val url = repository.unsplashApi.fetchImage(imageQueryForTrip(trip))
                                if (url != null) repository.updateTripImage(id, url)
                            }
                        }
                    },
                    onRenameTrip = { id, name -> repository.updateTrip(id, name) },   // ⬅ rename
                    onPinTrip = { repository.togglePin(it) },
                    onArchiveTrip = { repository.toggleArchive(it) },
                    onDeleteTrip = { repository.deleteTrip(it) },
                )


                Screen.Currency -> CurrencyScreen()

                is Screen.TripDetail -> {
                    val trip = repository.tripById(screen.tripId)
                    if (trip == null) navigator.back()
                    else TripDetailScreen(
                        trip = trip,
                        activities = repository.activitiesForTrip(screen.tripId),
                        onBack = { navigator.back() },
                        onAddLeg = { navigator.push(Screen.AddLeg(screen.tripId)) },
                        onAddPlace = { navigator.push(Screen.AddPlace(screen.tripId)) },
                        onEditActivity = { actId -> navigator.push(Screen.EditPlace(screen.tripId, actId)) },   // ⬅ ADD
                        onChecklist = { navigator.push(Screen.Checklist(screen.tripId)) },
                        onToggleLeg = { repository.toggleLegCompleted(screen.tripId, it) },
                        onEditLeg = { legId -> navigator.push(Screen.EditLeg(screen.tripId, legId)) },
                        onDeleteLeg = { legId -> repository.deleteLeg(screen.tripId, legId) },
                        onToggleActivity = { repository.toggleActivity(it) },
                        onDeleteActivity = { repository.deleteActivity(it) },
                    )
                }

                is Screen.AddPlace -> AddPlaceScreen(
                    onClose = { navigator.back() },
                    onSave = { date, title, time, location ->                          // ⬅ date, not day
                        repository.addActivity(screen.tripId, date, title, time, location)
                        navigator.back()
                    },
                )

                is Screen.EditPlace -> {
                    val act = repository.activitiesForTrip(screen.tripId).firstOrNull { it.id == screen.activityId }
                    if (act == null) navigator.back()
                    else AddPlaceScreen(
                        existing = act,
                        onClose = { navigator.back() },
                        onSave = { date, title, time, location ->
                            repository.updateActivity(act.id, date, title, time, location)
                            navigator.back()
                        },
                    )
                }

                is Screen.AddLeg -> AddLegScreen(                          // adding — no existing, no onDelete
                    onClose = { navigator.back() },
                    onSave = { leg -> repository.addLeg(screen.tripId, leg); navigator.back() },
                )

                is Screen.EditLeg -> {                                     // editing — pass existing + onDelete
                    val trip = repository.tripById(screen.tripId)
                    val leg = trip?.legs?.firstOrNull { it.id == screen.legId }
                    if (leg == null) navigator.back()
                    else AddLegScreen(
                        existing = leg,
                        onClose = { navigator.back() },
                        onSave = { updated -> repository.updateLeg(screen.tripId, updated); navigator.back() },
                        onDelete = { repository.deleteLeg(screen.tripId, screen.legId); navigator.back() },
                    )
                }

                is Screen.ArchivedTrips -> ArchivedTripsScreen(
                    trips = repository.archivedTrips(),
                    onBack = { navigator.back() },
                    onUnarchive = { repository.toggleArchive(it) },   // toggling an archived trip un-archives it
                    onDelete = { repository.deleteTrip(it) },
                )

                Screen.Documents -> DocumentsScreen(
                    documents = repository.documents,
                    onOpenDoc = { navigator.push(Screen.DocViewer(it)) },
                )

                is Screen.DocViewer -> {
                    val doc = repository.documents.firstOrNull { it.id == screen.docId }
                    if (doc == null) {
                        navigator.back()
                    } else {
                        DocumentViewerScreen(doc = doc, onBack = { navigator.back() })
                    }
                }

                is Screen.Checklist -> ChecklistScreen(
                    items = repository.checklistForTrip(screen.tripId),
                    onBack = { navigator.back() },
                    onToggle = { repository.toggleChecklistItem(it) },
                    onAdd = { text, group -> repository.addChecklistItem(screen.tripId, text, group) },
                )

                Screen.Calendar -> CalendarScreen(trips = repository.trips)

                Screen.Settings -> SettingsScreen(
                    profile = repository.profile,
                    onEditProfile = { navigator.push(Screen.EditProfile) },
                    onAppearance = { navigator.push(Screen.Appearance) },
                    onOpenLanguage = { navigator.push(Screen.LanguagePicker) },
                    onArchivedTrips = { navigator.push(Screen.ArchivedTrips) },
                    onLogOut = { navigator.resetTo(Screen.Login) },
                )

                Screen.Appearance -> AppearanceScreen(
                    selected = themeMode,
                    onSelect = onThemeChange,
                    onBack = { navigator.back() },
                )

                Screen.EditProfile -> EditProfileScreen(
                    profile = repository.profile,
                    onBack = { navigator.back() },
                    onSave = { updated ->
                        repository.updateProfile(updated)
                        navigator.back()
                    },
                )

                Screen.LanguagePicker -> LanguageScreen(
                    selected = language,
                    onSelect = onLanguageChange,
                    onBack = { navigator.back() },
                )
                else -> {}
            }
        }
    }
}

private val DarkColors = darkColorScheme(
    primary = Color(0xFF85B7EB),
    onPrimary = Color(0xFF042C53),
    secondary = Color(0xFF5DCAA5),
    background = Color(0xFF14130F),
    surface = Color(0xFF1E1D18),
    surfaceVariant = Color(0xFF2C2C2A),
    onSurface = Color(0xFFEDEAE3),
    error = Color(0xFFF09595),
)

private val LightColors = lightColorScheme(
    primary = Color(0xFF185FA5),
    onPrimary = Color.White,
    secondary = Color(0xFF0F6E56),
    background = Color(0xFFFBFAF7),
    surface = Color.White,
    surfaceVariant = Color(0xFFF1EFE8),
    onSurface = Color(0xFF1A1A1A),
    error = Color(0xFFA32D2D),
)

@Composable
fun ItineraTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content,
    )
}

@Composable
private fun SlidingPillBar(
    current: Screen,
    items: List<NavItem>,
    onSelect: (Screen) -> Unit,
) {
    val count = items.size
    val selectedIndex = items.indexOfFirst { it.screen == current }.coerceAtLeast(0)
    val onSurface = MaterialTheme.colorScheme.onSurface

    val targetBias = if (count <= 1) 0f else -1f + 2f * selectedIndex / (count - 1)
    val bias by animateFloatAsState(
        targetValue = targetBias,
        animationSpec = spring(dampingRatio = 0.75f, stiffness = 400f),
        label = "pillSlide",
    )

    // one "step" of bias between adjacent icons
    val step = if (count <= 1) 2f else 2f / (count - 1)

    Surface(
        shape = RoundedCornerShape(32.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 6.dp,
        shadowElevation = 8.dp,
    ) {
        Box(Modifier.padding(8.dp)) {

            // sliding highlight capsule
            Box(
                modifier = Modifier
                    .fillMaxWidth(1f / count)
                    .height(48.dp)
                    .align(BiasAlignment(horizontalBias = bias, verticalBias = 0f))
                    .padding(horizontal = 4.dp)
                    .clip(CircleShape)
                    .background(onSurface.copy(alpha = 0.12f))
            )

            Row(Modifier.fillMaxWidth()) {
                items.forEachIndexed { index, item ->
                    // this icon's resting position on the bias scale
                    val itemBias = if (count <= 1) 0f else -1f + 2f * index / (count - 1)
                    // 1f when the capsule is right here, 0f once it's a full step away
                    val selectedness = (1f - abs(bias - itemBias) / step).coerceIn(0f, 1f)
                    val tint = lerp(onSurface.copy(alpha = 0.55f), onSurface, selectedness)

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,                       // <- kills the ripple patch
                            ) { onSelect(item.screen) },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            item.icon,
                            contentDescription = item.label,
                            tint = tint,
                            modifier = Modifier.size(24.dp),
                        )
                    }
                }
            }
        }
    }
}
