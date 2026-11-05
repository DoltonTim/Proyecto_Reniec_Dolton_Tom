package pe.edu.upeu.reniecmovil.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val ReniecCrimson = Color(0xFF8B1538)
val ReniecCrimsonDark = Color(0xFF5D0B24)
val ReniecNavy = Color(0xFF1A365D)
val ReniecGold = Color(0xFFD97706)
val ReniecGreen = Color(0xFF15803D)

private val LightColorScheme = lightColorScheme(
    primary = ReniecCrimson,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFD9E0),
    onPrimaryContainer = Color(0xFF3A0013),
    secondary = ReniecNavy,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD6E3FF),
    onSecondaryContainer = Color(0xFF001B3E),
    tertiary = ReniecGold,
    background = Color(0xFFFDF8F9),
    surface = Color.White,
    surfaceVariant = Color(0xFFF4ECEE)
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFFFB1C3),
    onPrimary = Color(0xFF54001F),
    primaryContainer = Color(0xFF70082B),
    onPrimaryContainer = Color(0xFFFFD9E0),
    secondary = Color(0xFFA9C7FF),
    onSecondary = Color(0xFF003063),
    secondaryContainer = Color(0xFF1E4373),
    onSecondaryContainer = Color(0xFFD6E3FF),
    tertiary = Color(0xFFFBBF24),
    background = Color(0xFF191113),
    surface = Color(0xFF23191B),
    surfaceVariant = Color(0xFF36282B)
)

@Composable
fun ReniecMovilTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
