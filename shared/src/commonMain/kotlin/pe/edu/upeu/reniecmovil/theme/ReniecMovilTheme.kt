package pe.edu.upeu.reniecmovil.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val ReniecCrimson = Color(0xFF8B1538)
val ReniecCrimsonDark = Color(0xFF5D0B24)
val ReniecNavy = Color(0xFF1A365D)
val ReniecGold = Color(0xFFD97706)
val ReniecGreen = Color(0xFF15803D)

private val LightColorScheme = lightColorScheme(
    primary = ReniecCrimson,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFD9E0),
    onPrimaryContainer = Color(0xFF2B000D),
    secondary = ReniecNavy,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD6E3FF),
    onSecondaryContainer = Color(0xFF001838),
    tertiary = ReniecGold,
    background = Color(0xFFF8F9FA),
    onBackground = Color(0xFF111827),
    surface = Color.White,
    onSurface = Color(0xFF111827),
    surfaceVariant = Color(0xFFEEF0F4),
    onSurfaceVariant = Color(0xFF1F2937)
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
    background = Color(0xFF121214),
    onBackground = Color(0xFFF9FAFB),
    surface = Color(0xFF1C1C1F),
    onSurface = Color(0xFFF9FAFB),
    surfaceVariant = Color(0xFF2C2C30),
    onSurfaceVariant = Color(0xFFE5E7EB)
)

private val ReniecSharpTypography = Typography(
    headlineMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 26.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 21.sp,
        lineHeight = 27.sp
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.1.sp
    ),
    titleSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        lineHeight = 22.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 23.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 15.sp,
        lineHeight = 21.sp
    ),
    bodySmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 13.5.sp,
        lineHeight = 19.sp
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 14.5.sp,
        lineHeight = 20.sp
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 13.sp,
        lineHeight = 18.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp,
        lineHeight = 16.sp
    )
)

@Composable
fun ReniecMovilTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = ReniecSharpTypography,
        content = content
    )
}
