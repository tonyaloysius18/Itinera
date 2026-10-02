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
        handleShareLink(intent)                               // cold start from an itinera://s/<id> link

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
        handleShareLink(intent)
    }

    /** itinera://s/<id> and https://itinera-ae020.web.app/s/<id> open a shared itinerary. */
    private fun handleShareLink(intent: Intent?) {
        val uri = intent?.data ?: return
        val segments = uri.pathSegments
        val id = when {
            uri.scheme == "itinera" && uri.host == "s" -> segments.firstOrNull()
            uri.scheme == "https" && uri.host == "itinera-ae020.web.app" && segments.firstOrNull() == "s" -> segments.getOrNull(1)
            else -> null
        }
        id?.takeIf { it.isNotBlank() }?.let { PendingDeepLink.sharedId = it }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}
