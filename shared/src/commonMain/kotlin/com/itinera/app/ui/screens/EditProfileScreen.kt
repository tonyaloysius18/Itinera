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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.itinera.app.i18n.LocalStrings
import com.itinera.app.model.UserProfile


@Composable
fun EditProfileScreen(
    profile: UserProfile,
    onBack: () -> Unit,
    onSave: (UserProfile) -> Unit,
    onChangePhoto: () -> Unit = {},   // wired to the picker in the next step
) {
    var name by remember { mutableStateOf(profile.name) }
    var surname by remember { mutableStateOf(profile.surname) }
    var password by remember { mutableStateOf("") }
    var street by remember { mutableStateOf(profile.street) }
    var city by remember { mutableStateOf(profile.city) }
    var postalCode by remember { mutableStateOf(profile.postalCode) }

    val s = LocalStrings.current
    val textFieldShape = RoundedCornerShape(12.dp)

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

                    // Pen badge, bottom-right corner
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
            Spacer(Modifier.height(16.dp))   // ⬅ CHANGED: moved here, inside the fixed header
        }
        // ===== END FIXED HEADER =====

        // ===== SCROLLABLE BODY (only the fields scroll) =====
        Column(                                          // ⬅ CHANGED: new wrapper Column for the fields
            Modifier
                .weight(1f)                              // ⬅ CHANGED: fills remaining height
                .verticalScroll(rememberScrollState())   // ⬅ CHANGED: scroll lives here only
                .padding(horizontal = 24.dp),            // ⬅ CHANGED: fields keep their side padding
        ) {
            OutlinedTextField(name, { name = it }, label = { Text(s.name) }, singleLine = true, modifier = Modifier.fillMaxWidth(), shape = textFieldShape)
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(surname, { surname = it }, label = { Text(s.surname) }, singleLine = true, modifier = Modifier.fillMaxWidth(), shape = textFieldShape)

            // Fixed: email — shown but disabled
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = profile.email, onValueChange = {}, enabled = false,
                label = { Text(s.email) }, singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = textFieldShape,
            )

            // Fixed: date of birth — shown but disabled
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = profile.dob, onValueChange = {}, enabled = false,
                label = { Text(s.dob) }, singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = textFieldShape,
            )

            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                password, { password = it },
                label = { Text(s.newPassword) },
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true, modifier = Modifier.fillMaxWidth(),
                shape = textFieldShape,
            )

            Spacer(Modifier.height(20.dp))
            Text(s.address, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(street, { street = it }, label = { Text(s.street) }, singleLine = true, modifier = Modifier.fillMaxWidth(), shape = textFieldShape)
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(city, { city = it }, label = { Text(s.city) }, singleLine = true, modifier = Modifier.fillMaxWidth(), shape = textFieldShape)
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(postalCode, { postalCode = it }, label = { Text(s.postelCode) }, singleLine = true, modifier = Modifier.fillMaxWidth(), shape = textFieldShape)

            Spacer(Modifier.height(24.dp))
            Button(
                onClick = {
                    onSave(profile.copy(
                        name = name, surname = surname,
                        street = street, city = city, postalCode = postalCode,
                    ))
                    // password change would go to your auth provider here, not into UserProfile
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
            ) { Text(s.saveChanges) }
            Spacer(Modifier.height(24.dp))
        }
        // ===== END SCROLLABLE BODY =====
    }
}