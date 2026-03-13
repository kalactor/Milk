package com.rabarka.milk.data

import kotlinx.coroutines.flow.Flow

class OfflineMilkRepository(private val milkDao: MilkDao) : MilkRepository {

    override fun getRecordsByMonth(month: Int, year: Int): Flow<List<MilkRecord>> {
        return milkDao.getRecordsByMonth(month = month, year = year)
    }

    override fun getAllRecordsStream(): Flow<List<MilkRecord>> {
        return milkDao.getAllRecords()
    }

    override fun getRecordStream(id: Int): Flow<MilkRecord> {
        return milkDao.getRecord(id)
    }

    override suspend fun insertRecord(record: MilkRecord) {
        return milkDao.insertRecord(record)
    }

    override suspend fun updateRecord(record: MilkRecord) {
        return milkDao.updateRecord(record)
    }

    override suspend fun deleteRecord(record: MilkRecord) {
        return milkDao.deleteRecord(record)
    }

    override fun getCounterpartiesStream(): Flow<List<Counterparty>> {
        return milkDao.getAllCounterparties()
    }

    override fun getCounterpartiesForTransactionStream(
        transactionType: TransactionType
    ): Flow<List<Counterparty>> {
        val roles = when (transactionType) {
            TransactionType.SOLD -> listOf(
                CounterpartyRole.BUYER.name,
                CounterpartyRole.BOTH.name
            )

            TransactionType.BOUGHT -> listOf(
                CounterpartyRole.SELLER.name,
                CounterpartyRole.BOTH.name
            )
        }
        return milkDao.getCounterpartiesByRoles(roles)
    }

    override suspend fun upsertCounterparty(counterparty: Counterparty): Long {
        return milkDao.upsertCounterparty(counterparty)
    }

    override suspend fun deleteCounterparty(counterparty: Counterparty) {
        milkDao.deleteCounterparty(counterparty)
    }
}
