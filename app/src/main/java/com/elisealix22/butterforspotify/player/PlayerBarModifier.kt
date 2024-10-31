package com.elisealix22.butterforspotify.player

import android.util.Log
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.SpringSpec
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.awaitVerticalDragOrCancellation
import androidx.compose.foundation.gestures.awaitVerticalTouchSlopOrCancellation
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.input.pointer.util.addPointerInputChange
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import com.elisealix22.butterforspotify.R
import kotlin.math.roundToInt
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

private val MaximumVelocity = Velocity(1F, 500F)
private val AnimationSpec = SpringSpec<Float>(
    dampingRatio = Spring.DampingRatioNoBouncy,
    stiffness = Spring.StiffnessMediumLow
)

@Serializable
enum class PlayerBarExpandState {
    Expanded,
    Collapsed
}

@Composable
fun Modifier.expandablePlayerBar(
    collapsedHeight: Dp,
    containerWidth: Dp,
    containerHeight: Dp,
    horizontalPadding: Dp,
    enabled: Boolean = true,
    expandState: MutableState<PlayerBarExpandState> =
        remember { mutableStateOf(PlayerBarExpandState.Collapsed) },
    onExpandChange: (offset: Float) -> Unit = {}
): Modifier {
    val playerBarHeight = remember(collapsedHeight, containerHeight) {
        Animatable(
            if (expandState.value == PlayerBarExpandState.Expanded) {
                containerHeight.value
            } else {
                collapsedHeight.value
            }
        ).apply {
            updateBounds(lowerBound = collapsedHeight.value, upperBound = containerHeight.value)
        }
    }
//    val playerBarWidth = remember(playerBarHeight) {
//    }
    val scope = rememberCoroutineScope()
    val animateExpand: (expandUp: Boolean) -> Unit = { expandUp ->
        scope.launch {
            if (expandUp) {
                playerBarHeight.stop()
                playerBarHeight.animateTo(
                    targetValue = playerBarHeight.upperBound ?: error("Upper bound not set"),
                    animationSpec = AnimationSpec
                )
            } else {
                playerBarHeight.stop()
                playerBarHeight.animateTo(
                    targetValue = playerBarHeight.lowerBound ?: error("Lower bound not set"),
                    animationSpec = AnimationSpec
                )
            }
        }
    }
    LaunchedEffect(expandState.value) {
        when (expandState.value) {
            PlayerBarExpandState.Expanded -> {
                if (!playerBarHeight.isExpanded()) animateExpand(true)
            }
            PlayerBarExpandState.Collapsed -> {
                if (!playerBarHeight.isCollapsed()) animateExpand(false)
            }
        }
    }

    val offset = playerBarHeight.calculateExpandOffset(containerHeight)
            Log.e("###", "Size changed: ${offset}")
    val minPlayerBarWidth = containerWidth.value - horizontalPadding.times(2).value
    val expandableWidth = containerWidth.value - minPlayerBarWidth
    val width: Float = minPlayerBarWidth + (offset * expandableWidth)

//    onExpandChange(offset)
//    expandState.value = if (offset == 1F) {
//        PlayerBarExpandState.Expanded
//    } else {
//        PlayerBarExpandState.Collapsed
//    }

    return this
        .size(
            width = (minPlayerBarWidth + (offset * expandableWidth)).dp,
            height = playerBarHeight.value.dp
        )
        .onSizeChanged {
//            val expandOffset = playerBarHeight.calculateExpandOffset(containerHeight)
//            Log.e("###", "Size changed: ${expandOffset}")
            expandState.value = if (offset == 1F) {
                PlayerBarExpandState.Expanded
            } else {
                PlayerBarExpandState.Collapsed
            }
            onExpandChange(offset)
        }
        .clickable(
            enabled = enabled && playerBarHeight.isCollapsed(),
            onClickLabel = stringResource(R.string.open_fullscreen_player),
            onClick = { animateExpand(true) },
            indication = null,
            interactionSource = null
        )
        .pointerInput(Unit) {
            if (!enabled) return@pointerInput
            coroutineScope {
                val velocityTracker = VelocityTracker()
                awaitEachGesture {
                    var isMovingUp = false
                    var isDragging = false
                    val down = awaitFirstDown()
                    var change =
                        awaitVerticalTouchSlopOrCancellation(down.id) { change, _ ->
                            velocityTracker.resetTracking()
                            change.consume()
                        }
                    while (change != null && change.pressed) {
                        change = awaitVerticalDragOrCancellation(change.id)
                        if (change != null && change.pressed) {
                            velocityTracker.addPointerInputChange(change)
                            isMovingUp = change.previousPosition.y > change.position.y
                            isDragging = change.previousPosition.y != change.position.y
                            val changeDp = change.positionChange().y.toDp().value
                            val targetValue = playerBarHeight.value - changeDp
                            change.consume()
                            launch {
                                playerBarHeight.snapTo(targetValue)
                            }
                        }
                    }
                    val endHeight = if (isMovingUp) {
                        playerBarHeight.upperBound
                    } else {
                        playerBarHeight.lowerBound
                    } ?: error("Player bar bounds not set.")
                    val velocity = velocityTracker.calculateVelocity(MaximumVelocity).y
                    if (isDragging && endHeight != playerBarHeight.value) {
                        launch {
                            playerBarHeight.animateTo(
                                targetValue = endHeight,
                                initialVelocity = velocity,
                                animationSpec = AnimationSpec
                            )
                        }
                    }
                }
            }
        }
}

/**
 * @return Value between 0F and 1F where 0F is collapsed and 1F is expanded.
 */
private fun Animatable<Float, AnimationVector1D>.calculateExpandOffset(containerHeight: Dp): Float {
    val collapsedHeight: Dp = lowerBound?.dp ?: return 0F
    val availableHeight: Dp = containerHeight - collapsedHeight
    val traveledHeight = value.dp - collapsedHeight
    Log.e("###", "OFFSET: ${traveledHeight.div(availableHeight)}")
    return traveledHeight.div(availableHeight)
}

private fun Animatable<Float, AnimationVector1D>.isExpanded(): Boolean {
    return value == upperBound
}

private fun Animatable<Float, AnimationVector1D>.isCollapsed(): Boolean {
    return value == lowerBound
}
