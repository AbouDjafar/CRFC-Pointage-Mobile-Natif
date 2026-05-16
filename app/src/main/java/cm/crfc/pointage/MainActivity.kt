package cm.crfc.pointage

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import cm.crfc.pointage.ui.CrfcApp
import cm.crfc.pointage.ui.theme.CrfcTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val container = (application as CrfcPointageApp).appContainer
        setContent {
            CrfcTheme {
                CrfcApp(container = container)
            }
        }
    }
}

