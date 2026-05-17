package cm.crfc.pointage.ui.stats

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cm.crfc.pointage.data.ReportRepository
import cm.crfc.pointage.model.AbsenceReason
import cm.crfc.pointage.model.DailyReport
import cm.crfc.pointage.model.Employee
import cm.crfc.pointage.model.NamedCount
import cm.crfc.pointage.model.ReportStatus
import cm.crfc.pointage.model.User
import cm.crfc.pointage.ui.components.AppHeader
import cm.crfc.pointage.ui.components.EmployeeAvatar
import cm.crfc.pointage.ui.components.EmptyState
import cm.crfc.pointage.ui.components.FilterChipRow
import cm.crfc.pointage.ui.components.SectionCard
import cm.crfc.pointage.ui.components.StatChip
import cm.crfc.pointage.ui.theme.Dimens
import cm.crfc.pointage.ui.theme.LocalCrfcUiExtras
import cm.crfc.pointage.ui.theme.TextSecondary
import cm.crfc.pointage.ui.theme.horizontalPadding
import cm.crfc.pointage.util.subtractDays

@Composable
fun StatsScreen(
    user: User,
    reportRepository: ReportRepository
) {
    val extras = LocalCrfcUiExtras.current
    val horizontalPadding = horizontalPadding()
    val reports by reportRepository.observeReportsFor(user).collectAsStateWithLifecycle(initialValue = emptyList())
    val employees by reportRepository.observeEmployees().collectAsStateWithLifecycle(initialValue = emptyList())
    val reasons by reportRepository.observeAbsenceReasons().collectAsStateWithLifecycle(initialValue = emptyList())

    var period by remember { mutableStateOf("30 jours") }
    val threshold = when (period) {
        "7 jours" -> subtractDays(7)
        "30 jours" -> subtractDays(30)
        "90 jours" -> subtractDays(90)
        else -> null
    }
    val filtered = reports.filter { threshold == null || it.date >= threshold }
    val totalLate = filtered.sumOf { it.lateEntries.size }
    val totalAbsent = filtered.sumOf { it.absenceEntries.size }
    val totalVisitors = filtered.sumOf { it.visitorCount }
    val totalLateMinutes = filtered.sumOf { report -> report.lateEntries.sumOf { it.minutesLate } }
    val avgLate = if (filtered.isEmpty()) "0.0" else String.format("%.1f", totalLate.toDouble() / filtered.size)
    val avgAbsent = if (filtered.isEmpty()) "0.0" else String.format("%.1f", totalAbsent.toDouble() / filtered.size)
    val avgMinutes = if (totalLate == 0) 0 else totalLateMinutes / totalLate

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(Dimens.SpaceLG)
    ) {
        item {
            AppHeader(
                title = "Statistiques",
                subtitle = "${filtered.count { it.status == ReportStatus.FINALIZED }} rapport(s) finalises",
                bottomContent = {
                    Text(
                        text = "Lecture synthese des retards, absences et visiteurs sur la periode.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.82f)
                    )
                }
            )
        }
        item {
            SectionCard(modifier = Modifier.padding(horizontal = horizontalPadding)) {
                Text("Periode d'analyse", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(Dimens.SpaceSM))
                FilterChipRow(
                    options = listOf("7 jours", "30 jours", "90 jours", "Tout"),
                    selectedOption = period,
                    onOptionSelected = { period = it },
                    activeColor = extras.orangeAccent
                )
            }
        }

        if (filtered.isEmpty()) {
            item {
                SectionCard(modifier = Modifier.padding(horizontal = horizontalPadding)) {
                    EmptyState(
                        icon = Icons.Outlined.BarChart,
                        title = "Aucune donnee",
                        subtitle = "Les statistiques apparaitront ici des qu'au moins un rapport sera disponible."
                    )
                }
            }
        } else {
            item {
                Row(
                    modifier = Modifier.padding(horizontal = horizontalPadding),
                    horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceSM)
                ) {
                    KpiCard("Retards", totalLate.toString(), "$avgLate / jour", extras.orangeAccent, Modifier.weight(1f))
                    KpiCard("Absences", totalAbsent.toString(), "$avgAbsent / jour", extras.purpleAccent, Modifier.weight(1f))
                }
            }
            item {
                Row(
                    modifier = Modifier.padding(horizontal = horizontalPadding),
                    horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceSM)
                ) {
                    KpiCard("Visiteurs", totalVisitors.toString(), "cumul", MaterialTheme.colorScheme.primary, Modifier.weight(1f))
                    KpiCard("Retard moyen", "$avgMinutes min", "par retard", extras.greenAccent, Modifier.weight(1f))
                }
            }
            item {
                SectionCard(modifier = Modifier.padding(horizontal = horizontalPadding), highlighted = true) {
                    Text("Retard cumule", style = MaterialTheme.typography.titleLarge)
                    Text("$totalLateMinutes minutes au total", style = MaterialTheme.typography.headlineMedium, color = extras.orangeAccent)
                    Spacer(modifier = Modifier.height(Dimens.SpaceLG))
                    MiniBars(filtered)
                }
            }
            item {
                RankingCard(
                    modifier = Modifier.padding(horizontal = horizontalPadding),
                    title = "Top retardataires",
                    data = rankEmployees(filtered, employees, true),
                    chipColor = extras.orangeAccent
                )
            }
            item {
                RankingCard(
                    modifier = Modifier.padding(horizontal = horizontalPadding),
                    title = "Top absents",
                    data = rankEmployees(filtered, employees, false),
                    chipColor = extras.purpleAccent
                )
            }
            item {
                RankingCard(
                    modifier = Modifier.padding(horizontal = horizontalPadding),
                    title = "Motifs d'absence",
                    data = rankReasons(filtered, reasons),
                    chipColor = MaterialTheme.colorScheme.primary
                )
            }
        }

        item { Spacer(modifier = Modifier.navigationBarsPadding()) }
    }
}

