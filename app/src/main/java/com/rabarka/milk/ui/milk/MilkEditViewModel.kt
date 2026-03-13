package com.rabarka.milk.ui.milk

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rabarka.milk.data.Counterparty
import com.rabarka.milk.data.CounterpartyRole
import com.rabarka.milk.data.MilkRepository
import com.rabarka.milk.data.TransactionType
import com.rabarka.milk.helpers.UserMode
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class MilkEditViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val milkRepository: MilkRepository
) : ViewModel() {
    var milkUiState by mutableStateOf(MilkUiState())
        private set

    private val milkId: Int = checkNotNull(savedStateHandle[MilkEditDestination.milkIdArg])
    private val transactionTypeFlow = MutableStateFlow(TransactionType.SOLD)

    @OptIn(ExperimentalCoroutinesApi::class)
    val availableCounterparties: StateFlow<List<Counterparty>> = transactionTypeFlow
        .flatMapLatest { transactionType ->
            milkRepository.getCounterpartiesForTransactionStream(transactionType)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
            initialValue = emptyList()
        )

    init {
        viewModelScope.launch {
            val details = milkRepository.getRecordStream(milkId).first().toMilkDetails()
            transactionTypeFlow.value = details.transactionType
            milkUiState = MilkUiState(
                milkDetails = details,
                isEntryValid = validateInput(details)
            )
        }
    }

    suspend fun updateMilk() {
        if (validateInput(milkUiState.milkDetails)) {
            milkRepository.updateRecord(milkUiState.milkDetails.toMilkRecord())
        }
    }

    fun updateUiState(milkDetails: MilkDetails) {
        if (milkDetails.transactionType != transactionTypeFlow.value) {
            transactionTypeFlow.value = milkDetails.transactionType
        }
        milkUiState = MilkUiState(milkDetails = milkDetails, isEntryValid = validateInput(milkDetails))
    }

    fun applyUserMode(userMode: UserMode) {
        when (userMode) {
            UserMode.BUYER -> setTransactionType(TransactionType.BOUGHT, clearCounterparty = true)
            UserMode.SELLER -> setTransactionType(TransactionType.SOLD, clearCounterparty = true)
            UserMode.BOTH -> Unit
        }
    }

    fun setTransactionType(transactionType: TransactionType, clearCounterparty: Boolean = true) {
        val current = milkUiState.milkDetails
        val shouldClearCounterparty =
            clearCounterparty && current.transactionType != transactionType
        if (current.transactionType == transactionType && !shouldClearCounterparty) {
            return
        }

        val updated = current.copy(
            transactionType = transactionType,
            selectedCounterpartyId = if (shouldClearCounterparty) {
                null
            } else {
                current.selectedCounterpartyId
            },
            partyName = if (shouldClearCounterparty) "" else current.partyName,
            partyPhone = if (shouldClearCounterparty) "" else current.partyPhone
        )
        updateUiState(updated)
    }

    fun selectCounterparty(counterparty: Counterparty?) {
        val current = milkUiState.milkDetails
        val updated = current.copy(
            selectedCounterpartyId = counterparty?.id,
            partyName = counterparty?.name.orEmpty(),
            partyPhone = counterparty?.phone.orEmpty()
        )
        updateUiState(updated)
    }

    suspend fun addCounterpartyAndSelect(name: String, phone: String, role: CounterpartyRole) {
        val trimmedName = name.trim()
        if (trimmedName.isBlank()) return

        val trimmedPhone = phone.trim()
        val newId = milkRepository.upsertCounterparty(
            Counterparty(
                name = trimmedName,
                phone = trimmedPhone,
                role = role.name
            )
        ).toInt()

        selectCounterparty(
            Counterparty(
                id = newId,
                name = trimmedName,
                phone = trimmedPhone,
                role = role.name
            )
        )
    }

    private fun validateInput(uiState: MilkDetails): Boolean {
        if (uiState.partyName.isBlank()) return false

        val cowLiters = parseOptionalNumber(uiState.cowLiters) ?: return false
        val buffaloLiters = parseOptionalNumber(uiState.buffaloLiters) ?: return false

        if (cowLiters <= 0.0 && buffaloLiters <= 0.0) return false

        val cowFat = parseOptionalNumber(uiState.cowFat) ?: return false
        val buffaloFat = parseOptionalNumber(uiState.buffaloFat) ?: return false

        if (cowFat < 0.0 || buffaloFat < 0.0) return false

        return true
    }

    private fun parseOptionalNumber(value: String): Double? {
        if (value.isBlank()) return 0.0
        return value.toDoubleOrNull()?.takeIf { it >= 0.0 }
    }

    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
    }
}
