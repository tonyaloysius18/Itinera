package com.itinera.app

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import java.util.Locale

class AndroidPlatform : Platform {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"
    override val isIos = false
}

actual fun deviceLanguageCode(): String = Locale.getDefault().language
actual fun getPlatform(): Platform = AndroidPlatform()

actual fun deviceRegion(): String = Locale.getDefault().country   // e.g. "FR", "US"

actual fun prefersReducedMotion(): Boolean = runCatching {
    Settings.Global.getFloat(AndroidApp.context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f
}.getOrDefault(false)

actual fun dial(number: String) {
    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$number")).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)   // launched from non-Activity context
    }
    AndroidApp.context.startActivity(intent)
}