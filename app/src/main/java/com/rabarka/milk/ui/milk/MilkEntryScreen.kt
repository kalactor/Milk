package com.rabarka.milk.ui.milk

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rabarka.milk.MilkTopAppBar
import com.rabarka.milk.R
import com.rabarka.milk.helpers.convertLongToDate
import com.rabarka.milk.helpers.getMonthName
import com.rabarka.milk.helpers.months
import com.rabarka.milk.ui.AppViewModelProvider
import com.rabarka.milk.ui.navigation.NavigationDestination
import com.rabarka.milk.ui.theme.MilkTheme
import kotlinx.coroutines.launch
import java.util.Date


object MilkEntryDestination : NavigationDestination {
    override val route: String = "milk_entry"
    override val titleRes: Int = R.string.milk_entry_title
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MilkEntryScreen(
    navigateBack: () -> Unit,
    onNavigateUp: () -> Unit,
    canNavigateBack: Boolean = true,
    viewModel: MilkEntryViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val coroutineScope = rememberCoroutineScope()
    Scaffold(
        topBar = {
            MilkTopAppBar(
                title = stringResource(id = MilkEntryDestination.titleRes),
                canNavigateBack = canNavigateBack,
                navigateUp = onNavigateUp
            )
        }
    ) { innerPadding ->
        MilkEntryBody(
            milkUiState = viewModel.milkUiState,
            onMilkValueChange = viewModel::updateUiState,
            onSaveClick = {
                coroutineScope.launch {
                    viewModel.saveMilk()
                    navigateBack()
                }
            },
            modifier = Modifier
                .padding(
                    start = innerPadding.calculateStartPadding(LocalLayoutDirection.current),
                    top = innerPadding.calculateTopPadding(),
                    end = innerPadding.calculateEndPadding(LocalLayoutDirection.current)
                )
                .verticalScroll(rememberScrollState())
                .fillMaxWidth()
        )
    }
}


@Composable
fun MilkEntryBody(
    milkUiState: MilkUiState,
    onMilkValueChange: (MilkDetails) -> Unit,
    onSaveClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(dimensionResource(id = R.dimen.padding_medium)),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_large))
    ) {
        MilkInputForm(
            milkDetails = milkUiState.milkDetails,
            onValueChange = onMilkValueChange,
            modifier = Modifier.fillMaxWidth()
        )
        Button(
            onClick = onSaveClick,
            enabled = milkUiState.isEntryValid,
            shape = MaterialTheme.shapes.small,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = stringResource(id = R.string.save_action))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MilkInputForm(
    milkDetails: MilkDetails,
    modifier: Modifier = Modifier,
    onValueChange: (MilkDetails) -> Unit = {},
    enabled: Boolean = true
) {
    var dateDialogController by remember {
        mutableStateOf(false)
    }
    val dateState = rememberDatePickerState()

    var monthSpinnerController by remember {
        mutableStateOf(false)
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_medium))
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(onClick = { dateDialogController = true }) {
                Text(text = "Select Date")
            }
            Text(
                text = "Date: ${convertLongToDate(milkDetails.date)}",
                style = MaterialTheme.typography.bodyLarge,
            )
        }

        if (dateDialogController) {
            DatePickerDialog(
                onDismissRequest = { dateDialogController = false },
                confirmButton = {
                    Button(onClick = {
                        if (dateState.selectedDateMillis != null) {
                            onValueChange(milkDetails.copy(date = dateState.selectedDateMillis!!))
                        }
                        dateDialogController = false
                    }) {
                        Text(text = "Confirm")
                    }
                },
                dismissButton = {
                    Button(onClick = { dateDialogController = false }) {
                        Text(text = "Cancel")
                    }
                }
            ) {
                DatePicker(state = dateState)
            }
        }
        OutlinedTextField(
            value = milkDetails.buffalo,
            onValueChange = { onValueChange(milkDetails.copy(buffalo = it)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            label = { Text(text = "Buffalo") },
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                unfocusedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                disabledContainerColor = MaterialTheme.colorScheme.secondaryContainer,
            ),
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled,
            singleLine = true
        )
        OutlinedTextField(
            value = milkDetails.cow,
            onValueChange = { onValueChange(milkDetails.copy(cow = it)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            label = { Text(text = "Cow") },
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                unfocusedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                disabledContainerColor = MaterialTheme.colorScheme.secondaryContainer,
            ),
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled,
            singleLine = true
        )
        if (enabled) {
            Text(
                text = stringResource(id = R.string.required_fields), modifier = Modifier.padding(
                    start = dimensionResource(
                        id = R.dimen.padding_medium
                    )
                )
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Select Month", fontSize = 24.sp, modifier = Modifier.weight(1f))
            Text(
                text = getMonthName(milkDetails.month),
                fontSize = 24.sp,
                modifier = Modifier.clickable { monthSpinnerController = true })
                Icon(
                    painter = painterResource(id = R.drawable.arrow_drop_down),
                    contentDescription = null
                )
        }

        DropdownMenu(
            expanded = monthSpinnerController,
            onDismissRequest = { monthSpinnerController = false }) {
            months.forEach { month ->
                DropdownMenuItem(
                    text = { Text(text = month.first) },
                    onClick = {
                        onValueChange(milkDetails.copy(month = month.second))
                        monthSpinnerController = false
                    }
                )
            }
        }


        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "is it Amavasya?", fontSize = 24.sp)
            Switch(
                checked = milkDetails.isAmavasya,
                onCheckedChange = { onValueChange(milkDetails.copy(isAmavasya = it)) }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MilkEntryScreenPreview() {
    MilkTheme {
        MilkEntryBody(milkUiState = MilkUiState(
            MilkDetails(
                date = Date().time, buffalo = "5.00", cow = "5"
            )
        ), onMilkValueChange = {}, onSaveClick = {})
    }
}