package com.itinera.app.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.itinera.app.data.AuthService
import com.itinera.app.data.ProfileService
import com.itinera.app.i18n.LocalStrings
import com.itinera.app.model.UserProfile
import com.itinera.app.ui.components.cropToCircle
import com.preat.peekaboo.image.picker.SelectionMode
import com.preat.peekaboo.image.picker.rememberImagePickerLauncher
import com.preat.peekaboo.ui.camera.PeekabooCamera          // ⬅ from peekaboo-ui module
import kotlinx.coroutines.launch


/**
 * ─────────────────────────────────────────────────────────────────────
 * WHAT CHANGED vs. the previous version
 * ─────────────────────────────────────────────────────────────────────
 * • Removed `onChangePhoto` parameter — photo picking is now fully
 *   handled inside this screen.
 * • Added `onUploadPhoto` lambda — call site in App.kt passes the
 *   Firebase Storage upload so this screen stays storage-agnostic.
 * • Replaced `ProfileAvatar + Surface(edit)` header with
 *   `ProfilePhotoPicker` which shows the existing URL OR newly
 *   picked bytes.
 * • Added gallery / camera bottom sheet (peekaboo).
 * • Added full-screen `CropScreen` overlay (custom Compose crop).
 * • `ProfilePhotoPicker` now accepts both `photoBytes` and `photoUrl`
 *   so Coil can display either a ByteArray or a remote URL.
 *
 * STRINGS TO ADD to your i18n file:
 *   choosePhoto        → "Choose Photo"
 *   uploadFromGallery  → "Upload from Gallery"
 *   takePhoto          → "Take a Photo"
 *   removePhoto        → "Remove Photo"
 *   moveAndScale       → "Move and scale to adjust"
 *   usePhoto           → "Use Photo"
 *
 * MODEL CHANGE REQUIRED:
 *   Add `val photoUrl: String = ""` to UserProfile so the URL
 *   round-trips through Firestore.
 *
 * APP.KT CHANGE REQUIRED (see comment at bottom of file).
 * ─────────────────────────────────────────────────────────────────────
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    profile: UserProfile,
    authService: AuthService,
    profileService: ProfileService,
    onBack: () -> Unit,
    onSave: (UserProfile) -> Unit,
    // ⬅ ADD: pass `{ bytes -> repository.uploadProfilePhoto(uid, bytes) }` from App.kt
    onUploadPhoto: (suspend (ByteArray) -> String)? = null,
) {
    val s = LocalStrings.current
    val textFieldShape = RoundedCornerShape(12.dp)
    val scope = rememberCoroutineScope()

    // ── Editable fields ──────────────────────────────────────────────
    var name       by remember { mutableStateOf(profile.name) }
    var surname    by remember { mutableStateOf(profile.surname) }
    var password   by remember { mutableStateOf("") }
    var mobile by remember { mutableStateOf(profile.mobile) }
    var street     by remember { mutableStateOf(profile.street) }
    var city       by remember { mutableStateOf(profile.city) }
    var postalCode by remember { mutableStateOf(profile.postalCode) }

    // ── Photo state ──────────────────────────────────────────────────
    var photoBytes    by remember { mutableStateOf<ByteArray?>(null) }  // confirmed crop result
    var pendingBytes  by remember { mutableStateOf<ByteArray?>(null) }  // waiting for crop
    var showSourceSheet by remember { mutableStateOf(false) }
    var showCamera    by remember { mutableStateOf(false) }
    var showCrop      by remember { mutableStateOf(false) }

    // ── UI state ─────────────────────────────────────────────────────
    var passwordVisible by remember { mutableStateOf(false) }
    var error   by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }

    // ── Gallery picker (peekaboo) ─────────────────────────────────────
    val galleryLauncher = rememberImagePickerLauncher(
        selectionMode = SelectionMode.Single,
        scope = scope,
        onResult = { byteArrays ->
            byteArrays.firstOrNull()?.let {
                pendingBytes = it
                showSourceSheet = false
                showCrop = true
            }
        },
    )

    // ── Save logic ───────────────────────────────────────────────────
    fun attemptSave() {
        if (password.isNotBlank() && password.length < 6) {
            error = s.passwordTooShort
            return
        }
        error = null
        loading = true
        scope.launch {
            try {
                // Upload photo to Firebase Storage if a new one was picked
                var newPhotoUrl = profile.photoUrl       // keep existing URL by default
                if (photoBytes != null && onUploadPhoto != null) {
                    newPhotoUrl = onUploadPhoto(photoBytes!!)
                    println("UPLOADED PHOTO URL = $newPhotoUrl")   // ⬅ temporary debug
                }

                val updated = profile.copy(
                    name       = name.trim(),
                    surname    = surname.trim(),
                    mobile     = mobile.trim(),
                    street     = street.trim(),
                    city       = city.trim(),
                    postalCode = postalCode.trim(),
                    photoUrl   = newPhotoUrl,
                )

                val uid = authService.currentUid
                if (uid != null) profileService.saveProfile(uid, updated)
                if (password.isNotBlank()) authService.updatePassword(password)

                loading = false
                onSave(updated)
            } catch (e: Exception) {
                loading = false
                error = e.message ?: "Unknown error"   // ⬅ show the real error
                println("SAVE FAILED: ${e.message}")
            }
        }
    }

    // ── Root box — overlays crop + camera on top of main content ─────
    Box(Modifier.fillMaxSize()) {

        // ═══════════════════════════════════════════════════════════
        // MAIN CONTENT
        // ═══════════════════════════════════════════════════════════
        Column(Modifier.fillMaxSize()) {

            // ── Fixed header ──────────────────────────────────────
            Column(Modifier.padding(horizontal = 24.dp)) {
                Row(
                    Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = s.back)
                    }
                    Text(
                        s.editProfile,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Medium,
                    )
                }

                Spacer(Modifier.height(8.dp))

                // ⬅ CHANGED: replaced ProfileAvatar + edit Surface with ProfilePhotoPicker
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    ProfilePhotoPicker(
                        photoBytes = photoBytes,             // newly picked (overrides URL)
                        photoUrl   = profile.photoUrl,       // existing remote photo
                        onPickerRequested = { showSourceSheet = true },
                    )
                }
                Spacer(Modifier.height(14.dp))
            }

            // ── Scrollable body ───────────────────────────────────
            Column(
                Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp),
            ) {
                OutlinedTextField(
                    name, { name = it },
                    label = { Text(s.name) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = textFieldShape,
                )
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    surname, { surname = it },
                    label = { Text(s.surname) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = textFieldShape,
                )

                // Email — fixed, disabled
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = profile.email, onValueChange = {}, enabled = false,
                    label = { Text(s.email) }, singleLine = true,
                    modifier = Modifier.fillMaxWidth(), shape = textFieldShape,
                )

                OutlinedTextField(
                    value = mobile,
                    onValueChange = { mobile = it },
                    label = { Text(s.mobile) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth(),
                    shape = textFieldShape,
                )

                // Date of birth — fixed, disabled
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = profile.dob, onValueChange = {}, enabled = false,
                    label = { Text(s.dob) }, singleLine = true,
                    modifier = Modifier.fillMaxWidth(), shape = textFieldShape,
                )

                // New password (optional) with show/hide
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    password, { password = it; error = null },
                    label = { Text(s.newPassword) },
                    visualTransformation = if (passwordVisible)
                        VisualTransformation.None
                    else
                        PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Filled.Visibility
                                else Icons.Filled.VisibilityOff,
                                contentDescription = if (passwordVisible) s.hidePassword
                                else s.showPassword,
                            )
                        }
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = textFieldShape,
                )

                Spacer(Modifier.height(16.dp))
                Text(
                    s.address,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                )
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    street, { street = it },
                    label = { Text(s.street) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = textFieldShape,
                )
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    city, { city = it },
                    label = { Text(s.city) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = textFieldShape,
                )
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    postalCode, { postalCode = it },
                    label = { Text(s.postelCode) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = textFieldShape,
                )

                // Inline error
                if (error != null) {
                    Spacer(Modifier.height(10.dp))
                    Text(
                        error!!,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                Spacer(Modifier.height(18.dp))
                Button(
                    onClick = { attemptSave() },
                    enabled = !loading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .padding(horizontal = 60.dp),
                ) {
                    if (loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary,
                        )
                    } else {
                        Text(s.saveChanges)
                    }
                }
                Spacer(Modifier.height(24.dp))
            }
        }

        // ═══════════════════════════════════════════════════════════
        // BOTTOM SHEET — gallery / camera / remove
        // ═══════════════════════════════════════════════════════════
        if (showSourceSheet) {
            ModalBottomSheet(onDismissRequest = { showSourceSheet = false }) {
                Column(
                    Modifier
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 32.dp),
                ) {
                    Text(
                        s.choosePhoto,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(vertical = 12.dp),
                    )
                    HorizontalDivider()

                    ListItem(
                        headlineContent = { Text(s.uploadFromGallery) },
                        leadingContent = {
                            Icon(Icons.Filled.PhotoLibrary, contentDescription = null)
                        },
                        modifier = Modifier.clickable {
                            galleryLauncher.launch()
                        },
                    )

                    ListItem(
                        headlineContent = { Text(s.takePhoto) },
                        leadingContent = {
                            Icon(Icons.Filled.CameraAlt, contentDescription = null)
                        },
                        modifier = Modifier.clickable {
                            showSourceSheet = false
                            showCamera = true
                        },
                    )

                    // Remove option — only shown if there's already a photo
                    if (photoBytes != null || profile.photoUrl.isNotBlank()) {
                        ListItem(
                            headlineContent = {
                                Text(
                                    s.removePhoto,
                                    color = MaterialTheme.colorScheme.error,
                                )
                            },
                            leadingContent = {
                                Icon(
                                    Icons.Filled.Delete,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                )
                            },
                            modifier = Modifier.clickable {
                                photoBytes = null
                                showSourceSheet = false
                            },
                        )
                    }
                }
            }
        }

        // ═══════════════════════════════════════════════════════════
        // CAMERA OVERLAY (peekaboo-ui)
        // ═══════════════════════════════════════════════════════════
        if (showCamera) {
            PeekabooCamera(
                modifier = Modifier.fillMaxSize(),
                captureIcon = { onClick ->
                    IconButton(
                        onClick = onClick,
                        modifier = Modifier
                            .padding(bottom = 32.dp)
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                    ) {
                        Icon(
                            Icons.Filled.CameraAlt,
                            contentDescription = "Capture",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(32.dp),
                        )
                    }
                },
                onCapture = { bytes ->
                    if (bytes != null) {
                        pendingBytes = bytes
                        showCrop = true
                    }
                    showCamera = false
                },
                permissionDeniedContent = {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            "Camera permission denied",
                            color = Color.White,
                        )
                    }
                },
            )
        }

        // ═══════════════════════════════════════════════════════════
        // CROP OVERLAY
        // ═══════════════════════════════════════════════════════════
        if (showCrop && pendingBytes != null) {
            CropScreen(
                imageBytes = pendingBytes!!,
                onConfirm = { cropped ->
                    photoBytes   = cropped
                    pendingBytes = null
                    showCrop     = false
                },
                onDismiss = {
                    pendingBytes = null
                    showCrop     = false
                },
            )
        }
    }
}


// ═══════════════════════════════════════════════════════════════════
// PROFILE PHOTO PICKER
// ⬅ CHANGED: now accepts both `photoBytes` (new pick) and
//            `photoUrl` (existing remote URL) so Coil can handle both
// ═══════════════════════════════════════════════════════════════════
@Composable
fun ProfilePhotoPicker(
    photoBytes: ByteArray? = null,
    photoUrl: String? = null,
    onPickerRequested: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Coil AsyncImage accepts ByteArray OR String URL as `model`
    val imageModel = photoBytes ?: photoUrl?.takeIf { it.isNotBlank() }

    Box(
        contentAlignment = Alignment.BottomEnd,
        modifier = modifier.size(96.dp),
    ) {
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                ) { onPickerRequested() },
            contentAlignment = Alignment.Center,
        ) {
            if (imageModel != null) {
                AsyncImage(
                    model = imageModel,
                    contentDescription = "Profile photo",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape),
                )
            } else {
                Icon(
                    Icons.Filled.Person,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f),
                )
            }
        }

        // Pencil button
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                ) { onPickerRequested() },
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Filled.Edit,
                contentDescription = "Change photo",
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(14.dp),
            )
        }
    }
}


// ═══════════════════════════════════════════════════════════════════
// CROP SCREEN
// Full-screen black overlay with pan/zoom image and circular cutout.
// The actual pixel crop (cropToCircle) is an expect/actual function
// — see commonMain/cropToCircle.kt and platform implementations.
// ═══════════════════════════════════════════════════════════════════
@Composable
private fun CropScreen(
    imageBytes: ByteArray,
    onConfirm: (ByteArray) -> Unit,
    onDismiss: () -> Unit,
) {
    val s = LocalStrings.current
    var scale  by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    val transformableState = rememberTransformableState { zoomChange, panChange, _ ->
        scale  = (scale * zoomChange).coerceIn(0.5f, 5f)
        offset = offset + panChange
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
    ) {
        // ── Image with pan + zoom ──────────────────────────────────
        AsyncImage(
            model = imageBytes,
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxSize()
                .transformable(transformableState)
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale,
                    translationX = offset.x,
                    translationY = offset.y,
                ),
        )

        // ── Dark overlay with transparent circle cutout ────────────
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen },
        ) {
            drawRect(Color.Black.copy(alpha = 0.55f))
            drawCircle(
                color = Color.Transparent,
                radius = size.minDimension / 2f - 48f,
                blendMode = BlendMode.Clear,
            )
        }

        // ── Circle border ──────────────────────────────────────────
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                color = Color.White.copy(alpha = 0.75f),
                radius = size.minDimension / 2f - 48f,
                style = Stroke(width = 1.5f),
            )
        }

        // ── Hint ───────────────────────────────────────────────────
        Text(
            s.cropAndScale,
            color = Color.White.copy(alpha = 0.65f),
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(top = 16.dp),
        )

        // ── Buttons ────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(horizontal = 32.dp, vertical = 24.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            TextButton(onClick = onDismiss) {
                Text(s.cancel, color = Color.White)
            }
            Button(
                onClick = {
                    // cropToCircle is an expect/actual — see platform implementations
                    val cropped = cropToCircle(imageBytes, size = 512)
                    onConfirm(cropped)
                },
            ) {
                Text(s.usePhoto)
            }
        }
    }
}


/*
 * ─────────────────────────────────────────────────────────────────────
 * APP.KT — update the EditProfile case to add onUploadPhoto:
 *
 *   Screen.EditProfile -> EditProfileScreen(
 *       profile        = repository.profile,
 *       authService    = repository.authService,
 *       profileService = repository.profileService,
 *       onBack         = { navigator.back() },
 *       onUploadPhoto  = { bytes ->             // ⬅ ADD
 *           val uid = repository.authService.currentUid ?: return@EditProfileScreen ""
 *           repository.uploadProfilePhoto(uid, bytes)
 *       },
 *       onSave = { updated ->
 *           repository.updateProfile(updated)
 *           navigator.back()
 *           pillMessageTop = s.changesSaved
 *       },
 *   )
 *
 * REPOSITORY — add this function to TripRepository or a StorageService:
 *
 *   suspend fun uploadProfilePhoto(uid: String, bytes: ByteArray): String {
 *       val ref = Firebase.storage.reference.child("profile_photos/$uid.jpg")
 *       ref.putBytes(bytes)
 *       return ref.getDownloadUrl()
 *   }
 *
 * USER PROFILE MODEL — add photoUrl field:
 *   data class UserProfile(
 *       ...existing fields...
 *       val photoUrl: String = "",   // ⬅ ADD
 *   )
 * ─────────────────────────────────────────────────────────────────────
 */