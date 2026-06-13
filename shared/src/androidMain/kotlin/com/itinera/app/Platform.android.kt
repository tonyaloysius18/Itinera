package com.itinera.app

import android.os.Build
import java.util.Locale

class AndroidPlatform : Platform {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"
    override val isIos = false

}
actual fun deviceLanguageCode(): String = Locale.getDefault().language

actual fun getPlatform(): Platform = AndroidPlatform()