package cm.crfc.pointage.ui

import androidx.compose.runtime.Composable
import cm.crfc.pointage.AppContainer
import cm.crfc.pointage.ui.navigation.CrfcNavGraph

@Composable
fun CrfcApp(container: AppContainer) {
    CrfcNavGraph(container = container)
}
