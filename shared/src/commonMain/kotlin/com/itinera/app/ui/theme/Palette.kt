package com.itinera.app.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Raw colour values for Itinera. Nothing outside this package should reference these
 * directly — build a token in [Theme.kt] and use that instead, so both light and dark
 * always get a considered answer.
 *
 * Every text/background pairing exposed as a token is WCAG-verified: normal text meets
 * AA (4.5:1), body copy on surfaces meets AAA (7:1), and borders that carry meaning meet
 * the 3:1 non-text minimum.
 */

// ---------------------------------------------------------------------------
// Brand palette: three source swatches — mint teal #70D6C5, leaf green #CAE5BC, pale sage #E1EDD4.
//
// Only #70D6C5 has enough contrast on black to be a dark-mode primary (11.5:1); none of the three
// clears 4.5:1 against white, so light mode needs deeper partners for text and fills. Each deep
// value is the swatch's own hue darkened just far enough: MintDeep 5.5:1, LeafDeep 6.3:1 on white.
// ---------------------------------------------------------------------------

// Mint teal — the primary.
internal val Mint = Color(0xFF70D6C5)                    // source swatch; dark-mode primary
internal val MintDeep = Color(0xFF0B7669)                // light-mode primary (white text 5.5:1)
internal val MintContainerLight = Color(0xFFBFEBE2)      // a lighter tint of the swatch
internal val MintInk = Color(0xFF0A3A33)                 // text on the light container (9.7:1)
internal val MintDarkContainer = Color(0xFF0E4A43)
internal val MintDarkOnContainer = Color(0xFFCFF5EC)     // 8.6:1 on MintDarkContainer
internal val MintDarkInk = Color(0xFF00332D)             // text on Mint (8.0:1)

// Leaf green — the secondary.
internal val Leaf = Color(0xFFCAE5BC)                    // source swatch; light container and dark-mode secondary
internal val LeafDeep = Color(0xFF3E6B2E)                // light-mode secondary (white text 6.3:1)
internal val LeafInk = Color(0xFF1B3013)                 // text on Leaf (10.4:1)
internal val LeafDarkContainer = Color(0xFF2D4525)
internal val LeafDarkInk = Color(0xFF1E3316)             // text on Leaf in dark mode (10.0:1)

// Pale sage — the tertiary and the tint behind cards.
internal val Sage = Color(0xFFE1EDD4)                    // source swatch
internal val SageDeep = Color(0xFF2F6B4F)                // light-mode tertiary
internal val SageInk = Color(0xFF24371B)                 // text on Sage (10.5:1)
internal val SageDarkContainer = Color(0xFF34472B)

// ---------------------------------------------------------------------------
// Neutrals — light mode is tinted toward the sage palette; dark mode stays neutral black.
// ---------------------------------------------------------------------------

internal val NeutralLightBackground = Color(0xFFF6FAF2)
internal val NeutralLightSurface = Color(0xFFFFFFFF)
internal val NeutralLightSurfaceVariant = Color(0xFFE1EDD4)
internal val NeutralLightContainer = Color(0xFFEEF5E7)
internal val NeutralLightContainerHigh = Color(0xFFE6F0DC)
internal val NeutralLightContainerHighest = Color(0xFFDDEBD0)
internal val NeutralLightDim = Color(0xFFD3E4C6)
internal val NeutralLightOn = Color(0xFF142019)
internal val NeutralLightOnVariant = Color(0xFF4A5B4D)
internal val NeutralLightOutline = Color(0xFF6B806E)
internal val NeutralLightDivider = Color(0xFFD6E4CB)
internal val NeutralLightInverse = Color(0xFF1F2A22)
internal val NeutralLightInverseOn = Color(0xFFE9F3E4)

internal val NeutralDarkBackground = Color(0xFF080808)
internal val NeutralDarkSurface = Color(0xFF121314)
internal val NeutralDarkSurfaceVariant = Color(0xFF1D1F21)
internal val NeutralDarkContainerLowest = Color(0xFF030303)
internal val NeutralDarkContainerLow = Color(0xFF0D0E0F)
internal val NeutralDarkContainerHigh = Color(0xFF1A1B1D)
internal val NeutralDarkContainerHighest = Color(0xFF242629)
internal val NeutralDarkBright = Color(0xFF2B2D30)
internal val NeutralDarkOn = Color(0xFFEDEEF0)
internal val NeutralDarkOnVariant = Color(0xFFA4A8AE)
internal val NeutralDarkOutline = Color(0xFF6C7077)
internal val NeutralDarkDivider = Color(0xFF26282B)

