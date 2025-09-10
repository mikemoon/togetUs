package sky.kr.co.newtogetusa.repository

import kotlinx.coroutines.flow.Flow
import sky.kr.co.newtogetusa.data.remote.dto.users.ProfileDto

interface DataStoreRepository {
    suspend fun putString(key: String, value: String)
    suspend fun clearString(key: String)
    suspend fun putInt(key: String, value: Int)
    suspend fun putLong(key: String, value: Long)
    suspend fun putBoolean(key: String, value:Boolean)
    suspend fun putStringArray(key:String, value: ArrayList<String>)
    suspend fun getString(key: String): String?
    suspend fun getInt(key: String): Int?
    suspend fun getLong(key: String): Long?
    suspend fun getBoolean(key: String):Boolean?
    suspend fun getStringFlow(key: String): Flow<String?>
    suspend fun getBooleanFlow(key:String): Flow<Boolean?>
    suspend fun getIntFlow(key: String): Flow<Int?>
    suspend fun getStringArray(key :String) : ArrayList<String>?

    suspend fun putProfile(key: String, value: ProfileDto)
    suspend fun getProfile(key: String): ProfileDto?
}