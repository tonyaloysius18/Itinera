package com.itinera.app.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import platform.CoreLocation.CLHeading
import platform.CoreLocation.CLLocationManager
import platform.CoreLocation.CLLocationManagerDelegateProtocol
import platform.CoreLocation.kCLHeadingFilterNone
import platform.darwin.NSObject

/**
 * iOS compass via CLLocationManager heading updates. Requires location-when-in-use
 * authorization (Apple ties heading to the location manager) and an
 * NSLocationWhenInUseUsageDescription entry in Info.plist.
 */
actual class Compass actual constructor() {

    private val _heading = MutableStateFlow(0f)
    actual val heading: StateFlow<Float> = _heading.asStateFlow()

    private val _available = MutableStateFlow(false)
    actual val available: StateFlow<Boolean> = _available.asStateFlow()

    private val manager = CLLocationManager()

    private val delegate = object : NSObject(), CLLocationManagerDelegateProtocol {
        override fun locationManager(manager: CLLocationManager, didUpdateHeading: CLHeading) {
            // Use magnetic heading (0..360, where 0 = magnetic north).
            val h = didUpdateHeading.magneticHeading
            if (h >= 0.0) {
                _heading.value = ((h % 360.0) + 360.0).rem(360.0).toFloat()
                if (!_available.value) _available.value = true
            }
        }
    }

    actual fun start() {
        manager.delegate = delegate
        manager.headingFilter = kCLHeadingFilterNone   // report all changes
        manager.requestWhenInUseAuthorization()
        if (CLLocationManager.headingAvailable()) {
            manager.startUpdatingHeading()
        }
    }

    actual fun stop() {
        manager.stopUpdatingHeading()
    }
}