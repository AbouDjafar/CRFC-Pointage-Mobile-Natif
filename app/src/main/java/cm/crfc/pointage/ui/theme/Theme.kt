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
    primary = Color(0xFF163A6B),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD6E4FF),
    onPrimaryContainer = Color(0xFF0B1E37),
    secondary = Color(0xFFC9A84C),
    onSecondary = Color.White,
    tertiary = Color(0xFF2E9C43),
    onTertiary = Color.White,
    background = Color(0xFFF4F6FA),
    onBackground = Color(0xFF182132),
    surface = Color.White,
    onSurface = Color(0xFF182132),
    surfaceVariant = Color(0xFFEFF3FA),
    onSurfaceVariant = Color(0xFF607089),
    error = Color(0xFFD13B3B),
    onError = Color.White,
    outline = Color(0xFFD8E0ED)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF8DB8FF),
    onPrimary = Color(0xFF0C2341),
    secondary = Color(0xFFE1C46E),
    onSecondary = Color(0xFF362A00),
    tertiary = Color(0xFF8FDE9A),
    onTertiary = Color(0xFF093615),
    background = Color(0xFF0D1521),
    onBackground = Color(0xFFE9EEF8),
    surface = Color(0xFF142033),
    onSurface = Color(0xFFE9EEF8),
    surfaceVariant = Color(0xFF1D2A3D),
    onSurfaceVariant = Color(0xFFB7C4D8),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    outline = Color(0xFF39485D)
)

@Immutable
data class CrfcExtraColors(
    val late: Color,
    val absent: Color,
    val success: Color,
    val warning: Color,
    val headerStart: Color,
    val headerEnd: Color
)

private val LightExtra = CrfcExtraColors(
    late = Color(0xFFF97316),
    absent = Color(0xFF8559E6),
    success = Color(0xFF20B455),
    warning = Color(0xFFF4A119),
    headerStart = Color(0xFF163A6B),
    headerEnd = Color(0xFF234F8F)
)

private val DarkExtra = CrfcExtraColors(
    late = Color(0xFFFFA55A),
    absent = Color(0xFFC3A3FF),
    success = Color(0xFF72D889),
    warning = Color(0xFFFFC35C),
    headerStart = Color(0xFF122746),
    headerEnd = Color(0xFF1F3C68)
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

