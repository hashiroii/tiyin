package kz.hashiroii.tiyin.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import kz.hashiroii.analytics.navigation.analyticsScreen
import kz.hashiroii.groups.navigation.groupsScreen
import kz.hashiroii.home.navigation.homeScreen
import kz.hashiroii.navigation.Home
import kz.hashiroii.navigation.Profile
import kz.hashiroii.navigation.Settings
import kz.hashiroii.profile.navigation.profileScreen
import kz.hashiroii.settings.navigation.settingsScreen

@Composable
fun TiyinNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Home,
        modifier = modifier
    ) {
        homeScreen()
        analyticsScreen()
        groupsScreen()
        profileScreen(onBackClick = { navController.navigateUp() })
        settingsScreen()
    }
}
