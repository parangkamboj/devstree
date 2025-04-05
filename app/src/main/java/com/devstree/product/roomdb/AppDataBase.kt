package com.devstree.product.roomdb

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.devstree.product.model.AddressSuggestModel

@Database(entities = [AddressSuggestModel:: class], version = 6, exportSchema = false)
abstract class AppDataBase : RoomDatabase() {
    abstract fun Dao() : Dao
    companion object{
        @Volatile
        private var INSTANCE : AppDataBase? = null
        fun getDatabase(context: Context): AppDataBase{
            val tempInstance = INSTANCE
            if(tempInstance != null){
                return tempInstance
            }
            synchronized(this){
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDataBase::class.java,
                    "AppDatabase6"
                ).build()
                INSTANCE = instance
                return instance
            }
        }
    }
}