package com.monghit.familytrack.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.monghit.familytrack.R
import kotlinx.coroutines.launch
import com.monghit.familytrack.data.repository.SecurityRepository
import com.monghit.familytrack.data.repository.SettingsRepository
import com.monghit.familytrack.ui.screens.family.FamilyScreen
import com.monghit.familytrack.ui.screens.familysetup.FamilySetupScreen
import com.monghit.familytrack.ui.screens.pin.PinScreen
import com.monghit.familytrack.ui.screens.home.HomeScreen
import com.monghit.familytrack.ui.screens.map.MapScreen
import com.monghit.familytrack.ui.screens.chat.ChatScreen
import com.monghit.familytrack.ui.screens.onboarding.OnboardingScreen
import com.monghit.familytrack.ui.screens.permissions.PermissionsScreen
import com.monghit.familytrack.ui.screens.photos.PhotosScreen
import com.monghit.familytrack.ui.screens.profile.ProfileScreen
import com.monghit.familytrack.ui.screens.routehistory.RouteHistoryScreen
import com.monghit.familytrack.ui.screens.splash.SplashScreen
import com.monghit.familytrack.ui.screens.safezones.SafeZonesScreen
import com.monghit.familytrack.ui.screens.settings.SettingsScreen

data class BottomNavItem(
    val route: String,
    val titleResId: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

val bottomNavItems = listOf(
    BottomNavItem(
        route = NavRoutes.Home.route,
        titleResId = R.string.nav_home,
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home
    ),
    BottomNavItem(
        route = NavRoutes.Map.route,
        titleResId = R.string.nav_map,
        selectedIcon = Icons.Filled.Map,
        unselectedIcon = Icons.Outlined.Map
    ),
    BottomNavItem(
        route = NavRoutes.Family.route,
        titleResId = R.string.nav_family,
        selectedIcon = Icons.Filled.People,
        unselectedIcon = Icons.Outlined.People
    ),
    BottomNavItem(
        route = NavRoutes.Settings.route,
        titleResId = R.string.nav_settings,
        selectedIcon = Icons.Filled.Settings,
        unselectedIcon = Icons.Outlined.Settings
    )
)

private val screensWithBottomBar = setOf(
    NavRoutes.Home.route,
    NavRoutes.Map.route,
    NavRoutes.Family.route,
    NavRoutes.Settings.route
)

@Composable
fun FamilyTrackNavHost(
    settingsRepository: SettingsRepository,
    securityRepository: SecurityRepository
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val familyId by settingsRepository.familyId.collectAsState(initial = 0)
    val onboardingCompleted by settingsRepository.onboardingCompleted.collectAsState(initial = true)

    val startDestination = NavRoutes.Splash.route
    val showBottomBar = currentDestination?.route in screensWithBottomBar

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomNavItems.forEach { item ->
                        val selected = currentDestination?.hierarchy?.any { it.route == item.route } == true

                        NavigationBarItem(
                            icon = {
                                Icon(
                                    imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                    contentDescription = stringResource(item.titleResId)
                                )
                            },
                            label = { Text(stringResource(item.titleResId)) },
                            selected = selected,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(NavRoutes.Splash.route) {
                SplashScreen(
                    onSplashFinished = {
                        val nextRoute = when {
                            !onboardingCompleted -> NavRoutes.Onboarding.route
                            familyId == 0 -> NavRoutes.FamilySetup.route
                            securityRepository.isPinSet() -> NavRoutes.PinLock.route
                            else -> NavRoutes.Home.route
                        }
                        navController.navigate(nextRoute) {
                            popUpTo(NavRoutes.Splash.route) { inclusive = true }
                        }
                    }
                )
            }
            composable(NavRoutes.Onboarding.route) {
                val scope = rememberCoroutineScope()
                OnboardingScreen(
                    onOnboardingComplete = {
                        scope.launch {
                            settingsRepository.setOnboardingCompleted(true)
                        }
                        navController.navigate(NavRoutes.FamilySetup.route) {
                            popUpTo(NavRoutes.Onboarding.route) { inclusive = true }
                        }
                    }
                )
            }
            composable(NavRoutes.PinLock.route) {
                PinScreen(
                    onAuthenticated = {
                        navController.navigate(NavRoutes.Home.route) {
                            popUpTo(NavRoutes.PinLock.route) { inclusive = true }
                        }
                    }
                )
            }
            composable(NavRoutes.FamilySetup.route) {
                FamilySetupScreen(
                    onSetupComplete = {
                        navController.navigate(NavRoutes.Home.route) {
                            popUpTo(NavRoutes.FamilySetup.route) { inclusive = true }
                        }
                    }
                )
            }
            composable(NavRoutes.Home.route) {
                HomeScreen()
            }
            composable(NavRoutes.Map.route) {
                MapScreen(
                    onNavigateToSafeZones = {
                        navController.navigate(NavRoutes.SafeZones.route)
                    }
                )
            }
            composable(NavRoutes.Family.route) {
                FamilyScreen()
            }
            composable(NavRoutes.Settings.route) {
                SettingsScreen(
                    onNavigateToProfile = {
                        navController.navigate(NavRoutes.Profile.route)
                    },
                    onNavigateToChat = {
                        navController.navigate(NavRoutes.Chat.route)
                    },
                    onNavigateToPermissions = {
                        navController.navigate(NavRoutes.Permissions.route)
                    }
                )
            }
            composable(NavRoutes.Profile.route) {
                ProfileScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable(NavRoutes.Chat.route) {
                ChatScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable(NavRoutes.SafeZones.route) {
                SafeZonesScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable(NavRoutes.Permissions.route) {
                PermissionsScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable(NavRoutes.Photos.route) {
                PhotosScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable(NavRoutes.RouteHistory.route) {
                RouteHistoryScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}
