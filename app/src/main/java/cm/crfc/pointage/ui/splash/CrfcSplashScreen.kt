package cm.crfc.pointage.ui.splash

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cm.crfc.pointage.ui.components.CrfcBrandMark
import cm.crfc.pointage.ui.theme.LocalCrfcUiExtras
import cm.crfc.pointage.ui.theme.NavyPrimary
import kotlinx.coroutines.delay

@Composable
fun CrfcSplashRoute(onFinished: () -> Unit) {
    var started by remember { mutableStateOf(false) }
    val contentAlpha by animateFloatAsState(
        targetValue = if (started) 1f else 0f,
        animationSpec = tween(850, easing = FastOutSlowInEasing),
        label = "splashAlpha"
    )
    val contentOffset by animateFloatAsState(
        targetValue = if (started) 0f else 28f,
        animationSpec = tween(900, easing = FastOutSlowInEasing),
        label = "splashOffset"
    )
    val progress by animateFloatAsState(
        targetValue = if (started) 1f else 0f,
        animationSpec = tween(2200, delayMillis = 850, easing = FastOutSlowInEasing),
        label = "loaderProgress"
    )

    LaunchedEffect(Unit) {
        started = true
        delay(3000)
        onFinished()
    }

    CrfcSplashScreen(
        alpha = contentAlpha,
        offsetY = contentOffset.dp,
        progress = progress
    )
}

@Composable
private fun CrfcSplashScreen(
    alpha: Float,
    offsetY: androidx.compose.ui.unit.Dp,
    progress: Float
) {
    val extras = LocalCrfcUiExtras.current
    val ringRotation by rememberInfiniteTransition(label = "splashRing").animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ringRotation"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.linearGradient(listOf(NavyPrimary, extras.navyLight)))
    ) {
        DecorativeCircle(620.dp, (-210).dp, (-210).dp, Alignment.BottomEnd)
        DecorativeCircle(420.dp, (-110).dp, (-110).dp, Alignment.BottomEnd)
        DecorativeCircle(220.dp, 0.dp, 0.dp, Alignment.BottomEnd)
        DecorativeCircle(520.dp, (-190).dp, (-210).dp, Alignment.TopStart)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 28.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "9:41",
                    modifier = Modifier.align(Alignment.TopStart),
                    color = Color.White.copy(alpha = 0.72f),
                    style = MaterialTheme.typography.labelSmall
                )
                Text(
                    text = "5G",
                    modifier = Modifier.align(Alignment.TopEnd),
                    color = Color.White.copy(alpha = 0.72f),
                    style = MaterialTheme.typography.labelSmall
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Column(
                modifier = Modifier
                    .offset(y = offsetY)
                    .alpha(alpha),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Box(
                        modifier = Modifier
                            .size(164.dp)
                            .rotate(ringRotation)
                            .border(
                                width = 2.dp,
                                brush = Brush.sweepGradient(
                                    listOf(
                                        extras.orangeAccent.copy(alpha = 0.72f),
                                        Color.Transparent,
                                        Color.Transparent,
                                        extras.orangeAccent.copy(alpha = 0.08f)
                                    )
                                ),
                                shape = CircleShape
                            )
                    )
                    CrfcBrandMark(size = 140.dp, translucent = true)
                }

                Spacer(modifier = Modifier.height(32.dp))
                Text(
                    text = "CRFC",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 6.sp
                    ),
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(14.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.42f)
                        .height(2.dp)
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color.Transparent, extras.orangeAccent, Color.Transparent)
                            ),
                            RoundedCornerShape(999.dp)
                        )
                )
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "POINTAGE",
                    style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 4.sp),
                    color = Color.White.copy(alpha = 0.68f)
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Column(
                modifier = Modifier.alpha(alpha),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Surface(
                    modifier = Modifier.fillMaxWidth(0.46f),
                    color = Color.White.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(999.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(progress.coerceIn(0f, 1f))
                                .height(2.dp)
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(extras.orangeAccent, Color(0xFFFBBF24))
                                    ),
                                    RoundedCornerShape(999.dp)
                                )
                        )
                    }
                }
                Text(
                    text = "CHARGEMENT...",
                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 2.sp),
                    color = Color.White.copy(alpha = 0.4f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun BoxScope.DecorativeCircle(
    size: androidx.compose.ui.unit.Dp,
    xOffset: androidx.compose.ui.unit.Dp,
    yOffset: androidx.compose.ui.unit.Dp,
    alignment: Alignment
) {
    val pulse by rememberInfiniteTransition(label = "circlePulse$size").animateFloat(
        initialValue = 0.98f,
        targetValue = 1.03f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )
    Box(
        modifier = Modifier
            .align(alignment)
            .offset(x = xOffset, y = yOffset)
            .size(size * pulse)
            .border(1.dp, Color.White.copy(alpha = 0.06f), CircleShape)
    )
}
