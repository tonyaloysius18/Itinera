package com.itinera.app.data.pdf

import androidx.compose.ui.graphics.toArgb
import com.itinera.app.data.TripExporter.TripBundle
import com.itinera.app.model.Activity
import com.itinera.app.model.Expense
import com.itinera.app.model.ExpenseCategory
import com.itinera.app.model.Leg
import com.itinera.app.model.Trip
import com.itinera.app.model.TransportType
import com.itinera.app.ui.theme.TravellerAvatarPalette
import kotlinx.datetime.LocalDate
import kotlin.math.abs
import kotlin.math.round

/**
 * Renders the shareable trip PDF: a branded cover per trip, a day-by-day timeline that merges
 * journeys and places, and an expenses summary.
 *
 * Layout is done once here against [PdfCanvas]; the platform only draws primitives.
 */
object TripPdf {
    /**
     * @param tagline the localised "Where Every Journey Begins!" line, so the cover matches the login screen.
     */
    fun build(bundles: List<TripBundle>, assets: PdfAssets, tagline: String, today: LocalDate): ByteArray {
        // Pass 1 only counts pages so pass 2 can print "Page x of y".
        val pages = TripPdfLayout(createPdfWriter(assets), bundles, tagline, today, totalPages = 0).run().first
        return TripPdfLayout(createPdfWriter(assets), bundles, tagline, today, totalPages = pages).run().second
    }
}

private const val W = 595f
private const val H = 842f
private const val M = 40f
private const val CW = W - 2 * M
private const val BOTTOM = 62f

private class DayItem(val time: String, val leg: Leg?, val act: Activity?)

