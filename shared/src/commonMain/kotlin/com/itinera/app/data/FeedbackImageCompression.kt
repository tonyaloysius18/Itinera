package com.itinera.app.data

/**
 * Downscales a selected screenshot while preserving its aspect ratio, converts
 * it to JPEG, and strips metadata such as EXIF location before it is submitted.
 */
expect fun compressFeedbackImage(
    bytes: ByteArray,
    maxDimension: Int = 1600,
    quality: Int = 82,
): ByteArray
