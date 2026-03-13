package com.rabarka.milk.helpers

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

val months = listOf(
    "January" to 0,
    "February" to 1,
    "March" to 2,
    "April" to 3,
    "May" to 4,
    "June" to 5,
    "July" to 6,
    "August" to 7,
    "September" to 8,
    "October" to 9,
    "November" to 10,
    "December" to 11
)

fun getMonthName(monthIndex: Int): String {
    if (monthIndex !in 0..11) {
        throw IllegalArgumentException("Invalid month index: $monthIndex")
    }
    return months.first { it.second == monthIndex }.first
}

fun getCurrentMonthNumber(): Int {
    return Calendar.getInstance().get(Calendar.MONTH)
}

fun getCurrentYear(): Int {
    return Calendar.getInstance().get(Calendar.YEAR)
}

fun getMonthYearFromTimestamp(timestamp: Long): Pair<Int, Int> {
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = timestamp
    return calendar.get(Calendar.MONTH) to calendar.get(Calendar.YEAR)
}

fun formatMonthYear(month: Int, year: Int): String {
    return "${getMonthName(month)} $year"
}

fun formatDateTime(timestamp: Long): String {
    val formatter = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
    return formatter.format(Date(timestamp))
}

fun formatDate(timestamp: Long): String {
    val formatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    return formatter.format(Date(timestamp))
}

fun formatDecimal(value: Double, maxFractionDigits: Int = 2): String {
    val formatter = NumberFormat.getNumberInstance(Locale.getDefault())
    formatter.maximumFractionDigits = maxFractionDigits
    formatter.minimumFractionDigits = 0
    return formatter.format(value)
}

fun toNonNegativeDoubleOrNull(value: String): Double? {
    val parsed = value.toDoubleOrNull() ?: return null
    return if (parsed >= 0) parsed else null
}
