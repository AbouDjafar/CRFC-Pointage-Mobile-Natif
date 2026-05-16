package cm.crfc.pointage.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "crfc_session")

class SessionStorage(private val context: Context) {
    private val sessionKey = stringPreferencesKey("current_user_id")

    val currentUserId: Flow<String?> = context.dataStore.data.map { prefs -> prefs[sessionKey] }

    suspend fun saveCurrentUser(id: String) {
        context.dataStore.edit { prefs -> prefs[sessionKey] = id }
    }

    suspend fun clear() {
        context.dataStore.edit { prefs -> prefs.remove(sessionKey) }
    }
}

