package com.rabarka.milk.ui.milk

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rabarka.milk.data.MilkRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class MilkDetailsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val milkRepository: MilkRepository
) : ViewModel() {

    private val milkId: Int = checkNotNull(savedStateHandle[MilkDetailsDestination.milkIdArg])

    val uiState: StateFlow<MilkDetailsUiState> =
        milkRepository.getRecordStream(milkId).map {
            MilkDetailsUiState(milkDetails = it.toMilkDetails())
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
            initialValue = MilkDetailsUiState()
        )

    suspend fun deleteMilk() {
        milkRepository.deleteRecord(uiState.value.milkDetails.toMilkRecord())
    }

    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
    }
}

data class MilkDetailsUiState(
    val milkDetails: MilkDetails = MilkDetails()
)
