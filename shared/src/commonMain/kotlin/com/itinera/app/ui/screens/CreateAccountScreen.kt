package com.itinera.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.itinera.app.i18n.LocalStrings
import com.itinera.app.model.UserProfile                       // ⬅ ADD
import com.itinera.app.data.AuthService                        // ⬅ ADD
import androidx.compose.runtime.rememberCoroutineScope         // ⬅ ADD
import kotlinx.coroutines.launch                               // ⬅ ADD
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * Collects the user's details and creates a REAL Firebase account via AuthService.
 * Firebase stores only email + password; the rest of the details are handed up
 * through onCreate(UserProfile) so App.kt can save them to the repository profile.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateAccountScreen(
    authService: AuthService,                 // ⬅ ADD
    onBack: () -> Unit,
    onCreate: (UserProfile) -> Unit,          // ⬅ CHANGED: now carries the profile up
) {
    val s = LocalStrings.current
    val textFieldShape = RoundedCornerShape(12.dp)

    var name by remember { mutableStateOf("") }
    var surname by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    // Date of birth
    var dob by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    // Address
    var street by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var postalCode by remember { mutableStateOf("") }

    var error by remember { mutableStateOf<String?>(null) }    // ⬅ ADD
    var loading by remember { mutableStateOf(false) }          // ⬅ ADD
    val scope = rememberCoroutineScope()                       // ⬅ ADD

    // Validate, then create the Firebase account. Suspends, so runs in a coroutine.
    fun attemptCreate() {                                       // ⬅ ADD
        when {
            name.isBlank() || email.isBlank() || password.isBlank() -> {
                error = s.fillAllFields
                return
            }
            password.length < 6 -> {
                error = s.passwordTooShort
                return
            }
        }
        error = null
        loading = true
        scope.launch {
            try {
                authService.signUp(email, password)
                val profile = UserProfile(
                    name = name.trim(),
                    surname = surname.trim(),
                    email = email.trim(),
                    dob = dob,
                    street = street.trim(),
                    city = city.trim(),
                    postalCode = postalCode.trim(),
                )
                loading = false
                onCreate(profile)                              // success → save profile + navigate
            } catch (e: Exception) {
                loading = false
                error = s.signupFailed                         // email in use / weak pw / network
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 5.dp)
            .padding(top = 24.dp),
    ) {
        Spacer(Modifier.statusBarsPadding())
        Text(
            s.createAccount,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 8.dp),
        )


        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
        ) {
            Spacer(Modifier.height(5.dp))
            OutlinedTextField(
                value = name, onValueChange = { name = it; error = null },
                label = { Text(s.name) }, singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = textFieldShape,
            )
            Spacer(Modifier.height(9.dp))
            OutlinedTextField(
                value = surname, onValueChange = { surname = it },
                label = { Text(s.surname) }, singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = textFieldShape,
            )
            Spacer(Modifier.height(9.dp))
            EmailFieldWithSuggestions(
                email = email,
                onEmailChange = { email = it; error = null },
                label = s.email,
                shape = textFieldShape,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(9.dp))
            OutlinedTextField(
                value = password, onValueChange = { password = it; error = null },
                label = { Text(s.password) }, singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                shape = textFieldShape,
            )

            // Date of birth — read-only field that opens the calendar on tap
            Spacer(Modifier.height(9.dp))
            OutlinedTextField(
                value = dob,
                onValueChange = {},
                readOnly = true,
                enabled = false,                       // so the whole field is tappable
                label = { Text(s.dob) },
                trailingIcon = { Icon(Icons.Filled.CalendarMonth, contentDescription = null) },
                shape = textFieldShape,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showDatePicker = true },
                colors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                    disabledBorderColor = MaterialTheme.colorScheme.outline,
                    disabledLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    disabledTrailingIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                ),
            )

            // Address — three separate fields
            Spacer(Modifier.height(20.dp))
            Text(s.address, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(9.dp))
            OutlinedTextField(
                value = street, onValueChange = { street = it },
                label = { Text(s.street) }, singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = textFieldShape,
            )
            Spacer(Modifier.height(9.dp))
            OutlinedTextField(
                value = city, onValueChange = { city = it },
                label = { Text(s.city) }, singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = textFieldShape,
            )
            Spacer(Modifier.height(9.dp))
            OutlinedTextField(
                value = postalCode, onValueChange = { postalCode = it },
                label = { Text(s.postelCode) }, singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = textFieldShape,
            )

            // Inline error message
            if (error != null) {                               // ⬅ ADD
                Spacer(Modifier.height(12.dp))
                Text(
                    error!!,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            Spacer(Modifier.height(20.dp))
            Button(
                onClick = { attemptCreate() },                 // ⬅ CHANGED: real sign-up
                enabled = !loading,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 45.dp),
            ) {
                if (loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                } else {
                    Text(s.createAccount)
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
    // The calendar dialog
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val date = Instant.fromEpochMilliseconds(millis)
                            .toLocalDateTime(TimeZone.UTC).date
                        dob = "${date.dayOfMonth}/${date.monthNumber}/${date.year}"
                    }
                    showDatePicker = false
                }) { Text(s.ok) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text(s.cancel) }
            },
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EmailFieldWithSuggestions(
    email: String,
    onEmailChange: (String) -> Unit,
    label: String,
    shape: Shape,
    modifier: Modifier = Modifier,
) {
    val domains = listOf("gmail.com", "yahoo.com", "hotmail.com", "outlook.com", "icloud.com", "proton.me")

    // local part = text before any "@"
    val localPart = email.substringBefore("@")
    val afterAt = if (email.contains("@")) email.substringAfter("@") else null

    // build suggestions: only when there's a local part and "@" has been typed
    val suggestions = if (email.contains("@") && localPart.isNotBlank()) {
        domains
            .filter { it.startsWith(afterAt ?: "", ignoreCase = true) }
            .map { "$localPart@$it" }
            .filter { it != email }
    } else emptyList()
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = suggestions.isNotEmpty(),
        onExpandedChange = { },
        modifier = modifier,
    ) {
        OutlinedTextField(
            value = email,
            onValueChange = { onEmailChange(it) },
            label = { Text(label) },
            singleLine = true,
            shape = shape,
            modifier = Modifier.menuAnchor().fillMaxWidth(),
        )
        MaterialTheme(shapes = MaterialTheme.shapes.copy(extraSmall = RoundedCornerShape(16.dp)))
        {
            ExposedDropdownMenu(
                expanded = suggestions.isNotEmpty(),
                onDismissRequest = { },
            ) {
                suggestions.forEach { suggestion ->
                    DropdownMenuItem(
                        text = { Text(suggestion) },
                        onClick = {
                            onEmailChange(suggestion)
                            expanded = false
                        },
                    )
                }
            }
        }
    }
}