package com.itinera.app

interface Platform {
    val name: String
    val isIos: Boolean
}

expect fun getPlatform(): Platform