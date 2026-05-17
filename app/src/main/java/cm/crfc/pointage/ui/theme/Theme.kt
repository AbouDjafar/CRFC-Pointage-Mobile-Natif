package cm.crfc.pointage.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF1B3A6B),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDCE8FB),
    onPrimaryContainer = Color(0xFF102643),
    secondary = Color(0xFF32B432),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE8F7E8),
    onSecondaryContainer = Color(0xFF144614),
    tertiary = Color(0xFFC9A84C),
    onTertiary = Color.White,
    background = Color(0xFFF4F6FA),
    onBackground = Color(0xFF1A2033),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF1A2033),
    surfaceVariant = Color(0xFFF0F3F9),
    onSurfaceVariant = Color(0xFF64748B),
    error = Color(0xFFEF4444),
    onError = Color.White,
    outline = Color(0xFFE2E8F0)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF97B8EE),
    onPrimary = Color(0xFF08203B),
    primaryContainer = Color(0xFF17345C),
    onPrimaryContainer = Color(0xFFDCE8FB),
    secondary = Color(0xFF76D676),
    onSecondary = Color(0xFF0B3A0B),
    secondaryContainer = Color(0xFF1F5E1F),
    onSecondaryContainer = Color(0xFFE7F8E7),
    tertiary = Color(0xFFE2C56D),
    onTertiary = Color(0xFF3C2E00),
    background = Color(0xFF0E1522),
    onBackground = Color(0xFFE7EDF8),
    surface = Color(0xFF141D2B),
    onSurface = Color(0xFFE7EDF8),
    surfaceVariant = Color(0xFF1C2738),
    onSurfaceVariant = Color(0xFFAAB7CC),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    outline = Color(0xFF334155)
)

@Immutable
data class CrfcExtraColors(
    val late: Color,
    val absent: Color,
    val success: Color,
    val warning: Color,
    val destructive: Color,
    val brandGreenDark: Color,
    val brandGreenLight: Color,
    val headerStart: Color,
    val headerEnd: Color,
    val patternTint: Color
)

private val LightExtra = CrfcExtraColors(
    late = Color(0xFFF97316),
    absent = Color(0xFF8B5CF6),
    success = Color(0xFF22C55E),
    warning = Color(0xFFF59E0B),
    destructive = Color(0xFFEF4444),
    brandGreenDark = Color(0xFF268C26),
    brandGreenLight = Color(0xFFE8F7E8),
    headerStart = Color(0xFF1B3A6B),
    headerEnd = Color(0xFF24518F),
    patternTint = Color(0xFF2E8D3E)
)

private val DarkExtra = CrfcExtraColors(
    late = Color(0xFFFFA35B),
    absent = Color(0xFFC2A3FF),
    success = Color(0xFF6EE091),
    warning = Color(0xFFFFC35C),
    destructive = Color(0xFFFF8A80),
    brandGreenDark = Color(0xFF61C061),
    brandGreenLight = Color(0xFF16331B),
    headerStart = Color(0xFF0E2443),
    headerEnd = Color(0xFF1A3E70),
    patternTint = Color(0xFF205F2A)
)

val LocalCrfcExtraColors = staticCompositionLocalOf { LightExtra }

@Composable
fun CrfcTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val scheme: ColorScheme
    val extra: CrfcExtraColors
    if (darkTheme) {
        scheme = DarkColors
        extra = DarkExtra
    } else {
        scheme = LightColors
        extra = LightExtra
    }

    androidx.compose.runtime.CompositionLocalProvider(LocalCrfcExtraColors provides extra) {
        MaterialTheme(
            colorScheme = scheme,
            typography = CrfcTypography,
            content = content
        )
    }
}
