@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.rabarka.milk.ui.home

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.rabarka.milk.MilkTopAppBar
import com.rabarka.milk.R
import com.rabarka.milk.data.MilkRecord
import com.rabarka.milk.data.TransactionType
import com.rabarka.milk.helpers.AppSetup
import com.rabarka.milk.helpers.UserMode
import com.rabarka.milk.helpers.formatDate
import com.rabarka.milk.helpers.formatDateTime
import com.rabarka.milk.helpers.formatDecimal
import com.rabarka.milk.helpers.formatMonthYear
import com.rabarka.milk.ui.navigation.NavigationDestination
import java.util.Calendar
import java.util.Locale

object HomeDestination : NavigationDestination {
    override val route: String = "home"
    override val titleRes: Int = R.string.app_name
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun HomeScreen(
    navigateToMilkEntry: () -> Unit,
    navigateToMilkUpdate: (Int) -> Unit,
    navigateToSettings: () -> Unit,
    appSetup: AppSetup? = null,
    modifier: Modifier = Modifier,
    viewModel: HomeScreenViewModel = hiltViewModel()
) {
    val uiState by viewModel.homeUiState.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }

    LaunchedEffect(appSetup?.userMode) {
        viewModel.setUserMode(appSetup?.userMode ?: UserMode.BOTH)
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            MilkTopAppBar(
                title = stringResource(id = HomeDestination.titleRes),
                canNavigateBack = false,
                actions = {
                    IconButton(onClick = navigateToSettings) {
                        Icon(
                            imageVector = Icons.Filled.Settings,
                            contentDescription = stringResource(R.string.settings_title)
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            if (selectedTab != 1) {
                FloatingActionButton(onClick = navigateToMilkEntry) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = stringResource(id = R.string.record_entry_title)
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = dimensionResource(id = R.dimen.padding_medium))
        ) {
            MonthSelector(
                month = uiState.selectedMonth,
                year = uiState.selectedYear,
                onPreviousMonth = viewModel::previousMonth,
                onNextMonth = viewModel::nextMonth,
                canGoToNextMonth = uiState.canGoToNextMonth,
                onCurrentMonth = viewModel::currentMonth
            )

            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text(stringResource(R.string.records_tab)) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text(stringResource(R.string.analytics_tab)) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text(stringResource(R.string.calendar_tab)) }
                )
            }

            when (selectedTab) {
                0 -> RecordsTab(
                    records = uiState.records,
                    selectedFilter = uiState.transactionFilter,
                    allowFilterChange = uiState.allowFilterChange,
                    userMode = uiState.userMode,
                    onFilterChange = viewModel::setTransactionFilter,
                    onRecordClick = { navigateToMilkUpdate(it.id) }
                )

                1 -> AnalyticsTab(
                    analytics = uiState.analytics,
                    trend = uiState.monthlyTrend,
                    analyticsPeriodType = uiState.analyticsPeriodType,
                    analyticsDateRange = uiState.analyticsDateRange,
                    selectedMonth = uiState.selectedMonth,
                    selectedYear = uiState.selectedYear,
                    onAnalyticsPeriodTypeChange = viewModel::setAnalyticsPeriodType,
                    onAnalyticsRangeStartChange = viewModel::setAnalyticsRangeStart,
                    onAnalyticsRangeEndChange = viewModel::setAnalyticsRangeEnd
                )

                else -> CalendarTab(
                    calendarDays = uiState.calendarDays,
                    recordedDaysCount = uiState.recordedDaysCount,
                    totalDaysInMonth = uiState.totalDaysInMonth
                )
            }
        }
    }
}

@Composable
private fun MonthSelector(
    month: Int,
    year: Int,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    canGoToNextMonth: Boolean,
    onCurrentMonth: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = dimensionResource(id = R.dimen.padding_small)),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPreviousMonth) {
            Icon(imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = null)
        }

        Text(
            text = formatMonthYear(month, year),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier
                .weight(1f)
                .clip(MaterialTheme.shapes.small)
                .clickable { onCurrentMonth() }
                .padding(vertical = 10.dp),
            fontWeight = FontWeight.SemiBold
        )

        IconButton(
            onClick = onNextMonth,
            enabled = canGoToNextMonth
        ) {
            Icon(imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
        }
    }
}

