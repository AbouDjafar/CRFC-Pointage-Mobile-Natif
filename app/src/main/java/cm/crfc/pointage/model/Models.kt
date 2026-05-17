package cm.crfc.pointage.model

enum class UserRole { ADMIN, AGENT }

enum class ReportStatus { DRAFT, FINALIZED }

enum class ExportFileType { PDF, EXCEL }

data class User(
    val id: String,
    val firstName: String,
    val lastName: String,
    val email: String,
    val jobTitle: String,
    val password: String,
    val role: UserRole,
    val isActive: Boolean,
    val createdAt: String,
    val createdBy: String? = null
) {
    val fullName: String
        get() = listOf(firstName, lastName).filter { it.isNotBlank() }.joinToString(" ").trim()
}

data class Employee(
    val id: String,
    val fullName: String,
    val firstName: String,
    val lastName: String,
    val jobTitle: String,
    val department: String,
    val isActive: Boolean,
    val needsReview: Boolean,
    val importSource: String,
    val importedAt: String,
    val createdAt: String
)

data class AbsenceReason(
    val id: String,
    val label: String
)

data class RecurringAbsence(
    val id: String,
    val employeeId: String,
    val reasonId: String,
    val comment: String? = null
)

data class LateEntry(
    val id: String,
    val reportId: String,
    val employeeId: String,
    val arrivalTime: String,
    val minutesLate: Int,
    val note: String? = null
)

data class AbsenceEntry(
    val id: String,
    val reportId: String,
    val employeeId: String,
    val reasonId: String,
    val comment: String? = null
)

data class DailyReport(
    val id: String,
    val date: String,
    val status: ReportStatus,
    val lateEntries: List<LateEntry>,
    val absenceEntries: List<AbsenceEntry>,
    val visitorCount: Int,
    val introText: String,
    val createdBy: String,
    val createdAt: String,
    val updatedAt: String
)

data class ExportPayload(
    val reports: List<DailyReport>,
    val employees: List<Employee>,
    val absenceReasons: List<AbsenceReason>,
    val author: User,
    val periodStart: String,
    val periodEnd: String
)

data class ExportFileRecord(
    val id: String,
    val type: ExportFileType,
    val fileName: String,
    val filePath: String,
    val sizeBytes: Long,
    val createdAt: String,
    val reportDate: String? = null,
    val periodStart: String? = null,
    val periodEnd: String? = null
)

data class StatisticsSnapshot(
    val totalLate: Int,
    val totalAbsent: Int,
    val totalVisitors: Int,
    val totalDays: Int,
    val totalLateMinutes: Int,
    val finalizedCount: Int,
    val topLate: List<NamedCount>,
    val topAbsent: List<NamedCount>,
    val topReasons: List<NamedCount>,
    val recentReports: List<DailyReport>,
    val averageLatePerDay: String,
    val averageAbsentPerDay: String,
    val averageMinutesLate: Int
)

data class NamedCount(
    val id: String,
    val name: String,
    val count: Int
)

data class OperationResult(
    val success: Boolean,
    val error: String? = null
)
