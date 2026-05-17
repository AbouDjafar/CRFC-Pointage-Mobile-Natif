package cm.crfc.pointage.ui.navigation

object Routes {
    const val LOGIN = "login"
    const val TAB_HOME = "tabs/home"
    const val TAB_PERSONNEL = "tabs/personnel"
    const val TAB_SETTINGS = "tabs/settings"
    const val DAILY_REPORT = "daily-report/{date}"
    const val REPORT_HISTORY = "report-history"
    const val ANALYTICS = "analytics"
    const val EXPORTS = "exports"
    const val RECURRING_ABSENCES = "recurring-absences"
    const val USER_MANAGEMENT = "user-management"

    fun dailyReport(date: String): String = "daily-report/$date"
}
