package cm.crfc.pointage.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Group
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cm.crfc.pointage.data.ReportRepository
import cm.crfc.pointage.model.AbsenceReason
import cm.crfc.pointage.model.DailyReport
import cm.crfc.pointage.model.Employee
import cm.crfc.pointage.model.NamedCount
import cm.crfc.pointage.model.ReportStatus
import cm.crfc.pointage.model.User
import cm.crfc.pointage.ui.components.AvatarCircle
import cm.crfc.pointage.ui.components.CrfcCard
import cm.crfc.pointage.ui.components.EmptyState
import cm.crfc.pointage.ui.components.HeaderCard
import cm.crfc.pointage.ui.components.MiniBar
import cm.crfc.pointage.ui.components.ScreenList
import cm.crfc.pointage.ui.components.SectionLabel
import cm.crfc.pointage.ui.components.SectionTitle
import cm.crfc.pointage.ui.theme.LocalCrfcExtraColors
import cm.crfc.pointage.util.subtractDays

@Composable
fun StatsScreen(
    user: User,
    reportRepository: ReportRepository
) {
    val extra = LocalCrfcExtraColors.current
    val reports by reportRepository.observeReportsFor(user).collectAsStateWithLifecycle(initialValue = emptyList())
    val employees by reportRepository.observeEmployees().collectAsStateWithLifecycle(initialValue = emptyList())
    val reasons by reportRepository.observeAbsenceReasons().collectAsStateWithLifecycle(initialValue = emptyList())
    var period by remember { mutableStateOf("30d") }

    val threshold = when (period) {
        "7d" -> subtractDays(7)
        "30d" -> subtractDays(30)
        "90d" -> subtractDays(90)
        else -> null
    }
    val filtered = reports.filter { threshold == null || it.date >= threshold }
    val totalLate = filtered.sumOf { it.lateEntries.size }
    val totalAbsent = filtered.sumOf { it.absenceEntries.size }
    val totalVisitors = filtered.sumOf { it.visitorCount }
    val totalDays = filtered.size
    val totalLateMinutes = filtered.sumOf { report -> report.lateEntries.sumOf { it.minutesLate } }
    val avgLate = if (totalDays == 0) "0.0" else String.format("%.1f", totalLate.toDouble() / totalDays.toDouble())
    val avgAbsent = if (totalDays == 0) "0.0" else String.format("%.1f", totalAbsent.toDouble() / totalDays.toDouble())
    val avgMinutes = if (totalLate == 0) 0 else totalLateMinutes / totalLate

    ScreenList {
        item {
            HeaderCard(
                title = "Statistiques",
                subtitle = "${filtered.count { it.status == ReportStatus.FINALIZED }} rapport(s) finalises sur ${filtered.size}"
            )
        }

        item {
            CrfcCard(modifier = Modifier.padding(horizontal = 16.dp)) {
                SectionLabel("Periode d'analyse")
                Row(
                    modifier = Modifier.padding(top = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("7d" to "7 jours", "30d" to "30 jours", "90d" to "3 mois", "all" to "Tout").forEach { (key, label) ->
                        FilterChip(selected = period == key, onClick = { period = key }, label = { Text(label) })
                    }
                }
            }
        }

        if (filtered.isEmpty()) {
            item {
                CrfcCard(modifier = Modifier.padding(horizontal = 16.dp)) {
                    EmptyState("Aucune donnee", "Les statistiques apparaitront ici des que des rapports seront disponibles.")
                }
            }
        } else {
            item {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard(
                        title = "Retards",
                        value = totalLate.toString(),
                        caption = "$avgLate / jour",
                        color = extra.late,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Absences",
                        value = totalAbsent.toString(),
                        caption = "$avgAbsent / jour",
                        color = extra.absent,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard(
                        title = "Visiteurs",
                        value = totalVisitors.toString(),
                        caption = "Cumul observe",
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Retard moyen",
                        value = "$avgMinutes",
                        caption = "minutes",
                        color = extra.success,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                CrfcCard(modifier = Modifier.padding(horizontal = 16.dp)) {
                    SectionTitle("Vue d'ensemble")
                    Column(
                        modifier = Modifier.padding(top = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OverviewRow("Jours analyses", totalDays.toString())
                        OverviewRow("Rapports finalises", filtered.count { it.status == ReportStatus.FINALIZED }.toString())
                        OverviewRow("Visiteurs cumules", totalVisitors.toString())
                        OverviewRow("Retards cumules", totalLate.toString())
                    }
                }
            }

            item {
                RankingCard(
                    title = "Top retardataires",
                    data = rankEmployees(filtered, employees, late = true),
                    color = extra.late,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            item {
                RankingCard(
                    title = "Top absents",
                    data = rankEmployees(filtered, employees, late = false),
                    color = extra.absent,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            item {
                RankingCard(
                    title = "Motifs d'absence",
                    data = rankReasons(filtered, reasons),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    caption: String,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    CrfcCard(modifier = modifier) {
        Text(value, style = MaterialTheme.typography.headlineLarge, color = color)
        Text(title, style = MaterialTheme.typography.titleMedium)
        Text(caption, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun OverviewRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
private fun RankingCard(
    title: String,
    data: List<NamedCount>,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    CrfcCard(modifier = modifier) {
        SectionTitle(title)
        if (data.isEmpty()) {
            EmptyState("Aucune entree", "Les valeurs apparaitront ici quand des donnees seront disponibles.")
        } else {
            Column(
                modifier = Modifier.padding(top = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                data.forEach { item ->
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            AvatarCircle(text = item.name, color = color)
                            Column(modifier = Modifier.weight(1f)) {
                                Text(item.name, style = MaterialTheme.typography.bodyLarge)
                                Text("${item.count} occurrence(s)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Text(item.count.toString(), color = color, style = MaterialTheme.typography.titleMedium)
                        }
                        MiniBar(
                            value = if (data.maxOfOrNull { it.count } == 0) {
                                0f
                            } else {
                                item.count.toFloat() / (data.maxOfOrNull { it.count } ?: 1).toFloat()
                            },
                            color = color
                        )
                    }
                }
            }
        }
    }
}

private fun rankEmployees(reports: List<DailyReport>, employees: List<Employee>, late: Boolean): List<NamedCount> {
    val counts = mutableMapOf<String, Int>()
    reports.forEach { report ->
        if (late) {
            report.lateEntries.forEach { counts[it.employeeId] = (counts[it.employeeId] ?: 0) + 1 }
        } else {
            report.absenceEntries.forEach { counts[it.employeeId] = (counts[it.employeeId] ?: 0) + 1 }
        }
    }
    return counts.entries.sortedByDescending { it.value }.take(5).map { entry ->
        NamedCount(entry.key, employees.firstOrNull { it.id == entry.key }?.fullName ?: "Inconnu", entry.value)
    }
}

private fun rankReasons(reports: List<DailyReport>, reasons: List<AbsenceReason>): List<NamedCount> {
    val counts = mutableMapOf<String, Int>()
    reports.forEach { report ->
        report.absenceEntries.forEach { counts[it.reasonId] = (counts[it.reasonId] ?: 0) + 1 }
    }
    return counts.entries.sortedByDescending { it.value }.take(5).map { entry ->
        NamedCount(entry.key, reasons.firstOrNull { it.id == entry.key }?.label ?: "Inconnu", entry.value)
    }
}
