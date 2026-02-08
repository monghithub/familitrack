package com.monghit.familytrack.ui.navigation

sealed class NavRoutes(val route: String) {
    data object Home : NavRoutes("home")
    data object Map : NavRoutes("map")
    data object Family : NavRoutes("family")
    data object Settings : NavRoutes("settings")
    data object SafeZones : NavRoutes("safe_zones")
    data object Permissions : NavRoutes("permissions")
    data object FamilySetup : NavRoutes("family_setup")
    data object PinLock : NavRoutes("pin_lock")
    data object Profile : NavRoutes("profile")
    data object Chat : NavRoutes("chat")
    data object Splash : NavRoutes("splash")
    data object Onboarding : NavRoutes("onboarding")
    data object Photos : NavRoutes("photos")
    data object RouteHistory : NavRoutes("route_history")
}
