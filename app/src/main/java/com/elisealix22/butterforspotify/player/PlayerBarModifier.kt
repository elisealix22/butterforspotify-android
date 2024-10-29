package com.elisealix22.butterforspotify.player

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.SpringSpec
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.awaitVerticalDragOrCancellation
import androidx.compose.foundation.gestures.awaitVerticalTouchSlopOrCancellation
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.input.pointer.util.addPointerInputChange
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
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
    containerWidth: Dp,
    containerHeight: Dp,
    horizontalPadding: Dp,
    enabled: Boolean = true,
    expandState: MutableState<PlayerBarExpandState> =
        remember { mutableStateOf(PlayerBarExpandState.Collapsed) },
    onExpandChange: (offset: Float) -> Unit = {}
): Modifier {
    val playerBarHeight = remember {
        Animatable(
            if (expandState.value == PlayerBarExpandState.Expanded) {
                containerHeight.value
            } else {
                PlayerBarHeight.value
            }
        ).apply {
            updateBounds(lowerBound = PlayerBarHeight.value, upperBound = containerHeight.value)
        }
    }
    val minPlayerBarWidth = containerWidth.value - horizontalPadding.times(2).value
    val playerBarWidth = remember {
        derivedStateOf {
            val expandOffset = calculateExpandOffset(playerBarHeight.value.dp, containerHeight)
            val expandableWidth = containerWidth.value - minPlayerBarWidth
            minPlayerBarWidth + (expandOffset * expandableWidth)
        }
    }
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
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
    return this
        .size(width = playerBarWidth.value.dp, height = playerBarHeight.value.dp)
        .onSizeChanged {
            val expandOffset = calculateExpandOffset(playerBarHeight.value.dp, containerHeight)
            expandState.value = if (expandOffset == 1F) {
                PlayerBarExpandState.Expanded
            } else {
                PlayerBarExpandState.Collapsed
            }
            onExpandChange(expandOffset)
        }
        .clickable(
            enabled = enabled && playerBarHeight.isCollapsed(),
            onClickLabel = stringResource(R.string.open_fullscreen_player),
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                animateExpand(true)
            },
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
private fun calculateExpandOffset(playerBarHeight: Dp, containerHeight: Dp): Float {
    val availableHeight = containerHeight.value - PlayerBarHeight.value
    val traveledHeight = playerBarHeight.value - PlayerBarHeight.value
    return traveledHeight.roundToInt() / availableHeight
}

private fun Animatable<Float, AnimationVector1D>.isExpanded(): Boolean {
    return value == upperBound
}

private fun Animatable<Float, AnimationVector1D>.isCollapsed(): Boolean {
    return value == lowerBound
}
