package cm.crfc.pointage.ui.home

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Group
import androidx.compose.material.icons.rounded.LockOpen
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cm.crfc.pointage.data.ExportService
import cm.crfc.pointage.data.ReportRepository
import cm.crfc.pointage.model.AbsenceReason
import cm.crfc.pointage.model.DailyReport
import cm.crfc.pointage.model.Employee
import cm.crfc.pointage.model.ReportStatus
import cm.crfc.pointage.model.User
import cm.crfc.pointage.model.UserRole
import cm.crfc.pointage.ui.components.AppSearchField
import cm.crfc.pointage.ui.components.AvatarCircle
import cm.crfc.pointage.ui.components.ClickRow
import cm.crfc.pointage.ui.components.CrfcCard
import cm.crfc.pointage.ui.components.EmptyState
import cm.crfc.pointage.ui.components.HeaderCard
import cm.crfc.pointage.ui.components.ScreenList
import cm.crfc.pointage.ui.components.SectionLabel
import cm.crfc.pointage.ui.components.SectionTitle
import cm.crfc.pointage.ui.components.StatPill
import cm.crfc.pointage.ui.components.StatusBadge
import cm.crfc.pointage.ui.components.TextFieldBlock
import cm.crfc.pointage.ui.theme.LocalCrfcExtraColors
import cm.crfc.pointage.util.formatDisplayDate
import cm.crfc.pointage.util.todayIso
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private enum class ReportSheetMode { LATE, ABSENT }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportScreen(
    user: User,
    reportRepository: ReportRepository,
    exportService: ExportService,
    onOpenReport: (String) -> Unit
) {
    val context = LocalContext.current
    val extra = LocalCrfcExtraColors.current
    val today = todayIso()
    val employees by reportRepository.observeEmployees().collectAsStateWithLifecycle(initialValue = emptyList())
    val reasons by reportRepository.observeAbsenceReasons().collectAsStateWithLifecycle(initialValue = emptyList())
    val reports by reportRepository.observeReportsFor(user).collectAsStateWithLifecycle(initialValue = emptyList())
    val report = reports.firstOrNull { it.date == today }

    var activeSheet by remember { mutableStateOf<ReportSheetMode?>(null) }
    var selectedEmployeeId by remember { mutableStateOf("") }
    var arrivalTime by remember { mutableStateOf("08:30") }
    var selectedReasonId by remember { mutableStateOf("") }
    var comment by remember { mutableStateOf("") }
    var employeeSearch by remember { mutableStateOf("") }

    val lateAvailable = employees.filter {
        it.isActive &&
            report?.absenceEntries?.none { a -> a.employeeId == it.id } != false &&
            report?.lateEntries?.none { l -> l.employeeId == it.id } != false
    }
    val absenceAvailable = employees.filter {
        it.isActive &&
            report?.lateEntries?.none { l -> l.employeeId == it.id } != false &&
            report?.absenceEntries?.none { a -> a.employeeId == it.id } != false
    }

    ScreenList {
        item {
            HeaderCard(
                title = formatDisplayDate(today),
                subtitle = "Rapport du jour",
                actions = {
                    if (report != null) {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Surface(
                                color = if (report.status == ReportStatus.FINALIZED) {
                                    extra.success.copy(alpha = 0.2f)
                                } else {
                                    Color.White.copy(alpha = 0.16f)
                                },
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(999.dp)
                            ) {
                                Text(
                                    text = if (report.status == ReportStatus.FINALIZED) "Finalise" else "Brouillon",
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White
                                )
                            }
                            IconButton(onClick = { onOpenReport(report.id) }) {
                                Icon(Icons.Rounded.Description, contentDescription = null, tint = Color.White)
                            }
                        }
                    }
                }
            )
        }

        if (report == null) {
            item {
                CrfcCard(modifier = Modifier.padding(horizontal = 16.dp)) {
                    EmptyState(
                        title = "Aucun rapport pour aujourd'hui",
                        subtitle = "Cree le rapport du jour pour commencer la saisie des retards, absences et visiteurs."
                    )
                    Button(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .padding(top = 4.dp),
                        onClick = {
                            CoroutineScope(Dispatchers.Main).launch {
                                val created = reportRepository.createOrUpdateTodayReport(user, today)
                                onOpenReport(created.id)
                            }
                        }
                    ) {
                        Icon(Icons.Rounded.Add, contentDescription = null)
                        Text("Creer le rapport", modifier = Modifier.padding(start = 8.dp))
                    }
                }
            }
        } else {
            item {
                TodaySummaryCard(
                    report = report,
                    onExportPdf = {
                        val result = exportService.exportPdf(report, employees, reasons, user)
                        if (!result.success) {
                            Toast.makeText(context, result.error, Toast.LENGTH_LONG).show()
                        }
                    },
                    onOpenDetail = { onOpenReport(report.id) }
                )
            }

            item {
                SectionCard(
                    title = "Retardataires",
                    count = report.lateEntries.size,
                    action = {
                        if (report.status != ReportStatus.FINALIZED) {
                            TextButton(onClick = {
                                selectedEmployeeId = lateAvailable.firstOrNull()?.id.orEmpty()
                                arrivalTime = "08:30"
                                employeeSearch = ""
                                activeSheet = ReportSheetMode.LATE
                            }) {
                                Text("Ajouter")
                            }
                        }
                    }
                ) {
                    if (report.lateEntries.isEmpty()) {
                        EmptyState("Aucun retardataire", "Les retards saisis apparaitront ici.")
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            report.lateEntries.forEach { entry ->
                                val employeeName = employees.firstOrNull { it.id == entry.employeeId }?.fullName ?: "Inconnu"
                                ClickRow(
                                    title = employeeName,
                                    subtitle = "${entry.minutesLate} min de retard - arrivee ${entry.arrivalTime}",
                                    tint = extra.late,
                                    trailing = {
                                        if (report.status != ReportStatus.FINALIZED) {
                                            IconButton(onClick = {
                                                CoroutineScope(Dispatchers.Main).launch {
                                                    reportRepository.removeLateEntry(report, entry.id)
                                                }
                                            }) {
                                                Icon(
                                                    Icons.Rounded.Remove,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.error
                                                )
                                            }
                                        }
                                    },
                                    onClick = {}
                                )
                            }
                            StatPill(
                                icon = Icons.Rounded.CheckCircle,
                                label = "Total: ${report.lateEntries.sumOf { it.minutesLate }} min",
                                color = extra.late
                            )
                        }
                    }
                }
            }

            item {
                SectionCard(
                    title = "Absents",
                    count = report.absenceEntries.size,
                    action = {
                        if (report.status != ReportStatus.FINALIZED) {
                            TextButton(onClick = {
                                selectedEmployeeId = absenceAvailable.firstOrNull()?.id.orEmpty()
                                selectedReasonId = reasons.firstOrNull()?.id.orEmpty()
                                comment = ""
                                employeeSearch = ""
                                activeSheet = ReportSheetMode.ABSENT
                            }) {
                                Text("Ajouter")
                            }
                        }
                    }
                ) {
                    if (report.absenceEntries.isEmpty()) {
                        EmptyState("Aucun absent", "Les absences confirmees apparaitront ici.")
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            report.absenceEntries.forEach { entry ->
                                val employeeName = employees.firstOrNull { it.id == entry.employeeId }?.fullName ?: "Inconnu"
                                val reasonLabel = reasons.firstOrNull { it.id == entry.reasonId }?.label ?: "Motif inconnu"
                                ClickRow(
                                    title = employeeName,
                                    subtitle = listOf(reasonLabel, entry.comment?.takeIf { it.isNotBlank() })
                                        .filterNotNull()
                                        .joinToString(" - "),
                                    tint = extra.absent,
                                    trailing = {
                                        if (report.status != ReportStatus.FINALIZED) {
                                            IconButton(onClick = {
                                                CoroutineScope(Dispatchers.Main).launch {
                                                    reportRepository.removeAbsenceEntry(report, entry.id)
                                                }
                                            }) {
                                                Icon(
                                                    Icons.Rounded.Remove,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.error
                                                )
                                            }
                                        }
                                    },
                                    onClick = {}
                                )
                            }
                        }
                    }
                }
            }

            item {
                SectionCard(title = "Visiteurs", count = report.visitorCount) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(18.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                IconButton(
                                    onClick = {
                                        CoroutineScope(Dispatchers.Main).launch {
                                            reportRepository.setVisitorCount(report, report.visitorCount - 1)
                                        }
                                    },
                                    enabled = report.status != ReportStatus.FINALIZED
                                ) {
                                    Icon(Icons.Rounded.Remove, contentDescription = null)
                                }
                                Text(
                                    text = report.visitorCount.toString(),
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                IconButton(
                                    onClick = {
                                        CoroutineScope(Dispatchers.Main).launch {
                                            reportRepository.setVisitorCount(report, report.visitorCount + 1)
                                        }
                                    },
                                    enabled = report.status != ReportStatus.FINALIZED
                                ) {
                                    Icon(Icons.Rounded.Add, contentDescription = null)
                                }
                            }
                        }
                        StatPill(
                            icon = Icons.Rounded.Group,
                            label = "${report.visitorCount} visiteur(s)",
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            item {
                CrfcCard(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                    if (report.status == ReportStatus.DRAFT) {
                        Button(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            onClick = {
                                CoroutineScope(Dispatchers.Main).launch {
                                    reportRepository.finalizeReport(report)
                                }
                            }
                        ) {
                            Icon(Icons.Rounded.CheckCircle, contentDescription = null)
                            Text("Finaliser le rapport", modifier = Modifier.padding(start = 8.dp))
                        }
                    } else if (user.role == UserRole.ADMIN) {
                        OutlinedButton(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            onClick = {
                                CoroutineScope(Dispatchers.Main).launch {
                                    reportRepository.reopenReport(report)
                                }
                            }
                        ) {
                            Icon(Icons.Rounded.LockOpen, contentDescription = null)
                            Text("Reouvrir le rapport", modifier = Modifier.padding(start = 8.dp))
                        }
                    }
                }
            }
        }
    }

    if (activeSheet != null && report != null) {
        val pool = if (activeSheet == ReportSheetMode.LATE) lateAvailable else absenceAvailable
        val filteredPool = pool.filter {
            employeeSearch.isBlank() || it.fullName.contains(employeeSearch, ignoreCase = true)
        }

        ModalBottomSheet(onDismissRequest = { activeSheet = null }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    if (activeSheet == ReportSheetMode.LATE) "Ajouter un retard" else "Ajouter une absence",
                    style = MaterialTheme.typography.titleLarge
                )
                SectionLabel("Etape 1 : employe")
                AppSearchField(
                    value = employeeSearch,
                    onValueChange = { employeeSearch = it },
                    label = "Rechercher un employe..."
                )
                if (filteredPool.isEmpty()) {
                    EmptyState(
                        title = "Aucun employe disponible",
                        subtitle = "Tous les employes actifs ont deja ete comptabilises ou la recherche ne correspond a rien."
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        filteredPool.forEach { employee ->
                            EmployeePickerRow(
                                employee = employee,
                                selected = selectedEmployeeId == employee.id,
                                onClick = { selectedEmployeeId = employee.id }
                            )
                        }
                    }
                }

                AnimatedVisibility(
                    visible = selectedEmployeeId.isNotBlank(),
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (activeSheet == ReportSheetMode.LATE) {
                        SectionLabel("Etape 2 : details")
                        TextFieldBlock(
                            value = arrivalTime,
                            onValueChange = { arrivalTime = it },
                            label = "Heure d'arrivee (HH:MM)"
                        )
                        Button(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            onClick = {
                                CoroutineScope(Dispatchers.Main).launch {
                                    reportRepository.addLateEntry(report, selectedEmployeeId, arrivalTime)
                                    activeSheet = null
                                }
                            }
                        ) {
                            Text("Confirmer le retard")
                        }
                    } else {
                        SectionLabel("Etape 2 : motif")
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            reasons.forEach { reason ->
                                OutlinedButton(
                                    modifier = Modifier.fillMaxWidth(),
                                    onClick = { selectedReasonId = reason.id }
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
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
                            enabled = selectedReasonId.isNotBlank(),
                            onClick = {
                                CoroutineScope(Dispatchers.Main).launch {
                                    reportRepository.addAbsenceEntry(report, selectedEmployeeId, selectedReasonId, comment)
                                    activeSheet = null
                                }
                            }
                        ) {
                            Text("Confirmer l'absence")
                        }
                    }
                    }
                }
            }
        }
    }
}

