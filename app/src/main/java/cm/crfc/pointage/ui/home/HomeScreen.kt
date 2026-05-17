package cm.crfc.pointage.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.ArrowForwardIos
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cm.crfc.pointage.data.ReportRepository
import cm.crfc.pointage.model.DailyReport
import cm.crfc.pointage.model.ReportStatus
import cm.crfc.pointage.model.User
import cm.crfc.pointage.ui.components.EmployeeAvatar
import cm.crfc.pointage.ui.components.PrimaryButton
import cm.crfc.pointage.ui.components.SectionCard
import cm.crfc.pointage.ui.theme.Dimens
import cm.crfc.pointage.ui.theme.LocalCrfcUiExtras
import cm.crfc.pointage.ui.theme.NavyPrimary
import cm.crfc.pointage.ui.theme.TextSecondary
import cm.crfc.pointage.ui.theme.horizontalPadding
import cm.crfc.pointage.util.formatDisplayDate
import cm.crfc.pointage.util.todayIso

@Composable
fun HomeScreen(
    user: User,
    reportRepository: ReportRepository,
    onOpenDailyReport: (String) -> Unit,
    onOpenHistory: () -> Unit,
    onOpenExports: () -> Unit,
    onOpenAnalytics: () -> Unit
) {
    val extras = LocalCrfcUiExtras.current
    val horizontalPadding = horizontalPadding()
    val today = todayIso()
    val reports by reportRepository.observeReportsFor(user).collectAsStateWithLifecycle(initialValue = emptyList())

    val todayReport = reports.firstOrNull { it.date == today }
    val lateCount = todayReport?.lateEntries?.size ?: 0
    val absentCount = todayReport?.absenceEntries?.size ?: 0
    val visitorCount = todayReport?.visitorCount ?: 0
    val incidentCount = lateCount + absentCount
    val trendReports = reports.sortedByDescending { it.date }.take(7).reversed()

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
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text("Bonjour, ${user.firstName.ifBlank { "Admin" }}", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                        Text("Tableau de Bord", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onBackground)
                    }
                    EmployeeAvatar(name = user.fullName, size = 42.dp)
                }

                SectionCard(highlighted = true) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(Dimens.SpaceXS)
                        ) {
                            Text(
                                text = if (todayReport != null) "Rapport du ${formatDisplayDate(today).substringAfter(", ")}" else "Rapport du jour",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = when (todayReport?.status) {
                                    ReportStatus.FINALIZED -> "Statut : Finalise - $incidentCount entrees"
                                    ReportStatus.DRAFT -> "Statut : Brouillon - $incidentCount entrees"
                                    null -> "Aucun rapport enregistre pour aujourd'hui"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                        Surface(
                            onClick = { onOpenDailyReport(today) },
                            color = extras.orangeAccent,
                            shape = MaterialTheme.shapes.small
                        ) {
                            Text(
                                text = if (todayReport == null) "Commencer" else "Reprendre",
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                }

                Text("SYNTHESE DU JOUR", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                Row(horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceMD)) {
                    OverviewTile("Retards", lateCount.toString(), "Depuis 08:15", modifier = Modifier.weight(1f))
                    OverviewTile("Absences", absentCount.toString(), "Motifs varies", modifier = Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceMD)) {
                    OverviewTile("Visiteurs", visitorCount.toString(), "Flux total", modifier = Modifier.weight(1f))
                    OverviewTile("Incidents", incidentCount.toString(), "Total journalier", modifier = Modifier.weight(1f))
                }

                Text("ACTIONS RAPIDES", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                QuickActionCard("Nouveau Rapport", "Initialiser la saisie du jour", Icons.Outlined.Description, onClick = { onOpenDailyReport(today) })
                QuickActionCard("Historique", "Consulter les rapports passes", Icons.Outlined.History, onClick = onOpenHistory)
                QuickActionCard("Exports & Analyses", "Generer PDF ou Excel", Icons.Outlined.FolderOpen, onClick = onOpenExports)

                SectionCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Tendance (7 jours)", style = MaterialTheme.typography.titleMedium)
                        Surface(onClick = onOpenAnalytics, color = extras.greenLight) {
                            Text(
                                text = "Voir plus",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.labelMedium,
                                color = extras.greenAccent
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(Dimens.SpaceLG))
                    TrendMiniChart(trendReports = trendReports)
                }
            }
        }
        item { Spacer(modifier = Modifier.navigationBarsPadding()) }
    }
}

@Composable
private fun OverviewTile(
    title: String,
    value: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    SectionCard(modifier = modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(Dimens.SpaceSM)) {
            Text(title, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            Text(value, style = MaterialTheme.typography.headlineMedium, color = NavyPrimary)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
        }
    }
}

@Composable
private fun QuickActionCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    SectionCard {
        Surface(onClick = onClick, color = MaterialTheme.colorScheme.surface) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceMD),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = MaterialTheme.shapes.small) {
                    Box(modifier = Modifier.padding(10.dp), contentAlignment = Alignment.Center) {
                        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface)
                    }
                }
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(title, style = MaterialTheme.typography.titleMedium)
                    Text(subtitle, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                }
                Icon(Icons.Outlined.ArrowForwardIos, contentDescription = null, tint = TextSecondary)
            }
        }
    }
}

@Composable
private fun TrendMiniChart(trendReports: List<DailyReport>) {
    val values = trendReports.map { it.lateEntries.size + it.absenceEntries.size }
    val maxValue = values.maxOrNull()?.coerceAtLeast(1) ?: 1
    if (trendReports.isEmpty()) {
        PrimaryButton(
            label = "Aucune tendance disponible",
            onClick = {},
            enabled = false
        )
        return
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceSM),
        verticalAlignment = Alignment.Bottom
    ) {
        trendReports.forEach { report ->
            val value = report.lateEntries.size + report.absenceEntries.size
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(Dimens.SpaceSM)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height((24 + (72 * value / maxValue)).dp)
                ) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = if (value == maxValue) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(alpha = 0.55f),
                        shape = MaterialTheme.shapes.medium
                    ) {}
                }
                Text(report.date.takeLast(2), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold, color = TextSecondary)
            }
        }
    }
}
