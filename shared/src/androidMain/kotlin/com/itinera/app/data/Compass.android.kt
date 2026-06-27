package com.itinera.app.data

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.itinera.app.AndroidApp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Android compass via SensorManager's ROTATION_VECTOR (sensor-fused heading,
 * smoother and more accurate than raw magnetometer+accelerometer). No permission.
 */
actual class Compass actual constructor() {

    private val _heading = MutableStateFlow(0f)
    actual val heading: StateFlow<Float> = _heading.asStateFlow()

    private val _available = MutableStateFlow(false)
    actual val available: StateFlow<Boolean> = _available.asStateFlow()

    private val sensorManager: SensorManager? =
        AndroidApp.context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val rotationSensor: Sensor? =
        sensorManager?.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)

    private val rotationMatrix = FloatArray(9)
    private val orientation = FloatArray(3)

    private val listener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            if (event.sensor.type != Sensor.TYPE_ROTATION_VECTOR) return
            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
            SensorManager.getOrientation(rotationMatrix, orientation)
            // orientation[0] = azimuth in radians (-π..π); convert to 0..360
            val azimuthDeg = Math.toDegrees(orientation[0].toDouble()).toFloat()
            _heading.value = ((azimuthDeg % 360f) + 360f) % 360f
            if (!_available.value) _available.value = true
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    }

    actual fun start() {
        val sm = sensorManager ?: return
        val sensor = rotationSensor ?: return
        sm.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_UI)
    }

    actual fun stop() {
        sensorManager?.unregisterListener(listener)
    }
}