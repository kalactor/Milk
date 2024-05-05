package com.rabarka.milk.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Milk::class], version = 1, exportSchema = false)
abstract class MilkDatabase : RoomDatabase() {
    abstract fun milkDao(): MilkDao

    companion object {
        @Volatile
        private var Instance: MilkDatabase? = null

        fun getDatabase(context: Context): MilkDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(context, MilkDatabase::class.java, "milk_database")
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { Instance = it }
            }
        }
    }
}