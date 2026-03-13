package com.rabarka.milk.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rabarka.milk.data.MilkRecord
import com.rabarka.milk.data.MilkRepository
import com.rabarka.milk.data.TransactionType
import com.rabarka.milk.helpers.getCurrentMonthNumber
import com.rabarka.milk.helpers.getCurrentYear
import com.rabarka.milk.helpers.getMonthName
import com.rabarka.milk.helpers.UserMode
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import java.util.Calendar
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

enum class TransactionFilter {
    ALL,
    BOUGHT,
    SOLD
}

enum class AnalyticsPeriodType {
    SELECTED_MONTH,
    CUSTOM_RANGE
}

data class MonthSelection(
    val month: Int = getCurrentMonthNumber(),
    val year: Int = getCurrentYear()
)

data class AnalyticsDateRange(
    val startDateMillis: Long,
    val endDateMillis: Long
)

data class MilkAnalytics(
    val totalEntries: Int = 0,
    val totalCowLiters: Double = 0.0,
    val totalBuffaloLiters: Double = 0.0,
    val averageCowFat: Double = 0.0,
    val averageBuffaloFat: Double = 0.0,
    val totalBoughtLiters: Double = 0.0,
    val totalSoldLiters: Double = 0.0
)

data class MonthlyTrendPoint(
    val label: String,
    val totalLiters: Double
)

data class CalendarDayState(
    val dayOfMonth: Int? = null,
    val hasRecord: Boolean = false,
    val entryCount: Int = 0,
    val totalLiters: Double = 0.0,
    val isToday: Boolean = false
)

data class HomeUiState(
    val selectedMonth: Int = getCurrentMonthNumber(),
    val selectedYear: Int = getCurrentYear(),
    val transactionFilter: TransactionFilter = TransactionFilter.ALL,
    val userMode: UserMode = UserMode.BOTH,
    val canGoToNextMonth: Boolean = false,
    val analyticsPeriodType: AnalyticsPeriodType = AnalyticsPeriodType.SELECTED_MONTH,
    val analyticsDateRange: AnalyticsDateRange = AnalyticsDateRange(
        startDateMillis = System.currentTimeMillis(),
        endDateMillis = System.currentTimeMillis()
    ),
    val allowFilterChange: Boolean = true,
    val records: List<MilkRecord> = emptyList(),
    val analytics: MilkAnalytics = MilkAnalytics(),
    val monthlyTrend: List<MonthlyTrendPoint> = emptyList(),
    val calendarDays: List<CalendarDayState> = emptyList(),
    val recordedDaysCount: Int = 0,
    val totalDaysInMonth: Int = 0
)

private data class HomeConfig(
    val selection: MonthSelection,
    val filter: TransactionFilter,
    val userMode: UserMode,
    val analyticsPeriodType: AnalyticsPeriodType,
    val analyticsDateRange: AnalyticsDateRange
)