@Composable
private fun TodaySummaryCard(
    report: DailyReport,
    onExportPdf: () -> Unit,
    onOpenDetail: () -> Unit
) {
    val extra = LocalCrfcExtraColors.current
    CrfcCard(modifier = Modifier.padding(horizontal = 16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                StatusBadge(
                    text = if (report.status == ReportStatus.FINALIZED) "Rapport finalise" else "Rapport brouillon",
                    color = if (report.status == ReportStatus.FINALIZED) extra.success else extra.warning
                )
                Text(
                    text = "Suivi journalier des retards, absences et visiteurs.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            OutlinedButton(onClick = onExportPdf) {
                Icon(Icons.Rounded.Share, contentDescription = null)
                Text("PDF", modifier = Modifier.padding(start = 8.dp))
            }
        }
        Row(
            modifier = Modifier.padding(top = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatPill(Icons.Rounded.CheckCircle, "${report.lateEntries.size} retard(s)", extra.late)
            StatPill(Icons.Rounded.CheckCircle, "${report.absenceEntries.size} absence(s)", extra.absent)
            StatPill(Icons.Rounded.Group, "${report.visitorCount} visiteur(s)", MaterialTheme.colorScheme.primary)
        }
        FilledTonalButton(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 14.dp),
            onClick = onOpenDetail
        ) {
            Text("Ouvrir le detail")
        }
    }
}

@Composable
private fun SectionCard(
    title: String,
    count: Int,
    action: (@Composable () -> Unit)? = null,
    content: @Composable () -> Unit
) {
    CrfcCard(modifier = Modifier.padding(horizontal = 16.dp)) {
        SectionTitle(title = title, count = count, action = action)
        Column(
            modifier = Modifier.padding(top = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            content()
        }
    }
}

@Composable
private fun EmployeePickerRow(
    employee: Employee,
    selected: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (selected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.outline
    }
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
        color = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.06f) else MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AvatarCircle(text = employee.fullName, color = MaterialTheme.colorScheme.primary)
            Column(modifier = Modifier.weight(1f)) {
                Text(employee.fullName, style = MaterialTheme.typography.bodyLarge)
                Text(
                    if (selected) "Selectionne" else "Touchez pour choisir",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (selected) {
                StatusBadge(text = "OK", color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}
