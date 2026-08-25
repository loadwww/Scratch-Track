package com.caiji.app.util

import android.content.Context
import android.media.MediaPlayer
import com.caiji.app.data.ServiceLocator
import kotlinx.coroutines.flow.first
import java.io.File
import android.os.Handler
import android.os.Looper

/**
 * 盈利音乐播放器 - 从第6秒开始播放10秒。
 * 支持内置《好运来》和自定义音乐文件。
 */
object MusicPlayer {

    private var mediaPlayer: MediaPlayer? = null
    private val handler = Handler(Looper.getMainLooper())
    private var stopRunnable: Runnable? = null

    /**
     * 盈利时播放音乐。从第6秒开始播放10秒。
     * 读取设置判断是否启用以及使用哪个音频文件。
     */
    fun playOnProfit(context: Context) {
        try {
            val settings = kotlinx.coroutines.runBlocking {
                ServiceLocator.provideSettings(context).settings.first()
            }
            if (!settings.musicEnabled) return

            stop()

            val path = settings.musicPath
            mediaPlayer = if (path.isNotBlank() && File(path).exists()) {
                MediaPlayer().apply {
                    setDataSource(path)
                    prepare()
                    seekTo(6000)
                    start()
                }
            } else {
                // 无自定义文件时，用内置 raw 资源
                val resId = context.resources.getIdentifier("haoyunlai", "raw", context.packageName)
                if (resId != 0) {
                    MediaPlayer.create(context, resId)?.apply {
                        seekTo(6000)
                        start()
                    }
                } else null
            }

            // 10秒后自动停止
            mediaPlayer?.let { mp ->
                stopRunnable = Runnable {
                    try {
                        if (mp.isPlaying) mp.stop()
                        mp.release()
                    } catch (_: Exception) {}
                    mediaPlayer = null
                }
                handler.postDelayed(stopRunnable!!, 10000)
            }
        } catch (_: Exception) {
            // 播放失败不影响正常流程
        }
    }

    fun stop() {
        stopRunnable?.let { handler.removeCallbacks(it) }
        stopRunnable = null
        mediaPlayer?.let {
            try {
                if (it.isPlaying) it.stop()
                it.release()
            } catch (_: Exception) {}
        }
        mediaPlayer = null
    }
}
