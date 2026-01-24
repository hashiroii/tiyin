package kz.hashiroii.designsystem.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme = lightColorScheme(
    primary = TiyinPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE0E7FF),
    onPrimaryContainer = TiyinPrimary,
    secondary = TiyinSecondary,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFEDE9FE),
    onSecondaryContainer = TiyinSecondary,
    tertiary = Color(0xFFEC4899),
    onTertiary = Color.White,
    error = TiyinError,
    onError = Color.White,
    errorContainer = Color(0xFFFEE2E2),
    onErrorContainer = TiyinError,
    background = TiyinBackgroundLight,
    onBackground = TiyinOnSurfaceLight,
    surface = TiyinSurfaceLight,
    onSurface = TiyinOnSurfaceLight,
    surfaceVariant = Color(0xFFF3F4F6),
    onSurfaceVariant = Color(0xFF6B7280),
    outline = Color(0xFFE5E7EB),
    outlineVariant = Color(0xFFD1D5DB)
)

private val DarkColorScheme = darkColorScheme(
    primary = TiyinPrimaryDark,
    onPrimary = Color(0xFF1E1B4B),
    primaryContainer = Color(0xFF312E81),
    onPrimaryContainer = TiyinPrimaryDark,
    secondary = TiyinSecondaryDark,
    onSecondary = Color(0xFF3B1F5C),
    secondaryContainer = Color(0xFF4C1D95),
    onSecondaryContainer = TiyinSecondaryDark,
    tertiary = Color(0xFFF472B6),
    onTertiary = Color(0xFF831843),
    error = TiyinErrorDark,
    onError = Color(0xFF7F1D1D),
    errorContainer = Color(0xFF991B1B),
    onErrorContainer = TiyinErrorDark,
    background = TiyinBackgroundDark,
    onBackground = TiyinOnSurfaceDark,
    surface = TiyinSurfaceDark,
    onSurface = TiyinOnSurfaceDark,
    surfaceVariant = Color(0xFF334155),
    onSurfaceVariant = Color(0xFF94A3B8),
    outline = Color(0xFF475569),
    outlineVariant = Color(0xFF64748B)
)

@Composable
fun TiyinTheme(
    themePreference: String = "System",
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val isSystemDark = isSystemInDarkTheme()
    val darkTheme = when (themePreference) {
        "Dark" -> true
        "Light" -> false
        "System" -> isSystemDark
        else -> isSystemDark
    }
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = TiyinTypography,
        content = content
    )
}
