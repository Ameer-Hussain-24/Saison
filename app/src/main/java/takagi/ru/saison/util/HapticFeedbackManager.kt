package takagi.ru.saison.util

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 触觉反馈管理器
 * 专门用于日历UI的触觉反馈
 */
@Singleton
class HapticFeedbackManager @Inject constructor(
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
    
    private var isEnabled = true
    
    /**
     * 设置是否启用触觉反馈
     */
    fun setEnabled(enabled: Boolean) {
        isEnabled = enabled
    }
    
    /**
     * 轻触反馈
     * 用于按钮点击、日期选择等
     */
    fun performLightTap() {
        if (!isEnabled) return
        vibrate(10)
    }
    
    /**
     * 中等反馈
     * 用于任务完成、重要操作等
     */
    fun performMediumTap() {
        if (!isEnabled) return
        vibrate(20)
    }
    
    /**
     * 长按反馈
     * 用于长按操作
     */
    fun performLongPress() {
        if (!isEnabled) return
        vibrate(50)
    }
    
    /**
     * 成功反馈
     * 用于任务完成、操作成功等
     */
    fun performSuccess() {
        if (!isEnabled) return
        val pattern = longArrayOf(0, 30, 50, 30)
        vibratePattern(pattern)
    }
    
    /**
     * 错误反馈
     * 用于操作失败、错误提示等
     */
    fun performError() {
        if (!isEnabled) return
        val pattern = longArrayOf(0, 50, 100, 50, 100, 50)
        vibratePattern(pattern)
    }
    
    /**
     * 警告反馈
     * 用于紧急任务、重要提醒等
     */
    fun performWarning() {
        if (!isEnabled) return
        val pattern = longArrayOf(0, 100, 50, 100)
        vibratePattern(pattern)
    }
    
    /**
     * 庆祝反馈
     * 用于全部完成等庆祝场景
     */
    fun performCelebration() {
        if (!isEnabled) return
        val pattern = longArrayOf(
            0, 50, 50, 50, 50, 50, 100, 100
        )
        vibratePattern(pattern)
    }
    
    /**
     * 滑动反馈
     * 用于滑动切换月份等
     */
    fun performSwipe() {
        if (!isEnabled) return
        vibrate(15)
    }
    
    /**
     * 选择反馈
     * 用于选择日期、切换视图等
     */
    fun performSelection() {
        if (!isEnabled) return
        vibrate(25)
    }
    
    /**
     * 执行震动
     */
    private fun vibrate(duration: Long) {
        try {
            vibrator?.let { vib ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val effect = VibrationEffect.createOneShot(
                        duration,
                        VibrationEffect.DEFAULT_AMPLITUDE
                    )
                    vib.vibrate(effect)
                } else {
                    @Suppress("DEPRECATION")
                    vib.vibrate(duration)
                }
            }
        } catch (e: Exception) {
            // 忽略震动错误
        }
    }
    
    /**
     * 执行震动模式
     */
    private fun vibratePattern(pattern: LongArray) {
        try {
            vibrator?.let { vib ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val effect = VibrationEffect.createWaveform(pattern, -1)
                    vib.vibrate(effect)
                } else {
                    @Suppress("DEPRECATION")
                    vib.vibrate(pattern, -1)
                }
            }
        } catch (e: Exception) {
            // 忽略震动错误
        }
    }
    
    /**
     * 取消震动
     */
    fun cancel() {
        try {
            vibrator?.cancel()
        } catch (e: Exception) {
            // 忽略错误
        }
    }
    
    /**
     * 检查设备是否支持震动
     */
    fun hasVibrator(): Boolean {
        return vibrator?.hasVibrator() ?: false
    }
}


