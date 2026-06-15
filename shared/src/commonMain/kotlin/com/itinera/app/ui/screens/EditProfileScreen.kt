package com.itinera.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.itinera.app.i18n.LocalStrings
import com.itinera.app.model.UserProfile
import com.itinera.app.data.AuthService
import com.itinera.app.data.ProfileService
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch


@Composable
fun EditProfileScreen(
    profile: UserProfile,
    authService: AuthService,                 // ⬅ ADD
    profileService: ProfileService,           // ⬅ ADD
    onBack: () -> Unit,
    onSave: (UserProfile) -> Unit,            // updates in-memory profile + navigates
    onChangePhoto: () -> Unit = {},
) {
    var name by remember { mutableStateOf(profile.name) }
    var surname by remember { mutableStateOf(profile.surname) }
    var password by remember { mutableStateOf("") }
    var street by remember { mutableStateOf(profile.street) }
    var city by remember { mutableStateOf(profile.city) }
    var postalCode by remember { mutableStateOf(profile.postalCode) }

    var passwordVisible by remember { mutableStateOf(false) }     // ⬅ ADD: show/hide toggle
    var error by remember { mutableStateOf<String?>(null) }       // ⬅ ADD: inline error
    var loading by remember { mutableStateOf(false) }             // ⬅ ADD: button loading
    val scope = rememberCoroutineScope()                          // ⬅ ADD

    val s = LocalStrings.current
    val textFieldShape = RoundedCornerShape(12.dp)

    // Builds the updated profile, saves to Firestore, optionally changes the password.
    fun attemptSave() {                                           // ⬅ ADD
        // validate password only if one was entered
        if (password.isNotBlank() && password.length < 6) {
            error = s.passwordTooShort
            return
        }
        error = null
        loading = true
        val updated = profile.copy(
            name = name, surname = surname,
            street = street, city = city, postalCode = postalCode,
        )
        scope.launch {
            try {
                val uid = authService.currentUid
                // 1) save profile edits to Firestore (if signed in)
                if (uid != null) profileService.saveProfile(uid, updated)
                // 2) change password only if a new one was typed
                if (password.isNotBlank()) {
                    authService.updatePassword(password)
                }
                loading = false
                onSave(updated)            // update in-memory profile + navigate back
            } catch (e: Exception) {
                loading = false
                // most likely "requires recent login" for the password change
                error = s.recentLoginRequired
            }
        }
    }

    Column(Modifier.fillMaxSize()) {

        // ===== FIXED HEADER (does not scroll) =====
        Column(Modifier.padding(horizontal = 24.dp)) {

            Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") }
                Text(s.editProfile, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Medium)
            }

            // Avatar with a change-photo button
            Spacer(Modifier.height(8.dp))
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Box {
                    ProfileAvatar(profile, size = 96.dp)

                    Surface(
                        onClick = onChangePhoto,
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary,
                        border = BorderStroke(2.dp, MaterialTheme.colorScheme.surface),
                        modifier = Modifier
                            .size(32.dp)
                            .align(Alignment.BottomEnd),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Filled.Edit,
                                contentDescription = "Change photo",
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(16.dp),
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(14.dp))
        }
        // ===== END FIXED HEADER =====

        // ===== SCROLLABLE BODY =====
        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
        ) {
            OutlinedTextField(name, { name = it }, label = { Text(s.name) }, singleLine = true, modifier = Modifier.fillMaxWidth(), shape = textFieldShape)
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(surname, { surname = it }, label = { Text(s.surname) }, singleLine = true, modifier = Modifier.fillMaxWidth(), shape = textFieldShape)

            // Fixed: email — shown but disabled
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(
                value = profile.email, onValueChange = {}, enabled = false,
                label = { Text(s.email) }, singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = textFieldShape,
            )

            // Fixed: date of birth — shown but disabled
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(
                value = profile.dob, onValueChange = {}, enabled = false,
                label = { Text(s.dob) }, singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = textFieldShape,
            )

            // New password — optional; with show/hide toggle
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
                            imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                            contentDescription = if (passwordVisible) s.hidePassword else s.showPassword,
                        )
                    }
                },
                singleLine = true, modifier = Modifier.fillMaxWidth(),
                shape = textFieldShape,
            )

            Spacer(Modifier.height(16.dp))
            Text(s.address, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(street, { street = it }, label = { Text(s.street) }, singleLine = true, modifier = Modifier.fillMaxWidth(), shape = textFieldShape)
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(city, { city = it }, label = { Text(s.city) }, singleLine = true, modifier = Modifier.fillMaxWidth(), shape = textFieldShape)
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(postalCode, { postalCode = it }, label = { Text(s.postelCode) }, singleLine = true, modifier = Modifier.fillMaxWidth(), shape = textFieldShape)

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
                modifier = Modifier.fillMaxWidth().height(50.dp).padding(horizontal = 60.dp),
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
        // ===== END SCROLLABLE BODY =====
    }
}