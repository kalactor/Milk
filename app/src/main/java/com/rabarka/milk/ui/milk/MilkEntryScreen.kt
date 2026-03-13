@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.rabarka.milk.ui.milk

import android.Manifest
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.rabarka.milk.MilkTopAppBar
import com.rabarka.milk.R
import com.rabarka.milk.data.Counterparty
import com.rabarka.milk.data.CounterpartyRole
import com.rabarka.milk.data.TransactionType
import com.rabarka.milk.helpers.AppSetup
import com.rabarka.milk.helpers.LedgerProfileStore
import com.rabarka.milk.helpers.UserMode
import com.rabarka.milk.helpers.extractContactInfo
import com.rabarka.milk.helpers.formatDateTime
import com.rabarka.milk.ui.navigation.NavigationDestination
import kotlinx.coroutines.launch
import java.util.Calendar
import androidx.compose.ui.Alignment

object MilkEntryDestination : NavigationDestination {
    override val route: String = "milk_entry"
    override val titleRes: Int = R.string.record_entry_title
}

@Composable
fun MilkEntryScreen(
    navigateBack: () -> Unit,
    onNavigateUp: () -> Unit,
    canNavigateBack: Boolean = true,
    viewModel: MilkEntryViewModel = hiltViewModel()
) {
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    val context = LocalContext.current
    val profileStore = remember { LedgerProfileStore(context) }

    val appSetup = profileStore.getAppSetup() ?: AppSetup(
        accountType = com.rabarka.milk.helpers.AccountType.INDIVIDUAL,
        userMode = UserMode.BOTH
    )
    val userMode = appSetup.userMode

    val counterparties by viewModel.availableCounterparties.collectAsState()
    var isCounterpartyMenuExpanded by remember { mutableStateOf(false) }
    var addCounterpartyRole by remember { mutableStateOf<CounterpartyRole?>(null) }

    LaunchedEffect(userMode) {
        viewModel.applyUserMode(userMode)
    }

    LaunchedEffect(
        counterparties,
        viewModel.milkUiState.milkDetails.transactionType,
        viewModel.milkUiState.milkDetails.selectedCounterpartyId
    ) {
        viewModel.ensureValidCounterpartySelection(counterparties)
    }

    Scaffold(
        topBar = {
            MilkTopAppBar(
                title = stringResource(id = MilkEntryDestination.titleRes),
                canNavigateBack = canNavigateBack,
                navigateUp = onNavigateUp
            )
        }
    ) { innerPadding ->
        val currentDetails = viewModel.milkUiState.milkDetails
        val selectedCounterparty = counterparties.firstOrNull { it.id == currentDetails.selectedCounterpartyId }

        MilkEntryBody(
            milkUiState = viewModel.milkUiState,
            onMilkValueChange = viewModel::updateUiState,
            onSaveClick = {
                scope.launch {
                    viewModel.saveMilk()
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
                scope.launch {
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

@Composable
fun TransactionTypeCard(
    selectedType: TransactionType,
    onTypeSelected: (TransactionType) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimensionResource(id = R.dimen.padding_medium)),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = stringResource(R.string.form_section_transaction),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = selectedType == TransactionType.SOLD,
                    onClick = { onTypeSelected(TransactionType.SOLD) },
                    label = { Text(stringResource(R.string.transaction_sold)) }
                )
                FilterChip(
                    selected = selectedType == TransactionType.BOUGHT,
                    onClick = { onTypeSelected(TransactionType.BOUGHT) },
                    label = { Text(stringResource(R.string.transaction_bought)) }
                )
            }
        }
    }
}

@Composable
fun ModeLockedInfoCard(userMode: UserMode) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Text(
            text = if (userMode == UserMode.SELLER) {
                stringResource(R.string.entry_mode_locked_seller)
            } else {
                stringResource(R.string.entry_mode_locked_buyer)
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimensionResource(id = R.dimen.padding_medium))
        )
    }
}

@Composable
fun CounterpartyPickerCard(
    userMode: UserMode,
    transactionType: TransactionType,
    counterparties: List<Counterparty>,
    selectedCounterparty: Counterparty?,
    menuExpanded: Boolean,
    onToggleMenu: (Boolean) -> Unit,
    onSelectCounterparty: (Counterparty) -> Unit,
    onAddNew: () -> Unit
) {
    val selectText = when (transactionType) {
        TransactionType.SOLD -> stringResource(R.string.select_buyer)
        TransactionType.BOUGHT -> stringResource(R.string.select_seller)
    }
    val addText = when (transactionType) {
        TransactionType.SOLD -> stringResource(R.string.add_new_buyer)
        TransactionType.BOUGHT -> stringResource(R.string.add_new_seller)
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimensionResource(id = R.dimen.padding_medium)),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = stringResource(R.string.entry_counterparty_section),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            OutlinedButton(
                onClick = { onToggleMenu(true) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = selectedCounterparty?.name ?: selectText)
            }

            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { onToggleMenu(false) }
            ) {
                if (counterparties.isEmpty()) {
                    DropdownMenuItem(
                        text = { Text(text = stringResource(R.string.no_counterparties)) },
                        onClick = { onToggleMenu(false) }
                    )
                } else {
                    counterparties.forEach { counterparty ->
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text(text = counterparty.name)
                                    if (counterparty.phone.isNotBlank()) {
                                        Text(
                                            text = counterparty.phone,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            },
                            onClick = {
                                onSelectCounterparty(counterparty)
                                onToggleMenu(false)
                            }
                        )
                    }
                }
            }

            Button(
                onClick = onAddNew,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = addText)
            }

            if (userMode == UserMode.BOTH) {
                Text(
                    text = when (transactionType) {
                        TransactionType.SOLD -> stringResource(R.string.mode_seller_desc)
                        TransactionType.BOUGHT -> stringResource(R.string.mode_buyer_desc)
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun AddCounterpartyDialog(
    role: CounterpartyRole,
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit
) {
    val context = LocalContext.current
    var name by rememberSaveable { mutableStateOf("") }
    var phone by rememberSaveable { mutableStateOf("") }

    val contactPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickContact()
    ) { uri ->
        if (uri != null) {
            val contact = extractContactInfo(context, uri)
            if (contact != null) {
                if (name.isBlank()) {
                    name = contact.name
                }
                phone = contact.phone
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            contactPickerLauncher.launch(null)
        }
    }

    fun pickContact() {
        val permissionState = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_CONTACTS
        )
        if (permissionState == PackageManager.PERMISSION_GRANTED) {
            contactPickerLauncher.launch(null)
        } else {
            permissionLauncher.launch(Manifest.permission.READ_CONTACTS)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "${stringResource(R.string.add_counterparty_title)}: ${
                    when (role) {
                        CounterpartyRole.BUYER -> stringResource(R.string.mode_buyer)
                        CounterpartyRole.SELLER -> stringResource(R.string.mode_seller)
                        CounterpartyRole.BOTH -> stringResource(R.string.mode_both)
                    }
                }"
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.counterparty_name)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text(stringResource(R.string.counterparty_phone_optional)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedButton(
                    onClick = { pickContact() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = stringResource(R.string.pick_from_contacts))
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.cancel_action))
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(name, phone) },
                enabled = name.trim().isNotBlank()
            ) {
                Text(text = stringResource(R.string.save_action_short))
            }
        }
    )
}

