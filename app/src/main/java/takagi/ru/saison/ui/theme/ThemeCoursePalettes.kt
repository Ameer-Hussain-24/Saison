package takagi.ru.saison.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * 主题课程调色板
 * 为每个主题定义12种协调的课程颜色，确保：
 * 1. 与主题整体色调协调
 * 2. 满足 WCAG 对比度要求
 * 3. 在浅色和深色模式下都清晰可辨
 * 4. 相邻课程颜色有足够的区分度
 */
object ThemeCoursePalettes {
    
    // ============================================
    // 樱花主题 (Sakura)
    // ============================================
    val sakuraLight = listOf(
        Color(0xFFE91E63), // 粉红
        Color(0xFFFF4081), // 深粉
        Color(0xFFF06292), // 浅粉
        Color(0xFFAD1457), // 玫瑰
        Color(0xFFC2185B), // 深玫瑰
        Color(0xFFEC407A), // 亮粉
        Color(0xFFD81B60), // 中粉
        Color(0xFFF48FB1), // 柔粉
        Color(0xFF880E4F), // 暗粉
        Color(0xFFFF80AB), // 明粉
        Color(0xFFE91E63), // 粉红（重复以增加选择）
        Color(0xFFF50057)  // 鲜粉
    )
    
    val sakuraDark = listOf(
        Color(0xFFF48FB1), // 浅粉
        Color(0xFFFF80AB), // 亮粉
        Color(0xFFCE93D8), // 淡紫粉
        Color(0xFFE1BEE7), // 极淡紫粉
        Color(0xFFBA68C8), // 紫粉
        Color(0xFFFFCDD2), // 极淡粉
        Color(0xFFEF9A9A), // 淡红粉
        Color(0xFFF8BBD0), // 极淡玫瑰
        Color(0xFFFFAB91), // 淡橙粉
        Color(0xFFFFCC80), // 淡黄粉
        Color(0xFFF48FB1), // 浅粉（重复）
        Color(0xFFFF80AB)  // 亮粉（重复）
    )
    
    // ============================================
    // 薄荷主题 (Mint)
    // ============================================
    val mintLight = listOf(
        Color(0xFF00BFA5), // 薄荷绿
        Color(0xFF64FFDA), // 亮薄荷
        Color(0xFF1DE9B6), // 鲜薄荷
        Color(0xFF00897B), // 深薄荷
        Color(0xFF26A69A), // 中薄荷
        Color(0xFF4DB6AC), // 浅薄荷
        Color(0xFF80CBC4), // 淡薄荷
        Color(0xFF00695C), // 暗薄荷
        Color(0xFF009688), // 青薄荷
        Color(0xFF26C6DA), // 青蓝
        Color(0xFF00ACC1), // 深青蓝
        Color(0xFF00BCD4)  // 亮青蓝
    )
    
    val mintDark = listOf(
        Color(0xFF64FFDA), // 亮薄荷
        Color(0xFF69F0AE), // 明绿
        Color(0xFF80CBC4), // 淡薄荷
        Color(0xFFB2DFDB), // 极淡薄荷
        Color(0xFF4DB6AC), // 浅薄荷
        Color(0xFF26C6DA), // 青蓝
        Color(0xFF4DD0E1), // 亮青蓝
        Color(0xFF80DEEA), // 淡青蓝
        Color(0xFFB2EBF2), // 极淡青蓝
        Color(0xFF81C784), // 淡绿
        Color(0xFFA5D6A7), // 极淡绿
        Color(0xFF64FFDA)  // 亮薄荷（重复）
    )
    
    // ============================================
    // 琥珀主题 (Amber)
    // ============================================
    val amberLight = listOf(
        Color(0xFFFFC107), // 琥珀
        Color(0xFFFFD54F), // 亮琥珀
        Color(0xFFFFCA28), // 鲜琥珀
        Color(0xFFFF8F00), // 深琥珀
        Color(0xFFFFB300), // 中琥珀
        Color(0xFFFFA726), // 橙琥珀
        Color(0xFFFF9800), // 橙色
        Color(0xFFFF6F00), // 深橙
        Color(0xFFFFAB00), // 亮橙
        Color(0xFFFFD740), // 明黄
        Color(0xFFFFEA00), // 鲜黄
        Color(0xFFFFC400)  // 金黄
    )
    
