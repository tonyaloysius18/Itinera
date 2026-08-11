package com.itinera.app

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.itinera.app.data.PackingGroups
import com.itinera.app.data.TripRepository
import com.itinera.app.data.imageQueryForTrip
import com.itinera.app.data.packingSuggestions
import com.itinera.app.i18n.Language
import com.itinera.app.i18n.LocalStrings
import com.itinera.app.i18n.stringsFor
import com.itinera.app.i18n.systemLanguage
import com.itinera.app.model.ExpenseCategory
import com.itinera.app.model.Trip
import com.itinera.app.model.canEdit
import com.itinera.app.model.inferExpenseCategory
import com.itinera.app.ui.Navigator
import com.itinera.app.ui.Screen
import com.itinera.app.ui.components.PlaneLoader
import com.itinera.app.ui.components.drawPillShadow
import com.itinera.app.ui.rememberNavigator
import com.itinera.app.ui.screens.AboutScreen
import com.itinera.app.ui.screens.AccountScreen
import com.itinera.app.ui.screens.AddExpenseScreen
import com.itinera.app.ui.screens.AddLegScreen
import com.itinera.app.ui.screens.AddPlaceScreen
import com.itinera.app.ui.screens.AppearanceScreen
import com.itinera.app.ui.screens.ArchivedTripsScreen
import com.itinera.app.ui.screens.BackupStatusScreen
import com.itinera.app.ui.screens.CalendarScreen
import com.itinera.app.ui.screens.ChecklistScreen
import com.itinera.app.ui.screens.CompassScreen
import com.itinera.app.ui.screens.CreateAccountScreen
import com.itinera.app.ui.screens.CurrencyScreen
import com.itinera.app.ui.screens.DocumentViewerScreen
import com.itinera.app.ui.screens.DocumentsScreen
import com.itinera.app.ui.screens.EditProfileScreen
import com.itinera.app.ui.screens.EmergencyScreen
import com.itinera.app.ui.screens.ExportTripsScreen
import com.itinera.app.ui.screens.tripPhase
import com.itinera.app.ui.screens.statusLabel
import com.itinera.app.data.tripStartsInDays
import com.itinera.app.ui.screens.ChangePasswordScreen
import com.itinera.app.ui.screens.TripPhase
import kotlinx.datetime.LocalDate
import com.itinera.app.ui.screens.HelpScreen
import com.itinera.app.ui.screens.LanguageScreen
import com.itinera.app.ui.screens.LoginScreen
import com.itinera.app.ui.screens.MembersScreen
import com.itinera.app.ui.screens.NotificationsScreen
import com.itinera.app.ui.screens.SettingsScreen
import com.itinera.app.ui.screens.SplitScreen
import com.itinera.app.ui.screens.TranslateScreen
import com.itinera.app.ui.screens.TravellersScreen
import com.itinera.app.ui.screens.TripDetailScreen
import com.itinera.app.ui.screens.TripExpensesScreen
import com.itinera.app.ui.screens.TripMapScreen
import com.itinera.app.ui.screens.TripsHomeScreen
import com.itinera.app.ui.screens.WeatherScreen
import com.itinera.app.ui.screens.WorldClockScreen
import com.itinera.app.ui.screens.formatMoney
import com.itinera.app.ui.screens.languageForCountry
import com.itinera.app.ui.theme.ThemeMode
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlin.math.abs
import kotlin.time.Clock

/**
 * App root. Owns the global state and routes the current screen.
 */
@Composable
fun App() {
    val repository = remember { TripRepository() }
    var language by remember { mutableStateOf(Language.ENGLISH) }
    var themeMode by remember { mutableStateOf(ThemeMode.SYSTEM) }
    val navigator = rememberNavigator(Screen.Login)
    var authChecked by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val uid = repository.authService.currentUid
        if (uid != null) {
            var profile: com.itinera.app.model.UserProfile? = null
            try {
                profile = repository.profileService.loadProfile(uid)
                if (profile != null) repository.updateProfile(profile)
            } catch (e: Exception) { }
            repository.migrateToSharedIfNeeded(uid)
            repository.migrateDocsExpensesIfNeeded(uid)
            repository.startSync()
            repository.accountStore.remember(
                com.itinera.app.data.RememberedAccount(
                    uid = uid,
                    email = repository.profile.email,
                    name = repository.profile.fullName,
                    photoUrl = repository.profile.photoUrl,
                    method = repository.authService.currentSignInMethod(),
                )
            )
            repository.rescheduleAllReminders()
            navigator.resetTo(Screen.Home)
        }
        authChecked = true
    }

    val activeStrings = stringsFor(if (language == Language.SYSTEM) systemLanguage() else language)

    val darkTheme = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    ItineraTheme(darkTheme = darkTheme) {
        CompositionLocalProvider(LocalStrings provides activeStrings) {
            if (!authChecked) {
                Box(
                    Modifier.fillMaxSize()
                        .background(MaterialTheme.colorScheme.background),
                    contentAlignment = Alignment.Center,
                ) {
                    PlaneLoader(size = 130.dp)
                }
            } else {
                AppContent(
                    repository, navigator, language, themeMode,
                    { language = it }, { themeMode = it }
                )
            }
        }
    }
}