@Composable
private fun KpiCard(title: String, value: String, subtitle: String, color: Color, modifier: Modifier = Modifier) {
    SectionCard(modifier = modifier) {
        Text(value, style = MaterialTheme.typography.headlineMedium, color = color)
        Text(title, style = MaterialTheme.typography.titleMedium)
        Text(subtitle, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
    }
}

@Composable
private fun MiniBars(reports: List<DailyReport>) {
    val recent = reports.sortedByDescending { it.date }.take(6).reversed()
    val max = recent.maxOfOrNull { it.lateEntries.size + it.absenceEntries.size }?.coerceAtLeast(1) ?: 1
    Row(horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceSM)) {
        recent.forEach { report ->
            val value = report.lateEntries.size + report.absenceEntries.size
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(Dimens.SpaceSM)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height((40 + (120 * value / max)).dp)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f), RoundedCornerShape(14.dp))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height((30 + (90 * value / max)).dp)
                            .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(14.dp))
                    )
                }
                Text(report.date.takeLast(5), style = MaterialTheme.typography.labelSmall, color = TextSecondary)
            }
        }
    }
}

@Composable
private fun RankingCard(title: String, data: List<NamedCount>, chipColor: Color, modifier: Modifier = Modifier) {
    SectionCard(modifier = modifier) {
        Text(title, style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(Dimens.SpaceLG))
        if (data.isEmpty()) {
            EmptyState(Icons.Outlined.BarChart, "Aucune entree", "Les informations apparaitront ici au fil des rapports.")
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(Dimens.SpaceMD)) {
                data.forEach { item ->
                    Row(horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceMD)) {
                        EmployeeAvatar(item.name)
                        Column(modifier = Modifier.weight(1f)) {
                            Text(item.name, style = MaterialTheme.typography.titleMedium)
                            StatChip(
                                icon = if (title == "Motifs d'absence") Icons.Outlined.WarningAmber else Icons.Outlined.Schedule,
                                text = "${item.count} occurrence(s)",
                                containerColor = chipColor.copy(alpha = 0.1f),
                                contentColor = chipColor,
                                compact = true
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun rankEmployees(reports: List<DailyReport>, employees: List<Employee>, late: Boolean): List<NamedCount> {
    val counts = mutableMapOf<String, Int>()
    reports.forEach { report ->
        if (late) report.lateEntries.forEach { counts[it.employeeId] = (counts[it.employeeId] ?: 0) + 1 }
        else report.absenceEntries.forEach { counts[it.employeeId] = (counts[it.employeeId] ?: 0) + 1 }
    }
    return counts.entries.sortedByDescending { it.value }.take(5).map {
        NamedCount(it.key, employees.firstOrNull { employee -> employee.id == it.key }?.fullName ?: "Inconnu", it.value)
    }
}

private fun rankReasons(reports: List<DailyReport>, reasons: List<AbsenceReason>): List<NamedCount> {
    val counts = mutableMapOf<String, Int>()
    reports.forEach { report ->
        report.absenceEntries.forEach { counts[it.reasonId] = (counts[it.reasonId] ?: 0) + 1 }
    }
    return counts.entries.sortedByDescending { it.value }.take(5).map {
        NamedCount(it.key, reasons.firstOrNull { reason -> reason.id == it.key }?.label ?: "Inconnu", it.value)
    }
}
