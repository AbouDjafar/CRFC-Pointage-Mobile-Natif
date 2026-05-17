package cm.crfc.pointage.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users ORDER BY createdAt ASC")
    fun observeAll(): Flow<List<UserEntity>>

    @Query("SELECT * FROM users ORDER BY createdAt ASC")
    suspend fun getAll(): List<UserEntity>

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): UserEntity?

    @Query("SELECT * FROM users WHERE lower(email) = lower(:email) LIMIT 1")
    suspend fun getByEmail(email: String): UserEntity?

    @Query("SELECT COUNT(*) FROM users")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: UserEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<UserEntity>)

    @Query("DELETE FROM users WHERE id = :id")
    suspend fun deleteById(id: String)
}

@Dao
interface EmployeeDao {
    @Query("SELECT * FROM employees ORDER BY fullName COLLATE NOCASE ASC")
    fun observeAll(): Flow<List<EmployeeEntity>>

    @Query("SELECT * FROM employees ORDER BY fullName COLLATE NOCASE ASC")
    suspend fun getAll(): List<EmployeeEntity>

    @Query("SELECT COUNT(*) FROM employees")
    suspend fun count(): Int

    @Query("SELECT * FROM employees WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): EmployeeEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: EmployeeEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<EmployeeEntity>)
}

@Dao
interface AbsenceReasonDao {
    @Query("SELECT * FROM absence_reasons ORDER BY label ASC")
    fun observeAll(): Flow<List<AbsenceReasonEntity>>

    @Query("SELECT * FROM absence_reasons ORDER BY label ASC")
    suspend fun getAll(): List<AbsenceReasonEntity>

    @Query("SELECT COUNT(*) FROM absence_reasons")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<AbsenceReasonEntity>)
}

@Dao
interface RecurringAbsenceDao {
    @Query("SELECT * FROM recurring_absences")
    fun observeAll(): Flow<List<RecurringAbsenceEntity>>

    @Query("SELECT * FROM recurring_absences")
    suspend fun getAll(): List<RecurringAbsenceEntity>

    @Query("SELECT * FROM recurring_absences WHERE employeeId = :employeeId LIMIT 1")
    suspend fun getByEmployeeId(employeeId: String): RecurringAbsenceEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: RecurringAbsenceEntity)

    @Query("DELETE FROM recurring_absences WHERE employeeId = :employeeId")
    suspend fun deleteByEmployeeId(employeeId: String)
}

@Dao
interface ReportDao {
    @Transaction
    @Query("SELECT * FROM reports ORDER BY date DESC")
    fun observeBundles(): Flow<List<ReportBundleEntity>>

    @Transaction
    @Query("SELECT * FROM reports WHERE id = :id LIMIT 1")
    fun observeBundleById(id: String): Flow<ReportBundleEntity?>

    @Transaction
    @Query("SELECT * FROM reports WHERE date = :date LIMIT 1")
    fun observeBundleByDate(date: String): Flow<ReportBundleEntity?>

    @Transaction
    @Query("SELECT * FROM reports ORDER BY date DESC")
    suspend fun getBundles(): List<ReportBundleEntity>

    @Query("SELECT * FROM reports WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): ReportEntity?

    @Query("SELECT * FROM reports WHERE date = :date ORDER BY createdAt ASC")
    suspend fun getByDate(date: String): List<ReportEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: ReportEntity)

    @Query("DELETE FROM reports WHERE id = :id")
    suspend fun deleteById(id: String)
}

@Dao
interface LateEntryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: LateEntryEntity)

    @Query("DELETE FROM late_entries WHERE id = :id")
    suspend fun deleteById(id: String)
}

@Dao
interface AbsenceEntryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: AbsenceEntryEntity)

    @Query("DELETE FROM absence_entries WHERE id = :id")
    suspend fun deleteById(id: String)
}

@Dao
interface ExportFileDao {
    @Query("SELECT * FROM export_files ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<ExportFileEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(item: ExportFileEntity)

    @Query("DELETE FROM export_files WHERE id = :id")
    fun deleteById(id: String)
}
