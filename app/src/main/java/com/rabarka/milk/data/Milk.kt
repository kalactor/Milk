package com.rabarka.milk.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

enum class TransactionType {
    BOUGHT,
    SOLD
}

enum class CollectionType {
    MORNING,
    EVENING
}

enum class CounterpartyRole {
    BUYER,
    SELLER,
    BOTH
}

@Entity(tableName = "counterparties")
data class Counterparty(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    @ColumnInfo(defaultValue = "")
    val phone: String = "",
    val role: String = CounterpartyRole.BOTH.name
)

@Entity(tableName = "milk_records")
data class MilkRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val timestamp: Long,
    val transactionType: String = TransactionType.SOLD.name,
    val counterpartyId: Int? = null,
    val partyName: String,
    @ColumnInfo(defaultValue = "")
    val partyPhone: String = "",
    val collectionType: String = CollectionType.MORNING.name,
    @ColumnInfo(defaultValue = "0")
    val cowLiters: Double = 0.0,
    @ColumnInfo(defaultValue = "0")
    val cowFat: Double = 0.0,
    @ColumnInfo(defaultValue = "0")
    val buffaloLiters: Double = 0.0,
    @ColumnInfo(defaultValue = "0")
    val buffaloFat: Double = 0.0,
    @ColumnInfo(defaultValue = "")
    val note: String = "",
    val month: Int,
    val year: Int
)