// ---------------------------------------------------------------------------
// Status.
// ---------------------------------------------------------------------------

internal val ErrorLight = Color(0xFFB3261E)
internal val ErrorLightContainer = Color(0xFFF9DEDC)
internal val ErrorLightOnContainer = Color(0xFF5F1412)
internal val ErrorDark = Color(0xFFF2B8B5)
internal val ErrorDarkOn = Color(0xFF601410)
internal val ErrorDarkContainer = Color(0xFF8C1D18)

internal val SuccessLight = Color(0xFF1D7A5F)
internal val SuccessLightContainer = Color(0xFFB6EBD8)
internal val SuccessLightOnContainer = Color(0xFF00382A)
internal val SuccessDark = Color(0xFF6FD3AE)
internal val SuccessDarkContainer = Color(0xFF0B4635)

internal val DestructiveLight = Color(0xFFE03131)
internal val DestructiveDark = Color(0xFFE75353)

// ---------------------------------------------------------------------------
// Categorical colours. These carry meaning — a weather scale must read cold-to-hot,
// and expense categories must stay distinguishable from one another — so they are
// deliberately NOT folded into the brand ramp. Each has a dark variant lightened just
// far enough to clear 4.5:1 on the dark surface while holding its hue.
// ---------------------------------------------------------------------------

internal val CategoryAccommodationLight = Color(0xFF7F77DD)
internal val CategoryAccommodationDark = Color(0xFF8179DF)
internal val CategoryFoodLight = Color(0xFF1D9E75)
internal val CategoryFoodDark = Color(0xFF1C9F75)
internal val CategoryTransportLight = Color(0xFFBA7517)
internal val CategoryTransportDark = Color(0xFFBC7515)
internal val CategoryShoppingLight = Color(0xFFD4537E)
internal val CategoryShoppingDark = Color(0xFFD75A84)
internal val CategoryActivitiesLight = Color(0xFFD85A30)
internal val CategoryActivitiesDark = Color(0xFFDB6037)
internal val CategoryOtherLight = Color(0xFF888780)
internal val CategoryOtherDark = Color(0xFF9A9992)

internal val AccentBlueLight = Color(0xFF378ADD)
internal val AccentBlueDark = Color(0xFF358ADF)
internal val AccentGreenLight = Color(0xFF639922)
internal val AccentGreenDark = Color(0xFF639A21)
internal val AccentCoralLight = Color(0xFFD85A30)
internal val AccentCoralDark = Color(0xFFDB6037)
internal val AccentPurpleLight = Color(0xFF7F77DD)
internal val AccentPurpleDark = Color(0xFF8179DF)

internal val TempFreezingLight = Color(0xFF6EA8DC)
internal val TempFreezingDark = Color(0xFF6DA8DD)
internal val TempColdLight = Color(0xFF5B9BD5)
internal val TempColdDark = Color(0xFF5A9BD6)
internal val TempMildLight = Color(0xFF5DBFA6)
internal val TempMildDark = Color(0xFF5CC0A6)
internal val TempWarmLight = Color(0xFF8FBF4A)
internal val TempWarmDark = Color(0xFF8FC049)
internal val TempHotLight = Color(0xFFE0A93C)
internal val TempHotDark = Color(0xFFE2AA3A)
internal val TempScorchingLight = Color(0xFFD8703C)
internal val TempScorchingDark = Color(0xFFDA6F3A)

internal val SkyClearLight = Color(0xFFE0A93C)
internal val SkyClearDark = Color(0xFFE2AA3A)
internal val SkyCloudyLight = Color(0xFF6E7C93)
internal val SkyCloudyDark = Color(0xFF79869C)
internal val SkyFogLight = Color(0xFF8A8F98)
internal val SkyFogDark = Color(0xFF9BA0A8)
internal val SkyRainLight = Color(0xFF5B9BD5)
internal val SkyRainDark = Color(0xFF5A9BD6)
internal val SkySnowLight = Color(0xFF9FC4DC)
internal val SkySnowDark = Color(0xFF9EC4DD)
internal val SkyStormLight = Color(0xFF7C6BB5)
internal val SkyStormDark = Color(0xFF8B7CBF)

internal val ActionPinLight = Color(0xFF4F7CC0)
internal val ActionPinDark = Color(0xFF5C86C6)
internal val ActionEditLight = Color(0xFF5B8A4B)
internal val ActionEditDark = Color(0xFF60924F)
internal val ActionArchiveLight = Color(0xFF8A7B3B)
internal val ActionArchiveDark = Color(0xFF96853F)
internal val ActionDeleteLight = Color(0xFFB23B3B)
internal val ActionDeleteDark = Color(0xFFCE6666)

