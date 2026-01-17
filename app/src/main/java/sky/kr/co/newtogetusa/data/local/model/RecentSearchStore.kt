package sky.kr.co.newtogetusa.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

val Context.dataStore by preferencesDataStore("recent_search_store")

@Singleton
class RecentSearchStore @Inject constructor(
    @ApplicationContext private val context: Context
) {

    companion object {
        private val KEY_RECENT = stringSetPreferencesKey("recent_search_keywords")
        private const val MAX_SIZE = 20
    }

    val recentSearchFlow: Flow<List<String>> =
        context.dataStore.data.map { pref ->
            pref[KEY_RECENT]?.toList()?.sortedByDescending { it } ?: emptyList()
        }

    suspend fun addSearchKeyword(keyword: String) {
        context.dataStore.edit { pref ->
            val current = pref[KEY_RECENT]?.toMutableSet() ?: mutableSetOf()
            current.remove(keyword)
            current.add(keyword)
            while (current.size > MAX_SIZE) current.remove(current.first())
            pref[KEY_RECENT] = current
        }
    }

    suspend fun removeKeyword(keyword: String) {
        context.dataStore.edit { pref ->
            val current = pref[KEY_RECENT]?.toMutableSet() ?: mutableSetOf()
            current.remove(keyword)
            pref[KEY_RECENT] = current
        }
    }

    suspend fun clearAll() {
        context.dataStore.edit { pref ->
            pref.remove(KEY_RECENT)
        }
    }
}

