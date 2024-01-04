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

package de.gematik.dsr.android.deviceRegistration

import AppTheme
import PaddingDefaults
import SpacerMedium
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Icon
import androidx.compose.material.IconToggleButton
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import de.gematik.dsr.android.DebugButton
import de.gematik.dsr.android.R
import de.gematik.dsr.android.components.AnimatedElevationScaffold
import de.gematik.dsr.android.components.DeviceRegistrationBottomBar
import de.gematik.dsr.android.components.scrollOnFocus

/**
 * DeviceRegistrationPinScreen.kt
 *
 * This file contains the DeviceRegistrationPinScreen,
 * on which the user has to enter his Pin. Any entered six to eight digits long number is accepted,
 * since this screen is for demonstration purposes only.
 */

val secretRange = 6..8

@Composable
fun DeviceRegistrationPinScreen(
    pin: String,
    onPinChange: (String) -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit,
    onOpenDebug: () -> Unit,
) {
    val listState = rememberLazyListState()
    AnimatedElevationScaffold(
        modifier = Modifier,
        topBarTitle = "",
        listState = listState,
        actions = {
            DebugButton(onOpenDebug)
        },
        bottomBar = {
            DeviceRegistrationBottomBar(
                onNext = onNext,
                nextEnabled = pin.length in secretRange,
            )
        },
        onBack = onBack,
    ) { innerPadding ->
        val contentPadding = remember {
            PaddingValues(
                top = PaddingDefaults.Medium,
                bottom = PaddingDefaults.Medium + innerPadding.calculateBottomPadding(),
                start = PaddingDefaults.Medium,
                end = PaddingDefaults.Medium,
            )
        }
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = listState,
            contentPadding = contentPadding,
        ) {
            item {
                Text(
                    stringResource(R.string.deviceRegistrationPinHeader),
                    style = AppTheme.typography.h5,
                )
                SpacerMedium()
            }
            item {
                Text(
                    stringResource(R.string.deviceRegistrationPinBody),
                    style = AppTheme.typography.body1,
                )
                SpacerMedium()
            }
            item {
                SecretInputField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .scrollOnFocus(to = 2, listState),
                    secretRange = secretRange,
                    onSecretChange = onPinChange,
                    secret = pin,
                    label = stringResource(R.string.cdw_pin_label),
                    onNext = onNext,
                )
            }
        }
    }
}

@Composable
fun SecretInputField(
    modifier: Modifier,
    secretRange: IntRange,
    onSecretChange: (String) -> Unit,
    isConsistent: Boolean = true,
    secret: String,
    label: String,
    onNext: () -> Unit,
) {
    val secretRegexString = "^\\d{0,${secretRange.last}}$"
    val secretRegex = secretRegexString.toRegex()
    var secretVisible by remember { mutableStateOf(false) }

    OutlinedTextField(
        modifier = modifier,
        value = secret,
        onValueChange = {
            if (it.matches(secretRegex)) {
                onSecretChange(it)
            }
        },
        label = { Text(label) },
        visualTransformation = if (secretVisible) {
            VisualTransformation.None
        } else {
            PasswordVisualTransformation()
        },
        keyboardOptions = KeyboardOptions(
            autoCorrect = false,
            keyboardType = KeyboardType.NumberPassword,
            imeAction = ImeAction.Next,
        ),
        shape = RoundedCornerShape(8.dp),
        colors = TextFieldDefaults.outlinedTextFieldColors(
            unfocusedLabelColor = AppTheme.colors.neutral400,
            placeholderColor = AppTheme.colors.neutral400,
            trailingIconColor = AppTheme.colors.neutral400,
        ),
        keyboardActions = KeyboardActions {
            if (isConsistent && secret.length in secretRange) {
                onNext()
            }
        },
        trailingIcon = {
            IconToggleButton(
                checked = secretVisible,
                onCheckedChange = { secretVisible = it },
            ) {
                Icon(
                    if (secretVisible) {
                        Icons.Rounded.Visibility
                    } else {
                        Icons.Rounded.VisibilityOff
                    },
                    null,
                )
            }
        },
    )
}
