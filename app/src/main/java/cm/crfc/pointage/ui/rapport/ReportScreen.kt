package cm.crfc.pointage.ui.rapport

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ChevronLeft
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.PersonOff
import androidx.compose.material.icons.outlined.Schedule
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cm.crfc.pointage.data.ReportRepository
import cm.crfc.pointage.model.AbsenceReason
import cm.crfc.pointage.model.DailyReport
import cm.crfc.pointage.model.Employee
import cm.crfc.pointage.model.ReportStatus
import cm.crfc.pointage.model.User
import cm.crfc.pointage.model.UserRole
import cm.crfc.pointage.ui.components.BottomSheetHeader
import cm.crfc.pointage.ui.components.ButtonVariant
import cm.crfc.pointage.ui.components.ConfirmDialog
import cm.crfc.pointage.ui.components.EmptyState
import cm.crfc.pointage.ui.components.EmployeeAvatar
import cm.crfc.pointage.ui.components.FormField
import cm.crfc.pointage.ui.components.PrimaryButton
import cm.crfc.pointage.ui.components.SectionCard
import cm.crfc.pointage.ui.components.StatChip
import cm.crfc.pointage.ui.theme.Dimens
import cm.crfc.pointage.ui.theme.LocalCrfcUiExtras
import cm.crfc.pointage.ui.theme.TextSecondary
import cm.crfc.pointage.ui.theme.horizontalPadding
import cm.crfc.pointage.util.addDays
import cm.crfc.pointage.util.calcMinutesLate
import cm.crfc.pointage.util.formatDisplayDate
import kotlinx.coroutines.launch