@Composable
private fun RecordsTab(
    records: List<MilkRecord>,
    selectedFilter: TransactionFilter,
    allowFilterChange: Boolean,
    userMode: UserMode,
    onFilterChange: (TransactionFilter) -> Unit,
    onRecordClick: (MilkRecord) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        if (allowFilterChange) {
            Row(
                modifier = Modifier.padding(vertical = dimensionResource(id = R.dimen.padding_small)),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedFilter == TransactionFilter.ALL,
                    onClick = { onFilterChange(TransactionFilter.ALL) },
                    label = { Text(stringResource(R.string.filter_all)) }
                )
                FilterChip(
                    selected = selectedFilter == TransactionFilter.SOLD,
                    onClick = { onFilterChange(TransactionFilter.SOLD) },
                    label = { Text(stringResource(R.string.transaction_sold)) }
                )
                FilterChip(
                    selected = selectedFilter == TransactionFilter.BOUGHT,
                    onClick = { onFilterChange(TransactionFilter.BOUGHT) },
                    label = { Text(stringResource(R.string.transaction_bought)) }
                )
            }
        } else {
            Text(
                text = if (userMode == UserMode.SELLER) {
                    stringResource(R.string.mode_seller_view_only)
                } else {
                    stringResource(R.string.mode_buyer_view_only)
                },
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = dimensionResource(id = R.dimen.padding_small))
            )
        }

        if (records.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(id = R.string.no_records_description),
                    style = MaterialTheme.typography.titleMedium
                )
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(items = records, key = { it.id }) { record ->
                    RecordCard(
                        record = record,
                        onClick = { onRecordClick(record) }
                    )
                }
            }
        }
    }
}

