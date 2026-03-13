package com.rabarka.milk.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.rabarka.milk.helpers.LedgerProfileStore
import com.rabarka.milk.ui.home.HomeDestination
import com.rabarka.milk.ui.home.HomeScreen
import com.rabarka.milk.ui.milk.MilkDetailsDestination
import com.rabarka.milk.ui.milk.MilkDetailsScreen
import com.rabarka.milk.ui.milk.MilkEditDestination
import com.rabarka.milk.ui.milk.MilkEditScreen
import com.rabarka.milk.ui.milk.MilkEntryDestination
import com.rabarka.milk.ui.milk.MilkEntryScreen
import com.rabarka.milk.ui.onboarding.OnboardingDestination
import com.rabarka.milk.ui.onboarding.OnboardingScreen
import com.rabarka.milk.ui.settings.SettingsDestination
import com.rabarka.milk.ui.settings.SettingsScreen

@Composable
fun MilkNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val profileStore = remember { LedgerProfileStore(context) }
    val hasSetup = remember { profileStore.getAppSetup() != null }

    NavHost(
        navController = navController,
        startDestination = if (hasSetup) HomeDestination.route else OnboardingDestination.route,
        modifier = modifier
    ) {
        composable(route = OnboardingDestination.route) {
            OnboardingScreen(
                onContinueToApp = {
                    navController.navigate(HomeDestination.route) {
                        popUpTo(OnboardingDestination.route) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                }
            )
        }
        composable(route = HomeDestination.route) {
            val appSetup = profileStore.getAppSetup()
            HomeScreen(
                navigateToMilkEntry = { navController.navigate(MilkEntryDestination.route) },
                navigateToMilkUpdate = { navController.navigate("${MilkDetailsDestination.route}/${it}") },
                navigateToSettings = { navController.navigate(SettingsDestination.route) },
                appSetup = appSetup
            )
        }
        composable(route = MilkEntryDestination.route) {
            MilkEntryScreen(
                navigateBack = { navController.popBackStack() },
                onNavigateUp = { navController.navigateUp() })
        }

        composable(
            route = MilkDetailsDestination.routeWithArgs,
            arguments = listOf(navArgument(MilkDetailsDestination.milkIdArg) {
                type = NavType.IntType
            })
        ) {
            MilkDetailsScreen(
                navigateToEditMilk = { navController.navigate("${MilkEditDestination.route}/$it") },
                navigateBack = { navController.navigateUp() })
        }

        composable(
            route = MilkEditDestination.routeWithArgs,
            arguments = listOf(navArgument(MilkEditDestination.milkIdArg) {
                type = NavType.IntType
            })
        ) {
            MilkEditScreen(
                navigateBack = { navController.popBackStack() },
                onNavigateUp = { navController.navigateUp() })
        }

        composable(route = SettingsDestination.route) {
            SettingsScreen(
                navigateBack = { navController.navigateUp() }
            )
        }
    }
}
