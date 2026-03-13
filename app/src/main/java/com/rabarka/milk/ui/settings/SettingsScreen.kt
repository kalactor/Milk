@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.rabarka.milk.ui.settings

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.rabarka.milk.MilkTopAppBar
import com.rabarka.milk.R
import com.rabarka.milk.data.Counterparty
import com.rabarka.milk.data.CounterpartyRole
import com.rabarka.milk.helpers.AccountType
import com.rabarka.milk.helpers.AppSetup
import com.rabarka.milk.helpers.LedgerProfileStore
import com.rabarka.milk.helpers.UserMode
import com.rabarka.milk.ui.navigation.NavigationDestination
import kotlinx.coroutines.launch

object SettingsDestination : NavigationDestination {
    override val route: String = "settings"
    override val titleRes: Int = R.string.settings_title
}

@Composable
fun SettingsScreen(
    navigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val profileStore = remember { LedgerProfileStore(context) }
    val setup = remember {
        profileStore.getAppSetup() ?: AppSetup(
            accountType = AccountType.INDIVIDUAL,
            userMode = UserMode.BOTH
        )
    }

    var selectedMode by rememberSaveable { mutableStateOf(setup.userMode) }
    var showAddCounterpartyDialog by remember { mutableStateOf(false) }
    var pendingDeleteCounterparty by remember { mutableStateOf<Counterparty?>(null) }

    val counterparties by viewModel.counterparties.collectAsState()
    val scope = rememberCoroutineScope()
    val settingsSavedMessage = stringResource(R.string.settings_saved)

    Scaffold(
        modifier = modifier,
        topBar = {
            MilkTopAppBar(
                title = stringResource(id = SettingsDestination.titleRes),
                canNavigateBack = true,
                navigateUp = navigateBack
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(dimensionResource(id = R.dimen.padding_medium)),
            verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_medium))
        ) {
            item {
                SettingsModeCard(
                    selectedMode = selectedMode,
                    onModeSelected = { selectedMode = it }
                )
            }

            item {
                Button(
                    onClick = {
                        profileStore.saveAppSetup(
                            AppSetup(
                                accountType = setup.accountType,
                                userMode = selectedMode
                            )
                        )
                        Toast.makeText(
                            context,
                            settingsSavedMessage,
                            Toast.LENGTH_SHORT
                        ).show()
                        navigateBack()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = stringResource(R.string.save_settings))
                }
            }

            item {
                CounterpartySectionHeader(
                    onAddContactClick = { showAddCounterpartyDialog = true }
                )
            }

            if (counterparties.isEmpty()) {
                item {
                    Text(
                        text = stringResource(R.string.no_counterparties),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                items(items = counterparties, key = { it.id }) { counterparty ->
                    CounterpartyCard(
                        counterparty = counterparty,
                        onDelete = {
                            pendingDeleteCounterparty = counterparty
                        }
                    )
                }
            }
        }
    }

    if (showAddCounterpartyDialog) {
        AddCounterpartyDialog(
            onDismiss = { showAddCounterpartyDialog = false },
            onSave = { name, phone, role ->
                scope.launch {
                    viewModel.addCounterparty(name = name, phone = phone, role = role)
                    showAddCounterpartyDialog = false
                }
            }
        )
    }

    pendingDeleteCounterparty?.let { counterparty ->
        AlertDialog(
            onDismissRequest = { pendingDeleteCounterparty = null },
            title = { Text(text = stringResource(R.string.delete_counterparty_title)) },
            text = {
                Text(
                    text = stringResource(
                        R.string.delete_counterparty_message,
                        counterparty.name
                    )
                )
            },
            dismissButton = {
                TextButton(onClick = { pendingDeleteCounterparty = null }) {
                    Text(text = stringResource(R.string.cancel_action))
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            viewModel.deleteCounterparty(counterparty)
                            pendingDeleteCounterparty = null
                        }
                    }
                ) {
                    Text(text = stringResource(R.string.delete))
                }
            }
        )
    }
}

@Composable
private fun SettingsModeCard(
    selectedMode: UserMode,
    onModeSelected: (UserMode) -> Unit
) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = stringResource(R.string.settings_mode_section),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = selectedMode == UserMode.BUYER,
                    onClick = { onModeSelected(UserMode.BUYER) },
                    label = { Text(stringResource(R.string.mode_buyer)) }
                )
                FilterChip(
                    selected = selectedMode == UserMode.SELLER,
                    onClick = { onModeSelected(UserMode.SELLER) },
                    label = { Text(stringResource(R.string.mode_seller)) }
                )
                FilterChip(
                    selected = selectedMode == UserMode.BOTH,
                    onClick = { onModeSelected(UserMode.BOTH) },
                    label = { Text(stringResource(R.string.mode_both)) }
                )
            }
        }
    }
}

@Composable
private fun CounterpartySectionHeader(
    onAddContactClick: () -> Unit
) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = stringResource(R.string.counterparties_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Button(onClick = onAddContactClick, modifier = Modifier.fillMaxWidth()) {
                Text(text = stringResource(R.string.add_contact))
            }
        }
    }
}

@Composable
private fun CounterpartyCard(
    counterparty: Counterparty,
    onDelete: () -> Unit
) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = counterparty.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = counterparty.phone.ifBlank { "-" },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = when (runCatching { CounterpartyRole.valueOf(counterparty.role) }.getOrNull()) {
                        CounterpartyRole.BUYER -> stringResource(R.string.mode_buyer)
                        CounterpartyRole.SELLER -> stringResource(R.string.mode_seller)
                        else -> stringResource(R.string.mode_both)
                    },
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            IconButton(
                onClick = onDelete
            ) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = stringResource(R.string.remove_counterparty)
                )
            }
        }
    }
}

@Composable
private fun AddCounterpartyDialog(
    onDismiss: () -> Unit,
    onSave: (String, String, CounterpartyRole) -> Unit
) {
    val context = LocalContext.current
    var name by rememberSaveable { mutableStateOf("") }
    var phone by rememberSaveable { mutableStateOf("") }
    var selectedRole by rememberSaveable { mutableStateOf(CounterpartyRole.BUYER) }

    val contactPickerLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.PickContact()
    ) { uri ->
        if (uri != null) {
            val contact = com.rabarka.milk.helpers.extractContactInfo(context, uri)
            if (contact != null) {
                if (name.isBlank()) {
                    name = contact.name
                }
                phone = contact.phone
            }
        }
    }

    val permissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            contactPickerLauncher.launch(null)
        }
    }

    fun pickContact() {
        val permissionState = androidx.core.content.ContextCompat.checkSelfPermission(
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
            Text(text = stringResource(R.string.add_counterparty_title))
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = stringResource(R.string.counterparty_role),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = selectedRole == CounterpartyRole.BUYER,
                        onClick = { selectedRole = CounterpartyRole.BUYER },
                        label = { Text(stringResource(R.string.mode_buyer)) }
                    )
                    FilterChip(
                        selected = selectedRole == CounterpartyRole.SELLER,
                        onClick = { selectedRole = CounterpartyRole.SELLER },
                        label = { Text(stringResource(R.string.mode_seller)) }
                    )
                }
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
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Button(
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
                onClick = { onSave(name, phone, selectedRole) },
                enabled = name.trim().isNotBlank()
            ) {
                Text(text = stringResource(R.string.save_action_short))
            }
        }
    )
}
