package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [PerformanceLog::class], version = 1, exportSchema = false)
abstract class WarpDatabase : RoomDatabase() {
    abstract fun performanceLogDao(): PerformanceLogDao

    companion object {
        @Volatile
        private var INSTANCE: WarpDatabase? = null

        fun getDatabase(context: Context): WarpDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    WarpDatabase::class.java,
                    "warp4k_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
