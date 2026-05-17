package cm.crfc.pointage.ui.rapport

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.PersonOff
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Share
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cm.crfc.pointage.data.ExportService
import cm.crfc.pointage.data.ReportRepository
import cm.crfc.pointage.model.ReportStatus
import cm.crfc.pointage.model.User
import cm.crfc.pointage.model.UserRole
import cm.crfc.pointage.ui.components.AppHeader
import cm.crfc.pointage.ui.components.BottomSheetHeader
import cm.crfc.pointage.ui.components.ButtonVariant
import cm.crfc.pointage.ui.components.ConfirmDialog
import cm.crfc.pointage.ui.components.CountBadge
import cm.crfc.pointage.ui.components.EmployeeAvatar
import cm.crfc.pointage.ui.components.EmptyState
import cm.crfc.pointage.ui.components.FormField
import cm.crfc.pointage.ui.components.HeaderActionPill
import cm.crfc.pointage.ui.components.PrimaryButton
import cm.crfc.pointage.ui.components.SectionCard
import cm.crfc.pointage.ui.components.SectionHeader
import cm.crfc.pointage.ui.components.StatChip
import cm.crfc.pointage.ui.theme.Dimens
import cm.crfc.pointage.ui.theme.LocalCrfcUiExtras
import cm.crfc.pointage.ui.theme.NavyPrimary
import cm.crfc.pointage.ui.theme.TextSecondary
import cm.crfc.pointage.ui.theme.horizontalPadding
import cm.crfc.pointage.model.AbsenceReason
import cm.crfc.pointage.model.DailyReport
import cm.crfc.pointage.model.Employee
import cm.crfc.pointage.util.calcMinutesLate
import cm.crfc.pointage.util.formatDisplayDate
import cm.crfc.pointage.util.todayIso
import kotlinx.coroutines.launch

private enum class ReportSheet { LATE_PICK, LATE_DETAILS, ABSENT_PICK, ABSENT_DETAILS }

