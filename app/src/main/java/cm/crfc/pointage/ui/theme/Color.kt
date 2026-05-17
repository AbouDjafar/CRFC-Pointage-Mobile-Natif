package cm.crfc.pointage.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

val NavyPrimary = Color(0xFF1E3A5F)
val NavyDark = Color(0xFF152D4A)
val NavyLight = Color(0xFF2A4F7C)

val OrangeAccent = Color(0xFFF97316)
val OrangeLight = Color(0xFFFFF0E6)
val PurpleAccent = Color(0xFF7C3AED)
val PurpleLight = Color(0xFFF3EEFF)
val GreenAccent = Color(0xFF16A34A)
val GreenLight = Color(0xFFEDFCF2)
val AmberWarning = Color(0xFFF59E0B)
val AmberLight = Color(0xFFFFFBEB)

val Background = Color(0xFFF1F3F7)
val Surface = Color(0xFFFFFFFF)
val TextPrimary = Color(0xFF111827)
val TextSecondary = Color(0xFF6B7280)
val TextOnPrimary = Color(0xFFFFFFFF)
val Divider = Color(0xFFE5E7EB)
val AvatarBg = Color(0xFFE8ECF2)

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
