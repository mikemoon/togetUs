package sky.kr.co.newtogetusa.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import sky.kr.co.newtogetusa.data.remote.dto.users.ProfileDto
import javax.inject.Inject

private const val PREFERENCES_NAME = "togetus"
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = PREFERENCES_NAME)
object DataStoreKey{
    const val KEY_IS_MODE_PLAYER = "is_mode_player"
    const val KEY_TOKEN = "access_token"
    const val KEY_REFRESH_TOKEN = "refresh_token"
    const val RECENT_LOGIN_TYPE = "recent_login_type"
    const val KEY_PROFILE = "profile"
    const val KEY_ABROAD_DELIVERY_AGREE = "agree_abroad_delivery"
}

class DataStoreRepositoryImpl  @Inject constructor(private val context: Context): DataStoreRepository {

    private val gson = Gson()

    override suspend fun putString(key: String, value: String) {
        val preferencesKey = stringPreferencesKey(key)
        context.dataStore.edit { preferences ->
            preferences[preferencesKey] = value
        }
    }

    override suspend fun clearString(key:String){
        val preferencesKey = stringPreferencesKey(key)
        context.dataStore.edit { preferences ->
            preferences.remove(preferencesKey)
        }
    }

    override suspend fun putInt(key: String, value: Int) {
        val preferencesKey = intPreferencesKey(key)
        context.dataStore.edit { preferences ->
            preferences[preferencesKey] = value
        }
    }

    override suspend fun putLong(key: String, value:Long){
        val preferencesKey = longPreferencesKey(key)
        context.dataStore.edit { preferences ->
            preferences[preferencesKey] = value
        }
    }

    override suspend fun putBoolean(key: String, value: Boolean) {
        val preferencesKey = booleanPreferencesKey(key)
        context.dataStore.edit { preferences ->
            preferences[preferencesKey] = value
        }
    }

    override suspend fun putStringArray(key: String, value: ArrayList<String>) {
        val jsonArr = JSONArray()
        for( a in value){
            jsonArr.put(a)
        }
        val setItem = jsonArr.toString()
        val preferencesKey = stringPreferencesKey(key)
        context.dataStore.edit { preferences ->
            preferences[preferencesKey] =setItem
        }
    }

    override suspend fun getString(key: String): String? {
        val preferencesKey = stringPreferencesKey(key)
        val preferences = context.dataStore.data.first()
        return preferences[preferencesKey]
    }

    override suspend fun getInt(key: String): Int? {
        val preferencesKey = intPreferencesKey(key)
        val preferences = context.dataStore.data.first()
        return preferences[preferencesKey]
    }

    override suspend fun getLong(key: String): Long? {
        val preferencesKey = longPreferencesKey(key)
        val preferences = context.dataStore.data.first()
        return preferences[preferencesKey]
    }

    override suspend fun getBoolean(key: String): Boolean? {
        val preferencesKey = booleanPreferencesKey(key)
        val preferences = context.dataStore.data.first()
        return preferences[preferencesKey]
    }

    override suspend fun getStringFlow(key: String): Flow<String?> =
        context.dataStore.data.map { preference ->
            preference[stringPreferencesKey(key)]
        }

    override suspend fun getStringArray(key: String): ArrayList<String>? {
        val preferencesKey = stringPreferencesKey(key)
        val preferences = context.dataStore.data.first()
        val item = preferences[preferencesKey] ?: return null
        val resultArray = ArrayList<String>()
        val jsonItem = JSONArray(item)
        for(a in 0 until jsonItem.length()){
            if(jsonItem.optString(a)=="") continue
            resultArray.add(jsonItem.optString(a))
        }
        return resultArray
    }

    override suspend fun getBooleanFlow(key:String): Flow<Boolean> =
        context.dataStore.data.map { preference ->
            preference[booleanPreferencesKey(key)] ?: false
        }

    override suspend fun getIntFlow(key: String): Flow<Int?> =
        context.dataStore.data.map { preference ->
            preference[intPreferencesKey(key)]
        }

    override suspend fun putProfile(key: String, value: ProfileDto) {
        val json = gson.toJson(value)
        val preferencesKey = stringPreferencesKey(key)
        context.dataStore.edit { preferences ->
            preferences[preferencesKey] = json
        }
    }

    override suspend fun getProfile(key: String): ProfileDto? {
        val preferencesKey = stringPreferencesKey(key)
        val preferences = context.dataStore.data.first()
        val json = preferences[preferencesKey] ?: return null
        return try {
            val type = object : TypeToken<ProfileDto>() {}.type
            gson.fromJson<ProfileDto>(json, type)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}