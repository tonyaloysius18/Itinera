package com.itinera.app.ui.components

import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlin.math.abs

/**
 * A snapping wheel picker. Renders [values] in a vertically scrolling column;
 * the item centred in the viewport is selected. Neighbours fade and shrink with
 * distance from the centre.
 *
 * Height = [itemHeight] * [visibleCount]; [visibleCount] should be odd so there
 * is a clear centre row. The list is padded top & bottom by (visibleCount-1)/2
 * rows so the first/last real items can reach the centre, which means the
 * centred item index == state.firstVisibleItemIndex (after snapping).
 */
@Composable
fun <T> WheelPicker(
    values: List<T>,
    selectedIndex: Int,
    onSelectedIndexChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    visibleCount: Int = 5,
    itemHeight: Int = 40,
    label: (T) -> String = { it.toString() },
) {
    val state = rememberLazyListState(initialFirstVisibleItemIndex = selectedIndex)
    val fling = rememberSnapFlingBehavior(lazyListState = state)

    // After a snap, the item at the top of the viewport (firstVisibleItemIndex)
    // sits in the centre row thanks to the vertical content padding. While
    // mid-scroll we round by the partial offset for smooth highlight tracking.
    val centeredIndex by remember {
        derivedStateOf {
            val first = state.firstVisibleItemIndex
            val offsetPx = state.firstVisibleItemScrollOffset
            // itemHeight is in dp; offset is px. We only need a rough midpoint
            // test, and snapping makes offset ~0 at rest, so compare to half.
            if (offsetPx > 0 && state.layoutInfo.visibleItemsInfo.isNotEmpty()) {
                val itemPx = state.layoutInfo.visibleItemsInfo.first().size
                if (itemPx > 0 && offsetPx > itemPx / 2) first + 1 else first
            } else first
        }
    }

    // Report selection when scrolling settles.
    LaunchedEffect(state) {
        snapshotFlow { state.isScrollInProgress }
            .collect { scrolling ->
                if (!scrolling) {
                    onSelectedIndexChange(centeredIndex.coerceIn(0, values.lastIndex))
                }
            }
    }

    val edgeRows = (visibleCount - 1) / 2

    Box(
        modifier = modifier.height((itemHeight * visibleCount).dp),
        contentAlignment = Alignment.Center,
    ) {
        LazyColumn(
            state = state,
            flingBehavior = fling,
            contentPadding = PaddingValues(vertical = (itemHeight * edgeRows).dp),
        ) {
            itemsIndexed(values) { index, value ->
                val distance = abs(index - centeredIndex)
                val isCenter = index == centeredIndex
                Box(
                    Modifier.fillMaxWidth().height(itemHeight.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = label(value),
                        style = if (isCenter) MaterialTheme.typography.titleLarge
                        else MaterialTheme.typography.bodyLarge,
                        fontWeight = if (isCenter) FontWeight.SemiBold else FontWeight.Normal,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier
                            .alpha((1f - distance * 0.28f).coerceIn(0.25f, 1f))
                            .graphicsLayer {
                                val scale = (1f - distance * 0.08f).coerceIn(0.8f, 1f)
                                scaleX = scale; scaleY = scale
                            },
                    )
                }
            }
        }
    }
}