@HiltViewModel
class HomeScreenViewModel @Inject constructor(
    milkRepository: MilkRepository
) : ViewModel() {

    private val selectedMonthSelection = MutableStateFlow(MonthSelection())
    private val selectedFilter = MutableStateFlow(TransactionFilter.ALL)
    private val userModeFlow = MutableStateFlow(UserMode.BOTH)
    private val analyticsPeriodTypeFlow = MutableStateFlow(AnalyticsPeriodType.SELECTED_MONTH)
    private val analyticsDateRangeFlow = MutableStateFlow(defaultAnalyticsDateRange())

    @OptIn(ExperimentalCoroutinesApi::class)
    private val monthRecords = selectedMonthSelection.flatMapLatest { selection ->
        milkRepository.getRecordsByMonth(month = selection.month, year = selection.year)
    }

    private val homeConfig = combine(
        selectedMonthSelection,
        selectedFilter,
        userModeFlow,
        analyticsPeriodTypeFlow,
        analyticsDateRangeFlow
    ) { selection, filter, userMode, analyticsPeriodType, analyticsDateRange ->
        HomeConfig(
            selection = selection,
            filter = filter,
            userMode = userMode,
            analyticsPeriodType = analyticsPeriodType,
            analyticsDateRange = analyticsDateRange
        )
    }

    val homeUiState: StateFlow<HomeUiState> = combine(
        monthRecords,
        milkRepository.getAllRecordsStream(),
        homeConfig
    ) { monthlyRecords, allRecords, config ->
        val selection = config.selection
        val filter = config.filter
        val userMode = config.userMode
        val analyticsPeriodType = config.analyticsPeriodType
        val analyticsDateRange = config.analyticsDateRange

        val modeMonthlyRecords = when (userMode) {
            UserMode.BUYER -> monthlyRecords.filter { it.transactionType == TransactionType.BOUGHT.name }
            UserMode.SELLER -> monthlyRecords.filter { it.transactionType == TransactionType.SOLD.name }
            UserMode.BOTH -> monthlyRecords
        }
        val modeAllRecords = when (userMode) {
            UserMode.BUYER -> allRecords.filter { it.transactionType == TransactionType.BOUGHT.name }
            UserMode.SELLER -> allRecords.filter { it.transactionType == TransactionType.SOLD.name }
            UserMode.BOTH -> allRecords
        }

        val effectiveFilter = when (userMode) {
            UserMode.BUYER -> TransactionFilter.BOUGHT
            UserMode.SELLER -> TransactionFilter.SOLD
            UserMode.BOTH -> filter
        }

        val filteredRecords = when (effectiveFilter) {
            TransactionFilter.ALL -> modeMonthlyRecords
            TransactionFilter.BOUGHT -> modeMonthlyRecords.filter {
                it.transactionType == TransactionType.BOUGHT.name
            }

            TransactionFilter.SOLD -> modeMonthlyRecords.filter {
                it.transactionType == TransactionType.SOLD.name
            }
        }

        val analyticsRecords = when (analyticsPeriodType) {
            AnalyticsPeriodType.SELECTED_MONTH -> when (userMode) {
                UserMode.BOTH -> monthlyRecords
                else -> modeMonthlyRecords
            }

            AnalyticsPeriodType.CUSTOM_RANGE -> modeAllRecords.filter {
                it.timestamp in analyticsDateRange.startDateMillis..analyticsDateRange.endDateMillis
            }
        }
        val trackableDaysInMonth = getTrackableDaysInMonth(selection)
        val recordedTrackableDaysCount = modeMonthlyRecords
            .map { getDayOfMonth(it.timestamp) }
            .filter { it <= trackableDaysInMonth }
            .distinct()
            .size

        HomeUiState(
            selectedMonth = selection.month,
            selectedYear = selection.year,
            transactionFilter = effectiveFilter,
            userMode = userMode,
            canGoToNextMonth = canGoToNextMonth(selection),
            analyticsPeriodType = analyticsPeriodType,
            analyticsDateRange = analyticsDateRange,
            allowFilterChange = userMode == UserMode.BOTH,
            records = filteredRecords,
            analytics = buildAnalytics(analyticsRecords),
            monthlyTrend = when (analyticsPeriodType) {
                AnalyticsPeriodType.SELECTED_MONTH -> buildTrend(modeAllRecords, selection)
                AnalyticsPeriodType.CUSTOM_RANGE -> buildRangeTrend(modeAllRecords, analyticsDateRange)
            },
            calendarDays = buildCalendar(modeMonthlyRecords, selection),
            recordedDaysCount = recordedTrackableDaysCount,
            totalDaysInMonth = trackableDaysInMonth
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
        initialValue = HomeUiState()
    )

    fun setTransactionFilter(filter: TransactionFilter) {
        if (userModeFlow.value != UserMode.BOTH) return
        selectedFilter.value = filter
    }

    fun setUserMode(userMode: UserMode) {
        userModeFlow.value = userMode
        selectedFilter.value = when (userMode) {
            UserMode.BUYER -> TransactionFilter.BOUGHT
            UserMode.SELLER -> TransactionFilter.SOLD
            UserMode.BOTH -> TransactionFilter.ALL
        }
    }

    fun setAnalyticsPeriodType(periodType: AnalyticsPeriodType) {
        if (analyticsPeriodTypeFlow.value == periodType) return
        analyticsPeriodTypeFlow.value = periodType
        if (periodType == AnalyticsPeriodType.CUSTOM_RANGE) {
            analyticsDateRangeFlow.value = buildMonthAnalyticsRange(selectedMonthSelection.value)
        }
    }

    fun setAnalyticsRangeStart(timestamp: Long) {
        val normalizedStart = startOfDay(timestamp)
        val currentRange = analyticsDateRangeFlow.value
        val normalizedEnd = maxOf(currentRange.endDateMillis, endOfDay(normalizedStart))
        analyticsDateRangeFlow.value = AnalyticsDateRange(
            startDateMillis = minOf(normalizedStart, normalizedEnd),
            endDateMillis = maxOf(normalizedEnd, endOfDay(normalizedStart))
        )
    }

    fun setAnalyticsRangeEnd(timestamp: Long) {
        val normalizedEnd = endOfDay(timestamp)
        val currentRange = analyticsDateRangeFlow.value
        val normalizedStart = minOf(currentRange.startDateMillis, startOfDay(normalizedEnd))
        analyticsDateRangeFlow.value = AnalyticsDateRange(
            startDateMillis = normalizedStart,
            endDateMillis = maxOf(normalizedEnd, normalizedStart)
        )
    }

    fun previousMonth() {
        selectedMonthSelection.value = shiftMonth(selectedMonthSelection.value, -1)
    }

    fun nextMonth() {
        if (!canGoToNextMonth(selectedMonthSelection.value)) return
        selectedMonthSelection.value = shiftMonth(selectedMonthSelection.value, 1)
    }

    fun currentMonth() {
        selectedMonthSelection.value = MonthSelection()
    }

    private fun shiftMonth(selection: MonthSelection, offset: Int): MonthSelection {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.MONTH, selection.month)
            set(Calendar.YEAR, selection.year)
            set(Calendar.DAY_OF_MONTH, 1)
            add(Calendar.MONTH, offset)
        }
        return MonthSelection(
            month = calendar.get(Calendar.MONTH),
            year = calendar.get(Calendar.YEAR)
        )
    }

    private fun buildAnalytics(records: List<MilkRecord>): MilkAnalytics {
        val totalCowLiters = records.sumOf { it.cowLiters }
        val totalBuffaloLiters = records.sumOf { it.buffaloLiters }

        val weightedCowFat = records.filter { it.cowFat > 0.0 }.sumOf { it.cowLiters * it.cowFat }
        val weightedBuffaloFat = records.filter { it.buffaloFat > 0.0 }.sumOf { it.buffaloLiters * it.buffaloFat }
        val cowFatMeasuredLiters = records.filter { it.cowFat > 0.0 }.sumOf { it.cowLiters }
        val buffaloFatMeasuredLiters = records.filter { it.buffaloFat > 0.0 }.sumOf { it.buffaloLiters }

        val totalBoughtLiters = records.filter {
            it.transactionType == TransactionType.BOUGHT.name
        }.sumOf { it.cowLiters + it.buffaloLiters }

        val totalSoldLiters = records.filter {
            it.transactionType == TransactionType.SOLD.name
        }.sumOf { it.cowLiters + it.buffaloLiters }

        return MilkAnalytics(
            totalEntries = records.size,
            totalCowLiters = totalCowLiters,
            totalBuffaloLiters = totalBuffaloLiters,
            averageCowFat = if (cowFatMeasuredLiters == 0.0) 0.0 else weightedCowFat / cowFatMeasuredLiters,
            averageBuffaloFat = if (buffaloFatMeasuredLiters == 0.0) 0.0 else weightedBuffaloFat / buffaloFatMeasuredLiters,
            totalBoughtLiters = totalBoughtLiters,
            totalSoldLiters = totalSoldLiters
        )
    }

    private fun buildTrend(
        allRecords: List<MilkRecord>,
        selection: MonthSelection,
        points: Int = 6
    ): List<MonthlyTrendPoint> {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.MONTH, selection.month)
            set(Calendar.YEAR, selection.year)
            set(Calendar.DAY_OF_MONTH, 1)
        }

        val buckets = mutableListOf<MonthSelection>()
        repeat(points) {
            buckets.add(MonthSelection(calendar.get(Calendar.MONTH), calendar.get(Calendar.YEAR)))
            calendar.add(Calendar.MONTH, -1)
        }

        return buckets.reversed().map { bucket ->
            val totalLiters = allRecords.filter {
                it.month == bucket.month && it.year == bucket.year
            }.sumOf { it.cowLiters + it.buffaloLiters }

            MonthlyTrendPoint(
                label = "${getMonthName(bucket.month).take(3)} ${bucket.year.toString().takeLast(2)}",
                totalLiters = totalLiters
            )
        }
    }

    private fun buildRangeTrend(
        allRecords: List<MilkRecord>,
        range: AnalyticsDateRange
    ): List<MonthlyTrendPoint> {
        val start = Calendar.getInstance().apply {
            timeInMillis = range.startDateMillis
            set(Calendar.DAY_OF_MONTH, 1)
        }
        val end = Calendar.getInstance().apply {
            timeInMillis = range.endDateMillis
            set(Calendar.DAY_OF_MONTH, 1)
        }
        val buckets = mutableListOf<MonthSelection>()
        while (!start.after(end)) {
            buckets.add(
                MonthSelection(
                    month = start.get(Calendar.MONTH),
                    year = start.get(Calendar.YEAR)
                )
            )
            start.add(Calendar.MONTH, 1)
        }

        return buckets.map { bucket ->
            val totalLiters = allRecords.filter {
                it.month == bucket.month &&
                    it.year == bucket.year &&
                    it.timestamp in range.startDateMillis..range.endDateMillis
            }.sumOf { it.cowLiters + it.buffaloLiters }

            MonthlyTrendPoint(
                label = "${getMonthName(bucket.month).take(3)} ${bucket.year.toString().takeLast(2)}",
                totalLiters = totalLiters
            )
        }
    }

    private fun buildCalendar(
        records: List<MilkRecord>,
        selection: MonthSelection
    ): List<CalendarDayState> {
        val daysInMonth = getDaysInMonth(selection)
        val calendar = Calendar.getInstance().apply {
            set(Calendar.YEAR, selection.year)
            set(Calendar.MONTH, selection.month)
            set(Calendar.DAY_OF_MONTH, 1)
        }
        val leadingEmptyDays = ((calendar.get(Calendar.DAY_OF_WEEK) - calendar.firstDayOfWeek) + 7) % 7
        val recordsByDay = records.groupBy { getDayOfMonth(it.timestamp) }
        val today = Calendar.getInstance()
        val isCurrentMonth = today.get(Calendar.YEAR) == selection.year &&
            today.get(Calendar.MONTH) == selection.month

        val cells = MutableList(leadingEmptyDays) { CalendarDayState() }
        cells += (1..daysInMonth).map { day ->
            val dayRecords = recordsByDay[day].orEmpty()
            CalendarDayState(
                dayOfMonth = day,
                hasRecord = dayRecords.isNotEmpty(),
                entryCount = dayRecords.size,
                totalLiters = dayRecords.sumOf { it.cowLiters + it.buffaloLiters },
                isToday = isCurrentMonth && today.get(Calendar.DAY_OF_MONTH) == day
            )
        }
        val trailingEmptyDays = (7 - (cells.size % 7)) % 7
        repeat(trailingEmptyDays) {
            cells += CalendarDayState()
        }
        return cells
    }

    private fun getDaysInMonth(selection: MonthSelection): Int =
        Calendar.getInstance().apply {
            set(Calendar.YEAR, selection.year)
            set(Calendar.MONTH, selection.month)
        }.getActualMaximum(Calendar.DAY_OF_MONTH)

    private fun getTrackableDaysInMonth(selection: MonthSelection): Int {
        val today = Calendar.getInstance()
        val selectedMonthStart = Calendar.getInstance().apply {
            set(Calendar.YEAR, selection.year)
            set(Calendar.MONTH, selection.month)
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val currentMonthStart = Calendar.getInstance().apply {
            set(Calendar.YEAR, today.get(Calendar.YEAR))
            set(Calendar.MONTH, today.get(Calendar.MONTH))
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        return when {
            selectedMonthStart.before(currentMonthStart) -> getDaysInMonth(selection)
            selectedMonthStart.after(currentMonthStart) -> 0
            else -> today.get(Calendar.DAY_OF_MONTH)
        }
    }

    private fun canGoToNextMonth(selection: MonthSelection): Boolean {
        val currentMonth = MonthSelection()
        return selection.year < currentMonth.year ||
            (selection.year == currentMonth.year && selection.month < currentMonth.month)
    }

    private fun buildMonthAnalyticsRange(selection: MonthSelection): AnalyticsDateRange {
        val start = Calendar.getInstance().apply {
            set(Calendar.YEAR, selection.year)
            set(Calendar.MONTH, selection.month)
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        val end = Calendar.getInstance().apply {
            set(Calendar.YEAR, selection.year)
            set(Calendar.MONTH, selection.month)
            set(Calendar.DAY_OF_MONTH, getDaysInMonth(selection))
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.timeInMillis.coerceAtMost(endOfToday())
        return AnalyticsDateRange(
            startDateMillis = start,
            endDateMillis = maxOf(start, end)
        )
    }

    private fun getDayOfMonth(timestamp: Long): Int =
        Calendar.getInstance().apply { timeInMillis = timestamp }.get(Calendar.DAY_OF_MONTH)

    companion object {
        private const val TIMEOUT_MILLIS = 5_000L

        private fun defaultAnalyticsDateRange(): AnalyticsDateRange {
            val today = Calendar.getInstance()
            val start = Calendar.getInstance().apply {
                set(Calendar.YEAR, today.get(Calendar.YEAR))
                set(Calendar.MONTH, today.get(Calendar.MONTH))
                set(Calendar.DAY_OF_MONTH, 1)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis
            return AnalyticsDateRange(
                startDateMillis = start,
                endDateMillis = endOfToday()
            )
        }

        private fun startOfToday(): Long = startOfDay(System.currentTimeMillis())

        private fun endOfToday(): Long = endOfDay(System.currentTimeMillis())

        private fun startOfDay(timestamp: Long): Long =
            Calendar.getInstance().apply {
                timeInMillis = timestamp
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis

        private fun endOfDay(timestamp: Long): Long =
            Calendar.getInstance().apply {
                timeInMillis = timestamp
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
                set(Calendar.MILLISECOND, 999)
            }.timeInMillis
    }
}
