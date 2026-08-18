package com.itinera.app.ui.screens

import kotlin.math.abs
import kotlin.math.round

data class CurrencyOption(val code: String, val name: String)

val POPULAR_CURRENCY_CODES = listOf(
    "USD", "EUR", "GBP", "JPY", "SAR", "AED", "CHF", "CAD", "AUD", "CNY", "INR",
)

/**
 * Current ISO 4217 currencies, excluding testing codes, precious metals and
 * accounting/fund units. The picker sorts by name again at runtime so future
 * additions cannot accidentally break its alphabetical order.
 *
 * Source: ISO 4217 Maintenance Agency List One, published 2026-01-01.
 */
val ALL_CURRENCIES: List<CurrencyOption> = listOf(
    CurrencyOption("AFN", "Afghani"),
    CurrencyOption("DZD", "Algerian Dinar"),
    CurrencyOption("ARS", "Argentine Peso"),
    CurrencyOption("AMD", "Armenian Dram"),
    CurrencyOption("AWG", "Aruban Florin"),
    CurrencyOption("AUD", "Australian Dollar"),
    CurrencyOption("AZN", "Azerbaijan Manat"),
    CurrencyOption("BSD", "Bahamian Dollar"),
    CurrencyOption("BHD", "Bahraini Dinar"),
    CurrencyOption("THB", "Baht"),
    CurrencyOption("PAB", "Balboa"),
    CurrencyOption("BBD", "Barbados Dollar"),
    CurrencyOption("BYN", "Belarusian Ruble"),
    CurrencyOption("BZD", "Belize Dollar"),
    CurrencyOption("BMD", "Bermudian Dollar"),
    CurrencyOption("BOB", "Boliviano"),
    CurrencyOption("VED", "Bolívar Soberano"),
    CurrencyOption("VES", "Bolívar Soberano"),
    CurrencyOption("BRL", "Brazilian Real"),
    CurrencyOption("BND", "Brunei Dollar"),
    CurrencyOption("BIF", "Burundi Franc"),
    CurrencyOption("CVE", "Cabo Verde Escudo"),
    CurrencyOption("CAD", "Canadian Dollar"),
    CurrencyOption("XCG", "Caribbean Guilder"),
    CurrencyOption("KYD", "Cayman Islands Dollar"),
    CurrencyOption("XOF", "CFA Franc BCEAO"),
    CurrencyOption("XAF", "CFA Franc BEAC"),
    CurrencyOption("XPF", "CFP Franc"),
    CurrencyOption("CLP", "Chilean Peso"),
    CurrencyOption("COP", "Colombian Peso"),
    CurrencyOption("KMF", "Comorian Franc"),
    CurrencyOption("CDF", "Congolese Franc"),
    CurrencyOption("BAM", "Convertible Mark"),
    CurrencyOption("NIO", "Cordoba Oro"),
    CurrencyOption("CRC", "Costa Rican Colon"),
    CurrencyOption("CUP", "Cuban Peso"),
    CurrencyOption("CZK", "Czech Koruna"),
    CurrencyOption("GMD", "Dalasi"),
    CurrencyOption("DKK", "Danish Krone"),
    CurrencyOption("MKD", "Denar"),
    CurrencyOption("DJF", "Djibouti Franc"),
    CurrencyOption("STN", "Dobra"),
    CurrencyOption("DOP", "Dominican Peso"),
    CurrencyOption("VND", "Dong"),
    CurrencyOption("XCD", "East Caribbean Dollar"),
    CurrencyOption("EGP", "Egyptian Pound"),
    CurrencyOption("SVC", "El Salvador Colon"),
    CurrencyOption("ETB", "Ethiopian Birr"),
    CurrencyOption("EUR", "Euro"),
    CurrencyOption("FKP", "Falkland Islands Pound"),
    CurrencyOption("FJD", "Fiji Dollar"),
    CurrencyOption("HUF", "Forint"),
    CurrencyOption("GHS", "Ghana Cedi"),
    CurrencyOption("GIP", "Gibraltar Pound"),
    CurrencyOption("HTG", "Gourde"),
    CurrencyOption("PYG", "Guarani"),
    CurrencyOption("GNF", "Guinean Franc"),
    CurrencyOption("GYD", "Guyana Dollar"),
    CurrencyOption("HKD", "Hong Kong Dollar"),
    CurrencyOption("UAH", "Hryvnia"),
    CurrencyOption("ISK", "Iceland Krona"),
    CurrencyOption("INR", "Indian Rupee"),
    CurrencyOption("IRR", "Iranian Rial"),
    CurrencyOption("IQD", "Iraqi Dinar"),
    CurrencyOption("JMD", "Jamaican Dollar"),
    CurrencyOption("JOD", "Jordanian Dinar"),
    CurrencyOption("KES", "Kenyan Shilling"),
    CurrencyOption("PGK", "Kina"),
    CurrencyOption("KWD", "Kuwaiti Dinar"),
    CurrencyOption("AOA", "Kwanza"),
    CurrencyOption("MMK", "Kyat"),
    CurrencyOption("LAK", "Lao Kip"),
    CurrencyOption("GEL", "Lari"),
    CurrencyOption("LBP", "Lebanese Pound"),
    CurrencyOption("ALL", "Lek"),
    CurrencyOption("HNL", "Lempira"),
    CurrencyOption("SLE", "Leone"),
    CurrencyOption("LRD", "Liberian Dollar"),
    CurrencyOption("LYD", "Libyan Dinar"),
    CurrencyOption("SZL", "Lilangeni"),
    CurrencyOption("LSL", "Loti"),
    CurrencyOption("MGA", "Malagasy Ariary"),
    CurrencyOption("MWK", "Malawi Kwacha"),
    CurrencyOption("MYR", "Malaysian Ringgit"),
    CurrencyOption("MUR", "Mauritius Rupee"),
    CurrencyOption("MXN", "Mexican Peso"),
    CurrencyOption("MDL", "Moldovan Leu"),
    CurrencyOption("MAD", "Moroccan Dirham"),
    CurrencyOption("MZN", "Mozambique Metical"),
    CurrencyOption("NGN", "Naira"),
    CurrencyOption("ERN", "Nakfa"),
    CurrencyOption("NAD", "Namibia Dollar"),
    CurrencyOption("NPR", "Nepalese Rupee"),
    CurrencyOption("ILS", "New Israeli Sheqel"),
    CurrencyOption("TWD", "New Taiwan Dollar"),
    CurrencyOption("NZD", "New Zealand Dollar"),
    CurrencyOption("BTN", "Ngultrum"),
    CurrencyOption("KPW", "North Korean Won"),
    CurrencyOption("NOK", "Norwegian Krone"),
    CurrencyOption("MRU", "Ouguiya"),
    CurrencyOption("PKR", "Pakistan Rupee"),
    CurrencyOption("MOP", "Pataca"),
    CurrencyOption("TOP", "Pa’anga"),
    CurrencyOption("UYU", "Peso Uruguayo"),
    CurrencyOption("PHP", "Philippine Peso"),
    CurrencyOption("GBP", "Pound Sterling"),
    CurrencyOption("BWP", "Pula"),
    CurrencyOption("QAR", "Qatari Rial"),
    CurrencyOption("GTQ", "Quetzal"),
    CurrencyOption("ZAR", "Rand"),
    CurrencyOption("OMR", "Rial Omani"),
    CurrencyOption("KHR", "Riel"),
    CurrencyOption("RON", "Romanian Leu"),
    CurrencyOption("MVR", "Rufiyaa"),
    CurrencyOption("IDR", "Rupiah"),
    CurrencyOption("RUB", "Russian Ruble"),
    CurrencyOption("RWF", "Rwanda Franc"),
    CurrencyOption("SHP", "Saint Helena Pound"),
    CurrencyOption("SAR", "Saudi Riyal"),
    CurrencyOption("RSD", "Serbian Dinar"),
    CurrencyOption("SCR", "Seychelles Rupee"),
    CurrencyOption("SGD", "Singapore Dollar"),
    CurrencyOption("PEN", "Sol"),
    CurrencyOption("SBD", "Solomon Islands Dollar"),
    CurrencyOption("KGS", "Som"),
    CurrencyOption("SOS", "Somali Shilling"),
    CurrencyOption("TJS", "Somoni"),
    CurrencyOption("SSP", "South Sudanese Pound"),
    CurrencyOption("LKR", "Sri Lanka Rupee"),
    CurrencyOption("SDG", "Sudanese Pound"),
    CurrencyOption("SRD", "Surinam Dollar"),
    CurrencyOption("SEK", "Swedish Krona"),
    CurrencyOption("CHF", "Swiss Franc"),
    CurrencyOption("SYP", "Syrian Pound"),
    CurrencyOption("BDT", "Taka"),
    CurrencyOption("WST", "Tala"),
    CurrencyOption("TZS", "Tanzanian Shilling"),
    CurrencyOption("KZT", "Tenge"),
    CurrencyOption("TTD", "Trinidad and Tobago Dollar"),
    CurrencyOption("MNT", "Tugrik"),
    CurrencyOption("TND", "Tunisian Dinar"),
    CurrencyOption("TRY", "Turkish Lira"),
    CurrencyOption("TMT", "Turkmenistan New Manat"),
    CurrencyOption("AED", "UAE Dirham"),
    CurrencyOption("UGX", "Uganda Shilling"),
    CurrencyOption("USD", "US Dollar"),
    CurrencyOption("UZS", "Uzbekistan Sum"),
    CurrencyOption("VUV", "Vatu"),
    CurrencyOption("KRW", "Won"),
    CurrencyOption("YER", "Yemeni Rial"),
    CurrencyOption("JPY", "Yen"),
    CurrencyOption("CNY", "Yuan Renminbi"),
    CurrencyOption("ZMW", "Zambian Kwacha"),
    CurrencyOption("ZWG", "Zimbabwe Gold"),
    CurrencyOption("PLN", "Zloty"),
).sortedWith(compareBy<CurrencyOption> { it.name.lowercase() }.thenBy { it.code })

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
