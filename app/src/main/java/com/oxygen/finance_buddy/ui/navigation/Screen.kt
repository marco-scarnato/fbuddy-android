package com.oxygen.finance_buddy.ui.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Auth : Screen("auth")
    object Dashboard : Screen("dashboard")
    object Conti : Screen("conti")
    object Spese : Screen("spese")
    object SpeseDetail : Screen("spese_detail/{cardId}") {
        fun createRoute(cardId: Int) = "spese_detail/$cardId"
    }
    object Statistiche : Screen("statistiche")
    object Settings : Screen("settings")
}
