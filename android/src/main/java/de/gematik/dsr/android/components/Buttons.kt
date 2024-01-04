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

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonColors
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.ButtonElevation
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DevicesOther
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

/**
 * Buttons.kt
 *
 * This file contains different buttonStyles which are used in the app
 */

@Composable
fun PrimaryButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    elevation: ButtonElevation? = ButtonDefaults.elevation(defaultElevation = 0.dp, pressedElevation = 4.dp),
    shape: Shape = RoundedCornerShape(8.dp),
    border: BorderStroke? = null,
    colors: ButtonColors = ButtonDefaults.buttonColors(),
    contentPadding: PaddingValues = PaddingValues(
        horizontal = PaddingDefaults.Large,
        vertical = PaddingDefaults.Small,
    ),
    content: @Composable RowScope.() -> Unit,
) =
    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        interactionSource = interactionSource,
        elevation = elevation,
        shape = shape,
        border = border,
        colors = colors,
        contentPadding = contentPadding,
        content = content,
    )

@Composable
fun NavigationBack(modifier: Modifier = Modifier, onClick: () -> Unit) {
    val acc = "Zurück"

    IconButton(
        onClick = onClick,
        modifier = modifier
            .semantics { contentDescription = acc },
    ) {
        Icon(
            Icons.Rounded.ArrowBack,
            null,
            tint = MaterialTheme.colors.primary,
            modifier = Modifier.size(24.dp),
        )
    }
}

@Composable
fun NavigationClose(modifier: Modifier = Modifier, onClick: () -> Unit) {
    val acc = "Abbrechen"

    IconButton(
        onClick = onClick,
        modifier = modifier
            .semantics { contentDescription = acc },
    ) {
        Icon(
            Icons.Rounded.Close,
            null,
            tint = MaterialTheme.colors.primary,
            modifier = Modifier.size(24.dp),
        )
    }
}

@Composable
fun NavigationDevices(onClick: () -> Unit) {
    IconButton(
        onClick = onClick,
    ) {
        Icon(
            Icons.Rounded.DevicesOther,
            null,
            tint = MaterialTheme.colors.primary,
            modifier = Modifier.size(24.dp),
        )
    }
}
