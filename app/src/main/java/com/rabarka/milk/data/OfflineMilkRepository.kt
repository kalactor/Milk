package com.rabarka.milk.data

import kotlinx.coroutines.flow.Flow

class OfflineMilkRepository(private val milkDao: MilkDao) : MilkRepository {
    override fun getAllMilkStream(): Flow<List<Milk>> {
        return milkDao.getAllMilk()
    }

    override fun getMilkStream(id: Int): Flow<Milk> {
        return milkDao.getMilk(id)
    }

    override suspend fun insertMilk(milk: Milk) {
        return milkDao.insertMilk(milk)
    }

    override suspend fun updateMilk(milk: Milk) {
        return milkDao.updateMilk(milk)
    }

    override suspend fun deleteMilk(milk: Milk) {
        return milkDao.deleteMilk(milk)
    }


}