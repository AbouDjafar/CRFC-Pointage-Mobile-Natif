package cm.crfc.pointage.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

object Dimens {
    val AvatarSize = 40.dp
    val AvatarSizeLg = 56.dp
    val IconSize = 20.dp
    val TouchTarget = 48.dp
    val CardRadius = 16.dp
    val ButtonRadius = 28.dp
    val SheetRadius = 24.dp
    val ChipRadius = 50.dp

    val SpaceXS = 4.dp
    val SpaceSM = 8.dp
    val SpaceMD = 12.dp
    val SpaceLG = 16.dp
    val SpaceXL = 20.dp
    val SpaceXXL = 24.dp

    val ButtonMinHeight = 54.dp
    val ChipHeight = 36.dp
    val InputMinHeight = 52.dp
    val BottomNavHeight = 80.dp
}

@Composable
fun isSmallScreen(): Boolean = LocalConfiguration.current.screenWidthDp < 360

@Composable
fun horizontalPadding(): Dp = if (isSmallScreen()) 12.dp else 16.dp
