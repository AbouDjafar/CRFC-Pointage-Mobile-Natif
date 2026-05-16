package cm.crfc.pointage.ui.home

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
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
import cm.crfc.pointage.model.ExportPayload
import cm.crfc.pointage.model.ReportStatus
import cm.crfc.pointage.model.User
import cm.crfc.pointage.model.UserRole
import cm.crfc.pointage.ui.components.CrfcCard
import cm.crfc.pointage.ui.components.EmptyState
import cm.crfc.pointage.ui.components.HeaderCard
import cm.crfc.pointage.ui.components.StatPill
import cm.crfc.pointage.ui.theme.LocalCrfcExtraColors
import cm.crfc.pointage.util.subtractDays
import cm.crfc.pointage.util.todayIso
import cm.crfc.pointage.util.formatDisplayDate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun HistoryScreen(
    user: User,
    reportRepository: ReportRepository,
    exportService: ExportService,
    onOpenReport: (String) -> Unit
) {
    val context = LocalContext.current
    val extra = LocalCrfcExtraColors.current
    val reports by reportRepository.observeReportsFor(user).collectAsStateWithLifecycle(initialValue = emptyList())
    val employees by reportRepository.observeEmployees().collectAsStateWithLifecycle(initialValue = emptyList())
    val reasons by reportRepository.observeAbsenceReasons().collectAsStateWithLifecycle(initialValue = emptyList())

    var period by remember { mutableStateOf("all") }
    var status by remember { mutableStateOf("all") }

    val threshold = when (period) {
        "7d" -> subtractDays(7)
        "30d" -> subtractDays(30)
        "90d" -> subtractDays(90)
        else -> null
    }

    val filtered = reports
        .filter { threshold == null || it.date >= threshold }
        .filter {
            when (status) {
                "finalized" -> it.status == ReportStatus.FINALIZED
                "draft" -> it.status == ReportStatus.DRAFT
                else -> true
            }
        }
        .sortedByDescending { it.date }

    LazyColumn(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item {
            HeaderCard(
                title = "Historique",
                subtitle = "${filtered.size} rapport(s) affiche(s)",
                actions = {
                    OutlinedButton(
                        onClick = {
                            if (filtered.isEmpty()) {
                                Toast.makeText(context, "Aucune donnee a exporter.", Toast.LENGTH_LONG).show()
                            } else {
                                val result = exportService.exportExcel(
                                    ExportPayload(
                                        reports = filtered,
                                        employees = employees,
                                        absenceReasons = reasons,
                                        author = user,
                                        periodStart = threshold ?: filtered.minOfOrNull { it.date } ?: todayIso(),
                                        periodEnd = todayIso()
                                    )
                                )
                                if (!result.success) Toast.makeText(context, result.error, Toast.LENGTH_LONG).show()
                            }
                        }
                    ) {
                        Icon(Icons.Rounded.Download, contentDescription = null)
                        Text("Excel", modifier = Modifier.padding(start = 8.dp))
                    }
                }
            )
        }

        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("all" to "Tout", "7d" to "7j", "30d" to "30j", "90d" to "3 mois").forEach { (key, label) ->
                        FilterChip(selected = period == key, onClick = { period = key }, label = { Text(label) })
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("all" to "Tous", "finalized" to "Finalises", "draft" to "Brouillons").forEach { (key, label) ->
                        FilterChip(selected = status == key, onClick = { status = key }, label = { Text(label) })
                    }
                }
            }
        }

        if (filtered.isEmpty()) {
            item {
                CrfcCard(modifier = Modifier.padding(horizontal = 16.dp)) {
                    EmptyState("Aucun rapport", "Aucun rapport ne correspond aux filtres selectionnes.")
                }
            }
        } else {
            items(filtered, key = { it.id }) { report ->
                CrfcCard(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    onClick = { onOpenReport(report.id) }
                ) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text(formatDisplayDate(report.date), style = MaterialTheme.typography.titleMedium)
                            Text(
                                if (report.status == ReportStatus.FINALIZED) "Finalise" else "Brouillon",
                                color = if (report.status == ReportStatus.FINALIZED) extra.success else extra.warning
                            )
                        }
                        if (user.role == UserRole.ADMIN) {
                            IconButton(onClick = {
                                CoroutineScope(Dispatchers.Main).launch {
                                    reportRepository.deleteReport(report.id)
                                }
                            }) {
                                Icon(Icons.Rounded.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                    Row(
                        modifier = Modifier.padding(top = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StatPill(Icons.Rounded.Download, "${report.lateEntries.size} retard(s)", extra.late)
                        StatPill(Icons.Rounded.Download, "${report.absenceEntries.size} absence(s)", extra.absent)
                        StatPill(Icons.Rounded.Download, "${report.visitorCount} visiteur(s)", MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}

