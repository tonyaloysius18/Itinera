package com.itinera.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.itinera.app.data.AuthService
import com.itinera.app.i18n.LocalStrings
import com.itinera.app.model.UserProfile
import com.itinera.app.ui.components.PhoneNumberField
import com.itinera.app.ui.components.PlaneLoader
import com.itinera.app.ui.components.countries
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/*
 * ─── New string keys (EN + FR) ──────────────────────────────────────────────
 *   optionalAddLater  "Optional — you can add these later"
 *                     / "Facultatif — vous pourrez les ajouter plus tard"
 *   optionalLabel     "Optional"           / "Facultatif"   (shared)
 *   requiredField     "Required"           / "Obligatoire"
 *   invalidEmail      "Enter a valid e-mail" / "Saisissez un e-mail valide"
 *   alreadyHaveAccount "Already have an account?" / "Vous avez déjà un compte ?"
 *   termsPrefix       "By continuing you agree to our"
 *                     / "En continuant, vous acceptez nos"
 *   termsLabel        "Terms"              / "Conditions"
 *   andLabel          "and"                / "et"
 *   privacyLabel      "Privacy Policy"     / "Politique de confidentialité"
 *   firstName         "First name"         / "Prénom"
 *
 * Reused: createAccount, back, surname, email, password, mobile, dob, address,
 * street, city, postelCode, ok, cancel, signupFailed, showPassword,
 * hidePassword, uppercase, lowercase, number, specialCharacter,
 * minimumCharacters, signIn.
 *
 * No longer used here: fillAllFields, passwordTooShort — validation is inline
 * and per-field now.
 */