    val amberDark = listOf(
        Color(0xFFFFD54F), // 亮琥珀
        Color(0xFFFFE082), // 淡琥珀
        Color(0xFFFFECB3), // 极淡琥珀
        Color(0xFFFFCC80), // 淡橙
        Color(0xFFFFAB91), // 极淡橙
        Color(0xFFFFD180), // 淡金
        Color(0xFFFFE57F), // 淡黄
        Color(0xFFFFF59D), // 极淡黄
        Color(0xFFFFE082), // 淡琥珀（重复）
        Color(0xFFFFD54F), // 亮琥珀（重复）
        Color(0xFFFFCC80), // 淡橙（重复）
        Color(0xFFFFAB91)  // 极淡橙（重复）
    )
    
    // ============================================
    // 雪主题 (Snow)
    // ============================================
    val snowLight = listOf(
        Color(0xFF90CAF9), // 浅蓝
        Color(0xFFBBDEFB), // 淡蓝
        Color(0xFF81D4FA), // 天蓝
        Color(0xFF64B5F6), // 中蓝
        Color(0xFF42A5F5), // 亮蓝
        Color(0xFF2196F3), // 蓝色
        Color(0xFF1976D2), // 深蓝
        Color(0xFF1565C0), // 暗蓝
        Color(0xFF0D47A1), // 极暗蓝
        Color(0xFF82B1FF), // 明蓝
        Color(0xFF448AFF), // 鲜蓝
        Color(0xFF2979FF)  // 亮鲜蓝
    )
    
    val snowDark = listOf(
        Color(0xFF64B5F6), // 中蓝
        Color(0xFF81D4FA), // 天蓝
        Color(0xFF4FC3F7), // 亮天蓝
        Color(0xFFB3E5FC), // 极淡蓝
        Color(0xFF90CAF9), // 浅蓝
        Color(0xFFBBDEFB), // 淡蓝
        Color(0xFF82B1FF), // 明蓝
        Color(0xFF448AFF), // 鲜蓝
        Color(0xFF2979FF), // 亮鲜蓝
        Color(0xFF80DEEA), // 淡青蓝
        Color(0xFFB2EBF2), // 极淡青蓝
        Color(0xFF64B5F6)  // 中蓝（重复）
    )
    
    // ============================================
    // 雨主题 (Rain)
    // ============================================
    val rainLight = listOf(
        Color(0xFF607D8B), // 蓝灰
        Color(0xFF90A4AE), // 浅蓝灰
        Color(0xFF78909C), // 中蓝灰
        Color(0xFF546E7A), // 深蓝灰
        Color(0xFF455A64), // 暗蓝灰
        Color(0xFF37474F), // 极暗蓝灰
        Color(0xFF263238), // 最暗蓝灰
        Color(0xFFB0BEC5), // 淡蓝灰
        Color(0xFFCFD8DC), // 极淡蓝灰
        Color(0xFF757575), // 灰色
        Color(0xFF9E9E9E), // 浅灰
        Color(0xFF616161)  // 深灰
    )
    
    val rainDark = listOf(
        Color(0xFF90A4AE), // 浅蓝灰
        Color(0xFFB0BEC5), // 淡蓝灰
        Color(0xFFCFD8DC), // 极淡蓝灰
        Color(0xFFECEFF1), // 最淡蓝灰
        Color(0xFF78909C), // 中蓝灰
        Color(0xFF9E9E9E), // 浅灰
        Color(0xFFBDBDBD), // 淡灰
        Color(0xFFE0E0E0), // 极淡灰
        Color(0xFF90A4AE), // 浅蓝灰（重复）
        Color(0xFFB0BEC5), // 淡蓝灰（重复）
        Color(0xFFCFD8DC), // 极淡蓝灰（重复）
        Color(0xFF78909C)  // 中蓝灰（重复）
    )
    
