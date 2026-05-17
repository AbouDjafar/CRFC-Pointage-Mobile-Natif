package cm.crfc.pointage.ui

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Assessment
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.People
import androidx.compose.material.icons.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import cm.crfc.pointage.AppContainer
import cm.crfc.pointage.ui.auth.LoginScreen
import cm.crfc.pointage.ui.auth.RegisterScreen
import cm.crfc.pointage.ui.components.BottomBarChrome
import cm.crfc.pointage.ui.home.EmployeesScreen
import cm.crfc.pointage.ui.home.HistoryScreen
import cm.crfc.pointage.ui.home.ReportDetailScreen
import cm.crfc.pointage.ui.home.ReportScreen
import cm.crfc.pointage.ui.home.SettingsScreen
import cm.crfc.pointage.ui.home.StatsScreen

object Routes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val TAB_REPORT = "tabs/report"
    const val TAB_HISTORY = "tabs/history"
    const val TAB_STATS = "tabs/stats"
    const val TAB_EMPLOYEES = "tabs/employees"
    const val TAB_SETTINGS = "tabs/settings"
}

private data class TabItem(
    val route: String,
    val label: String,
    val icon: @Composable () -> Unit
)

@Composable
fun CrfcApp(container: AppContainer) {
    val navController = rememberNavController()
    val currentUser by container.authRepository.observeCurrentUser().collectAsStateWithLifecycle(initialValue = null)
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    val tabs = listOf(
        TabItem(Routes.TAB_REPORT, "Rapport", { Icon(Icons.Rounded.ReceiptLong, contentDescription = null) }),
        TabItem(Routes.TAB_HISTORY, "Historique", { Icon(Icons.Rounded.History, contentDescription = null) }),
        TabItem(Routes.TAB_STATS, "Stats", { Icon(Icons.Rounded.Assessment, contentDescription = null) }),
        TabItem(Routes.TAB_EMPLOYEES, "Employes", { Icon(Icons.Rounded.People, contentDescription = null) }),
        TabItem(Routes.TAB_SETTINGS, "Reglages", { Icon(Icons.Rounded.Settings, contentDescription = null) })
    )

    LaunchedEffect(currentUser?.id) {
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
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (currentRoute?.startsWith("tabs/") == true && currentUser != null) {
                BottomBarChrome {
                    tabs.forEach { tab ->
                        val selected = backStackEntry?.destination?.hierarchy?.any { it.route == tab.route } == true
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(tab.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = tab.icon,
                            label = { androidx.compose.material3.Text(tab.label) }
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = paddingValues.calculateBottomPadding())
        ) {
            NavHost(
                navController = navController,
                startDestination = if (currentUser == null) Routes.LOGIN else Routes.TAB_REPORT,
                enterTransition = {
                    fadeIn(animationSpec = tween(260))
                },
                exitTransition = {
                    fadeOut(animationSpec = tween(200))
                },
                popEnterTransition = {
                    fadeIn(animationSpec = tween(260))
                },
                popExitTransition = {
                    fadeOut(animationSpec = tween(200))
                }
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
                    currentUser?.let {
                        ReportScreen(
                            user = it,
                            reportRepository = container.reportRepository,
                            exportService = container.exportService,
                            onOpenReport = { id -> navController.navigate("report/$id") }
                        )
                    }
                }
                composable(Routes.TAB_HISTORY) {
                    currentUser?.let {
                        HistoryScreen(
                            user = it,
                            reportRepository = container.reportRepository,
                            exportService = container.exportService,
                            onOpenReport = { id -> navController.navigate("report/$id") }
                        )
                    }
                }
                composable(Routes.TAB_STATS) {
                    currentUser?.let { StatsScreen(user = it, reportRepository = container.reportRepository) }
                }
                composable(Routes.TAB_EMPLOYEES) {
                    currentUser?.let { EmployeesScreen(user = it, reportRepository = container.reportRepository) }
                }
                composable(Routes.TAB_SETTINGS) {
                    currentUser?.let {
                        SettingsScreen(
                            user = it,
                            authRepository = container.authRepository,
                            onLoggedOut = {
                                navController.navigate(Routes.LOGIN) {
                                    popUpTo(navController.graph.id)
                                }
                            }
                        )
                    }
                }
                composable("report/{id}") { backStack ->
                    currentUser?.let {
                        val reportId = backStack.arguments?.getString("id").orEmpty()
                        ReportDetailScreen(
                            user = it,
                            reportId = reportId,
                            reportRepository = container.reportRepository,
                            authRepository = container.authRepository,
                            exportService = container.exportService,
                            onBack = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }
}
