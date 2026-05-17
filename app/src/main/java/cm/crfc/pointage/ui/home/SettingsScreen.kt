package cm.crfc.pointage.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Logout
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.ToggleOff
import androidx.compose.material.icons.rounded.ToggleOn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cm.crfc.pointage.data.AuthRepository
import cm.crfc.pointage.model.User
import cm.crfc.pointage.model.UserRole
import cm.crfc.pointage.ui.components.AvatarCircle
import cm.crfc.pointage.ui.components.ClickRow
import cm.crfc.pointage.ui.components.CrfcCard
import cm.crfc.pointage.ui.components.HeaderCard
import cm.crfc.pointage.ui.components.LabelValue
import cm.crfc.pointage.ui.components.SectionLabel
import cm.crfc.pointage.ui.components.SectionTitle
import cm.crfc.pointage.ui.components.SettingRow
import cm.crfc.pointage.ui.components.StatusBadge
import cm.crfc.pointage.ui.components.TextFieldBlock
import cm.crfc.pointage.ui.theme.LocalCrfcExtraColors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private enum class SettingsSheet { PROFILE, PASSWORD, CREATE_USER }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    user: User,
    authRepository: AuthRepository,
    onLoggedOut: () -> Unit
) {
    val extra = LocalCrfcExtraColors.current
    val users by authRepository.observeUsers().collectAsStateWithLifecycle(initialValue = emptyList())
    val currentUser = users.firstOrNull { it.id == user.id } ?: user
    var activeSheet by remember { mutableStateOf<SettingsSheet?>(null) }

    var firstName by remember(currentUser.id, currentUser.firstName) { mutableStateOf(currentUser.firstName) }
    var lastName by remember(currentUser.id, currentUser.lastName) { mutableStateOf(currentUser.lastName) }
    var jobTitle by remember(currentUser.id, currentUser.jobTitle) { mutableStateOf(currentUser.jobTitle) }
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var createFirstName by remember { mutableStateOf("") }
    var createLastName by remember { mutableStateOf("") }
    var createEmail by remember { mutableStateOf("") }
    var createJobTitle by remember { mutableStateOf("") }
    var createPassword by remember { mutableStateOf("") }
    var createRole by remember { mutableStateOf(UserRole.AGENT) }
    var message by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        HeaderCard(title = "Reglages", subtitle = "Compte et preferences")

        CrfcCard(modifier = Modifier.padding(horizontal = 16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AvatarCircle(text = currentUser.fullName, size = 80, color = MaterialTheme.colorScheme.primary)
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(currentUser.fullName, style = MaterialTheme.typography.titleLarge)
                    Text(currentUser.email, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(currentUser.jobTitle, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    StatusBadge(
                        text = if (currentUser.role == UserRole.ADMIN) "Admin" else "Agent",
                        color = if (currentUser.role == UserRole.ADMIN) MaterialTheme.colorScheme.primary else extra.success
                    )
                }
            }
        }

        CrfcCard(modifier = Modifier.padding(horizontal = 16.dp)) {
            SectionLabel("Mon profil")
            Column(modifier = Modifier.padding(top = 8.dp)) {
                SettingRow(
                    icon = Icons.Rounded.Person,
                    title = "Mon profil",
                    subtitle = "Nom complet et fonction"
                ) {
                    firstName = currentUser.firstName
                    lastName = currentUser.lastName
                    jobTitle = currentUser.jobTitle
                    activeSheet = SettingsSheet.PROFILE
                }
                SettingRow(
                    icon = Icons.Rounded.Lock,
                    title = "Mot de passe",
                    subtitle = "Modifier les acces du compte"
                ) {
                    currentPassword = ""
                    newPassword = ""
                    activeSheet = SettingsSheet.PASSWORD
                }
            }
            message?.let {
                Text(
                    text = it,
                    modifier = Modifier.padding(top = 6.dp),
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        if (currentUser.role == UserRole.ADMIN) {
            CrfcCard(modifier = Modifier.padding(horizontal = 16.dp)) {
                SectionTitle(
                    title = "Gestion utilisateurs",
                    action = {
                        OutlinedButton(onClick = {
                            createFirstName = ""
                            createLastName = ""
                            createEmail = ""
                            createJobTitle = ""
                            createPassword = ""
                            createRole = UserRole.AGENT
                            activeSheet = SettingsSheet.CREATE_USER
                        }) {
                            Icon(Icons.Rounded.Add, contentDescription = null)
                            Text("Nouvel utilisateur", modifier = Modifier.padding(start = 8.dp))
                        }
                    }
                )
                Column(
                    modifier = Modifier.padding(top = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    users.filter { it.id != currentUser.id }.forEach { other ->
                        ClickRow(
                            title = other.fullName,
                            subtitle = "${other.email} - ${if (other.role == UserRole.ADMIN) "Admin" else "Agent"} - ${other.jobTitle}",
                            tint = if (other.role == UserRole.ADMIN) MaterialTheme.colorScheme.primary else extra.success,
                            trailing = {
                                Row {
                                    IconButton(onClick = {
                                        CoroutineScope(Dispatchers.Main).launch {
                                            authRepository.toggleUserActive(currentUser, other.id)
                                        }
                                    }) {
                                        Icon(
                                            if (other.isActive) Icons.Rounded.ToggleOn else Icons.Rounded.ToggleOff,
                                            contentDescription = null,
                                            tint = if (other.isActive) extra.success else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    IconButton(onClick = {
                                        CoroutineScope(Dispatchers.Main).launch {
                                            authRepository.deleteUser(currentUser, other.id)
                                        }
                                    }) {
                                        Icon(Icons.Rounded.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                                    }
                                }
                            },
                            onClick = {}
                        )
                    }
                }
            }
        }

        CrfcCard(modifier = Modifier.padding(horizontal = 16.dp)) {
            SectionLabel("Session")
            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .padding(top = 12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                onClick = {
                    CoroutineScope(Dispatchers.Main).launch {
                        authRepository.logout()
                        onLoggedOut()
                    }
                }
            ) {
                Icon(Icons.Rounded.Logout, contentDescription = null)
                Text("Deconnexion", modifier = Modifier.padding(start = 8.dp))
            }
        }

        CrfcCard(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            SectionLabel("Developpeur")
            Column(
                modifier = Modifier.padding(top = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                LabelValue("Nom", "A. A. Djafar")
                LabelValue("Role", "Cadre Informaticien au CRFC")
                LabelValue("Mail", "djafar@crfc.cm")
            }
        }
    }

    if (activeSheet != null) {
        ModalBottomSheet(onDismissRequest = { activeSheet = null }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                when (activeSheet) {
                    SettingsSheet.PROFILE -> {
                        Text("Modifier le profil", style = MaterialTheme.typography.titleLarge)
                        TextFieldBlock(firstName, { firstName = it }, "Prenom")
                        TextFieldBlock(lastName, { lastName = it }, "Nom")
                        TextFieldBlock(jobTitle, { jobTitle = it }, "Fonction")
                        Button(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            onClick = {
                                CoroutineScope(Dispatchers.Main).launch {
                                    val result = authRepository.updateProfile(currentUser, firstName, lastName, jobTitle)
                                    message = if (result.success) "Profil mis a jour." else result.error
                                    if (result.success) activeSheet = null
                                }
                            }
                        ) {
                            Text("Enregistrer")
                        }
                    }

                    SettingsSheet.PASSWORD -> {
                        Text("Changer le mot de passe", style = MaterialTheme.typography.titleLarge)
                        TextFieldBlock(currentPassword, { currentPassword = it }, "Mot de passe actuel")
                        TextFieldBlock(newPassword, { newPassword = it }, "Nouveau mot de passe")
                        Button(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            onClick = {
                                CoroutineScope(Dispatchers.Main).launch {
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
                        ) {
                            Text("Modifier")
                        }
                    }

                    SettingsSheet.CREATE_USER -> {
                        Text("Nouvel utilisateur", style = MaterialTheme.typography.titleLarge)
                        TextFieldBlock(createFirstName, { createFirstName = it }, "Prenom")
                        TextFieldBlock(createLastName, { createLastName = it }, "Nom")
                        TextFieldBlock(createEmail, { createEmail = it }, "Email")
                        TextFieldBlock(createJobTitle, { createJobTitle = it }, "Fonction")
                        TextFieldBlock(createPassword, { createPassword = it }, "Mot de passe")
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(onClick = { createRole = UserRole.AGENT }) {
                                Text(if (createRole == UserRole.AGENT) "Agent choisi" else "Agent")
                            }
                            OutlinedButton(onClick = { createRole = UserRole.ADMIN }) {
                                Text(if (createRole == UserRole.ADMIN) "Admin choisi" else "Admin")
                            }
                        }
                        Button(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            onClick = {
                                CoroutineScope(Dispatchers.Main).launch {
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
                        ) {
                            Text("Creer")
                        }
                    }

                    null -> Unit
                }
            }
        }
    }
}
