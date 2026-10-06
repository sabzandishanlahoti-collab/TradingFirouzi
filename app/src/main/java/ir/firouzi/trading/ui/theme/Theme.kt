package ir.firouzi.trading.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import ir.firouzi.trading.App
import ir.firouzi.trading.data.settings.ThemeMode

val BullGreen = Color(0xFF16A34A)
val BearRed = Color(0xFFDC2626)

private val DarkColors = darkColorScheme(
    primary = BullGreen,
    onPrimary = Color.White,
    secondary = Color(0xFF38BDF8),
    background = Color(0xFF0E1117),
    surface = Color(0xFF161B22),
    onBackground = Color(0xFFE6EDF3),
    onSurface = Color(0xFFE6EDF3),
    error = BearRed
)

private val LightColors = lightColorScheme(
    primary = BullGreen,
    onPrimary = Color.White,
    secondary = Color(0xFF0284C7),
    background = Color(0xFFF7F8FA),
    surface = Color.White,
    onBackground = Color(0xFF0E1117),
    onSurface = Color(0xFF0E1117),
    error = BearRed
)

@Composable
fun TradingFirouziTheme(content: @Composable () -> Unit) {
    val app = LocalContext.current.applicationContext as App
    val mode by app.settings.themeModeFlow.collectAsState(initial = ThemeMode.SYSTEM)

    val dark = when (mode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    MaterialTheme(
        colorScheme = if (dark) DarkColors else LightColors,
        typography = Typography(),
        content = content
    )
}
