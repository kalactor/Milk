package com.rabarka.milk.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rabarka.milk.data.Milk
import com.rabarka.milk.data.MilkRepository
import com.rabarka.milk.helpers.formatIfNoDecimalValue
import com.rabarka.milk.helpers.getCurrentMonthNumber
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class HomeScreenViewModel(milkRepository: MilkRepository) : ViewModel() {

    private val _monthNumber = MutableStateFlow(getCurrentMonthNumber())
    var monthNumber: StateFlow<Int> = _monthNumber

    fun setMonthNumber(monthNumber: Int) {
        _monthNumber.value = monthNumber
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val homeUiState: StateFlow<HomeUiState> =
        if (_monthNumber.value < 12) {
            _monthNumber.flatMapLatest { monthNumber ->
                milkRepository.getMilkByMonth(monthNumber).map { HomeUiState(it) }
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
                initialValue = HomeUiState()
            )
        } else {
            milkRepository.getAllMilkStream().map { HomeUiState(it) }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
                initialValue = HomeUiState()
            )
        }
    fun calculateMilkRevenue(): Triple<Pair<String, String>, String, String> {
        val homeState = homeUiState.value
        val buffaloMilkTotal = homeState.milkList.sumOf { it.buffalo }
        val cowMilkTotal = homeState.milkList.sumOf { it.cow }
        val buffaloMilkRevenue = buffaloMilkTotal * 60
        val cowMilkRevenue = cowMilkTotal * 35

        return Triple(
            Pair(formatIfNoDecimalValue(buffaloMilkRevenue), formatIfNoDecimalValue(cowMilkRevenue)),
            formatIfNoDecimalValue(buffaloMilkTotal),
            formatIfNoDecimalValue(cowMilkTotal)
        )
    }


    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
    }

}

data class HomeUiState(val milkList: List<Milk> = listOf())