    // ============================================
    // 枫叶主题 (Maple)
    // ============================================
    val mapleLight = listOf(
        Color(0xFFFF5722), // 深橙红
        Color(0xFFFF7043), // 橙红
        Color(0xFFFF8A65), // 浅橙红
        Color(0xFFE64A19), // 暗橙红
        Color(0xFFD84315), // 极暗橙红
        Color(0xFFBF360C), // 最暗橙红
        Color(0xFFFFAB91), // 淡橙红
        Color(0xFFFFCCBC), // 极淡橙红
        Color(0xFFFF6E40), // 鲜橙红
        Color(0xFFFF3D00), // 亮橙红
        Color(0xFFDD2C00), // 深鲜橙红
        Color(0xFFFF5722)  // 深橙红（重复）
    )
    
    val mapleDark = listOf(
        Color(0xFFFF8A65), // 浅橙红
        Color(0xFFFFAB91), // 淡橙红
        Color(0xFFFFCCBC), // 极淡橙红
        Color(0xFFFFE0B2), // 极淡橙
        Color(0xFFFF7043), // 橙红
        Color(0xFFFFCC80), // 淡橙
        Color(0xFFFFD180), // 淡金橙
        Color(0xFFFFE082), // 淡黄橙
        Color(0xFFFF8A65), // 浅橙红（重复）
        Color(0xFFFFAB91), // 淡橙红（重复）
        Color(0xFFFFCCBC), // 极淡橙红（重复）
        Color(0xFFFF7043)  // 橙红（重复）
    )
    
    // ============================================
    // 海洋主题 (Ocean)
    // ============================================
    val oceanLight = listOf(
        Color(0xFF0288D1), // 海蓝
        Color(0xFF03A9F4), // 亮海蓝
        Color(0xFF29B6F6), // 明海蓝
        Color(0xFF0277BD), // 深海蓝
        Color(0xFF01579B), // 暗海蓝
        Color(0xFF4FC3F7), // 淡海蓝
        Color(0xFF81D4FA), // 极淡海蓝
        Color(0xFFB3E5FC), // 最淡海蓝
        Color(0xFF00BCD4), // 青蓝
        Color(0xFF00ACC1), // 深青蓝
        Color(0xFF0097A7), // 暗青蓝
        Color(0xFF006064)  // 极暗青蓝
    )
    
    val oceanDark = listOf(
        Color(0xFF4FC3F7), // 淡海蓝
        Color(0xFF29B6F6), // 明海蓝
        Color(0xFF81D4FA), // 极淡海蓝
        Color(0xFFB3E5FC), // 最淡海蓝
        Color(0xFF4DD0E1), // 亮青蓝
        Color(0xFF26C6DA), // 青蓝
        Color(0xFF80DEEA), // 淡青蓝
        Color(0xFFB2EBF2), // 极淡青蓝
        Color(0xFF4FC3F7), // 淡海蓝（重复）
        Color(0xFF29B6F6), // 明海蓝（重复）
        Color(0xFF81D4FA), // 极淡海蓝（重复）
        Color(0xFF4DD0E1)  // 亮青蓝（重复）
    )
    
    // ============================================
    // 日落主题 (Sunset)
    // ============================================
    val sunsetLight = listOf(
        Color(0xFFFF6F00), // 深橙
        Color(0xFFFF9800), // 橙色
        Color(0xFFFFB74D), // 浅橙
        Color(0xFFF57C00), // 暗橙
        Color(0xFFEF6C00), // 极暗橙
        Color(0xFFE65100), // 最暗橙
        Color(0xFFFFCC80), // 淡橙
        Color(0xFFFFE0B2), // 极淡橙
        Color(0xFFFF9100), // 鲜橙
        Color(0xFFFF6D00), // 亮橙
        Color(0xFFFF3D00), // 深鲜橙
        Color(0xFFDD2C00)  // 极深鲜橙
    )
    
