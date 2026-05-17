package cm.crfc.pointage.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

val NavyPrimary = Color(0xFF234C88)
val NavyDark = Color(0xFF1A3661)
val NavyLight = Color(0xFF3A649A)

val OrangeAccent = Color(0xFFD28A1C)
val OrangeLight = Color(0xFFF7E8CF)
val PurpleAccent = Color(0xFF8794A6)
val PurpleLight = Color(0xFFEFF2F5)
val GreenAccent = Color(0xFF7A9A60)
val GreenLight = Color(0xFFE6EFDB)
val AmberWarning = Color(0xFFE08D2D)
val AmberLight = Color(0xFFFBF0DC)

val Background = Color(0xFFF2F6EA)
val Surface = Color(0xFFFFFFFF)
val TextPrimary = Color(0xFF31343A)
val TextSecondary = Color(0xFF7B7E80)
val TextOnPrimary = Color(0xFFFFFFFF)
val Divider = Color(0xFFDCE6D1)
val AvatarBg = Color(0xFFF2EEE6)

@Immutable
data class CrfcUiExtras(
    val navyDark: Color,
    val navyLight: Color,
    val orangeAccent: Color,
    val orangeLight: Color,
    val purpleAccent: Color,
    val purpleLight: Color,
    val greenAccent: Color,
    val greenLight: Color,
    val amberWarning: Color,
    val amberLight: Color,
    val avatarBg: Color,
    val divider: Color
)

val LocalCrfcUiExtras = staticCompositionLocalOf {
    CrfcUiExtras(
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
}
