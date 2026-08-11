package com.itinera.app.ui.components

import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.zIndex
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.math.abs

@Stable
class ReorderableLazyListState internal constructor(
    val listState: LazyListState,
    private val canMoveOver: (String) -> Boolean,
    private val onMove: (draggedKey: String, targetKey: String) -> Unit,
    private val onDrop: () -> Unit,
) {
    private var draggedKey by mutableStateOf<String?>(null)
    private var initialItemOffset by mutableFloatStateOf(0f)
    private var draggedDistance by mutableFloatStateOf(0f)
    private var movePendingFromIndex: Int? = null

    fun isDragging(key: String): Boolean = draggedKey == key

    fun translationY(key: String): Float {
        if (draggedKey != key) return 0f
        val item = listState.layoutInfo.visibleItemsInfo.firstOrNull { it.key == key } ?: return 0f
        return initialItemOffset + draggedDistance - item.offset
    }

    fun startDragging(key: String) {
        val item = listState.layoutInfo.visibleItemsInfo.firstOrNull { it.key == key } ?: return
        draggedKey = key
        initialItemOffset = item.offset.toFloat()
        draggedDistance = 0f
        movePendingFromIndex = null
    }

    /** Returns a small scroll delta when the dragged card approaches a viewport edge. */
    fun dragBy(deltaY: Float): Float {
        val key = draggedKey ?: return 0f
        draggedDistance += deltaY

        val layoutInfo = listState.layoutInfo
        val current = layoutInfo.visibleItemsInfo.firstOrNull { it.key == key } ?: return 0f
        if (movePendingFromIndex != null && current.index != movePendingFromIndex) {
            movePendingFromIndex = null
        }

        val displacement = initialItemOffset + draggedDistance - current.offset
        val draggedTop = current.offset + displacement
        val draggedBottom = draggedTop + current.size

        if (movePendingFromIndex == null) {
            val candidates = layoutInfo.visibleItemsInfo.filter {
                it.key != key && canMoveOver(it.key as? String ?: return@filter false)
            }
            val target = when {
                displacement > 0f -> candidates
                    .filter { it.index > current.index && draggedBottom > it.offset + it.size / 2f }
                    .maxByOrNull { it.index }
                displacement < 0f -> candidates
                    .filter { it.index < current.index && draggedTop < it.offset + it.size / 2f }
                    .minByOrNull { it.index }
                else -> null
            }
            val targetKey = target?.key as? String
            if (targetKey != null) {
                movePendingFromIndex = current.index
                onMove(key, targetKey)
            }
        }

        val edgeThreshold = 72f
        return when {
            draggedBottom > layoutInfo.viewportEndOffset - edgeThreshold ->
                ((draggedBottom - (layoutInfo.viewportEndOffset - edgeThreshold)) / edgeThreshold * 22f)
                    .coerceIn(0f, 22f)
            draggedTop < layoutInfo.viewportStartOffset + edgeThreshold ->
                -(((layoutInfo.viewportStartOffset + edgeThreshold) - draggedTop) / edgeThreshold * 22f)
                    .coerceIn(0f, 22f)
            else -> 0f
        }
    }

    suspend fun scrollBy(delta: Float) {
        if (abs(delta) > 0.5f) listState.scrollBy(delta)
    }

    fun finishDragging() {
        if (draggedKey != null) onDrop()
        draggedKey = null
        initialItemOffset = 0f
        draggedDistance = 0f
        movePendingFromIndex = null
    }
}

@Composable
fun rememberReorderableLazyListState(
    listState: LazyListState,
    canMoveOver: (String) -> Boolean = { true },
    onMove: (draggedKey: String, targetKey: String) -> Unit,
    onDrop: () -> Unit,
): ReorderableLazyListState {
    val latestCanMoveOver by rememberUpdatedState(canMoveOver)
    val latestOnMove by rememberUpdatedState(onMove)
    val latestOnDrop by rememberUpdatedState(onDrop)
    return remember(listState) {
        ReorderableLazyListState(
            listState = listState,
            canMoveOver = { latestCanMoveOver(it) },
            onMove = { dragged, target -> latestOnMove(dragged, target) },
            onDrop = { latestOnDrop() },
        )
    }
}

fun Modifier.reorderableItem(
    state: ReorderableLazyListState,
    key: String,
    itemLabel: String,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
): Modifier = this
    .zIndex(if (state.isDragging(key)) 1f else 0f)
    .graphicsLayer {
        translationY = state.translationY(key)
        shadowElevation = if (state.isDragging(key)) 12f else 0f
    }
    .semantics {
        customActions = buildList {
            if (canMoveUp) {
                add(CustomAccessibilityAction("Move $itemLabel up") {
                    onMoveUp()
                    true
                })
            }
            if (canMoveDown) {
                add(CustomAccessibilityAction("Move $itemLabel down") {
                    onMoveDown()
                    true
                })
            }
        }
    }
    .pointerInput(state, key) {
        coroutineScope {
            detectDragGesturesAfterLongPress(
                onDragStart = { state.startDragging(key) },
                onDragCancel = { state.finishDragging() },
                onDragEnd = { state.finishDragging() },
                onDrag = { change, dragAmount ->
                    change.consume()
                    val scrollDelta = state.dragBy(dragAmount.y)
                    if (abs(scrollDelta) > 0.5f) {
                        launch { state.scrollBy(scrollDelta) }
                    }
                },
            )
        }
    }
