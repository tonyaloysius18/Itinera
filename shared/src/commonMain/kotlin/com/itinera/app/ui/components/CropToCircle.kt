package com.itinera.app.ui.components

expect fun cropToCircle(
    bytes: ByteArray,
    left: Float = 0f, top: Float = 0f, right: Float = 1f, bottom: Float = 1f,
    size: Int = 512
): ByteArray
