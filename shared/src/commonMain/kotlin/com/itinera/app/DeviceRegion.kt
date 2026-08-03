package com.itinera.app

/**
 * The English name of the device's region, e.g. "France".
 *
 * Region, not language: someone living in France with an English phone should
 * still be offered French. Empty when the platform can't tell us.
 */
expect fun deviceCountryName(): String