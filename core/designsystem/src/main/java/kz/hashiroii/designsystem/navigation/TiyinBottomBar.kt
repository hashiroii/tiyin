package kz.hashiroii.designsystem.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import kz.hashiroii.navigation.Analytics
import kz.hashiroii.navigation.Groups
import kz.hashiroii.navigation.Home
import kz.hashiroii.navigation.TiyinDestination

data class BottomNavItem(
    val destination: TiyinDestination,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val label: String,
    val contentDescription: String
)

@Composable
fun TiyinBottomBar(
    currentDestination: TiyinDestination?,
    homeLabel: String,
    analyticsLabel: String,
    groupsLabel: String,
    onNavigate: (TiyinDestination) -> Unit
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surfaceContainer
    ) {
        NavigationBarItem(
            selected = currentDestination is Home,
            onClick = { onNavigate(Home) },
            icon = {
                Icon(
                    imageVector = Icons.Default.Dashboard,
                    contentDescription = homeLabel,
                    tint = if (currentDestination is Home) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            },
            label = {
                Text(
                    text = homeLabel,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        )
        NavigationBarItem(
            selected = currentDestination is Analytics,
            onClick = { onNavigate(Analytics) },
            icon = {
                Icon(
                    imageVector = Icons.Default.BarChart,
                    contentDescription = analyticsLabel,
                    tint = if (currentDestination is Analytics) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            },
            label = {
                Text(
                    text = analyticsLabel,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        )
        NavigationBarItem(
            selected = currentDestination is Groups,
            onClick = { onNavigate(Groups) },
            icon = {
                Icon(
                    imageVector = Icons.Default.Groups,
                    contentDescription = groupsLabel,
                    tint = if (currentDestination is Groups) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            },
            label = {
                Text(
                    text = groupsLabel,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        )
    }
}
