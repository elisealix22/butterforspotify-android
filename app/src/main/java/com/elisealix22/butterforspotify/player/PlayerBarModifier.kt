package com.elisealix22.butterforspotify.player

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector2D
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.VectorConverter
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.awaitVerticalDragOrCancellation
import androidx.compose.foundation.gestures.awaitVerticalTouchSlopOrCancellation
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.input.pointer.util.addPointerInputChange
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import com.elisealix22.butterforspotify.R
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

private val MaximumVelocity = Velocity(1F, 500F)
private val ExpandAnimationSpec = SpringSpec<Size>(
    dampingRatio = Spring.DampingRatioNoBouncy,
    stiffness = Spring.StiffnessMediumLow
)
private val CollapseAnimationSpec = SpringSpec<Size>(
    dampingRatio = Spring.DampingRatioNoBouncy,
    stiffness = Spring.StiffnessMedium
)

@Serializable
enum class PlayerBarExpandState {
    Expanded,
    Collapsed
}

fun PlayerBarExpandState.initialOffset(): Float = when (this) {
    PlayerBarExpandState.Expanded -> 1F
    PlayerBarExpandState.Collapsed -> 0F
}

@Composable
fun Modifier.expandablePlayerBar(
    collapsedHeight: Dp,
    collapsedHorizontalPadding: Dp,
    containerWidth: Dp,
    containerHeight: Dp,
    enabled: Boolean = true,
    expandState: PlayerBarExpandState = PlayerBarExpandState.Collapsed,
    onExpandOffsetChange: (newOffset: Float) -> Unit
): Modifier {
    val leftInsetPadding = WindowInsets.safeDrawing
        .asPaddingValues().calculateLeftPadding(LayoutDirection.Ltr)
    val rightInsetPadding = WindowInsets.safeDrawing
        .asPaddingValues().calculateRightPadding(LayoutDirection.Ltr)
    val minPlayerBarWidth: Float = containerWidth.value
        .minus(collapsedHorizontalPadding.times(2).value)
        .plus(leftInsetPadding.value)
        .plus(rightInsetPadding.value)
    val maxPlayerBarWidth: Float = containerWidth.value
    val playerBarSize = remember(collapsedHeight, containerHeight, minPlayerBarWidth) {
        val lowerBound = Size(minPlayerBarWidth, collapsedHeight.value)
        val upperBound = Size(maxPlayerBarWidth, containerHeight.value)
        Animatable(
            if (expandState == PlayerBarExpandState.Expanded) {
                upperBound
            } else {
                lowerBound
            },
            Size.VectorConverter
        ).apply {
            updateBounds(lowerBound = lowerBound, upperBound = upperBound)
        }
    }
    val scope = rememberCoroutineScope()
    val animateExpand: (expandUp: Boolean) -> Unit = { expandUp ->
        scope.launch {
            if (expandUp) {
                playerBarSize.stop()
                playerBarSize.animateTo(
                    targetValue = playerBarSize.upperBound ?: error("Upper bound not set"),
                    animationSpec = ExpandAnimationSpec
                )
            } else {
                playerBarSize.stop()
                playerBarSize.animateTo(
                    targetValue = playerBarSize.lowerBound ?: error("Lower bound not set"),
                    animationSpec = CollapseAnimationSpec
                )
            }
        }
    }
    LaunchedEffect(expandState) {
        when (expandState) {
            PlayerBarExpandState.Expanded -> {
                if (!playerBarSize.isExpanded()) animateExpand(true)
            }
            PlayerBarExpandState.Collapsed -> {
                if (!playerBarSize.isCollapsed()) animateExpand(false)
            }
        }
    }

    val expandOffset = remember(playerBarSize.value) {
        val newExpandOffset = playerBarSize.calculateExpandOffset(containerHeight)
        onExpandOffsetChange(newExpandOffset)
        newExpandOffset
    }

    return this
        .offset(
            x = 1F.minus(expandOffset).times(
                leftInsetPadding.div(2).value.minus(rightInsetPadding.div(2).value)
            ).dp
        )
        .size(
            width = playerBarSize.value.width.dp,
            height = playerBarSize.value.height.dp
        )
        .clickable(
            enabled = enabled && playerBarSize.isCollapsed(),
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
                            val changeDp = change.positionChange().y.toDp()
                            val targetHeight = playerBarSize.value.height.dp - changeDp
                            change.consume()
                            launch {
                                val availableHeight: Dp = containerHeight - collapsedHeight
                                val traveledHeight: Dp = targetHeight - collapsedHeight
                                val widthOffset = traveledHeight.div(availableHeight)
                                val availableWidth = maxPlayerBarWidth - minPlayerBarWidth
                                val targetWidth = minPlayerBarWidth + (widthOffset * availableWidth)
                                playerBarSize.snapTo(
                                    Size(targetWidth, targetHeight.value)
                                )
                            }
                        }
                    }
                    val endSize = if (isMovingUp) {
                        playerBarSize.upperBound
                    } else {
                        playerBarSize.lowerBound
                    } ?: error("Player bar bounds not set.")
                    val velocity = velocityTracker.calculateVelocity(MaximumVelocity).let {
                        Size(it.x, it.y)
                    }
                    if (isDragging && endSize != playerBarSize.value) {
                        launch {
                            playerBarSize.animateTo(
                                targetValue = endSize,
                                initialVelocity = velocity,
                                animationSpec = if (isMovingUp) {
                                    ExpandAnimationSpec
                                } else {
                                    CollapseAnimationSpec
                                }
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
private fun Animatable<Size, AnimationVector2D>.calculateExpandOffset(containerHeight: Dp): Float {
    val collapsedHeight: Dp = lowerBound?.height?.dp ?: return 0F
    val availableHeight: Dp = containerHeight - collapsedHeight
    val traveledHeight = value.height.dp - collapsedHeight
    return traveledHeight.div(availableHeight)
}

private fun Animatable<Size, AnimationVector2D>.isExpanded(): Boolean {
    return value.height == upperBound?.height
}

private fun Animatable<Size, AnimationVector2D>.isCollapsed(): Boolean {
    return value.height == lowerBound?.height
}