/**
 * One page, two halves.
 *
 * The top card is what's actually required — Firebase needs an email and
 * password, and the app needs a name. Everything below it used to be mandatory
 * but isn't read by anything in Itinera: street, city, postal code and date of
 * birth are stored and displayed back, nothing more. Marking them optional
 * removes eight blockers between someone and a working account without hiding
 * the fields from anyone who wants to fill them in.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateAccountScreen(
    authService: AuthService,
    onBack: () -> Unit,
    onCreate: (UserProfile) -> Unit,
    onMessage: (String) -> Unit,
    /** Optional — shows an "Already have an account? Sign in" link. */
    onSignIn: (() -> Unit)? = null,
    /** Optional — Play Store review expects these to be reachable from signup. */
    onOpenTerms: (() -> Unit)? = null,
    onOpenPrivacy: (() -> Unit)? = null,
    /**
     * ISO code for the phone field's default country. Was hardcoded to "US" in
     * a French-market app; pass one derived from the device region.
     */
    defaultCountryCode: String = "US",
) {
    val s = LocalStrings.current
    val scope = rememberCoroutineScope()


    // step 1
    var name by remember { mutableStateOf("") }
    var surname by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var triedSubmit by remember { mutableStateOf(false) }

    // step 2 — all optional
    var mobile by remember { mutableStateOf("") }
    var country by remember {
        mutableStateOf(
            countries.find { it.code == defaultCountryCode } ?: countries.first()
        )
    }
    var dob by remember { mutableStateOf("") }
    var street by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var postalCode by remember { mutableStateOf("") }

    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()
    var loading by remember { mutableStateOf(false) }

    // ⬅ CHANGED — cased once when the profile is built. Per keystroke it fought
    // anyone typing "van der Berg" or a lowercase street name.
    fun String.toTitleCase(): String =
        split(" ").joinToString(" ") { word ->
            word.replaceFirstChar { c -> if (c.isLowerCase()) c.titlecase() else c.toString() }
        }

    // ⬅ CHANGED — per-field errors shown under the field they belong to, rather
    // than one pill at a time on submit. The old flow made you press Create up
    // to five times to discover five problems.
    val emailLooksValid = email.contains("@") && email.substringAfter("@").contains(".")
    val pwUpper = password.any { it.isUpperCase() }
    val pwLower = password.any { it.isLowerCase() }
    val pwDigit = password.any { it.isDigit() }
    val pwSpecial = password.any { !it.isLetterOrDigit() }
    val pwLength = password.length >= 6
    val passwordOk = pwUpper && pwLower && pwDigit && pwSpecial && pwLength

    val nameError = if (triedSubmit && name.isBlank()) s.requiredField else null
    val surnameError = if (triedSubmit && surname.isBlank()) s.requiredField else null
    val emailError = when {
        !triedSubmit -> null
        email.isBlank() -> s.requiredField
        !emailLooksValid -> s.invalidEmail
        else -> null
    }
    val requiredValid = name.isNotBlank() && surname.isNotBlank() && emailLooksValid && passwordOk

    fun buildProfile() = UserProfile(
        name = name.trim().toTitleCase(),
        surname = surname.trim().toTitleCase(),
        email = email.trim(),
        mobile = if (mobile.isBlank()) "" else country.dialCode + mobile.trim(),
        dob = dob,
        street = street.trim().toTitleCase(),
        city = city.trim().toTitleCase(),
        postalCode = postalCode.trim(),
    )

    fun attemptCreate() {
        triedSubmit = true
        // ⬅ CHANGED — was a chain of `when` branches each calling onMessage and
        // returning, so you learned about one problem per button press. The
        // errors are on the fields now; this just stops the submit.
        if (!requiredValid) return
        loading = true
        scope.launch {
            try {
                authService.signUp(email.trim(), password)
                loading = false
                onCreate(buildProfile())
            } catch (e: Exception) {
                loading = false
                // Email in use / weak password / network. Firebase's own text
                // isn't for users, so the detail goes to the log.
                onMessage(s.signupFailed)
                println("ITINERA: SIGNUP FAILED — ${e.message}")
            }
        }
    }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().statusBarsPadding()) {
            Row(
                Modifier.fillMaxWidth().padding(start = 4.dp, end = 16.dp, top = 4.dp, bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = s.back)
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
                    .verticalScroll(rememberScrollState())
                    .imePadding()
                    .padding(horizontal = 20.dp),
            ) {
                // ── Required ──────────────────────────────────────────────
                SignupCard {
                    // Short fields side by side — three rows saved.
                    Row(Modifier.height(IntrinsicSize.Min)) {
                        SignupField(
                            label = s.firstName,
                            value = name,
                            error = nameError,
                            modifier = Modifier.weight(1f),
                        ) { name = it }
                        VerticalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.10f))
                        SignupField(
                            label = s.surname,
                            value = surname,
                            error = surnameError,
                            modifier = Modifier.weight(1f),
                        ) { surname = it }
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.10f))

                    // Keeps the domain autocomplete, which is genuinely good.
                    Column(Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                        Text(
                            s.email,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (emailError != null) MaterialTheme.colorScheme.error
                            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        )
                        EmailFieldWithSuggestions(
                            email = email,
                            onEmailChange = { email = it },
                            modifier = Modifier.fillMaxWidth(),
                        )
                        if (emailError != null) FieldError(emailError)
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.10f))

                    Row(
                        Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                s.password,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            )
                            BasicTextField(
                                value = password,
                                onValueChange = { password = it },
                                singleLine = true,
                                textStyle = MaterialTheme.typography.bodyLarge.copy(
                                    color = MaterialTheme.colorScheme.onSurface,
                                ),
                                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                                visualTransformation = if (passwordVisible) VisualTransformation.None
                                else PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                        Icon(
                            if (passwordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                            contentDescription = if (passwordVisible) s.hidePassword else s.showPassword,
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            modifier = Modifier.size(20.dp).clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() },
                            ) { passwordVisible = !passwordVisible },
                        )
                    }
                }

                PasswordRequirementsDropdown(password = password)

                // ── Optional ──────────────────────────────────────────────
                // ⬅ CHANGED — these were all required. Nothing in Itinera reads
                // them; they're stored and displayed back. Same page, clearly
                // marked, so anyone who wants to fill them still can.
                Text(
                    s.optionalAddLater,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    modifier = Modifier.padding(start = 2.dp, top = 26.dp, bottom = 8.dp),
                )

                SignupCard {
                    Column(Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                        Text(
                            s.mobile,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        )
                        PhoneNumberField(
                            value = mobile,
                            onValueChange = { mobile = it },
                            selectedCountry = country,
                            onCountrySelected = { country = it },
                            label = { Text(s.optionalLabel) },
                            shape = RoundedCornerShape(12.dp),
                        )
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.10f))
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clickable { showDatePicker = true }
                            .padding(horizontal = 14.dp, vertical = 11.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                s.dob,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            )
                            Text(
                                dob.ifBlank { s.optionalLabel },
                                style = MaterialTheme.typography.bodyLarge,
                                color = if (dob.isBlank())
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                                else MaterialTheme.colorScheme.onSurface,
                            )
                        }
                        Icon(
                            Icons.Filled.CalendarMonth,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }

                Text(
                    s.address.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    modifier = Modifier.padding(start = 2.dp, top = 20.dp, bottom = 8.dp),
                )
                SignupCard {
                    SignupField(
                        label = s.street,
                        value = street,
                        placeholder = s.optionalLabel,
                    ) { street = it }
                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.10f))
                    Row(Modifier.height(IntrinsicSize.Min)) {
                        SignupField(
                            label = s.city,
                            value = city,
                            placeholder = s.optionalLabel,
                            modifier = Modifier.weight(1.6f),
                        ) { city = it }
                        VerticalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.10f))
                        SignupField(
                            label = s.postelCode,
                            value = postalCode,
                            placeholder = s.optionalLabel,
                            keyboardType = KeyboardType.Number,
                            modifier = Modifier.weight(1f),
                        ) { postalCode = it }
                    }
                }

                Spacer(Modifier.height(150.dp))
            }
        }

        Column(
            Modifier.align(Alignment.BottomCenter).fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(top = 16.dp, bottom = 32.dp)
                .imePadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Button(
                onClick = { attemptCreate() },
                enabled = !loading,
                shape = RoundedCornerShape(26.dp),
                modifier = Modifier.fillMaxWidth().height(52.dp),
            ) {
                if (loading) PlaneLoader() else Text(s.createAccount)
            }

//            // ⬅ ADD — Play Store review expects these reachable from signup.
//            Spacer(Modifier.height(12.dp))
//            Row(
//                horizontalArrangement = Arrangement.Center,
//                verticalAlignment = Alignment.CenterVertically,
//            ) {
//                Text(
//                    s.termsPrefix + " ",
//                    style = MaterialTheme.typography.labelSmall,
//                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
//                    textAlign = TextAlign.Center,
//                )
//                Text(
//                    s.termsLabel,
//                    style = MaterialTheme.typography.labelSmall,
//                    color = MaterialTheme.colorScheme.primary,
//                    modifier = Modifier.clickable(enabled = onOpenTerms != null) { onOpenTerms?.invoke() },
//                )
//                Text(
//                    " ${s.andLabel} ",
//                    style = MaterialTheme.typography.labelSmall,
//                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
//                )
//                Text(
//                    s.privacyLabel,
//                    style = MaterialTheme.typography.labelSmall,
//                    color = MaterialTheme.colorScheme.primary,
//                    modifier = Modifier.clickable(enabled = onOpenPrivacy != null) { onOpenPrivacy?.invoke() },
//                )
//            }

            if (onSignIn != null) {
                Spacer(Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        s.alreadyHaveAccount,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    )
                    Spacer(Modifier.width(5.dp))
                    Text(
                        s.signIn,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable { onSignIn() },
                    )
                }
            }
        }
    }

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
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text(s.cancel) } },
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun SignupCard(content: @Composable ColumnScope.() -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Column(content = content)
    }
}

