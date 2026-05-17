package cm.crfc.pointage.ui.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cm.crfc.pointage.ui.theme.Background
import cm.crfc.pointage.ui.theme.Dimens
import cm.crfc.pointage.ui.theme.Divider
import cm.crfc.pointage.ui.theme.LocalCrfcUiExtras

data class BottomDestination(
    val route: String,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

val BottomDestinations = listOf(
    BottomDestination(Routes.TAB_HOME, "Accueil", Icons.Outlined.Home),
    BottomDestination(Routes.TAB_PERSONNEL, "Personnel", Icons.Outlined.Group),
    BottomDestination(Routes.TAB_SETTINGS, "Reglages", Icons.Outlined.Settings)
)

@Composable
fun CrfcBottomBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit
) {
    Surface(
        tonalElevation = 0.dp,
        shadowElevation = 8.dp,
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, Divider)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .height(Dimens.BottomNavHeight)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomDestinations.forEach { destination ->
                BottomNavItem(
                    destination = destination,
                    selected = currentRoute == destination.route,
                    onClick = { onNavigate(destination.route) }
                )
            }
        }
    }
}

@Composable
private fun RowScope.BottomNavItem(
    destination: BottomDestination,
    selected: Boolean,
    onClick: () -> Unit
) {
    val extras = LocalCrfcUiExtras.current
    val iconColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
    Surface(
        onClick = onClick,
        color = if (selected) extras.orangeLight else Background,
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.weight(1f)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier.size(28.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = destination.icon,
                    contentDescription = null,
                    tint = if (selected) extras.orangeAccent else iconColor
                )
            }
            Text(
                text = destination.label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = iconColor,
                maxLines = 1
            )
        }
    }
}
