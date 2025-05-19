package com.example.adventure.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = DarkSkyBluePrimaryDark,
    onPrimary = OnDarkSkyBluePrimaryDark,
    primaryContainer = DarkSkyBluePrimaryContainerDark,
    onPrimaryContainer = Color.Black,

    secondary = LightBlueSecondaryDark,
    onSecondary = OnLightBlueSecondaryDark,
    secondaryContainer = LightBlueSecondaryContainerDark,
    onSecondaryContainer = Color.White,

    tertiary = YellowAccentTertiaryDark,
    onTertiary = OnYellowAccentTertiaryDark,
    tertiaryContainer = YellowAccentTertiaryContainerDark,
    onTertiaryContainer = Color.Black,

    error = ErrorDark,
    onError = OnErrorDark,
    errorContainer = Color(0xFFB3261E),
    onErrorContainer = Color.White,

    background = BackgroundDark,
    onBackground = OnBackgroundDark,

    surface = SurfaceDark,
    onSurface = OnSurfaceDark,

    surfaceVariant = Color(0xFF49454F),
    onSurfaceVariant = OnSurfaceVariantDark,

    outline = OutlineDark
)

private val LightColorScheme = lightColorScheme(
    primary = SkyBluePrimaryLight,
    onPrimary = OnSkyBluePrimaryLight,
    primaryContainer = SkyBluePrimaryContainerLight,
    onPrimaryContainer = OnSkyBluePrimaryLight,

    secondary = DeepBlueSecondaryLight,
    onSecondary = OnDeepBlueSecondaryLight,
    secondaryContainer = DeepBlueSecondaryContainerLight,
    onSecondaryContainer = Color.Black,

    tertiary = OrangeAccentTertiaryLight,
    onTertiary = OnOrangeAccentTertiaryLight,
    tertiaryContainer = OrangeAccentTertiaryContainerLight,
    onTertiaryContainer = Color.Black,

    error = ErrorLight,
    onError = OnErrorLight,
    errorContainer = Color(0xFFF9DEDC),
    onErrorContainer = Color(0xFF410E0B),

    background = BackgroundLight,
    onBackground = OnBackgroundLight,

    surface = SurfaceLight,
    onSurface = OnSurfaceLight,

    surfaceVariant = Color(0xFFE7E0EC),
    onSurfaceVariant = OnSurfaceVariantLight,

    outline = OutlineLight
)

@Composable
fun AdventureTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = AppShapes,
        content = content
    )
}