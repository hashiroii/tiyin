package kz.hashiroii.designsystem.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TiyinTopAppBar(
    title: String,
    showBackButton: Boolean = false,
    showProfileButton: Boolean = false,
    showSettingsButton: Boolean = false,
    backContentDescription: String = "Back",
    profileContentDescription: String = "Profile",
    settingsContentDescription: String = "Settings",
    onBackClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {}
) {
    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge
            )
        },
        navigationIcon = {
            if (showBackButton) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = backContentDescription
                    )
                }
            }
        },
        actions = {
            if (showProfileButton) {
                IconButton(onClick = onProfileClick) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = profileContentDescription
                    )
                }
            }
            if (showSettingsButton) {
                IconButton(onClick = onSettingsClick) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = settingsContentDescription
                    )
                }
            }
        }
    )
}
