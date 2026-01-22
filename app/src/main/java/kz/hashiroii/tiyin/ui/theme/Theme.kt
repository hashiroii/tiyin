package kz.hashiroii.tiyin.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import kz.hashiroii.designsystem.theme.TiyinTheme as DesignSystemTiyinTheme

@Composable
fun TiyinTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    DesignSystemTiyinTheme(
        darkTheme = darkTheme,
        dynamicColor = dynamicColor
    ) {
        val view = LocalView.current
        val context = LocalContext.current
        if (!view.isInEditMode && context is Activity) {
            val window = context.window
            val colorScheme = androidx.compose.material3.MaterialTheme.colorScheme
            SideEffect {
                window.statusBarColor = colorScheme.background.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            }
        }
        content()
    }
}