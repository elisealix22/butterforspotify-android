package com.elisealix22.butterforspotify.player

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.SpringSpec
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.awaitVerticalDragOrCancellation
import androidx.compose.foundation.gestures.awaitVerticalTouchSlopOrCancellation
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
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
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

private val MaximumVelocity = Velocity(1F, 100F)
private val AnimationSpec = SpringSpec<Float>(
    dampingRatio = Spring.DampingRatioNoBouncy,
    stiffness = Spring.StiffnessMediumLow
)

@Composable
fun Modifier.expandablePlayerBar(
    containerWidth: Dp,
    containerHeight: Dp,
    horizontalPadding: Dp,
    enabled: Boolean = true,
    onExpandChange: (offset: Float) -> Unit = {}
): Modifier {
    val playerBarHeight = remember {
        Animatable(PlayerBarHeight.value).apply {
            updateBounds(lowerBound = PlayerBarHeight.value, upperBound = containerHeight.value)
        }
    }
    // Value between 0F and 1F where 0F is collapsed and 1F is expanded.
    val expandedOffset by remember {
        derivedStateOf {
            val availableHeight = containerHeight.value - PlayerBarHeight.value
            val traveledHeight = playerBarHeight.value - PlayerBarHeight.value
            traveledHeight.roundToInt() / availableHeight
        }
    }
    val minPlayerBarWidth = containerWidth.value - horizontalPadding.times(2).value
    val playerBarWidth = remember {
        derivedStateOf {
            val expandableWidth = containerWidth.value - minPlayerBarWidth
            minPlayerBarWidth + (expandedOffset * expandableWidth)
        }
    }
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    var isMovingUp = false
    return this
        .size(width = playerBarWidth.value.dp, height = playerBarHeight.value.dp)
        .onSizeChanged { onExpandChange(expandedOffset) }
        .clickable(
            enabled = enabled && expandedOffset == 0F,
            onClickLabel = stringResource(R.string.open_fullscreen_player),
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                scope.launch {
                    playerBarHeight.animateTo(
                        targetValue = containerHeight.value,
                        animationSpec = AnimationSpec
                    )
                }
            },
            indication = null,
            interactionSource = null
        )
        .pointerInput(Unit) {
            if (!enabled) return@pointerInput
            coroutineScope {
                val velocityTracker = VelocityTracker()
                awaitEachGesture {
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
                        if (endHeight != playerBarHeight.value) {
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
