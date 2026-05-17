package cm.crfc.pointage.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import cm.crfc.pointage.R
import cm.crfc.pointage.ui.theme.LocalCrfcUiExtras
import cm.crfc.pointage.ui.theme.NavyPrimary

@Composable
fun CrfcBrandMark(
    modifier: Modifier = Modifier,
    size: Dp = 72.dp,
    translucent: Boolean = false
) {
    val extras = LocalCrfcUiExtras.current
    val shape = RoundedCornerShape(if (size >= 72.dp) 28.dp else 20.dp)
    Box(
        modifier = modifier
            .size(size)
            .clip(shape)
            .background(
                Brush.linearGradient(
                    colors = if (translucent) {
                        listOf(Color.White.copy(alpha = 0.18f), Color.White.copy(alpha = 0.08f))
                    } else {
                        listOf(NavyPrimary, extras.navyLight)
                    }
                )
            )
            .border(1.dp, Color.White.copy(alpha = if (translucent) 0.18f else 0.12f), shape),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_launcher_foreground),
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .padding(if (size >= 72.dp) 12.dp else 10.dp),
            contentScale = ContentScale.Fit
        )
        if (size >= 72.dp) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            listOf(Color.White.copy(alpha = 0.12f), Color.Transparent)
                        )
                    )
            )
        }
    }
}
