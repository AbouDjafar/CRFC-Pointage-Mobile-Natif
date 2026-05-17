package cm.crfc.pointage.ui.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.matchParentSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material.icons.outlined.WorkOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import cm.crfc.pointage.R
import cm.crfc.pointage.data.AuthRepository
import cm.crfc.pointage.ui.components.EmployeeAvatar
import cm.crfc.pointage.ui.components.FormField
import cm.crfc.pointage.ui.components.PrimaryButton
import cm.crfc.pointage.ui.components.SectionCard
import cm.crfc.pointage.ui.theme.Dimens
import cm.crfc.pointage.ui.theme.LocalCrfcUiExtras
import cm.crfc.pointage.ui.theme.NavyPrimary
import cm.crfc.pointage.ui.theme.TextOnPrimary
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
    var loginId by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var passwordVisible by remember { mutableStateOf(false) }

    AuthScaffold(
        title = "Pointage CRFC",
        subtitle = "Un suivi journalier clair, rapide et elegant.",
        kicker = "Connexion"
    ) {
        SectionCard(highlighted = true) {
            Column(verticalArrangement = Arrangement.spacedBy(Dimens.SpaceLG)) {
                Column(verticalArrangement = Arrangement.spacedBy(Dimens.SpaceXS)) {
                    Text("Bienvenue", style = MaterialTheme.typography.titleLarge)
                    Text(
                        "Connectez-vous pour acceder au rapport du jour, a l'historique et aux statistiques.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }

                FormField(
                    label = "Identifiant ou email",
                    value = loginId,
                    onValueChange = { loginId = it },
                    leadingIcon = Icons.Outlined.Person
                )
                FormField(
                    label = "Mot de passe",
                    value = password,
                    onValueChange = { password = it },
                    leadingIcon = Icons.Outlined.Lock,
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                                contentDescription = null
                            )
                        }
                    }
                )

                error?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                PrimaryButton(
                    label = "Se connecter",
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
                )
                PrimaryButton(
                    label = "Creer un compte agent",
                    onClick = onRegister,
                    variant = cm.crfc.pointage.ui.components.ButtonVariant.GHOST
                )
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
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var jobTitle by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmVisible by remember { mutableStateOf(false) }

    AuthScaffold(
        title = "Nouveau compte",
        subtitle = "Creez un acces agent pour participer au pointage quotidien.",
        kicker = "Inscription",
        topAction = {
            IconButton(onClick = onBack) {
                Icon(Icons.Outlined.ArrowBack, contentDescription = null, tint = TextOnPrimary)
            }
        }
    ) {
        SectionCard(highlighted = true) {
            Column(verticalArrangement = Arrangement.spacedBy(Dimens.SpaceLG)) {
                Row(horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceMD)) {
                    FormField(
                        label = "Prenom",
                        value = firstName,
                        onValueChange = { firstName = it },
                        leadingIcon = Icons.Outlined.Person,
                        modifier = Modifier.weight(1f)
                    )
                    FormField(
                        label = "Nom",
                        value = lastName,
                        onValueChange = { lastName = it },
                        leadingIcon = Icons.Outlined.Person,
                        modifier = Modifier.weight(1f)
                    )
                }
                FormField(
                    label = "Email",
                    value = email,
                    onValueChange = { email = it },
                    leadingIcon = Icons.Outlined.Email
                )
                FormField(
                    label = "Fonction",
                    value = jobTitle,
                    onValueChange = { jobTitle = it },
                    leadingIcon = Icons.Outlined.WorkOutline
                )
                FormField(
                    label = "Mot de passe",
                    value = password,
                    onValueChange = { password = it },
                    leadingIcon = Icons.Outlined.Lock,
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                                contentDescription = null
                            )
                        }
                    }
                )
                FormField(
                    label = "Confirmation",
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    leadingIcon = Icons.Outlined.Lock,
                    visualTransformation = if (confirmVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { confirmVisible = !confirmVisible }) {
                            Icon(
                                imageVector = if (confirmVisible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                                contentDescription = null
                            )
                        }
                    }
                )

                error?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                PrimaryButton(
                    label = "Creer le compte",
                    onClick = {
                        CoroutineScope(Dispatchers.Main).launch {
                            error = null
                            if (password != confirmPassword) {
                                error = "Les mots de passe ne correspondent pas."
                                return@launch
                            }
                            val result = authRepository.register(firstName, lastName, email, jobTitle, password)
                            if (result.success) {
                                onRegistered()
                            } else {
                                error = result.error
                            }
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun AuthScaffold(
    title: String,
    subtitle: String,
    kicker: String,
    topAction: @Composable (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val extras = LocalCrfcUiExtras.current
    val scrollState = rememberScrollState()
    val horizontalPadding = horizontalPadding()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(NavyPrimary, extras.navyLight)))
    ) {
        Image(
            painter = painterResource(id = R.drawable.bg_pattern),
            contentDescription = null,
            modifier = Modifier.matchParentSize(),
            contentScale = ContentScale.Crop,
            alpha = 0.12f
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(scrollState)
                .padding(horizontal = horizontalPadding, vertical = Dimens.SpaceXL),
            verticalArrangement = Arrangement.spacedBy(Dimens.SpaceXL)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(Dimens.SpaceSM)
                ) {
                    Surface(
                        shape = RoundedCornerShape(Dimens.ChipRadius),
                        color = Color.White.copy(alpha = 0.16f)
                    ) {
                        Text(
                            text = kicker,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            color = TextOnPrimary,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceMD),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        EmployeeAvatar(
                            name = "CR FC",
                            size = 56.dp,
                            modifier = Modifier.background(Color.White.copy(alpha = 0.14f), CircleShape)
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(Dimens.SpaceXS)) {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.headlineLarge,
                                color = TextOnPrimary
                            )
                            Text(
                                text = subtitle,
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextOnPrimary.copy(alpha = 0.78f)
                            )
                        }
                    }
                }
                topAction?.invoke()
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
            ) {
                content()
            }
        }
    }
}
