package cm.crfc.pointage.ui.reglages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cm.crfc.pointage.data.AuthRepository
import cm.crfc.pointage.model.User
import cm.crfc.pointage.model.UserRole
import cm.crfc.pointage.ui.components.AppHeader
import cm.crfc.pointage.ui.components.BottomSheetHeader
import cm.crfc.pointage.ui.components.ButtonVariant
import cm.crfc.pointage.ui.components.EmployeeAvatar
import cm.crfc.pointage.ui.components.FormField
import cm.crfc.pointage.ui.components.PrimaryButton
import cm.crfc.pointage.ui.components.SectionCard
import cm.crfc.pointage.ui.components.StatChip
import cm.crfc.pointage.ui.theme.Dimens
import cm.crfc.pointage.ui.theme.LocalCrfcUiExtras
import cm.crfc.pointage.ui.theme.TextSecondary
import cm.crfc.pointage.ui.theme.horizontalPadding
import kotlinx.coroutines.launch

private enum class SettingsSheet { PROFILE, PASSWORD, CREATE_USER }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    user: User,
    authRepository: AuthRepository,
    onLoggedOut: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val extras = LocalCrfcUiExtras.current
    val horizontalPadding = horizontalPadding()
    val users by authRepository.observeUsers().collectAsStateWithLifecycle(initialValue = emptyList())
    val currentUser = users.firstOrNull { it.id == user.id } ?: user

    var activeSheet by remember { mutableStateOf<SettingsSheet?>(null) }
    var firstName by remember(currentUser.id) { mutableStateOf(currentUser.firstName) }
    var lastName by remember(currentUser.id) { mutableStateOf(currentUser.lastName) }
    var jobTitle by remember(currentUser.id) { mutableStateOf(currentUser.jobTitle) }
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var createFirstName by remember { mutableStateOf("") }
    var createLastName by remember { mutableStateOf("") }
    var createEmail by remember { mutableStateOf("") }
    var createJobTitle by remember { mutableStateOf("") }
    var createPassword by remember { mutableStateOf("") }
    var createRole by remember { mutableStateOf(UserRole.AGENT) }
    var message by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(Dimens.SpaceLG)
    ) {
        item {
            AppHeader(
                title = "Reglages",
                subtitle = "Compte, utilisateurs et session"
            )
        }
        item {
            SectionCard(modifier = Modifier.padding(horizontal = horizontalPadding), highlighted = true) {
                Row(horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceMD)) {
                    EmployeeAvatar(currentUser.fullName, size = Dimens.AvatarSizeLg)
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(Dimens.SpaceXS)) {
                        Text(currentUser.fullName, style = MaterialTheme.typography.titleLarge)
                        Text(currentUser.email, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                        Text(currentUser.jobTitle, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        StatChip(
                            icon = Icons.Outlined.Person,
                            text = if (currentUser.role == UserRole.ADMIN) "Administrateur" else "Agent",
                            containerColor = if (currentUser.role == UserRole.ADMIN) extras.orangeLight else extras.greenLight,
                            contentColor = if (currentUser.role == UserRole.ADMIN) extras.orangeAccent else extras.greenAccent,
                            compact = true
                        )
                    }
                }
            }
        }
        item {
            SectionCard(modifier = Modifier.padding(horizontal = horizontalPadding)) {
                Text("Mon profil", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(Dimens.SpaceMD))
                PrimaryButton("Modifier le profil", { activeSheet = SettingsSheet.PROFILE }, variant = ButtonVariant.GHOST)
                Spacer(modifier = Modifier.height(Dimens.SpaceSM))
                PrimaryButton("Changer le mot de passe", {
                    currentPassword = ""
                    newPassword = ""
                    activeSheet = SettingsSheet.PASSWORD
                }, variant = ButtonVariant.GHOST)
                message?.let {
                    Spacer(modifier = Modifier.height(Dimens.SpaceSM))
                    Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                }
            }
        }
        if (currentUser.role == UserRole.ADMIN) {
            item {
                SectionCard(modifier = Modifier.padding(horizontal = horizontalPadding)) {
                    Text("Gestion des utilisateurs", style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.height(Dimens.SpaceMD))
                    PrimaryButton(
                        label = "Nouvel utilisateur",
                        onClick = {
                            createFirstName = ""
                            createLastName = ""
                            createEmail = ""
                            createJobTitle = ""
                            createPassword = ""
                            createRole = UserRole.AGENT
                            activeSheet = SettingsSheet.CREATE_USER
                        },
                        variant = ButtonVariant.GHOST
                    )
                    Spacer(modifier = Modifier.height(Dimens.SpaceLG))
                    Column(verticalArrangement = Arrangement.spacedBy(Dimens.SpaceMD)) {
                        users.filter { it.id != currentUser.id }.forEach { other ->
                            Row(horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceMD)) {
                                EmployeeAvatar(other.fullName)
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(other.fullName, style = MaterialTheme.typography.titleMedium)
                                    Text("${other.email} • ${other.jobTitle}", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                }
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceSM)) {
                                PrimaryButton(
                                    label = if (other.isActive) "Desactiver" else "Reactiver",
                                    onClick = { scope.launch { authRepository.toggleUserActive(currentUser, other.id) } },
                                    modifier = Modifier.weight(1f),
                                    variant = ButtonVariant.GHOST
                                )
                                PrimaryButton(
                                    label = "Supprimer",
                                    onClick = { scope.launch { authRepository.deleteUser(currentUser, other.id) } },
                                    modifier = Modifier.weight(1f),
                                    variant = ButtonVariant.GHOST
                                )
                            }
                        }
                    }
                }
            }
        }
        item {
            SectionCard(modifier = Modifier.padding(horizontal = horizontalPadding)) {
                Text("Session", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(Dimens.SpaceMD))
                PrimaryButton(
                    label = "Se deconnecter",
                    onClick = {
                        scope.launch {
                            authRepository.logout()
                            onLoggedOut()
                        }
                    },
                    variant = ButtonVariant.ORANGE
                )
            }
        }
        item {
            SectionCard(modifier = Modifier.padding(horizontal = horizontalPadding)) {
                Text("Developpeur", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(Dimens.SpaceSM))
                Text("A. A. Djafar", style = MaterialTheme.typography.titleMedium)
                Text("Cadre informaticien au CRFC", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                Text("djafar@crfc.cm", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            }
        }
        item { Spacer(modifier = Modifier.navigationBarsPadding()) }
    }

    if (activeSheet != null) {
        ModalBottomSheet(onDismissRequest = { activeSheet = null }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = horizontalPadding, vertical = Dimens.SpaceLG),
                verticalArrangement = Arrangement.spacedBy(Dimens.SpaceLG)
            ) {
                when (activeSheet) {
                    SettingsSheet.PROFILE -> {
                        BottomSheetHeader("Modifier le profil", onClose = { activeSheet = null })
                        FormField("Prenom", firstName, { firstName = it })
                        FormField("Nom", lastName, { lastName = it })
                        FormField("Fonction", jobTitle, { jobTitle = it })
                        PrimaryButton(
                            label = "Enregistrer",
                            onClick = {
                                scope.launch {
                                    val result = authRepository.updateProfile(currentUser, firstName, lastName, jobTitle)
                                    message = if (result.success) "Profil mis a jour." else result.error
                                    if (result.success) activeSheet = null
                                }
                            }
                        )
                    }
                    SettingsSheet.PASSWORD -> {
                        BottomSheetHeader("Changer le mot de passe", onClose = { activeSheet = null })
                        FormField("Mot de passe actuel", currentPassword, { currentPassword = it })
                        FormField("Nouveau mot de passe", newPassword, { newPassword = it })
                        PrimaryButton(
                            label = "Modifier",
                            onClick = {
                                scope.launch {
                                    val result = authRepository.updateProfile(
                                        currentUser,
                                        currentUser.firstName,
                                        currentUser.lastName,
                                        currentUser.jobTitle,
                                        currentPassword,
                                        newPassword
                                    )
                                    message = if (result.success) "Mot de passe modifie." else result.error
                                    if (result.success) activeSheet = null
                                }
                            }
                        )
                    }
                    SettingsSheet.CREATE_USER -> {
                        BottomSheetHeader("Nouvel utilisateur", onClose = { activeSheet = null })
                        FormField("Prenom", createFirstName, { createFirstName = it })
                        FormField("Nom", createLastName, { createLastName = it })
                        FormField("Email", createEmail, { createEmail = it })
                        FormField("Fonction", createJobTitle, { createJobTitle = it })
                        FormField("Mot de passe", createPassword, { createPassword = it })
                        Row(horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceSM)) {
                            PrimaryButton(
                                label = if (createRole == UserRole.AGENT) "Agent selectionne" else "Agent",
                                onClick = { createRole = UserRole.AGENT },
                                modifier = Modifier.weight(1f),
                                variant = if (createRole == UserRole.AGENT) ButtonVariant.NAVY else ButtonVariant.GHOST
                            )
                            PrimaryButton(
                                label = if (createRole == UserRole.ADMIN) "Admin selectionne" else "Admin",
                                onClick = { createRole = UserRole.ADMIN },
                                modifier = Modifier.weight(1f),
                                variant = if (createRole == UserRole.ADMIN) ButtonVariant.NAVY else ButtonVariant.GHOST
                            )
                        }
                        PrimaryButton(
                            label = "Creer l'utilisateur",
                            onClick = {
                                scope.launch {
                                    val result = authRepository.createUser(
                                        currentUser,
                                        createFirstName,
                                        createLastName,
                                        createEmail,
                                        createJobTitle,
                                        createPassword,
                                        createRole
                                    )
                                    message = if (result.success) "Utilisateur cree." else result.error
                                    if (result.success) activeSheet = null
                                }
                            }
                        )
                    }
                    null -> Unit
                }
            }
        }
    }
}