@Composable
fun MilkEntryBody(
    milkUiState: MilkUiState,
    onMilkValueChange: (MilkDetails) -> Unit,
    onSaveClick: () -> Unit,
    modifier: Modifier = Modifier,
    saveEnabledOverride: Boolean? = null,
    headerContent: (@Composable () -> Unit)? = null
) {
    Column(
        modifier = modifier.padding(dimensionResource(id = R.dimen.padding_medium)),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_medium))
    ) {
        headerContent?.invoke()

        MilkInputForm(
            milkDetails = milkUiState.milkDetails,
            onValueChange = onMilkValueChange,
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = onSaveClick,
            enabled = saveEnabledOverride ?: milkUiState.isEntryValid,
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = stringResource(id = R.string.save_action))
        }
    }
}

@Composable
fun MilkInputForm(
    milkDetails: MilkDetails,
    modifier: Modifier = Modifier,
    onValueChange: (MilkDetails) -> Unit = {},
    enabled: Boolean = true
) {
    val context = LocalContext.current

    fun openDatePicker() {
        val calendar = Calendar.getInstance().apply { timeInMillis = milkDetails.timestamp }
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val updated = Calendar.getInstance().apply {
                    timeInMillis = milkDetails.timestamp
                    set(Calendar.YEAR, year)
                    set(Calendar.MONTH, month)
                    set(Calendar.DAY_OF_MONTH, dayOfMonth)
                }
                onValueChange(milkDetails.copy(timestamp = updated.timeInMillis))
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    fun openTimePicker() {
        val calendar = Calendar.getInstance().apply { timeInMillis = milkDetails.timestamp }
        TimePickerDialog(
            context,
            { _, hourOfDay, minute ->
                val updated = Calendar.getInstance().apply {
                    timeInMillis = milkDetails.timestamp
                    set(Calendar.HOUR_OF_DAY, hourOfDay)
                    set(Calendar.MINUTE, minute)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                onValueChange(milkDetails.copy(timestamp = updated.timeInMillis))
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            false
        ).show()
    }

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Column(
            modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_medium)),
            verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_medium))
        ) {
            Text(
                text = stringResource(R.string.form_section_schedule),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = "${stringResource(R.string.date_time)}: ${formatDateTime(milkDetails.timestamp)}",
                style = MaterialTheme.typography.bodyLarge
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = { openDatePicker() },
                    enabled = enabled
                ) {
                    Text(stringResource(R.string.select_date))
                }
                OutlinedButton(
                    onClick = { openTimePicker() },
                    enabled = enabled
                ) {
                    Text(stringResource(R.string.select_time))
                }
            }

            Text(
                text = stringResource(R.string.form_section_milk),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.Top
            ) {
                OutlinedTextField(
                    value = milkDetails.cowLiters,
                    onValueChange = { onValueChange(milkDetails.copy(cowLiters = it)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    label = {
                        Text(
                            text = stringResource(R.string.cow_liters),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    enabled = enabled,
                    singleLine = true,
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 64.dp)
                )
                OutlinedTextField(
                    value = milkDetails.buffaloLiters,
                    onValueChange = { onValueChange(milkDetails.copy(buffaloLiters = it)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    label = {
                        Text(
                            text = stringResource(R.string.buffalo_liters),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    enabled = enabled,
                    singleLine = true,
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 64.dp)
                )
            }

            Text(
                text = stringResource(R.string.fat_optional_section),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.Top
            ) {
                OutlinedTextField(
                    value = milkDetails.cowFat,
                    onValueChange = { onValueChange(milkDetails.copy(cowFat = it)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    label = {
                        Text(
                            text = stringResource(R.string.cow_fat),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    enabled = enabled,
                    singleLine = true,
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 64.dp)
                )
                OutlinedTextField(
                    value = milkDetails.buffaloFat,
                    onValueChange = { onValueChange(milkDetails.copy(buffaloFat = it)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    label = {
                        Text(
                            text = stringResource(R.string.buffalo_fat),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    enabled = enabled,
                    singleLine = true,
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 64.dp)
                )
            }

            if (enabled) {
                Text(
                    text = stringResource(id = R.string.required_fields),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
