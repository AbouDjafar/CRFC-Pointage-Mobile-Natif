package cm.crfc.pointage.ui.historique

import android.widget.Toast
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
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import cm.crfc.pointage.ui.components.EmptyState
import cm.crfc.pointage.ui.components.FilterChipRow
import cm.crfc.pointage.ui.components.HeaderActionPill
import cm.crfc.pointage.ui.components.SectionCard
import cm.crfc.pointage.ui.components.StatChip
import cm.crfc.pointage.ui.theme.Dimens
import cm.crfc.pointage.ui.theme.LocalCrfcUiExtras
import cm.crfc.pointage.ui.theme.TextSecondary
import cm.crfc.pointage.ui.theme.horizontalPadding
import cm.crfc.pointage.util.formatDisplayDate
import cm.crfc.pointage.util.subtractDays
import cm.crfc.pointage.util.todayIso

@Composable
fun HistoryScreen(
    user: User,
    reportRepository: ReportRepository,
    exportService: ExportService,
    onOpenReport: (String) -> Unit
) {
    val context = LocalContext.current
    val extras = LocalCrfcUiExtras.current
    val horizontalPadding = horizontalPadding()
    val reports by reportRepository.observeReportsFor(user).collectAsStateWithLifecycle(initialValue = emptyList())
    val employees by reportRepository.observeEmployees().collectAsStateWithLifecycle(initialValue = emptyList())
    val reasons by reportRepository.observeAbsenceReasons().collectAsStateWithLifecycle(initialValue = emptyList())

    var period by remember { mutableStateOf("30 jours") }
    var status by remember { mutableStateOf("Tous") }

    val threshold = when (period) {
        "7 jours" -> subtractDays(7)
        "30 jours" -> subtractDays(30)
        "90 jours" -> subtractDays(90)
        else -> null
    }
    val filtered = reports
        .filter { threshold == null || it.date >= threshold }
        .filter {
            when (status) {
                "Brouillon" -> it.status == ReportStatus.DRAFT
                "Finalise" -> it.status == ReportStatus.FINALIZED
                else -> true
            }
        }
        .sortedByDescending { it.date }

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
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text("HISTORIQUE DES RAPPORTS", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                        Text("Report History", style = MaterialTheme.typography.headlineMedium)
                    }
                    HeaderActionPill(
                        text = "Excel",
                        onClick = {
                            if (filtered.isEmpty()) {
                                Toast.makeText(context, "Aucune donnee a exporter.", Toast.LENGTH_LONG).show()
                            } else {
                                exportService.exportExcel(
                                    ExportPayload(
                                        reports = filtered,
                                        employees = employees,
                                        absenceReasons = reasons,
                                        author = user,
                                        periodStart = threshold ?: filtered.minOf { it.date },
                                        periodEnd = filtered.maxOf { it.date }
                                    )
                                )
                            }
                        }
                    )
                }

                SectionCard {
                    Text("Periode", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.padding(top = Dimens.SpaceSM))
                    FilterChipRow(
                        options = listOf("7 jours", "30 jours", "90 jours", "Tout"),
                        selectedOption = period,
                        onOptionSelected = { period = it },
                        activeColor = extras.orangeAccent
                    )
                    Spacer(modifier = Modifier.padding(top = Dimens.SpaceLG))
                    Text("Statut", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.padding(top = Dimens.SpaceSM))
                    FilterChipRow(
                        options = listOf("Tous", "Finalise", "Brouillon"),
                        selectedOption = status,
                        onOptionSelected = { status = it },
                        activeColor = extras.greenAccent
                    )
                }
            }
        }

        if (filtered.isEmpty()) {
            item {
                SectionCard(modifier = Modifier.padding(horizontal = horizontalPadding)) {
                    EmptyState(
                        icon = Icons.Outlined.Download,
                        title = "Aucun rapport",
                        subtitle = "Aucun rapport ne correspond aux filtres selectionnes."
                    )
                }
            }
        } else {
            items(filtered, key = { it.id }) { report ->
                SectionCard(modifier = Modifier.padding(horizontal = horizontalPadding)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(Dimens.SpaceSM)
                        ) {
                            Text(formatDisplayDate(report.date), style = MaterialTheme.typography.titleLarge)
                            Row(horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceSM)) {
                                StatChip(Icons.Outlined.Schedule, "${report.lateEntries.size}", extras.orangeLight, extras.orangeAccent)
                                StatChip(Icons.Outlined.WarningAmber, "${report.absenceEntries.size}", extras.greenLight, extras.greenAccent)
                                StatChip(Icons.Outlined.Group, "${report.visitorCount}", MaterialTheme.colorScheme.primary.copy(alpha = 0.12f), MaterialTheme.colorScheme.primary)
                            }
                            Text(
                                text = if (report.status == ReportStatus.FINALIZED) "Rapport finalise" else "Rapport brouillon",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                        }
                        Surface(onClick = { onOpenReport(report.date) }, color = extras.orangeAccent, shape = MaterialTheme.shapes.small) {
                            Text(
                                text = "Ouvrir",
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.navigationBarsPadding()) }
    }
}
