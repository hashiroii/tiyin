package kz.hashiroii.tiyin

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import kz.hashiroii.data.network.NetworkMonitor
import kz.hashiroii.designsystem.navigation.TiyinBottomBar
import kz.hashiroii.designsystem.navigation.TiyinTopAppBar
import kz.hashiroii.designsystem.theme.TiyinTheme
import kz.hashiroii.domain.usecase.preferences.GetPreferencesUseCase
import kz.hashiroii.navigation.Analytics
import kz.hashiroii.navigation.Groups
import kz.hashiroii.navigation.Home
import kz.hashiroii.navigation.Profile
import kz.hashiroii.navigation.Settings
import kz.hashiroii.tiyin.navigation.TiyinNavHost
import kz.hashiroii.tiyin.ui.rememberTiyinAppState
import kz.hashiroii.ui.LocaleHelper
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    @Inject
    lateinit var networkMonitor: NetworkMonitor
    
    @Inject
    lateinit var getPreferencesUseCase: GetPreferencesUseCase
    
    override fun attachBaseContext(newBase: Context) {
        val prefs = newBase.getSharedPreferences("settings", Context.MODE_PRIVATE)
        val languageCode = prefs.getString("language", "") ?: ""
        
        val wrappedContext = if (languageCode.isNotEmpty() && languageCode != "System") {
            LocaleHelper.wrap(newBase, languageCode)
        } else {
            newBase
        }
        
        super.attachBaseContext(wrappedContext)
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TiyinAppWithTheme(getPreferencesUseCase = getPreferencesUseCase)
        }
    }
}

@Composable
private fun TiyinAppWithTheme(
    getPreferencesUseCase: GetPreferencesUseCase
) {
    val preferencesState by getPreferencesUseCase().collectAsState(
        initial = null
    )
    
    preferencesState?.let { prefs ->
        TiyinTheme(themePreference = prefs.theme) {
            TiyinApp()
        }
    }
}

@Composable
fun TiyinApp() {
    val navController = rememberNavController()
    val appState = rememberTiyinAppState(navController)
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    
    val currentDestination = navBackStackEntry?.destination
    val currentTopLevelDestination = appState.currentTopLevelDestination
    val shouldShowBottomBar = appState.shouldShowBottomBar
    val shouldShowBackButton = appState.shouldShowBackButton
    
    val topBarTitle = when (currentTopLevelDestination) {
        is Home -> stringResource(id = R.string.nav_home)
        is Analytics -> stringResource(id = R.string.nav_analytics)
        is Groups -> stringResource(id = R.string.nav_groups)
        else -> when {
            currentDestination?.route?.contains("Profile") == true -> 
                stringResource(id = R.string.nav_profile)
            currentDestination?.route?.contains("Settings") == true -> 
                stringResource(id = R.string.nav_settings)
            else -> stringResource(id = R.string.app_name)
        }
    }
    
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TiyinTopAppBar(
                title = topBarTitle,
                showBackButton = shouldShowBackButton,
                showProfileButton = appState.shouldShowSettingsIcons && !shouldShowBackButton,
                showSettingsButton = appState.shouldShowSettingsIcons && !shouldShowBackButton,
                backContentDescription = stringResource(id = R.string.nav_back),
                profileContentDescription = stringResource(id = R.string.nav_profile),
                settingsContentDescription = stringResource(id = R.string.nav_settings),
                onBackClick = { navController.navigateUp() },
                onProfileClick = { navController.navigate(Profile) },
                onSettingsClick = { navController.navigate(Settings) }
            )
        },
        bottomBar = {
            if (shouldShowBottomBar) {
                TiyinBottomBar(
                    currentDestination = currentTopLevelDestination,
                    homeLabel = stringResource(id = R.string.nav_home),
                    analyticsLabel = stringResource(id = R.string.nav_analytics),
                    groupsLabel = stringResource(id = R.string.nav_groups),
                    onNavigate = { destination ->
                        navController.navigate(destination) {
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        TiyinNavHost(
            navController = navController,
            modifier = Modifier.padding(innerPadding)
        )
    }
}
