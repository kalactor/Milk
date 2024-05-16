package com.rabarka.milk.data

import android.content.Context
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Milk::class], version = 2, autoMigrations = [AutoMigration(from = 1, to = 2)])
abstract class MilkDatabase : RoomDatabase() {
    abstract fun milkDao(): MilkDao

    companion object {
        @Volatile
        private var INSTANCE: MilkDatabase? = null

        fun getDatabase(context: Context): MilkDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(context, MilkDatabase::class.java, "milk_database")
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}