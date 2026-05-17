package cm.crfc.pointage.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import cm.crfc.pointage.AppContainer
import cm.crfc.pointage.model.UserRole
import cm.crfc.pointage.ui.auth.LoginScreen
import cm.crfc.pointage.ui.employes.EmployeesScreen
import cm.crfc.pointage.ui.exports.ExportsScreen
import cm.crfc.pointage.ui.historique.HistoryScreen
import cm.crfc.pointage.ui.home.HomeScreen
import cm.crfc.pointage.ui.rapport.DailyReportScreen
import cm.crfc.pointage.ui.recurring.RecurringAbsencesScreen
import cm.crfc.pointage.ui.reglages.PreferencesScreen
import cm.crfc.pointage.ui.reglages.SettingsScreen
import cm.crfc.pointage.ui.stats.StatsScreen
import cm.crfc.pointage.ui.users.UserManagementScreen
import cm.crfc.pointage.util.todayIso

@Composable
fun CrfcNavGraph(container: AppContainer) {
    val navController = rememberNavController()
    val currentUser by container.authRepository.observeCurrentUser().collectAsStateWithLifecycle(initialValue = null)
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    LaunchedEffect(currentUser?.id, currentRoute) {
        if (currentUser == null && currentRoute != Routes.LOGIN) {
            navController.navigate(Routes.LOGIN) {
                popUpTo(navController.graph.id)
            }
        } else if (currentUser != null && (currentRoute == Routes.LOGIN || currentRoute == null)) {
            navController.navigate(Routes.TAB_HOME) {
                popUpTo(navController.graph.id)
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (currentRoute?.startsWith("tabs/") == true && currentUser != null) {
                CrfcBottomBar(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = if (currentUser == null) Routes.LOGIN else Routes.TAB_HOME,
            modifier = Modifier.padding(bottom = paddingValues.calculateBottomPadding())
        ) {
            composable(Routes.LOGIN) {
                LoginScreen(
                    authRepository = container.authRepository,
                    onSuccess = {
                        navController.navigate(Routes.TAB_HOME) {
                            popUpTo(Routes.LOGIN) { inclusive = true }
                        }
                    },
                    onRegister = {}
                )
            }
            composable(Routes.TAB_HOME) {
                currentUser?.let { user ->
                    HomeScreen(
                        user = user,
                        reportRepository = container.reportRepository,
                        onOpenDailyReport = { date -> navController.navigate(Routes.dailyReport(date)) },
                        onOpenHistory = { navController.navigate(Routes.REPORT_HISTORY) },
                        onOpenExports = { navController.navigate(Routes.EXPORTS) },
                        onOpenAnalytics = { navController.navigate(Routes.ANALYTICS) }
                    )
                }
            }
            composable(Routes.TAB_PERSONNEL) {
                currentUser?.let { user ->
                    if (user.role == UserRole.ADMIN) {
                        EmployeesScreen(
                            user = user,
                            reportRepository = container.reportRepository,
                            onOpenUserManagement = { navController.navigate(Routes.USER_MANAGEMENT) },
                            onOpenRecurringAbsences = { navController.navigate(Routes.RECURRING_ABSENCES) }
                        )
                    } else {
                        SettingsScreen(
                            user = user,
                            authRepository = container.authRepository,
                            onLoggedOut = {
                                navController.navigate(Routes.LOGIN) {
                                    popUpTo(navController.graph.id)
                                }
                            }
                        )
                    }
                }
            }
            composable(Routes.TAB_SETTINGS) {
                currentUser?.let { user ->
                    if (user.role == UserRole.ADMIN) {
                        SettingsScreen(
                            user = user,
                            authRepository = container.authRepository,
                            onLoggedOut = {
                                navController.navigate(Routes.LOGIN) {
                                    popUpTo(navController.graph.id)
                                }
                            },
                            onOpenUserManagement = { navController.navigate(Routes.USER_MANAGEMENT) },
                            onOpenRecurringAbsences = { navController.navigate(Routes.RECURRING_ABSENCES) }
                        )
                    } else {
                        PreferencesScreen(
                            user = user,
                            authRepository = container.authRepository,
                            onLoggedOut = {
                                navController.navigate(Routes.LOGIN) {
                                    popUpTo(navController.graph.id)
                                }
                            }
                        )
                    }
                }
            }
            composable(Routes.DAILY_REPORT) { state ->
                currentUser?.let { user ->
                    DailyReportScreen(
                        user = user,
                        selectedDate = state.arguments?.getString("date") ?: todayIso(),
                        reportRepository = container.reportRepository,
                        onBack = { navController.popBackStack() },
                        onNavigateDate = { date ->
                            navController.navigate(Routes.dailyReport(date)) {
                                launchSingleTop = true
                            }
                        }
                    )
                }
            }
            composable(Routes.REPORT_HISTORY) {
                currentUser?.let { user ->
                    HistoryScreen(
                        user = user,
                        reportRepository = container.reportRepository,
                        exportService = container.exportService,
                        onOpenReport = { date -> navController.navigate(Routes.dailyReport(date)) }
                    )
                }
            }
            composable(Routes.ANALYTICS) {
                currentUser?.let { user ->
                    StatsScreen(
                        user = user,
                        reportRepository = container.reportRepository,
                        exportService = container.exportService
                    )
                }
            }
            composable(Routes.EXPORTS) {
                currentUser?.let { user ->
                    ExportsScreen(
                        user = user,
                        reportRepository = container.reportRepository,
                        exportService = container.exportService,
                        onBack = { navController.popBackStack() }
                    )
                }
            }
            composable(Routes.RECURRING_ABSENCES) {
                currentUser?.let { user ->
                    RecurringAbsencesScreen(
                        user = user,
                        reportRepository = container.reportRepository,
                        onBack = { navController.popBackStack() }
                    )
                }
            }
            composable(Routes.USER_MANAGEMENT) {
                currentUser?.let { user ->
                    UserManagementScreen(
                        currentUser = user,
                        authRepository = container.authRepository,
                        onBack = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}
