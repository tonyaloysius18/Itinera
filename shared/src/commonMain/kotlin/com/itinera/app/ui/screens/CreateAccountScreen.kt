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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.itinera.app.i18n.LocalStrings
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
//import kotlin.time.Instant

/**
 * Collects the user's details to create an account. Mock for now — onCreate()
 * just proceeds. To make it real, call Firebase Auth createUserWithEmailAndPassword
 * here and save the name to the user profile.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateAccountScreen(
    onBack: () -> Unit,
    onCreate: () -> Unit,
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 24.dp),
    ) {
        Spacer(Modifier.statusBarsPadding())
        Text(
            s.createAccount,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp),
        )


        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
        ) {
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = name, onValueChange = { name = it },
                label = { Text(s.name) }, singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = textFieldShape,
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = surname, onValueChange = { surname = it },
                label = { Text(s.surname) }, singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = textFieldShape,
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = email, onValueChange = { email = it },
                label = { Text(s.email) }, singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = textFieldShape,
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = password, onValueChange = { password = it },
                label = { Text(s.password) }, singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                shape = textFieldShape,
            )

            // Date of birth — read-only field that opens the calendar on tap
            Spacer(Modifier.height(12.dp))
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
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = street, onValueChange = { street = it },
                label = { Text(s.street) }, singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = textFieldShape,
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = city, onValueChange = { city = it },
                label = { Text(s.city) }, singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = textFieldShape,
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = postalCode, onValueChange = { postalCode = it },
                label = { Text(s.postelCode) }, singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = textFieldShape,
            )

            Spacer(Modifier.height(24.dp))
            Button(onClick = onCreate, modifier = Modifier.fillMaxWidth()) {
                Text(s.createAccount)
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