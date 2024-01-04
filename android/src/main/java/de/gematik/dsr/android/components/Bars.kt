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

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.AppBarDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.contentColorFor
import androidx.compose.material.primarySurface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import de.gematik.dsr.android.R

/**
 * Bars.kt
 *
 * This file contains different stylings of top- and bottomBars which are used in the app
 */

enum class NavigationBarMode {
    Back,
    Close,
    Devices,
}

@Composable
fun NavigationTopAppBar(
    navigationMode: NavigationBarMode?,
    title: String,
    backgroundColor: Color = MaterialTheme.colors.surface,
    elevation: Dp = AppBarDefaults.TopAppBarElevation,
    actions: @Composable RowScope.() -> Unit = {},
    onBack: () -> Unit,
) = TopAppBar(
    title = {
        Text(title, overflow = TextOverflow.Ellipsis)
    },
    backgroundColor = backgroundColor,
    navigationIcon = {
        when (navigationMode) {
            NavigationBarMode.Back -> NavigationBack { onBack() }
            NavigationBarMode.Close -> NavigationClose { onBack() }
            NavigationBarMode.Devices -> NavigationDevices { onBack() }
            else -> {}
        }
    },
    elevation = elevation,
    actions = actions,
)

@Composable
fun TopAppBar(
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    backgroundColor: Color = MaterialTheme.colors.primarySurface,
    contentColor: Color = contentColorFor(backgroundColor),
    elevation: Dp = AppBarDefaults.TopAppBarElevation,
) {
    Surface(
        modifier = modifier,
        color = backgroundColor,
        elevation = elevation,
        shape = RectangleShape,
    ) {
        androidx.compose.material.TopAppBar(
            title,
            Modifier.statusBarsPadding(),
            navigationIcon,
            actions,
            backgroundColor,
            contentColor,
            elevation = 0.dp,
        )
    }
}

@Composable
fun BottomAppBar(
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colors.surface,
    contentColor: Color = contentColorFor(backgroundColor),
    cutoutShape: Shape? = null,
    elevation: Dp = AppBarDefaults.BottomAppBarElevation,
    contentPadding: PaddingValues = AppBarDefaults.ContentPadding,
    content: @Composable RowScope.() -> Unit,
) {
    Surface(
        modifier = modifier,
        color = backgroundColor,
        elevation = elevation,
    ) {
        androidx.compose.material.BottomAppBar(
            Modifier.navigationBarsPadding(),
            backgroundColor,
            contentColor,
            cutoutShape,
            elevation = 0.dp,
            contentPadding,
            content,
        )
    }
}

@Composable
fun DeviceRegistrationBottomBar(
    onNext: () -> Unit,
    nextEnabled: Boolean,
    nextText: String = stringResource(R.string.next),
    aligment: Alignment.Horizontal = Alignment.End,
) {
    BottomAppBar {
        Column(
            Modifier
                .fillMaxWidth(),
        ) {
            PrimaryButton(
                onClick = onNext,
                enabled = nextEnabled,
                modifier = Modifier
                    .padding(
                        horizontal = PaddingDefaults.Medium,
                        vertical = PaddingDefaults.ShortMedium,
                    )
                    .align(aligment),
            ) {
                Text(
                    nextText,
                )
            }
        }
    }
}
