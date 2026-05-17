package cm.crfc.pointage.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import cm.crfc.pointage.ui.theme.AvatarBg
import cm.crfc.pointage.ui.theme.Dimens
import cm.crfc.pointage.ui.theme.NavyPrimary

@Composable
fun EmployeeAvatar(
    name: String,
    modifier: Modifier = Modifier,
    size: Dp = Dimens.AvatarSize
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(AvatarBg),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initialsFrom(name),
            style = if (size > Dimens.AvatarSize) MaterialTheme.typography.titleMedium else MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = NavyPrimary
        )
    }
}

private fun initialsFrom(name: String): String =
    name.split(" ")
        .filter { it.isNotBlank() }
        .take(2)
        .mapNotNull { it.firstOrNull()?.uppercaseChar()?.toString() }
        .joinToString("")
        .ifBlank { "?" }