private enum class ReportSheet { LATE_PICK, LATE_DETAILS, ABSENCE_PICK, ABSENCE_DETAILS }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyReportScreen(
    user: User,
    selectedDate: String,
    reportRepository: ReportRepository,
    onBack: () -> Unit,
    onNavigateDate: (String) -> Unit
) {
    val scope = rememberCoroutineScope()
    val extras = LocalCrfcUiExtras.current
    val horizontalPadding = horizontalPadding()

    val report by reportRepository.observeReportByDate(selectedDate).collectAsStateWithLifecycle(initialValue = null)
    val employees by reportRepository.observeEmployees().collectAsStateWithLifecycle(initialValue = emptyList())
    val reasons by reportRepository.observeAbsenceReasons().collectAsStateWithLifecycle(initialValue = emptyList())
    val recurring by reportRepository.observeRecurringAbsences().collectAsStateWithLifecycle(initialValue = emptyList())

    var activeSheet by remember { mutableStateOf<ReportSheet?>(null) }
    var selectedEmployeeId by remember { mutableStateOf("") }
    var selectedReasonId by remember { mutableStateOf("") }
    var arrivalTime by remember { mutableStateOf("08:30") }
    var note by remember { mutableStateOf("") }
    var comment by remember { mutableStateOf("") }
    var query by remember { mutableStateOf("") }
    var confirmFinalize by remember { mutableStateOf(false) }

    val draft = report
    val editable = draft?.status == ReportStatus.DRAFT
    val availableEmployees = employees.filter { employee ->
        employee.isActive &&
            draft?.lateEntries?.none { it.employeeId == employee.id } != false &&
            draft?.absenceEntries?.none { it.employeeId == employee.id } != false
    }

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
                    IconButton(onClick = onBack) {
                        Icon(Icons.Outlined.ArrowBack, contentDescription = null)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("RAPPORT JOURNALIER", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                        Text(formatDisplayDate(selectedDate), style = MaterialTheme.typography.headlineMedium)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { onNavigateDate(addDays(selectedDate, -1)) }) {
                            Icon(Icons.Outlined.ChevronLeft, contentDescription = null)
                        }
                        IconButton(onClick = { onNavigateDate(addDays(selectedDate, 1)) }) {
                            Icon(Icons.Outlined.ChevronRight, contentDescription = null)
                        }
                    }
                }

                StatusCard(
                    status = draft?.status ?: ReportStatus.DRAFT,
                    hasReport = draft != null
                )

                if (draft == null) {
                    SectionCard {
                        EmptyState(
                            icon = Icons.Outlined.Schedule,
                            title = "Aucun rapport pour cette date",
                            subtitle = "Creer le rapport global de la journee pour commencer la saisie.",
                            actionLabel = "Creer le rapport",
                            onAction = {
                                scope.launch { reportRepository.createOrUpdateTodayReport(user, selectedDate) }
                            }
                        )
                    }
                } else {
                    VisitorCounterCard(
                        count = draft.visitorCount,
                        enabled = editable,
                        onMinus = { scope.launch { reportRepository.setVisitorCount(draft, draft.visitorCount - 1) } },
                        onPlus = { scope.launch { reportRepository.setVisitorCount(draft, draft.visitorCount + 1) } }
                    )

                    ReportSectionCard(
                        title = "RETARDS (08:15)",
                        entries = draft.lateEntries.map { entry ->
                            val employee = employees.firstOrNull { it.id == entry.employeeId }
                            ReportRowData(
                                id = entry.id,
                                title = employee?.fullName ?: "Employe inconnu",
                                subtitle = "Arrivee a ${entry.arrivalTime} (+${entry.minutesLate} min)"
                            )
                        },
                        addEnabled = editable,
                        onAdd = {
                            selectedEmployeeId = availableEmployees.firstOrNull()?.id.orEmpty()
                            query = ""
                            activeSheet = ReportSheet.LATE_PICK
                        },
                        onRemove = { id -> scope.launch { reportRepository.removeLateEntry(draft, id) } }
                    )

                    ReportSectionCard(
                        title = "ABSENCES",
                        entries = draft.absenceEntries.map { entry ->
                            val employee = employees.firstOrNull { it.id == entry.employeeId }
                            val label = reasons.firstOrNull { it.id == entry.reasonId }?.label ?: "Motif"
                            val recurringText = if (recurring.any { it.employeeId == entry.employeeId && it.reasonId == entry.reasonId }) " (Recurrent)" else ""
                            ReportRowData(
                                id = entry.id,
                                title = employee?.fullName ?: "Employe inconnu",
                                subtitle = label + recurringText
                            )
                        },
                        addEnabled = editable,
                        onAdd = {
                            selectedEmployeeId = availableEmployees.firstOrNull()?.id.orEmpty()
                            selectedReasonId = reasons.firstOrNull()?.id.orEmpty()
                            query = ""
                            activeSheet = ReportSheet.ABSENCE_PICK
                        },
                        onRemove = { id -> scope.launch { reportRepository.removeAbsenceEntry(draft, id) } }
                    )

                    if (draft.status == ReportStatus.DRAFT) {
                        PrimaryButton(
                            label = "Finaliser le rapport",
                            onClick = { confirmFinalize = true },
                            modifier = Modifier.padding(horizontal = horizontalPadding),
                            variant = ButtonVariant.ORANGE
                        )
                        Text(
                            text = "La finalisation verrouille les donnees et prepare l'export PDF.",
                            modifier = Modifier.padding(horizontal = horizontalPadding),
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    } else {
                        SectionCard(modifier = Modifier.padding(horizontal = horizontalPadding)) {
                            Text("Ce rapport est finalise.", style = MaterialTheme.typography.titleMedium)
                            if (user.role == UserRole.ADMIN) {
                                Spacer(modifier = Modifier.size(Dimens.SpaceLG))
                                PrimaryButton(
                                    label = "Reouvrir le rapport",
                                    onClick = { scope.launch { reportRepository.reopenReport(draft) } },
                                    variant = ButtonVariant.GHOST
                                )
                            }
                        }
                    }
                }
            }
        }
        item { Spacer(modifier = Modifier.navigationBarsPadding()) }
    }

    if (confirmFinalize && draft != null) {
        ConfirmDialog(
            title = "Finaliser le rapport ?",
            message = "Le rapport sera verrouille pour tous les agents.",
            confirmLabel = "Finaliser",
            onConfirm = {
                confirmFinalize = false
                scope.launch { reportRepository.finalizeReport(draft) }
            },
            onDismiss = { confirmFinalize = false }
        )
    }

    if (activeSheet != null && draft != null) {
        ModalBottomSheet(onDismissRequest = { activeSheet = null }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = horizontalPadding, vertical = Dimens.SpaceLG),
                verticalArrangement = Arrangement.spacedBy(Dimens.SpaceLG)
            ) {
                when (activeSheet) {
                    ReportSheet.LATE_PICK -> {
                        BottomSheetHeader(title = "Choisir un employe", onClose = { activeSheet = null })
                        FormField("Recherche", query, { query = it }, placeholder = "Rechercher un employe")
                        EmployeePickList(
                            employees = availableEmployees,
                            query = query,
                            selectedEmployeeId = selectedEmployeeId,
                            onSelect = { selectedEmployeeId = it }
                        )
                        PrimaryButton(
                            label = "Continuer",
                            onClick = { activeSheet = ReportSheet.LATE_DETAILS },
                            enabled = selectedEmployeeId.isNotBlank()
                        )
                    }
                    ReportSheet.LATE_DETAILS -> {
                        BottomSheetHeader(title = "Heure d'arrivee", onClose = { activeSheet = null })
                        FormField("Heure d'arrivee", arrivalTime, { arrivalTime = it }, placeholder = "08:30")
                        FormField("Note", note, { note = it }, placeholder = "Optionnel", singleLine = false)
                        Text("Retard calcule : ${calcMinutesLate(arrivalTime)} min", style = MaterialTheme.typography.titleMedium)
                        PrimaryButton(
                            label = "Confirmer le retard",
                            onClick = {
                                scope.launch {
                                    reportRepository.addLateEntry(draft, selectedEmployeeId, arrivalTime, note)
                                    note = ""
                                    activeSheet = null
                                }
                            }
                        )
                    }
                    ReportSheet.ABSENCE_PICK -> {
                        BottomSheetHeader(title = "Choisir un employe", onClose = { activeSheet = null })
                        FormField("Recherche", query, { query = it }, placeholder = "Rechercher un employe")
                        EmployeePickList(
                            employees = availableEmployees,
                            query = query,
                            selectedEmployeeId = selectedEmployeeId,
                            onSelect = { selectedEmployeeId = it }
                        )
                        PrimaryButton(
                            label = "Continuer",
                            onClick = { activeSheet = ReportSheet.ABSENCE_DETAILS },
                            enabled = selectedEmployeeId.isNotBlank()
                        )
                    }
                    ReportSheet.ABSENCE_DETAILS -> {
                        BottomSheetHeader(title = "Motif d'absence", onClose = { activeSheet = null })
                        ReasonPickList(reasons = reasons, selectedReasonId = selectedReasonId, onSelect = { selectedReasonId = it })
                        FormField("Commentaire", comment, { comment = it }, placeholder = "Optionnel", singleLine = false)
                        PrimaryButton(
                            label = "Confirmer l'absence",
                            onClick = {
                                scope.launch {
                                    reportRepository.addAbsenceEntry(draft, selectedEmployeeId, selectedReasonId, comment)
                                    comment = ""
                                    activeSheet = null
                                }
                            },
                            enabled = selectedReasonId.isNotBlank()
                        )
                    }
                    null -> Unit
                }
            }
        }
    }
}

