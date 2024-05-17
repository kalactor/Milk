package com.rabarka.milk.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rabarka.milk.MilkTopAppBar
import com.rabarka.milk.R
import com.rabarka.milk.data.Milk
import com.rabarka.milk.helpers.convertLongToDate
import com.rabarka.milk.ui.AppViewModelProvider
import com.rabarka.milk.ui.navigation.NavigationDestination
object HomeDestination : NavigationDestination {
    override val route: String = "home"
    override val titleRes: Int = R.string.app_name
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navigateToMilkEntry: () -> Unit,
    navigateToMilkUpdate: (Int) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeScreenViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val homeUiState by viewModel.homeUiState.collectAsState()

    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            MilkTopAppBar(
                title = stringResource(id = HomeDestination.titleRes),
                canNavigateBack = false,
                scrollBehavior = scrollBehavior
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = navigateToMilkEntry,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.padding(
                    end = WindowInsets.safeDrawing.asPaddingValues().calculateEndPadding(
                        LocalLayoutDirection.current
                    )
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(id = R.string.milk_entry_title)
                )
            }
        }
    ) { innerPadding ->
        HomeBody(
            milkList = homeUiState.milkList,
            onMilkClick = navigateToMilkUpdate,
            modifier = modifier.fillMaxSize(),
            contentPadding = innerPadding
        )
    }

}


@Composable
private fun HomeBody(
    milkList: List<Milk>,
    onMilkClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        if (milkList.isEmpty()) {
            Text(
                text = stringResource(id = R.string.no_milk_description),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(contentPadding)
            )
        } else {
            MilkList(
                milkList = milkList,
                onMilkClick = { onMilkClick(it.id) },
                contentPadding = contentPadding,
                modifier = Modifier.padding(
                    horizontal = dimensionResource(
                        id = R.dimen.padding_small
                    )
                )
            )
        }
    }
}


@Composable
private fun MilkList(
    milkList: List<Milk>,
    onMilkClick: (Milk) -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = contentPadding
    ) {
        item {
            MilkListHeader()
        }
        items(items = milkList, key = { it.id }) { milk ->
            MilkItem(
                milk = milk,
                modifier = Modifier
                    .padding(dimensionResource(id = R.dimen.padding_small))
                    .clickable { onMilkClick(milk) })
        }
    }
}

@Composable
private fun MilkListHeader(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimensionResource(id = R.dimen.padding_medium)),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            Text(
                text =  "Date",
                style = MaterialTheme.typography.headlineLarge
            )
            Text(
                text = "Buffalo",
                style = MaterialTheme.typography.headlineLarge
            )
            Text(
                text = "Cow",
                style = MaterialTheme.typography.headlineLarge
            )
        }
    }
}

@Composable
private fun MilkItem(milk: Milk, modifier: Modifier = Modifier) {
    var cardColor = MaterialTheme.colorScheme.surfaceVariant
    if (milk.isAmavasya){
        cardColor = MaterialTheme.colorScheme.surfaceTint
    }
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor)
    ) {
        Column(
            modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_large)),
            verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_small))
        ) {
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(text = convertLongToDate(milk.date), style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.weight(0.5f))
                Text(text = milk.buffalo.toString(), style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.weight(1f))
                Text(text = milk.cow.toString(), style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}