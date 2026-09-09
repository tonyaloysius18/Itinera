package com.itinera.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily

enum class ThemeMode { SYSTEM, LIGHT, DARK }

/**
 * Cathopedia's editorial type treatment uses the platform serif family. Applying
 * it at the theme level gives Itinera the same font on every Material title,
 * paragraph and label while retaining the existing, accessible type scale.
 */
private val ItineraTypography = Typography().let { type ->
    type.copy(
        displayLarge = type.displayLarge.copy(fontFamily = FontFamily.Serif),
        displayMedium = type.displayMedium.copy(fontFamily = FontFamily.Serif),
        displaySmall = type.displaySmall.copy(fontFamily = FontFamily.Serif),
        headlineLarge = type.headlineLarge.copy(fontFamily = FontFamily.Serif),
        headlineMedium = type.headlineMedium.copy(fontFamily = FontFamily.Serif),
        headlineSmall = type.headlineSmall.copy(fontFamily = FontFamily.Serif),
        titleLarge = type.titleLarge.copy(fontFamily = FontFamily.Serif),
        titleMedium = type.titleMedium.copy(fontFamily = FontFamily.Serif),
        titleSmall = type.titleSmall.copy(fontFamily = FontFamily.Serif),
        bodyLarge = type.bodyLarge.copy(fontFamily = FontFamily.Serif),
        bodyMedium = type.bodyMedium.copy(fontFamily = FontFamily.Serif),
        bodySmall = type.bodySmall.copy(fontFamily = FontFamily.Serif),
        labelLarge = type.labelLarge.copy(fontFamily = FontFamily.Serif),
        labelMedium = type.labelMedium.copy(fontFamily = FontFamily.Serif),
        labelSmall = type.labelSmall.copy(fontFamily = FontFamily.Serif),
    )
}

// ---------------------------------------------------------------------------
// Material slots.
//
// Every slot is filled explicitly. Anything left unset falls back to Material's
// stock baseline — which is purple — and that default leaks into components we
// never styled by hand.
// ---------------------------------------------------------------------------

private val LightColors = lightColorScheme(
    primary = Brand400,
    onPrimary = Color.White,
    primaryContainer = Brand100,
    onPrimaryContainer = Brand800,
    inversePrimary = Brand100,

    secondary = Brand500,
    onSecondary = Color.White,
    secondaryContainer = Brand050,
    onSecondaryContainer = Brand800,

    tertiary = SuccessLight,
    onTertiary = Color.White,
    tertiaryContainer = SuccessLightContainer,
    onTertiaryContainer = SuccessLightOnContainer,

    background = NeutralLightBackground,
    onBackground = NeutralLightOn,
    surface = NeutralLightSurface,
    onSurface = NeutralLightOn,
    surfaceVariant = NeutralLightSurfaceVariant,
    onSurfaceVariant = NeutralLightOnVariant,
    surfaceTint = Brand400,
    inverseSurface = NeutralLightInverse,
    inverseOnSurface = NeutralLightInverseOn,

    surfaceBright = NeutralLightSurface,
    surfaceDim = NeutralLightDim,
    surfaceContainerLowest = NeutralLightSurface,
    surfaceContainerLow = NeutralLightBackground,
    surfaceContainer = NeutralLightContainer,
    surfaceContainerHigh = NeutralLightContainerHigh,
    surfaceContainerHighest = NeutralLightContainerHighest,

    error = ErrorLight,
    onError = Color.White,
    errorContainer = ErrorLightContainer,
    onErrorContainer = ErrorLightOnContainer,

    outline = NeutralLightOutline,
    outlineVariant = NeutralLightDivider,
    scrim = Color.Black,
)

