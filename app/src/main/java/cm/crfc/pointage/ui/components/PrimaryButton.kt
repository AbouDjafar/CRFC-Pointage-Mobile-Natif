package cm.crfc.pointage.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cm.crfc.pointage.ui.theme.Dimens
import cm.crfc.pointage.ui.theme.GreenAccent
import cm.crfc.pointage.ui.theme.NavyPrimary
import cm.crfc.pointage.ui.theme.OrangeAccent
import cm.crfc.pointage.ui.theme.TextOnPrimary

enum class ButtonVariant { NAVY, ORANGE, GREEN, GHOST }

@Composable
fun PrimaryButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: ButtonVariant = ButtonVariant.NAVY,
    icon: ImageVector? = null,
    enabled: Boolean = true
) {
    val shapedModifier = modifier
        .fillMaxWidth()
        .heightIn(min = Dimens.ButtonMinHeight)

    if (variant == ButtonVariant.GHOST) {
        OutlinedButton(
            onClick = onClick,
            modifier = shapedModifier,
            enabled = enabled,
            shape = RoundedCornerShape(Dimens.ButtonRadius),
            contentPadding = ButtonDefaults.ContentPadding
        ) {
            PrimaryButtonContent(label, icon, NavyPrimary)
        }
    } else {
        Button(
            onClick = onClick,
            modifier = shapedModifier,
            enabled = enabled,
            shape = RoundedCornerShape(Dimens.ButtonRadius),
            colors = ButtonDefaults.buttonColors(
                containerColor = when (variant) {
                    ButtonVariant.NAVY -> NavyPrimary
                    ButtonVariant.ORANGE -> OrangeAccent
                    ButtonVariant.GREEN -> GreenAccent
                    ButtonVariant.GHOST -> Color.Transparent
                },
                contentColor = TextOnPrimary
            ),
            contentPadding = ButtonDefaults.ContentPadding
        ) {
            PrimaryButtonContent(label, icon, TextOnPrimary)
        }
    }
}

@Composable
private fun PrimaryButtonContent(
    label: String,
    icon: ImageVector?,
    color: Color
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceSM),
        verticalAlignment = Alignment.CenterVertically
    ) {
        icon?.let { Icon(imageVector = it, contentDescription = null, tint = color) }
        Text(
            text = label,
            color = color,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(vertical = 2.dp)
        )
    }
}
