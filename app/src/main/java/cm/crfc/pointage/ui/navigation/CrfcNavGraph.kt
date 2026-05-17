package cm.crfc.pointage.ui.navigation

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.padding
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import cm.crfc.pointage.AppContainer
import cm.crfc.pointage.ui.auth.LoginScreen
import cm.crfc.pointage.ui.auth.RegisterScreen
import cm.crfc.pointage.ui.employes.EmployeesScreen
import cm.crfc.pointage.ui.historique.HistoryScreen
import cm.crfc.pointage.ui.rapport.ReportDetailScreen
import cm.crfc.pointage.ui.rapport.ReportScreen
import cm.crfc.pointage.ui.reglages.SettingsScreen
import cm.crfc.pointage.ui.stats.StatsScreen

@Composable
fun CrfcNavGraph(container: AppContainer) {
    val navController = rememberNavController()
    val currentUser by container.authRepository.observeCurrentUser().collectAsStateWithLifecycle(initialValue = null)
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    LaunchedEffect(currentUser?.id, currentRoute) {
        if (currentUser == null && currentRoute != Routes.LOGIN && currentRoute != Routes.REGISTER) {
            navController.navigate(Routes.LOGIN) {
                popUpTo(navController.graph.id)
            }
        } else if (currentUser != null && (currentRoute == Routes.LOGIN || currentRoute == Routes.REGISTER || currentRoute == null)) {
            navController.navigate(Routes.TAB_REPORT) {
                popUpTo(navController.graph.id)
            }
        }
    }

    Scaffold(
        modifier = Modifier,
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
            startDestination = if (currentUser == null) Routes.LOGIN else Routes.TAB_REPORT,
            modifier = Modifier.padding(bottom = paddingValues.calculateBottomPadding()),
        ) {
            composable(Routes.LOGIN) {
                LoginScreen(
                    authRepository = container.authRepository,
                    onSuccess = {
                        navController.navigate(Routes.TAB_REPORT) {
                            popUpTo(Routes.LOGIN) { inclusive = true }
                        }
                    },
                    onRegister = { navController.navigate(Routes.REGISTER) }
                )
            }
            composable(Routes.REGISTER) {
                RegisterScreen(
                    authRepository = container.authRepository,
                    onBack = { navController.popBackStack() },
                    onRegistered = { navController.popBackStack() }
                )
            }
            composable(Routes.TAB_REPORT) {
                currentUser?.let { user ->
                    ReportScreen(
                        user = user,
                        reportRepository = container.reportRepository,
                        exportService = container.exportService,
                        onOpenReport = { id -> navController.navigate(Routes.reportDetail(id)) }
                    )
                }
            }
            composable(Routes.TAB_HISTORY) {
                currentUser?.let { user ->
                    HistoryScreen(
                        user = user,
                        reportRepository = container.reportRepository,
                        exportService = container.exportService,
                        onOpenReport = { id -> navController.navigate(Routes.reportDetail(id)) }
                    )
                }
            }
            composable(Routes.TAB_STATS) {
                currentUser?.let { user ->
                    StatsScreen(
                        user = user,
                        reportRepository = container.reportRepository
                    )
                }
            }
            composable(Routes.TAB_EMPLOYEES) {
                currentUser?.let { user ->
                    EmployeesScreen(
                        user = user,
                        reportRepository = container.reportRepository
                    )
                }
            }
            composable(Routes.TAB_SETTINGS) {
                currentUser?.let { user ->
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
            composable(Routes.REPORT_DETAIL) { backStackEntryState ->
                currentUser?.let { user ->
                    ReportDetailScreen(
                        user = user,
                        reportId = backStackEntryState.arguments?.getString("id").orEmpty(),
                        reportRepository = container.reportRepository,
                        authRepository = container.authRepository,
                        exportService = container.exportService,
                        onBack = { navController.popBackStack() },
                        contentPadding = paddingValues
                    )
                }
            }
        }
    }
}
