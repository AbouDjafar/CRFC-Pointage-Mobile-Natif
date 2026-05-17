package cm.crfc.pointage.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import cm.crfc.pointage.R
import cm.crfc.pointage.ui.theme.Dimens
import cm.crfc.pointage.ui.theme.LocalCrfcUiExtras
import cm.crfc.pointage.ui.theme.NavyPrimary
import cm.crfc.pointage.ui.theme.TextOnPrimary
import cm.crfc.pointage.ui.theme.horizontalPadding

@Composable
fun AppHeader(
    title: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier,
    statusText: String? = null,
    statusContainerColor: Color = Color.Black.copy(alpha = 0.18f),
    statusContentColor: Color = TextOnPrimary,
    actions: @Composable (RowScope.() -> Unit)? = null,
    bottomContent: @Composable (ColumnScope.() -> Unit)? = null
) {
    val extras = LocalCrfcUiExtras.current
    val horizontalPadding = horizontalPadding()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(Brush.linearGradient(listOf(NavyPrimary, extras.navyLight)))
            .statusBarsPadding()
    ) {
        Image(
            painter = painterResource(R.drawable.bg_pattern),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            alpha = 0.12f
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = horizontalPadding, vertical = Dimens.SpaceXL),
            verticalArrangement = Arrangement.spacedBy(Dimens.SpaceSM)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    subtitle?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextOnPrimary.copy(alpha = 0.72f)
                        )
                    }
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineLarge,
                        color = TextOnPrimary
                    )
                    statusText?.let {
                        HeaderStatusBadge(
                            text = it,
                            containerColor = statusContainerColor,
                            contentColor = statusContentColor
                        )
                    }
                }
                if (actions != null) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceSM),
                        verticalAlignment = Alignment.CenterVertically,
                        content = actions
                    )
                }
            }

            if (bottomContent != null) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(Dimens.SpaceSM),
                    content = bottomContent
                )
            }
        }
    }
}

@Composable
fun HeaderActionPill(
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier,
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = Color.White.copy(alpha = 0.16f)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            style = MaterialTheme.typography.labelMedium,
            color = TextOnPrimary
        )
    }
}

@Composable
fun HeaderStatusBadge(
    text: String,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = containerColor,
        shape = RoundedCornerShape(Dimens.ChipRadius)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelSmall,
            color = contentColor
        )
    }
}