private data class ReportRowData(
    val id: String,
    val title: String,
    val subtitle: String
)

@Composable
private fun StatusCard(status: ReportStatus, hasReport: Boolean) {
    SectionCard {
        Row(horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceMD), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Text(
                text = if (!hasReport) "Statut : A creer" else "Statut : ${if (status == ReportStatus.DRAFT) "Brouillon" else "Finalise"}",
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

@Composable
private fun VisitorCounterCard(
    count: Int,
    enabled: Boolean,
    onMinus: () -> Unit,
    onPlus: () -> Unit
) {
    SectionCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Visiteurs", style = MaterialTheme.typography.titleLarge)
                Text("Compte total de la journee", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceMD), verticalAlignment = Alignment.CenterVertically) {
                CounterBox("-", enabled, onMinus)
                Text(count.toString(), style = MaterialTheme.typography.headlineMedium)
                CounterBox("+", enabled, onPlus)
            }
        }
    }
}

@Composable
private fun CounterBox(label: String, enabled: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = MaterialTheme.shapes.small
    ) {
        Box(modifier = Modifier.size(34.dp), contentAlignment = Alignment.Center) {
            Text(label, style = MaterialTheme.typography.titleLarge)
        }
    }
}

@Composable
private fun ReportSectionCard(
    title: String,
    entries: List<ReportRowData>,
    addEnabled: Boolean,
    onAdd: () -> Unit,
    onRemove: (String) -> Unit
) {
    val icon = if (title.startsWith("RETARDS")) Icons.Outlined.Schedule else Icons.Outlined.PersonOff
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Dimens.SpaceMD)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = horizontalPadding()),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = TextSecondary)
            if (addEnabled) {
                Surface(onClick = onAdd, color = Color.Transparent) {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Add, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary)
                        Text("Ajouter", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.tertiary)
                    }
                }
            }
        }
        if (entries.isEmpty()) {
            SectionCard(modifier = Modifier.padding(horizontal = horizontalPadding())) {
                EmptyState(icon, "Aucune entree", "Les enregistrements apparaitront ici.")
            }
        } else {
            entries.forEach { entry ->
                SectionCard(modifier = Modifier.padding(horizontal = horizontalPadding())) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(entry.title, style = MaterialTheme.typography.titleMedium)
                            Text(entry.subtitle, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                        }
                        if (addEnabled) {
                            Surface(onClick = { onRemove(entry.id) }, color = Color.Transparent) {
                                Icon(Icons.Outlined.Close, contentDescription = null, tint = Color(0xFFD94D4D))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmployeePickList(
    employees: List<Employee>,
    query: String,
    selectedEmployeeId: String,
    onSelect: (String) -> Unit
) {
    val filtered = employees.filter { query.isBlank() || it.fullName.contains(query, ignoreCase = true) }
    if (filtered.isEmpty()) {
        EmptyState(Icons.Outlined.Groups, "Aucun employe", "Aucun employe disponible pour cette saisie.")
        return
    }
    Column(verticalArrangement = Arrangement.spacedBy(Dimens.SpaceSM)) {
        filtered.forEach { employee ->
            val selected = employee.id == selectedEmployeeId
            SectionCard {
                Surface(onClick = { onSelect(employee.id) }, color = MaterialTheme.colorScheme.surface) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceMD),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        EmployeeAvatar(employee.fullName)
                        Column(modifier = Modifier.weight(1f)) {
                            Text(employee.fullName, style = MaterialTheme.typography.titleMedium)
                            Text("${employee.jobTitle} - ${employee.department}", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        }
                        if (selected) {
                            StatChip(
                                icon = Icons.Outlined.Schedule,
                                text = "Selectionne",
                                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                contentColor = MaterialTheme.colorScheme.primary,
                                compact = true
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ReasonPickList(
    reasons: List<AbsenceReason>,
    selectedReasonId: String,
    onSelect: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(Dimens.SpaceSM)) {
        reasons.forEach { reason ->
            val selected = selectedReasonId == reason.id
            SectionCard {
                Surface(onClick = { onSelect(reason.id) }, color = MaterialTheme.colorScheme.surface) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(reason.label, style = MaterialTheme.typography.titleMedium)
                        if (selected) {
                            StatChip(
                                icon = Icons.Outlined.PersonOff,
                                text = "Choisi",
                                containerColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.14f),
                                contentColor = MaterialTheme.colorScheme.tertiary,
                                compact = true
                            )
                        }
                    }
                }
            }
        }
    }
}
