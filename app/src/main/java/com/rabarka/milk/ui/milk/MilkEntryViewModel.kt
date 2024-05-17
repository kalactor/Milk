package com.rabarka.milk.ui.milk

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.rabarka.milk.data.Milk
import com.rabarka.milk.data.MilkRepository
import com.rabarka.milk.helpers.getCurrentMonthNumber
import java.util.Date

class MilkEntryViewModel(private val milkRepository: MilkRepository) : ViewModel() {
    var milkUiState by mutableStateOf(MilkUiState())
        private set

    fun updateUiState(milkDetails: MilkDetails) {
        milkUiState = MilkUiState(milkDetails, validateInput(milkDetails))
    }

    suspend fun saveMilk() {
        if (validateInput()) {
            milkRepository.insertMilk(milkUiState.milkDetails.toMilk())
        }
    }

    private fun validateInput(uiState: MilkDetails = milkUiState.milkDetails): Boolean {
        return with(uiState) {
            buffalo.isNotBlank() && cow.isNotBlank()
        }
    }

}

data class MilkUiState(
    val milkDetails: MilkDetails = MilkDetails(),
    val isEntryValid: Boolean = false
)

data class MilkDetails(
    val id: Int = 0,
    val date: Long = Date().time,
    val buffalo: String = "",
    val cow: String = "",
    val isAmavasya: Boolean = false,
    val month: Int = getCurrentMonthNumber()
)

fun MilkDetails.toMilk(): Milk =
    Milk(
        id = id,
        date = date,
        buffalo = buffalo.toDoubleOrNull() ?: 0.0,
        cow = cow.toDoubleOrNull() ?: 0.0,
        isAmavasya = isAmavasya,
        month = month
    )

fun Milk.toMilkUiState(isEntryValid: Boolean = false): MilkUiState =
    MilkUiState(
        milkDetails = this.toMilkDetails(),
        isEntryValid = isEntryValid
    )

fun Milk.toMilkDetails(): MilkDetails =
    MilkDetails(
        id = id,
        date = date,
        buffalo = buffalo.toString(),
        cow = cow.toString(),
        isAmavasya = isAmavasya,
        month = month
    )