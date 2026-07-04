package com.itinera.app.data

import com.itinera.app.model.Trip
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.roundToInt

/** Great-circle distance between two coordinates, in km. */
fun haversineKm(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
    val r = 6371.0
    val dLat = (lat2 - lat1) * PI_180
    val dLng = (lng2 - lng1) * PI_180
    val a = sin(dLat / 2) * sin(dLat / 2) +
            cos(lat1 * PI_180) * cos(lat2 * PI_180) * sin(dLng / 2) * sin(dLng / 2)
    return 2 * r * asin(sqrt(a))
}
private const val PI_180 = kotlin.math.PI / 180.0

/** The trip's dominant country (most legs), for the postcard "From {country}" line. */
fun Trip.primaryCountry(): String {
    val countries = legs
        .sortedWith(compareBy({ it.date }, { it.timeLabel }))
        .mapNotNull { it.country.trim().ifBlank { null } }
    if (countries.isEmpty()) return ""
    val weighted = if (countries.size > 1) countries.dropLast(1) else countries
    return weighted.groupingBy { it }.eachCount().maxByOrNull { it.value }?.key ?: ""
}

/** Distinct countries across the trip's legs (blank = not yet geocoded, skipped). */
fun Trip.countriesCovered(): Int =
    legs.map { it.country.trim().lowercase() }.filter { it.isNotBlank() }.distinct().size

/** Total km across legs whose both endpoints are geocoded. */
fun Trip.distanceTravelledKm(): Int =
    legs.filter { (it.fromLat != 0.0 || it.fromLng != 0.0) && (it.toLat != 0.0 || it.toLng != 0.0) }
        .sumOf { haversineKm(it.fromLat, it.fromLng, it.toLat, it.toLng) }
        .roundToInt()

