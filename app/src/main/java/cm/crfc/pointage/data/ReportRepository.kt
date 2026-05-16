package cm.crfc.pointage.data

import cm.crfc.pointage.data.local.AbsenceEntryDao
import cm.crfc.pointage.data.local.AbsenceEntryEntity
import cm.crfc.pointage.data.local.AbsenceReasonDao
import cm.crfc.pointage.data.local.EmployeeDao
import cm.crfc.pointage.data.local.EmployeeEntity
import cm.crfc.pointage.data.local.LateEntryDao
import cm.crfc.pointage.data.local.LateEntryEntity
import cm.crfc.pointage.data.local.RecurringAbsenceDao
import cm.crfc.pointage.data.local.RecurringAbsenceEntity
import cm.crfc.pointage.data.local.ReportDao
import cm.crfc.pointage.model.AbsenceReason
import cm.crfc.pointage.model.DailyReport
import cm.crfc.pointage.model.Employee
import cm.crfc.pointage.model.LateEntry
import cm.crfc.pointage.model.NamedCount
import cm.crfc.pointage.model.RecurringAbsence
import cm.crfc.pointage.model.ReportStatus
import cm.crfc.pointage.model.StatisticsSnapshot
import cm.crfc.pointage.model.User
import cm.crfc.pointage.model.UserRole
import cm.crfc.pointage.util.calcMinutesLate
import cm.crfc.pointage.util.genId
import cm.crfc.pointage.util.generateIntroText
import cm.crfc.pointage.util.nowIso
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

