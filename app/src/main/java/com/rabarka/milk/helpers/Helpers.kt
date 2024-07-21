package com.rabarka.milk.helpers

import android.icu.util.Calendar
import java.text.SimpleDateFormat
import java.util.Date

val months = listOf(
    Pair("January", 0),
    Pair("February", 1),
    Pair("March", 2),
    Pair("April", 3),
    Pair("May", 4),
    Pair("June", 5),
    Pair("July", 6),
    Pair("August", 7),
    Pair("September", 8),
    Pair("October", 9),
    Pair("November", 10),
    Pair("December", 11)
)

fun getMonthName(monthIndex: Int): String {
    // Ensure the monthIndex is within valid range
    if (monthIndex < 0 || monthIndex > 11) {
        throw IllegalArgumentException("Invalid month index: $monthIndex")
    }
    // Find the corresponding month name for the given monthIndex
    val monthName = months.first { it.second == monthIndex }.first
    return monthName
}



fun getCurrentMonthNumber(): Int {
    // Get the current date and time
    val currentDate = Date()

    // Create a Calendar instance and set the time to the current date
    val calendar = Calendar.getInstance()
    calendar.time = currentDate

    // Get the month number (Calendar.MONTH returns 0 for January, 1 for February, etc.)
    val monthNumber = calendar.get(Calendar.MONTH)

    return monthNumber
}

fun convertLongToDate(time: Long): String {
    val date = Date(time)
    val format = SimpleDateFormat.getDateInstance()
    return format.format(date)
}

fun formatIfNoDecimalValue(value: Double): String {
    return if (value == value.toInt().toDouble()) {
        value.toInt().toString()
    } else {
        value.toString()
    }
}