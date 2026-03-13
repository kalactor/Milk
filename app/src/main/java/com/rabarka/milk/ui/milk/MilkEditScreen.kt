package com.rabarka.milk.ui.milk

import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.rabarka.milk.MilkTopAppBar
import com.rabarka.milk.R
import com.rabarka.milk.data.CounterpartyRole
import com.rabarka.milk.data.TransactionType
import com.rabarka.milk.helpers.AccountType
import com.rabarka.milk.helpers.AppSetup
import com.rabarka.milk.helpers.LedgerProfileStore
import com.rabarka.milk.helpers.UserMode
import com.rabarka.milk.ui.navigation.NavigationDestination
import kotlinx.coroutines.launch

object MilkEditDestination : NavigationDestination {
    override val route = "milk_edit"
    override val titleRes = R.string.record_edit_title
    const val milkIdArg = "milkId"
    val routeWithArgs = "$route/{$milkIdArg}"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MilkEditScreen(
    navigateBack: () -> Unit,
    onNavigateUp: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MilkEditViewModel = hiltViewModel()
) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val profileStore = remember { LedgerProfileStore(context) }

    val appSetup = profileStore.getAppSetup() ?: AppSetup(
        accountType = AccountType.INDIVIDUAL,
        userMode = UserMode.BOTH
    )
    val userMode = appSetup.userMode

    val counterparties by viewModel.availableCounterparties.collectAsState()
    var isCounterpartyMenuExpanded by remember { mutableStateOf(false) }
    var addCounterpartyRole by remember { mutableStateOf<CounterpartyRole?>(null) }

    LaunchedEffect(userMode, viewModel.milkUiState.milkDetails.id) {
        viewModel.applyUserMode(userMode)
    }

    Scaffold(
        topBar = {
            MilkTopAppBar(
                title = stringResource(id = MilkEditDestination.titleRes),
                canNavigateBack = true,
                navigateUp = onNavigateUp
            )
        },
        modifier = modifier
    ) { innerPadding ->
        val currentDetails = viewModel.milkUiState.milkDetails
        val selectedCounterparty = counterparties.firstOrNull { it.id == currentDetails.selectedCounterpartyId }

        MilkEntryBody(
            milkUiState = viewModel.milkUiState,
            onMilkValueChange = viewModel::updateUiState,
            onSaveClick = {
                coroutineScope.launch {
                    viewModel.updateMilk()
                    navigateBack()
                }
            },
            headerContent = {
                if (userMode == UserMode.BOTH) {
                    TransactionTypeCard(
                        selectedType = currentDetails.transactionType,
                        onTypeSelected = { viewModel.setTransactionType(it) }
                    )
                } else {
                    ModeLockedInfoCard(userMode = userMode)
                }

                CounterpartyPickerCard(
                    userMode = userMode,
                    transactionType = currentDetails.transactionType,
                    counterparties = counterparties,
                    selectedCounterparty = selectedCounterparty,
                    menuExpanded = isCounterpartyMenuExpanded,
                    onToggleMenu = { isCounterpartyMenuExpanded = it },
                    onSelectCounterparty = { counterparty ->
                        viewModel.selectCounterparty(counterparty)
                    },
                    onAddNew = {
                        addCounterpartyRole = when (currentDetails.transactionType) {
                            TransactionType.SOLD -> CounterpartyRole.BUYER
                            TransactionType.BOUGHT -> CounterpartyRole.SELLER
                        }
                    }
                )
            },
            modifier = Modifier
                .padding(
                    start = innerPadding.calculateStartPadding(LocalLayoutDirection.current),
                    top = innerPadding.calculateTopPadding(),
                    end = innerPadding.calculateEndPadding(LocalLayoutDirection.current),
                    bottom = innerPadding.calculateBottomPadding()
                )
                .verticalScroll(rememberScrollState())
                .fillMaxWidth()
        )
    }

    addCounterpartyRole?.let { role ->
        AddCounterpartyDialog(
            role = role,
            onDismiss = { addCounterpartyRole = null },
            onSave = { name, phone ->
                coroutineScope.launch {
                    viewModel.addCounterpartyAndSelect(
                        name = name,
                        phone = phone,
                        role = role
                    )
                    addCounterpartyRole = null
                }
            }
        )
    }
}