@Composable
private fun RecordCard(record: MilkRecord, onClick: () -> Unit) {
    val totalLiters = record.cowLiters + record.buffaloLiters
    val isSold = record.transactionType == TransactionType.SOLD.name

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSold) {
                MaterialTheme.colorScheme.tertiaryContainer
            } else {
                MaterialTheme.colorScheme.secondaryContainer
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_medium)),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = record.partyName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = if (isSold) {
                        stringResource(R.string.transaction_sold)
                    } else {
                        stringResource(R.string.transaction_bought)
                    },
                    style = MaterialTheme.typography.labelLarge
                )
            }

            Text(
                text = formatDateTime(record.timestamp),
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                text = "${stringResource(R.string.total_liters)}: ${formatDecimal(totalLiters)} L",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                val cowDetail = if (record.cowFat > 0) {
                    "${stringResource(R.string.cow)} ${formatDecimal(record.cowLiters)}L @ ${formatDecimal(record.cowFat)}%"
                } else {
                    "${stringResource(R.string.cow)} ${formatDecimal(record.cowLiters)}L"
                }

                val buffaloDetail = if (record.buffaloFat > 0) {
                    "${stringResource(R.string.buffalo)} ${formatDecimal(record.buffaloLiters)}L @ ${formatDecimal(record.buffaloFat)}%"
                } else {
                    "${stringResource(R.string.buffalo)} ${formatDecimal(record.buffaloLiters)}L"
                }

                Text(
                    text = cowDetail,
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = buffaloDetail,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun AnalyticsTab(
    analytics: MilkAnalytics,
    trend: List<MonthlyTrendPoint>,
    analyticsPeriodType: AnalyticsPeriodType,
    analyticsDateRange: AnalyticsDateRange,
    selectedMonth: Int,
    selectedYear: Int,
    onAnalyticsPeriodTypeChange: (AnalyticsPeriodType) -> Unit,
    onAnalyticsRangeStartChange: (Long) -> Unit,
    onAnalyticsRangeEndChange: (Long) -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val totalHandled = analytics.totalBoughtLiters + analytics.totalSoldLiters
    val netFlow = analytics.totalSoldLiters - analytics.totalBoughtLiters
    val cowTotal = analytics.totalCowLiters
    val buffaloTotal = analytics.totalBuffaloLiters
    val speciesTotal = cowTotal + buffaloTotal
    val cowShare = if (speciesTotal <= 0.0) 0f else (cowTotal / speciesTotal).toFloat()
    val buffaloShare = if (speciesTotal <= 0.0) 0f else (buffaloTotal / speciesTotal).toFloat()
    val periodLabel = when (analyticsPeriodType) {
        AnalyticsPeriodType.SELECTED_MONTH -> formatMonthYear(selectedMonth, selectedYear)
        AnalyticsPeriodType.CUSTOM_RANGE ->
            "${formatDate(analyticsDateRange.startDateMillis)} - ${formatDate(analyticsDateRange.endDateMillis)}"
    }
    val trendTitle = if (analyticsPeriodType == AnalyticsPeriodType.SELECTED_MONTH) {
        stringResource(R.string.analytics_last_six_months)
    } else {
        stringResource(R.string.analytics_range_trend)
    }

    fun openDatePicker(
        initialMillis: Long,
        minDate: Long? = null,
        maxDate: Long? = null,
        onDateSelected: (Long) -> Unit
    ) {
        val calendar = Calendar.getInstance().apply { timeInMillis = initialMillis }
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val selected = Calendar.getInstance().apply {
                    set(Calendar.YEAR, year)
                    set(Calendar.MONTH, month)
                    set(Calendar.DAY_OF_MONTH, dayOfMonth)
                    set(Calendar.HOUR_OF_DAY, 12)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis
                onDateSelected(selected)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).apply {
            minDate?.let { datePicker.minDate = it }
            maxDate?.let { datePicker.maxDate = it }
        }.show()
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = 8.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            AnalyticsControlCard(
                analyticsPeriodType = analyticsPeriodType,
                periodLabel = periodLabel,
                analyticsDateRange = analyticsDateRange,
                onAnalyticsPeriodTypeChange = onAnalyticsPeriodTypeChange,
                onPickRangeStart = {
                    openDatePicker(
                        initialMillis = analyticsDateRange.startDateMillis,
                        maxDate = analyticsDateRange.endDateMillis.coerceAtMost(System.currentTimeMillis()),
                        onDateSelected = onAnalyticsRangeStartChange
                    )
                },
                onPickRangeEnd = {
                    openDatePicker(
                        initialMillis = analyticsDateRange.endDateMillis,
                        minDate = analyticsDateRange.startDateMillis,
                        maxDate = System.currentTimeMillis(),
                        onDateSelected = onAnalyticsRangeEndChange
                    )
                }
            )
        }
        item {
            AnalyticsHeroCard(
                analyticsPeriodType = analyticsPeriodType,
                periodLabel = periodLabel,
                totalHandled = totalHandled,
                sold = analytics.totalSoldLiters,
                bought = analytics.totalBoughtLiters,
                netFlow = netFlow,
                entries = analytics.totalEntries
            )
        }
        item {
            MilkDistributionCard(
                cowTotal = cowTotal,
                buffaloTotal = buffaloTotal,
                cowShare = cowShare,
                buffaloShare = buffaloShare
            )
        }
        item {
            AnalyticsGrid(
                sold = analytics.totalSoldLiters,
                bought = analytics.totalBoughtLiters,
                averageCowFat = analytics.averageCowFat,
                averageBuffaloFat = analytics.averageBuffaloFat
            )
        }
        item {
            TrendSectionCard(
                title = trendTitle,
                trend = trend
            )
        }
    }
}

@Composable
private fun AnalyticsControlCard(
    analyticsPeriodType: AnalyticsPeriodType,
    periodLabel: String,
    analyticsDateRange: AnalyticsDateRange,
    onAnalyticsPeriodTypeChange: (AnalyticsPeriodType) -> Unit,
    onPickRangeStart: () -> Unit,
    onPickRangeEnd: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = stringResource(R.string.analytics_controls_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = analyticsPeriodType == AnalyticsPeriodType.SELECTED_MONTH,
                    onClick = { onAnalyticsPeriodTypeChange(AnalyticsPeriodType.SELECTED_MONTH) },
                    label = { Text(stringResource(R.string.analytics_period_selected_month)) }
                )
                FilterChip(
                    selected = analyticsPeriodType == AnalyticsPeriodType.CUSTOM_RANGE,
                    onClick = { onAnalyticsPeriodTypeChange(AnalyticsPeriodType.CUSTOM_RANGE) },
                    label = { Text(stringResource(R.string.analytics_period_custom_range)) }
                )
            }
            Text(
                text = periodLabel,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            if (analyticsPeriodType == AnalyticsPeriodType.CUSTOM_RANGE) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = onPickRangeStart,
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = stringResource(R.string.analytics_from))
                            Text(
                                text = formatDate(analyticsDateRange.startDateMillis),
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                    OutlinedButton(
                        onClick = onPickRangeEnd,
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = stringResource(R.string.analytics_to))
                            Text(
                                text = formatDate(analyticsDateRange.endDateMillis),
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AnalyticsHeroCard(
    analyticsPeriodType: AnalyticsPeriodType,
    periodLabel: String,
    totalHandled: Double,
    sold: Double,
    bought: Double,
    netFlow: Double,
    entries: Int
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = if (analyticsPeriodType == AnalyticsPeriodType.SELECTED_MONTH) {
                    stringResource(R.string.analytics_snapshot_month)
                } else {
                    stringResource(R.string.analytics_snapshot_range)
                },
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = "${formatDecimal(totalHandled)} L",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = periodLabel,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MetricPill(
                    title = stringResource(R.string.transaction_sold),
                    value = "${formatDecimal(sold)} L",
                    modifier = Modifier.weight(1f)
                )
                MetricPill(
                    title = stringResource(R.string.transaction_bought),
                    value = "${formatDecimal(bought)} L",
                    modifier = Modifier.weight(1f)
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MetricPill(
                    title = stringResource(R.string.analytics_net_flow),
                    value = "${if (netFlow >= 0) "+" else "-"}${formatDecimal(kotlin.math.abs(netFlow))} L",
                    modifier = Modifier.weight(1f)
                )
                MetricPill(
                    title = stringResource(R.string.analytics_entries),
                    value = entries.toString(),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun MetricPill(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.72f)
        )
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun MilkDistributionCard(
    cowTotal: Double,
    buffaloTotal: Double,
    cowShare: Float,
    buffaloShare: Float
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Species Distribution",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            DistributionRow(
                label = stringResource(R.string.cow),
                liters = cowTotal,
                progress = cowShare,
                tint = MaterialTheme.colorScheme.primary
            )
            DistributionRow(
                label = stringResource(R.string.buffalo),
                liters = buffaloTotal,
                progress = buffaloShare,
                tint = MaterialTheme.colorScheme.secondary
            )
        }
    }
}

@Composable
private fun DistributionRow(
    label: String,
    liters: Double,
    progress: Float,
    tint: androidx.compose.ui.graphics.Color
) {
    val animatedProgress by animateFloatAsState(targetValue = progress, label = "distribution-progress")
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = label, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
            Text(
                text = "${formatDecimal(liters)} L",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(MaterialTheme.shapes.extraLarge)
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedProgress)
                    .fillMaxHeight()
                    .background(tint)
            )
        }
    }
}

