package cm.crfc.pointage.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Logout
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.ToggleOff
import androidx.compose.material.icons.rounded.ToggleOn
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cm.crfc.pointage.data.AuthRepository
import cm.crfc.pointage.model.User
import cm.crfc.pointage.model.UserRole
import cm.crfc.pointage.ui.components.CrfcCard
import cm.crfc.pointage.ui.components.HeaderCard
import cm.crfc.pointage.ui.components.LabelValue
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
    val users by authRepository.observeUsers().collectAsStateWithLifecycle(initialValue = emptyList())
    val currentUser = users.firstOrNull { it.id == user.id } ?: user
    var activeSheet by remember { mutableStateOf<SettingsSheet?>(null) }

    var firstName by remember { mutableStateOf(currentUser.firstName) }
    var lastName by remember { mutableStateOf(currentUser.lastName) }
    var jobTitle by remember { mutableStateOf(currentUser.jobTitle) }
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
            Text(currentUser.fullName, style = MaterialTheme.typography.headlineSmall)
            Text(currentUser.email, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(currentUser.jobTitle, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(if (currentUser.role == UserRole.ADMIN) "Administrateur" else "Agent", color = MaterialTheme.colorScheme.primary)
        }
        CrfcCard(modifier = Modifier.padding(horizontal = 16.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(onClick = {
                    firstName = currentUser.firstName
                    lastName = currentUser.lastName
                    jobTitle = currentUser.jobTitle
                    activeSheet = SettingsSheet.PROFILE
                }) {
                    Icon(Icons.Rounded.Edit, contentDescription = null)
                    Text("Profil", modifier = Modifier.padding(start = 8.dp))
                }
                OutlinedButton(onClick = {
                    currentPassword = ""
                    newPassword = ""
                    activeSheet = SettingsSheet.PASSWORD
                }) {
                    Icon(Icons.Rounded.Lock, contentDescription = null)
                    Text("Mot de passe", modifier = Modifier.padding(start = 8.dp))
                }
                Button(onClick = {
                    CoroutineScope(Dispatchers.Main).launch {
                        authRepository.logout()
                        onLoggedOut()
                    }
                }) {
                    Icon(Icons.Rounded.Logout, contentDescription = null)
                    Text("Quitter", modifier = Modifier.padding(start = 8.dp))
                }
            }
            message?.let { Text(it, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(top = 12.dp)) }
        }

        if (currentUser.role == UserRole.ADMIN) {
            CrfcCard(modifier = Modifier.padding(horizontal = 16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Gestion des utilisateurs", style = MaterialTheme.typography.titleMedium)
                    IconButton(onClick = {
                        createFirstName = ""
                        createLastName = ""
                        createEmail = ""
                        createJobTitle = ""
                        createPassword = ""
                        createRole = UserRole.AGENT
                        activeSheet = SettingsSheet.CREATE_USER
                    }) {
                        Icon(Icons.Rounded.Add, contentDescription = null)
                    }
                }
                Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.padding(top = 10.dp)) {
                    users.filter { it.id != currentUser.id }.forEach { other ->
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(other.fullName)
                                Text("${other.email} • ${if (other.role == UserRole.ADMIN) "Admin" else "Agent"}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Row {
                                IconButton(onClick = {
                                    CoroutineScope(Dispatchers.Main).launch {
                                        authRepository.toggleUserActive(currentUser, other.id)
                                    }
                                }) {
                                    Icon(if (other.isActive) Icons.Rounded.ToggleOn else Icons.Rounded.ToggleOff, contentDescription = null)
                                }
                                IconButton(onClick = {
                                    CoroutineScope(Dispatchers.Main).launch {
                                        authRepository.deleteUser(currentUser, other.id)
                                    }
                                }) {
                                    Icon(Icons.Rounded.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            }
        }

        CrfcCard(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            Text("Developpeur", style = MaterialTheme.typography.titleMedium)
            Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.padding(top = 12.dp)) {
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
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                when (activeSheet) {
                    SettingsSheet.PROFILE -> {
                        Text("Modifier le profil", style = MaterialTheme.typography.titleLarge)
                        OutlinedTextField(value = firstName, onValueChange = { firstName = it }, label = { Text("Prenom") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = lastName, onValueChange = { lastName = it }, label = { Text("Nom") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = jobTitle, onValueChange = { jobTitle = it }, label = { Text("Fonction") }, modifier = Modifier.fillMaxWidth())
                        Button(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = {
                                CoroutineScope(Dispatchers.Main).launch {
                                    val result = authRepository.updateProfile(currentUser, firstName, lastName, jobTitle)
                                    message = if (result.success) "Profil mis a jour." else result.error
                                    if (result.success) activeSheet = null
                                }
                            }
                        ) { Text("Enregistrer") }
                    }
                    SettingsSheet.PASSWORD -> {
                        Text("Changer le mot de passe", style = MaterialTheme.typography.titleLarge)
                        OutlinedTextField(value = currentPassword, onValueChange = { currentPassword = it }, label = { Text("Mot de passe actuel") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = newPassword, onValueChange = { newPassword = it }, label = { Text("Nouveau mot de passe") }, modifier = Modifier.fillMaxWidth())
                        Button(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = {
                                CoroutineScope(Dispatchers.Main).launch {
                                    val result = authRepository.updateProfile(currentUser, currentUser.firstName, currentUser.lastName, currentUser.jobTitle, currentPassword, newPassword)
                                    message = if (result.success) "Mot de passe modifie." else result.error
                                    if (result.success) activeSheet = null
                                }
                            }
                        ) { Text("Modifier") }
                    }
                    SettingsSheet.CREATE_USER -> {
                        Text("Nouvel utilisateur", style = MaterialTheme.typography.titleLarge)
                        OutlinedTextField(value = createFirstName, onValueChange = { createFirstName = it }, label = { Text("Prenom") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = createLastName, onValueChange = { createLastName = it }, label = { Text("Nom") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = createEmail, onValueChange = { createEmail = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = createJobTitle, onValueChange = { createJobTitle = it }, label = { Text("Fonction") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = createPassword, onValueChange = { createPassword = it }, label = { Text("Mot de passe") }, modifier = Modifier.fillMaxWidth())
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(onClick = { createRole = UserRole.AGENT }) {
                                Icon(Icons.Rounded.Person, contentDescription = null)
                                Text("Agent")
                            }
                            OutlinedButton(onClick = { createRole = UserRole.ADMIN }) {
                                Icon(Icons.Rounded.Person, contentDescription = null)
                                Text("Admin")
                            }
                        }
                        Button(
                            modifier = Modifier.fillMaxWidth(),
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
                        ) { Text("Creer") }
                    }
                    null -> Unit
                }
            }
        }
    }
}
