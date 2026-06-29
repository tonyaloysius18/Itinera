package com.itinera.app.ui.components

expect fun cropToRect(
    bytes: ByteArray,
    left: Float, top: Float, right: Float, bottom: Float,
    outWidth: Int, outHeight: Int,
): ByteArray