@Composable
private fun AnalyticsGrid(
    sold: Double,
    bought: Double,
    averageCowFat: Double,
    averageBuffaloFat: Double
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            AnalyticsMiniCard(
                title = stringResource(R.string.analytics_total_sold_liters),
                value = "${formatDecimal(sold)} L",
                modifier = Modifier.weight(1f)
            )
            AnalyticsMiniCard(
                title = stringResource(R.string.analytics_total_bought_liters),
                value = "${formatDecimal(bought)} L",
                modifier = Modifier.weight(1f)
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            AnalyticsMiniCard(
                title = "Avg ${stringResource(R.string.cow)} Fat",
                value = if (averageCowFat <= 0.0) "-" else "${formatDecimal(averageCowFat)}%",
                modifier = Modifier.weight(1f)
            )
            AnalyticsMiniCard(
                title = "Avg ${stringResource(R.string.buffalo)} Fat",
                value = if (averageBuffaloFat <= 0.0) "-" else "${formatDecimal(averageBuffaloFat)}%",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun AnalyticsMiniCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun TrendSectionCard(
    title: String,
    trend: List<MonthlyTrendPoint>
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            if (trend.all { it.totalLiters <= 0.0 }) {
                Text(
                    text = "No volume trend yet for selected period.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                val maxValue = trend.maxOfOrNull { it.totalLiters } ?: 0.0
                trend.forEach { point ->
                    TrendRow(point = point, maxValue = maxValue)
                }
            }
        }
    }
}

@Composable
private fun TrendRow(point: MonthlyTrendPoint, maxValue: Double) {
    val progress = if (maxValue <= 0.0) 0f else (point.totalLiters / maxValue).toFloat()
    val animatedProgress by animateFloatAsState(targetValue = progress, label = "trend-progress")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, MaterialTheme.shapes.small)
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = point.label,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "${formatDecimal(point.totalLiters)} L",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        LinearProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier
                .fillMaxWidth()
                .height(7.dp)
                .clip(MaterialTheme.shapes.extraLarge),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}

@Composable
private fun CalendarTab(
    calendarDays: List<CalendarDayState>,
    recordedDaysCount: Int,
    totalDaysInMonth: Int
) {
    val missingDaysCount = (totalDaysInMonth - recordedDaysCount).coerceAtLeast(0)
    val weekdayLabels = remember { buildWeekdayLabels() }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = 8.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = stringResource(R.string.calendar_overview_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        MetricPill(
                            title = stringResource(R.string.calendar_recorded_days),
                            value = recordedDaysCount.toString(),
                            modifier = Modifier.weight(1f)
                        )
                        MetricPill(
                            title = stringResource(R.string.calendar_missing_days),
                            value = missingDaysCount.toString(),
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CalendarLegendDot(
                            color = MaterialTheme.colorScheme.primary,
                            label = stringResource(R.string.calendar_day_added)
                        )
                        CalendarLegendDot(
                            color = MaterialTheme.colorScheme.outlineVariant,
                            label = stringResource(R.string.calendar_day_missing)
                        )
                    }
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        weekdayLabels.forEach { label ->
                            Text(
                                text = label,
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    calendarDays.chunked(7).forEach { week ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            week.forEach { day ->
                                Box(modifier = Modifier.weight(1f)) {
                                    CalendarDayCell(day = day)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CalendarDayCell(day: CalendarDayState) {
    if (day.dayOfMonth == null) {
        Spacer(
            modifier = Modifier
                .aspectRatio(1f)
                .fillMaxWidth()
        )
        return
    }

    val containerColor = if (day.hasRecord) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surface
    }
    val indicatorColor = if (day.hasRecord) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.outlineVariant
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = if (day.isToday) {
            BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
        } else {
            BorderStroke(1.dp, Color.Transparent)
        },
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 4.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = day.dayOfMonth.toString(),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(MaterialTheme.shapes.extraLarge)
                    .background(indicatorColor)
            )
            Text(
                text = if (day.hasRecord) day.entryCount.toString() else "-",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun CalendarLegendDot(
    color: Color,
    label: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(MaterialTheme.shapes.extraLarge)
                .background(color)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun buildWeekdayLabels(): List<String> {
    val calendar = Calendar.getInstance()
    val firstDayOfWeek = calendar.firstDayOfWeek
    return (0..6).map { offset ->
        val dayOfWeek = ((firstDayOfWeek - Calendar.SUNDAY + offset) % 7) + Calendar.SUNDAY
        calendar.set(Calendar.DAY_OF_WEEK, dayOfWeek)
        calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, Locale.getDefault())
            ?.take(2)
            ?.uppercase(Locale.getDefault())
            .orEmpty()
    }
}
