package cm.crfc.pointage.ui.navigation

object Routes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val TAB_REPORT = "tabs/report"
    const val TAB_HISTORY = "tabs/history"
    const val TAB_STATS = "tabs/stats"
    const val TAB_EMPLOYEES = "tabs/employees"
    const val TAB_SETTINGS = "tabs/settings"
    const val REPORT_DETAIL = "report/{id}"

    fun reportDetail(id: String): String = "report/$id"
}
