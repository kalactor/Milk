package com.rabarka.milk.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.rabarka.milk.ui.home.HomeDestination
import com.rabarka.milk.ui.home.HomeScreen
import com.rabarka.milk.ui.milk.MilkDetailsDestination
import com.rabarka.milk.ui.milk.MilkDetailsScreen
import com.rabarka.milk.ui.milk.MilkEditDestination
import com.rabarka.milk.ui.milk.MilkEditScreen
import com.rabarka.milk.ui.milk.MilkEntryDestination
import com.rabarka.milk.ui.milk.MilkEntryScreen

@Composable
fun MilkNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = HomeDestination.route,
        modifier = modifier
    ) {
        composable(route = HomeDestination.route) {
            HomeScreen(
                navigateToMilkEntry = { navController.navigate(MilkEntryDestination.route) },
                navigateToMilkUpdate = { navController.navigate("${MilkDetailsDestination.route}/${it}") }
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
    }
}