package com.itinera.app.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.Image
import androidx.compose.ui.geometry.isUnspecified
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import coil3.compose.rememberAsyncImagePainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.itinera.app.data.AuthService
import com.itinera.app.data.ProfileService
import com.itinera.app.i18n.LocalStrings
import com.itinera.app.model.UserProfile
import com.itinera.app.ui.components.PlaneLoader
import com.itinera.app.ui.components.cropToCircle
import com.preat.peekaboo.image.picker.SelectionMode
import com.preat.peekaboo.image.picker.rememberImagePickerLauncher
import kotlinx.coroutines.launch


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
    /**
     * Opens the change-password screen. Optional — omit and the Security
     * section is hidden.
     */
    onChangePassword: (() -> Unit)? = null,
) {
    val s = LocalStrings.current
    val textFieldShape = RoundedCornerShape(12.dp)
    val scope = rememberCoroutineScope()

    // ── Editable fields ──────────────────────────────────────────────
    var name       by remember { mutableStateOf(profile.name) }
    var surname    by remember { mutableStateOf(profile.surname) }
    var mobile by remember { mutableStateOf(profile.mobile) }
    var street     by remember { mutableStateOf(profile.street) }
    var city       by remember { mutableStateOf(profile.city) }
    var postalCode by remember { mutableStateOf(profile.postalCode) }
    var currentPhotoUrl by remember { mutableStateOf(profile.photoUrl) }

    // ── Photo state ──────────────────────────────────────────────────
    var photoBytes    by remember { mutableStateOf<ByteArray?>(null) }  // confirmed crop result
    var pendingBytes  by remember { mutableStateOf<ByteArray?>(null) }  // waiting for crop
    var showSourceSheet by remember { mutableStateOf(false) }
    var showCrop      by remember { mutableStateOf(false) }

    // ── UI state ─────────────────────────────────────────────────────
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
        error = null
        loading = true
        scope.launch {
            try {
                // Upload photo to Firebase Storage if a new one was picked
                var newPhotoUrl = currentPhotoUrl       // use stateful URL (might be cleared)
                if (photoBytes != null && onUploadPhoto != null) {
                    newPhotoUrl = onUploadPhoto(photoBytes!!)
                }

                val updated = profile.copy(
                    name       = name.trim(),
                    surname    = surname.trim(),
                    mobile     = mobile.trim(),
                    street     = street.trim(),
                    city       = city.trim(),
                    postalCode = postalCode.trim(),
                    photoUrl   = newPhotoUrl,
                    photoBytes = photoBytes,
                )

                val uid = authService.currentUid
                if (uid != null) profileService.saveProfile(uid, updated)

                loading = false
                onSave(updated)
            } catch (e: Exception) {
                loading = false
                // ⬅ CHANGED — was e.message, which surfaces raw Firebase text like
                // "An internal error has occurred. [ CONFIGURATION_NOT_FOUND ]".
                // The detail still goes to the log for you.
                error = s.couldntSaveProfile
                println("ITINERA: PROFILE SAVE FAILED — ${e.message}")
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
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp),
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

            // ── Scrollable body ───────────────────────────────────
            Column(
                Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .imePadding()
                    .padding(horizontal = 16.dp),
            ) {
                // ⬅ CHANGED — name and email under the avatar. Free context, and
                // it makes the top of the screen read as a profile rather than
                // the first row of a form.
                Column(
                    Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    ProfilePhotoPicker(
                        photoBytes = photoBytes,
                        photoUrl = currentPhotoUrl,
                        onPickerRequested = { showSourceSheet = true },
                    )
                    val shownName = listOf(name, surname).filter { it.isNotBlank() }.joinToString(" ")
                    if (shownName.isNotBlank()) {
                        Spacer(Modifier.height(12.dp))
                        Text(
                            shownName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                    if (profile.email.isNotBlank()) {
                        Text(
                            profile.email,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                        )
                    }
                }

                // ── YOUR DETAILS ──────────────────────────────────
                // ⬅ CHANGED — was nine identically-outlined boxes. Grouped cards
                // with borderless inputs, matching the leg and place forms.
                FieldSectionLabel(s.yourDetails)
                FieldCard {
                    LabeledField(s.name, name) { name = it }
                    FieldDivider()
                    LabeledField(s.surname, surname) { surname = it }
                    FieldDivider()
                    // ⬅ CHANGED — mobile was the one field with no Spacer before
                    // it, so it butted up against the e-mail box.
                    LabeledField(
                        label = s.mobile,
                        value = mobile,
                        keyboardType = KeyboardType.Phone,
                    ) { mobile = it }
                }

                // ── ACCOUNT (locked) ──────────────────────────────
                FieldSectionLabel(s.account)
                FieldCard {
                    // ⬅ CHANGED — these were OutlinedTextFields with
                    // enabled = false, which renders at ~38% alpha and reads as a
                    // rendering fault. A padlock and a reason say what's going on.
                    LockedRow(Icons.Filled.MailOutline, s.email, profile.email)
                    FieldDivider(startInset = 46.dp)
                    LockedRow(Icons.Filled.Cake, s.dob, profile.dob)
                }
                Text(
                    s.setAtSignup,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f),
                    modifier = Modifier.padding(start = 4.dp, top = 6.dp),
                )

                // ── ADDRESS ───────────────────────────────────────
                FieldSectionLabel(s.address)
                FieldCard {
                    LabeledField(s.street, street) { street = it }
                    FieldDivider()
                    // City and postal code are short and always entered together;
                    // two full-width boxes for them was wasteful.
                    Row(Modifier.height(IntrinsicSize.Min)) {
                        LabeledField(
                            label = s.city,
                            value = city,
                            modifier = Modifier.weight(1.6f),
                        ) { city = it }
                        VerticalDivider(
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.10f),
                        )
                        LabeledField(
                            label = s.postelCode,
                            value = postalCode,
                            keyboardType = KeyboardType.Number,
                            modifier = Modifier.weight(1f),
                        ) { postalCode = it }
                    }
                }

                // ── SECURITY ──────────────────────────────────────
                // ⬅ CHANGED — the password field sat between date of birth and
                // street. Changing a password is a different kind of action, and
                // it needs a current-password check this form never had.
                if (onChangePassword != null) {
                    FieldSectionLabel(s.security)
                    FieldCard {
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clickable { onChangePassword() }
                                .padding(horizontal = 14.dp, vertical = 13.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                Icons.Filled.Key,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                modifier = Modifier.size(18.dp),
                            )
                            Spacer(Modifier.width(14.dp))
                            Text(
                                s.changePassword,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.weight(1f),
                            )
                            Icon(
                                Icons.Filled.ChevronRight,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                                modifier = Modifier.size(18.dp),
                            )
                        }
                    }
                }

                if (error != null) {
                    Spacer(Modifier.height(14.dp))
                    Text(
                        error!!,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                Spacer(Modifier.height(120.dp))
                Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.safeDrawing))
            }
        }

        // ⬅ CHANGED — was inside the scroll at the very bottom, so it sat over
        // the home indicator and scrolled away.
        Column(
            Modifier.align(Alignment.BottomCenter).fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        0f to Color.Transparent,
                        0.4f to MaterialTheme.colorScheme.background,
                    )
                )
                .padding(horizontal = 16.dp)
                .padding(top = 24.dp, bottom = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Button(
                onClick = { attemptSave() },
                enabled = !loading,
                shape = RoundedCornerShape(26.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp),
                contentPadding = PaddingValues(horizontal = 52.dp, vertical = 0.dp),
                modifier = Modifier.height(52.dp),
            ) {
                if (loading) PlaneLoader() else Text(s.saveChanges)
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

                    // Remove option — only shown if there's already a photo
                    if (photoBytes != null || currentPhotoUrl.isNotBlank()) {
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
                                currentPhotoUrl = ""
                                showSourceSheet = false
                            },
                        )
                    }
                }
            }
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
// FORM PIECES
// ═══════════════════════════════════════════════════════════════════

