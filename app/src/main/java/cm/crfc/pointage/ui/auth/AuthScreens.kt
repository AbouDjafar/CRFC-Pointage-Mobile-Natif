package cm.crfc.pointage.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Login
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cm.crfc.pointage.data.AuthRepository
import cm.crfc.pointage.ui.components.CrfcBrandMark
import cm.crfc.pointage.ui.components.FormField
import cm.crfc.pointage.ui.components.PrimaryButton
import cm.crfc.pointage.ui.components.SectionCard
import cm.crfc.pointage.ui.theme.Dimens
import cm.crfc.pointage.ui.theme.TextSecondary
import cm.crfc.pointage.ui.theme.horizontalPadding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    authRepository: AuthRepository,
    onSuccess: () -> Unit,
    onRegister: () -> Unit
) {
    val horizontalPadding = horizontalPadding()
    var loginId by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = horizontalPadding, vertical = Dimens.SpaceXXL),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(Dimens.SpaceLG)
            ) {
                Spacer(modifier = Modifier.height(20.dp))
                CrfcBrandMark(size = 88.dp)
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("CRFC Pointage", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
                    Text("Systeme de Gestion des Presences", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                }
            }

            SectionCard(modifier = Modifier.fillMaxWidth(), highlighted = true) {
                Column(verticalArrangement = Arrangement.spacedBy(Dimens.SpaceLG)) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(Dimens.SpaceSM)) {
                        Text("Connexion", style = MaterialTheme.typography.headlineMedium)
                        Text(
                            "Veuillez entrer vos identifiants pour acceder au tableau de bord.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                    FormField(
                        label = "Identifiant ou email",
                        value = loginId,
                        onValueChange = { loginId = it },
                        placeholder = "ex : j.dupont",
                        leadingIcon = Icons.Outlined.Person
                    )
                    FormField(
                        label = "Mot de passe",
                        value = password,
                        onValueChange = { password = it },
                        placeholder = "********",
                        leadingIcon = Icons.Outlined.Lock
                    )
                    Text(
                        text = "Mot de passe oublie ?",
                        modifier = Modifier.align(Alignment.End),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                    error?.let {
                        Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                    }
                    PrimaryButton(
                        label = "Se connecter",
                        onClick = {
                            CoroutineScope(Dispatchers.Main).launch {
                                error = null
                                if (authRepository.login(loginId, password)) onSuccess()
                                else error = "Identifiants invalides ou compte inactif."
                            }
                        },
                        variant = cm.crfc.pointage.ui.components.ButtonVariant.ORANGE,
                        icon = Icons.Outlined.Login
                    )
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Civic Functionalism v2.4", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                Text("Acces securise reserve au personnel du CRFC", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            }
        }
    }
}

@Composable
fun RegisterScreen(
    authRepository: AuthRepository,
    onBack: () -> Unit,
    onRegistered: () -> Unit
) {
    LoginScreen(authRepository = authRepository, onSuccess = onRegistered, onRegister = onBack)
}