internal data class DisplayEntry(
    val id: String,
    val title: String,
    val subtitle: String,
    val note: String?
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportScreen(
    user: User,
    reportRepository: ReportRepository,
    exportService: ExportService,
    onOpenReport: (String) -> Unit
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val extras = LocalCrfcUiExtras.current
    val today = todayIso()
    val horizontalPadding = horizontalPadding()

    val employees by reportRepository.observeEmployees().collectAsStateWithLifecycle(initialValue = emptyList())
    val reasons by reportRepository.observeAbsenceReasons().collectAsStateWithLifecycle(initialValue = emptyList())
    val reports by reportRepository.observeReportsFor(user).collectAsStateWithLifecycle(initialValue = emptyList())
    val report = reports.firstOrNull { it.date == today }

    var activeSheet by remember { mutableStateOf<ReportSheet?>(null) }
    var selectedEmployeeId by remember { mutableStateOf("") }
    var selectedReasonId by remember { mutableStateOf("") }
    var arrivalTime by remember { mutableStateOf("08:30") }
    var lateNote by remember { mutableStateOf("") }
    var absenceComment by remember { mutableStateOf("") }
    var searchQuery by remember { mutableStateOf("") }
    var confirmFinalize by remember { mutableStateOf(false) }

    val lateAvailable = employees.filter {
        it.isActive &&
            report?.lateEntries?.none { entry -> entry.employeeId == it.id } != false &&
            report?.absenceEntries?.none { entry -> entry.employeeId == it.id } != false
    }
    val absenceAvailable = employees.filter {
        it.isActive &&
            report?.lateEntries?.none { entry -> entry.employeeId == it.id } != false &&
            report?.absenceEntries?.none { entry -> entry.employeeId == it.id } != false
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(Dimens.SpaceLG)
    ) {
        item {
            AppHeader(
                title = formatDisplayDate(today),
                subtitle = "Rapport du jour",
                statusText = when (report?.status) {
                    ReportStatus.FINALIZED -> "Finalise"
                    ReportStatus.DRAFT -> "Brouillon"
                    null -> "A creer"
                },
                statusContainerColor = when (report?.status) {
                    ReportStatus.FINALIZED -> extras.greenLight
                    ReportStatus.DRAFT -> extras.orangeLight
                    null -> Color.White.copy(alpha = 0.16f)
                },
                statusContentColor = when (report?.status) {
                    ReportStatus.FINALIZED -> extras.greenAccent
                    ReportStatus.DRAFT -> extras.orangeAccent
                    null -> Color.White
                },
                actions = {
                    if (report != null) {
                        HeaderActionPill(text = "Detail", onClick = { onOpenReport(report.id) })
                    }
                },
                bottomContent = {
                    if (report != null) {
                        StatRow(
                            lateCount = report.lateEntries.size,
                            absentCount = report.absenceEntries.size,
                            visitorCount = report.visitorCount
                        )
                    } else {
                        Text(
                            text = "Creez le rapport du jour pour commencer la saisie.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.82f)
                        )
                    }
                }
            )
        }

        if (report == null) {
            item {
                SectionCard(modifier = Modifier.padding(horizontal = horizontalPadding), highlighted = true) {
                    EmptyState(
                        icon = Icons.Outlined.Schedule,
                        title = "Aucun rapport pour aujourd'hui",
                        subtitle = "Creer le rapport du jour pour enregistrer les retards, absences et visiteurs.",
                        actionLabel = "Creer le rapport",
                        onAction = {
                            scope.launch {
                                val created = reportRepository.createOrUpdateTodayReport(user, today)
                                onOpenReport(created.id)
                            }
                        }
                    )
                }
            }
        } else {
            item {
                ReportSummaryCard(
                    modifier = Modifier.padding(horizontal = horizontalPadding),
                    report = report,
                    onShare = {
                        val result = exportService.exportPdf(report, employees, reasons, user)
                        if (!result.success) Toast.makeText(context, result.error, Toast.LENGTH_LONG).show()
                    }
                )
            }
            item {
                ReportEntriesCard(
                    modifier = Modifier.padding(horizontal = horizontalPadding),
                    title = "Retardataires",
                    accent = extras.orangeAccent,
                    entries = report.lateEntries.map { entry ->
                        DisplayEntry(
                            id = entry.id,
                            title = employees.firstOrNull { it.id == entry.employeeId }?.fullName ?: "Employe inconnu",
                            subtitle = "Arrivee a ${entry.arrivalTime} - ${entry.minutesLate} min de retard",
                            note = entry.note
                        )
                    },
                    emptyTitle = "Aucun retard saisi",
                    emptySubtitle = "Les agents en retard apparaitront ici.",
                    canEdit = report.status == ReportStatus.DRAFT,
                    onAdd = {
                        searchQuery = ""
                        selectedEmployeeId = lateAvailable.firstOrNull()?.id.orEmpty()
                        activeSheet = ReportSheet.LATE_PICK
                    },
                    onRemove = { entryId -> scope.launch { reportRepository.removeLateEntry(report, entryId) } }
                )
            }
            item {
                ReportEntriesCard(
                    modifier = Modifier.padding(horizontal = horizontalPadding),
                    title = "Absents",
                    accent = extras.purpleAccent,
                    entries = report.absenceEntries.map { entry ->
                        DisplayEntry(
                            id = entry.id,
                            title = employees.firstOrNull { it.id == entry.employeeId }?.fullName ?: "Employe inconnu",
                            subtitle = reasons.firstOrNull { it.id == entry.reasonId }?.label ?: "Motif inconnu",
                            note = entry.comment
                        )
                    },
                    emptyTitle = "Aucune absence saisie",
                    emptySubtitle = "Les absences du jour seront listees ici.",
                    canEdit = report.status == ReportStatus.DRAFT,
                    onAdd = {
                        searchQuery = ""
                        selectedEmployeeId = absenceAvailable.firstOrNull()?.id.orEmpty()
                        activeSheet = ReportSheet.ABSENT_PICK
                    },
                    onRemove = { entryId -> scope.launch { reportRepository.removeAbsenceEntry(report, entryId) } }
                )
            }
            item {
                VisitorCard(
                    modifier = Modifier.padding(horizontal = horizontalPadding),
                    count = report.visitorCount,
                    enabled = report.status == ReportStatus.DRAFT,
                    onMinus = { scope.launch { reportRepository.setVisitorCount(report, report.visitorCount - 1) } },
                    onPlus = { scope.launch { reportRepository.setVisitorCount(report, report.visitorCount + 1) } }
                )
            }
            item {
                SectionCard(modifier = Modifier.padding(horizontal = horizontalPadding)) {
                    if (report.status == ReportStatus.DRAFT) {
                        PrimaryButton(
                            label = "Finaliser le rapport",
                            onClick = { confirmFinalize = true },
                            variant = ButtonVariant.GREEN
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(Dimens.SpaceMD)) {
                            Text(
                                text = "Ce rapport est verrouille et pret a etre partage.",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            if (user.role == UserRole.ADMIN) {
                                PrimaryButton(
                                    label = "Reouvrir le rapport",
                                    onClick = { scope.launch { reportRepository.reopenReport(report) } },
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

    if (confirmFinalize && report != null) {
        ConfirmDialog(
            title = "Finaliser ce rapport ?",
            message = "Une fois finalise, le rapport ne sera plus modifiable par les agents.",
            confirmLabel = "Finaliser",
            onConfirm = {
                confirmFinalize = false
                scope.launch { reportRepository.finalizeReport(report) }
            },
            onDismiss = { confirmFinalize = false }
        )
    }

    if (activeSheet != null && report != null) {
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
                        FormField("Rechercher", searchQuery, { searchQuery = it }, placeholder = "Nom de l'employe")
                        EmployeeSelectionBlock(lateAvailable, searchQuery, selectedEmployeeId) { selectedEmployeeId = it }
                        PrimaryButton(
                            label = "Continuer",
                            onClick = { activeSheet = ReportSheet.LATE_DETAILS },
                            enabled = selectedEmployeeId.isNotBlank()
                        )
                    }
                    ReportSheet.LATE_DETAILS -> {
                        BottomSheetHeader(title = "Heure d'arrivee", onClose = { activeSheet = null })
                        FormField("Heure d'arrivee (HH:MM)", arrivalTime, { arrivalTime = it })
                        FormField("Note", lateNote, { lateNote = it }, placeholder = "Optionnel", singleLine = false)
                        Text("Retard calcule: ${calcMinutesLate(arrivalTime)} min", style = MaterialTheme.typography.titleMedium)
                        PrimaryButton(
                            label = "Confirmer le retard",
                            onClick = {
                                scope.launch {
                                    reportRepository.addLateEntry(report, selectedEmployeeId, arrivalTime, lateNote)
                                    lateNote = ""
                                    activeSheet = null
                                }
                            },
                            enabled = selectedEmployeeId.isNotBlank()
                        )
                    }
                    ReportSheet.ABSENT_PICK -> {
                        BottomSheetHeader(title = "Choisir un employe", onClose = { activeSheet = null })
                        FormField("Rechercher", searchQuery, { searchQuery = it }, placeholder = "Nom de l'employe")
                        EmployeeSelectionBlock(absenceAvailable, searchQuery, selectedEmployeeId) { selectedEmployeeId = it }
                        PrimaryButton(
                            label = "Continuer",
                            onClick = {
                                selectedReasonId = reasons.firstOrNull()?.id.orEmpty()
                                activeSheet = ReportSheet.ABSENT_DETAILS
                            },
                            enabled = selectedEmployeeId.isNotBlank()
                        )
                    }
                    ReportSheet.ABSENT_DETAILS -> {
                        BottomSheetHeader(title = "Motif d'absence", onClose = { activeSheet = null })
                        ReasonSelectionBlock(reasons, selectedReasonId) { selectedReasonId = it }
                        FormField("Commentaire", absenceComment, { absenceComment = it }, placeholder = "Optionnel", singleLine = false)
                        PrimaryButton(
                            label = "Confirmer l'absence",
                            onClick = {
                                scope.launch {
                                    reportRepository.addAbsenceEntry(report, selectedEmployeeId, selectedReasonId, absenceComment)
                                    absenceComment = ""
                                    activeSheet = null
                                }
                            },
                            enabled = selectedReasonId.isNotBlank() && selectedEmployeeId.isNotBlank()
                        )
                    }
                    null -> Unit
                }
            }
        }
    }
}

@Composable
private fun StatRow(lateCount: Int, absentCount: Int, visitorCount: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceSM)) {
        StatChip(Icons.Outlined.Schedule, "$lateCount retards", Color.White.copy(alpha = 0.12f), Color.White)
        StatChip(Icons.Outlined.PersonOff, "$absentCount absences", Color.White.copy(alpha = 0.12f), Color.White)
        StatChip(Icons.Outlined.Group, "$visitorCount visiteurs", Color.White.copy(alpha = 0.12f), Color.White)
    }
}

@Composable
private fun ReportSummaryCard(
    report: DailyReport,
    onShare: () -> Unit,
    modifier: Modifier = Modifier
) {
    SectionCard(modifier = modifier, highlighted = true) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(Dimens.SpaceXS)) {
                Text("Synthese du rapport", style = MaterialTheme.typography.titleLarge)
                Text(report.introText, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
            }
            IconButton(onClick = onShare) {
                Icon(Icons.Outlined.Share, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            }
        }
        Spacer(modifier = Modifier.height(Dimens.SpaceLG))
        Row(horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceSM)) {
            MetricCard("Retard cumule", "${report.lateEntries.sumOf { it.minutesLate }} min", modifier = Modifier.weight(1f))
            MetricCard("Visiteurs", report.visitorCount.toString(), modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun MetricCard(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.06f)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = Dimens.SpaceLG, vertical = Dimens.SpaceMD),
            verticalArrangement = Arrangement.spacedBy(Dimens.SpaceXS)
        ) {
            Text(label, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            Text(value, style = MaterialTheme.typography.titleLarge, color = NavyPrimary)
        }
    }
}

@Composable
internal fun ReportEntriesCard(
    title: String,
    accent: Color,
    entries: List<DisplayEntry>,
    emptyTitle: String,
    emptySubtitle: String,
    canEdit: Boolean,
    onAdd: () -> Unit,
    onRemove: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    SectionCard(modifier = modifier) {
        SectionHeader(
            title = title,
            count = entries.size,
            accentColor = accent,
            icon = if (title == "Retardataires") Icons.Outlined.Schedule else Icons.Outlined.PersonOff,
            onAdd = if (canEdit) onAdd else null
        )
        Spacer(modifier = Modifier.height(Dimens.SpaceLG))
        if (entries.isEmpty()) {
            EmptyState(
                icon = if (title == "Retardataires") Icons.Outlined.Schedule else Icons.Outlined.PersonOff,
                title = emptyTitle,
                subtitle = emptySubtitle
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(Dimens.SpaceMD)) {
                entries.forEach { entry ->
                    DisplayEntryRow(entry = entry, accent = accent, removable = canEdit, onRemove = { onRemove(entry.id) })
                }
            }
        }
    }
}

@Composable
private fun DisplayEntryRow(
    entry: DisplayEntry,
    accent: Color,
    removable: Boolean,
    onRemove: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = accent.copy(alpha = 0.08f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.SpaceLG),
            verticalArrangement = Arrangement.spacedBy(Dimens.SpaceXS)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(entry.title, style = MaterialTheme.typography.titleMedium)
                    Text(entry.subtitle, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                }
                if (removable) {
                    Surface(
                        onClick = onRemove,
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White
                    ) {
                        Text(
                            text = "Retirer",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
            entry.note?.takeIf { it.isNotBlank() }?.let {
                Text(it, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            }
        }
    }
}

@Composable
private fun VisitorCard(
    count: Int,
    enabled: Boolean,
    onMinus: () -> Unit,
    onPlus: () -> Unit,
    modifier: Modifier = Modifier
) {
    SectionCard(modifier = modifier) {
        SectionHeader(
            title = "Visiteurs",
            count = count,
            accentColor = MaterialTheme.colorScheme.primary,
            icon = Icons.Outlined.Group
        )
        Spacer(modifier = Modifier.height(Dimens.SpaceLG))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            CounterButton("-", enabled, onMinus)
            Column {
                Text(count.toString(), style = MaterialTheme.typography.headlineLarge, color = NavyPrimary)
                Text("visiteur(s)", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            }
            CounterButton("+", enabled, onPlus)
        }
    }
}

@Composable
private fun CounterButton(label: String, enabled: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        shape = CircleShape,
        color = if (enabled) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surfaceVariant
    ) {
        Box(modifier = Modifier.size(44.dp), contentAlignment = Alignment.Center) {
            Text(label, style = MaterialTheme.typography.titleLarge, color = NavyPrimary)
        }
    }
}

@Composable
private fun EmployeeSelectionBlock(
    employees: List<Employee>,
    query: String,
    selectedEmployeeId: String,
    onSelect: (String) -> Unit
) {
    val filtered = employees.filter { query.isBlank() || it.fullName.contains(query, ignoreCase = true) }
    if (filtered.isEmpty()) {
        EmptyState(Icons.Outlined.PersonOff, "Aucun employe disponible", "Tous les employes sont deja comptabilises.")
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(Dimens.SpaceSM)) {
            filtered.forEach { employee ->
                val selected = employee.id == selectedEmployeeId
                Surface(
                    onClick = { onSelect(employee.id) },
                    shape = RoundedCornerShape(16.dp),
                    color = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.06f) else MaterialTheme.colorScheme.surface,
                    border = BorderStroke(if (selected) 2.dp else 1.dp, if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = Dimens.SpaceLG, vertical = Dimens.SpaceMD),
                        horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceMD)
                    ) {
                        EmployeeAvatar(employee.fullName)
                        Column(modifier = Modifier.weight(1f)) {
                            Text(employee.fullName, style = MaterialTheme.typography.titleMedium)
                            Text(if (selected) "Selectionne" else "Touchez pour selectionner", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        }
                        Icon(Icons.Outlined.KeyboardArrowRight, contentDescription = null)
                    }
                }
            }
        }
    }
}

@Composable
private fun ReasonSelectionBlock(
    reasons: List<AbsenceReason>,
    selectedReasonId: String,
    onSelect: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(Dimens.SpaceSM)) {
        reasons.forEach { reason ->
            val selected = reason.id == selectedReasonId
            Surface(
                onClick = { onSelect(reason.id) },
                shape = RoundedCornerShape(16.dp),
                color = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.06f) else MaterialTheme.colorScheme.surface,
                border = BorderStroke(if (selected) 2.dp else 1.dp, if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Dimens.SpaceLG, vertical = Dimens.SpaceMD),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(reason.label, style = MaterialTheme.typography.bodyLarge)
                    if (selected) CountBadge(1, MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}
