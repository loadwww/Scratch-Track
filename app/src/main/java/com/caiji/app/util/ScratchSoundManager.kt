package com.caiji.app.util

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlin.random.Random

/**
 * 刮刮乐"沙沙"摩擦音效管理器 - 使用 AudioTrack 实时生成白噪声。
 */
object ScratchSoundManager {

    private const val SAMPLE_RATE = 22050
    private var audioTrack: AudioTrack? = null
    private var playThread: Thread? = null
    @Volatile
    private var isPlaying = false

    fun start() {
        if (isPlaying) return
        isPlaying = true
        try {
            val minBuf = AudioTrack.getMinBufferSize(
                SAMPLE_RATE,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            ).coerceAtLeast(1024)

            audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setSampleRate(SAMPLE_RATE)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .build()
                )
                .setBufferSizeInBytes(minBuf)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()

            audioTrack?.play()

            playThread = Thread {
                val noise = ShortArray(minBuf / 2)
                while (isPlaying) {
                    for (i in noise.indices) {
                        // 低音量白噪声模拟"沙沙"声
                        noise[i] = (Random.nextInt(3000) - 1500).toShort()
                    }
                    audioTrack?.write(noise, 0, noise.size)
                }
            }.also { it.start() }
        } catch (_: Exception) {
            isPlaying = false
        }
    }

    fun stop() {
        isPlaying = false
        try {
            playThread?.join(200)
        } catch (_: Exception) {}
        playThread = null
        try {
            audioTrack?.stop()
            audioTrack?.release()
        } catch (_: Exception) {}
        audioTrack = null
    }
}
