package kz.hashiroii.home.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kz.hashiroii.home.HomeScreenRoute
import kz.hashiroii.navigation.AddSubscription
import kz.hashiroii.navigation.EditSubscription
import kz.hashiroii.navigation.Home

fun NavGraphBuilder.homeScreen(
    onAddSubscriptionClick: () -> Unit,
    onEditSubscriptionClick: (String, String) -> Unit
) {
    composable<Home> {
        HomeScreenRoute(
            onAddSubscriptionClick = onAddSubscriptionClick,
            onEditSubscriptionClick = onEditSubscriptionClick
        )
    }
}
