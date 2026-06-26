package com.itinera.app

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.animation.core.Spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.material.icons.filled.ReceiptLong
import com.itinera.app.data.imageQueryForTrip
import com.itinera.app.ui.theme.ThemeMode
import kotlin.math.abs
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import kotlinx.coroutines.launch
import com.itinera.app.i18n.systemLanguage
import com.itinera.app.model.canEdit
import com.itinera.app.ui.components.PlaneLoader
import kotlinx.coroutines.delay


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

    // ===== ROOT BOX: everything overlays here, so the pill shows on every screen incl. Login =====
    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(onTap = { focusManager.clearFocus() })
            }
            .background(MaterialTheme.colorScheme.background)
            .nestedScroll(nestedScrollConnection),
    ) {
        // Login and LanguagePicker render full-screen (no Surface padding, no nav bar).
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
                                    repository.updateProfile(existing)          // returning user
                                } else {
                                    // new Google user → build profile from Google account
                                    val googleProfile = repository.authService.currentUserProfile()
                                    if (googleProfile != null) {
                                        repository.updateProfile(googleProfile)
                                        repository.profileService.saveProfile(uid, googleProfile)
                                    }
                                }
                            } catch (e: Exception) { }

                            // record this account for the switcher (after profile is set, both paths)
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
                                repository.updateProfile(profile)            // in-memory (immediate UI)
                                scope.launch {                               // persist to Firestore
                                    val uid = repository.authService.currentUid
                                    if (uid != null) {
                                        try {
                                            repository.profileService.saveProfile(uid, profile)
                                        } catch (e: Exception) {
                                            // non-fatal
                                        }
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
                            onOpenMembers = { navigator.push(Screen.Members(it)) },   // ⬅ CHANGED from onCreateInvite
                            onJoinByCode = { repository.joinTripByCode(it) },

                        )

                        Screen.Currency -> CurrencyScreen(
                            onMessage = { pillMessage = it },
                        )

                        is Screen.TripDetail -> {
                            val trip = repository.tripById(screen.tripId)
                            if (trip == null) navigator.back()
                            else TripDetailScreen(
                                trip = trip,
                                activities = repository.activitiesForTrip(screen.tripId),
                                onBack = { navigator.back() },
                                onTravellers = { navigator.push(Screen.Travellers(screen.tripId)) },
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
                                onMembers = { navigator.push(Screen.Members(screen.tripId)) },
                            )
                        }

                        is Screen.AddPlace -> AddPlaceScreen(
                            onClose = { navigator.back() },
                            onSave = { date, title, time, location ->
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

                        is Screen.AddLeg -> AddLegScreen(
                            onClose = { navigator.back() },
                            onSave = { leg -> repository.addLeg(screen.tripId, leg); navigator.back() },
                        )

                        is Screen.EditLeg -> {
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
                            onUnarchive = { repository.toggleArchive(it) },
                            onDelete = { repository.deleteTrip(it) },
                        )

                        is Screen.Travellers -> {
                            val trip = repository.tripById(screen.tripId)
                            if (trip == null) navigator.back()
                            else {
                                LaunchedEffect(screen.tripId) {
                                    repository.ensureOwnerTraveller(screen.tripId)
                                    repository.reconcileMembersToTravellers(screen.tripId)   // ⬅ ADD
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
                            else DocumentsScreen(
                                trip = trip,
                                documents = repository.documentsForTrip(screen.tripId),
                                isLoading = !repository.documentsSyncedOnce,
                                onBack = { navigator.back() },
                                onOpenDoc = { navigator.push(Screen.DocViewer(it)) },
                                onDeleteDocument = { repository.deleteDocument(it) },
                                onMessage = { pillMessage = it },
                                onUpload = { file, title, category ->
                                    repository.addDocumentWithFile(screen.tripId, title, category, file)
                                },
                                canEdit = trip.canEdit(repository.authService.currentUid ?: "")
                            )
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
                            if (trip == null) navigator.back()
                            else ChecklistScreen(
                                items = repository.checklistForTrip(screen.tripId),
                                onBack = { navigator.back() },
                                onToggle = { repository.toggleChecklistItem(it) },
                                onAdd = { text, group -> repository.addChecklistItem(screen.tripId, text, group) },
                                onDelete = { repository.deleteChecklistItem(it) },

                            )
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
                                    repository.reconcileMembersToTravellers(screen.tripId)   // ⬅ ADD
                                }
                                TripExpensesScreen(
                                    trip = trip,
                                    expenses = repository.expensesForTrip(screen.tripId),
                                    isLoading = !repository.expensesSyncedOnce,
                                    onBack = { navigator.back() },
                                    onAddExpense = { navigator.push(Screen.AddExpense(screen.tripId)) },
                                    onEditExpense = { navigator.push(Screen.AddExpense(screen.tripId, it)) },
                                    onDeleteExpense = { repository.deleteExpense(it) },
                                    onSetCurrency = { repository.setTripCurrency(screen.tripId, it) },
                                    canEdit = trip.canEdit(repository.authService.currentUid ?: "")
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
                                    onSave = { exp ->
                                        if (existing == null) repository.addExpense(exp) else repository.updateExpense(exp)
                                        navigator.back()
                                    },
                                )
                            }
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
                            onArchivedTrips = { navigator.push(Screen.ArchivedTrips) },
                            onExportTrips = { navigator.push(Screen.ExportTrips) },
                            onBackupStatus = { navigator.push(Screen.BackupStatus) },
                            onHelp = { navigator.push(Screen.Help) },
                            onAbout = { navigator.push(Screen.About) },
                        )

                        Screen.Account -> AccountScreen(
                            profile = repository.profile,
                            accounts = repository.accountStore.all(),
                            currentUid = repository.authService.currentUid ?: "",
                            onSwitchAccount = { account ->
                                scope.launch {
                                    prefillEmail = account.email
                                    repository.clearLocal()
                                    repository.authService.signOut()
                                    navigator.resetTo(Screen.Login)
                                }
                            },
                            onForgetAccount = { account ->                       // ⬅ ADD
                                repository.accountStore.forget(account.uid)
                                navigator.resetTo(Screen.Account)                // refresh the screen
                            },
                            onAddAccount = {
                                repository.clearLocal()
                                navigator.resetTo(Screen.Login)
                            },
                            onLogOut = {
                                repository.clearLocal()
                                navigator.resetTo(Screen.Login)
                            },
                            onDeleteAccount = {
                                scope.launch {
                                    val uid = repository.authService.currentUid
                                    try {
                                        if (uid != null) {
                                            repository.profileService.deleteProfile(uid)   // Firestore FIRST (while authed)
                                        }
                                        repository.authService.deleteAccount()             // then Auth account
                                        navigator.resetTo(Screen.Login)
                                        pillMessage = s.accountDeleted
                                    } catch (e: Exception) {
                                        navigator.resetTo(Screen.Login)
                                    }
                                }
                            },
                            onBack = { navigator.back() },
                        )

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
                                NotificationPermission.request()                       // Android system dialog
                                scope.launch { repository.notificationScheduler.requestPermission() }  // iOS path
                            },
                            onBack = { navigator.back() },
                        )

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
                        )

                        else -> {}
                    }
                }
            }
        }

        // Floating pill nav bar (only on top-level screens).
        if (showBottomBar) {
            val items = listOf(
                NavItem(Icons.Filled.Flight, s.myTrips, Screen.Home),
                NavItem(Icons.Filled.CalendarMonth, s.calendar, Screen.Calendar),
                NavItem(Icons.Filled.CurrencyExchange, s.currencyUnits, Screen.Currency),
                NavItem(Icons.Filled.ReceiptLong, s.split, Screen.Split),
                NavItem(Icons.Filled.Settings, s.settings, Screen.Settings),
            )
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom))
                    .padding(horizontal = 24.dp)
                    .padding(top = 6.dp)
                    .graphicsLayer {
                        scaleX = animatedScale
                        scaleY = animatedScale
                        transformOrigin = androidx.compose.ui.graphics.TransformOrigin(0.5f, 1f)
                        alpha = 1f
                    },
                contentAlignment = Alignment.Center,
            ) {
                SlidingPillBar(current = current, items = items) { navigator.resetTo(it) }
            }
        }

        // Message pill overlay — last child, so it floats above everything on every screen.
        MessagePill(
            message = pillMessage,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom))
                .padding(bottom = 60.dp),
        )

        MessagePill(
            message = pillMessageTop,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom))
                .padding(bottom = 120.dp),
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
    val onSurface = MaterialTheme.colorScheme.onSurface

    val targetBias = if (count <= 1) 0f else -1f + 2f * selectedIndex / (count - 1)
    val bias by animateFloatAsState(
        targetValue = targetBias,
        animationSpec = spring(dampingRatio = 0.75f, stiffness = 400f),
        label = "pillSlide",
    )

    val step = if (count <= 1) 2f else 2f / (count - 1)

    // ===== Whole-bar hop on screen change =====
    val barPulse = remember { Animatable(1f) }
    LaunchedEffect(current) {
        barPulse.snapTo(1f)
        barPulse.animateTo(1.05f, animationSpec = tween(durationMillis = 100))   // subtle swell
        barPulse.animateTo(
            targetValue = 1f,
            animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f),       // gentler settle
        )
    }

    Surface(
        modifier = Modifier.graphicsLayer {
            scaleX = barPulse.value
            scaleY = barPulse.value
        },   // ⬅ the bounce
        shape = RoundedCornerShape(32.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 6.dp,
        shadowElevation = 2.dp,
    ) {
        Box(Modifier.padding(8.dp)) {

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
                    val itemBias = if (count <= 1) 0f else -1f + 2f * index / (count - 1)
                    val selectedness = (1f - abs(bias - itemBias) / step).coerceIn(0f, 1f)
                    val tint = lerp(onSurface.copy(alpha = 0.55f), onSurface, selectedness)

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
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

@Composable
private fun MessagePill(message: String?, modifier: Modifier = Modifier) {
    // Hold the last non-null text so it stays visible during the fade-out animation.
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

/** Human "time ago" label for the last sync, using localized strings. */
private fun syncLabel(lastSyncedAt: Long?, s: com.itinera.app.i18n.Strings): String {
    if (lastSyncedAt == null) return s.never
    val diff = nowMillisApp() - lastSyncedAt
    val mins = diff / 60_000
    val hours = diff / 3_600_000
    val days = diff / 86_400_000
    return when {
        diff < 60_000 -> s.justNow
        mins < 60 -> "$mins ${if (mins == 1L) s.minuteAgo else s.minutesAgo}"
        hours < 24 -> "$hours ${if (hours == 1L) s.hourAgo else s.hoursAgo}"
        else -> "$days ${if (days == 1L) s.dayAgo else s.daysAgo}"
    }
}