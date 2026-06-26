package com.itinera.app

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

class MainActivity : ComponentActivity() {

    // Registered launcher for the POST_NOTIFICATIONS runtime permission (Android 13+).
    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { /* granted: Boolean */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        AndroidApp.init(this)

        intent?.getStringExtra("tripId")?.takeIf { it.isNotBlank() }?.let {
            PendingDeepLink.tripId = it                       // ⬅ cold start from notification
        }

        // Let shared code trigger the system permission dialog.
        NotificationPermission.requester = {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        setContent {
            App()
        }
    }
    override fun onNewIntent(intent: Intent) {                // ⬅ ADD — app already running
        super.onNewIntent(intent)
        setIntent(intent)
        intent.getStringExtra("tripId")?.takeIf { it.isNotBlank() }?.let {
            PendingDeepLink.tripId = it
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}