package cm.crfc.pointage.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import cm.crfc.pointage.ui.theme.AvatarBg
import cm.crfc.pointage.ui.theme.Dimens
import cm.crfc.pointage.ui.theme.Divider
import cm.crfc.pointage.ui.theme.TextSecondary

@Composable
fun FormField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = label,
    leadingIcon: ImageVector? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    singleLine: Boolean = true,
    enabled: Boolean = true,
    visualTransformation: VisualTransformation = VisualTransformation.None
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = Dimens.InputMinHeight),
            placeholder = { Text(placeholder) },
            leadingIcon = leadingIcon?.let { icon -> { Icon(icon, contentDescription = null) } },
            trailingIcon = trailingIcon,
            singleLine = singleLine,
            enabled = enabled,
            shape = RoundedCornerShape(12.dp),
            visualTransformation = visualTransformation,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = Divider,
                focusedContainerColor = AvatarBg.copy(alpha = 0.45f),
                unfocusedContainerColor = AvatarBg.copy(alpha = 0.45f)
            )
        )
    }
}
