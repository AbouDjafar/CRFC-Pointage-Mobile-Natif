package cm.crfc.pointage.ui.employes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.AdminPanelSettings
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Repeat
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cm.crfc.pointage.data.ReportRepository
import cm.crfc.pointage.model.Employee
import cm.crfc.pointage.model.User
import cm.crfc.pointage.ui.components.BottomSheetHeader
import cm.crfc.pointage.ui.components.EmployeeAvatar
import cm.crfc.pointage.ui.components.FormField
import cm.crfc.pointage.ui.components.HeaderActionPill
import cm.crfc.pointage.ui.components.SectionCard
import cm.crfc.pointage.ui.theme.Dimens
import cm.crfc.pointage.ui.theme.LocalCrfcUiExtras
import cm.crfc.pointage.ui.theme.TextSecondary
import cm.crfc.pointage.ui.theme.horizontalPadding
import kotlinx.coroutines.launch

private enum class EmployeeSheet { ADD, EDIT }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmployeesScreen(
    user: User,
    reportRepository: ReportRepository,
    onOpenUserManagement: () -> Unit,
    onOpenRecurringAbsences: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val extras = LocalCrfcUiExtras.current
    val horizontalPadding = horizontalPadding()
    val employees by reportRepository.observeEmployees().collectAsStateWithLifecycle(initialValue = emptyList())

    var query by remember { mutableStateOf("") }
    var showAll by remember { mutableStateOf(true) }
    var activeSheet by remember { mutableStateOf<EmployeeSheet?>(null) }
    var editingEmployee by remember { mutableStateOf<Employee?>(null) }
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var jobTitle by remember { mutableStateOf("") }
    var department by remember { mutableStateOf("") }

    val filtered = employees
        .filter { showAll || it.isActive }
        .filter {
            query.isBlank() ||
                it.fullName.contains(query, ignoreCase = true) ||
                it.jobTitle.contains(query, ignoreCase = true) ||
                it.department.contains(query, ignoreCase = true)
        }
    val required = filtered.filter { it.needsReview }

    Box(modifier = Modifier.fillMaxSize()) {
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
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text("ANNUAIRE PERSONNEL", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                            Text("Personnel", style = MaterialTheme.typography.headlineMedium)
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceSM)) {
                            HeaderActionPill(text = "Users", onClick = onOpenUserManagement)
                            HeaderActionPill(text = "Recurrence", onClick = onOpenRecurringAbsences)
                        }
                    }

                    FormField(
                        label = "Rechercher un employe",
                        value = query,
                        onValueChange = { query = it },
                        placeholder = "Rechercher un employe...",
                        leadingIcon = Icons.Outlined.Search
                    )

                    SectionCard {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            EmployeeMetric("Total", employees.size.toString(), modifier = Modifier.weight(1f))
                            EmployeeMetric("Actifs", employees.count { it.isActive }.toString(), modifier = Modifier.weight(1f))
                            EmployeeMetric("Alertes", employees.count { it.needsReview }.toString(), modifier = Modifier.weight(1f))
                        }
                    }
                }
            }

            if (required.isNotEmpty()) {
                item {
                    Column(
                        modifier = Modifier.padding(horizontal = horizontalPadding),
                        verticalArrangement = Arrangement.spacedBy(Dimens.SpaceMD)
                    ) {
                        Text("ACTIONS REQUISES", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                        required.forEach { employee ->
                            EmployeeRow(
                                employee = employee,
                                accentLabel = "A VERIFIER",
                                onToggle = { scope.launch { reportRepository.toggleEmployeeActive(employee.id) } },
                                onEdit = {
                                    editingEmployee = employee
                                    firstName = employee.firstName
                                    lastName = employee.lastName
                                    jobTitle = employee.jobTitle
                                    department = employee.department
                                    activeSheet = EmployeeSheet.EDIT
                                }
                            )
                        }
                    }
                }
            }

            item {
                Column(
                    modifier = Modifier.padding(horizontal = horizontalPadding),
                    verticalArrangement = Arrangement.spacedBy(Dimens.SpaceMD)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("TOUS LES EMPLOYES", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                        Row(horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceSM), verticalAlignment = Alignment.CenterVertically) {
                            Text(if (showAll) "Tous" else "Actifs", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            Switch(checked = showAll, onCheckedChange = { showAll = it })
                        }
                    }
                }
            }

            items(filtered.filterNot { it.needsReview }, key = { it.id }) { employee ->
                Box(modifier = Modifier.padding(horizontal = horizontalPadding)) {
                    EmployeeRow(
                        employee = employee,
                        accentLabel = null,
                        onToggle = { scope.launch { reportRepository.toggleEmployeeActive(employee.id) } },
                        onEdit = {
                            editingEmployee = employee
                            firstName = employee.firstName
                            lastName = employee.lastName
                            jobTitle = employee.jobTitle
                            department = employee.department
                            activeSheet = EmployeeSheet.EDIT
                        }
                    )
                }
            }

            item { Spacer(modifier = Modifier.navigationBarsPadding()) }
        }

        Surface(
            onClick = {
                editingEmployee = null
                firstName = ""
                lastName = ""
                jobTitle = ""
                department = ""
                activeSheet = EmployeeSheet.ADD
            },
            color = extras.orangeAccent,
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .navigationBarsPadding()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Outlined.Add, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary)
                Text("Nouvel Employe", color = MaterialTheme.colorScheme.onPrimary, style = MaterialTheme.typography.labelLarge)
            }
        }
    }

    if (activeSheet != null) {
        ModalBottomSheet(onDismissRequest = { activeSheet = null }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = horizontalPadding, vertical = Dimens.SpaceLG),
                verticalArrangement = Arrangement.spacedBy(Dimens.SpaceLG)
            ) {
                BottomSheetHeader(
                    title = if (activeSheet == EmployeeSheet.ADD) "Ajouter un employe" else "Modifier l'employe",
                    onClose = { activeSheet = null }
                )
                FormField("Prenom", firstName, { firstName = it })
                FormField("Nom", lastName, { lastName = it })
                FormField("Fonction / Poste", jobTitle, { jobTitle = it })
                FormField("Service / Departement", department, { department = it })
                Surface(
                    onClick = {
                        scope.launch {
                            val target = editingEmployee
                            if (target == null) {
                                reportRepository.addEmployee(firstName, lastName, jobTitle, department)
                            } else {
                                reportRepository.updateEmployee(
                                    target.copy(
                                        firstName = firstName.trim(),
                                        lastName = lastName.trim(),
                                        fullName = listOf(firstName.trim(), lastName.trim()).filter { it.isNotBlank() }.joinToString(" ").trim(),
                                        jobTitle = jobTitle.trim(),
                                        department = department.trim(),
                                        needsReview = false
                                    )
                                )
                            }
                            activeSheet = null
                        }
                    },
                    color = extras.orangeAccent,
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text(
                        text = if (activeSheet == EmployeeSheet.ADD) "Ajouter l'employe" else "Enregistrer",
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
private fun EmployeeMetric(title: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(value, style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
        Text(title, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
    }
}

@Composable
private fun EmployeeRow(
    employee: Employee,
    accentLabel: String?,
    onToggle: () -> Unit,
    onEdit: () -> Unit
) {
    val extras = LocalCrfcUiExtras.current
    SectionCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceMD),
            verticalAlignment = Alignment.CenterVertically
        ) {
            EmployeeAvatar(employee.fullName)
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceSM), verticalAlignment = Alignment.CenterVertically) {
                    Text(employee.fullName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    accentLabel?.let {
                        Surface(color = extras.orangeLight, shape = MaterialTheme.shapes.small) {
                            Text(
                                text = it,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = extras.orangeAccent
                            )
                        }
                    }
                }
                Text(employee.jobTitle, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                Text(employee.department, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            }
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Switch(checked = employee.isActive, onCheckedChange = { onToggle() })
                Surface(onClick = onEdit, color = MaterialTheme.colorScheme.surfaceVariant, shape = MaterialTheme.shapes.small) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Outlined.Info, contentDescription = null, modifier = Modifier.size(14.dp))
                        Text("Editer", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}
