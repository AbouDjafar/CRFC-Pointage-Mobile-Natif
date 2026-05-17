package cm.crfc.pointage.ui.stats

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Description
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cm.crfc.pointage.data.ExportService
import cm.crfc.pointage.data.ReportRepository
import cm.crfc.pointage.model.AbsenceReason
import cm.crfc.pointage.model.DailyReport
import cm.crfc.pointage.model.Employee
import cm.crfc.pointage.model.ExportPayload
import cm.crfc.pointage.model.NamedCount
import cm.crfc.pointage.model.User
import cm.crfc.pointage.ui.components.EmployeeAvatar
import cm.crfc.pointage.ui.components.FilterChipRow
import cm.crfc.pointage.ui.components.SectionCard
import cm.crfc.pointage.ui.theme.Dimens
import cm.crfc.pointage.ui.theme.LocalCrfcUiExtras
import cm.crfc.pointage.ui.theme.TextSecondary
import cm.crfc.pointage.ui.theme.horizontalPadding
import cm.crfc.pointage.util.subtractDays
import cm.crfc.pointage.util.todayIso

@Composable
fun StatsScreen(
    user: User,
    reportRepository: ReportRepository,
    exportService: ExportService
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
    val filtered = reports.filter { threshold == null || it.date >= threshold }.sortedBy { it.date }
    val incidentsByDay = filtered.map { it.lateEntries.size + it.absenceEntries.size }
    val totalIncidents = incidentsByDay.sum()
    val totalVisitors = filtered.sumOf { it.visitorCount }
    val avgDelay = filtered.flatMap { it.lateEntries }.map { it.minutesLate }.average().let { if (it.isNaN()) 0 else it.toInt() }
    val reportRatio = "${filtered.size}/${reports.size.coerceAtLeast(1)}"
    val reasonShares = reasonBreakdown(filtered, reasons)
    val topLate = topLateWithAverage(filtered, employees)

    androidx.compose.foundation.lazy.LazyColumn(
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
                Text("STATISTIQUES & ANALYSE", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                Text("Vue d'ensemble", style = MaterialTheme.typography.headlineMedium)
                SectionCard {
                    FilterChipRow(
                        options = listOf("7 jours", "30 jours", "90 jours", "Tout"),
                        selectedOption = period,
                        onOptionSelected = { period = it },
                        activeColor = extras.orangeAccent
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceMD)) {
                    InsightTile("INCIDENTS", totalIncidents.toString(), if (totalIncidents > 0) "+${incidentsByDay.lastOrNull() ?: 0}" else "Stable", modifier = Modifier.weight(1f))
                    InsightTile("VISITEURS", totalVisitors.toString(), "Flux", modifier = Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceMD)) {
                    InsightTile("RETARD MOYEN", "$avgDelay min", "Moyenne", modifier = Modifier.weight(1f))
                    InsightTile("RAPPORTS", reportRatio, "Couverts", modifier = Modifier.weight(1f))
                }

                SectionCard {
                    Text("Tendance des incidents", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(Dimens.SpaceLG))
                    IncidentLineChart(values = incidentsByDay)
                    Spacer(modifier = Modifier.height(Dimens.SpaceSM))
                    Row(horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceSM), verticalAlignment = Alignment.CenterVertically) {
                        Surface(color = extras.orangeAccent, shape = CircleShape) { Box(modifier = Modifier.size(8.dp)) }
                        Text("Retards & Absences", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    }
                }

                SectionCard {
                    Text("Motifs d'absence", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(Dimens.SpaceLG))
                    Row(horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceLG), verticalAlignment = Alignment.CenterVertically) {
                        DonutChart(reasonShares = reasonShares)
                        Column(verticalArrangement = Arrangement.spacedBy(Dimens.SpaceSM)) {
                            reasonShares.forEachIndexed { index, entry ->
                                Row(horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceSM), verticalAlignment = Alignment.CenterVertically) {
                                    Surface(color = chartColors(extras)[index % chartColors(extras).size], shape = CircleShape) {
                                        Box(modifier = Modifier.size(10.dp))
                                    }
                                    Text("${entry.count}% ${entry.name}", style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                }

                SectionCard {
                    Text("Top retards (Frequence)", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(Dimens.SpaceLG))
                    if (topLate.isEmpty()) {
                        Text("Aucune donnee exploitable.", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(Dimens.SpaceLG)) {
                            topLate.forEachIndexed { index, item ->
                                Row(horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceMD), verticalAlignment = Alignment.CenterVertically) {
                                    Surface(color = extras.greenLight, shape = MaterialTheme.shapes.small) {
                                        Text(
                                            text = "${index + 1}",
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                            style = MaterialTheme.typography.labelLarge,
                                            color = extras.greenAccent
                                        )
                                    }
                                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                        Text(item.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                                        Text("Heure moy. ${item.averageArrival}", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                    }
                                    Text(item.count.toString(), style = MaterialTheme.typography.titleMedium, color = extras.orangeAccent)
                                }
                            }
                        }
                    }
                }

                SectionCard {
                    Surface(onClick = {
                        if (filtered.isNotEmpty()) {
                            exportService.exportExcel(
                                ExportPayload(
                                    reports = filtered,
                                    employees = employees,
                                    absenceReasons = reasons,
                                    author = user,
                                    periodStart = threshold ?: filtered.first().date,
                                    periodEnd = filtered.last().date.ifBlank { todayIso() }
                                )
                            )
                        }
                    }, color = MaterialTheme.colorScheme.surfaceVariant, shape = MaterialTheme.shapes.small) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            androidx.compose.material3.Icon(Icons.Outlined.Description, contentDescription = null)
                            Spacer(modifier = Modifier.size(8.dp))
                            Text("Generer la synthese Excel", style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
            }
        }
        item { Spacer(modifier = Modifier.navigationBarsPadding()) }
    }
}

private data class LateFrequencyItem(
    val name: String,
    val count: Int,
    val averageArrival: String
)

@Composable
private fun InsightTile(
    title: String,
    value: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    SectionCard(modifier = modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(Dimens.SpaceSM)) {
            Text(title, style = MaterialTheme.typography.labelMedium, color = TextSecondary)
            Text(value, style = MaterialTheme.typography.headlineMedium)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
        }
    }
}

@Composable
private fun IncidentLineChart(values: List<Int>) {
    val safeValues = if (values.isEmpty()) listOf(0, 0, 0, 0, 0, 0, 0) else values.takeLast(7).let { list ->
        if (list.size >= 7) list else List(7 - list.size) { 0 } + list
    }
    val max = safeValues.maxOrNull()?.coerceAtLeast(1) ?: 1
    Canvas(modifier = Modifier.fillMaxWidth().height(160.dp)) {
        val stepX = size.width / (safeValues.size - 1).coerceAtLeast(1)
        val points = safeValues.mapIndexed { index, value ->
            val x = index * stepX
            val y = size.height - ((value.toFloat() / max.toFloat()) * (size.height - 24.dp.toPx())) - 12.dp.toPx()
            Offset(x, y)
        }
        points.zipWithNext().forEach { (start, end) ->
            drawLine(
                color = androidx.compose.ui.graphics.Color(0xFFD28A1C),
                start = start,
                end = end,
                strokeWidth = 4.dp.toPx(),
                cap = StrokeCap.Round
            )
        }
        points.forEach { point ->
            drawCircle(color = androidx.compose.ui.graphics.Color(0xFFD28A1C), radius = 5.dp.toPx(), center = point)
        }
    }
}

@Composable
private fun DonutChart(reasonShares: List<NamedCount>) {
    val colors = chartColors(LocalCrfcUiExtras.current)
    val shares = if (reasonShares.isEmpty()) listOf(NamedCount("none", "Aucune donnee", 100)) else reasonShares
    Canvas(modifier = Modifier.size(112.dp)) {
        var startAngle = -90f
        shares.forEachIndexed { index, item ->
            val sweep = 360f * (item.count / 100f)
            drawArc(
                color = colors[index % colors.size],
                startAngle = startAngle,
                sweepAngle = sweep,
                useCenter = false,
                topLeft = Offset.Zero,
                size = Size(size.width, size.height),
                style = Stroke(width = 18.dp.toPx(), cap = StrokeCap.Butt)
            )
            startAngle += sweep
        }
    }
}

private fun chartColors(extras: cm.crfc.pointage.ui.theme.CrfcUiExtras) = listOf(
    extras.navyDark,
    extras.navyLight,
    extras.orangeAccent,
    extras.greenAccent
)

private fun reasonBreakdown(reports: List<DailyReport>, reasons: List<AbsenceReason>): List<NamedCount> {
    val total = reports.sumOf { it.absenceEntries.size }
    if (total == 0) return emptyList()
    val counts = mutableMapOf<String, Int>()
    reports.forEach { report ->
        report.absenceEntries.forEach { entry ->
            counts[entry.reasonId] = (counts[entry.reasonId] ?: 0) + 1
        }
    }
    return counts.entries.sortedByDescending { it.value }.take(4).map { entry ->
        val percent = ((entry.value.toFloat() / total.toFloat()) * 100f).toInt().coerceAtLeast(1)
        NamedCount(entry.key, reasons.firstOrNull { it.id == entry.key }?.label ?: "Autre", percent)
    }
}

private fun topLateWithAverage(reports: List<DailyReport>, employees: List<Employee>): List<LateFrequencyItem> {
    val counts = mutableMapOf<String, MutableList<String>>()
    reports.forEach { report ->
        report.lateEntries.forEach { entry ->
            counts.getOrPut(entry.employeeId) { mutableListOf() }.add(entry.arrivalTime)
        }
    }
    return counts.entries.sortedByDescending { it.value.size }.take(5).map { entry ->
        LateFrequencyItem(
            name = employees.firstOrNull { it.id == entry.key }?.fullName ?: "Inconnu",
            count = entry.value.size,
            averageArrival = averageTime(entry.value)
        )
    }
}

private fun averageTime(values: List<String>): String {
    if (values.isEmpty()) return "--:--"
    val totalMinutes = values.mapNotNull {
        val parts = it.split(":")
        if (parts.size != 2) null else parts[0].toIntOrNull()?.times(60)?.plus(parts[1].toIntOrNull() ?: 0)
    }.sum()
    val avg = totalMinutes / values.size
    val hours = (avg / 60).toString().padStart(2, '0')
    val minutes = (avg % 60).toString().padStart(2, '0')
    return "$hours:$minutes"
}
