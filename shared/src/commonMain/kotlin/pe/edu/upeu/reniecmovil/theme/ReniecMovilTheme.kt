package pe.edu.upeu.reniecmovil.theme

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

// Paleta "The Stori / RENIEC Cyber-Purple & Neon Lime"
val ReniecCrimson = Color(0xFF7B0D54)
val ReniecCrimsonDark = Color(0xFF4A0635)
val ReniecNavy = Color(0xFF40C4FF)
val ReniecGold = Color(0xFFC6FF00)
val ReniecGreen = Color(0xFF00E676)

val StoriPurpleTop = Color(0xFF8C0B42)
val StoriPurpleMid = Color(0xFF640D5F)
val StoriDarkBg = Color(0xFF160821)
val StoriDarkCard = Color(0xFF231033)
val StoriDarkCardVariant = Color(0xFF311747)
val StoriNeonLime = Color(0xFFC6FF00)
val StoriNeonTextOnLime = Color(0xFF14071E)
val StoriLavenderMuted = Color(0xFFC9B6D9)

private val DarkColorScheme = darkColorScheme(
    primary = StoriNeonLime,
    onPrimary = StoriNeonTextOnLime,
    primaryContainer = Color(0xFF4B125E),
    onPrimaryContainer = Color.White,
    secondary = Color(0xFF40C4FF),
    onSecondary = Color(0xFF00263A),
    secondaryContainer = Color(0xFF2E1647),
    onSecondaryContainer = Color(0xFFEEDCFF),
    tertiary = Color(0xFFFFB300),
    onTertiary = Color(0xFF261600),
    tertiaryContainer = Color(0xFF4A2545),
    onTertiaryContainer = Color(0xFFFFD8EC),
    background = StoriDarkBg,
    onBackground = Color.White,
    surface = StoriDarkCard,
    onSurface = Color.White,
    surfaceVariant = StoriDarkCardVariant,
    onSurfaceVariant = StoriLavenderMuted,
    error = Color(0xFFFF5252),
    onError = Color(0xFF2D0004),
    errorContainer = Color(0xFF5C1127),
    onErrorContainer = Color(0xFFFFCDD2)
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF640D5F),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFF3D9FA),
    onPrimaryContainer = Color(0xFF28002E),
    secondary = Color(0xFF8C0B42),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFD9E3),
    onSecondaryContainer = Color(0xFF3A0019),
    tertiary = Color(0xFF557000),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFD8FF66),
    onTertiaryContainer = Color(0xFF172100),
    background = Color(0xFFF6F2F9),
    onBackground = Color(0xFF1A0926),
    surface = Color.White,
    onSurface = Color(0xFF1A0926),
    surfaceVariant = Color(0xFFEDE4F4),
    onSurfaceVariant = Color(0xFF3E2753)
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
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = ReniecSharpTypography,
        content = content
    )
}
