package kz.hashiroii.tiyin.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavDestination
import androidx.navigation.NavHostController
import kz.hashiroii.navigation.Analytics
import kz.hashiroii.navigation.Groups
import kz.hashiroii.navigation.Home
import kz.hashiroii.navigation.Profile
import kz.hashiroii.navigation.Settings
import kz.hashiroii.navigation.TiyinDestination

@Composable
fun rememberTiyinAppState(
    navController: NavHostController
): TiyinAppState {
    return remember(navController) {
        TiyinAppState(navController)
    }
}

class TiyinAppState(
    val navController: NavHostController
) {
    val currentDestination: NavDestination?
        get() = navController.currentDestination

    val currentTopLevelDestination: TiyinDestination?
        get() {
            val route = currentDestination?.route ?: return null
            return when {
                route.contains("Home") -> Home
                route.contains("Analytics") -> Analytics
                route.contains("Groups") -> Groups
                else -> null
            }
        }

    val shouldShowBottomBar: Boolean
        get() = currentTopLevelDestination != null

    val shouldShowSettingsIcons: Boolean
        get() = currentTopLevelDestination != null

    val shouldShowBackButton: Boolean
        get() {
            val route = currentDestination?.route ?: return false
            return route.contains("Profile") || route.contains("Settings")
        }
}