    val sunsetDark = listOf(
        Color(0xFFFFB74D), // 浅橙
        Color(0xFFFFCC80), // 淡橙
        Color(0xFFFFE0B2), // 极淡橙
        Color(0xFFFFD180), // 淡金橙
        Color(0xFFFFE082), // 淡黄橙
        Color(0xFFFFF59D), // 极淡黄
        Color(0xFFFFAB91), // 淡橙红
        Color(0xFFFFCCBC), // 极淡橙红
        Color(0xFFFFB74D), // 浅橙（重复）
        Color(0xFFFFCC80), // 淡橙（重复）
        Color(0xFFFFE0B2), // 极淡橙（重复）
        Color(0xFFFFD180)  // 淡金橙（重复）
    )
    
    // ============================================
    // 森林主题 (Forest)
    // ============================================
    val forestLight = listOf(
        Color(0xFF388E3C), // 森林绿
        Color(0xFF66BB6A), // 亮森林绿
        Color(0xFF4CAF50), // 绿色
        Color(0xFF2E7D32), // 深森林绿
        Color(0xFF1B5E20), // 暗森林绿
        Color(0xFF81C784), // 浅绿
        Color(0xFFA5D6A7), // 淡绿
        Color(0xFFC8E6C9), // 极淡绿
        Color(0xFF43A047), // 中绿
        Color(0xFF00C853), // 鲜绿
        Color(0xFF00E676), // 亮鲜绿
        Color(0xFF69F0AE)  // 明绿
    )
    
    val forestDark = listOf(
        Color(0xFF81C784), // 浅绿
        Color(0xFF66BB6A), // 亮森林绿
        Color(0xFFA5D6A7), // 淡绿
        Color(0xFFC8E6C9), // 极淡绿
        Color(0xFF69F0AE), // 明绿
        Color(0xFF00E676), // 亮鲜绿
        Color(0xFF00C853), // 鲜绿
        Color(0xFF80CBC4), // 淡薄荷
        Color(0xFFB2DFDB), // 极淡薄荷
        Color(0xFF81C784), // 浅绿（重复）
        Color(0xFFA5D6A7), // 淡绿（重复）
        Color(0xFF69F0AE)  // 明绿（重复）
    )
    
    // ============================================
    // 薰衣草主题 (Lavender)
    // ============================================
    val lavenderLight = listOf(
        Color(0xFF9C27B0), // 紫色
        Color(0xFFBA68C8), // 浅紫
        Color(0xFFAB47BC), // 中紫
        Color(0xFF8E24AA), // 深紫
        Color(0xFF7B1FA2), // 暗紫
        Color(0xFF6A1B9A), // 极暗紫
        Color(0xFF4A148C), // 最暗紫
        Color(0xFFCE93D8), // 淡紫
        Color(0xFFE1BEE7), // 极淡紫
        Color(0xFFEA80FC), // 鲜紫
        Color(0xFFE040FB), // 亮紫
        Color(0xFFD500F9)  // 深鲜紫
    )
    
    val lavenderDark = listOf(
        Color(0xFFCE93D8), // 淡紫
        Color(0xFFBA68C8), // 浅紫
        Color(0xFFE1BEE7), // 极淡紫
        Color(0xFFF3E5F5), // 最淡紫
        Color(0xFFEA80FC), // 鲜紫
        Color(0xFFE040FB), // 亮紫
        Color(0xFFD500F9), // 深鲜紫
        Color(0xFFF48FB1), // 浅粉
        Color(0xFFFF80AB), // 亮粉
        Color(0xFFCE93D8), // 淡紫（重复）
        Color(0xFFE1BEE7), // 极淡紫（重复）
        Color(0xFFBA68C8)  // 浅紫（重复）
    )
    
