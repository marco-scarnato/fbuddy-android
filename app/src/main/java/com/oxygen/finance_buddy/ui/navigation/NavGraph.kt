package com.oxygen.finance_buddy.ui.navigation

import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.oxygen.finance_buddy.ui.auth.AuthScreen
import com.oxygen.finance_buddy.ui.conti.ContiScreen
import com.oxygen.finance_buddy.ui.dashboard.DashboardScreen
import com.oxygen.finance_buddy.ui.settings.SettingsScreen
import com.oxygen.finance_buddy.ui.spese.SpeseScreen
import com.oxygen.finance_buddy.ui.spese.SpeseDetailScreen
import com.oxygen.finance_buddy.ui.splash.SplashScreen
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier

@Composable
fun FinanceBuddyNavGraph(
    navController: NavHostController = rememberNavController(),
    startDestination: String = Screen.Splash.route
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showBottomBar = currentRoute in listOf(Screen.Dashboard.route, Screen.Conti.route, Screen.Spese.route, Screen.Settings.route)

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Home, contentDescription = "Dashboard") },
                        label = { Text("Home") },
                        selected = currentRoute == Screen.Dashboard.route,
                        onClick = { navController.navigate(Screen.Dashboard.route) { popUpTo(Screen.Dashboard.route) { inclusive = true } } }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.AccountBalance, contentDescription = "Conti") },
                        label = { Text("Conti") },
                        selected = currentRoute == Screen.Conti.route,
                        onClick = { navController.navigate(Screen.Conti.route) { popUpTo(Screen.Dashboard.route) } }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.ShoppingCart, contentDescription = "Spese") },
                        label = { Text("Spese") },
                        selected = currentRoute == Screen.Spese.route,
                        onClick = { navController.navigate(Screen.Spese.route) { popUpTo(Screen.Dashboard.route) } }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Settings, contentDescription = "Impostazioni") },
                        label = { Text("Impostazioni") },
                        selected = currentRoute == Screen.Settings.route,
                        onClick = { navController.navigate(Screen.Settings.route) { popUpTo(Screen.Dashboard.route) } }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Splash.route) {
                SplashScreen(
                    onNavigateToAuth = {
                        navController.navigate(Screen.Auth.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    },
                    onNavigateToDashboard = {
                        navController.navigate(Screen.Dashboard.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    }
                )
            }
            composable(Screen.Auth.route) {
                AuthScreen(
                    onAuthSuccess = {
                        navController.navigate(Screen.Dashboard.route) {
                            popUpTo(Screen.Auth.route) { inclusive = true }
                        }
                    }
                )
            }
            composable(Screen.Dashboard.route) {
                DashboardScreen()
            }
            composable(Screen.Conti.route) {
                ContiScreen()
            }
            composable(Screen.Spese.route) {
                SpeseScreen(onCardClick = { cardId -> navController.navigate(Screen.SpeseDetail.createRoute(cardId)) })
            }
            composable(
                route = Screen.SpeseDetail.route,
                arguments = listOf(navArgument("cardId") { type = NavType.IntType })
            ) {
                SpeseDetailScreen(onNavigateBack = { navController.popBackStack() })
            }
            composable(Screen.Settings.route) {
                SettingsScreen()
            }
        }
    }
}