@Composable
private fun SignupField(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    error: String? = null,
    placeholder: String = "",
    keyboardType: KeyboardType = KeyboardType.Text,
    onValueChange: (String) -> Unit,
) {
    val onSurface = MaterialTheme.colorScheme.onSurface
    Column(modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = if (error != null) MaterialTheme.colorScheme.error
            else onSurface.copy(alpha = 0.5f),
        )
        Spacer(Modifier.height(1.dp))
        Box {
            if (value.isEmpty() && placeholder.isNotEmpty()) {
                Text(
                    placeholder,
                    style = MaterialTheme.typography.bodyLarge,
                    color = onSurface.copy(alpha = 0.35f),
                )
            }
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
        if (error != null) FieldError(error)
    }
}

@Composable
private fun FieldError(message: String) {
    Text(
        message,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.error,
        modifier = Modifier.padding(top = 2.dp),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmailFieldWithSuggestions(
    email: String,
    onEmailChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    /**
     * Override for surfaces that aren't themed — the login panel sits on a
     * photo and is dark regardless of the app's light/dark setting, so
     * onSurface would be invisible there in light mode.
     */
    textColor: Color? = null,
) {
    val domains = listOf("gmail.com", "yahoo.com", "hotmail.com", "outlook.com", "icloud.com", "proton.me")

    var fieldValue by remember { mutableStateOf(TextFieldValue(email, TextRange(email.length))) }
    var justSelected by remember { mutableStateOf(false) }

    LaunchedEffect(email) {
        if (fieldValue.text != email) {
            fieldValue = TextFieldValue(email, TextRange(email.length))
        }
    }

    val localPart = fieldValue.text.substringBefore("@")
    val afterAt = if (fieldValue.text.contains("@")) fieldValue.text.substringAfter("@") else null

    LaunchedEffect(afterAt) {
        if (afterAt == null || afterAt.isEmpty()) justSelected = false
    }

    val suggestions = if (
        !justSelected &&
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
        // ⬅ CHANGED — borderless now, so it sits inside the card like the others
        // instead of drawing a second outline within one.
        BasicTextField(
            value = fieldValue,
            onValueChange = {
                fieldValue = it
                onEmailChange(it.text)
            },
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyLarge.copy(
                color = textColor ?: MaterialTheme.colorScheme.onSurface,
            ),
            cursorBrush = SolidColor(textColor ?: MaterialTheme.colorScheme.primary),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.menuAnchor().fillMaxWidth(),
        )
        MaterialTheme(shapes = MaterialTheme.shapes.copy(extraSmall = RoundedCornerShape(16.dp))) {
            ExposedDropdownMenu(
                expanded = suggestions.isNotEmpty(),
                onDismissRequest = { },
                modifier = Modifier.exposedDropdownSize(),
            ) {
                suggestions.forEach { suggestion ->
                    DropdownMenuItem(
                        text = { Text("@${suggestion.substringAfter("@")}") },
                        onClick = {
                            fieldValue = TextFieldValue(suggestion, TextRange(suggestion.length))
                            onEmailChange(suggestion)
                            justSelected = true
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun PasswordRequirementsDropdown(password: String) {
    val s = LocalStrings.current
    val requirements = listOf(
        s.uppercase to password.any { it.isUpperCase() },
        s.lowercase to password.any { it.isLowerCase() },
        s.number to password.any { it.isDigit() },
        s.specialCharacter to password.any { !it.isLetterOrDigit() },
        s.minimumCharacters to (password.length >= 6),
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
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
            ),
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        ) {
            Column(Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
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