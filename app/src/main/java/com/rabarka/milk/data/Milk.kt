package com.rabarka.milk.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "milk")
data class Milk(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val date: Long,
    val buffalo: Double,
    val cow: Double,
    @ColumnInfo(name = "isAmavasya", defaultValue = "false")
    val isAmavasya: Boolean = false
)
