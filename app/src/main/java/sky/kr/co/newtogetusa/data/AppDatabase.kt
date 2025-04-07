package sky.kr.co.newtogetusa.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import sky.kr.co.newtogetusa.data.local.model.ChatMessageModel

@Database(entities = [ChatMessageModel::class], version = 1)
abstract class AppDatabase : RoomDatabase(){

    companion object{
        private var instance : AppDatabase? = null

        fun getInstance(context: Context): AppDatabase?{
            return instance ?: synchronized(this){
                val instance = Room.databaseBuilder(context.applicationContext, AppDatabase::class.java,
                    "togetus-database").build()
                this.instance = instance
                instance
            }
        }
    }

}