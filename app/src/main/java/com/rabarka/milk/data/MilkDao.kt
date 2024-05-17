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
    @Query("SELECT * from milk WHERE month = :month")
    fun getMilkByMonth(month: Int): Flow<List<Milk>>

    @Query("SELECT * from milk ORDER BY date DESC")
    fun getAllMilk(): Flow<List<Milk>>

    @Query("SELECT * from milk WHERE id = :id")
    fun getMilk(id: Int): Flow<Milk>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertMilk(milk: Milk)

    @Update
    suspend fun updateMilk(milk: Milk)

    @Delete
    suspend fun deleteMilk(milk: Milk)
}