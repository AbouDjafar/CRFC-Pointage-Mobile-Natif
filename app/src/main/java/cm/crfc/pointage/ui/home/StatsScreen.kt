package cm.crfc.pointage.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
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
import cm.crfc.pointage.ui.components.CrfcCard
import cm.crfc.pointage.ui.components.EmptyState
import cm.crfc.pointage.ui.components.HeaderCard
import cm.crfc.pointage.ui.components.MiniBar
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

    LazyColumn(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item {
            HeaderCard(
                title = "Statistiques",
                subtitle = "${filtered.count { it.status == ReportStatus.FINALIZED }} rapport(s) finalise(s) sur ${filtered.size}"
            )
        }
        item {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("7d" to "7 jours", "30d" to "30 jours", "90d" to "3 mois", "all" to "Tout").forEach { (key, label) ->
                    FilterChip(selected = period == key, onClick = { period = key }, label = { Text(label) })
                }
            }
        }
        item {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    StatCard("Retards", totalLate.toString(), extra.late)
                }
                Column(modifier = Modifier.weight(1f)) {
                    StatCard("Absences", totalAbsent.toString(), extra.absent)
                }
            }
        }
        item {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    StatCard("Visiteurs", totalVisitors.toString(), MaterialTheme.colorScheme.primary)
                }
                Column(modifier = Modifier.weight(1f)) {
                    StatCard("Jours", totalDays.toString(), extra.success)
                }
            }
        }
        item {
            CrfcCard(modifier = Modifier.padding(horizontal = 16.dp)) {
                SectionTitle("Moyennes")
                Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.padding(top = 12.dp)) {
                    Text("Retards par jour : $avgLate")
                    Text("Absences par jour : $avgAbsent")
                    Text("Retard moyen : $avgMinutes min")
                }
            }
        }
        if (filtered.isEmpty()) {
            item {
                CrfcCard(modifier = Modifier.padding(horizontal = 16.dp)) {
                    EmptyState("Aucune donnee", "Les statistiques apparaitront ici quand des rapports existeront.")
                }
            }
        } else {
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
private fun StatCard(title: String, value: String, color: androidx.compose.ui.graphics.Color, modifier: Modifier = Modifier) {
    CrfcCard(modifier = modifier) {
        Text(value, style = MaterialTheme.typography.displaySmall, color = color)
        Text(title, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
        Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.padding(top = 12.dp)) {
            data.forEach { item ->
                Column {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(item.name)
                        Text(item.count.toString(), color = color)
                    }
                    MiniBar(
                        value = if (data.maxOfOrNull { it.count } == 0) 0f else item.count.toFloat() / (data.maxOfOrNull { it.count } ?: 1).toFloat(),
                        color = color
                    )
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
