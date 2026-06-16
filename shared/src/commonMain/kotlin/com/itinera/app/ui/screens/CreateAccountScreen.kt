package com.itinera.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import kotlinx.coroutines.launch                               // ⬅ ADD
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import com.itinera.app.ui.components.PhoneNumberField
import com.itinera.app.ui.components.countries

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
    onCreate: (UserProfile) -> Unit, // ⬅ CHANGED: now carries the profile up
    onMessage: (String) -> Unit,
) {
    val s = LocalStrings.current
    val textFieldShape = RoundedCornerShape(12.dp)

    var name by remember { mutableStateOf("") }
    var surname by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    var mobile by remember { mutableStateOf("") }
    
    // Default country (e.g., US)
    var country by remember { mutableStateOf(countries.find { it.code == "US" } ?: countries.first()) }

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
    val scope = rememberCoroutineScope()

    // ⬅ ADD


    fun String.toTitleCase(): String =
        split(" ").joinToString(" ") { word ->
            word.replaceFirstChar { char ->
                if (char.isLowerCase()) char.titlecase() else char.toString()
            }
        }

    // Validate, then create the Firebase account. Suspends, so runs in a coroutine.
    fun attemptCreate() {
        when {
            listOf(name, surname, email, password, mobile, dob, street, city, postalCode).any { it.isBlank() } -> {
                onMessage(s.fillAllFields); return
            }
            password.length < 6 -> {
                onMessage(s.passwordTooShort); return
            }
            !password.any { it.isUpperCase() } -> {
                onMessage("Password needs an uppercase letter"); return
            }
            !password.any { it.isLowerCase() } -> {
                onMessage("Password needs a lowercase letter"); return
            }
            !password.any { it.isDigit() } -> {
                onMessage("Password needs a number"); return
            }
            !password.any { !it.isLetterOrDigit() } -> {
                onMessage("Password needs a special character"); return
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
                    mobile = country.dialCode + mobile.trim(),
                    dob = dob,
                    street = street.trim(),
                    city = city.trim(),
                    postalCode = postalCode.trim(),
                )
                loading = false
                onCreate(profile)                              // success → save profile + navigate
            } catch (e: Exception) {
                loading = false
                onMessage(s.signupFailed)                         // email in use / weak pw / network
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 5.dp)
            .padding(top = 8.dp),
    ) {
        Spacer(Modifier.statusBarsPadding())

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 16.dp).offset(x = (-12).dp)
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = s.back
                )
            }
            Text(
                s.createAccount,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Medium,
            )
        }



        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
        ) {
            Spacer(Modifier.height(5.dp))
            OutlinedTextField(
                value = name, onValueChange = { name = it.toTitleCase() ; error = null },
                label = { RequiredLabel(s.name) }, singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = textFieldShape,
            )
            Spacer(Modifier.height(9.dp))
            OutlinedTextField(
                value = surname, onValueChange = { surname = it.toTitleCase() },
                label = { RequiredLabel(s.surname) }, singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = textFieldShape,
            )
            Spacer(Modifier.height(9.dp))
            EmailFieldWithSuggestions(
                email = email,
                onEmailChange = { email = it; error = null },
                label = { RequiredLabel(s.email) },
                shape = textFieldShape,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(9.dp))
            OutlinedTextField(
                value = password, onValueChange = { password = it; error = null },
                label = { RequiredLabel(s.password) }, singleLine = true,
                visualTransformation = if (passwordVisible)               // ⬅ CHANGED
                    VisualTransformation.None
                else
                    PasswordVisualTransformation(),
                trailingIcon = {                                          // ⬅ ADD
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                            contentDescription = if (passwordVisible) s.hidePassword else s.showPassword,
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = textFieldShape,
            )

            PasswordRequirementsDropdown(password = password)

            Spacer(Modifier.height(9.dp))
            PhoneNumberField(
                value = mobile,
                onValueChange = { mobile = it; error = null },
                selectedCountry = country,
                onCountrySelected = { country = it },
                label = { RequiredLabel(s.mobile) },
                shape = textFieldShape,
            )


            // Date of birth — read-only field that opens the calendar on tap
            Spacer(Modifier.height(9.dp))
            OutlinedTextField(
                value = dob,
                onValueChange = {},
                readOnly = true,
                enabled = false,                       // so the whole field is tappable
                label = { RequiredLabel(s.dob) },
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
                value = street, onValueChange = { street = it.toTitleCase()},
                label = { RequiredLabel(s.street) }, singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = textFieldShape,
            )
            Spacer(Modifier.height(9.dp))
            OutlinedTextField(
                value = city, onValueChange = { city = it.toTitleCase() },
                label = { RequiredLabel(s.city) }, singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = textFieldShape,
            )
            Spacer(Modifier.height(9.dp))
            OutlinedTextField(
                value = postalCode, onValueChange = { postalCode = it },
                label = { RequiredLabel(s.postelCode) }, singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = textFieldShape,
            )

//            // Inline error message
//            if (error != null) {                               // ⬅ ADD
//                Spacer(Modifier.height(12.dp))
//                Text(
//                    error!!,
//                    style = MaterialTheme.typography.bodySmall,
//                    color = MaterialTheme.colorScheme.error,
//                    modifier = Modifier.fillMaxWidth(),
//                )
//            }

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
fun EmailFieldWithSuggestions(
    email: String,
    onEmailChange: (String) -> Unit,
    label: @Composable () -> Unit,
    shape: Shape,
    modifier: Modifier = Modifier,
    colors: TextFieldColors = OutlinedTextFieldDefaults.colors(),
) {
    val domains = listOf("gmail.com", "yahoo.com", "hotmail.com", "outlook.com", "icloud.com", "proton.me")

    var fieldValue by remember { mutableStateOf(TextFieldValue(email, TextRange(email.length))) }
    var justSelected by remember { mutableStateOf(false) }  // ⬅ ADD

    LaunchedEffect(email) {
        if (fieldValue.text != email) {
            fieldValue = TextFieldValue(email, TextRange(email.length))
        }
    }

    val localPart = fieldValue.text.substringBefore("@")
    val afterAt = if (fieldValue.text.contains("@")) fieldValue.text.substringAfter("@") else null

    // ⬅ Reset justSelected only when user fully backspaces to "@"
    LaunchedEffect(afterAt) {
        if (afterAt == null || afterAt.isEmpty()) justSelected = false
    }

    val suggestions = if (
        !justSelected &&                         // ⬅ suppressed after a selection
        fieldValue.text.contains("@") &&
        localPart.isNotBlank() &&
        afterAt != null &&
        !afterAt.contains(".")
    ) {
        domains
            .filter { it.startsWith(afterAt, ignoreCase = true) }
            .map { "$localPart@$it" }
            .filter { it != fieldValue.text }
    } else emptyList()

    ExposedDropdownMenuBox(
        expanded = suggestions.isNotEmpty(),
        onExpandedChange = { },
        modifier = modifier,
    ) {
        OutlinedTextField(
            value = fieldValue,
            onValueChange = {
                fieldValue = it
                onEmailChange(it.text)
            },
            label = label,
            singleLine = true,
            shape = shape,
            modifier = Modifier.menuAnchor().fillMaxWidth(),
            colors = colors,
        )
        MaterialTheme(shapes = MaterialTheme.shapes.copy(extraSmall = RoundedCornerShape(16.dp))) {
            ExposedDropdownMenu(
                expanded = suggestions.isNotEmpty(),
                onDismissRequest = { },
                modifier = Modifier.exposedDropdownSize()
            ) {
                suggestions.forEach { suggestion ->
                    DropdownMenuItem(
                        text = { Text("@${suggestion.substringAfter("@")}") },
                        onClick = {
                            fieldValue = TextFieldValue(suggestion, TextRange(suggestion.length))
                            onEmailChange(suggestion)
                            justSelected = true   // ⬅ suppress dropdown until back at "@"
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun RequiredLabel(text: String) {
    Row {
        Text(text)
        Text(" *", color = MaterialTheme.colorScheme.error)
    }
}

@Composable
private fun PasswordRequirementsDropdown(password: String) {
    val requirements = listOf(
        "At least one uppercase letter" to password.any { it.isUpperCase() },
        "At least one lowercase letter" to password.any { it.isLowerCase() },
        "At least one number"           to password.any { it.isDigit() },
        "At least one special character" to password.any { !it.isLetterOrDigit() },
        "Minimum 6 characters"          to (password.length >= 6),
    )

    val allMet = requirements.all { it.second }

    AnimatedVisibility(
        visible = password.isNotEmpty() && !allMet,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically(),
    ) {
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 6.dp),
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                requirements.forEach { (label, met) ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 4.dp),
                    ) {
                        Icon(
                            imageVector = if (met) Icons.Filled.CheckCircle
                            else Icons.Filled.RadioButtonUnchecked,
                            contentDescription = null,
                            tint = if (met) Color(0xFF4CAF50)
                            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f),
                            modifier = Modifier.size(16.dp),
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(
                            label,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (met) Color(0xFF4CAF50)
                            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        )
                    }
                }
            }
        }
    }
}