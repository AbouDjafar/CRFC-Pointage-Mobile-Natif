package cm.crfc.pointage.ui.employes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.PersonOff
import androidx.compose.material.icons.outlined.Repeat
import androidx.compose.material.icons.outlined.ToggleOff
import androidx.compose.material.icons.outlined.ToggleOn
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import cm.crfc.pointage.data.ReportRepository
import cm.crfc.pointage.model.Employee
import cm.crfc.pointage.model.User
import cm.crfc.pointage.model.UserRole
import cm.crfc.pointage.ui.components.AppHeader
import cm.crfc.pointage.ui.components.BottomSheetHeader
import cm.crfc.pointage.ui.components.ButtonVariant
import cm.crfc.pointage.ui.components.EmployeeAvatar
import cm.crfc.pointage.ui.components.EmptyState
import cm.crfc.pointage.ui.components.FilterChipRow
import cm.crfc.pointage.ui.components.FormField
import cm.crfc.pointage.ui.components.HeaderActionPill
import cm.crfc.pointage.ui.components.PrimaryButton
import cm.crfc.pointage.ui.components.SectionCard
import cm.crfc.pointage.ui.components.StatChip
import cm.crfc.pointage.ui.theme.Dimens
import cm.crfc.pointage.ui.theme.LocalCrfcUiExtras
import cm.crfc.pointage.ui.theme.TextSecondary
import cm.crfc.pointage.ui.theme.horizontalPadding
import kotlinx.coroutines.launch

