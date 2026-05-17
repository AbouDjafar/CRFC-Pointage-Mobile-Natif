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
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material.icons.rounded.Work
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import cm.crfc.pointage.R
import cm.crfc.pointage.data.AuthRepository
import cm.crfc.pointage.ui.components.AvatarCircle
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
        Image(
            painter = painterResource(id = R.drawable.bg_pattern),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            alpha = 0.1f
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 28.dp),
            verticalArrangement = Arrangement.Center
        ) {
            AuthHero(
                title = "CRFC Pointage",
                subtitle = "Application mobile native de pointage"
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 18.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Connexion", style = MaterialTheme.typography.titleLarge)
                        Text(
                            "Renseigne ton identifiant et ton mot de passe pour acceder a l'espace de pointage.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    AuthField(
                        value = loginId,
                        onValueChange = { loginId = it },
                        label = "Identifiant ou email",
                        icon = Icons.Rounded.Person
                    )
                    AuthField(
                        value = password,
                        onValueChange = { password = it },
                        label = "Mot de passe",
                        icon = Icons.Rounded.Lock,
                        secure = true
                    )

                    AnimatedVisibility(
                        visible = error != null,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Text(
                            text = error.orEmpty(),
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    Button(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
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
                        Text("Se connecter")
                    }

                    OutlinedButton(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.secondary),
                        onClick = onRegister
                    ) {
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(extra.headerStart, extra.headerEnd)))
    ) {
        Image(
            painter = painterResource(id = R.drawable.bg_pattern),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            alpha = 0.09f
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp)),
                color = Color.White.copy(alpha = 0.08f)
            ) {
                Box(modifier = Modifier.padding(18.dp)) {
                    Image(
                        painter = painterResource(id = R.drawable.bg_pattern),
                        contentDescription = null,
                        modifier = Modifier.matchParentSize(),
                        contentScale = ContentScale.Crop,
                        alpha = 0.12f
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Surface(
                            modifier = Modifier.size(42.dp),
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.14f)
                        ) {
                            IconButton(onClick = onBack) {
                                Icon(Icons.Rounded.ArrowBack, contentDescription = null, tint = Color.White)
                            }
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                "Nouveau compte",
                                style = MaterialTheme.typography.titleLarge,
                                color = Color.White
                            )
                            Text(
                                "Creer un compte agent pour acceder au pointage CRFC.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.78f)
                            )
                        }
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        AuthField(
                            value = firstName,
                            onValueChange = { firstName = it },
                            label = "Prenom",
                            icon = Icons.Rounded.Person,
                            modifier = Modifier.weight(1f)
                        )
                        AuthField(
                            value = lastName,
                            onValueChange = { lastName = it },
                            label = "Nom",
                            icon = Icons.Rounded.Person,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    AuthField(
                        value = email,
                        onValueChange = { email = it },
                        label = "Email professionnel",
                        icon = Icons.Rounded.Email
                    )
                    AuthField(
                        value = jobTitle,
                        onValueChange = { jobTitle = it },
                        label = "Fonction",
                        icon = Icons.Rounded.Work
                    )
                    AuthField(
                        value = password,
                        onValueChange = { password = it },
                        label = "Mot de passe",
                        icon = Icons.Rounded.Lock,
                        secure = true
                    )
                    AuthField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        label = "Confirmation",
                        icon = Icons.Rounded.Lock,
                        secure = true
                    )

                    AnimatedVisibility(
                        visible = success != null,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Text(
                            text = success.orEmpty(),
                            color = extra.success,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    AnimatedVisibility(
                        visible = error != null,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Text(
                            text = error.orEmpty(),
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    Button(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
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
                                    success = "Compte cree avec succes. Tu peux maintenant te connecter."
                                    onRegistered()
                                } else {
                                    error = result.error
                                }
                            }
                        }
                    ) {
                        Text("Creer le compte")
                    }

                    TextButton(
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        onClick = onBack
                    ) {
                        Text("Deja un compte ? Se connecter")
                    }
                }
            }
        }
    }
}

@Composable
private fun AuthHero(
    title: String,
    subtitle: String
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp)),
        color = Color.White.copy(alpha = 0.08f)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(210.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.bg_pattern),
                contentDescription = null,
                modifier = Modifier.matchParentSize(),
                contentScale = ContentScale.Crop,
                alpha = 0.14f
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    modifier = Modifier.size(88.dp),
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.16f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        AvatarCircle(
                            text = "CR",
                            size = 64,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
                Text(
                    text = title,
                    modifier = Modifier.padding(top = 16.dp),
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White
                )
                Text(
                    text = subtitle,
                    modifier = Modifier.padding(top = 6.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.8f)
                )
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
    modifier: Modifier = Modifier,
    secure: Boolean = false
) {
    var visible by remember { mutableStateOf(false) }
    OutlinedTextField(
        modifier = modifier.fillMaxWidth(),
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = { Icon(icon, contentDescription = null) },
        trailingIcon = {
            if (secure) {
                IconButton(onClick = { visible = !visible }) {
                    Icon(
                        if (visible) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                        contentDescription = null
                    )
                }
            }
        },
        shape = RoundedCornerShape(10.dp),
        visualTransformation = if (secure && !visible) {
            PasswordVisualTransformation()
        } else {
            VisualTransformation.None
        },
        singleLine = true
    )
}
