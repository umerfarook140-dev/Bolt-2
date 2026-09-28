package com.example.engine

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

class SoundEngine {
    private val scope = CoroutineScope(Dispatchers.Default)
    private val sampleRate = 44100
    private var isSoundEnabled = true
    private var isMusicEnabled = true

    fun setSoundEnabled(enabled: Boolean) {
        isSoundEnabled = enabled
    }

    fun setMusicEnabled(enabled: Boolean) {
        isMusicEnabled = enabled
    }

    private fun playPcm(samples: ShortArray) {
        if (!isSoundEnabled) return
        scope.launch {
            try {
                val minBufferSize = AudioTrack.getMinBufferSize(
                    sampleRate,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT
                )
                val bufferSize = maxOf(samples.size * 2, minBufferSize)
                val audioTrack = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_GAME)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(bufferSize)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                audioTrack.write(samples, 0, samples.size)
                audioTrack.play()
                // Auto release after sound duration
                kotlinx.coroutines.delay((samples.size * 1000L / sampleRate) + 50)
                audioTrack.release()
            } catch (_: Exception) {
                // Ignore audio track fallback
            }
        }
    }

    fun playTap() {
        val durationMs = 30
        val count = (sampleRate * durationMs) / 1000
        val buffer = ShortArray(count)
        for (i in 0 until count) {
            val t = i.toDouble() / sampleRate
            val freq = 800.0 - (t * 6000.0)
            val envelope = exp(-t * 80.0)
            val sample = sin(2.0 * PI * freq * t) * envelope * 0.4
            buffer[i] = (sample * Short.MAX_VALUE).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        playPcm(buffer)
    }

    fun playUnscrewFast() {
        // Snappy, fast 0.18s unscrew spinning sound (pitch ramping with metallic clicks)
        val durationMs = 180
        val count = (sampleRate * durationMs) / 1000
        val buffer = ShortArray(count)
        for (i in 0 until count) {
            val t = i.toDouble() / sampleRate
            val progress = t / (durationMs / 1000.0)
            val freq = 450.0 + (progress * 850.0)
            val mod = sin(2.0 * PI * 60.0 * t) // fast mechanical ratchet modulation
            val env = sin(PI * progress.coerceIn(0.0, 1.0))
            val sample = sin(2.0 * PI * freq * t + mod * 0.8) * env * 0.45
            buffer[i] = (sample * Short.MAX_VALUE).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        playPcm(buffer)
    }

    fun playToolboxSnap() {
        // Metallic positive snap when bolt lands in toolbox
        val durationMs = 90
        val count = (sampleRate * durationMs) / 1000
        val buffer = ShortArray(count)
        for (i in 0 until count) {
            val t = i.toDouble() / sampleRate
            val freq1 = 1200.0
            val freq2 = 2400.0
            val env = exp(-t * 60.0)
            val sample = (0.6 * sin(2.0 * PI * freq1 * t) + 0.4 * sin(2.0 * PI * freq2 * t)) * env * 0.5
            buffer[i] = (sample * Short.MAX_VALUE).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        playPcm(buffer)
    }

    fun playToolboxComplete() {
        // 3-tone arpeggio fanfare for 3 matching screws completion
        val durationMs = 320
        val count = (sampleRate * durationMs) / 1000
        val buffer = ShortArray(count)
        for (i in 0 until count) {
            val t = i.toDouble() / sampleRate
            val phase = (t / 0.1).toInt()
            val freq = when (phase) {
                0 -> 523.25 // C5
                1 -> 659.25 // E5
                else -> 783.99 // G5
            }
            val localT = t % 0.1
            val env = exp(-localT * 25.0)
            val sample = sin(2.0 * PI * freq * t) * env * 0.45
            buffer[i] = (sample * Short.MAX_VALUE).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        playPcm(buffer)
    }

    fun playPieceFall() {
        // Low rumble swoosh/drop sound when panel falls downward
        val durationMs = 260
        val count = (sampleRate * durationMs) / 1000
        val buffer = ShortArray(count)
        for (i in 0 until count) {
            val t = i.toDouble() / sampleRate
            val freq = 220.0 - (t * 400.0).coerceAtMost(160.0)
            val noise = (Math.random() * 2.0 - 1.0) * 0.2
            val env = exp(-t * 12.0)
            val sample = (sin(2.0 * PI * freq * t) * 0.8 + noise) * env * 0.4
            buffer[i] = (sample * Short.MAX_VALUE).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        playPcm(buffer)
    }

    fun playLevelComplete() {
        // Glorious victory chord
        val durationMs = 600
        val count = (sampleRate * durationMs) / 1000
        val buffer = ShortArray(count)
        val freqs = listOf(523.25, 659.25, 783.99, 1046.50) // C-E-G-C chord
        for (i in 0 until count) {
            val t = i.toDouble() / sampleRate
            var sum = 0.0
            for ((idx, f) in freqs.withIndex()) {
                val delay = idx * 0.08
                if (t >= delay) {
                    val localT = t - delay
                    sum += sin(2.0 * PI * f * localT) * exp(-localT * 5.0) * 0.2
                }
            }
            buffer[i] = (sum * Short.MAX_VALUE).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        playPcm(buffer)
    }

    fun playMistake() {
        // Error buzzer click
        val durationMs = 120
        val count = (sampleRate * durationMs) / 1000
        val buffer = ShortArray(count)
        for (i in 0 until count) {
            val t = i.toDouble() / sampleRate
            val freq = 140.0
            val env = exp(-t * 20.0)
            val sample = sin(2.0 * PI * freq * t) * env * 0.4
            buffer[i] = (sample * Short.MAX_VALUE).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        playPcm(buffer)
    }

    fun playBooster() {
        // Magic power-up whoosh
        val durationMs = 220
        val count = (sampleRate * durationMs) / 1000
        val buffer = ShortArray(count)
        for (i in 0 until count) {
            val t = i.toDouble() / sampleRate
            val freq = 300.0 + (t / 0.22) * 1200.0
            val env = exp(-t * 8.0)
            val sample = sin(2.0 * PI * freq * t) * env * 0.4
            buffer[i] = (sample * Short.MAX_VALUE).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        playPcm(buffer)
    }
}
