package cm.crfc.pointage.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cm.crfc.pointage.data.ReportRepository
import cm.crfc.pointage.model.Employee
import cm.crfc.pointage.model.User
import cm.crfc.pointage.model.UserRole
import cm.crfc.pointage.ui.components.AppSearchField
import cm.crfc.pointage.ui.components.AvatarCircle
import cm.crfc.pointage.ui.components.CrfcCard
import cm.crfc.pointage.ui.components.EmptyState
import cm.crfc.pointage.ui.components.HeaderCard
import cm.crfc.pointage.ui.components.ScreenList
import cm.crfc.pointage.ui.components.SectionLabel
import cm.crfc.pointage.ui.components.SectionTitle
import cm.crfc.pointage.ui.components.StatusBadge
import cm.crfc.pointage.ui.components.TextFieldBlock
import cm.crfc.pointage.ui.theme.LocalCrfcExtraColors
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
    val extra = LocalCrfcExtraColors.current
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

    ScreenList {
        item {
            HeaderCard(
                title = "Employes",
                subtitle = "${employees.count { it.isActive }} actifs - ${employees.size} au total",
                actions = {
                    if (user.role == UserRole.ADMIN) {
                        IconButton(onClick = {
                            selected = null
                            firstName = ""
                            lastName = ""
                            activeSheet = EmployeeSheet.ADD
                        }) {
                            Icon(Icons.Rounded.Add, contentDescription = null, tint = Color.White)
                        }
                    }
                }
            )
        }

        item {
            CrfcCard(modifier = Modifier.padding(horizontal = 16.dp)) {
                AppSearchField(
                    value = search,
                    onValueChange = { search = it },
                    label = "Rechercher un employe..."
                )
                Row(
                    modifier = Modifier.padding(top = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(selected = !showInactive, onClick = { showInactive = false }, label = { Text("Actifs") })
                    FilterChip(selected = showInactive, onClick = { showInactive = true }, label = { Text("Tous") })
                }
            }
        }

        if (filtered.isEmpty()) {
            item {
                CrfcCard(modifier = Modifier.padding(horizontal = 16.dp)) {
                    EmptyState("Aucun employe", "Aucun employe ne correspond a la recherche courante.")
                }
            }
        } else {
            items(filtered, key = { it.id }) { employee ->
                val recurringLabel = recurring.firstOrNull { it.employeeId == employee.id }?.let { match ->
                    reasons.firstOrNull { it.id == match.reasonId }?.label
                }
                CrfcCard(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        AvatarCircle(text = employee.fullName, color = MaterialTheme.colorScheme.primary)
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(employee.fullName, style = MaterialTheme.typography.titleMedium)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                if (employee.needsReview) {
                                    StatusBadge(text = "A verifier", color = MaterialTheme.colorScheme.error)
                                }
                                if (recurringLabel != null) {
                                    StatusBadge(text = recurringLabel, color = extra.absent)
                                }
                                if (!employee.isActive) {
                                    StatusBadge(text = "Inactif", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                        if (user.role == UserRole.ADMIN) {
                            Row {
                                IconButton(onClick = {
                                    selected = employee
                                    firstName = employee.firstName
                                    lastName = employee.lastName
                                    activeSheet = EmployeeSheet.EDIT
                                }) {
                                    Icon(Icons.Rounded.Edit, contentDescription = null)
                                }
                                IconButton(onClick = {
                                    selected = employee
                                    selectedReasonId = recurring.firstOrNull { it.employeeId == employee.id }?.reasonId
                                        ?: reasons.firstOrNull()?.id.orEmpty()
                                    comment = recurring.firstOrNull { it.employeeId == employee.id }?.comment.orEmpty()
                                    activeSheet = EmployeeSheet.RECURRING
                                }) {
                                    Icon(Icons.Rounded.Repeat, contentDescription = null)
                                }
                                IconButton(onClick = {
                                    CoroutineScope(Dispatchers.Main).launch {
                                        reportRepository.toggleEmployeeActive(employee.id)
                                    }
                                }) {
                                    Icon(
                                        if (employee.isActive) Icons.Rounded.ToggleOn else Icons.Rounded.ToggleOff,
                                        contentDescription = null,
                                        tint = if (employee.isActive) extra.success else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
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
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                when (activeSheet) {
                    EmployeeSheet.ADD -> {
                        Text("Ajouter un employe", style = MaterialTheme.typography.titleLarge)
                        TextFieldBlock(firstName, { firstName = it }, "Prenom(s)")
                        TextFieldBlock(lastName, { lastName = it }, "Nom de famille")
                        Button(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            onClick = {
                                CoroutineScope(Dispatchers.Main).launch {
                                    reportRepository.addEmployee(firstName, lastName)
                                    activeSheet = null
                                }
                            }
                        ) {
                            Text("Ajouter")
                        }
                    }

                    EmployeeSheet.EDIT -> {
                        Text("Modifier l'employe", style = MaterialTheme.typography.titleLarge)
                        selected?.let {
                            Text(it.fullName, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        TextFieldBlock(firstName, { firstName = it }, "Prenom(s)")
                        TextFieldBlock(lastName, { lastName = it }, "Nom de famille")
                        Button(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            onClick = {
                                selected?.let { employee ->
                                    CoroutineScope(Dispatchers.Main).launch {
                                        reportRepository.updateEmployee(
                                            employee.copy(
                                                firstName = firstName.trim(),
                                                lastName = lastName.trim(),
                                                fullName = listOf(lastName.trim(), firstName.trim())
                                                    .filter { it.isNotBlank() }
                                                    .joinToString(" ")
                                                    .trim(),
                                                needsReview = false
                                            )
                                        )
                                        activeSheet = null
                                    }
                                }
                            }
                        ) {
                            Text("Enregistrer")
                        }
                    }

                    EmployeeSheet.RECURRING -> {
                        Text("Absence recurrente", style = MaterialTheme.typography.titleLarge)
                        selected?.let {
                            Text("Configurer l'absence recurrente pour ${it.fullName}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        SectionLabel("Motif")
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            reasons.forEach { reason ->
                                OutlinedButton(
                                    modifier = Modifier.fillMaxWidth(),
                                    onClick = { selectedReasonId = reason.id }
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(reason.label)
                                        if (selectedReasonId == reason.id) {
                                            StatusBadge(text = "Choisi", color = extra.absent)
                                        }
                                    }
                                }
                            }
                        }
                        TextFieldBlock(
                            value = comment,
                            onValueChange = { comment = it },
                            label = "Commentaire",
                            singleLine = false
                        )
                        Button(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            onClick = {
                                selected?.let { employee ->
                                    CoroutineScope(Dispatchers.Main).launch {
                                        reportRepository.setRecurringAbsence(employee.id, selectedReasonId, comment)
                                        activeSheet = null
                                    }
                                }
                            }
                        ) {
                            Text("Enregistrer")
                        }
                        selected?.let { employee ->
                            if (recurring.any { it.employeeId == employee.id }) {
                                OutlinedButton(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(50.dp),
                                    onClick = {
                                        CoroutineScope(Dispatchers.Main).launch {
                                            reportRepository.removeRecurringAbsence(employee.id)
                                            activeSheet = null
                                        }
                                    }
                                ) {
                                    Text("Supprimer la configuration")
                                }
                            }
                        }
                    }

                    null -> Unit
                }
            }
        }
    }
}
