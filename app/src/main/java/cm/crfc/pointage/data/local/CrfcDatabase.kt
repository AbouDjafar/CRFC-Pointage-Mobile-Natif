package cm.crfc.pointage.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        UserEntity::class,
        EmployeeEntity::class,
        AbsenceReasonEntity::class,
        RecurringAbsenceEntity::class,
        ReportEntity::class,
        LateEntryEntity::class,
        AbsenceEntryEntity::class,
        ExportFileEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class CrfcDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun employeeDao(): EmployeeDao
    abstract fun absenceReasonDao(): AbsenceReasonDao
    abstract fun recurringAbsenceDao(): RecurringAbsenceDao
    abstract fun reportDao(): ReportDao
    abstract fun lateEntryDao(): LateEntryDao
    abstract fun absenceEntryDao(): AbsenceEntryDao
    abstract fun exportFileDao(): ExportFileDao

    companion object {
        fun create(context: Context): CrfcDatabase =
            Room.databaseBuilder(context, CrfcDatabase::class.java, "crfc_pointage.db")
                .fallbackToDestructiveMigration()
                .build()
    }
}
