package cm.crfc.pointage.ui.recurring

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
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.EventBusy
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cm.crfc.pointage.data.ReportRepository
import cm.crfc.pointage.model.User
import cm.crfc.pointage.ui.components.BottomSheetHeader
import cm.crfc.pointage.ui.components.EmptyState
import cm.crfc.pointage.ui.components.FormField
import cm.crfc.pointage.ui.components.SectionCard
import cm.crfc.pointage.ui.theme.Dimens
import cm.crfc.pointage.ui.theme.TextSecondary
import cm.crfc.pointage.ui.theme.horizontalPadding
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecurringAbsencesScreen(
    user: User,
    reportRepository: ReportRepository,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val horizontalPadding = horizontalPadding()
    val recurring by reportRepository.observeRecurringAbsences().collectAsStateWithLifecycle(initialValue = emptyList())
    val employees by reportRepository.observeEmployees().collectAsStateWithLifecycle(initialValue = emptyList())
    val reasons by reportRepository.observeAbsenceReasons().collectAsStateWithLifecycle(initialValue = emptyList())

    var showSheet by remember { mutableStateOf(false) }
    var selectedEmployeeId by remember { mutableStateOf("") }
    var selectedReasonId by remember { mutableStateOf("") }
    var comment by remember { mutableStateOf("") }

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
                        Text("CONFIGURATION", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                        Text("Absences Recurrentes", style = MaterialTheme.typography.headlineMedium)
                    }
                }
                SectionCard {
                    Text("Automatisation du pointage", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.padding(top = Dimens.SpaceSM))
                    Text(
                        "Les employes listes ici seront automatiquement marques comme absents lors de la creation de chaque nouveau rapport journalier.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
            }
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = horizontalPadding),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("RECURRENCES ACTIVES", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                Text("${recurring.size} employes", style = MaterialTheme.typography.labelMedium)
            }
        }

        if (recurring.isEmpty()) {
            item {
                SectionCard(modifier = Modifier.padding(horizontal = horizontalPadding)) {
                    EmptyState(Icons.Outlined.EventBusy, "Aucune absence recurrente", "Ajoute une recurrence pour automatiser le pointage.")
                }
            }
        } else {
            items(recurring, key = { it.id }) { item ->
                val employee = employees.firstOrNull { it.id == item.employeeId }
                val reason = reasons.firstOrNull { it.id == item.reasonId }
                SectionCard(modifier = Modifier.padding(horizontal = horizontalPadding)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceMD), verticalAlignment = Alignment.CenterVertically) {
                            Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = MaterialTheme.shapes.small) {
                                Icon(
                                    Icons.Outlined.EventBusy,
                                    contentDescription = null,
                                    modifier = Modifier.padding(10.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(employee?.fullName ?: "Employe inconnu", style = MaterialTheme.typography.titleMedium)
                                Text(
                                    text = buildString {
                                        append(reason?.label ?: "Motif")
                                        item.comment?.takeIf { it.isNotBlank() }?.let {
                                            append(" - ")
                                            append(it)
                                        }
                                    },
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextSecondary
                                )
                            }
                        }
                        IconButton(onClick = { scope.launch { reportRepository.removeRecurringAbsence(item.employeeId) } }) {
                            Icon(Icons.Outlined.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }

        item {
            Surface(
                onClick = {
                    showSheet = true
                    selectedEmployeeId = employees.firstOrNull()?.id.orEmpty()
                    selectedReasonId = reasons.firstOrNull()?.id.orEmpty()
                    comment = ""
                },
                color = MaterialTheme.colorScheme.tertiary,
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
                    Icon(Icons.Outlined.Add, contentDescription = null, tint = MaterialTheme.colorScheme.onTertiary)
                    Text("Ajouter une absence recurrente", color = MaterialTheme.colorScheme.onTertiary, style = MaterialTheme.typography.labelLarge)
                }
            }
        }

        item {
            Text(
                text = "Les modifications n'affectent pas les rapports deja finalises.",
                modifier = Modifier.padding(horizontal = horizontalPadding),
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
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
                BottomSheetHeader(title = "Ajouter une recurrence", onClose = { showSheet = false })
                FormField("Employe", selectedEmployeeId, { selectedEmployeeId = it }, placeholder = "Saisir l'identifiant employe")
                FormField("Motif", selectedReasonId, { selectedReasonId = it }, placeholder = "Saisir l'identifiant motif")
                FormField("Detail", comment, { comment = it }, placeholder = "Ex: Mission - projet terrain", singleLine = false)
                Surface(
                    onClick = {
                        scope.launch {
                            reportRepository.setRecurringAbsence(selectedEmployeeId, selectedReasonId, comment)
                            showSheet = false
                        }
                    },
                    color = MaterialTheme.colorScheme.tertiary,
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text(
                        text = "Enregistrer",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        color = MaterialTheme.colorScheme.onTertiary,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}