private val DarkColors = darkColorScheme(
    primary = Brand100,
    onPrimary = Brand900,
    primaryContainer = Brand600,
    onPrimaryContainer = Brand050Bright,
    inversePrimary = Brand400,

    secondary = Brand200,
    onSecondary = BrandInk,
    secondaryContainer = Brand700,
    onSecondaryContainer = Brand075Bright,

    tertiary = SuccessDark,
    onTertiary = SuccessLightOnContainer,
    tertiaryContainer = SuccessDarkContainer,
    onTertiaryContainer = SuccessLightContainer,

    background = NeutralDarkBackground,
    onBackground = NeutralDarkOn,
    surface = NeutralDarkSurface,
    onSurface = NeutralDarkOn,
    surfaceVariant = NeutralDarkSurfaceVariant,
    onSurfaceVariant = NeutralDarkOnVariant,
    surfaceTint = Brand100,
    inverseSurface = NeutralDarkOn,
    inverseOnSurface = NeutralDarkSurface,

    surfaceBright = NeutralDarkBright,
    surfaceDim = NeutralDarkBackground,
    surfaceContainerLowest = NeutralDarkContainerLowest,
    surfaceContainerLow = NeutralDarkContainerLow,
    surfaceContainer = NeutralDarkSurface,
    surfaceContainerHigh = NeutralDarkContainerHigh,
    surfaceContainerHighest = NeutralDarkContainerHighest,

    error = ErrorDark,
    onError = ErrorDarkOn,
    errorContainer = ErrorDarkContainer,
    onErrorContainer = ErrorLightContainer,

    outline = NeutralDarkOutline,
    outlineVariant = NeutralDarkDivider,
    scrim = Color.Black,
)

// ---------------------------------------------------------------------------
// Itinera's own tokens — the things Material has no slot for.
// ---------------------------------------------------------------------------

@Immutable
data class ItineraColors(
    val isDark: Boolean,

    val success: Color,
    val successContainer: Color,
    val destructive: Color,
    val divider: Color,
    val expenseCardSurface: Color,

    val categoryAccommodation: Color,
    val categoryFood: Color,
    val categoryTransport: Color,
    val categoryShopping: Color,
    val categoryActivities: Color,
    val categoryOther: Color,

    val accentBlue: Color,
    val accentGreen: Color,
    val accentCoral: Color,
    val accentPurple: Color,

    val tempFreezing: Color,
    val tempCold: Color,
    val tempMild: Color,
    val tempWarm: Color,
    val tempHot: Color,
    val tempScorching: Color,

    val skyClear: Color,
    val skyCloudy: Color,
    val skyFog: Color,
    val skyRain: Color,
    val skySnow: Color,
    val skyStorm: Color,

    val actionPin: Color,
    val actionEdit: Color,
    val actionArchive: Color,
    val actionDelete: Color,

    val dayTint: Color,
    val nightTint: Color,
    val star: Color,
    val strengthWeak: Color,
    val strengthFair: Color,

    val docPdf: Color,
    val docImage: Color,
    val docOther: Color,

    val avatarPalette: List<Color>,

    /** Chrome behind the bottom navigation pill. */
    val navBarSurface: Color,
    val navBarBorder: Color,
    val navPillSurface: Color,
    val navIconSelected: Color,
    val navIconUnselected: Color,
)

