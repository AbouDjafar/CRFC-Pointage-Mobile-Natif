package cm.crfc.pointage.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = NavyPrimary,
    onPrimary = TextOnPrimary,
    primaryContainer = NavyLight,
    onPrimaryContainer = TextOnPrimary,
    secondary = GreenAccent,
    onSecondary = TextOnPrimary,
    tertiary = OrangeAccent,
    onTertiary = TextOnPrimary,
    background = Background,
    onBackground = TextPrimary,
    surface = Surface,
    onSurface = TextPrimary,
    surfaceVariant = AvatarBg,
    onSurfaceVariant = TextSecondary,
    error = OrangeAccent,
    onError = TextOnPrimary,
    outline = Divider
)

private val DarkColors = darkColorScheme(
    primary = NavyLight,
    onPrimary = TextOnPrimary,
    primaryContainer = NavyPrimary,
    onPrimaryContainer = TextOnPrimary,
    secondary = GreenAccent,
    onSecondary = TextOnPrimary,
    tertiary = OrangeAccent,
    onTertiary = TextOnPrimary,
    background = Color(0xFF0F172A),
    onBackground = Color(0xFFE5E7EB),
    surface = Color(0xFF111827),
    onSurface = Color(0xFFF3F4F6),
    surfaceVariant = Color(0xFF1F2937),
    onSurfaceVariant = Color(0xFF9CA3AF),
    error = Color(0xFFFFA07A),
    onError = Color(0xFF2E0F00),
    outline = Color(0xFF374151)
)

private val LightExtras = CrfcUiExtras(
    navyDark = NavyDark,
    navyLight = NavyLight,
    orangeAccent = OrangeAccent,
    orangeLight = OrangeLight,
    purpleAccent = PurpleAccent,
    purpleLight = PurpleLight,
    greenAccent = GreenAccent,
    greenLight = GreenLight,
    amberWarning = AmberWarning,
    amberLight = AmberLight,
    avatarBg = AvatarBg,
    divider = Divider
)

private val DarkExtras = CrfcUiExtras(
    navyDark = NavyDark,
    navyLight = NavyLight,
    orangeAccent = OrangeAccent,
    orangeLight = Color(0xFF3B1B0B),
    purpleAccent = PurpleAccent,
    purpleLight = Color(0xFF24163F),
    greenAccent = GreenAccent,
    greenLight = Color(0xFF12301B),
    amberWarning = AmberWarning,
    amberLight = Color(0xFF3B3410),
    avatarBg = Color(0xFF243041),
    divider = Color(0xFF334155)
)

@Composable
fun CrfcTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val scheme = if (darkTheme) DarkColors else LightColors
    val extras = if (darkTheme) DarkExtras else LightExtras

    androidx.compose.runtime.CompositionLocalProvider(LocalCrfcUiExtras provides extras) {
        MaterialTheme(
            colorScheme = scheme,
            typography = CrfcTypography,
            content = content
        )
    }
}