    // ============================================
    // 沙漠主题 (Desert)
    // ============================================
    val desertLight = listOf(
        Color(0xFF8D6E63), // 棕色
        Color(0xFFA1887F), // 浅棕
        Color(0xFFBCAAA4), // 淡棕
        Color(0xFF6D4C41), // 深棕
        Color(0xFF5D4037), // 暗棕
        Color(0xFF4E342E), // 极暗棕
        Color(0xFF3E2723), // 最暗棕
        Color(0xFFD7CCC8), // 极淡棕
        Color(0xFFEFEBE9), // 最淡棕
        Color(0xFFFFAB91), // 淡橙
        Color(0xFFFFCC80), // 淡黄橙
        Color(0xFFFFE082)  // 淡黄
    )
    
    val desertDark = listOf(
        Color(0xFFBCAAA4), // 淡棕
        Color(0xFFA1887F), // 浅棕
        Color(0xFFD7CCC8), // 极淡棕
        Color(0xFFEFEBE9), // 最淡棕
        Color(0xFFFFCC80), // 淡黄橙
        Color(0xFFFFE082), // 淡黄
        Color(0xFFFFF59D), // 极淡黄
        Color(0xFFFFAB91), // 淡橙
        Color(0xFFFFCCBC), // 极淡橙红
        Color(0xFFBCAAA4), // 淡棕（重复）
        Color(0xFFD7CCC8), // 极淡棕（重复）
        Color(0xFFFFCC80)  // 淡黄橙（重复）
    )
    
    // ============================================
    // 极光主题 (Aurora)
    // ============================================
    val auroraLight = listOf(
        Color(0xFF00BCD4), // 青色
        Color(0xFF26C6DA), // 亮青
        Color(0xFF18FFFF), // 鲜青
        Color(0xFF00ACC1), // 深青
        Color(0xFF0097A7), // 暗青
        Color(0xFF00838F), // 极暗青
        Color(0xFF006064), // 最暗青
        Color(0xFF4DD0E1), // 淡青
        Color(0xFF80DEEA), // 极淡青
        Color(0xFFB2EBF2), // 最淡青
        Color(0xFF84FFFF), // 明青
        Color(0xFF00E5FF)  // 亮鲜青
    )
    
    val auroraDark = listOf(
        Color(0xFF4DD0E1), // 淡青
        Color(0xFF26C6DA), // 亮青
        Color(0xFF80DEEA), // 极淡青
        Color(0xFFB2EBF2), // 最淡青
        Color(0xFF18FFFF), // 鲜青
        Color(0xFF84FFFF), // 明青
        Color(0xFF00E5FF), // 亮鲜青
        Color(0xFF81D4FA), // 极淡海蓝
        Color(0xFFB3E5FC), // 最淡海蓝
        Color(0xFF4DD0E1), // 淡青（重复）
        Color(0xFF80DEEA), // 极淡青（重复）
        Color(0xFF18FFFF)  // 鲜青（重复）
    )
    
    // ============================================
    // 动态主题 (Dynamic) - 使用通用调色板
    // ============================================
    val dynamicLight = listOf(
        Color(0xFF6650a4), // 紫色
        Color(0xFF625b71), // 紫灰
        Color(0xFF7D5260), // 粉灰
        Color(0xFF1976D2), // 蓝色
        Color(0xFF388E3C), // 绿色
        Color(0xFFFF9800), // 橙色
        Color(0xFFE91E63), // 粉红
        Color(0xFF00BCD4), // 青色
        Color(0xFF9C27B0), // 紫色
        Color(0xFFFF5722), // 深橙
        Color(0xFF607D8B), // 蓝灰
        Color(0xFF8D6E63)  // 棕色
    )
    
    val dynamicDark = listOf(
        Color(0xFFD0BCFF), // 浅紫
        Color(0xFFCCC2DC), // 浅紫灰
        Color(0xFFEFB8C8), // 浅粉灰
        Color(0xFF90CAF9), // 浅蓝
        Color(0xFF81C784), // 浅绿
        Color(0xFFFFCC80), // 淡橙
        Color(0xFFF48FB1), // 浅粉
        Color(0xFF80DEEA), // 淡青
        Color(0xFFCE93D8), // 淡紫
        Color(0xFFFFAB91), // 淡橙红
        Color(0xFFB0BEC5), // 淡蓝灰
        Color(0xFFBCAAA4)  // 淡棕
    )
}
