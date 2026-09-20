package com.example.domain

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.example.util.SafeLog
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.util.Locale
import kotlin.math.sqrt

data class SensorReading(
    val name: String,
    val type: Int,
    val isAvailable: Boolean,
    val values: List<Float> = emptyList(),
    val displayValue: String = "Inactive",
    val magnitude: Float = 0f
)

class SensorMonitor(private val context: Context) {
    private val sensorManager: SensorManager? by lazy {
        try {
            context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        } catch (t: Throwable) {
            SafeLog.w("SensorService not accessible", t)
            null
        }
    }

    fun hasSensor(type: Int): Boolean {
        return try {
            sensorManager?.getDefaultSensor(type) != null
        } catch (_: Throwable) {
            false
        }
    }

    fun observeSensors(): Flow<Map<Int, SensorReading>> = callbackFlow {
        val sm = sensorManager
        if (sm == null) {
            trySend(emptyMap())
            close()
            return@callbackFlow
        }

        val sensorTypes = listOf(
            Sensor.TYPE_ACCELEROMETER,
            Sensor.TYPE_GYROSCOPE,
            Sensor.TYPE_MAGNETIC_FIELD,
            Sensor.TYPE_LIGHT,
            Sensor.TYPE_PROXIMITY,
            Sensor.TYPE_PRESSURE,
            Sensor.TYPE_ROTATION_VECTOR
        )

        val activeReadings = mutableMapOf<Int, SensorReading>()

        // Initialize default availability
        for (type in sensorTypes) {
            val sensor = try { sm.getDefaultSensor(type) } catch (_: Throwable) { null }
            val name = when (type) {
                Sensor.TYPE_ACCELEROMETER -> "Accelerometer"
                Sensor.TYPE_GYROSCOPE -> "Gyroscope"
                Sensor.TYPE_MAGNETIC_FIELD -> "Magnetometer"
                Sensor.TYPE_LIGHT -> "Light Sensor"
                Sensor.TYPE_PROXIMITY -> "Proximity Sensor"
                Sensor.TYPE_PRESSURE -> "Barometer (Pressure)"
                Sensor.TYPE_ROTATION_VECTOR -> "Rotation Vector"
                else -> "Sensor $type"
            }
            activeReadings[type] = SensorReading(
                name = name,
                type = type,
                isAvailable = sensor != null,
                displayValue = if (sensor != null) "Ready" else "Not available on this device"
            )
        }
        trySend(activeReadings.toMap())

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                if (event == null) return
                try {
                    val type = event.sensor.type
                    val vals = event.values.toList()

                    val (displayStr, mag) = when (type) {
                        Sensor.TYPE_ACCELEROMETER -> {
                            val magVal = sqrt(event.values[0] * event.values[0] + event.values[1] * event.values[1] + event.values[2] * event.values[2])
                            Pair(String.format(Locale.US, "X: %.1f, Y: %.1f, Z: %.1f m/s²", event.values[0], event.values[1], event.values[2]), magVal)
                        }
                        Sensor.TYPE_GYROSCOPE -> {
                            Pair(String.format(Locale.US, "X: %.2f, Y: %.2f, Z: %.2f rad/s", event.values[0], event.values[1], event.values[2]), 0f)
                        }
                        Sensor.TYPE_MAGNETIC_FIELD -> {
                            val magVal = sqrt(event.values[0] * event.values[0] + event.values[1] * event.values[1] + event.values[2] * event.values[2])
                            Pair(String.format(Locale.US, "%.1f µT (Heading)", magVal), magVal)
                        }
                        Sensor.TYPE_LIGHT -> {
                            Pair(String.format(Locale.US, "%.0f Lux", event.values[0]), event.values[0])
                        }
                        Sensor.TYPE_PROXIMITY -> {
                            val dist = event.values[0]
                            val state = if (dist < 3.0f) "NEAR (${dist.toInt()} cm)" else "FAR (${dist.toInt()} cm)"
                            Pair(state, dist)
                        }
                        Sensor.TYPE_PRESSURE -> {
                            Pair(String.format(Locale.US, "%.1f hPa", event.values[0]), event.values[0])
                        }
                        Sensor.TYPE_ROTATION_VECTOR -> {
                            Pair("Active Orientation", 1f)
                        }
                        else -> Pair("Active", 0f)
                    }

                    val current = activeReadings[type]
                    if (current != null) {
                        activeReadings[type] = current.copy(
                            values = vals,
                            displayValue = displayStr,
                            magnitude = mag
                        )
                        trySend(activeReadings.toMap())
                    }
                } catch (t: Throwable) {
                    SafeLog.w("Error processing sensor event", t)
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        for (type in sensorTypes) {
            try {
                val sensor = sm.getDefaultSensor(type)
                if (sensor != null) {
                    sm.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_UI)
                }
            } catch (t: Throwable) {
                SafeLog.w("Could not register listener for sensor type $type", t)
            }
        }

        awaitClose {
            try {
                sm.unregisterListener(listener)
            } catch (t: Throwable) {
                SafeLog.w("Error unregistering sensor listener", t)
            }
        }
    }
}
