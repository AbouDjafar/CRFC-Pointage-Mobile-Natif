package cm.crfc.pointage.ui.users

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
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
import cm.crfc.pointage.ui.components.BottomSheetHeader
import cm.crfc.pointage.ui.components.EmployeeAvatar
import cm.crfc.pointage.ui.components.FormField
import cm.crfc.pointage.ui.components.SectionCard
import cm.crfc.pointage.ui.theme.Dimens
import cm.crfc.pointage.ui.theme.LocalCrfcUiExtras
import cm.crfc.pointage.ui.theme.TextSecondary
import cm.crfc.pointage.ui.theme.horizontalPadding
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserManagementScreen(
    currentUser: User,
    authRepository: AuthRepository,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val extras = LocalCrfcUiExtras.current
    val horizontalPadding = horizontalPadding()
    val users by authRepository.observeUsers().collectAsStateWithLifecycle(initialValue = emptyList())

    var showSheet by remember { mutableStateOf(false) }
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var jobTitle by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var role by remember { mutableStateOf(UserRole.AGENT) }

    val admins = users.filter { it.role == UserRole.ADMIN }
    val agents = users.filter { it.role == UserRole.AGENT }

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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceMD),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Outlined.ArrowBack, contentDescription = null)
                    }
                    Column {
                        Text("ADMINISTRATION", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                        Text("Gestion Utilisateurs", style = MaterialTheme.typography.headlineMedium)
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceMD)) {
                    UserMetric("TOTAL", users.size.toString(), modifier = Modifier.weight(1f))
                    UserMetric("ACTIFS", users.count { it.isActive }.toString(), modifier = Modifier.weight(1f))
                }
            }
        }

        item { SectionTitle("ADMINISTRATEURS", modifier = Modifier.padding(horizontal = horizontalPadding)) }
        items(admins, key = { it.id }) { user ->
            UserRow(
                user = user,
                modifier = Modifier.padding(horizontal = horizontalPadding),
                onToggle = { scope.launch { authRepository.toggleUserActive(currentUser, user.id) } }
            )
        }

        item { SectionTitle("AGENTS DE POINTAGE", modifier = Modifier.padding(horizontal = horizontalPadding)) }
        items(agents, key = { it.id }) { user ->
            UserRow(
                user = user,
                modifier = Modifier.padding(horizontal = horizontalPadding),
                onToggle = { scope.launch { authRepository.toggleUserActive(currentUser, user.id) } }
            )
        }

        item {
            Surface(
                onClick = {
                    firstName = ""
                    lastName = ""
                    email = ""
                    jobTitle = ""
                    password = ""
                    role = UserRole.AGENT
                    showSheet = true
                },
                color = extras.orangeAccent,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.padding(horizontal = horizontalPadding)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Outlined.Add, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary)
                    Text("Nouvel Utilisateur", color = MaterialTheme.colorScheme.onPrimary, style = MaterialTheme.typography.labelLarge)
                }
            }
        }

        item { Spacer(modifier = Modifier.navigationBarsPadding()) }
    }

    if (showSheet) {
        ModalBottomSheet(onDismissRequest = { showSheet = false }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = horizontalPadding, vertical = Dimens.SpaceLG),
                verticalArrangement = Arrangement.spacedBy(Dimens.SpaceLG)
            ) {
                BottomSheetHeader(title = "Nouvel utilisateur", onClose = { showSheet = false })
                FormField("Prenom", firstName, { firstName = it })
                FormField("Nom", lastName, { lastName = it })
                FormField("Email", email, { email = it })
                FormField("Fonction / Poste", jobTitle, { jobTitle = it })
                FormField("Mot de passe", password, { password = it })
                Row(horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceSM)) {
                    RoleToggle("Agent", role == UserRole.AGENT, onClick = { role = UserRole.AGENT }, modifier = Modifier.weight(1f))
                    RoleToggle("Admin", role == UserRole.ADMIN, onClick = { role = UserRole.ADMIN }, modifier = Modifier.weight(1f))
                }
                Surface(
                    onClick = {
                        scope.launch {
                            authRepository.createUser(currentUser, firstName, lastName, email, jobTitle, password, role)
                            showSheet = false
                        }
                    },
                    color = extras.orangeAccent,
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text(
                        text = "Creer l'utilisateur",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}

@Composable
private fun UserMetric(title: String, value: String, modifier: Modifier = Modifier) {
    SectionCard(modifier = modifier) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title, style = MaterialTheme.typography.labelMedium, color = TextSecondary)
            Text(value, style = MaterialTheme.typography.headlineMedium)
        }
    }
}

@Composable
private fun SectionTitle(title: String, modifier: Modifier = Modifier) {
    Text(text = title, modifier = modifier, style = MaterialTheme.typography.labelMedium, color = TextSecondary)
}

@Composable
private fun UserRow(
    user: User,
    modifier: Modifier = Modifier,
    onToggle: () -> Unit
) {
    val extras = LocalCrfcUiExtras.current
    SectionCard(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceMD),
            verticalAlignment = Alignment.CenterVertically
        ) {
            EmployeeAvatar(user.fullName)
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceSM), verticalAlignment = Alignment.CenterVertically) {
                    Text(user.fullName, style = MaterialTheme.typography.titleMedium)
                    if (user.role == UserRole.ADMIN) {
                        Surface(color = extras.orangeLight, shape = MaterialTheme.shapes.small) {
                            Text(
                                text = "ADMIN",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = extras.orangeAccent
                            )
                        }
                    }
                }
                Text(user.email, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                Text(user.jobTitle, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            }
            Surface(onClick = onToggle, color = MaterialTheme.colorScheme.surfaceVariant, shape = MaterialTheme.shapes.small) {
                Icon(Icons.Outlined.Edit, contentDescription = null, modifier = Modifier.padding(10.dp))
            }
        }
    }
}

@Composable
private fun RoleToggle(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
        shape = MaterialTheme.shapes.medium,
        modifier = modifier
    ) {
        Text(
            text = label,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.labelLarge
        )
    }
}
