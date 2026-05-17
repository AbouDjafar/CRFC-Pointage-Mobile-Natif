package cm.crfc.pointage.ui.rapport

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.PersonOff
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import cm.crfc.pointage.data.AuthRepository
import cm.crfc.pointage.data.ExportService
import cm.crfc.pointage.data.ReportRepository
import cm.crfc.pointage.model.ReportStatus
import cm.crfc.pointage.model.User
import cm.crfc.pointage.model.UserRole
import cm.crfc.pointage.ui.components.AppHeader
import cm.crfc.pointage.ui.components.ButtonVariant
import cm.crfc.pointage.ui.components.ConfirmDialog
import cm.crfc.pointage.ui.components.EmptyState
import cm.crfc.pointage.ui.components.HeaderActionPill
import cm.crfc.pointage.ui.components.PrimaryButton
import cm.crfc.pointage.ui.components.SectionCard
import cm.crfc.pointage.ui.components.StatChip
import cm.crfc.pointage.ui.theme.Dimens
import cm.crfc.pointage.ui.theme.LocalCrfcUiExtras
import cm.crfc.pointage.ui.theme.TextSecondary
import cm.crfc.pointage.ui.theme.horizontalPadding
import cm.crfc.pointage.util.formatDateTime
import cm.crfc.pointage.util.formatDisplayDate
import kotlinx.coroutines.launch

@Composable
fun ReportDetailScreen(
    user: User,
    reportId: String,
    reportRepository: ReportRepository,
    authRepository: AuthRepository,
    exportService: ExportService,
    onBack: () -> Unit,
    contentPadding: PaddingValues = PaddingValues()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val extras = LocalCrfcUiExtras.current
    val horizontalPadding = horizontalPadding()
    val report by reportRepository.observeReportById(reportId).collectAsStateWithLifecycle(initialValue = null)
    val employees by reportRepository.observeEmployees().collectAsStateWithLifecycle(initialValue = emptyList())
    val reasons by reportRepository.observeAbsenceReasons().collectAsStateWithLifecycle(initialValue = emptyList())
    val users by authRepository.observeUsers().collectAsStateWithLifecycle(initialValue = emptyList())

    var askDelete by remember { mutableStateOf(false) }

    val currentReport = report
    if (currentReport == null) {
        SectionCard(modifier = Modifier.padding(horizontal = horizontalPadding, vertical = Dimens.SpaceLG)) {
            EmptyState(Icons.Outlined.Delete, "Rapport introuvable", "Ce rapport a peut-etre ete supprime.")
        }
        return
    }

    val author = users.firstOrNull { it.id == currentReport.createdBy } ?: user

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding),
        verticalArrangement = Arrangement.spacedBy(Dimens.SpaceLG)
    ) {
        item {
            AppHeader(
                title = formatDisplayDate(currentReport.date),
                subtitle = "Detail du rapport",
                actions = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Outlined.ArrowBack, contentDescription = null)
                    }
                    HeaderActionPill(
                        text = "Partager",
                        onClick = {
                            val result = exportService.exportPdf(currentReport, employees, reasons, author)
                            if (!result.success) Toast.makeText(context, result.error, Toast.LENGTH_LONG).show()
                        }
                    )
                }
            )
        }
        item {
            SectionCard(modifier = Modifier.padding(horizontal = horizontalPadding), highlighted = true) {
                Text("Informations generales", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(Dimens.SpaceSM))
                Text("Auteur: ${author.fullName}", style = MaterialTheme.typography.bodyMedium)
                Text("Fonction: ${author.jobTitle}", style = MaterialTheme.typography.bodyMedium)
                Text("Mis a jour: ${formatDateTime(currentReport.updatedAt)}", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                Spacer(modifier = Modifier.height(Dimens.SpaceMD))
                StatChip(
                    icon = Icons.Outlined.Schedule,
                    text = "${currentReport.visitorCount} visiteurs",
                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                    contentColor = MaterialTheme.colorScheme.primary
                )
            }
        }
        item {
            ReportEntriesCard(
                modifier = Modifier.padding(horizontal = horizontalPadding),
                title = "Retardataires",
                accent = extras.orangeAccent,
                entries = currentReport.lateEntries.map { entry ->
                    DisplayEntry(
                        id = entry.id,
                        title = employees.firstOrNull { it.id == entry.employeeId }?.fullName ?: "Employe inconnu",
                        subtitle = "${entry.arrivalTime} - ${entry.minutesLate} min",
                        note = entry.note
                    )
                },
                emptyTitle = "Aucun retardataire",
                emptySubtitle = "Aucun retard n'a ete enregistre pour cette date.",
                canEdit = false,
                onAdd = {},
                onRemove = {}
            )
        }
        item {
            ReportEntriesCard(
                modifier = Modifier.padding(horizontal = horizontalPadding),
                title = "Absents",
                accent = extras.purpleAccent,
                entries = currentReport.absenceEntries.map { entry ->
                    DisplayEntry(
                        id = entry.id,
                        title = employees.firstOrNull { it.id == entry.employeeId }?.fullName ?: "Employe inconnu",
                        subtitle = reasons.firstOrNull { it.id == entry.reasonId }?.label ?: "Motif inconnu",
                        note = entry.comment
                    )
                },
                emptyTitle = "Aucun absent",
                emptySubtitle = "Aucune absence n'a ete enregistree pour cette date.",
                canEdit = false,
                onAdd = {},
                onRemove = {}
            )
        }
        item {
            SectionCard(modifier = Modifier.padding(horizontal = horizontalPadding)) {
                Column(verticalArrangement = Arrangement.spacedBy(Dimens.SpaceSM)) {
                    if (currentReport.status == ReportStatus.DRAFT) {
                        PrimaryButton(
                            label = "Finaliser",
                            onClick = { scope.launch { reportRepository.finalizeReport(currentReport) } }
                        )
                    } else if (user.role == UserRole.ADMIN) {
                        PrimaryButton(
                            label = "Reouvrir",
                            onClick = { scope.launch { reportRepository.reopenReport(currentReport) } },
                            variant = ButtonVariant.GHOST
                        )
                    }
                    if (user.role == UserRole.ADMIN) {
                        PrimaryButton(
                            label = "Supprimer",
                            onClick = { askDelete = true },
                            variant = ButtonVariant.GHOST
                        )
                    }
                }
            }
        }
        item { Spacer(modifier = Modifier.navigationBarsPadding()) }
    }

    if (askDelete) {
        ConfirmDialog(
            title = "Supprimer ce rapport ?",
            message = "Cette action est irreversible.",
            confirmLabel = "Supprimer",
            onConfirm = {
                askDelete = false
                scope.launch {
                    reportRepository.deleteReport(currentReport.id)
                    onBack()
                }
            },
            onDismiss = { askDelete = false }
        )
    }
}
