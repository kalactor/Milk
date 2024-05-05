package com.rabarka.milk.ui.milk

import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rabarka.milk.MilkTopAppBar
import com.rabarka.milk.R
import com.rabarka.milk.ui.AppViewModelProvider
import com.rabarka.milk.ui.navigation.NavigationDestination
import com.rabarka.milk.ui.theme.MilkTheme
import kotlinx.coroutines.launch

object MilkEditDestination : NavigationDestination {
    override val route = "milk_edit"
    override val titleRes = R.string.milk_edit_title
    const val milkIdArg = "milkId"
    val routeWithArgs = "$route/{$milkIdArg}"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MilkEditScreen(
    navigateBack: () -> Unit,
    onNavigateUp: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MilkEditViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val coroutineScope = rememberCoroutineScope()

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
        MilkEntryBody(
            milkUiState = viewModel.milkUiState,
            onMilkValueChange = viewModel::updateUiState,
            onSaveClick = {
                coroutineScope.launch {
                    viewModel.updateMilk()
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
        )
    }
}

@Preview(showBackground = true)
@Composable
fun MilkEditScreenPreview() {
    MilkTheme {
        MilkEditScreen(navigateBack = {}, onNavigateUp = {})
    }
}