package com.rabarka.milk.ui.milk

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rabarka.milk.data.MilkRepository
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MilkEditViewModel(
    savedStateHandle: SavedStateHandle,
    private val milkRepository: MilkRepository
) : ViewModel() {
    var milkUiState by mutableStateOf(MilkUiState())
        private set

    private val milkId: Int = checkNotNull(savedStateHandle[MilkEditDestination.milkIdArg])

    init {
        viewModelScope.launch {
            milkUiState =
                milkRepository.getMilkStream(milkId).filterNotNull().first().toMilkUiState(true)
        }
    }

    suspend fun updateMilk() {
        if (validateInput(milkUiState.milkDetails)) {
            milkRepository.updateMilk(milkUiState.milkDetails.toMilk())
        }
    }

    fun updateUiState(milkDetails: MilkDetails) {
        milkUiState =
            MilkUiState(milkDetails = milkDetails, isEntryValid = validateInput(milkDetails))
    }


    private fun validateInput(uiState: MilkDetails = milkUiState.milkDetails): Boolean {
        return with(uiState) {
            buffalo.isNotBlank() && cow.isNotBlank()
        }
    }
}