package com.itinera.app

interface Platform {
    val name: String
    val isIos: Boolean
}
expect fun deviceLanguageCode(): String
expect fun getPlatform(): Platform