package com.takaapoo.weatherer.ui.screens.detail


import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.DecayAnimationSpec
import androidx.compose.animation.core.tween
import androidx.compose.animation.rememberSplineBasedDecay
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.TopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.unit.Velocity
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun customScrollBehavior(
    contentScrollState: ScrollState,
    state: TopAppBarState,
    topBarScrollLimit: Float,
    canScroll: () -> Boolean = { true },
): TopAppBarScrollBehavior {
//    val state = rememberTopAppBarState()
    val decayAnimationSpec = rememberSplineBasedDecay<Float>()
    val coroutineScope = rememberCoroutineScope()
    val flingAnimation = remember { Animatable(initialValue = state.heightOffset) }

    val scrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(
                available: Offset,
                source: NestedScrollSource,
            ): Offset {
                // Handle scroll events before they're consumed by children
                if (contentScrollState.value < topBarScrollLimit || !canScroll()) {
//                    return Offset.Zero // Return consumed offset
                } else  {
                    coroutineScope.launch {
                        state.heightOffset += available.y
                        flingAnimation.snapTo(state.heightOffset)
                    }
                }
                return Offset.Zero
            }

            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                // Handle fling
                if (state.heightOffset < state.heightOffsetLimit / 2) { // Flinging up
                    coroutineScope.launch {
                        launch {
                            contentScrollState.animateScrollBy(
                                value = -(state.heightOffsetLimit - state.heightOffset)
                            )
                        }
                        launch {
                            flingAnimation.animateTo(
                                targetValue = state.heightOffsetLimit,
                                initialVelocity = available.y,
                            ){
                                state.heightOffset = value
                            }
                        }
                    }
                    return Velocity.Zero
                } else if (state.heightOffset >= state.heightOffsetLimit / 2 && state.heightOffset < 0){ // Flinging down
                    coroutineScope.launch {
                        launch {
                            contentScrollState.animateScrollBy(
                                value = state.heightOffset
                            )
                        }
                        launch {
                            flingAnimation.animateTo(
                                targetValue = 0f,
                                initialVelocity = available.y,
                            ){
                                state.heightOffset = value
                            }
                        }
                    }
                    return Velocity.Zero
                }
                return available
            }
        }
    }

    return object : TopAppBarScrollBehavior {
        override val state: TopAppBarState = state
        override val nestedScrollConnection: NestedScrollConnection = scrollConnection
        override val isPinned: Boolean = true
        override val snapAnimationSpec: AnimationSpec<Float> = tween(durationMillis = 250)
        override val flingAnimationSpec: DecayAnimationSpec<Float> = decayAnimationSpec
    }
}
