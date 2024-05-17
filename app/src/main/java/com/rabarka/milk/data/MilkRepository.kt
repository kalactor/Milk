package com.rabarka.milk.data

import kotlinx.coroutines.flow.Flow

interface MilkRepository {
    fun getMilkByMonth(month: Int): Flow<List<Milk>>

    fun getAllMilkStream(): Flow<List<Milk>>

    fun getMilkStream(id: Int): Flow<Milk>

    suspend fun insertMilk(milk: Milk)

    suspend fun updateMilk(milk: Milk)

    suspend fun deleteMilk(milk: Milk)
}