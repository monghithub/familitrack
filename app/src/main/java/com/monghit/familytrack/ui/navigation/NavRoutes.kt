package com.monghit.familytrack.ui.navigation

sealed class NavRoutes(val route: String) {
    data object Home : NavRoutes("home")
    data object Map : NavRoutes("map")
    data object Family : NavRoutes("family")
    data object Settings : NavRoutes("settings")
    data object SafeZones : NavRoutes("safe_zones")
    data object Permissions : NavRoutes("permissions")
}
