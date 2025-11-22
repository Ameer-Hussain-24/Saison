package takagi.ru.saison.util

import androidx.compose.ui.graphics.Color
import org.junit.Test
import takagi.ru.saison.ui.theme.*

/**
 * 主题颜色对比度测试
 * 
 * 验证所有主题的颜色对比度是否符合 WCAG 2.1 AA 标准
 */
class ThemeColorContrastTest {
    
    @Test
    fun `test Sakura theme contrast`() {
        // Light mode
        val sakuraLightResult = ColorContrastChecker.validateColorScheme(
            primary = SakuraPrimary,
            onPrimary = SakuraOnPrimary,
            background = SakuraBackground,
            onBackground = SakuraOnBackground,
            surface = SakuraSurface,
            onSurface = SakuraOnSurface
        )
        printValidationResult("Sakura Light", sakuraLightResult)
        
        // Dark mode
        val sakuraDarkResult = ColorContrastChecker.validateColorScheme(
            primary = SakuraDarkPrimary,
            onPrimary = SakuraDarkOnPrimary,
            background = SakuraDarkBackground,
            onBackground = SakuraDarkOnBackground,
            surface = SakuraDarkSurface,
            onSurface = SakuraDarkOnSurface
        )
        printValidationResult("Sakura Dark", sakuraDarkResult)
    }
    
    @Test
    fun `test Mint theme contrast`() {
        val mintLightResult = ColorContrastChecker.validateColorScheme(
            primary = MintPrimary,
            onPrimary = MintOnPrimary,
            background = MintBackground,
            onBackground = MintOnBackground,
            surface = MintSurface,
            onSurface = MintOnSurface
        )
        printValidationResult("Mint Light", mintLightResult)
        
        val mintDarkResult = ColorContrastChecker.validateColorScheme(
            primary = MintDarkPrimary,
            onPrimary = MintDarkOnPrimary,
            background = MintDarkBackground,
            onBackground = MintDarkOnBackground,
            surface = MintDarkSurface,
            onSurface = MintDarkOnSurface
        )
        printValidationResult("Mint Dark", mintDarkResult)
    }
    
    @Test
    fun `test Amber theme contrast`() {
        val amberLightResult = ColorContrastChecker.validateColorScheme(
            primary = AmberPrimary,
            onPrimary = AmberOnPrimary,
            background = AmberBackground,
            onBackground = AmberOnBackground,
            surface = AmberSurface,
            onSurface = AmberOnSurface
        )
        printValidationResult("Amber Light", amberLightResult)
        
        val amberDarkResult = ColorContrastChecker.validateColorScheme(
            primary = AmberDarkPrimary,
            onPrimary = AmberDarkOnPrimary,
            background = AmberDarkBackground,
            onBackground = AmberDarkOnBackground,
            surface = AmberDarkSurface,
            onSurface = AmberDarkOnSurface
        )
        printValidationResult("Amber Dark", amberDarkResult)
    }
    
    @Test
    fun `test Tech Purple theme contrast`() {
        val techPurpleLightResult = ColorContrastChecker.validateColorScheme(
            primary = TechPurplePrimary,
            onPrimary = TechPurpleOnPrimary,
            background = TechPurpleBackground,
            onBackground = TechPurpleOnBackground,
            surface = TechPurpleSurface,
            onSurface = TechPurpleOnSurface
        )
        printValidationResult("Tech Purple Light", techPurpleLightResult)
        
        val techPurpleDarkResult = ColorContrastChecker.validateColorScheme(
            primary = TechPurplePrimaryDark,
            onPrimary = TechPurpleOnPrimaryDark,
            background = TechPurpleBackgroundDark,
            onBackground = TechPurpleOnBackgroundDark,
            surface = TechPurpleSurfaceDark,
            onSurface = TechPurpleOnSurfaceDark
        )
        printValidationResult("Tech Purple Dark", techPurpleDarkResult)
    }
    
    @Test
    fun `test Black Mamba theme contrast`() {
        val blackMambaLightResult = ColorContrastChecker.validateColorScheme(
            primary = BlackMambaPrimary,
            onPrimary = BlackMambaOnPrimary,
            background = BlackMambaBackground,
            onBackground = BlackMambaOnBackground,
            surface = BlackMambaSurface,
            onSurface = BlackMambaOnSurface
        )
        printValidationResult("Black Mamba Light", blackMambaLightResult)
        
        val blackMambaDarkResult = ColorContrastChecker.validateColorScheme(
            primary = BlackMambaPrimaryDark,
            onPrimary = BlackMambaOnPrimaryDark,
            background = BlackMambaBackgroundDark,
            onBackground = BlackMambaOnBackgroundDark,
            surface = BlackMambaSurfaceDark,
            onSurface = BlackMambaOnSurfaceDark
        )
        printValidationResult("Black Mamba Dark", blackMambaDarkResult)
    }
    
    @Test
    fun `test Grey Style theme contrast`() {
        val greyStyleLightResult = ColorContrastChecker.validateColorScheme(
            primary = GreyStylePrimary,
            onPrimary = GreyStyleOnPrimary,
            background = GreyStyleBackground,
            onBackground = GreyStyleOnBackground,
            surface = GreyStyleSurface,
            onSurface = GreyStyleOnSurface
        )
        printValidationResult("Grey Style Light", greyStyleLightResult)
        
        val greyStyleDarkResult = ColorContrastChecker.validateColorScheme(
            primary = GreyStylePrimaryDark,
            onPrimary = GreyStyleOnPrimaryDark,
            background = GreyStyleBackgroundDark,
            onBackground = GreyStyleOnBackgroundDark,
            surface = GreyStyleSurfaceDark,
            onSurface = GreyStyleOnSurfaceDark
        )
        printValidationResult("Grey Style Dark", greyStyleDarkResult)
    }
    
    private fun printValidationResult(
        themeName: String,
        result: ColorContrastChecker.ColorSchemeValidationResult
    ) {
        println("\n=== $themeName ===")
        println("Overall AA: ${if (result.overallMeetsAA) "✓ PASS" else "✗ FAIL"}")
        println("Overall AAA: ${if (result.overallMeetsAAA) "✓ PASS" else "✗ FAIL"}")
        
        result.results.forEach { check ->
            val status = when (check.level) {
                ColorContrastChecker.ContrastLevel.AAA -> "✓ AAA"
                ColorContrastChecker.ContrastLevel.AA -> "✓ AA"
                ColorContrastChecker.ContrastLevel.FAIL -> "✗ FAIL"
            }
            println("  ${check.name}: ${String.format("%.2f", check.contrast)}:1 $status")
        }
        
        if (result.failedChecks.isNotEmpty()) {
            println("  ⚠ Failed checks:")
            result.failedChecks.forEach { check ->
                println("    - ${check.name}: ${String.format("%.2f", check.contrast)}:1 (需要 >= 4.5:1)")
            }
        }
    }
}
