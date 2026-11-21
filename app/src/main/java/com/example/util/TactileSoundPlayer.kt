package com.example.util

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

class TactileSoundPlayer {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // DRD §12.2: soft pencil scratch
    fun playLineDraw() {
        playSequence(listOf(ToneStep(460.0, 28, 0.20)))
    }

    // DRD §12.2: bright wooden tock + small chime
    fun playBoxClaim() {
        playSequence(
            listOf(
                ToneStep(660.0, 36, 0.28),
                ToneStep(990.0, 60, 0.22),
            )
        )
    }

    // DRD §12.2: soft wooden knock
    fun playTurnChange() {
        playSequence(listOf(ToneStep(340.0, 22, 0.16)))
    }

    // DRD §12.2: two-bar marimba fanfare
    fun playLevelComplete() {
        playSequence(
            listOf(
                ToneStep(523.25, 70, 0.25),
                ToneStep(659.25, 70, 0.25),
                ToneStep(783.99, 110, 0.28),
            )
        )
    }

    // DRD §12.2: celebratory match win jingle
    fun playMatchWin() {
        playSequence(
            listOf(
                ToneStep(523.25, 65, 0.25),
                ToneStep(659.25, 65, 0.25),
                ToneStep(783.99, 65, 0.25),
                ToneStep(1046.50, 140, 0.30),
            )
        )
    }

    // DRD §12.2: soft paper rustle / tap
    fun playChatMessage() {
        playSequence(listOf(ToneStep(590.0, 20, 0.14)))
    }

    // DRD §12.2: rising three-note level-up chime
    fun playLevelUp() {
        playSequence(
            listOf(
                ToneStep(587.33, 65, 0.24),
                ToneStep(739.99, 65, 0.24),
                ToneStep(880.00, 120, 0.28),
            )
        )
    }

    private data class ToneStep(val frequencyHz: Double, val durationMs: Int, val amplitude: Double)

    private fun playSequence(steps: List<ToneStep>) {
        scope.launch {
            runCatching {
                val sampleRate = 22050
                for (step in steps) {
                    val count = (sampleRate * step.durationMs) / 1000
                    val samples = ShortArray(count)
                    for (i in 0 until count) {
                        val t = i.toDouble() / sampleRate
                        val envelope = exp(-t * 55.0)
                        val wave = sin(2.0 * PI * step.frequencyHz * t) * envelope * step.amplitude
                        samples[i] = (wave * Short.MAX_VALUE).toInt().toShort()
                    }
                    val track = AudioTrack.Builder()
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
                        .setTransferMode(AudioTrack.MODE_STATIC)
                        .setBufferSizeInBytes(samples.size * 2)
                        .build()

                    track.write(samples, 0, samples.size)
                    track.play()
                    delay(step.durationMs.toLong() + 18L)
                    track.stop()
                    track.release()
                }
            }
        }
    }
}
