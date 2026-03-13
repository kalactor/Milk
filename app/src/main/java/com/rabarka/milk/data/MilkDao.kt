package com.rabarka.milk.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MilkDao {
    @Query("SELECT * FROM milk_records WHERE month = :month AND year = :year ORDER BY timestamp DESC")
    fun getRecordsByMonth(month: Int, year: Int): Flow<List<MilkRecord>>

    @Query("SELECT * FROM milk_records ORDER BY timestamp DESC")
    fun getAllRecords(): Flow<List<MilkRecord>>

    @Query("SELECT * FROM milk_records WHERE id = :id")
    fun getRecord(id: Int): Flow<MilkRecord>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: MilkRecord)

    @Update
    suspend fun updateRecord(record: MilkRecord)

    @Delete
    suspend fun deleteRecord(record: MilkRecord)

    @Query("SELECT * FROM counterparties ORDER BY name COLLATE NOCASE ASC")
    fun getAllCounterparties(): Flow<List<Counterparty>>

    @Query("SELECT * FROM counterparties WHERE role IN (:roles) ORDER BY name COLLATE NOCASE ASC")
    fun getCounterpartiesByRoles(roles: List<String>): Flow<List<Counterparty>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCounterparty(counterparty: Counterparty): Long

    @Delete
    suspend fun deleteCounterparty(counterparty: Counterparty)
}