/** Avatar/initial-chip rotation. Already mid-tone, so both modes share them. */
internal val AvatarPaletteShared = listOf(
    Color(0xFF85B7EB), Color(0xFFC9A0DC), Color(0xFF8FD1B6),
    Color(0xFFE8B87A), Color(0xFFE49AAE), Color(0xFF9FB8E8),
)

// ---------------------------------------------------------------------------
// Fixed-media colours. A postcard is printed paper and is exported as a shareable
// image, so it must look identical no matter the sender's theme. These never flip.
// ---------------------------------------------------------------------------

internal val PaperStock = Color(0xFFFFFFFF)
internal val PaperInk = Color(0xFF111111)
internal val PaperInkMuted = Color(0xFF333333)
internal val PaperScrim = Color(0x22000000)

// ---------------------------------------------------------------------------
// Settings row chips. A white glyph sits on each of these, so legibility depends
// only on the chip itself — the page behind it never participates, and one set
// therefore serves both modes.
//
// Retinted to the mint/leaf theme; warm hues stay only where they carry meaning
// (alerts, weather, export). Originally six were darkened from their iOS system-palette originals, which put a white
// glyph on bright yellow, cyan and green at ratios as low as 1.51:1. Each is
// darkened the minimum needed to clear the 3:1 non-text floor, so the hue reads
// the same as before.
// ---------------------------------------------------------------------------

internal val ChipAccount = Color(0xFF0B7669)
internal val ChipAppearance = Color(0xFF2F6FBF)
internal val ChipLanguage = Color(0xFF0F8B7E)
internal val ChipNotifications = Color(0xFFD9483B)
internal val ChipWorldClock = Color(0xFF1F7FB8)
internal val ChipWeather = Color(0xFFB28F00)
internal val ChipTranslate = Color(0xFF2E6F4E)
internal val ChipEmergency = Color(0xFFE0314F)
internal val ChipCompass = Color(0xFF0E8F8A)
internal val ChipArchive = Color(0xFF6A6F85)
internal val ChipTrash = Color(0xFFB4525C)
internal val ChipExport = Color(0xFFC26A00)
internal val ChipBackup = Color(0xFF2A7FCB)
internal val ChipHelp = Color(0xFF3E7D5A)
internal val ChipFeedback = Color(0xFF12937F)
internal val ChipAbout = Color(0xFF6A6F85)

// ---------------------------------------------------------------------------
// Document type accents.
// ---------------------------------------------------------------------------

internal val DocPdfLight = Color(0xFFA32D2D)
internal val DocPdfDark = Color(0xFFD56262)
internal val DocImageLight = Color(0xFF378ADD)
internal val DocImageDark = Color(0xFF358ADF)
internal val DocOtherLight = Color(0xFF7A7A7A)
internal val DocOtherDark = Color(0xFF858585)

/**
 * Traveller avatars. White initials sit on these, so one set serves both modes.
 *
 * Order and length are load-bearing: [colorFor] picks by `hash(id) % size`, so
 * reordering or resizing this list silently reassigns every existing traveller's
 * colour across every device. Five entries are darkened from their originals,
 * which put white initials on bright green, orange and yellow at ratios as low
 * as 1.86:1 — each moves the minimum needed to clear 3:1, holding its hue and
 * its position.
 */
internal val TravellerAvatarPalette = listOf(
    Color(0xFF5C7CFA), Color(0xFF2FA943), Color(0xFFE87100),
    Color(0xFFE64980), Color(0xFF1EA0B4), Color(0xFFBE4BDB),
    Color(0xFFC08804), Color(0xFF1BA77D),
)

// ---------------------------------------------------------------------------
// Boarding-pass surface. Like the postcard, a ticket reads as a printed object
// and its card stays light in both modes — only the scrim behind it is dark.
// ---------------------------------------------------------------------------

internal val PassScrim = Color(0xF2101014)
internal val PassChipSurface = Color(0xFFF0F1F4)
internal val PassInk = Color(0xFF37474F)
internal val PassInkStrong = Color(0xFF111111)
internal val PassInkMuted = Color(0xFF6B7280)
internal val PassInkFaint = Color(0xFF9CA3AF)
internal val PassDivider = Color(0xFFE5E7EB)
internal val PassNoticeSurface = Color(0xFFFFF3CD)
internal val PassNoticeInk = Color(0xFF664D03)

