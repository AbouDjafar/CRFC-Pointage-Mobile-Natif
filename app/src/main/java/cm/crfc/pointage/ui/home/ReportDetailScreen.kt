package cm.crfc.pointage.ui.home

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.LockOpen
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cm.crfc.pointage.data.AuthRepository
import cm.crfc.pointage.data.ExportService
import cm.crfc.pointage.data.ReportRepository
import cm.crfc.pointage.model.ReportStatus
import cm.crfc.pointage.model.User
import cm.crfc.pointage.model.UserRole
import cm.crfc.pointage.ui.components.CrfcCard
import cm.crfc.pointage.ui.components.EmptyState
import cm.crfc.pointage.ui.components.HeaderCard
import cm.crfc.pointage.ui.components.LabelValue
import cm.crfc.pointage.ui.theme.LocalCrfcExtraColors
import cm.crfc.pointage.util.formatDateTime
import cm.crfc.pointage.util.formatDisplayDate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun ReportDetailScreen(
    user: User,
    reportId: String,
    reportRepository: ReportRepository,
    authRepository: AuthRepository,
    exportService: ExportService,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val extra = LocalCrfcExtraColors.current
    val report by reportRepository.observeReportById(reportId).collectAsStateWithLifecycle(initialValue = null)
    val employees by reportRepository.observeEmployees().collectAsStateWithLifecycle(initialValue = emptyList())
    val reasons by reportRepository.observeAbsenceReasons().collectAsStateWithLifecycle(initialValue = emptyList())
    val users by authRepository.observeUsers().collectAsStateWithLifecycle(initialValue = emptyList())

    if (report == null) {
        Column(modifier = Modifier.padding(24.dp)) {
            HeaderCard(title = "Rapport", subtitle = "Introuvable", actions = {
                IconButton(onClick = onBack) {
                    Icon(Icons.Rounded.ArrowBack, contentDescription = null, tint = androidx.compose.ui.graphics.Color.White)
                }
            })
            CrfcCard(modifier = Modifier.padding(top = 16.dp)) {
                EmptyState("Rapport introuvable", "Ce rapport a peut-etre ete supprime.")
            }
        }
        return
    }

    val author = users.firstOrNull { it.id == report!!.createdBy } ?: user

    Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        HeaderCard(
            title = formatDisplayDate(report!!.date),
            subtitle = if (report!!.status == ReportStatus.FINALIZED) "Rapport finalise" else "Rapport brouillon",
            actions = {
                Row {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Rounded.ArrowBack, contentDescription = null, tint = androidx.compose.ui.graphics.Color.White)
                    }
                    IconButton(onClick = {
                        val result = exportService.exportPdf(report!!, employees, reasons, author)
                        if (!result.success) Toast.makeText(context, result.error, Toast.LENGTH_LONG).show()
                    }) {
                        Icon(Icons.Rounded.Share, contentDescription = null, tint = androidx.compose.ui.graphics.Color.White)
                    }
                }
            }
        )

        CrfcCard(modifier = Modifier.padding(horizontal = 16.dp)) {
            Text("Meta-donnees", style = MaterialTheme.typography.titleMedium)
            Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.padding(top = 12.dp)) {
                LabelValue("Redige par", author.fullName)
                LabelValue("Fonction", author.jobTitle)
                LabelValue("Modifie le", formatDateTime(report!!.updatedAt))
            }
        }

        CrfcCard(modifier = Modifier.padding(horizontal = 16.dp)) {
            Text("Retardataires", style = MaterialTheme.typography.titleMedium, color = extra.late)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 12.dp)) {
                report!!.lateEntries.ifEmpty {
                    listOf()
                }.forEach { entry ->
                    Text("${employees.firstOrNull { it.id == entry.employeeId }?.fullName ?: "Inconnu"} • ${entry.arrivalTime} • ${entry.minutesLate} min")
                }
                if (report!!.lateEntries.isEmpty()) {
                    Text("Aucun retardataire", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        CrfcCard(modifier = Modifier.padding(horizontal = 16.dp)) {
            Text("Absents", style = MaterialTheme.typography.titleMedium, color = extra.absent)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 12.dp)) {
                report!!.absenceEntries.forEach { entry ->
                    Text("${employees.firstOrNull { it.id == entry.employeeId }?.fullName ?: "Inconnu"} • ${reasons.firstOrNull { it.id == entry.reasonId }?.label ?: "Motif"}")
                }
                if (report!!.absenceEntries.isEmpty()) {
                    Text("Aucun absent", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        CrfcCard(modifier = Modifier.padding(horizontal = 16.dp)) {
            LabelValue("Visiteurs", report!!.visitorCount.toString())
        }

        CrfcCard(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                if (report!!.status == ReportStatus.DRAFT) {
                    Button(
                        modifier = Modifier.weight(1f),
                        onClick = {
                            CoroutineScope(Dispatchers.Main).launch {
                                reportRepository.finalizeReport(report!!)
                            }
                        }
                    ) { Text("Finaliser") }
                } else if (user.role == UserRole.ADMIN) {
                    OutlinedButton(
                        modifier = Modifier.weight(1f),
                        onClick = {
                            CoroutineScope(Dispatchers.Main).launch {
                                reportRepository.reopenReport(report!!)
                            }
                        }
                    ) {
                        Icon(Icons.Rounded.LockOpen, contentDescription = null)
                        Text("Reouvrir", modifier = Modifier.padding(start = 8.dp))
                    }
                }
                if (user.role == UserRole.ADMIN) {
                    OutlinedButton(
                        modifier = Modifier.weight(1f),
                        onClick = {
                            CoroutineScope(Dispatchers.Main).launch {
                                reportRepository.deleteReport(report!!.id)
                                onBack()
                            }
                        }
                    ) {
                        Icon(Icons.Rounded.Delete, contentDescription = null)
                        Text("Supprimer", modifier = Modifier.padding(start = 8.dp))
                    }
                }
            }
        }
    }
}

