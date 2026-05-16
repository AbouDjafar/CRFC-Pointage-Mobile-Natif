package cm.crfc.pointage.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.ToggleOff
import androidx.compose.material.icons.rounded.ToggleOn
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
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
import cm.crfc.pointage.data.ReportRepository
import cm.crfc.pointage.model.Employee
import cm.crfc.pointage.model.User
import cm.crfc.pointage.model.UserRole
import cm.crfc.pointage.ui.components.CrfcCard
import cm.crfc.pointage.ui.components.EmptyState
import cm.crfc.pointage.ui.components.HeaderCard
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private enum class EmployeeSheet { ADD, EDIT, RECURRING }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmployeesScreen(
    user: User,
    reportRepository: ReportRepository
) {
    val employees by reportRepository.observeEmployees().collectAsStateWithLifecycle(initialValue = emptyList())
    val reasons by reportRepository.observeAbsenceReasons().collectAsStateWithLifecycle(initialValue = emptyList())
    val recurring by reportRepository.observeRecurringAbsences().collectAsStateWithLifecycle(initialValue = emptyList())

    var search by remember { mutableStateOf("") }
    var showInactive by remember { mutableStateOf(false) }
    var activeSheet by remember { mutableStateOf<EmployeeSheet?>(null) }
    var selected by remember { mutableStateOf<Employee?>(null) }
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var selectedReasonId by remember { mutableStateOf("") }
    var comment by remember { mutableStateOf("") }

    val filtered = employees
        .filter { showInactive || it.isActive }
        .filter { search.isBlank() || it.fullName.contains(search, ignoreCase = true) }

    LazyColumn(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item {
            HeaderCard(
                title = "Employes",
                subtitle = "${employees.count { it.isActive }} actifs • ${employees.size} au total",
                actions = {
                    if (user.role == UserRole.ADMIN) {
                        IconButton(onClick = {
                            firstName = ""
                            lastName = ""
                            activeSheet = EmployeeSheet.ADD
                        }) {
                            Icon(Icons.Rounded.Add, contentDescription = null, tint = androidx.compose.ui.graphics.Color.White)
                        }
                    }
                }
            )
        }

        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = search,
                    onValueChange = { search = it },
                    label = { Text("Rechercher") }
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = !showInactive, onClick = { showInactive = false }, label = { Text("Actifs") })
                    FilterChip(selected = showInactive, onClick = { showInactive = true }, label = { Text("Tous") })
                }
            }
        }

        if (filtered.isEmpty()) {
            item {
                CrfcCard(modifier = Modifier.padding(horizontal = 16.dp)) {
                    EmptyState("Aucun employe", "Aucun employe ne correspond a la recherche.")
                }
            }
        } else {
            items(filtered, key = { it.id }) { employee ->
                val recurringLabel = recurring.firstOrNull { it.employeeId == employee.id }?.let { match ->
                    reasons.firstOrNull { it.id == match.reasonId }?.label
                }
                CrfcCard(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(employee.fullName, style = MaterialTheme.typography.titleMedium)
                            if (employee.needsReview) {
                                Text("A verifier", color = MaterialTheme.colorScheme.error)
                            }
                            if (recurringLabel != null) {
                                Text("Absence recurrente : $recurringLabel", color = MaterialTheme.colorScheme.primary)
                            }
                            if (!employee.isActive) {
                                Text("Inactif", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        if (user.role == UserRole.ADMIN) {
                            Row {
                                IconButton(onClick = {
                                    selected = employee
                                    firstName = employee.firstName
                                    lastName = employee.lastName
                                    activeSheet = EmployeeSheet.EDIT
                                }) { Icon(Icons.Rounded.Edit, contentDescription = null) }
                                IconButton(onClick = {
                                    selected = employee
                                    selectedReasonId = recurring.firstOrNull { it.employeeId == employee.id }?.reasonId ?: reasons.firstOrNull()?.id.orEmpty()
                                    comment = recurring.firstOrNull { it.employeeId == employee.id }?.comment.orEmpty()
                                    activeSheet = EmployeeSheet.RECURRING
                                }) { Icon(Icons.Rounded.Repeat, contentDescription = null) }
                                IconButton(onClick = {
                                    CoroutineScope(Dispatchers.Main).launch {
                                        reportRepository.toggleEmployeeActive(employee.id)
                                    }
                                }) {
                                    Icon(if (employee.isActive) Icons.Rounded.ToggleOn else Icons.Rounded.ToggleOff, contentDescription = null)
                                }
                            }
                        }
                    }
                }
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
                    EmployeeSheet.ADD -> {
                        Text("Ajouter un employe", style = MaterialTheme.typography.titleLarge)
                        OutlinedTextField(value = firstName, onValueChange = { firstName = it }, label = { Text("Prenom(s)") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = lastName, onValueChange = { lastName = it }, label = { Text("Nom de famille") }, modifier = Modifier.fillMaxWidth())
                        Button(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = {
                                CoroutineScope(Dispatchers.Main).launch {
                                    reportRepository.addEmployee(firstName, lastName)
                                    activeSheet = null
                                }
                            }
                        ) { Text("Ajouter") }
                    }
                    EmployeeSheet.EDIT -> {
                        Text("Modifier ${selected?.fullName.orEmpty()}", style = MaterialTheme.typography.titleLarge)
                        OutlinedTextField(value = firstName, onValueChange = { firstName = it }, label = { Text("Prenom(s)") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = lastName, onValueChange = { lastName = it }, label = { Text("Nom de famille") }, modifier = Modifier.fillMaxWidth())
                        Button(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = {
                                selected?.let { employee ->
                                    CoroutineScope(Dispatchers.Main).launch {
                                        reportRepository.updateEmployee(
                                            employee.copy(
                                                firstName = firstName.trim(),
                                                lastName = lastName.trim(),
                                                fullName = listOf(lastName.trim(), firstName.trim()).filter { it.isNotBlank() }.joinToString(" ").trim(),
                                                needsReview = false
                                            )
                                        )
                                        activeSheet = null
                                    }
                                }
                            }
                        ) { Text("Enregistrer") }
                    }
                    EmployeeSheet.RECURRING -> {
                        Text("Absence recurrente", style = MaterialTheme.typography.titleLarge)
                        reasons.forEach { reason ->
                            OutlinedButton(modifier = Modifier.fillMaxWidth(), onClick = { selectedReasonId = reason.id }) {
                                Text(reason.label)
                            }
                        }
                        OutlinedTextField(value = comment, onValueChange = { comment = it }, label = { Text("Commentaire") }, modifier = Modifier.fillMaxWidth())
                        Button(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = {
                                selected?.let { employee ->
                                    CoroutineScope(Dispatchers.Main).launch {
                                        reportRepository.setRecurringAbsence(employee.id, selectedReasonId, comment)
                                        activeSheet = null
                                    }
                                }
                            }
                        ) { Text("Enregistrer") }
                        selected?.let { employee ->
                            if (recurring.any { it.employeeId == employee.id }) {
                                OutlinedButton(
                                    modifier = Modifier.fillMaxWidth(),
                                    onClick = {
                                        CoroutineScope(Dispatchers.Main).launch {
                                            reportRepository.removeRecurringAbsence(employee.id)
                                            activeSheet = null
                                        }
                                    }
                                ) { Text("Supprimer l'absence recurrente") }
                            }
                        }
                    }
                    null -> Unit
                }
            }
        }
    }
}
