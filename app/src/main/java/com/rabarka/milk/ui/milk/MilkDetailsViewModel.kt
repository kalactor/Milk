package com.rabarka.milk.ui.milk

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rabarka.milk.data.MilkRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class MilkDetailsViewModel(
    savedStateHandle: SavedStateHandle,
    private val milkRepository: MilkRepository
) : ViewModel() {

    private val milkId: Int = checkNotNull(savedStateHandle[MilkDetailsDestination.milkIdArg])

    val uiState: StateFlow<MilkDetailsUiState> =
        milkRepository.getMilkStream(milkId).filterNotNull().map {
            MilkDetailsUiState(milkDetails = it.toMilkDetails())
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
            initialValue = MilkDetailsUiState()
        )

    suspend fun deleteMilk() {
        milkRepository.deleteMilk(uiState.value.milkDetails.toMilk())
    }

    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
    }

}

data class MilkDetailsUiState(
    val milkDetails: MilkDetails = MilkDetails()
)