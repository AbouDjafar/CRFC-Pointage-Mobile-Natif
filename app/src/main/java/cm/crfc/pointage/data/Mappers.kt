package cm.crfc.pointage.data

import cm.crfc.pointage.data.local.AbsenceEntryEntity
import cm.crfc.pointage.data.local.AbsenceReasonEntity
import cm.crfc.pointage.data.local.EmployeeEntity
import cm.crfc.pointage.data.local.LateEntryEntity
import cm.crfc.pointage.data.local.RecurringAbsenceEntity
import cm.crfc.pointage.data.local.ReportBundleEntity
import cm.crfc.pointage.data.local.ReportEntity
import cm.crfc.pointage.data.local.UserEntity
import cm.crfc.pointage.model.AbsenceEntry
import cm.crfc.pointage.model.AbsenceReason
import cm.crfc.pointage.model.DailyReport
import cm.crfc.pointage.model.Employee
import cm.crfc.pointage.model.LateEntry
import cm.crfc.pointage.model.RecurringAbsence
import cm.crfc.pointage.model.ReportStatus
import cm.crfc.pointage.model.User
import cm.crfc.pointage.model.UserRole

fun UserEntity.toDomain() = User(
    id = id,
    firstName = firstName,
    lastName = lastName,
    email = email,
    jobTitle = jobTitle,
    password = password,
    role = UserRole.valueOf(role),
    isActive = isActive,
    createdAt = createdAt,
    createdBy = createdBy
)

fun User.toEntity() = UserEntity(
    id = id,
    firstName = firstName,
    lastName = lastName,
    email = email,
    jobTitle = jobTitle,
    password = password,
    role = role.name,
    isActive = isActive,
    createdAt = createdAt,
    createdBy = createdBy
)

fun EmployeeEntity.toDomain() = Employee(
    id = id,
    fullName = fullName,
    firstName = firstName,
    lastName = lastName,
    isActive = isActive,
    needsReview = needsReview,
    importSource = importSource,
    importedAt = importedAt,
    createdAt = createdAt
)

fun AbsenceReasonEntity.toDomain() = AbsenceReason(id = id, label = label)

fun RecurringAbsenceEntity.toDomain() = RecurringAbsence(
    id = id,
    employeeId = employeeId,
    reasonId = reasonId,
    comment = comment
)

fun LateEntryEntity.toDomain() = LateEntry(
    id = id,
    reportId = reportId,
    employeeId = employeeId,
    arrivalTime = arrivalTime,
    minutesLate = minutesLate,
    note = note
)

fun AbsenceEntryEntity.toDomain() = AbsenceEntry(
    id = id,
    reportId = reportId,
    employeeId = employeeId,
    reasonId = reasonId,
    comment = comment
)

fun ReportBundleEntity.toDomain() = DailyReport(
    id = report.id,
    date = report.date,
    status = ReportStatus.valueOf(report.status),
    lateEntries = lateEntries.map { it.toDomain() },
    absenceEntries = absenceEntries.map { it.toDomain() },
    visitorCount = report.visitorCount,
    introText = report.introText,
    createdBy = report.createdBy,
    createdAt = report.createdAt,
    updatedAt = report.updatedAt
)

fun DailyReport.toEntity() = ReportEntity(
    id = id,
    date = date,
    status = status.name,
    visitorCount = visitorCount,
    introText = introText,
    createdBy = createdBy,
    createdAt = createdAt,
    updatedAt = updatedAt
)

