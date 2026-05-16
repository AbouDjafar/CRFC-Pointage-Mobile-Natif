package cm.crfc.pointage

import android.app.Application

class CrfcPointageApp : Application() {
    val appContainer: AppContainer by lazy { AppContainer(this) }
}

