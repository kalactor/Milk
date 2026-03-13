package com.rabarka.milk.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rabarka.milk.data.Counterparty
import com.rabarka.milk.data.CounterpartyRole
import com.rabarka.milk.data.MilkRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val milkRepository: MilkRepository
) : ViewModel() {
    val counterparties: StateFlow<List<Counterparty>> =
        milkRepository.getCounterpartiesStream().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
            initialValue = emptyList()
        )

    suspend fun addCounterparty(name: String, phone: String, role: CounterpartyRole) {
        val trimmedName = name.trim()
        if (trimmedName.isBlank()) return

        milkRepository.upsertCounterparty(
            Counterparty(
                name = trimmedName,
                phone = phone.trim(),
                role = role.name
            )
        )
    }

    suspend fun deleteCounterparty(counterparty: Counterparty) {
        milkRepository.deleteCounterparty(counterparty)
    }

    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
    }
}
