package com.example.domain

import android.content.Context
import android.hardware.camera2.CameraManager
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaRecorder
import android.os.Build
import android.os.CombinedVibration
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.sin
import kotlin.math.sqrt

class HardwareTestController(private val context: Context) {

    private val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
    private var isFlashOn = false

    fun vibrateDevice() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                val effect = VibrationEffect.createWaveform(
                    longArrayOf(0, 200, 100, 250, 100, 300),
                    intArrayOf(0, 180, 0, 220, 0, 255),
                    -1
                )
                vibratorManager?.vibrate(CombinedVibration.createParallel(effect))
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(VibrationEffect.createOneShot(400, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(400)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun playDiagnosticTone(isEarpiece: Boolean = false) = withContext(Dispatchers.IO) {
        val sampleRate = 44100
        val durationSeconds = 1.2
        val numSamples = (durationSeconds * sampleRate).toInt()
        val sample = DoubleArray(numSamples)
        val generatedSnd = ByteArray(2 * numSamples)
        val freq = if (isEarpiece) 1000.0 else 520.0

        for (i in 0 until numSamples) {
            sample[i] = sin(2.0 * Math.PI * i / (sampleRate / freq))
        }

        var idx = 0
        for (dVal in sample) {
            val valShort = (dVal * 32767).toInt().toShort()
            generatedSnd[idx++] = (valShort.toInt() and 0x00ff).toByte()
            generatedSnd[idx++] = ((valShort.toInt() and 0xff00) ushr 8).toByte()
        }

        try {
            val streamType = if (isEarpiece) AudioManager.STREAM_VOICE_CALL else AudioManager.STREAM_MUSIC
            val audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(if (isEarpiece) AudioAttributes.USAGE_VOICE_COMMUNICATION else AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(generatedSnd.size)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            audioTrack.write(generatedSnd, 0, generatedSnd.size)
            audioTrack.play()
            kotlinx.coroutines.delay(1300)
            audioTrack.stop()
            audioTrack.release()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun toggleTorch(): Boolean {
        try {
            if (cameraManager != null) {
                val cameraId = cameraManager.cameraIdList.firstOrNull { id ->
                    val characteristics = cameraManager.getCameraCharacteristics(id)
                    characteristics.get(android.hardware.camera2.CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
                }
                if (cameraId != null) {
                    isFlashOn = !isFlashOn
                    cameraManager.setTorchMode(cameraId, isFlashOn)
                    return isFlashOn
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    suspend fun recordAndAnalyzeMicrophone(durationMillis: Long = 3000): Float = withContext(Dispatchers.IO) {
        val sampleRate = 44100
        val bufferSize = AudioRecord.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )

        if (bufferSize <= 0) return@withContext 0f

        var audioRecord: AudioRecord? = null
        try {
            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                sampleRate,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                bufferSize
            )

            if (audioRecord.state != AudioRecord.STATE_INITIALIZED) {
                return@withContext 0f
            }

            audioRecord.startRecording()
            val buffer = ShortArray(bufferSize)
            val startTime = System.currentTimeMillis()
            var maxAmplitude = 0f
            var sumSquares = 0.0
            var sampleCount = 0

            while (System.currentTimeMillis() - startTime < durationMillis) {
                val read = audioRecord.read(buffer, 0, buffer.size)
                if (read > 0) {
                    for (i in 0 until read) {
                        val absVal = kotlin.math.abs(buffer[i].toInt())
                        if (absVal > maxAmplitude) maxAmplitude = absVal.toFloat()
                        sumSquares += (buffer[i] * buffer[i]).toDouble()
                        sampleCount++
                    }
                }
                kotlinx.coroutines.delay(50)
            }

            audioRecord.stop()
            val rms = if (sampleCount > 0) sqrt(sumSquares / sampleCount).toFloat() else 0f
            return@withContext rms
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext 0f
        } finally {
            try {
                audioRecord?.release()
            } catch (e: Exception) {
                // Ignore
            }
        }
    }
}
