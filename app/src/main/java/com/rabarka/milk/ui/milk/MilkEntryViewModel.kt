package com.rabarka.milk.ui.milk

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rabarka.milk.data.CollectionType
import com.rabarka.milk.data.Counterparty
import com.rabarka.milk.data.CounterpartyRole
import com.rabarka.milk.data.MilkRecord
import com.rabarka.milk.data.MilkRepository
import com.rabarka.milk.data.TransactionType
import com.rabarka.milk.helpers.formatDecimal
import com.rabarka.milk.helpers.getMonthYearFromTimestamp
import com.rabarka.milk.helpers.UserMode
import com.rabarka.milk.helpers.toNonNegativeDoubleOrNull
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class MilkEntryViewModel @Inject constructor(
    private val milkRepository: MilkRepository
) : ViewModel() {
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

    var milkUiState by mutableStateOf(MilkUiState())
        private set

    fun updateUiState(milkDetails: MilkDetails) {
        if (milkDetails.transactionType != transactionTypeFlow.value) {
            transactionTypeFlow.value = milkDetails.transactionType
        }
        milkUiState = MilkUiState(milkDetails, validateInput(milkDetails))
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

    fun ensureValidCounterpartySelection(counterparties: List<Counterparty>) {
        if (counterparties.isEmpty()) return

        val currentSelectionId = milkUiState.milkDetails.selectedCounterpartyId
        val currentSelection = counterparties.firstOrNull { it.id == currentSelectionId }
        if (currentSelection != null) return

        selectCounterparty(counterparties.first())
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

    suspend fun saveMilk() {
        if (validateInput()) {
            milkRepository.insertRecord(milkUiState.milkDetails.toMilkRecord())
        }
    }

    private fun validateInput(uiState: MilkDetails = milkUiState.milkDetails): Boolean {
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
        return toNonNegativeDoubleOrNull(value)
    }
}

data class MilkUiState(
    val milkDetails: MilkDetails = MilkDetails(),
    val isEntryValid: Boolean = false
)

data class MilkDetails(
    val id: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val transactionType: TransactionType = TransactionType.SOLD,
    val selectedCounterpartyId: Int? = null,
    val partyName: String = "",
    val partyPhone: String = "",
    val collectionType: CollectionType = CollectionType.MORNING,
    val cowLiters: String = "",
    val cowFat: String = "",
    val buffaloLiters: String = "",
    val buffaloFat: String = "",
    val note: String = ""
)

fun MilkDetails.toMilkRecord(): MilkRecord {
    val (month, year) = getMonthYearFromTimestamp(timestamp)
    return MilkRecord(
        id = id,
        timestamp = timestamp,
        transactionType = transactionType.name,
        counterpartyId = selectedCounterpartyId,
        partyName = partyName.trim(),
        partyPhone = partyPhone.trim(),
        collectionType = collectionType.name,
        cowLiters = toNonNegativeDoubleOrNull(cowLiters) ?: 0.0,
        cowFat = toNonNegativeDoubleOrNull(cowFat) ?: 0.0,
        buffaloLiters = toNonNegativeDoubleOrNull(buffaloLiters) ?: 0.0,
        buffaloFat = toNonNegativeDoubleOrNull(buffaloFat) ?: 0.0,
        note = note.trim(),
        month = month,
        year = year
    )
}

fun MilkRecord.toMilkUiState(isEntryValid: Boolean = false): MilkUiState =
    MilkUiState(
        milkDetails = this.toMilkDetails(),
        isEntryValid = isEntryValid
    )

fun MilkRecord.toMilkDetails(): MilkDetails =
    MilkDetails(
        id = id,
        timestamp = timestamp,
        transactionType = runCatching { TransactionType.valueOf(transactionType) }
            .getOrDefault(TransactionType.SOLD),
        selectedCounterpartyId = counterpartyId,
        partyName = partyName,
        partyPhone = partyPhone,
        collectionType = runCatching { CollectionType.valueOf(collectionType) }
            .getOrDefault(CollectionType.MORNING),
        cowLiters = if (cowLiters == 0.0) "" else formatDecimal(cowLiters),
        cowFat = if (cowFat == 0.0) "" else formatDecimal(cowFat),
        buffaloLiters = if (buffaloLiters == 0.0) "" else formatDecimal(buffaloLiters),
        buffaloFat = if (buffaloFat == 0.0) "" else formatDecimal(buffaloFat),
        note = note
    )

private const val TIMEOUT_MILLIS = 5_000L
