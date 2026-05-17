package cm.crfc.pointage.data.local

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val firstName: String,
    val lastName: String,
    val email: String,
    val jobTitle: String,
    val password: String,
    val role: String,
    val isActive: Boolean,
    val createdAt: String,
    val createdBy: String? = null
)

@Entity(tableName = "employees")
data class EmployeeEntity(
    @PrimaryKey val id: String,
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

@Entity(tableName = "absence_reasons")
data class AbsenceReasonEntity(
    @PrimaryKey val id: String,
    val label: String
)

@Entity(
    tableName = "recurring_absences",
    foreignKeys = [
        ForeignKey(
            entity = EmployeeEntity::class,
            parentColumns = ["id"],
            childColumns = ["employeeId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("employeeId"), Index("reasonId")]
)
data class RecurringAbsenceEntity(
    @PrimaryKey val id: String,
    val employeeId: String,
    val reasonId: String,
    val comment: String? = null
)

@Entity(tableName = "reports")
data class ReportEntity(
    @PrimaryKey val id: String,
    val date: String,
    val status: String,
    val visitorCount: Int,
    val introText: String,
    val createdBy: String,
    val createdAt: String,
    val updatedAt: String
)

@Entity(
    tableName = "late_entries",
    foreignKeys = [
        ForeignKey(
            entity = ReportEntity::class,
            parentColumns = ["id"],
            childColumns = ["reportId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("reportId"), Index("employeeId")]
)
data class LateEntryEntity(
    @PrimaryKey val id: String,
    val reportId: String,
    val employeeId: String,
    val arrivalTime: String,
    val minutesLate: Int,
    val note: String? = null
)

@Entity(
    tableName = "absence_entries",
    foreignKeys = [
        ForeignKey(
            entity = ReportEntity::class,
            parentColumns = ["id"],
            childColumns = ["reportId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("reportId"), Index("employeeId"), Index("reasonId")]
)
data class AbsenceEntryEntity(
    @PrimaryKey val id: String,
    val reportId: String,
    val employeeId: String,
    val reasonId: String,
    val comment: String? = null
)

@Entity(tableName = "export_files")
data class ExportFileEntity(
    @PrimaryKey val id: String,
    val type: String,
    val fileName: String,
    val filePath: String,
    val sizeBytes: Long,
    val createdAt: String,
    val reportDate: String? = null,
    val periodStart: String? = null,
    val periodEnd: String? = null
)

data class ReportBundleEntity(
    @Embedded val report: ReportEntity,
    @Relation(parentColumn = "id", entityColumn = "reportId")
    val lateEntries: List<LateEntryEntity>,
    @Relation(parentColumn = "id", entityColumn = "reportId")
    val absenceEntries: List<AbsenceEntryEntity>
)
