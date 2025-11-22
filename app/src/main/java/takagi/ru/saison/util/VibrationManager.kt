package takagi.ru.saison.util

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 震动管理器
 * 负责管理设备震动功能
 */
@Singleton
class VibrationManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val vibrator: Vibrator? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }
    
    /**
     * 播放完成震动
     * 震动模式：震动-停止-震动
     */
    fun vibrateCompletion() {
        val pattern = longArrayOf(
            0,    // 延迟0ms开始
            500,  // 震动500ms
            200,  // 停止200ms
            500   // 震动500ms
        )
        vibratePattern(pattern)
    }
    
    /**
     * 播放自定义震动模式
     * @param pattern 震动模式数组，交替表示等待和震动的时长（毫秒）
     */
    fun vibratePattern(pattern: LongArray) {
        try {
            vibrator?.let { vib ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    // Android 8.0+ 使用 VibrationEffect
                    val effect = VibrationEffect.createWaveform(pattern, -1) // -1 表示不重复
                    vib.vibrate(effect)
                } else {
                    // 旧版本使用已弃用的方法
                    @Suppress("DEPRECATION")
                    vib.vibrate(pattern, -1)
                }
            }
        } catch (e: Exception) {
            // 忽略震动错误（可能没有权限或设备不支持）
            e.printStackTrace()
        }
    }
    
    /**
     * 取消当前震动
     */
    fun cancel() {
        try {
            vibrator?.cancel()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    /**
     * 检查设备是否支持震动
     */
    fun hasVibrator(): Boolean {
        return vibrator?.hasVibrator() ?: false
    }
}
