package cm.crfc.pointage.ui.exports

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
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cm.crfc.pointage.data.ExportService
import cm.crfc.pointage.data.ReportRepository
import cm.crfc.pointage.model.ExportFileRecord
import cm.crfc.pointage.model.ExportFileType
import cm.crfc.pointage.model.ExportPayload
import cm.crfc.pointage.model.User
import cm.crfc.pointage.ui.components.SectionCard
import cm.crfc.pointage.ui.theme.Dimens
import cm.crfc.pointage.ui.theme.LocalCrfcUiExtras
import cm.crfc.pointage.ui.theme.TextSecondary
import cm.crfc.pointage.ui.theme.horizontalPadding
import cm.crfc.pointage.util.formatDateTime
import cm.crfc.pointage.util.todayIso
import kotlinx.coroutines.launch

@Composable
fun ExportsScreen(
    user: User,
    reportRepository: ReportRepository,
    exportService: ExportService,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val extras = LocalCrfcUiExtras.current
    val horizontalPadding = horizontalPadding()
    val allFiles by exportService.observeExportFiles().collectAsStateWithLifecycle(initialValue = emptyList())
    val reports by reportRepository.observeReportsFor(user).collectAsStateWithLifecycle(initialValue = emptyList())
    val employees by reportRepository.observeEmployees().collectAsStateWithLifecycle(initialValue = emptyList())
    val reasons by reportRepository.observeAbsenceReasons().collectAsStateWithLifecycle(initialValue = emptyList())
    var tab by remember { mutableStateOf(ExportFileType.PDF) }

    val files = allFiles.filter { it.type == tab }

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
                    Row(horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceMD), verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Outlined.ArrowBack, contentDescription = null)
                        }
                        Text("Archives & Exports", style = MaterialTheme.typography.headlineMedium)
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceMD)) {
                    ExportTab("Rapports PDF", tab == ExportFileType.PDF, onClick = { tab = ExportFileType.PDF }, modifier = Modifier.weight(1f))
                    ExportTab("Syntheses Excel", tab == ExportFileType.EXCEL, onClick = { tab = ExportFileType.EXCEL }, modifier = Modifier.weight(1f))
                }
            }
        }

        if (files.isEmpty()) {
            item {
                SectionCard(modifier = Modifier.padding(horizontal = horizontalPadding)) {
                    Text(
                        text = "Aucun fichier archive pour cet onglet.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
            }
        } else {
            items(files, key = { it.id }) { item ->
                SectionCard(modifier = Modifier.padding(horizontal = horizontalPadding)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceMD),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = if (item.type == ExportFileType.PDF) extras.orangeLight else extras.greenLight,
                            shape = MaterialTheme.shapes.small
                        ) {
                            Icon(
                                if (item.type == ExportFileType.PDF) Icons.Outlined.Description else Icons.Outlined.GridView,
                                contentDescription = null,
                                modifier = Modifier.padding(12.dp),
                                tint = if (item.type == ExportFileType.PDF) extras.orangeAccent else extras.greenAccent
                            )
                        }
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(item.fileName, style = MaterialTheme.typography.titleMedium)
                            Text("${formatDateTime(item.createdAt)} - ${formatBytes(item.sizeBytes)}", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        }
                        IconButton(onClick = { exportService.shareExport(item) }) {
                            Icon(Icons.Outlined.Share, contentDescription = null)
                        }
                        IconButton(onClick = { scope.launch { exportService.deleteExport(item) } }) {
                            Icon(Icons.Outlined.Delete, contentDescription = null)
                        }
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = horizontalPadding),
                horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceMD)
            ) {
                Surface(
                    onClick = {
                        reports.firstOrNull()?.let { latest ->
                            exportService.exportPdf(latest, employees, reasons, user)
                        }
                    },
                    color = MaterialTheme.colorScheme.surface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Nouveau PDF",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        style = MaterialTheme.typography.labelLarge
                    )
                }
                Surface(
                    onClick = {
                        if (reports.isNotEmpty()) {
                            exportService.exportExcel(
                                ExportPayload(
                                    reports = reports,
                                    employees = employees,
                                    absenceReasons = reasons,
                                    author = user,
                                    periodStart = reports.minOf { it.date },
                                    periodEnd = reports.maxOf { it.date }.ifBlank { todayIso() }
                                )
                            )
                        }
                    },
                    color = extras.orangeAccent,
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Synthese Excel",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }

        item { Spacer(modifier = Modifier.navigationBarsPadding()) }
    }
}

@Composable
private fun ExportTab(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val extras = LocalCrfcUiExtras.current
    Surface(
        onClick = onClick,
        color = if (selected) extras.greenLight else MaterialTheme.colorScheme.surface,
        shape = MaterialTheme.shapes.small,
        modifier = modifier
    ) {
        Text(
            text = label,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            style = MaterialTheme.typography.labelLarge,
            color = if (selected) MaterialTheme.colorScheme.primary else TextSecondary
        )
    }
}

private fun formatBytes(sizeBytes: Long): String {
    if (sizeBytes < 1024) return "$sizeBytes B"
    val kb = sizeBytes / 1024.0
    if (kb < 1024) return String.format("%.1f KB", kb)
    return String.format("%.1f MB", kb / 1024.0)
}
