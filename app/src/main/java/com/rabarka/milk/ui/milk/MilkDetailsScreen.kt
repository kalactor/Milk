package com.rabarka.milk.ui.milk

import android.widget.Toast
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.rabarka.milk.MilkTopAppBar
import com.rabarka.milk.R
import com.rabarka.milk.helpers.buildRecordShareMessage
import com.rabarka.milk.helpers.formatDateTime
import com.rabarka.milk.helpers.formatDecimal
import com.rabarka.milk.helpers.openShareSheet
import com.rabarka.milk.helpers.openWhatsApp
import com.rabarka.milk.ui.navigation.NavigationDestination
import kotlinx.coroutines.launch

object MilkDetailsDestination : NavigationDestination {
    override val route = "milk_details"
    override val titleRes = R.string.record_detail_title
    const val milkIdArg = "milkId"
    val routeWithArgs = "$route/{$milkIdArg}"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MilkDetailsScreen(
    navigateToEditMilk: (Int) -> Unit,
    navigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MilkDetailsViewModel = hiltViewModel()
) {
    val uiState = viewModel.uiState.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            MilkTopAppBar(
                title = stringResource(id = MilkDetailsDestination.titleRes),
                canNavigateBack = true,
                navigateUp = navigateBack
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navigateToEditMilk(uiState.value.milkDetails.id) },
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.padding(
                    end = WindowInsets.safeDrawing.asPaddingValues().calculateEndPadding(
                        LocalLayoutDirection.current
                    )
                )
            ) {
                Icon(
                    imageVector = Icons.Filled.Edit,
                    contentDescription = stringResource(id = R.string.record_edit_title)
                )
            }
        },
        modifier = modifier
    ) { innerPadding ->
        MilkDetailsBody(
            milkDetailsUiState = uiState.value,
            onDelete = {
                coroutineScope.launch {
                    viewModel.deleteMilk()
                    navigateBack()
                }
            },
            modifier = Modifier
                .padding(
                    start = innerPadding.calculateStartPadding(LocalLayoutDirection.current),
                    top = innerPadding.calculateTopPadding(),
                    end = innerPadding.calculateEndPadding(LocalLayoutDirection.current),
                    bottom = innerPadding.calculateBottomPadding()
                )
                .verticalScroll(rememberScrollState())
        )
    }
}

@Composable
private fun MilkDetailsBody(
    milkDetailsUiState: MilkDetailsUiState,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var deleteConfirmationRequired by rememberSaveable { mutableStateOf(false) }
    val context = LocalContext.current
    val phoneMissingMessage = stringResource(R.string.phone_missing_for_whatsapp)
    val record = milkDetailsUiState.milkDetails.toMilkRecord()

    Column(
        modifier = modifier.padding(dimensionResource(id = R.dimen.padding_medium)),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_medium))
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(dimensionResource(id = R.dimen.padding_medium)),
                verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_medium))
            ) {
                MilkDetailsRow(
                    labelResID = R.string.date_time,
                    detail = formatDateTime(record.timestamp)
                )
                MilkDetailsRow(
                    labelResID = R.string.party_name,
                    detail = record.partyName
                )
                MilkDetailsRow(
                    labelResID = R.string.phone_number,
                    detail = if (record.partyPhone.isBlank()) "-" else record.partyPhone
                )
                MilkDetailsRow(
                    labelResID = R.string.cow,
                    detail = if (record.cowFat > 0) {
                        "${formatDecimal(record.cowLiters)} L | ${formatDecimal(record.cowFat)}%"
                    } else {
                        "${formatDecimal(record.cowLiters)} L"
                    }
                )
                MilkDetailsRow(
                    labelResID = R.string.buffalo,
                    detail = if (record.buffaloFat > 0) {
                        "${formatDecimal(record.buffaloLiters)} L | ${formatDecimal(record.buffaloFat)}%"
                    } else {
                        "${formatDecimal(record.buffaloLiters)} L"
                    }
                )
                if (record.note.isNotBlank()) {
                    MilkDetailsRow(
                        labelResID = R.string.note,
                        detail = record.note
                    )
                }
            }
        }

        FilledTonalButton(
            onClick = {
                val message = buildRecordShareMessage(record)
                if (record.partyPhone.isBlank()) {
                    Toast.makeText(
                        context,
                        phoneMissingMessage,
                        Toast.LENGTH_SHORT
                    ).show()
                } else if (!openWhatsApp(context, record.partyPhone, message)) {
                    openShareSheet(context, message)
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(imageVector = Icons.Filled.Share, contentDescription = null)
            Spacer(modifier = Modifier.padding(horizontal = dimensionResource(id = R.dimen.padding_small)))
            Text(text = stringResource(R.string.share_on_whatsapp))
        }

        OutlinedButton(
            onClick = { openShareSheet(context, buildRecordShareMessage(record)) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = stringResource(R.string.share_other_apps))
        }

        OutlinedButton(
            onClick = { deleteConfirmationRequired = true },
            shape = MaterialTheme.shapes.small,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = stringResource(id = R.string.delete))
        }

        if (deleteConfirmationRequired) {
            DeleteConfirmationDialog(
                onDeleteConfirm = {
                    deleteConfirmationRequired = false
                    onDelete()
                },
                onDeleteCancel = { deleteConfirmationRequired = false },
                modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_medium))
            )
        }
    }
}

@Composable
private fun MilkDetailsRow(
    @StringRes labelResID: Int,
    detail: String,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier.fillMaxWidth()) {
        Text(text = stringResource(id = labelResID), fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.weight(1f))
        Text(text = detail)
    }
}

@Composable
private fun DeleteConfirmationDialog(
    onDeleteConfirm: () -> Unit,
    onDeleteCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = { },
        title = { Text(text = stringResource(id = R.string.attention)) },
        text = { Text(text = stringResource(id = R.string.delete_question)) },
        modifier = modifier,
        dismissButton = {
            TextButton(onClick = onDeleteCancel) {
                Text(text = stringResource(id = R.string.cancel_action))
            }
        },
        confirmButton = {
            TextButton(onClick = onDeleteConfirm) {
                Text(text = stringResource(id = R.string.delete))
            }
        }
    )
}