private class TripPdfLayout(
    private val c: PdfWriter,
    private val bundles: List<TripBundle>,
    private val tagline: String,
    private val today: LocalDate,
    private val totalPages: Int,
) {
    private var page = 0
    private var y = 0f
    private var onCover = false
    private lateinit var trip: Trip
    private var sym = "€"

    fun run(): Pair<Int, ByteArray> {
        bundles.forEach { drawTrip(it) }
        if (page == 0) { // nothing selected: still emit a valid one-page document
            c.newPage(); page = 1
        }
        footer()
        return page to c.finish()
    }

    // ── one trip ─────────────────────────────────────────────────
    private fun drawTrip(b: TripBundle) {
        trip = b.trip
        sym = currencySymbol(trip.currencyCode)
        val legs = b.trip.legs.sortedWith(compareBy({ it.date.toString() }, { it.timeLabel }))
        val acts = b.activities
        val expenses = b.expenses.sortedByDescending { it.createdAt }
        val dates = (legs.map { it.date } + acts.map { it.date }).sorted()
        val start = dates.firstOrNull()
        val end = dates.lastOrNull()

        startPage(cover = true)
        cover(start, end, legs.size, acts.size, expenses.sumOf { it.amount })

        val days = groupByDay(legs, acts)
        if (days.isNotEmpty() && start != null) {
            nextPage()
            heading(PdfIcon.Calendar, "Day by day", rangeShort(start, end!!))
            days.forEach { (d, items) -> dayBlock(d, items, start) }
        }
        if (expenses.isNotEmpty()) {
            nextPage()
            expensesSection(expenses)
        }
    }

    // ── pages ────────────────────────────────────────────────────
    private fun startPage(cover: Boolean) {
        if (page > 0) footer()
        c.newPage()
        page++
        onCover = cover
        y = H - M
    }

    private fun nextPage() {
        startPage(cover = false)
        c.image(PdfImage.LogoTile, M, H - M - 14, 26f)
        text("Itinera", M + 32, H - M - 4, PdfFont.Wordmark, 21f, Pdf.B5)
        text(trip.title, W - M, H - M - 4, PdfFont.Medium, 9f, Pdf.Faint, PdfAlign.Right)
        c.line(M, H - M - 20, W - M, H - M - 20, Pdf.Line, 0.7f)
        y = H - M - 44
    }

    private fun ensure(h: Float) {
        if (y - h < BOTTOM) nextPage()
    }

    private fun footer() {
        if (onCover || page == 0) return
        c.line(M, 44f, W - M, 44f, Pdf.Line, 0.7f)
        text("${trip.title}  ·  Itinera", M, 30f, PdfFont.Regular, 8f, Pdf.Faint)
        val label = if (totalPages > 0) "Page $page of $totalPages" else "Page $page"
        text(label, W - M, 30f, PdfFont.Medium, 8f, Pdf.Muted, PdfAlign.Right)
    }

    // ── cover ────────────────────────────────────────────────────
    private fun cover(start: LocalDate?, end: LocalDate?, journeys: Int, places: Int, total: Double) {
        c.save(); c.alpha(0.22f); c.fillCircle(W - 40, H - 60, 250f, Pdf.B1); c.restore()
        c.save(); c.alpha(0.28f); c.fillCircle(W - 40, H - 60, 170f, Pdf.B1); c.restore()
        c.save(); c.alpha(0.9f); c.image(PdfImage.Plane, W - 250, H - 210, 190f, Pdf.B4); c.restore()

        // Same lockup as the login screen: app icon, script wordmark, tagline.
        c.image(PdfImage.LogoTile, M - 6, H - M - 78, 84f)
        text("Itinera", M + 88, H - M - 42, PdfFont.Wordmark, 58f, Pdf.B5)
        if (tagline.isNotBlank()) text(tagline, M + 92, H - M - 66, PdfFont.Tagline, 14f, Pdf.B4)

        var cy = H - 330
        text("TRAVEL ITINERARY", M, cy + 62, PdfFont.Semibold, 9.5f, Pdf.B3, tracking = 2.6f)
        var lines = wrap(trip.title, PdfFont.Heavy, 46f, CW - 20)
        val words = trip.title.split(" ").filter { it.isNotBlank() }
        if (words.size > 2 && lines.size <= 2) { // balance into two lines rather than leaving a lone word ("… Summer / 2026")
            val best = (1 until words.size)
                .map { k -> listOf(words.take(k).joinToString(" "), words.drop(k).joinToString(" ")) }
                .filter { pair -> pair.all { c.textWidth(it, PdfFont.Heavy, 46f) <= CW - 20 } }
                .minByOrNull { pair -> pair.maxOf { c.textWidth(it, PdfFont.Heavy, 46f) } }
            if (best != null) lines = best
        }
        if (lines.size > 3) lines = lines.take(2) + fit(lines.drop(2).joinToString(" "), PdfFont.Heavy, 46f, CW - 20).first
        for (ln in lines) {
            text(ln, M - 2, cy, PdfFont.Heavy, 46f, Pdf.Navy); cy -= 54
        }
        c.fillRoundRect(M, cy + 20, 56f, 5f, 2.5f, Pdf.Teal)
        cy -= 14
        if (start != null && end != null) {
            c.icon(PdfIcon.Calendar, M + 8, cy + 4, 17f, Pdf.B4)
            text(rangeLong(start, end), M + 26, cy, PdfFont.Medium, 14f, Pdf.Ink)
        }

        cy -= 52
        if (trip.travellers.isNotEmpty()) {
            text("TRAVELLERS", M, cy + 16, PdfFont.Semibold, 8f, Pdf.Faint, tracking = 1.8f)
            var x = M
            var row = 0
            for ((i, t) in trip.travellers.withIndex()) {
                val name = t.firstName.ifBlank { "?" }
                val w = c.textWidth(name, PdfFont.Medium, 10.5f) + 40
                if (x + w > W - M) {
                    if (row == 1) { // cap at two rows
                        val more = "+${trip.travellers.size - i} more"
                        c.fillRoundRect(x, cy - 18, c.textWidth(more, PdfFont.Medium, 10.5f) + 24, 30f, 15f, Pdf.Tint2)
                        text(more, x + 12, cy - 6.5f, PdfFont.Medium, 10.5f, Pdf.Muted)
                        break
                    }
                    x = M; cy -= 38; row++
                }
                c.fillRoundRect(x, cy - 18, w, 30f, 15f, Pdf.Tint2)
                avatar(x + 15, cy - 3, 11f, name, colorFor(t.id), 10f)
                text(name, x + 32, cy - 6.5f, PdfFont.Medium, 10.5f, Pdf.Ink)
                x += w + 8
            }
        }

        val stats = listOf(
            Triple(PdfIcon.Calendar, if (start != null && end != null) "${daysBetween(start, end) + 1}" else "–", "Days"),
            Triple(PdfIcon.Route, "$journeys", "Journeys"),
            Triple(PdfIcon.Pin, "$places", "Places"),
            Triple(PdfIcon.Wallet, money(total), "Total spent"),
        )
        val sy = 190f
        val cw = (CW - 3 * 12) / 4
        for ((i, s) in stats.withIndex()) {
            val x = M + i * (cw + 12)
            shadow(x, sy, cw, 78f, 12f)
            c.fillRoundRect(x, sy, cw, 78f, 12f, Pdf.White); c.strokeRoundRect(x, sy, cw, 78f, 12f, Pdf.Line, 0.8f)
            c.fillRoundRect(x + 14, sy + 44, 26f, 26f, 8f, Pdf.Tint2)
            c.icon(s.first, x + 27, sy + 57, 15f, Pdf.B4, Pdf.Tint2)
            val (v, sz) = fit(s.second, PdfFont.Bold, 19f, cw - 24, 12f)
            text(v, x + 14, sy + 24, PdfFont.Bold, sz, Pdf.Navy)
            text(s.third.uppercase(), x + 14, sy + 10, PdfFont.Semibold, 7f, Pdf.Faint, tracking = 1.2f)
        }

        c.fillRoundRect(0f, 0f, W, 118f, 0f, Pdf.Navy)
        c.fillRoundRect(0f, 0f, W, 8f, 0f, Pdf.B5)
        c.save(); c.alpha(0.9f); c.image(PdfImage.Plane, W - 180, 34f, 130f, Pdf.White); c.restore()
        text("Planned with Itinera", M, 68f, PdfFont.Semibold, 15f, Pdf.White)
        text("Exported ${today.day} ${monthName(today)} ${today.year}", M, 50f, PdfFont.Regular, 9f, Pdf.B1)
    }

    // ── day-by-day ───────────────────────────────────────────────
    private fun groupByDay(legs: List<Leg>, acts: List<Activity>): List<Pair<LocalDate, List<DayItem>>> {
        val all = legs.map { DayItem(it.timeLabel, it, null) } + acts.map { DayItem(it.time, null, it) }
        val byDate = all.groupBy { it.leg?.date ?: it.act!!.date }
        return byDate.keys.sorted().map { d ->
            val items = byDate.getValue(d)
            d to (items.filter { it.time.isNotBlank() }.sortedBy { it.time } + items.filter { it.time.isBlank() })
        }
    }

    private fun heading(ic: PdfIcon, title: String, sub: String = "") {
        ensure(60f)
        c.fillRoundRect(M, y - 26, 30f, 30f, 9f, Pdf.B5)
        c.icon(ic, M + 15, y - 11, 16f, Pdf.White, Pdf.B5)
        text(title, M + 42, y - 8, PdfFont.Bold, 19f, Pdf.Navy)
        if (sub.isNotBlank()) text(sub, W - M, y - 6, PdfFont.Medium, 9.5f, Pdf.Muted, PdfAlign.Right)
        y -= 46
    }

    private fun dayBlock(d: LocalDate, items: List<DayItem>, start: LocalDate) {
        val legs = items.count { it.leg != null }
        val acts = items.size - legs
        ensure(70f)
        c.fillRoundRect(M, y - 34, 42f, 40f, 10f, Pdf.B5)
        text("${d.day}", M + 21, y - 8, PdfFont.Bold, 17f, Pdf.White, PdfAlign.Center)
        text(monthName(d).take(3).uppercase(), M + 21, y - 24, PdfFont.Semibold, 7.5f, Pdf.B1, PdfAlign.Center, 1.2f)
        text(weekdayName(d), M + 54, y - 6, PdfFont.Bold, 14f, Pdf.Navy)
        var sub = "Day ${daysBetween(start, d) + 1}"
        if (legs > 0) sub += "   ·   $legs journey${if (legs > 1) "s" else ""}"
        if (acts > 0) sub += "   ·   $acts stop${if (acts > 1) "s" else ""}"
        text(sub, M + 54, y - 22, PdfFont.Medium, 8.8f, Pdf.Muted)
        y -= 46

        val tx = M + 62; val ix = M + 84; val lx = M + 102
        val textW = W - M - lx - 8
        for ((k, it) in items.withIndex()) {
            val leg = it.leg
            val sub2 = if (leg != null) legSubline(leg) else ""
            val locLines = it.act?.location?.takeIf { l -> l.isNotBlank() }?.let { l -> wrap(l, PdfFont.Regular, 8.6f, textW) } ?: emptyList()
            val h = if (sub2.isEmpty() && locLines.isEmpty()) 30f else 34f + maxOf(0, locLines.size - 1) * 10f
            ensure(h + 4)
            val yy0 = y
            c.line(ix, yy0 + 6, ix, if (k < items.lastIndex) yy0 - h + 6 else yy0 - 6, Pdf.Line, 1.4f)
            val dotBg = if (leg != null) Pdf.Tint2 else Pdf.TealTint
            c.fillCircle(ix, yy0 - 5, 10.5f, Pdf.White)
            c.fillCircle(ix, yy0 - 5, 10f, dotBg)
            c.icon(if (leg != null) modeIcon(leg.transport) else PdfIcon.Pin, ix, yy0 - 5, 11.5f, if (leg != null) Pdf.B4 else Pdf.TealDeep, dotBg)
            if (it.time.isNotBlank()) text(it.time, tx, yy0 - 8, PdfFont.Semibold, 9.4f, Pdf.Muted, PdfAlign.Right)
            else text("All day", tx, yy0 - 8, PdfFont.Medium, 8f, Pdf.Faint, PdfAlign.Right)
            if (leg != null) {
                route(lx, yy0 - 8, leg.fromCity, leg.toCity, textW)
            } else {
                val (s, sz) = fit(it.act!!.title, PdfFont.Semibold, 11f, textW)
                text(s, lx, yy0 - 8, PdfFont.Semibold, sz, Pdf.Ink)
            }
            var ty = yy0 - 20
            if (sub2.isNotEmpty()) { text(sub2, lx, ty, PdfFont.Regular, 8.6f, Pdf.Muted); ty -= 10 }
            for (ln in locLines) { text(ln, lx, ty, PdfFont.Regular, 8.6f, Pdf.Faint); ty -= 10 }
            y -= h
        }
        y -= 14
    }

    private fun legSubline(l: Leg): String {
        val parts = mutableListOf(l.transport.name.lowercase().replaceFirstChar { it.uppercase() })
        if (l.operator.isNotBlank()) parts += l.operator
        if (l.endTimeLabel.isNotBlank()) parts += "arr. ${l.endTimeLabel}"
        val dur = duration(l.timeLabel, l.endTimeLabel)
        if (dur != null) parts += dur
        return parts.joinToString("  ·  ")
    }

    /** "FROM → TO", shrinking the type if a long pair of place names wouldn't fit. */
    private fun route(x: Float, by: Float, from: String, to: String, maxW: Float) {
        var size = 11f
        while (routeWidth(from, to, size) > maxW && size > 8f) size -= 0.25f
        text(from, x, by, PdfFont.Semibold, size, Pdf.Ink)
        val ax = x + c.textWidth(from, PdfFont.Semibold, size) + 6
        arrow(ax, by + size * 0.32f, 15f, Pdf.B3)
        text(to, ax + 21, by, PdfFont.Semibold, size, Pdf.Ink)
    }

    private fun routeWidth(a: String, b: String, size: Float) =
        c.textWidth(a, PdfFont.Semibold, size) + c.textWidth(b, PdfFont.Semibold, size) + 27

    private fun arrow(x: Float, ay: Float, w: Float, col: Int) {
        c.line(x, ay, x + w - 1, ay, col, 1.1f)
        c.strokePath(PdfPath().moveTo(x + w - 4, ay + 2.6f).lineTo(x + w, ay).lineTo(x + w - 4, ay - 2.6f), col, 1.1f)
    }

    // ── expenses ─────────────────────────────────────────────────
    private fun expensesSection(expenses: List<Expense>) {
        val total = expenses.sumOf { it.amount }
        heading(PdfIcon.Wallet, "Expenses", "${expenses.size} entr${if (expenses.size == 1) "y" else "ies"}  ·  ${trip.title}")

        // total banner
        c.fillRoundRect(M, y - 92, CW, 92f, 14f, Pdf.Navy)
        c.save(); c.alpha(0.13f); c.image(PdfImage.Plane, W - M - 150, y - 82, 120f, Pdf.White); c.restore()
        text("TOTAL TRIP SPEND", M + 22, y - 30, PdfFont.Semibold, 8f, Pdf.B2, tracking = 1.8f)
        text(money(total), M + 22, y - 64, PdfFont.Heavy, 32f, Pdf.White)
        if (trip.travellers.isNotEmpty()) {
            val n = trip.travellers.size
            text("Average ${money(total / n)} per traveller across $n ${if (n == 1) "person" else "people"}", M + 22, y - 82, PdfFont.Regular, 8.6f, Pdf.B1)
        }
        y -= 116

        // per-person tiles
        val paid = trip.travellers.map { t -> t to expenses.filter { it.paidByTravellerId == t.id }.sumOf { it.amount } }
        val tw = (CW - 20) / 3
        val th = 62f
        for ((i, p) in paid.withIndex()) {
            val (t, v) = p
            val x = M + (i % 3) * (tw + 10)
            val ty = y - th - (i / 3) * (th + 10)
            val col = colorFor(t.id)
            shadow(x, ty, tw, th, 11f)
            c.fillRoundRect(x, ty, tw, th, 11f, Pdf.White); c.strokeRoundRect(x, ty, tw, th, 11f, Pdf.Line, 0.8f)
            val name = t.firstName.ifBlank { "?" }
            avatar(x + 22, ty + th - 22, 13f, name, col, 11.5f)
            text(fit(name, PdfFont.Semibold, 10.5f, tw - 56).first, x + 42, ty + th - 26, PdfFont.Semibold, 10.5f, Pdf.Ink)
            text(fit(money(v), PdfFont.Bold, 15f, tw - 56, 10f).first, x + 12, ty + 16, PdfFont.Bold, 15f, Pdf.Navy)
            val share = if (total > 0) (v / total).toFloat() else 0f
            text("${round(share * 100).toInt()}%", x + tw - 12, ty + 17, PdfFont.Semibold, 9f, col, PdfAlign.Right)
            c.fillRoundRect(x + 12, ty + 6, tw - 24, 3.5f, 1.75f, Pdf.Tint2)
            c.fillRoundRect(x + 12, ty + 6, maxOf(4f, (tw - 24) * share), 3.5f, 1.75f, col)
        }
        y -= ((paid.size + 2) / 3) * (th + 10) + 14

        // receipt list
        ensure(80f)
        text("ALL EXPENSES", M, y, PdfFont.Semibold, 8f, Pdf.Faint, tracking = 1.8f)
        y -= 12
        val rh = 27f
        var i = 0
        while (i < expenses.size) {
            val avail = y - BOTTOM - 10
            val n = maxOf(1, (avail / rh).toInt())
            var chunk = expenses.subList(i, minOf(expenses.size, i + n))
            var last = i + chunk.size >= expenses.size
            if (last && chunk.size * rh + 12 + 44 > avail && chunk.size > 1) { chunk = chunk.dropLast(1); last = false }
            val ph = chunk.size * rh + 12 + if (last) 44 else 0
            c.fillRoundRect(M, y - ph, CW, ph, 12f, Pdf.White); c.strokeRoundRect(M, y - ph, CW, ph, 12f, Pdf.Line, 0.8f)
            var ry = y - 6
            for (e in chunk) {
                val (ic, col) = categoryStyle(e.category)
                c.icon(ic, M + 24, ry - 13, 12f, col)
                val (s, sz) = fit(e.description, PdfFont.Semibold, 10f, 300f)
                text(s, M + 40, ry - 11, PdfFont.Semibold, sz, Pdf.Ink)
                val payer = trip.travellers.firstOrNull { it.id == e.paidByTravellerId }?.firstName ?: "?"
                text("paid by $payer", M + 40, ry - 21, PdfFont.Regular, 7.6f, Pdf.Faint)
                val amt = money(e.amount)
                text(amt, W - M - 20, ry - 14, PdfFont.Bold, 10.5f, Pdf.Navy, PdfAlign.Right)
                val lx0 = M + 40 + c.textWidth(s, PdfFont.Semibold, sz) + 8
                val lx1 = W - M - 20 - c.textWidth(amt, PdfFont.Bold, 10.5f) - 8
                if (lx1 > lx0) c.line(lx0, ry - 11, lx1, ry - 11, Pdf.Line, 1f, floatArrayOf(1f, 3f))
                ry -= rh
            }
            if (last) {
                c.fillRoundRect(M + 12, ry - 40, CW - 24, 34f, 9f, Pdf.B5)
                text("TOTAL", M + 28, ry - 27, PdfFont.Semibold, 8.6f, Pdf.B1, tracking = 1.8f)
                text(money(total), W - M - 28, ry - 28, PdfFont.Heavy, 15f, Pdf.White, PdfAlign.Right)
            }
            i += chunk.size
            y -= ph + 8
            if (!last) nextPage()
        }
    }

    private fun categoryStyle(cat: ExpenseCategory): Pair<PdfIcon, Int> = when (cat) {
        ExpenseCategory.FOOD -> PdfIcon.Food to argb(0xE8A33D)
        ExpenseCategory.TRANSPORT -> PdfIcon.Ticket to Pdf.B4
        ExpenseCategory.ACCOMMODATION -> PdfIcon.Bed to argb(0x7C6FD0)
        ExpenseCategory.SHOPPING -> PdfIcon.Bag to argb(0xD9667A)
        ExpenseCategory.ACTIVITIES -> PdfIcon.Pin to argb(0x4FAF8A)
        ExpenseCategory.OTHER -> PdfIcon.Dot to Pdf.B3
    }

    private fun modeIcon(t: TransportType) = when (t) {
        TransportType.TRAIN -> PdfIcon.Train
        TransportType.BUS -> PdfIcon.Bus
        TransportType.FLIGHT -> PdfIcon.Flight
        TransportType.FERRY, TransportType.CAR -> PdfIcon.Route
    }

    // ── drawing helpers ──────────────────────────────────────────
    private fun text(s: String, x: Float, ty: Float, f: PdfFont, size: Float, col: Int, a: PdfAlign = PdfAlign.Left, tracking: Float = 0f) =
        c.text(s, x, ty, f, size, col, a, tracking)

    private fun avatar(cx: Float, cy: Float, r: Float, name: String, col: Int, size: Float) {
        c.fillCircle(cx, cy, r, col)
        text(name.take(1).uppercase(), cx, cy - r * 0.28f, PdfFont.Bold, size, Pdf.White, PdfAlign.Center)
    }

    private fun shadow(x: Float, sy: Float, w: Float, h: Float, r: Float) {
        for (k in intArrayOf(3, 2, 1)) {
            c.save(); c.alpha(0.045f)
            c.fillRoundRect(x - k * 0.6f, sy - 2.2f - k * 0.5f, w + k * 1.2f, h + k * 0.4f, r + k * 0.5f, Pdf.Shadow)
            c.restore()
        }
    }

    private fun fit(s: String, f: PdfFont, size: Float, maxW: Float, min: Float = 7.5f): Pair<String, Float> {
        var sz = size
        while (c.textWidth(s, f, sz) > maxW && sz > min) sz -= 0.25f
        if (c.textWidth(s, f, sz) <= maxW) return s to sz
        var t = s
        while (t.isNotEmpty() && c.textWidth("$t…", f, sz) > maxW) t = t.dropLast(1)
        return "$t…" to sz
    }

    private fun wrap(s: String, f: PdfFont, size: Float, maxW: Float): List<String> {
        val out = mutableListOf<String>()
        var cur = ""
        for (w in s.split(" ")) {
            val cand = if (cur.isEmpty()) w else "$cur $w"
            if (c.textWidth(cand, f, size) <= maxW || cur.isEmpty()) cur = cand else { out += cur; cur = w }
        }
        if (cur.isNotEmpty()) out += cur
        return out.ifEmpty { listOf("") }
    }

    // ── formatting ───────────────────────────────────────────────
    private fun money(v: Double): String {
        val cents = round(abs(v) * 100).toLong()
        val whole = (cents / 100).toString().reversed().chunked(3).joinToString(",").reversed()
        val frac = (cents % 100).toString().padStart(2, '0')
        return (if (v < 0) "-" else "") + sym + whole + "." + frac
    }

    private fun currencySymbol(code: String) = when (code.uppercase()) {
        "EUR" -> "€"; "USD" -> "$"; "GBP" -> "£"; "INR" -> "₹"; "JPY" -> "¥"
        else -> "${code.uppercase()} "
    }

    private fun monthName(d: LocalDate) = d.month.name.lowercase().replaceFirstChar { it.uppercase() }
    private fun weekdayName(d: LocalDate) = d.dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() }
    private fun daysBetween(a: LocalDate, b: LocalDate) = (b.toEpochDays() - a.toEpochDays()).toInt()

    private fun rangeShort(a: LocalDate, b: LocalDate) =
        "${a.day} ${monthName(a).take(3)} – ${b.day} ${monthName(b).take(3)} ${b.year}"

    private fun rangeLong(a: LocalDate, b: LocalDate) =
        if (a == b) "${a.day} ${monthName(a)} ${a.year}"
        else if (a.month == b.month && a.year == b.year) "${a.day} – ${b.day} ${monthName(b)} ${b.year}"
        else rangeShort(a, b)

    /** "4h 35m" between two HH:mm labels, rolling over midnight; null if either is missing/unparseable. */
    private fun duration(from: String, to: String): String? {
        fun mins(s: String): Int? {
            val p = s.trim().split(":")
            if (p.size < 2) return null
            return (p[0].toIntOrNull() ?: return null) * 60 + (p[1].take(2).toIntOrNull() ?: return null)
        }
        val a = mins(from) ?: return null
        val b = mins(to) ?: return null
        val d = ((b - a) % 1440 + 1440) % 1440
        return if (d / 60 > 0) "${d / 60}h ${(d % 60).toString().padStart(2, '0')}m" else "${d}m"
    }

    /** Same id-hash the Travellers screen uses, so a person keeps their colour in the PDF. */
    private fun colorFor(id: String): Int {
        if (id.isBlank()) return TravellerAvatarPalette[0].toArgb()
        val h = id.fold(0) { acc, ch -> acc * 31 + ch.code }
        return TravellerAvatarPalette[((h % TravellerAvatarPalette.size) + TravellerAvatarPalette.size) % TravellerAvatarPalette.size].toArgb()
    }
}
