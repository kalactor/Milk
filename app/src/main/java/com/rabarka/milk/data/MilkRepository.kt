package com.rabarka.milk.data

import kotlinx.coroutines.flow.Flow

interface MilkRepository {
    fun getRecordsByMonth(month: Int, year: Int): Flow<List<MilkRecord>>

    fun getAllRecordsStream(): Flow<List<MilkRecord>>

    fun getRecordStream(id: Int): Flow<MilkRecord>

    suspend fun insertRecord(record: MilkRecord)

    suspend fun updateRecord(record: MilkRecord)

    suspend fun deleteRecord(record: MilkRecord)

    fun getCounterpartiesStream(): Flow<List<Counterparty>>

    fun getCounterpartiesForTransactionStream(transactionType: TransactionType): Flow<List<Counterparty>>

    suspend fun upsertCounterparty(counterparty: Counterparty): Long

    suspend fun deleteCounterparty(counterparty: Counterparty)
}