private enum class EmployeeSheet { ACTIONS, ADD, EDIT, RECURRING }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmployeesScreen(
    user: User,
    reportRepository: ReportRepository
) {
    val scope = rememberCoroutineScope()
    val extras = LocalCrfcUiExtras.current
    val horizontalPadding = horizontalPadding()
    val employees by reportRepository.observeEmployees().collectAsStateWithLifecycle(initialValue = emptyList())
    val reasons by reportRepository.observeAbsenceReasons().collectAsStateWithLifecycle(initialValue = emptyList())
    val recurring by reportRepository.observeRecurringAbsences().collectAsStateWithLifecycle(initialValue = emptyList())

    var search by remember { mutableStateOf("") }
    var tab by remember { mutableStateOf("Actifs") }
    var activeSheet by remember { mutableStateOf<EmployeeSheet?>(null) }
    var selectedEmployee by remember { mutableStateOf<Employee?>(null) }
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var selectedReasonId by remember { mutableStateOf("") }
    var comment by remember { mutableStateOf("") }

    val visibleEmployees = employees
        .filter { tab == "Tous" || it.isActive }
        .filter { search.isBlank() || it.fullName.contains(search, ignoreCase = true) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(Dimens.SpaceLG)
    ) {
        item {
            AppHeader(
                title = "Employes",
                subtitle = "${employees.count { it.isActive }} actifs • ${employees.size} au total",
                actions = {
                    if (user.role == UserRole.ADMIN) {
                        HeaderActionPill(
                            text = "Ajouter",
                            onClick = {
                                firstName = ""
                                lastName = ""
                                activeSheet = EmployeeSheet.ADD
                            }
                        )
                    }
                },
                bottomContent = {
                    FormField(
                        label = "Rechercher un employe",
                        value = search,
                        onValueChange = { search = it },
                        placeholder = "Nom de l'employe"
                    )
                }
            )
        }
        if (employees.any { it.needsReview }) {
            item {
                SectionCard(modifier = Modifier.padding(horizontal = horizontalPadding), highlighted = true) {
                    StatChip(
                        icon = Icons.Outlined.WarningAmber,
                        text = "${employees.count { it.needsReview }} employe(s) a verifier",
                        containerColor = extras.orangeLight,
                        contentColor = extras.orangeAccent
                    )
                    Spacer(modifier = Modifier.padding(top = Dimens.SpaceSM))
                    Text(
                        text = "Ces employes meritent une verification manuelle apres import ou modification.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
            }
        }
        item {
            SectionCard(modifier = Modifier.padding(horizontal = horizontalPadding)) {
                FilterChipRow(
                    options = listOf("Actifs", "Tous"),
                    selectedOption = tab,
                    onOptionSelected = { tab = it },
                    activeColor = extras.greenAccent
                )
            }
        }
        if (visibleEmployees.isEmpty()) {
            item {
                SectionCard(modifier = Modifier.padding(horizontal = horizontalPadding)) {
                    EmptyState(Icons.Outlined.Groups, "Aucun employe", "Aucun employe ne correspond au filtre courant.")
                }
            }
        } else {
            items(visibleEmployees, key = { it.id }) { employee ->
                val recurringReason = recurring.firstOrNull { it.employeeId == employee.id }?.let { item ->
                    reasons.firstOrNull { it.id == item.reasonId }?.label
                }
                SectionCard(modifier = Modifier.padding(horizontal = horizontalPadding), highlighted = true) {
                    Row(horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceMD)) {
                        EmployeeAvatar(name = employee.fullName, size = Dimens.AvatarSizeLg)
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(Dimens.SpaceSM)) {
                            Text(employee.fullName, style = MaterialTheme.typography.titleLarge)
                            Row(horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceSM)) {
                                if (employee.needsReview) {
                                    StatChip(Icons.Outlined.WarningAmber, "A verifier", extras.orangeLight, extras.orangeAccent, compact = true)
                                }
                                if (!employee.isActive) {
                                    StatChip(Icons.Outlined.PersonOff, "Inactif", MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.onSurfaceVariant, compact = true)
                                }
                                recurringReason?.let {
                                    StatChip(Icons.Outlined.Repeat, it, extras.purpleLight, extras.purpleAccent, compact = true)
                                }
                            }
                        }
                    }
                    if (user.role == UserRole.ADMIN) {
                        Spacer(modifier = Modifier.padding(top = Dimens.SpaceLG))
                        PrimaryButton(
                            label = "Actions",
                            onClick = {
                                selectedEmployee = employee
                                activeSheet = EmployeeSheet.ACTIONS
                            },
                            variant = ButtonVariant.GHOST
                        )
                    }
                }
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
                    EmployeeSheet.ACTIONS -> {
                        selectedEmployee?.let { employee ->
                            BottomSheetHeader(title = employee.fullName, onClose = { activeSheet = null })
                            PrimaryButton(
                                label = "Modifier l'employe",
                                onClick = {
                                    firstName = employee.firstName
                                    lastName = employee.lastName
                                    activeSheet = EmployeeSheet.EDIT
                                },
                                variant = ButtonVariant.NAVY
                            )
                            PrimaryButton(
                                label = "Configurer l'absence recurrente",
                                onClick = {
                                    selectedReasonId = recurring.firstOrNull { it.employeeId == employee.id }?.reasonId ?: reasons.firstOrNull()?.id.orEmpty()
                                    comment = recurring.firstOrNull { it.employeeId == employee.id }?.comment.orEmpty()
                                    activeSheet = EmployeeSheet.RECURRING
                                },
                                variant = ButtonVariant.GHOST
                            )
                            PrimaryButton(
                                label = if (employee.isActive) "Desactiver l'employe" else "Reactiver l'employe",
                                onClick = {
                                    scope.launch {
                                        reportRepository.toggleEmployeeActive(employee.id)
                                        activeSheet = null
                                    }
                                },
                                variant = ButtonVariant.GHOST
                            )
                        }
                    }
                    EmployeeSheet.ADD -> {
                        BottomSheetHeader(title = "Ajouter un employe", onClose = { activeSheet = null })
                        FormField("Prenom", firstName, { firstName = it })
                        FormField("Nom", lastName, { lastName = it })
                        PrimaryButton(
                            label = "Ajouter",
                            onClick = {
                                scope.launch {
                                    reportRepository.addEmployee(firstName, lastName)
                                    activeSheet = null
                                }
                            }
                        )
                    }
                    EmployeeSheet.EDIT -> {
                        selectedEmployee?.let { employee ->
                            BottomSheetHeader(title = "Modifier l'employe", onClose = { activeSheet = null })
                            FormField("Prenom", firstName, { firstName = it })
                            FormField("Nom", lastName, { lastName = it })
                            PrimaryButton(
                                label = "Enregistrer",
                                onClick = {
                                    scope.launch {
                                        reportRepository.updateEmployee(
                                            employee.copy(
                                                firstName = firstName.trim(),
                                                lastName = lastName.trim(),
                                                fullName = listOf(lastName.trim(), firstName.trim()).filter { it.isNotBlank() }.joinToString(" "),
                                                needsReview = false
                                            )
                                        )
                                        activeSheet = null
                                    }
                                }
                            )
                        }
                    }
                    EmployeeSheet.RECURRING -> {
                        selectedEmployee?.let { employee ->
                            BottomSheetHeader(title = "Absence recurrente", onClose = { activeSheet = null })
                            Text(employee.fullName, style = MaterialTheme.typography.titleMedium)
                            Column(verticalArrangement = Arrangement.spacedBy(Dimens.SpaceSM)) {
                                reasons.forEach { reason ->
                                    PrimaryButton(
                                        label = if (selectedReasonId == reason.id) "${reason.label} selectionne" else reason.label,
                                        onClick = { selectedReasonId = reason.id },
                                        variant = if (selectedReasonId == reason.id) ButtonVariant.NAVY else ButtonVariant.GHOST
                                    )
                                }
                            }
                            FormField("Commentaire", comment, { comment = it }, placeholder = "Optionnel", singleLine = false)
                            PrimaryButton(
                                label = "Enregistrer",
                                onClick = {
                                    scope.launch {
                                        reportRepository.setRecurringAbsence(employee.id, selectedReasonId, comment)
                                        activeSheet = null
                                    }
                                }
                            )
                            if (recurring.any { it.employeeId == employee.id }) {
                                PrimaryButton(
                                    label = "Supprimer la configuration",
                                    onClick = {
                                        scope.launch {
                                            reportRepository.removeRecurringAbsence(employee.id)
                                            activeSheet = null
                                        }
                                    },
                                    variant = ButtonVariant.GHOST
                                )
                            }
                        }
                    }
                    null -> Unit
                }
            }
        }
    }
}
