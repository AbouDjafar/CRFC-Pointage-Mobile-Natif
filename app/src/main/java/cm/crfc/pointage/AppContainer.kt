package cm.crfc.pointage

import android.content.Context
import cm.crfc.pointage.data.AuthRepository
import cm.crfc.pointage.data.ExportService
import cm.crfc.pointage.data.ReportRepository
import cm.crfc.pointage.data.SeedInstaller
import cm.crfc.pointage.data.SessionStorage
import cm.crfc.pointage.data.local.CrfcDatabase

class AppContainer(context: Context) {
    private val appContext = context.applicationContext
    private val database = CrfcDatabase.create(appContext)
    private val sessionStorage = SessionStorage(appContext)

    val authRepository = AuthRepository(
        userDao = database.userDao(),
        sessionStorage = sessionStorage
    )

    val reportRepository = ReportRepository(
        employeeDao = database.employeeDao(),
        absenceReasonDao = database.absenceReasonDao(),
        recurringAbsenceDao = database.recurringAbsenceDao(),
        reportDao = database.reportDao(),
        lateEntryDao = database.lateEntryDao(),
        absenceEntryDao = database.absenceEntryDao()
    )

    val exportService = ExportService(
        context = appContext,
        exportFileDao = database.exportFileDao()
    )

    init {
        SeedInstaller(
            userDao = database.userDao(),
            employeeDao = database.employeeDao(),
            absenceReasonDao = database.absenceReasonDao()
        ).installIfNeeded()
    }
}
