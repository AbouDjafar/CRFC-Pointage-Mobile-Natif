package cm.crfc.pointage.ui.reglages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AdminPanelSettings
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.material.icons.outlined.Person
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cm.crfc.pointage.data.AuthRepository
import cm.crfc.pointage.model.User
import cm.crfc.pointage.model.UserRole
import cm.crfc.pointage.ui.components.EmployeeAvatar
import cm.crfc.pointage.ui.components.FormField
import cm.crfc.pointage.ui.components.SectionCard
import cm.crfc.pointage.ui.theme.Dimens
import cm.crfc.pointage.ui.theme.LocalCrfcUiExtras
import cm.crfc.pointage.ui.theme.TextSecondary
import cm.crfc.pointage.ui.theme.horizontalPadding
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    user: User,
    authRepository: AuthRepository,
    onLoggedOut: () -> Unit,
    onOpenUserManagement: (() -> Unit)? = null,
    onOpenRecurringAbsences: (() -> Unit)? = null
) {
    val scope = rememberCoroutineScope()
    val extras = LocalCrfcUiExtras.current
    val horizontalPadding = horizontalPadding()
    val users by authRepository.observeUsers().collectAsStateWithLifecycle(initialValue = emptyList())
    val currentUser = users.firstOrNull { it.id == user.id } ?: user

    var firstName by remember(currentUser.id) { mutableStateOf(currentUser.firstName) }
    var lastName by remember(currentUser.id) { mutableStateOf(currentUser.lastName) }
    var jobTitle by remember(currentUser.id) { mutableStateOf(currentUser.jobTitle) }
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var darkMode by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(Dimens.SpaceLG)
    ) {
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = horizontalPadding, vertical = Dimens.SpaceXL),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(Dimens.SpaceLG)
            ) {
                Text("MON PROFIL", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                EmployeeAvatar(currentUser.fullName, size = 72.dp)
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(currentUser.fullName, style = MaterialTheme.typography.headlineMedium)
                    Text(
                        if (currentUser.role == UserRole.ADMIN) "ADMINISTRATEUR" else "AGENT DE POINTAGE",
                        style = MaterialTheme.typography.labelMedium,
                        color = extras.orangeAccent
                    )
                }
            }
        }
        item {
            SectionCard(modifier = Modifier.padding(horizontal = horizontalPadding)) {
                Column(verticalArrangement = Arrangement.spacedBy(Dimens.SpaceLG)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceSM), verticalAlignment = Alignment.CenterVertically) {
                        androidx.compose.material3.Icon(Icons.Outlined.Person, contentDescription = null, tint = extras.orangeAccent)
                        Text("Informations personnelles", style = MaterialTheme.typography.titleMedium)
                    }
                    FormField("Prenom", firstName, { firstName = it })
                    FormField("Nom", lastName, { lastName = it })
                    FormField("Fonction / Poste", jobTitle, { jobTitle = it })
                    FormField("Email (identifiant)", currentUser.email, {}, enabled = false)
                }
            }
        }
        item {
            SectionCard(modifier = Modifier.padding(horizontal = horizontalPadding)) {
                Column(verticalArrangement = Arrangement.spacedBy(Dimens.SpaceLG)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceSM), verticalAlignment = Alignment.CenterVertically) {
                        androidx.compose.material3.Icon(Icons.Outlined.Lock, contentDescription = null, tint = extras.orangeAccent)
                        Text("Securite", style = MaterialTheme.typography.titleMedium)
                    }
                    Text("Changer le mot de passe", style = MaterialTheme.typography.bodyMedium)
                    FormField("Mot de passe actuel", currentPassword, { currentPassword = it })
                    FormField("Nouveau mot de passe", newPassword, { newPassword = it }, placeholder = "Minimum 6 caracteres")
                    Surface(
                        onClick = {
                            scope.launch {
                                val result = authRepository.updateProfile(
                                    currentUser = currentUser,
                                    firstName = firstName,
                                    lastName = lastName,
                                    jobTitle = jobTitle,
                                    currentPassword = currentPassword,
                                    newPassword = newPassword
                                )
                                message = if (result.success) "Profil mis a jour." else result.error
                            }
                        },
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Text(
                            text = "Mettre a jour le mot de passe",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
        }
        item {
            SectionCard(modifier = Modifier.padding(horizontal = horizontalPadding)) {
                Column(verticalArrangement = Arrangement.spacedBy(Dimens.SpaceLG)) {
                    Text("SESSION", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Mode sombre", style = MaterialTheme.typography.bodyLarge)
                        Switch(checked = darkMode, onCheckedChange = { darkMode = it })
                    }
                    Surface(
                        onClick = {
                            scope.launch {
                                authRepository.logout()
                                onLoggedOut()
                            }
                        },
                        color = MaterialTheme.colorScheme.surface,
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceSM),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            androidx.compose.material3.Icon(Icons.Outlined.Logout, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                            Text("Deconnexion", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            }
        }
        if (currentUser.role == UserRole.ADMIN) {
            item {
                SectionCard(modifier = Modifier.padding(horizontal = horizontalPadding)) {
                    Column(verticalArrangement = Arrangement.spacedBy(Dimens.SpaceLG)) {
                        Text("Administration", style = MaterialTheme.typography.titleMedium)
                        onOpenUserManagement?.let {
                            AdminLink("Gestion Utilisateurs", onClick = it)
                        }
                        onOpenRecurringAbsences?.let {
                            AdminLink("Absences Recurrentes", onClick = it)
                        }
                    }
                }
            }
        }
        message?.let {
            item {
                Text(
                    text = it,
                    modifier = Modifier.padding(horizontal = horizontalPadding),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
        item {
            Surface(
                onClick = {
                    scope.launch {
                        val result = authRepository.updateProfile(currentUser, firstName, lastName, jobTitle)
                        message = if (result.success) "Modifications enregistrees." else result.error
                    }
                },
                color = extras.orangeAccent,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.padding(horizontal = horizontalPadding)
            ) {
                Text(
                    text = "Enregistrer les modifications",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
        item { Spacer(modifier = Modifier.navigationBarsPadding()) }
    }
}

@Composable
private fun AdminLink(label: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceSM),
            verticalAlignment = Alignment.CenterVertically
        ) {
            androidx.compose.material3.Icon(Icons.Outlined.AdminPanelSettings, contentDescription = null)
            Text(label, style = MaterialTheme.typography.labelLarge)
        }
    }
}
