package cm.crfc.pointage.ui.reglages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cm.crfc.pointage.data.AuthRepository
import cm.crfc.pointage.model.User
import cm.crfc.pointage.ui.components.SectionCard
import cm.crfc.pointage.ui.theme.Dimens
import cm.crfc.pointage.ui.theme.TextSecondary
import cm.crfc.pointage.ui.theme.horizontalPadding
import kotlinx.coroutines.launch

@Composable
fun PreferencesScreen(
    user: User,
    authRepository: AuthRepository,
    onLoggedOut: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val horizontalPadding = horizontalPadding()
    var darkMode by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(Dimens.SpaceLG)
    ) {
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = horizontalPadding, vertical = Dimens.SpaceXL),
                verticalArrangement = Arrangement.spacedBy(Dimens.SpaceLG)
            ) {
                Text("REGLAGES", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                Text("Preferences", style = MaterialTheme.typography.headlineMedium)
                SectionCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceSM), verticalAlignment = Alignment.CenterVertically) {
                            androidx.compose.material3.Icon(Icons.Outlined.Palette, contentDescription = null)
                            Text("Mode sombre", style = MaterialTheme.typography.bodyLarge)
                        }
                        Switch(checked = darkMode, onCheckedChange = { darkMode = it })
                    }
                }
                SectionCard {
                    Column(verticalArrangement = Arrangement.spacedBy(Dimens.SpaceSM)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceSM), verticalAlignment = Alignment.CenterVertically) {
                            androidx.compose.material3.Icon(Icons.Outlined.Info, contentDescription = null)
                            Text("A propos", style = MaterialTheme.typography.titleMedium)
                        }
                        Text("Utilisateur: ${user.fullName}", style = MaterialTheme.typography.bodyMedium)
                        Text("Version: Civic Functionalism v2.4", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                        Text("Acces securise reserve au personnel du CRFC", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    }
                }
                Surface(
                    onClick = {
                        scope.launch {
                            authRepository.logout()
                            onLoggedOut()
                        }
                    },
                    color = MaterialTheme.colorScheme.tertiary,
                    shape = MaterialTheme.shapes.medium
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        androidx.compose.material3.Icon(Icons.Outlined.Logout, contentDescription = null, tint = MaterialTheme.colorScheme.onTertiary)
                        Spacer(modifier = Modifier.size(6.dp))
                        Text("Se deconnecter", color = MaterialTheme.colorScheme.onTertiary, style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }
        item { Spacer(modifier = Modifier.navigationBarsPadding()) }
    }
}
