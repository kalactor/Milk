package com.rabarka.milk.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [MilkRecord::class, Counterparty::class],
    version = 5,
    exportSchema = true
)
abstract class MilkDatabase : RoomDatabase() {
    abstract fun milkDao(): MilkDao

    companion object {
        @Volatile
        private var INSTANCE: MilkDatabase? = null

        fun getDatabase(context: Context): MilkDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(context, MilkDatabase::class.java, "milk_database")
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