class ReportRepository(
    private val employeeDao: EmployeeDao,
    private val absenceReasonDao: AbsenceReasonDao,
    private val recurringAbsenceDao: RecurringAbsenceDao,
    private val reportDao: ReportDao,
    private val lateEntryDao: LateEntryDao,
    private val absenceEntryDao: AbsenceEntryDao
) {
    fun observeEmployees(): Flow<List<Employee>> =
        employeeDao.observeAll().map { items -> items.map(EmployeeEntity::toDomain) }

    fun observeAbsenceReasons(): Flow<List<AbsenceReason>> =
        absenceReasonDao.observeAll().map { items -> items.map { it.toDomain() } }

    fun observeRecurringAbsences(): Flow<List<RecurringAbsence>> =
        recurringAbsenceDao.observeAll().map { items -> items.map { it.toDomain() } }

    fun observeReportsFor(user: User): Flow<List<DailyReport>> =
        reportDao.observeBundles().map { bundles ->
            val reports = bundles.map { it.toDomain() }
            if (user.role == UserRole.ADMIN) reports else reports.filter { it.createdBy == user.id }
        }

    fun observeReportById(id: String): Flow<DailyReport?> =
        reportDao.observeBundleById(id).map { it?.toDomain() }

    suspend fun getEmployees(): List<Employee> = employeeDao.getAll().map { it.toDomain() }

    suspend fun getAbsenceReasons(): List<AbsenceReason> = absenceReasonDao.getAll().map { it.toDomain() }

    suspend fun getReportsFor(user: User): List<DailyReport> {
        val reports = reportDao.getBundles().map { it.toDomain() }
        return if (user.role == UserRole.ADMIN) reports else reports.filter { it.createdBy == user.id }
    }

    suspend fun getReportByDate(user: User, date: String): DailyReport? {
        val candidates = reportDao.getByDate(date)
        val reportEntity = if (user.role == UserRole.ADMIN) {
            candidates.firstOrNull()
        } else {
            candidates.firstOrNull { it.createdBy == user.id }
        } ?: return null
        return reportDao.getBundles().firstOrNull { it.report.id == reportEntity.id }?.toDomain()
    }

    suspend fun addEmployee(firstName: String, lastName: String) {
        val fn = firstName.trim()
        val ln = lastName.trim()
        employeeDao.insert(
            EmployeeEntity(
                id = genId(),
                fullName = listOf(ln, fn).filter { it.isNotBlank() }.joinToString(" ").trim(),
                firstName = fn,
                lastName = ln,
                isActive = true,
                needsReview = false,
                importSource = "manual",
                importedAt = nowIso().substringBefore("T"),
                createdAt = nowIso()
            )
        )
    }

    suspend fun updateEmployee(employee: Employee) {
        employeeDao.insert(
            EmployeeEntity(
                id = employee.id,
                fullName = employee.fullName,
                firstName = employee.firstName,
                lastName = employee.lastName,
                isActive = employee.isActive,
                needsReview = employee.needsReview,
                importSource = employee.importSource,
                importedAt = employee.importedAt,
                createdAt = employee.createdAt
            )
        )
    }

    suspend fun toggleEmployeeActive(id: String) {
        val employee = employeeDao.getAll().firstOrNull { it.id == id } ?: return
        employeeDao.insert(employee.copy(isActive = !employee.isActive))
    }

    suspend fun createOrUpdateTodayReport(user: User, date: String): DailyReport {
        val existing = getReportByDate(user, date)
        if (existing != null) {
            val updated = existing.copy(updatedAt = nowIso())
            reportDao.insert(updated.toEntity())
            return updated
        }

        val reportId = genId()
        val recurring = recurringAbsenceDao.getAll()
        val createdAt = nowIso()
        val newReport = DailyReport(
            id = reportId,
            date = date,
            status = ReportStatus.DRAFT,
            lateEntries = emptyList(),
            absenceEntries = recurring.map {
                cm.crfc.pointage.model.AbsenceEntry(
                    id = genId(),
                    reportId = reportId,
                    employeeId = it.employeeId,
                    reasonId = it.reasonId,
                    comment = it.comment
                )
            },
            visitorCount = 0,
            introText = generateIntroText(date),
            createdBy = user.id,
            createdAt = createdAt,
            updatedAt = createdAt
        )
        reportDao.insert(newReport.toEntity())
        newReport.absenceEntries.forEach { entry ->
            absenceEntryDao.insert(
                AbsenceEntryEntity(
                    id = entry.id,
                    reportId = reportId,
                    employeeId = entry.employeeId,
                    reasonId = entry.reasonId,
                    comment = entry.comment
                )
            )
        }
        return newReport
    }

    suspend fun addLateEntry(report: DailyReport, employeeId: String, arrivalTime: String, note: String? = null) {
        lateEntryDao.insert(
            LateEntryEntity(
                id = genId(),
                reportId = report.id,
                employeeId = employeeId,
                arrivalTime = arrivalTime,
                minutesLate = calcMinutesLate(arrivalTime),
                note = note?.takeIf { it.isNotBlank() }
            )
        )
        reportDao.insert(report.copy(updatedAt = nowIso()).toEntity())
    }

    suspend fun removeLateEntry(report: DailyReport, entryId: String) {
        lateEntryDao.deleteById(entryId)
        reportDao.insert(report.copy(updatedAt = nowIso()).toEntity())
    }

    suspend fun addAbsenceEntry(report: DailyReport, employeeId: String, reasonId: String, comment: String?) {
        absenceEntryDao.insert(
            AbsenceEntryEntity(
                id = genId(),
                reportId = report.id,
                employeeId = employeeId,
                reasonId = reasonId,
                comment = comment?.takeIf { it.isNotBlank() }
            )
        )
        reportDao.insert(report.copy(updatedAt = nowIso()).toEntity())
    }

    suspend fun removeAbsenceEntry(report: DailyReport, entryId: String) {
        absenceEntryDao.deleteById(entryId)
        reportDao.insert(report.copy(updatedAt = nowIso()).toEntity())
    }

    suspend fun setVisitorCount(report: DailyReport, count: Int) {
        reportDao.insert(report.copy(visitorCount = count.coerceAtLeast(0), updatedAt = nowIso()).toEntity())
    }

    suspend fun finalizeReport(report: DailyReport) {
        reportDao.insert(report.copy(status = ReportStatus.FINALIZED, updatedAt = nowIso()).toEntity())
    }

    suspend fun reopenReport(report: DailyReport) {
        reportDao.insert(report.copy(status = ReportStatus.DRAFT, updatedAt = nowIso()).toEntity())
    }

    suspend fun deleteReport(reportId: String) {
        reportDao.deleteById(reportId)
    }

    suspend fun setRecurringAbsence(employeeId: String, reasonId: String, comment: String?) {
        val existing = recurringAbsenceDao.getByEmployeeId(employeeId)
        recurringAbsenceDao.insert(
            RecurringAbsenceEntity(
                id = existing?.id ?: genId(),
                employeeId = employeeId,
                reasonId = reasonId,
                comment = comment?.takeIf { it.isNotBlank() }
            )
        )
    }

    suspend fun removeRecurringAbsence(employeeId: String) {
        recurringAbsenceDao.deleteByEmployeeId(employeeId)
    }

    fun observeStatistics(user: User): Flow<StatisticsSnapshot> =
        combine(observeReportsFor(user), observeEmployees(), observeAbsenceReasons()) { reports, employees, reasons ->
            buildStatistics(reports, employees, reasons)
        }

    private fun buildStatistics(
        reports: List<DailyReport>,
        employees: List<Employee>,
        reasons: List<AbsenceReason>
    ): StatisticsSnapshot {
        val totalLate = reports.sumOf { it.lateEntries.size }
        val totalAbsent = reports.sumOf { it.absenceEntries.size }
        val totalVisitors = reports.sumOf { it.visitorCount }
        val totalDays = reports.size
        val totalLateMinutes = reports.sumOf { report -> report.lateEntries.sumOf(LateEntry::minutesLate) }
        val finalizedCount = reports.count { it.status == ReportStatus.FINALIZED }

        val lateByEmployee = mutableMapOf<String, Int>()
        val absentByEmployee = mutableMapOf<String, Int>()
        val byReason = mutableMapOf<String, Int>()

        reports.forEach { report ->
            report.lateEntries.forEach { late -> lateByEmployee[late.employeeId] = (lateByEmployee[late.employeeId] ?: 0) + 1 }
            report.absenceEntries.forEach { absence ->
                absentByEmployee[absence.employeeId] = (absentByEmployee[absence.employeeId] ?: 0) + 1
                byReason[absence.reasonId] = (byReason[absence.reasonId] ?: 0) + 1
            }
        }

        fun employeeName(id: String) = employees.firstOrNull { it.id == id }?.fullName ?: "Inconnu"
        fun reasonLabel(id: String) = reasons.firstOrNull { it.id == id }?.label ?: "Inconnu"
        fun top(source: Map<String, Int>, resolver: (String) -> String) =
            source.entries.sortedByDescending { it.value }.take(5).map { NamedCount(it.key, resolver(it.key), it.value) }

        return StatisticsSnapshot(
            totalLate = totalLate,
            totalAbsent = totalAbsent,
            totalVisitors = totalVisitors,
            totalDays = totalDays,
            totalLateMinutes = totalLateMinutes,
            finalizedCount = finalizedCount,
            topLate = top(lateByEmployee, ::employeeName),
            topAbsent = top(absentByEmployee, ::employeeName),
            topReasons = top(byReason, ::reasonLabel),
            recentReports = reports.sortedByDescending { it.date }.take(7).reversed(),
            averageLatePerDay = if (totalDays == 0) "0.0" else String.format("%.1f", totalLate.toDouble() / totalDays.toDouble()),
            averageAbsentPerDay = if (totalDays == 0) "0.0" else String.format("%.1f", totalAbsent.toDouble() / totalDays.toDouble()),
            averageMinutesLate = if (totalLate == 0) 0 else totalLateMinutes / totalLate
        )
    }
}