@Composable
private fun FieldSectionLabel(label: String) {
    Text(
        label.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
        modifier = Modifier.padding(start = 2.dp, top = 20.dp, bottom = 8.dp),
    )
}

@Composable
private fun FieldCard(content: @Composable ColumnScope.() -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Column(content = content)
    }
}

@Composable
private fun FieldDivider(startInset: Dp = 0.dp) {
    HorizontalDivider(
        modifier = Modifier.padding(start = startInset),
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.10f),
    )
}

/** Label above a borderless input, so fields inside a card share one outline. */
@Composable
private fun LabeledField(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
    onValueChange: (String) -> Unit,
) {
    val onSurface = MaterialTheme.colorScheme.onSurface
    Column(modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = onSurface.copy(alpha = 0.5f),
        )
        Spacer(Modifier.height(1.dp))
        Box {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyLarge.copy(color = onSurface),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

/**
 * A value the user can't change, with a padlock.
 *
 * Replaces an OutlinedTextField with enabled = false, which renders every part
 * at ~38% alpha and reads as a rendering fault rather than a deliberate lock.
 */
@Composable
private fun LockedRow(icon: ImageVector, label: String, value: String) {
    val onSurface = MaterialTheme.colorScheme.onSurface
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = onSurface.copy(alpha = 0.45f),
            modifier = Modifier.size(18.dp),
        )
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = onSurface.copy(alpha = 0.5f),
            )
            Text(
                value.ifBlank { "—" },
                style = MaterialTheme.typography.bodyLarge,
                color = onSurface.copy(alpha = 0.75f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Icon(
            Icons.Filled.Lock,
            contentDescription = null,
            tint = onSurface.copy(alpha = 0.35f),
            modifier = Modifier.size(15.dp),
        )
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
    val s = LocalStrings.current
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
                    contentDescription = s.profilePhoto,
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
                contentDescription = s.changePhoto,
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
    val painter = rememberAsyncImagePainter(model = imageBytes)
    var scale  by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var parentSize by remember { mutableStateOf(IntSize.Zero) }
    val density = LocalDensity.current

    val transformableState = rememberTransformableState { zoomChange, panChange, _ ->
        scale  = (scale * zoomChange).coerceIn(0.5f, 8f)
        offset = offset + panChange
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .onSizeChanged { parentSize = it },
    ) {
        // ── Image with pan + zoom ──────────────────────────────────
        Image(
            painter = painter,
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

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen },
        ) {
            val holeRadius = minOf(size.width, size.height) / 2f - 48.dp.toPx()
            drawRect(Color.Black.copy(alpha = 0.55f))
            drawCircle(
                color = Color.Transparent,
                radius = holeRadius,
                blendMode = BlendMode.Clear,
            )
        }

        // ── Circle border ──────────────────────────────────────────
        Canvas(modifier = Modifier.fillMaxSize()) {
            val holeRadius = minOf(size.width, size.height) / 2f - 48.dp.toPx()
            drawCircle(
                color = Color.White.copy(alpha = 0.75f),
                radius = holeRadius,
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
                    val img = painter.intrinsicSize
                    if (img.isUnspecified || parentSize.width <= 0) return@Button

                    val iw = img.width; val ih = img.height
                    val pw = parentSize.width.toFloat(); val ph = parentSize.height.toFloat()
                    val fitScale = minOf(pw / iw, ph / ih)
                    val scl = fitScale * scale
                    val cx = pw / 2f; val cy = ph / 2f
                    val holeRadius = (minOf(pw, ph) / 2f) - with(density) { 48.dp.toPx() }

                    fun srcX(gx: Float) = (iw / 2f) + (gx - cx - offset.x) / scl
                    fun srcY(gy: Float) = (ih / 2f) + (gy - cy - offset.y) / scl

                val nL = (srcX(cx - holeRadius) / iw).coerceIn(0f, 1f)
                val nT = (srcY(cy - holeRadius) / ih).coerceIn(0f, 1f)
                val nR = (srcX(cx + holeRadius) / iw).coerceIn(0f, 1f)
                val nB = (srcY(cy + holeRadius) / ih).coerceIn(0f, 1f)

                val cropped = cropToCircle(imageBytes, nL, nT, nR, nB, size = 512)
                onConfirm(cropped)
                },
            ) {
                Text(s.usePhoto)
            }
        }
    }
}
