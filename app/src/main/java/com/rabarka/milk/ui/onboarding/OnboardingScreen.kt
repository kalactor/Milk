package com.rabarka.milk.ui.onboarding

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rabarka.milk.R
import com.rabarka.milk.helpers.AccountType
import com.rabarka.milk.helpers.AppSetup
import com.rabarka.milk.helpers.LedgerProfileStore
import com.rabarka.milk.helpers.UserMode
import com.rabarka.milk.ui.navigation.NavigationDestination

object OnboardingDestination : NavigationDestination {
    override val route: String = "onboarding"
    override val titleRes: Int = R.string.onboarding_title
}

@Composable
fun OnboardingScreen(
    onContinueToApp: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val store = remember { LedgerProfileStore(context) }

    val (selectedMode, setSelectedMode) = remember { mutableStateOf<UserMode?>(null) }

    Scaffold(modifier = modifier) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(dimensionResource(id = R.dimen.padding_medium)),
            verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_medium))
        ) {
            Text(
                text = stringResource(id = R.string.onboarding_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = stringResource(id = R.string.onboarding_step_user_mode),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            SelectionCard(
                title = stringResource(id = R.string.mode_buyer),
                subtitle = stringResource(id = R.string.mode_buyer_desc),
                selected = selectedMode == UserMode.BUYER,
                onClick = { setSelectedMode(UserMode.BUYER) }
            )
            SelectionCard(
                title = stringResource(id = R.string.mode_seller),
                subtitle = stringResource(id = R.string.mode_seller_desc),
                selected = selectedMode == UserMode.SELLER,
                onClick = { setSelectedMode(UserMode.SELLER) }
            )
            SelectionCard(
                title = stringResource(id = R.string.mode_both),
                subtitle = stringResource(id = R.string.mode_both_desc),
                selected = selectedMode == UserMode.BOTH,
                onClick = { setSelectedMode(UserMode.BOTH) }
            )

            Button(
                onClick = {
                    val userMode = selectedMode ?: return@Button
                    store.saveAppSetup(
                        AppSetup(
                            accountType = AccountType.INDIVIDUAL,
                            userMode = userMode
                        )
                    )
                    onContinueToApp()
                },
                enabled = selectedMode != null,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = stringResource(id = R.string.continue_action))
            }
        }
    }
}

@Composable
private fun SelectionCard(
    title: String,
    subtitle: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (selected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceContainerLow
            }
        ),
        border = BorderStroke(
            width = 1.dp,
            color = if (selected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.outlineVariant
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
