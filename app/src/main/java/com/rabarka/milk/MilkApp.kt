package com.rabarka.milk

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.rabarka.milk.ui.navigation.MilkNavHost

@Composable
fun MilkApp(navController: NavHostController = rememberNavController()) {
    MilkNavHost(navController = navController)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MilkTopAppBar(
    title: String,
    canNavigateBack: Boolean,
    modifier: Modifier = Modifier,
    isHomeScreen: Boolean = false,
    scrollBehavior: TopAppBarScrollBehavior? = null,
    navigateUp: () -> Unit = {},
    onMoneyClick:  () -> Unit = {},
    onDateRangeClick: () -> Unit = {}

) {
    CenterAlignedTopAppBar(title = {
        Text(
            text = title,
            fontSize = 36.sp,
            fontWeight = FontWeight.Medium
        )
    },
        modifier = modifier,
        scrollBehavior = scrollBehavior,
        navigationIcon = {
            if (canNavigateBack) {
                IconButton(onClick = navigateUp) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = null
                    )
                }
            }
        },
        actions = {
            if (isHomeScreen) {
                IconButton(onClick = onDateRangeClick) {
                    Icon(
                        painter = painterResource(id = R.drawable.date_range),
                        contentDescription = null
                    )
                }
                IconButton(onClick = onMoneyClick) {
                    Icon(
                        painter = painterResource(id = R.drawable.money),
                        contentDescription = null
                    )
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun TopBarPreview(modifier: Modifier = Modifier) {
    MilkTopAppBar(
        title = "Milk",
        canNavigateBack = false,
        onDateRangeClick = {},
        onMoneyClick = {},
        isHomeScreen = true
    )
}