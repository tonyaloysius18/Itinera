package com.itinera.app.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsBoat
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Train
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.util.lerp
import kotlin.math.absoluteValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.itinera.app.model.TransportType
import com.itinera.app.model.WalletTicket


@Composable
fun TicketWalletDialog(
    legRoute: String,          // e.g. "Toulouse → Paris"
    legDateLabel: String,      // e.g. "10 Jul"
    legTime: String,           // e.g. "10:27" (may be blank)
    operator: String,          // e.g. "SNCF" (may be blank)
    transport: TransportType,
    tickets: List<WalletTicket>,
    onOpenFullTicket: (docId: String) -> Unit,
    onDismiss: () -> Unit,

    ) {
    if (tickets.isEmpty()) return
    val pagerState = rememberPagerState(pageCount = { tickets.size })
    val multiDoc = tickets.map { it.docId }.distinct().size > 1
    val s = com.itinera.app.i18n.LocalStrings.current

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        KeepMaxBrightness()

        Box(Modifier.fillMaxSize().background(Color(0xF2101014))) {
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 48.dp),   // reveal neighbour cards
                    pageSpacing = 12.dp,
                ) { page ->
                    val ticket = tickets[page]
                    // distance of this page from the settled center (0 = centered, 1 = one away)
                    val fraction = (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
                    val dist = fraction.absoluteValue.coerceIn(0f, 1f)

                    WalletCard(
                        legRoute = legRoute,
                        legDateLabel = legDateLabel,
                        legTime = legTime,
                        operator = operator,
                        icon = transportIconFor(transport),
                        ticket = ticket,
                        showDocTitle = multiDoc && ticket.docTitle.isNotBlank(),
                        onOpenFullTicket = { onDismiss(); onOpenFullTicket(ticket.docId) },
                        modifier = Modifier.graphicsLayer {
                            val scale = lerp(0.84f, 1f, 1f - dist)
                            scaleX = scale
                            scaleY = scale
                            alpha = lerp(0.45f, 1f, 1f - dist)
                            rotationY = lerp(0f, 10f, fraction.coerceIn(-1f, 1f))
                            cameraDistance = 12f * density
                        },
                    )
                }

                if (tickets.size > 1) {
                    Spacer(Modifier.height(18.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        repeat(tickets.size) { i ->
                            Box(
                                Modifier.size(8.dp).clip(CircleShape)
                                    .background(
                                        if (pagerState.currentPage == i) Color.White
                                        else Color.White.copy(alpha = 0.35f),
                                    ),
                            )
                            if (i != tickets.size - 1) Spacer(Modifier.width(6.dp))
                        }
                    }
                }
            }

            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .statusBarsPadding()
                    .padding(12.dp)
                    .background(Color.White.copy(alpha = 0.12f), CircleShape),
            ) {
                Icon(Icons.Default.Close, contentDescription = s.close, tint = Color.White)
            }
        }
    }
}

@Composable
private fun WalletCard(
    legRoute: String,
    legDateLabel: String,
    legTime: String,
    operator: String,
    icon: ImageVector,
    ticket: WalletTicket,
    showDocTitle: Boolean,
    onOpenFullTicket: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val s = com.itinera.app.i18n.LocalStrings.current
    Surface(
        color = Color.White,
        shape = RoundedCornerShape(24.dp),
        shadowElevation = 8.dp,
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            Modifier.padding(horizontal = 22.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // header: transport + operator
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(36.dp).clip(CircleShape).background(Color(0xFFF0F1F4)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(icon, null, tint = Color(0xFF37474F), modifier = Modifier.size(20.dp))
                }
                Spacer(Modifier.width(10.dp))
                Text(
                    operator.ifBlank { s.ticketSingular },
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF37474F),
                )
            }

            Spacer(Modifier.height(14.dp))

            // route
            Text(
                ticket.routeOverride.ifBlank { legRoute },
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF111111),
                textAlign = TextAlign.Center,
                maxLines = 2, overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                listOf(legDateLabel, ticket.timeOverride.ifBlank { legTime })   // ⬅ CHANGED
                    .filter { it.isNotBlank() }.joinToString(" · "),
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF6B7280),
            )

            if (ticket.travellerName.isNotBlank()) {
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Person, null, tint = Color(0xFF6B7280), modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(ticket.travellerName, style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium, color = Color(0xFF37474F))
                }
            }

            Spacer(Modifier.height(16.dp))
            HorizontalDivider(color = Color(0xFFE5E7EB))
            Spacer(Modifier.height(16.dp))

            // the real scannable code
            val img = ticket.extraction.image
            val aspect = img.width.toFloat() / img.height.toFloat().coerceAtLeast(1f)
            Image(
                bitmap = img,
                contentDescription = s.ticketCode,
                modifier = Modifier
                    .fillMaxWidth(if (aspect > 2f) 0.98f else 0.78f)   // wide PDF417 vs square QR/Aztec
                    .aspectRatio(aspect),
                contentScale = ContentScale.Fit,
                filterQuality = FilterQuality.None,
            )

            if (showDocTitle) {
                Spacer(Modifier.height(10.dp))
                Text(
                    ticket.docTitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF9CA3AF),
                    maxLines = 1, overflow = TextOverflow.Ellipsis,
                )
            }

            Spacer(Modifier.height(12.dp))
            TextButton(onClick = onOpenFullTicket) {
                Icon(
                    Icons.AutoMirrored.Filled.InsertDriveFile, null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary,
                )
                Spacer(Modifier.width(6.dp))
                Text(s.viewFullTicket)
            }
        }
    }
}

private fun transportIconFor(t: TransportType): ImageVector = when (t) {
    TransportType.FLIGHT -> Icons.Filled.Flight
    TransportType.TRAIN  -> Icons.Filled.Train
    TransportType.BUS    -> Icons.Filled.DirectionsBus
    TransportType.FERRY  -> Icons.Filled.DirectionsBoat
    TransportType.CAR    -> Icons.Filled.DirectionsCar
}