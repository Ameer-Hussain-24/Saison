package takagi.ru.saison.util

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MetronomeSoundManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var soundPool: SoundPool? = null
    private var accentSoundId: Int = -1
    private var normalSoundId: Int = -1
    
    init {
        initializeSoundPool()
    }
    
    private fun initializeSoundPool() {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .build()
        
        soundPool = SoundPool.Builder()
            .setMaxStreams(2)
            .setAudioAttributes(audioAttributes)
            .build()
        
        // 使用系统音效作为临时方案
        // TODO: 添加自定义音效资源文件
        generateBeepSounds()
    }
    
    private fun generateBeepSounds() {
        // 使用 ToneGenerator 生成简单的哔哔声
        // 这是一个临时方案，实际应用中应该使用音频文件
        // accentSoundId = soundPool?.load(context, R.raw.metronome_accent, 1) ?: -1
        // normalSoundId = soundPool?.load(context, R.raw.metronome_normal, 1) ?: -1
    }
    
    fun playAccentBeat(volume: Float) {
        if (accentSoundId != -1) {
            soundPool?.play(accentSoundId, volume, volume, 1, 0, 1.0f)
        } else {
            // 使用系统音效作为后备
            playSystemBeep(true)
        }
    }
    
    fun playNormalBeat(volume: Float) {
        if (normalSoundId != -1) {
            soundPool?.play(normalSoundId, volume, volume, 1, 0, 1.0f)
        } else {
            // 使用系统音效作为后备
            playSystemBeep(false)
        }
    }
    
    private fun playSystemBeep(isAccent: Boolean) {
        // 使用 ToneGenerator 生成简单音效
        try {
            val toneGenerator = android.media.ToneGenerator(
                android.media.AudioManager.STREAM_MUSIC,
                if (isAccent) 100 else 80
            )
            toneGenerator.startTone(
                if (isAccent) android.media.ToneGenerator.TONE_PROP_BEEP else android.media.ToneGenerator.TONE_PROP_BEEP2,
                50
            )
            toneGenerator.release()
        } catch (e: Exception) {
            // 忽略音效播放错误
        }
    }
    
    fun release() {
        soundPool?.release()
        soundPool = null
    }
}
