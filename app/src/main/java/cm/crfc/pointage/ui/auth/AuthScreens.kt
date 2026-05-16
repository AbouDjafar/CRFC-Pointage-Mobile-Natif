package cm.crfc.pointage.ui.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PersonAdd
import androidx.compose.material.icons.rounded.Work
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import cm.crfc.pointage.R
import cm.crfc.pointage.data.AuthRepository
import cm.crfc.pointage.ui.theme.LocalCrfcExtraColors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    authRepository: AuthRepository,
    onSuccess: () -> Unit,
    onRegister: () -> Unit
) {
    val extra = LocalCrfcExtraColors.current
    var loginId by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(extra.headerStart, extra.headerEnd)))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.bg_pattern),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(210.dp)
                    .clip(RoundedCornerShape(28.dp)),
                contentScale = ContentScale.Crop
            )
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(22.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Surface(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f), shape = CircleShape) {
                            Image(
                                painter = painterResource(R.drawable.logo_crfc_pointage),
                                contentDescription = null,
                                modifier = Modifier.padding(12.dp).size(28.dp)
                            )
                        }
                        Column {
                            Text("CRFC Pointage", style = MaterialTheme.typography.headlineSmall)
                            Text("Connexion a l'espace de pointage", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    AuthField(loginId, { loginId = it }, "Identifiant (email ou login)", Icons.Rounded.Person)
                    AuthField(password, { password = it }, "Mot de passe", Icons.Rounded.Lock, secure = true)
                    error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        onClick = {
                            CoroutineScope(Dispatchers.Main).launch {
                                error = null
                                if (authRepository.login(loginId, password)) {
                                    onSuccess()
                                } else {
                                    error = "Identifiant ou mot de passe incorrect, ou compte desactive."
                                }
                            }
                        }
                    ) {
                        Text("Se connecter", modifier = Modifier.padding(vertical = 6.dp))
                    }
                    TextButton(onClick = onRegister, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                        Text("Creer un compte agent")
                    }
                }
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
    val extra = LocalCrfcExtraColors.current
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var jobTitle by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var success by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(extra.headerStart, extra.headerEnd)))
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        IconButton(onClick = onBack) {
            Icon(Icons.Rounded.ArrowBack, contentDescription = null, tint = androidx.compose.ui.graphics.Color.White)
        }
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(22.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Nouveau compte agent", style = MaterialTheme.typography.headlineSmall)
                Text("La partie avant @ servira aussi de login court.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                AuthField(firstName, { firstName = it }, "Prenom", Icons.Rounded.Person)
                AuthField(lastName, { lastName = it }, "Nom", Icons.Rounded.PersonAdd)
                AuthField(email, { email = it }, "Email", Icons.Rounded.Person)
                AuthField(jobTitle, { jobTitle = it }, "Fonction", Icons.Rounded.Work)
                AuthField(password, { password = it }, "Mot de passe", Icons.Rounded.Lock, secure = true)
                AuthField(confirmPassword, { confirmPassword = it }, "Confirmer le mot de passe", Icons.Rounded.Lock, secure = true)
                error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                success?.let { Text(it, color = MaterialTheme.colorScheme.tertiary) }
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    onClick = {
                        CoroutineScope(Dispatchers.Main).launch {
                            error = null
                            success = null
                            if (password != confirmPassword) {
                                error = "Les mots de passe ne correspondent pas."
                                return@launch
                            }
                            val result = authRepository.register(firstName, lastName, email, jobTitle, password)
                            if (result.success) {
                                success = "Compte cree avec succes. Vous pouvez maintenant vous connecter."
                                onRegistered()
                            } else {
                                error = result.error
                            }
                        }
                    }
                ) {
                    Text("Creer le compte", modifier = Modifier.padding(vertical = 6.dp))
                }
            }
        }
    }
}

@Composable
private fun AuthField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    secure: Boolean = false
) {
    OutlinedTextField(
        modifier = Modifier.fillMaxWidth(),
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        shape = RoundedCornerShape(18.dp),
        leadingIcon = { Icon(icon, contentDescription = null) },
        visualTransformation = if (secure) PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None
    )
}

