package cm.crfc.pointage.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import cm.crfc.pointage.ui.theme.Dimens
import cm.crfc.pointage.ui.theme.Divider

@Composable
fun SectionCard(
    modifier: Modifier = Modifier,
    highlighted: Boolean = false,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Dimens.CardRadius),
        colors = CardDefaults.cardColors(containerColor = cm.crfc.pointage.ui.theme.Surface),
        border = BorderStroke(1.dp, Divider),
        elevation = CardDefaults.cardElevation(defaultElevation = if (highlighted) 4.dp else 2.dp)
    ) {
        Column(modifier = Modifier.padding(Dimens.SpaceLG), content = { content() })
    }
}

@Composable
fun SectionHeader(
    title: String,
    count: Int,
    accentColor: Color,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onAdd: (() -> Unit)? = null
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceSM)
    ) {
        Surface(
            shape = CircleShape,
            color = accentColor.copy(alpha = 0.12f)
        ) {
            Box(
                modifier = Modifier.size(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(16.dp))
            }
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.weight(1f)
        )
        if (count > 0) {
            CountBadge(count = count, color = accentColor)
        }
        if (onAdd != null) {
            Surface(
                onClick = onAdd,
                shape = RoundedCornerShape(10.dp),
                color = accentColor.copy(alpha = 0.1f),
                modifier = Modifier.size(Dimens.TouchTarget)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Rounded.Add, contentDescription = null, tint = accentColor)
                }
            }
        }
    }
}
