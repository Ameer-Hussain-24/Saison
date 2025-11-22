package takagi.ru.saison.util

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.media.ToneGenerator
import android.media.AudioManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 番茄钟通知管理器
 * 负责播放番茄钟完成时的声音提醒
 */
@Singleton
class PomodoroNotificationManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var soundPool: SoundPool? = null
    private var completionSoundId: Int = -1
    
    /**
     * 初始化音频资源
     */
    fun initialize() {
        if (soundPool != null) return
        
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_NOTIFICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        
        soundPool = SoundPool.Builder()
            .setMaxStreams(1)
            .setAudioAttributes(audioAttributes)
            .build()
        
        // TODO: 如果有自定义音频文件，可以在这里加载
        // completionSoundId = soundPool?.load(context, R.raw.pomodoro_complete, 1) ?: -1
    }
    
    /**
     * 播放完成提示音
     * @param volume 音量 (0.0 - 1.0)
     */
    fun playCompletionSound(volume: Float = 1.0f) {
        if (completionSoundId != -1 && soundPool != null) {
            soundPool?.play(completionSoundId, volume, volume, 1, 0, 1.0f)
        } else {
            // 使用系统音效作为后备方案
            playSystemNotification()
        }
    }
    
    /**
     * 使用系统ToneGenerator播放通知音
     */
    private fun playSystemNotification() {
        try {
            val toneGenerator = ToneGenerator(
                AudioManager.STREAM_NOTIFICATION,
                100 // 音量
            )
            // 播放两次短促的提示音
            toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP, 200)
            Thread.sleep(250)
            toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP, 200)
            toneGenerator.release()
        } catch (e: Exception) {
            // 忽略音效播放错误
            e.printStackTrace()
        }
    }
    
    /**
     * 释放音频资源
     */
    fun release() {
        soundPool?.release()
        soundPool = null
        completionSoundId = -1
    }
}