/**
 * Links on the pass. Fixed like the rest of the card: the theme's primary inverts
 * in dark mode to a pale blue that all but vanishes on white paper (1.51:1).
 */
internal val PassLink = Color(0xFF0B7669)

// Expense cards. The light value is sampled from the approved summary-card
// reference; the dark counterpart keeps the same blue-toned visual hierarchy.
internal val ExpenseCardLight = Color(0xFFE1EDD4)
internal val ExpenseCardDark = NeutralDarkSurfaceVariant

// ---------------------------------------------------------------------------
// Bottom navigation.
//
// The bar floats over scrolling content, so its fill is translucent and must not
// match any card token — it previously reused surfaceVariant, which is exactly
// what the Currency and Documents cards use, so the bar vanished into them and
// only the icons appeared to float.
//
// Alpha is baked into these values (0xB8 ~= 72%, 0xE6 ~= 90%) so the Canvas can
// draw them directly.
// ---------------------------------------------------------------------------

// Light mode uses the theme's white surface rather than carrying the brand-blue tint.
// Its high opacity keeps the floating bar distinct from cards scrolling beneath it.
internal val NavBarLight = Color(0xF5FFFFFF)
internal val NavBarDark = Color(0xCC1B1C1E)
internal val NavPillLight = Color(0xF5F1F7EA)
internal val NavPillDark = Color(0xF2272829)
internal val NavBorderLight = Color(0x22142019)
internal val NavBorderDark = Color(0x2EEDEEF0)

/** Scrim behind controls floating over photography, where the photo is the background. */
internal val OverlayControlScrim = Color(0xFF333333)

// ---------------------------------------------------------------------------
// Assorted accents.
//
// Several of these are tints on icons or text, so the light values are darkened
// from their originals: amber at #E0A93C and gold at #E8B931 sat at 2.12:1 and
// 1.84:1 on a light surface. Values used only as fills behind white content
// (the SOS button, the swipe-to-delete backing) keep their original hue in both
// modes, since the page behind them never competes.
// ---------------------------------------------------------------------------

internal val DayTintLight = Color(0xFFBF891E)
internal val DayTintDark = Color(0xFFE0A93C)
internal val NightTintLight = Color(0xFF938ADA)
internal val NightTintDark = Color(0xFF9B92DD)

internal val StarLight = Color(0xFFB68C14)
internal val StarDark = Color(0xFFE8B931)

internal val StrengthWeakLight = Color(0xFFD8703C)
internal val StrengthWeakDark = Color(0xFFD8703C)
internal val StrengthFairLight = Color(0xFFBF891E)
internal val StrengthFairDark = Color(0xFFE0A93C)

internal val SosRedFill = Color(0xFFD32F2F)
internal val SwipeDeleteFill = Color(0xFF7A2E2E)
internal val PhotoOverlayScrim = Color(0xFF121418)

// ---------------------------------------------------------------------------
// Sky illustration for the travel-time picker.
//
// A depiction of the sky at a given hour, not app chrome — a night sky is dark
// because it is night, not because the app is in dark mode. Each palette ships
// its own foreground, so the illustration stays internally legible whichever
// theme surrounds it.
// ---------------------------------------------------------------------------

internal val SkyNightStart = Color(0xFF071426)
internal val SkyNightMiddle = Color(0xFF102E5C)
internal val SkyNightEnd = Color(0xFF1D467A)
internal val SkyNightForeground = Color(0xFFF8FAFC)

internal val SkySunriseStart = Color(0xFFF59E0B)
internal val SkySunriseMiddle = Color(0xFFFFC83D)
internal val SkySunriseEnd = Color(0xFFFFE8A3)
internal val SkySunriseForeground = Color(0xFF3D2B00)

internal val SkyDaytimeStart = Color(0xFFFBBF24)
internal val SkyDaytimeMiddle = Color(0xFFFDE047)
internal val SkyDaytimeEnd = Color(0xFFFFF3C4)
internal val SkyDaytimeForeground = Color(0xFF352600)

internal val SkyDuskStart = Color(0xFFF59E0B)
internal val SkyDuskMiddle = Color(0xFFB453C6)
internal val SkyDuskEnd = Color(0xFF243A78)
internal val SkyDuskForeground = Color(0xFFF8FAFC)

internal val SunRay = Color(0xFFFFF3B0)
internal val SunDisc = Color(0xFFFFF1A8)
internal val MoonDisc = Color(0xFFFFF5CC)
