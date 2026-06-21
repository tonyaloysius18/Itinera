package com.itinera.app.i18n

import androidx.compose.runtime.compositionLocalOf

/**
 * Provides the active Strings table down the Compose tree. Read it anywhere with
 * `val s = LocalStrings.current`. The App composable swaps the provided value when
 * the user changes language, which recomposes every screen in the new language.
 */
val LocalStrings = compositionLocalOf { stringsFor(Language.ENGLISH) }