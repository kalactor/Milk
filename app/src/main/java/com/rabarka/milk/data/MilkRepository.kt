package com.rabarka.milk.data

import kotlinx.coroutines.flow.Flow

interface MilkRepository {
    fun getAllMilkStream(): Flow<List<Milk>>

    fun getMilkStream(id: Int): Flow<Milk>

    suspend fun insertMilk(milk: Milk)

    suspend fun updateMilk(milk: Milk)

    suspend fun deleteMilk(milk: Milk)
}