private data class NavItem(
    val icon: ImageVector,
    val label: String,
    val screen: Screen,
    val photoModel: Any? = null,   // if set, the tab shows this image instead of the icon
)

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
    val focusManager = LocalFocusManager.current

    val scope = rememberCoroutineScope()
    val topLevel = remember { setOf(Screen.Home, Screen.Calendar, Screen.Currency, Screen.Split, Screen.Settings) }

    var prefillEmail by remember { mutableStateOf("") }

    // Scroll-to-shrink logic
    var barScale by remember { mutableStateOf(1f) }
    val animatedScale by animateFloatAsState(
        targetValue = barScale,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "barScale"
    )

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (available.y < -5) {
                    barScale = 0.85f
                } else if (available.y > 5) {
                    barScale = 1f
                }
                return Offset.Zero
            }
        }
    }

    LaunchedEffect(current) {
        barScale = 1f
    }

    LaunchedEffect(current, repository.tripsSyncedOnce) {
        val pending = PendingDeepLink.tripId
        if (pending != null && repository.tripById(pending) != null) {
            PendingDeepLink.tripId = null
            navigator.push(Screen.TripDetail(pending))
        }
    }

    // ===== app-level message pill =====
    var pillMessage by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(pillMessage) {
        if (pillMessage != null) {
            delay(2000)
            pillMessage = null
        }
    }

    var pillMessageTop by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(pillMessageTop) {
        if (pillMessageTop != null) { delay(2000); pillMessageTop = null }
    }

    val scheduler = remember { NotificationScheduler() }

    var lastSyncedAt by remember { mutableStateOf<Long?>(null) }
    var syncing by remember { mutableStateOf(false) }

    val showBottomBar = current in topLevel

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(onTap = { focusManager.clearFocus() })
            }
            .background(MaterialTheme.colorScheme.background)
            .nestedScroll(nestedScrollConnection),
    ) {
        val stateHolder = rememberSaveableStateHolder()
        // Preserve each screen's rememberSaveable state (e.g. scroll position) across
        // navigation. This hand-rolled `when` navigator drops a screen from composition
        // when another is pushed; keying by the screen's type name restores its state
        // (scroll, expanded rows, field text) when we return to it.
        stateHolder.SaveableStateProvider(current::class.simpleName ?: "screen") {
            when (current) {
                Screen.Login -> LoginScreen(
                    authService = repository.authService,
                    prefillEmail = prefillEmail,
                    onAuthed = {
                        scope.launch {
                            val uid = repository.authService.currentUid
                            if (uid != null) {
                                try {
                                    val existing = repository.profileService.loadProfile(uid)
                                    if (existing != null) {
                                        repository.updateProfile(existing)
                                    } else {
                                        val googleProfile = repository.authService.currentUserProfile()
                                        if (googleProfile != null) {
                                            repository.updateProfile(googleProfile)
                                            repository.profileService.saveProfile(uid, googleProfile)
                                        }
                                    }
                                } catch (e: Exception) { }

                                repository.accountStore.remember(
                                    com.itinera.app.data.RememberedAccount(
                                        uid = uid,
                                        email = repository.profile.email,
                                        name = repository.profile.fullName,
                                        photoUrl = repository.profile.photoUrl,
                                        method = repository.authService.currentSignInMethod(),
                                    )
                                )

                                repository.migrateToSharedIfNeeded(uid)
                                repository.migrateDocsExpensesIfNeeded(uid)
                                repository.startSync()
                                lastSyncedAt = nowMillisApp()
                            }
                            navigator.resetTo(Screen.Home)
                            prefillEmail = ""
                        }
                    },
                    onCreateAccount = { navigator.push(Screen.CreateAccount) },
                    onMessage = { pillMessage = it },
                )

                Screen.LanguagePicker -> LanguageScreen(
                    selected = language,
                    onSelect = onLanguageChange,
                    onBack = { navigator.back() },
                    // ⬅ CHANGED — was languageForCountry(whereYouAre(trips)), which
                    // suggested the language of whatever trip you're mid-travel on
                    // instead of where you actually live. Someone in France planning
                    // a Germany trip would get German offered, not French.
                    localLanguage = languageForCountry(deviceCountryName()),
                )

                else -> {
                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .statusBarsPadding(),
                        color = MaterialTheme.colorScheme.background,
                    ) {
                        when (val screen = current) {
                            Screen.CreateAccount -> CreateAccountScreen(
                                authService = repository.authService,
                                onBack = { navigator.back() },
                                onMessage = { pillMessage = it },
                                onCreate = { profile ->
                                    repository.updateProfile(profile)
                                    scope.launch {
                                        val uid = repository.authService.currentUid
                                        if (uid != null) {
                                            try {
                                                repository.profileService.saveProfile(uid, profile)
                                            } catch (e: Exception) {}
                                        }
                                    }
                                    navigator.resetTo(Screen.Login)
                                    pillMessage = s.accountCreated
                                },
                            )

                            is Screen.Home -> TripsHomeScreen(
                                trips = repository.activeTrips(),
                                isLoading = !repository.tripsSyncedOnce,
                                onOpenTrip = { navigator.push(Screen.TripDetail(it)) },
                                onCreateTrip = { name ->
                                    val id = repository.addTrip(name)
                                    scope.launch {
                                        val trip = repository.tripById(id)
                                        if (trip != null) {
                                            val url = repository.unsplashApi.fetchImage(imageQueryForTrip(trip))
                                            if (url != null) repository.updateTripImage(id, url)
                                        }
                                    }
                                },
                                onRenameTrip = { id, name -> repository.updateTrip(id, name) },
                                onPinTrip = { repository.togglePin(it) },
                                onArchiveTrip = { repository.toggleArchive(it) },
                                onDeleteTrip = { repository.deleteTrip(it) },
                                currentUid = repository.authService.currentUid ?: "",
                                onOpenMembers = { navigator.push(Screen.Members(it)) },
                                onJoinByCode = { repository.joinTripByCode(it) },
                            )

                            Screen.Currency -> CurrencyScreen(
                                onMessage = { pillMessage = it },
                            )

                            is Screen.TripDetail -> {
                                val trip = repository.tripById(screen.tripId)
                                if (trip == null) navigator.back()
                                else {
                                    LaunchedEffect(screen.tripId) {
                                        repository.ensureOwnerTraveller(screen.tripId)
                                        repository.reconcileMembersToTravellers(screen.tripId)
                                    }
                                    TripDetailScreen(
                                    trip = trip,
                                    activities = repository.activitiesForTrip(screen.tripId),
                                    travellers = trip.travellers,
                                    currentUid = repository.authService.currentUid ?: "",
                                    onBack = { navigator.back() },
                                    onTravellers = { navigator.push(Screen.Travellers(screen.tripId)) },
                                    onUploadPostcardPhoto = { slot, bytes -> repository.uploadPostcardPhoto(screen.tripId, slot, bytes) },
                                    onRemovePostcardPhoto = { slot -> repository.removePostcardPhoto(screen.tripId, slot) },
                                    onLoadImageBytes = { url -> repository.loadBytes(url) },
                                    onDocuments = { navigator.push(Screen.TripDocuments(screen.tripId)) },
                                    onAddLeg = { navigator.push(Screen.AddLeg(screen.tripId)) },
                                    onAddPlace = { navigator.push(Screen.AddPlace(screen.tripId)) },
                                    onEditActivity = { actId -> navigator.push(Screen.EditPlace(screen.tripId, actId)) },
                                    onChecklist = { navigator.push(Screen.Checklist(screen.tripId)) },
                                    onToggleLeg = { repository.toggleLegCompleted(screen.tripId, it) },
                                    onEditLeg = { legId -> navigator.push(Screen.EditLeg(screen.tripId, legId)) },
                                    onDeleteLeg = { legId -> repository.deleteLeg(screen.tripId, legId) },
                                    onToggleActivity = { repository.toggleActivity(it) },
                                    onDeleteActivity = { repository.deleteActivity(it) },
                                    canEdit = trip.canEdit(repository.authService.currentUid ?: ""),
                                    //onMembers = { navigator.push(Screen.Members(screen.tripId)) },
                                    onMap = { navigator.push(Screen.TripMap(screen.tripId)) },
                                    expensesTotal = repository.expensesForTrip(screen.tripId).sumOf { it.amount },
                                    documents = repository.documents.filter { it.tripId == screen.tripId },
                                    onOpenDoc = { docId -> navigator.push(Screen.DocViewer(docId)) },
                                )
                                }
                            }

                            is Screen.AddPlace -> AddPlaceScreen(
                                onClose = { navigator.back() },
                                tripDates = repository.tripDates(screen.tripId),          // ⬅ ADD — day chips
                                onSave = { date, title, time, endTime, location, note ->  // ⬅ CHANGED
                                    repository.addActivity(screen.tripId, date, title, time, endTime, location, note)
                                    navigator.back()
                                },
                            )

                            is Screen.EditPlace -> {
                                val act = repository.activitiesForTrip(screen.tripId).firstOrNull { it.id == screen.activityId }
                                if (act == null) navigator.back()
                                else AddPlaceScreen(
                                    existing = act,
                                    onClose = { navigator.back() },
                                    tripDates = repository.tripDates(screen.tripId),          // ⬅ ADD
                                    onSave = { date, title, time, endTime, location, note ->  // ⬅ CHANGED
                                        repository.updateActivity(act.id, date, title, time, endTime, location, note)
                                        navigator.back()
                                    },
                                )
                            }

                            is Screen.AddLeg -> AddLegScreen(
                                travellers = repository.tripById(screen.tripId)?.travellers ?: emptyList(),
                                onClose = { navigator.back() },
                                onSave = { leg -> repository.addLeg(screen.tripId, leg); navigator.back() },
                            )

                            is Screen.EditLeg -> {
                                val trip = repository.tripById(screen.tripId)
                                val leg = trip?.legs?.firstOrNull { it.id == screen.legId }
                                if (leg == null) navigator.back()
                                else AddLegScreen(
                                    existing = leg,
                                    travellers = trip.travellers,
                                    onClose = { navigator.back() },
                                    onSave = { updated -> repository.updateLeg(screen.tripId, updated); navigator.back() },
                                    onDelete = { repository.deleteLeg(screen.tripId, screen.legId); navigator.back() },
                                )
                            }

                            is Screen.ArchivedTrips -> ArchivedTripsScreen(
                                trips = repository.archivedTrips(),
                                onBack = { navigator.back() },
                                onUnarchive = { repository.toggleArchive(it) },
                                onDelete = { repository.deleteTrip(it) },
                            )

                            is Screen.Travellers -> {
                                val trip = repository.tripById(screen.tripId)
                                if (trip == null) navigator.back()
                                else {
                                    LaunchedEffect(screen.tripId) {
                                        repository.ensureOwnerTraveller(screen.tripId)
                                        repository.reconcileMembersToTravellers(screen.tripId)
                                    }
                                    TravellersScreen(
                                        travellers = trip.travellers,
                                        onBack = { navigator.back() },
                                        onAdd = { repository.addTraveller(screen.tripId, it) },
                                        onUpdate = { repository.updateTraveller(screen.tripId, it) },
                                        onDelete = { repository.removeTraveller(screen.tripId, it) },
                                        canEdit = trip.canEdit(repository.authService.currentUid ?: ""),
                                        currentUid = repository.authService.currentUid ?: "",
                                    )
                                }
                            }

                            is Screen.Members -> {
                                val trip = repository.tripById(screen.tripId)
                                if (trip == null) navigator.back()
                                else MembersScreen(
                                    trip = trip,
                                    currentUid = repository.authService.currentUid ?: "",
                                    onSetRole = { uid, role -> repository.setMemberRole(screen.tripId, uid, role) },
                                    onRemoveMember = { uid -> repository.removeMember(screen.tripId, uid) },
                                    onCreateInvite = { repository.createTripInvite(it) },
                                    onLeaveTrip = {
                                        scope.launch {
                                            val left = repository.leaveTrip(screen.tripId)
                                            if (left) navigator.resetTo(Screen.Home)
                                        }
                                    },
                                    onBack = { navigator.back() },
                                )
                            }

                            is Screen.TripDocuments -> {
                                val trip = repository.tripById(screen.tripId)
                                if (trip == null) navigator.back()
                                else {
                                    LaunchedEffect(screen.tripId) {
                                        repository.ensureOwnerTraveller(screen.tripId)
                                        repository.reconcileMembersToTravellers(screen.tripId)
                                    }
                                    DocumentsScreen(
                                    trip = trip,
                                    documents = repository.documentsForTrip(screen.tripId),
                                    isLoading = !repository.documentsSyncedOnce,
                                    onBack = { navigator.back() },
                                    onOpenDoc = { navigator.push(Screen.DocViewer(it)) },
                                    onDeleteDocument = { repository.deleteDocument(it) },
                                    onUpdateDocument = { docId, title, category, legId, segmentIndex, travellerId ->
                                        repository.updateDocument(docId, title, category, legId, segmentIndex, travellerId)
                                    },
                                    onMessage = { pillMessage = it },
                                    onUpload = { file, title, category, legId, segmentIndex, travellerId ->
                                        repository.addDocumentWithFile(
                                            tripId = screen.tripId, title = title, category = category,
                                            file = file, legId = legId, segmentIndex = segmentIndex, travellerId = travellerId,
                                        )
                                    },
                                    canEdit = trip.canEdit(repository.authService.currentUid ?: "")
                                )
                                }
                            }

                            is Screen.DocViewer -> {
                                val doc = repository.documents.firstOrNull { it.id == screen.docId }
                                if (doc == null) navigator.back()
                                else DocumentViewerScreen(
                                    doc = doc,
                                    onBack = { navigator.back() },
                                    onLoadBytes = { url -> repository.downloadBytes(url) },
                                    onMessage = { pillMessage = it },
                                )
                            }

                            is Screen.Checklist -> {
                                val trip = repository.tripById(screen.tripId)
                                if (trip == null) {
                                    navigator.back()
                                } else {
                                    val today = remember { Clock.System.todayIn(TimeZone.currentSystemDefault()) }
                                    val items = repository.checklistForTrip(screen.tripId)
                                    val groups = PackingGroups(
                                        documents = s.documents,
                                        packing = s.packing,
                                        transport = s.transport,
                                        money = s.money,
                                        gadget = s.gadget,
                                        other = s.other,
                                    )

                                    var suggestions by remember(trip.id) {
                                        mutableStateOf(packingSuggestions(trip, emptyList(), groups, items.map { it.text }.toSet()))
                                    }

                                    LaunchedEffect(trip.id) {
                                        val weather = repository.fetchTripWeatherFor(trip)
                                        suggestions = packingSuggestions(trip, weather, groups, items.map { it.text }.toSet())
                                    }

                                    val days = tripStartsInDays(trip)
                                    val note = if (days == Int.MAX_VALUE) "" else statusLabel(trip, today, tripPhase(trip, today))

                                    ChecklistScreen(
                                        items = items,
                                        onBack = { navigator.back() },
                                        onToggle = { repository.toggleChecklistItem(it) },
                                        onAdd = { text, group -> repository.addChecklistItem(screen.tripId, text, group) },
                                        onDelete = { repository.deleteChecklistItem(it) },
                                        suggestions = suggestions,
                                        departureNote = note,
                                    )
                                }
                            }

                            is Screen.Split -> {
                                SplitScreen(
                                    trips = repository.activeTrips(),
                                    expenses = repository.expenses,
                                    onOpenTrip = { navigator.push(Screen.TripExpenses(it)) },
                                    isLoading = !repository.expensesSyncedOnce,
                                )
                            }
                            is Screen.TripExpenses -> {
                                val trip = repository.tripById(screen.tripId)
                                if (trip == null) navigator.back()
                                else {
                                    LaunchedEffect(screen.tripId) {
                                        repository.ensureOwnerTraveller(screen.tripId)
                                        repository.reconcileMembersToTravellers(screen.tripId)
                                    }
                                    TripExpensesScreen(
                                        trip = trip,
                                        expenses = repository.expensesForTrip(screen.tripId),
                                        payments = repository.paymentsForTrip(screen.tripId),
                                        isLoading = !repository.expensesSyncedOnce,
                                        onBack = { navigator.back() },
                                        onAddExpense = { navigator.push(Screen.AddExpense(screen.tripId)) },
                                        onEditExpense = { navigator.push(Screen.AddExpense(screen.tripId, it)) },
                                        onDeleteExpense = { repository.deleteExpense(it) },
                                        onSetCurrency = { repository.setTripCurrency(screen.tripId, it) },
                                        canEdit = trip.canEdit(repository.authService.currentUid ?: ""),
                                        currentUid = repository.authService.currentUid ?: "",
                                        onSetSettled = { repository.setTripSettled(screen.tripId, it) },
                                        onMarkPaid = { from, to, amount ->
                                            repository.addPayment(screen.tripId, from, to, amount)
                                        },
                                        onDeletePayment = { repository.deletePayment(it) },
                                    )
                                }
                            }

                            is Screen.AddExpense -> {
                                val trip = repository.tripById(screen.tripId)
                                if (trip == null) navigator.back()
                                else {
                                    val existing = screen.expenseId?.let { id -> repository.expenses.firstOrNull { it.id == id } }
                                    AddExpenseScreen(
                                        trip = trip,
                                        existing = existing,
                                        onBack = { navigator.back() },
                                        onSave = { rawExp ->
                                            val exp = if (rawExp.category == ExpenseCategory.OTHER) {
                                                rawExp.copy(category = inferExpenseCategory(rawExp.description))
                                            } else rawExp
                                            if (existing == null) {
                                                repository.addExpense(exp)
                                                scope.launch {
                                                    NotificationPermission.request()
                                                    repository.notificationScheduler.requestPermission()
                                                    repository.notificationScheduler.notifyNow(
                                                        id = "expense_added_${exp.id}",
                                                        title = "New Expense added to ${trip.title}",
                                                        body = "${exp.description} - ${formatMoney(exp.amount, trip.currencyCode)}",
                                                        tripId = trip.id,
                                                    )
                                                }
                                            } else {
                                                repository.updateExpense(exp)
                                            }
                                            navigator.back()
                                        },
                                    )
                                }
                            }

                            is Screen.TripMap -> {
                                val trip = repository.tripById(screen.tripId)
                                if (trip == null) navigator.back()
                                else TripMapScreen(
                                    trip = trip,
                                    onBack = { navigator.back() },
                                    onBackfill = { repository.backfillLegCoordinates(screen.tripId) },
                                )
                            }

                            Screen.Calendar -> CalendarScreen(
                                trips = repository.trips,
                                onMarkAdded = { tripId, legId -> repository.markLegAddedToCalendar(tripId, legId) },
                            )

                            Screen.Settings -> SettingsScreen(
                                profile = repository.profile,
                                onEditProfile = { navigator.push(Screen.EditProfile) },
                                onAccount = { navigator.push(Screen.Account) },
                                onAppearance = { navigator.push(Screen.Appearance) },
                                onOpenLanguage = { navigator.push(Screen.LanguagePicker) },
                                onNotifications = { navigator.push(Screen.Notifications) },
                                onWorldClock = { navigator.push(Screen.WorldClock) },
                                onEmergency = { navigator.push(Screen.Emergency) },
                                onWeather = { navigator.push(Screen.Weather) },
                                onTranslate = { navigator.push(Screen.Translate) },
                                onCompass = { navigator.push(Screen.Compass) },
                                onArchivedTrips = { navigator.push(Screen.ArchivedTrips) },
                                onExportTrips = { navigator.push(Screen.ExportTrips) },
                                onBackupStatus = { navigator.push(Screen.BackupStatus) },
                                onHelp = { navigator.push(Screen.Help) },
                                onAbout = { navigator.push(Screen.About) },
                                appearanceValue = when (themeMode) {
                                    ThemeMode.SYSTEM -> if (isSystemInDarkTheme()) s.dark else s.light
                                    ThemeMode.LIGHT -> s.light
                                    ThemeMode.DARK -> s.dark
                                },
                                languageValue = if (language == Language.SYSTEM) systemLanguage().nativeName else language.nativeName,
                            )

                            Screen.Account -> {
                                // ⬅ FIX (back button) — accountStore.all() was read
                                // inline, so removing an account didn't recompose.
                                // The workaround was navigator.resetTo(Screen.Account),
                                // which WIPES THE BACK STACK — that's why the back
                                // arrow stopped working after removing an account.
                                //
                                // Local state refreshes the list without touching
                                // navigation. Same pattern as Screen.WorldClock below.
                                var accounts by remember {
                                    mutableStateOf(repository.accountStore.all())
                                }

                                AccountScreen(
                                    profile = repository.profile,
                                    accounts = accounts,
                                    currentUid = repository.authService.currentUid ?: "",
                                    onSwitchAccount = { account ->
                                        scope.launch {
                                            prefillEmail = account.email
                                            repository.clearLocal()
                                            repository.authService.signOut()
                                            navigator.resetTo(Screen.Login)
                                        }
                                    },
                                    onForgetAccount = { account ->
                                        repository.accountStore.forget(account.uid)
                                        accounts = repository.accountStore.all()   // ⬅ FIX
                                    },
                                    onAddAccount = {
                                        scope.launch {
                                            repository.clearLocal()
                                            // ⬅ FIX — clearLocal() only empties the
                                            // in-memory lists; without signOut() the
                                            // Firebase session survives, so the
                                            // LaunchedEffect at line 153 signs the old
                                            // user straight back in on next launch.
                                            repository.authService.signOut()
                                            navigator.resetTo(Screen.Login)
                                        }
                                    },
                                    onLogOut = {
                                        scope.launch {
                                            repository.clearLocal()
                                            repository.authService.signOut()   // ⬅ FIX — same
                                            navigator.resetTo(Screen.Login)
                                        }
                                    },
                                    onDeleteAccount = {
                                        scope.launch {
                                            val uid = repository.authService.currentUid
                                            try {
                                                if (uid != null) {
                                                    repository.deleteAllUserData(uid)
                                                    repository.profileService.deleteProfile(uid)
                                                }
                                                repository.authService.deleteAccount()
                                                // ⬅ FIX (empty card) — the account was
                                                // deleted from Firebase but left in the
                                                // remembered list, so it kept appearing
                                                // as a row with nothing in it.
                                                if (uid != null) repository.accountStore.forget(uid)
                                                repository.clearLocal()
                                                navigator.resetTo(Screen.Login)
                                                pillMessage = s.accountDeleted
                                            } catch (e: Exception) {
                                                // ⬅ FIX — the old catch navigated to
                                                // Login silently, so a failed deletion
                                                // looked exactly like a successful one.
                                                // Firebase throws requires-recent-login
                                                // here on an older session, which is the
                                                // common case.
                                                println("ITINERA: DELETE ACCOUNT FAILED — ${e.message}")
                                                pillMessage = s.couldntDeleteAccount
                                            }
                                        }
                                    },
                                    onBack = { navigator.back() },
                                )
                            }

                            Screen.Notifications -> NotificationsScreen(
                                offsetMinutes = repository.profile.reminderOffsetMinutes,
                                hasPermission = scheduler.hasPermission(),
                                onChangeOffset = { newOffset ->
                                    val updated = repository.profile.copy(reminderOffsetMinutes = newOffset)
                                    repository.updateProfile(updated)
                                    repository.rescheduleAllReminders()
                                    scope.launch {
                                        val uid = repository.authService.currentUid
                                        if (uid != null) repository.profileService.saveProfile(uid, updated)
                                    }
                                },
                                onRequestPermission = {
                                    NotificationPermission.request()
                                    scope.launch { repository.notificationScheduler.requestPermission() }
                                },
                                onBack = { navigator.back() },
                            )

                            Screen.WorldClock -> {
                                var zones by remember { mutableStateOf(repository.worldClockStore.all()) }
                                WorldClockScreen(
                                    zones = zones,
                                    onAddZone = { label, zoneId ->
                                        repository.worldClockStore.add(label, zoneId)
                                        zones = repository.worldClockStore.all()
                                    },
                                    onRemoveZone = { entry ->
                                        repository.worldClockStore.remove(entry)
                                        zones = repository.worldClockStore.all()
                                    },
                                    onReorderZones = { reordered ->
                                        repository.worldClockStore.saveOrder(reordered)
                                        zones = repository.worldClockStore.all()
                                    },
                                    onBack = { navigator.back() },
                                    tripCities = activeTripCities(repository.trips),
                                )

                            }

                            Screen.Emergency -> EmergencyScreen(onBack = { navigator.back() })

                            Screen.Translate -> TranslateScreen(translator = repository.translator, onBack = { navigator.back() })

                            Screen.Compass -> CompassScreen(
                                compass = repository.compass,
                                onBack = { navigator.back() },
                            )

                            Screen.Weather -> {
                                var cities by remember { mutableStateOf(repository.weatherStore.all()) }
                                WeatherScreen(
                                    cities = cities,
                                    onAddCity = { repository.weatherStore.add(it); cities = repository.weatherStore.all() },
                                    onRemoveCity = { repository.weatherStore.remove(it); cities = repository.weatherStore.all() },
                                    onReorderCities = { reordered ->
                                        repository.weatherStore.saveOrder(reordered)
                                        cities = repository.weatherStore.all()
                                    },
                                    onBack = { navigator.back() },
                                    tripCities = activeTripCities(repository.trips),
                                )
                            }

                            Screen.BackupStatus -> BackupStatusScreen(
                                profile = repository.profile,
                                tripCount = repository.trips.size,
                                expenseCount = repository.expenses.size,
                                documentCount = repository.documents.size,
                                lastSyncedLabel = syncLabel(lastSyncedAt, s),
                                syncing = syncing,
                                onSyncNow = {
                                    val uid = repository.authService.currentUid
                                    if (uid != null && !syncing) {
                                        scope.launch {
                                            syncing = true
                                            try {
                                                repository.loadTrips(uid)
                                                repository.loadDocuments(uid)
                                                repository.loadExpenses(uid)
                                                lastSyncedAt = nowMillisApp()
                                                pillMessage = s.syncComplete
                                            } catch (e: Exception) {
                                                pillMessage = s.syncFailed
                                            } finally {
                                                syncing = false
                                            }
                                        }
                                    }
                                },
                                onBack = { navigator.back() },
                            )
                            Screen.Help -> HelpScreen(onBack = { navigator.back() })

                            Screen.ExportTrips -> ExportTripsScreen(
                                trips = repository.activeTrips(),
                                activitiesForTrip = { repository.activitiesForTrip(it) },
                                expensesForTrip = { repository.expensesForTrip(it) },
                                onBack = { navigator.back() },
                            )

                            Screen.About -> AboutScreen(onBack = { navigator.back() })

                            Screen.Appearance -> AppearanceScreen(
                                selected = themeMode,
                                onSelect = onThemeChange,
                                onBack = { navigator.back() },
                            )

                            Screen.EditProfile -> EditProfileScreen(
                                profile        = repository.profile,
                                authService    = repository.authService,
                                profileService = repository.profileService,
                                onBack         = { navigator.back() },
                                onUploadPhoto  = { bytes ->
                                    val uid = repository.authService.currentUid
                                        ?: return@EditProfileScreen ""
                                    repository.uploadProfilePhoto(uid, bytes)
                                },
                                onSave = { updated ->
                                    repository.updateProfile(updated)
                                    navigator.back()
                                    pillMessageTop = s.changesSaved
                                },
                                onChangePassword = { navigator.push(Screen.ChangePassword) },
                            )

                            Screen.ChangePassword -> ChangePasswordScreen(
                                onBack = { navigator.back() },
                                onChanged = { navigator.back() },
                                reauthenticate = { repository.authService.reauthenticate(it) },
                                updatePassword = { repository.authService.updatePassword(it) },
                                hasPasswordAccount = repository.authService.hasPasswordProvider,
                            )
                        }
                    }
                }
            }
        }

        // Floating pill nav bar (only on top-level screens).
        if (showBottomBar) {
            val profilePhoto = repository.profile.photoUrl.takeIf { it.isNotBlank() }
            val items = listOf(
                NavItem(Icons.Filled.CurrencyExchange, s.currencyUnits, Screen.Currency),
                NavItem(Icons.Filled.CalendarMonth, s.calendar, Screen.Calendar),
                NavItem(Icons.Filled.Flight, s.myTrips, Screen.Home),
                NavItem(Icons.AutoMirrored.Filled.ReceiptLong, s.split, Screen.Split),
                NavItem(Icons.Filled.Settings, s.settings, Screen.Settings, photoModel = profilePhoto),
            )
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom))
                    .padding(horizontal = 12.dp)
                    .padding(bottom = 16.dp)
                    .graphicsLayer {
                        scaleX = animatedScale
                        scaleY = animatedScale
                        transformOrigin = TransformOrigin(0.5f, 1f)
                    },
                contentAlignment = Alignment.BottomCenter,
            ) {
                SlidingPillBar(current = current, items = items) { navigator.resetTo(it) }
            }
        }

        // Message pill overlay
        MessagePill(
            message = pillMessage,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom))
                .padding(bottom = 96.dp),
        )

        MessagePill(
            message = pillMessageTop,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom))
                .padding(bottom = 156.dp),
        )
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

    val barBackgroundColor = MaterialTheme.colorScheme.surfaceVariant
    val gapColor = MaterialTheme.colorScheme.background

    // 🎨 DYNAMIC LUMINANCE CHECK: Determines whether the current layout context is light or dark
    // by checking the red color channel component value of your surfaceVariant theme configuration.
    val isLightModeColor = barBackgroundColor.red > 0.5f

    // 🎯 FIX: Explicitly forces the circle color to mirror the main background surface variant
    // when running in light mode, ensuring perfect color uniformity.
    val activeCircleColor = if (isLightModeColor) barBackgroundColor else Color(0xFF3D3C3A)

    // 🎯 FIX: Sets the selected icon color to match the clean unselected tone, avoiding any blue tints.
    val selectedIconColor = if (isLightModeColor) Color(0xFF5A5957) else Color(0xFFDBDBDB)
    val unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)

    val targetBias = if (count <= 1) 0f else -1f + 2f * selectedIndex / (count - 1)
    val bias by animateFloatAsState(
        targetValue = targetBias,
        animationSpec = spring(dampingRatio = 0.78f, stiffness = 350f),
        label = "pillSlide",
    )
    val step = if (count <= 1) 2f else 2f / (count - 1)

    val barPulse = remember { Animatable(1f) }
    LaunchedEffect(current) {
        barPulse.snapTo(1f)
        barPulse.animateTo(1.02f, animationSpec = tween(durationMillis = 80))
        barPulse.animateTo(
            targetValue = 1f,
            animationSpec = spring(dampingRatio = 0.65f, stiffness = 400f),
        )
    }

    val animatedIndexFloat = (bias + 1f) / 2f * (count - 1)
    val sidePaddingPx = with(LocalDensity.current) { 32.dp.toPx() }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .height(96.dp)
            .graphicsLayer {
                scaleX = barPulse.value
                scaleY = barPulse.value
            },
        contentAlignment = Alignment.BottomCenter
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(96.dp)
        ) {
            val width = size.width
            val height = size.height
            val cornerRadius = 28.dp.toPx()

            val barHeightPx = 56.dp.toPx()
            val barTopY = height - barHeightPx

            val centerX = sidePaddingPx + (width - 2 * sidePaddingPx) * (animatedIndexFloat + 0.5f) / count

            val dipWidth = 52.dp.toPx()
            val dipDepth = 40.dp.toPx()

            val bubbleRadius = 24.dp.toPx()
            val bubbleCenterY = barTopY + 8.dp.toPx()

            // Base Pill Path Geometry
            val path = Path().apply {
                val dipStart = centerX - dipWidth
                val dipEnd = centerX + dipWidth

                // Start bottom-left
                moveTo(0f, height - cornerRadius)
                quadraticTo(0f, height, cornerRadius, height)
                lineTo(width - cornerRadius, height)
                quadraticTo(width, height, width, height - cornerRadius)

                // Right Edge
                lineTo(width, barTopY + cornerRadius)
                if (dipEnd < width - cornerRadius) {
                    quadraticTo(width, barTopY, width - cornerRadius, barTopY)
                    lineTo(dipEnd, barTopY)
                } else {
                    lineTo(width, barTopY)
                    lineTo(dipEnd, barTopY)
                }

                // The Dip (Right to Left)
                cubicTo(
                    x1 = dipEnd - (dipWidth * 0.42f), y1 = barTopY,
                    x2 = centerX + (dipWidth * 0.52f), y2 = barTopY + dipDepth,
                    x3 = centerX, y3 = barTopY + dipDepth
                )
                cubicTo(
                    x1 = centerX - (dipWidth * 0.52f), y1 = barTopY + dipDepth,
                    x2 = dipStart + (dipWidth * 0.42f), y2 = barTopY,
                    x3 = dipStart, y3 = barTopY
                )

                // Left Edge
                if (dipStart > cornerRadius) {
                    lineTo(cornerRadius, barTopY)
                    quadraticTo(0f, barTopY, 0f, barTopY + cornerRadius)
                } else {
                    lineTo(0f, barTopY)
                    lineTo(0f, barTopY + cornerRadius)
                }
                close()
            }

            // Combined Shadow Path
            val shadowPath = Path().apply {
                addPath(path)
                addOval(Rect(centerX - bubbleRadius - 6.dp.toPx(), bubbleCenterY - bubbleRadius - 6.dp.toPx(), centerX + bubbleRadius + 6.dp.toPx(), bubbleCenterY + bubbleRadius + 6.dp.toPx()))
            }

            // Native Drop Shadow Layer
            drawPillShadow(
                path = shadowPath,
                radius = 14.dp.toPx(),
                dy = 4.dp.toPx(),
                isLightMode = isLightModeColor
            )

            // Draw Base Pill Fill
            drawPath(path = path, color = barBackgroundColor)

            // Draw Core Active Bubble Circle Surface
            drawCircle(
                color = activeCircleColor,
                radius = bubbleRadius,
                center = Offset(centerX, bubbleCenterY)
            )
        }

        // Icons Row Layout
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp)
                .height(56.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEachIndexed { index, item ->
                val itemBias = if (count <= 1) 0f else -1f + 2f * index / (count - 1)
                val selectedness = (1f - abs(bias - itemBias) / step).coerceIn(0f, 1f)
                val tint = lerp(unselectedIconColor, selectedIconColor, selectedness)

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                        ) { onSelect(item.screen) },
                    contentAlignment = Alignment.Center,
                ) {
                    val verticalOffset = with(LocalDensity.current) { (-20.dp * selectedness).toPx() }

                    if (item.photoModel != null) {
                        // Profile photo tab — circular crop. Grows when selected.
                        val photoScale = 1f + 0.45f * selectedness   // 1.0 → 1.45
                        AsyncImage(
                            model = item.photoModel,
                            contentDescription = item.label,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .graphicsLayer {
                                    translationY = verticalOffset
                                    scaleX = photoScale
                                    scaleY = photoScale
                                }
                                .size(26.dp)
                                .clip(CircleShape),
                        )
                    } else {

                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.label,
                            tint = tint,
                            modifier = Modifier
                                .graphicsLayer { translationY = verticalOffset }
                                .size(24.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MessagePill(message: String?, modifier: Modifier = Modifier) {
    var lastMessage by remember { mutableStateOf("") }
    if (message != null) lastMessage = message

    AnimatedVisibility(
        visible = message != null,
        enter = fadeIn() + slideInVertically { it / 2 },
        exit = fadeOut() + slideOutVertically { it / 2 },
        modifier = modifier,
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color.DarkGray.copy(alpha = 0.6f),
            shadowElevation = 6.dp,
        ) {
            Text(
                lastMessage,
                color = Color.White,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
            )
        }
    }
}

@OptIn(kotlin.time.ExperimentalTime::class)
private fun nowMillisApp(): Long =
    kotlin.time.Clock.System.now().toEpochMilliseconds()

private fun syncLabel(lastSyncedAt: Long?, s: com.itinera.app.i18n.Strings): String {
    if (lastSyncedAt == null) return s.never
    val diff = nowMillisApp() - lastSyncedAt
    val mins = diff / 60_000
    val hours = diff / 3_360_000
    val days = diff / 86_400_000
    return when {
        diff < 60_000 -> s.justNow
        mins < 60 -> "$mins ${if (mins == 1L) s.minuteAgo else s.minutesAgo}"
        hours < 24 -> "$hours ${if (hours == 1L) s.hourAgo else s.hoursAgo}"
        else -> "$days ${if (days == 1L) s.dayAgo else s.daysAgo}"
    }
}

private fun activeTripCities(trips: List<Trip>): List<String> {
    val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
    return trips
        .filter { tripPhase(it, today) != TripPhase.PAST }
        .flatMap { it.legs }
        .map { it.toCity }
        .filter { it.isNotBlank() }
        .distinct()
        .take(6)
}

private fun currentTripCountry(trips: List<Trip>): String? {
    val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
    val trip = trips.firstOrNull { tripPhase(it, today) == TripPhase.IN_PROGRESS } ?: return null
    // The most recent leg on or before today — where you've actually arrived.
    return trip.legs
        .filter { it.date <= today && it.country.isNotBlank() }
        .maxByOrNull { it.date }
        ?.country
}
