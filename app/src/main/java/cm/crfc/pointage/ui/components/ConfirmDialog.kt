package cm.crfc.pointage.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun ConfirmDialog(
    title: String,
    message: String,
    confirmLabel: String,
    dismissLabel: String = "Annuler",
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, style = MaterialTheme.typography.titleLarge) },
        text = { Text(message, style = MaterialTheme.typography.bodyMedium) },
        confirmButton = {
            PrimaryButton(
                label = confirmLabel,
                onClick = onConfirm,
                variant = ButtonVariant.NAVY
            )
        },
        dismissButton = {
            PrimaryButton(
                label = dismissLabel,
                onClick = onDismiss,
                variant = ButtonVariant.GHOST
            )
        }
    )
}