private val LightItineraColors = ItineraColors(
    isDark = false,
    success = SuccessLight,
    successContainer = SuccessLightContainer,
    destructive = DestructiveLight,
    divider = NeutralLightDivider,
    expenseCardSurface = ExpenseCardLight,

    categoryAccommodation = CategoryAccommodationLight,
    categoryFood = CategoryFoodLight,
    categoryTransport = CategoryTransportLight,
    categoryShopping = CategoryShoppingLight,
    categoryActivities = CategoryActivitiesLight,
    categoryOther = CategoryOtherLight,

    accentBlue = AccentBlueLight,
    accentGreen = AccentGreenLight,
    accentCoral = AccentCoralLight,
    accentPurple = AccentPurpleLight,

    tempFreezing = TempFreezingLight,
    tempCold = TempColdLight,
    tempMild = TempMildLight,
    tempWarm = TempWarmLight,
    tempHot = TempHotLight,
    tempScorching = TempScorchingLight,

    skyClear = SkyClearLight,
    skyCloudy = SkyCloudyLight,
    skyFog = SkyFogLight,
    skyRain = SkyRainLight,
    skySnow = SkySnowLight,
    skyStorm = SkyStormLight,

    actionPin = ActionPinLight,
    actionEdit = ActionEditLight,
    actionArchive = ActionArchiveLight,
    actionDelete = ActionDeleteLight,

    dayTint = DayTintLight,
    nightTint = NightTintLight,
    star = StarLight,
    strengthWeak = StrengthWeakLight,
    strengthFair = StrengthFairLight,

    docPdf = DocPdfLight,
    docImage = DocImageLight,
    docOther = DocOtherLight,

    avatarPalette = AvatarPaletteShared,

    navBarSurface = NavBarLight,
    navBarBorder = NavBorderLight,
    navPillSurface = NavPillLight,
    navIconSelected = Brand500,
    navIconUnselected = NeutralLightOnVariant,
)

private val DarkItineraColors = ItineraColors(
    isDark = true,
    success = SuccessDark,
    successContainer = SuccessDarkContainer,
    destructive = DestructiveDark,
    divider = NeutralDarkDivider,
    expenseCardSurface = ExpenseCardDark,

    categoryAccommodation = CategoryAccommodationDark,
    categoryFood = CategoryFoodDark,
    categoryTransport = CategoryTransportDark,
    categoryShopping = CategoryShoppingDark,
    categoryActivities = CategoryActivitiesDark,
    categoryOther = CategoryOtherDark,

    accentBlue = AccentBlueDark,
    accentGreen = AccentGreenDark,
    accentCoral = AccentCoralDark,
    accentPurple = AccentPurpleDark,

    tempFreezing = TempFreezingDark,
    tempCold = TempColdDark,
    tempMild = TempMildDark,
    tempWarm = TempWarmDark,
    tempHot = TempHotDark,
    tempScorching = TempScorchingDark,

    skyClear = SkyClearDark,
    skyCloudy = SkyCloudyDark,
    skyFog = SkyFogDark,
    skyRain = SkyRainDark,
    skySnow = SkySnowDark,
    skyStorm = SkyStormDark,

    actionPin = ActionPinDark,
    actionEdit = ActionEditDark,
    actionArchive = ActionArchiveDark,
    actionDelete = ActionDeleteDark,

    dayTint = DayTintDark,
    nightTint = NightTintDark,
    star = StarDark,
    strengthWeak = StrengthWeakDark,
    strengthFair = StrengthFairDark,

    docPdf = DocPdfDark,
    docImage = DocImageDark,
    docOther = DocOtherDark,

    avatarPalette = AvatarPaletteShared,

    navBarSurface = NavBarDark,
    navBarBorder = NavBorderDark,
    navPillSurface = NavPillDark,
    navIconSelected = Brand100,
    navIconUnselected = NeutralDarkOnVariant,
)

val LocalItineraColors = staticCompositionLocalOf { LightItineraColors }

/**
 * Itinera's extended tokens, alongside [MaterialTheme.colorScheme].
 *
 * Read `MaterialTheme.itinera.isDark` rather than inspecting a colour's channels —
 * inferring the mode from a surface colour breaks the moment that surface changes.
 */
val MaterialTheme.itinera: ItineraColors
    @Composable
    @ReadOnlyComposable
    get() = LocalItineraColors.current

@Composable
fun ItineraTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        LocalItineraColors provides if (darkTheme) DarkItineraColors else LightItineraColors,
    ) {
        MaterialTheme(
            colorScheme = if (darkTheme) DarkColors else LightColors,
            typography = ItineraTypography,
            content = content,
        )
    }
}
