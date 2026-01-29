package kz.hashiroii.subscriptionmanager.navigation

import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import kz.hashiroii.navigation.AddSubscription
import kz.hashiroii.navigation.EditSubscription
import kz.hashiroii.subscriptionmanager.SubscriptionManagerScreenRoute

fun NavGraphBuilder.subscriptionManagerScreen(
    onBackClick: () -> Unit,
    onDeleteSubscriptionReady: ((() -> Unit) -> Unit)? = null
) {
    composable<AddSubscription> { backStackEntry: NavBackStackEntry ->
        val addSubscription: AddSubscription = backStackEntry.toRoute()
        SubscriptionManagerScreenRoute(
            subscriptionId = addSubscription.subscriptionId,
            onBackClick = onBackClick
        )
    }
    
    composable<EditSubscription> { backStackEntry: NavBackStackEntry ->
        val editSubscription: EditSubscription = backStackEntry.toRoute()
        SubscriptionManagerScreenRoute(
            serviceName = editSubscription.serviceName,
            serviceDomain = editSubscription.serviceDomain,
            onBackClick = onBackClick,
            onDeleteSubscriptionReady = onDeleteSubscriptionReady
        )
    }
}
