/*
 * Copyright (c) 2024 gematik GmbH
 * 
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the Licence);
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 *     https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * 
 */

package de.gematik.dsr.android.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.MutatePriority
import androidx.compose.foundation.MutatorMutex
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.focus.onFocusChanged
import androidx.navigation.NavHostController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Animations.kt
 *
 * This file contains different animations which are used in the app
 */

enum class NavigationMode {
    Forward,
    Back,
    Closed,
    Open,
}

@Composable
fun NavigationAnimation(
    modifier: Modifier = Modifier,
    mode: NavigationMode = NavigationMode.Forward,
    content: @Composable AnimatedVisibilityScope.() -> Unit,
) {
    val transition = when (mode) {
        NavigationMode.Forward -> slideInHorizontally(initialOffsetX = { it / 2 })
        NavigationMode.Back -> slideInHorizontally(initialOffsetX = { -it / 2 })
        NavigationMode.Closed -> slideInVertically(initialOffsetY = { -it / 3 })
        NavigationMode.Open -> slideInVertically(initialOffsetY = { it / 2 })
    }
    AnimatedVisibility(
        visibleState = remember { MutableTransitionState(false) }.apply { targetState = true },
        modifier = modifier,
        enter = transition,
        exit = ExitTransition.None,
        content = content,
    )
}

@Composable
fun NavHostController.navigationModeState(
    startDestination: String,
    intercept: ((previousRoute: String?, currentRoute: String?) -> NavigationMode?)? = null,
): State<NavigationMode> {
    var prevNumOfEntries by rememberSaveable(this, startDestination) { mutableIntStateOf(-1) }
    var prevRoute by rememberSaveable(this, startDestination) { mutableStateOf<String?>(null) }

    return produceState(intercept?.invoke(null, startDestination) ?: NavigationMode.Open) {
        this@navigationModeState.currentBackStackEntryFlow.collect {
            val currentRoute = it.destination.route
            val interceptedMode = when {
                intercept != null -> intercept(prevRoute, currentRoute)
                else -> null
            }

            value = interceptedMode ?: when {
                prevNumOfEntries == -1 && currentRoute == startDestination -> NavigationMode.Open
                this@navigationModeState.currentBackStack.value.size < prevNumOfEntries -> NavigationMode.Back
                this@navigationModeState.currentBackStack.value.size > prevNumOfEntries -> NavigationMode.Forward
                else -> NavigationMode.Open
            }

            prevRoute = currentRoute
            prevNumOfEntries = this@navigationModeState.currentBackStack.value.size
        }
    }
}

private const val LayoutDelay = 330L

@OptIn(ExperimentalLayoutApi::class)
fun Modifier.scrollOnFocus(to: Int, listState: LazyListState, offset: Int = 0) = composed {
    val coroutineScope = rememberCoroutineScope()
    val mutex = MutatorMutex()

    var hasFocus by remember { mutableStateOf(false) }
    val keyboardVisible = WindowInsets.isImeVisible

    LaunchedEffect(hasFocus, keyboardVisible) {
        if (hasFocus && keyboardVisible) {
            mutex.mutate {
                delay(LayoutDelay)
                listState.animateScrollToItem(to, offset)
            }
        }
    }

    onFocusChanged {
        if (it.hasFocus) {
            hasFocus = true
            coroutineScope.launch {
                mutex.mutate(MutatePriority.UserInput) {
                    delay(LayoutDelay)
                    listState.animateScrollToItem(to, offset)
                }
            }
        } else {
            hasFocus = false
        }
    }
}
