package cm.crfc.pointage.ui.historique

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cm.crfc.pointage.data.ExportService
import cm.crfc.pointage.data.ReportRepository
import cm.crfc.pointage.model.ExportPayload
import cm.crfc.pointage.model.ReportStatus
import cm.crfc.pointage.model.User
import cm.crfc.pointage.model.UserRole
import cm.crfc.pointage.ui.components.AppHeader
import cm.crfc.pointage.ui.components.ButtonVariant
import cm.crfc.pointage.ui.components.ConfirmDialog
import cm.crfc.pointage.ui.components.EmptyState
import cm.crfc.pointage.ui.components.FilterChipRow
import cm.crfc.pointage.ui.components.HeaderActionPill
import cm.crfc.pointage.ui.components.PrimaryButton
import cm.crfc.pointage.ui.components.SectionCard
import cm.crfc.pointage.ui.components.StatChip
import cm.crfc.pointage.ui.theme.Dimens
import cm.crfc.pointage.ui.theme.LocalCrfcUiExtras
import cm.crfc.pointage.ui.theme.horizontalPadding
import cm.crfc.pointage.util.formatDisplayDate
import cm.crfc.pointage.util.subtractDays
import cm.crfc.pointage.util.todayIso
import kotlinx.coroutines.launch

@Composable
fun HistoryScreen(
    user: User,
    reportRepository: ReportRepository,
    exportService: ExportService,
    onOpenReport: (String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val extras = LocalCrfcUiExtras.current
    val horizontalPadding = horizontalPadding()

    val reports by reportRepository.observeReportsFor(user).collectAsStateWithLifecycle(initialValue = emptyList())
    val employees by reportRepository.observeEmployees().collectAsStateWithLifecycle(initialValue = emptyList())
    val reasons by reportRepository.observeAbsenceReasons().collectAsStateWithLifecycle(initialValue = emptyList())

    var period by remember { mutableStateOf("Tout") }
    var status by remember { mutableStateOf("Tous") }
    var reportToDelete by remember { mutableStateOf<String?>(null) }

    val threshold = when (period) {
        "7 jours" -> subtractDays(7)
        "30 jours" -> subtractDays(30)
        "90 jours" -> subtractDays(90)
        else -> null
    }

    val filteredReports = reports
        .filter { threshold == null || it.date >= threshold }
        .filter {
            when (status) {
                "Brouillons" -> it.status == ReportStatus.DRAFT
                "Finalises" -> it.status == ReportStatus.FINALIZED
                else -> true
            }
        }
        .sortedByDescending { it.date }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(Dimens.SpaceLG)
    ) {
        item {
            AppHeader(
                title = "Historique",
                subtitle = "${filteredReports.size} rapport(s) affiches",
                actions = {
                    HeaderActionPill(
                        text = "Excel",
                        onClick = {
                            if (filteredReports.isEmpty()) {
                                Toast.makeText(context, "Aucune donnee a exporter.", Toast.LENGTH_LONG).show()
                            } else {
                                val result = exportService.exportExcel(
                                    ExportPayload(
                                        reports = filteredReports,
                                        employees = employees,
                                        absenceReasons = reasons,
                                        author = user,
                                        periodStart = threshold ?: filteredReports.minOfOrNull { it.date } ?: todayIso(),
                                        periodEnd = todayIso()
                                    )
                                )
                                if (!result.success) Toast.makeText(context, result.error, Toast.LENGTH_LONG).show()
                            }
                        }
                    )
                }
            )
        }
        item {
            SectionCard(modifier = Modifier.padding(horizontal = horizontalPadding)) {
                Text("Periode", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(Dimens.SpaceSM))
                FilterChipRow(
                    options = listOf("Tout", "7 jours", "30 jours", "90 jours"),
                    selectedOption = period,
                    onOptionSelected = { period = it },
                    activeColor = extras.orangeAccent
                )
                Spacer(modifier = Modifier.height(Dimens.SpaceLG))
                Text("Statut", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(Dimens.SpaceSM))
                FilterChipRow(
                    options = listOf("Tous", "Finalises", "Brouillons"),
                    selectedOption = status,
                    onOptionSelected = { status = it },
                    activeColor = extras.purpleAccent
                )
            }
        }

        if (filteredReports.isEmpty()) {
            item {
                SectionCard(modifier = Modifier.padding(horizontal = horizontalPadding)) {
                    EmptyState(
                        icon = Icons.Outlined.Download,
                        title = "Aucun rapport",
                        subtitle = "Aucun rapport ne correspond aux filtres actuellement selectionnes."
                    )
                }
            }
        } else {
            items(filteredReports, key = { it.id }) { report ->
                SectionCard(modifier = Modifier.padding(horizontal = horizontalPadding), highlighted = true) {
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Column(verticalArrangement = Arrangement.spacedBy(Dimens.SpaceSM)) {
                            Text(formatDisplayDate(report.date), style = MaterialTheme.typography.titleLarge)
                            Row(horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceSM)) {
                                StatChip(
                                    icon = Icons.Outlined.Schedule,
                                    text = "${report.lateEntries.size} retards",
                                    containerColor = extras.orangeLight,
                                    contentColor = extras.orangeAccent
                                )
                                StatChip(
                                    icon = Icons.Outlined.WarningAmber,
                                    text = "${report.absenceEntries.size} absences",
                                    containerColor = extras.purpleLight,
                                    contentColor = extras.purpleAccent
                                )
                                StatChip(
                                    icon = Icons.Outlined.Group,
                                    text = "${report.visitorCount} visiteurs",
                                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                                    contentColor = MaterialTheme.colorScheme.primary
                                )
                            }
                            Text(
                                text = if (report.status == ReportStatus.FINALIZED) "Rapport finalise" else "Rapport en brouillon",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(Dimens.SpaceLG))
                    PrimaryButton(
                        label = "Ouvrir le rapport",
                        onClick = { onOpenReport(report.id) },
                        variant = ButtonVariant.NAVY
                    )
                    if (user.role == UserRole.ADMIN) {
                        Spacer(modifier = Modifier.height(Dimens.SpaceSM))
                        PrimaryButton(
                            label = "Supprimer",
                            onClick = { reportToDelete = report.id },
                            variant = ButtonVariant.GHOST
                        )
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.navigationBarsPadding()) }
    }

    reportToDelete?.let { targetId ->
        ConfirmDialog(
            title = "Supprimer ce rapport ?",
            message = "Cette suppression est definitive.",
            confirmLabel = "Supprimer",
            onConfirm = {
                reportToDelete = null
                scope.launch { reportRepository.deleteReport(targetId) }
            },
            onDismiss = { reportToDelete = null }
        )
    }
}
