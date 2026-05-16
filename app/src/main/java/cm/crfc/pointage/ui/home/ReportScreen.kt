package cm.crfc.pointage.ui.home

import android.widget.Toast
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
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Group
import androidx.compose.material.icons.rounded.LockOpen
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
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
import cm.crfc.pointage.ui.components.Badge
import cm.crfc.pointage.ui.components.ClickRow
import cm.crfc.pointage.ui.components.CrfcCard
import cm.crfc.pointage.ui.components.EmptyState
import cm.crfc.pointage.ui.components.HeaderCard
import cm.crfc.pointage.ui.components.SectionTitle
import cm.crfc.pointage.ui.components.StatPill
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

    val lateAvailable = employees.filter { it.isActive && report?.absenceEntries?.none { a -> a.employeeId == it.id } != false && report?.lateEntries?.none { l -> l.employeeId == it.id } != false }
    val absenceAvailable = employees.filter { it.isActive && report?.lateEntries?.none { l -> l.employeeId == it.id } != false && report?.absenceEntries?.none { a -> a.employeeId == it.id } != false }

    LazyColumn(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item {
            HeaderCard(
                title = "Rapport du jour",
                subtitle = formatDisplayDate(today),
                actions = {
                    if (report != null) {
                        IconButton(onClick = { onOpenReport(report.id) }) {
                            Icon(Icons.Rounded.Description, contentDescription = null, tint = androidx.compose.ui.graphics.Color.White)
                        }
                    }
                }
            )
        }

        if (report == null) {
            item {
                CrfcCard(modifier = Modifier.padding(horizontal = 16.dp)) {
                    EmptyState("Aucun rapport aujourd'hui", "Cree le rapport du jour pour commencer la saisie.")
                    Button(
                        modifier = Modifier.fillMaxWidth(),
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
                CrfcCard(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Badge(if (report.status == ReportStatus.FINALIZED) "Finalise" else "Brouillon", if (report.status == ReportStatus.FINALIZED) extra.success else extra.warning)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(onClick = {
                                val result = exportService.exportPdf(report, employees, reasons, user)
                                if (!result.success) {
                                    Toast.makeText(context, result.error, Toast.LENGTH_LONG).show()
                                }
                            }) {
                                Icon(Icons.Rounded.Share, contentDescription = null)
                                Text("PDF", modifier = Modifier.padding(start = 8.dp))
                            }
                            FilledTonalButton(onClick = { onOpenReport(report.id) }) {
                                Text("Detail")
                            }
                        }
                    }
                }
            }

            item {
                CrfcCard(modifier = Modifier.padding(horizontal = 16.dp)) {
                    SectionTitle("Retardataires", report.lateEntries.size) {
                        if (report.status != ReportStatus.FINALIZED) {
                            TextButton(onClick = {
                                selectedEmployeeId = lateAvailable.firstOrNull()?.id.orEmpty()
                                arrivalTime = "08:30"
                                activeSheet = ReportSheetMode.LATE
                            }) { Text("Ajouter") }
                        }
                    }
                    if (report.lateEntries.isEmpty()) {
                        EmptyState("Aucun retardataire", "La saisie des retards apparaitra ici.")
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 10.dp)) {
                            report.lateEntries.forEach { entry ->
                                ClickRow(
                                    title = employees.firstOrNull { it.id == entry.employeeId }?.fullName ?: "Inconnu",
                                    subtitle = "Arrivee ${entry.arrivalTime} • ${entry.minutesLate} min"
                                ) {
                                    if (report.status != ReportStatus.FINALIZED) {
                                        CoroutineScope(Dispatchers.Main).launch {
                                            reportRepository.removeLateEntry(report, entry.id)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                CrfcCard(modifier = Modifier.padding(horizontal = 16.dp)) {
                    SectionTitle("Absents", report.absenceEntries.size) {
                        if (report.status != ReportStatus.FINALIZED) {
                            TextButton(onClick = {
                                selectedEmployeeId = absenceAvailable.firstOrNull()?.id.orEmpty()
                                selectedReasonId = reasons.firstOrNull()?.id.orEmpty()
                                comment = ""
                                activeSheet = ReportSheetMode.ABSENT
                            }) { Text("Ajouter") }
                        }
                    }
                    if (report.absenceEntries.isEmpty()) {
                        EmptyState("Aucun absent", "Les absences du jour seront listees ici.")
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 10.dp)) {
                            report.absenceEntries.forEach { entry ->
                                ClickRow(
                                    title = employees.firstOrNull { it.id == entry.employeeId }?.fullName ?: "Inconnu",
                                    subtitle = reasons.firstOrNull { it.id == entry.reasonId }?.label ?: "Motif inconnu"
                                ) {
                                    if (report.status != ReportStatus.FINALIZED) {
                                        CoroutineScope(Dispatchers.Main).launch {
                                            reportRepository.removeAbsenceEntry(report, entry.id)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                CrfcCard(modifier = Modifier.padding(horizontal = 16.dp)) {
                    SectionTitle("Visiteurs", report.visitorCount)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                CoroutineScope(Dispatchers.Main).launch {
                                    reportRepository.setVisitorCount(report, report.visitorCount - 1)
                                }
                            },
                            enabled = report.status != ReportStatus.FINALIZED
                        ) { Text("-1") }
                        OutlinedButton(
                            onClick = {
                                CoroutineScope(Dispatchers.Main).launch {
                                    reportRepository.setVisitorCount(report, report.visitorCount + 1)
                                }
                            },
                            enabled = report.status != ReportStatus.FINALIZED
                        ) { Text("+1") }
                        StatPill(Icons.Rounded.Group, "${report.visitorCount} visiteur(s)", MaterialTheme.colorScheme.primary)
                    }
                }
            }

            item {
                CrfcCard(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        if (report.status == ReportStatus.DRAFT) {
                            Button(
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    CoroutineScope(Dispatchers.Main).launch {
                                        reportRepository.finalizeReport(report)
                                    }
                                }
                            ) {
                                Icon(Icons.Rounded.CheckCircle, contentDescription = null)
                                Text("Finaliser", modifier = Modifier.padding(start = 8.dp))
                            }
                        } else if (user.role == UserRole.ADMIN) {
                            OutlinedButton(
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    CoroutineScope(Dispatchers.Main).launch {
                                        reportRepository.reopenReport(report)
                                    }
                                }
                            ) {
                                Icon(Icons.Rounded.LockOpen, contentDescription = null)
                                Text("Reouvrir", modifier = Modifier.padding(start = 8.dp))
                            }
                        }
                    }
                }
            }
        }
    }

    if (activeSheet != null && report != null) {
        ModalBottomSheet(onDismissRequest = { activeSheet = null }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    if (activeSheet == ReportSheetMode.LATE) "Ajouter un retardataire" else "Ajouter un absent",
                    style = MaterialTheme.typography.titleLarge
                )
                val pool = if (activeSheet == ReportSheetMode.LATE) lateAvailable else absenceAvailable
                if (pool.isEmpty()) {
                    EmptyState("Plus personne a saisir", "Tous les employes actifs ont deja ete comptabilises.")
                } else {
                    Text("Employe", style = MaterialTheme.typography.labelLarge)
                    pool.forEach { employee ->
                        OutlinedButton(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = { selectedEmployeeId = employee.id }
                        ) {
                            Text(employee.fullName)
                        }
                    }
                    if (activeSheet == ReportSheetMode.LATE) {
                        TextFieldBlock(arrivalTime, { arrivalTime = it }, "Heure d'arrivee (HH:MM)")
                        Button(
                            modifier = Modifier.fillMaxWidth(),
                            enabled = selectedEmployeeId.isNotBlank(),
                            onClick = {
                                CoroutineScope(Dispatchers.Main).launch {
                                    reportRepository.addLateEntry(report, selectedEmployeeId, arrivalTime)
                                    activeSheet = null
                                }
                            }
                        ) { Text("Confirmer le retard") }
                    } else {
                        Text("Motif", style = MaterialTheme.typography.labelLarge)
                        reasons.forEach { reason ->
                            OutlinedButton(
                                modifier = Modifier.fillMaxWidth(),
                                onClick = { selectedReasonId = reason.id }
                            ) { Text(reason.label) }
                        }
                        TextFieldBlock(comment, { comment = it }, "Commentaire", singleLine = false)
                        Button(
                            modifier = Modifier.fillMaxWidth(),
                            enabled = selectedEmployeeId.isNotBlank() && selectedReasonId.isNotBlank(),
                            onClick = {
                                CoroutineScope(Dispatchers.Main).launch {
                                    reportRepository.addAbsenceEntry(report, selectedEmployeeId, selectedReasonId, comment)
                                    activeSheet = null
                                }
                            }
                        ) { Text("Confirmer l'absence") }
                    }
                }
            }
        }
    }
}
