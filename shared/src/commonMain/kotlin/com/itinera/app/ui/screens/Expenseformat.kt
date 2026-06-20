package com.itinera.app.ui.screens

import kotlin.math.abs
import kotlin.math.round

val COMMON_CURRENCIES = listOf(
    "EUR", "USD", "GBP", "JPY", "CHF", "INR", "AUD", "CAD", "CNY",
    "SGD", "HKD", "THB", "AED", "SEK", "NOK", "DKK", "PLN", "CZK",
    "MXN", "BRL", "ZAR", "NZD", "KRW", "TRY",
)

fun currencySymbol(code: String): String = when (code) {
    "EUR" -> "€"
    "USD", "AUD", "CAD", "SGD", "HKD", "NZD", "MXN" -> "$"
    "GBP" -> "£"
    "JPY", "CNY" -> "¥"
    "INR" -> "₹"
    "KRW" -> "₩"
    "THB" -> "฿"
    "TRY" -> "₺"
    else -> ""
}

fun formatMoney(amount: Double, code: String): String {
    val sym = currencySymbol(code)
    val body = twoDecimals(amount)
    return if (sym.isNotEmpty()) "$sym$body" else "$body $code"
}

private fun twoDecimals(v: Double): String {
    val cents = round(v * 100).toLong()
    val sign = if (cents < 0) "-" else ""
    val a = abs(cents)
    val whole = a / 100
    val frac = a % 100
    val fracStr = if (frac < 10) "0$frac" else "$frac"
    return "$sign$whole.$fracStr"
}