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
import SpacerXXLarge
import android.media.Image
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import de.gematik.dsr.android.DebugButton
import de.gematik.dsr.android.R
import de.gematik.dsr.android.components.AnimatedElevationScaffold
import de.gematik.dsr.android.components.DeviceRegistrationBottomBar
import de.gematik.dsr.android.components.scrollOnFocus

/**
 * DeviceRegistrationCanScreen.kt
 *
 * This file contains the DeviceRegistrationCanScreen,
 * on which the user has to enter his CAN. Any entered six digits long number is accepted,
 * since this screen is for demonstration purposes only.
 */

const val EXPECTED_CAN_LENGTH = 6

@Composable
fun DeviceRegistrationCanScreen(
    can: String,
    onCanChange: (String) -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit,
    onOpenDebug: () -> Unit,
) {
    val listState = rememberLazyListState()
    AnimatedElevationScaffold(
        modifier = Modifier,
        topBarTitle = "",
        listState = listState,
        bottomBar = {
            DeviceRegistrationBottomBar(
                onNext = onNext,
                nextEnabled = can.length == EXPECTED_CAN_LENGTH,
            )
        },
        onBack = onBack,
        actions = {
            DebugButton(onOpenDebug)
        },
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
                HealthCardCanImage()
                SpacerXXLarge()
            }
            item {
                Text(
                    stringResource(R.string.deviceRegistrationCanHeader),
                    style = AppTheme.typography.h5,
                )
                SpacerMedium()
            }
            item {
                Text(
                    stringResource(R.string.deviceRegistrationCanBody),
                    style = AppTheme.typography.body1,
                )
                SpacerMedium()
            }
            item {
                CanInputField(
                    modifier = Modifier.scrollOnFocus(to = 2, listState),
                    can = can,
                    onCanChange = onCanChange,
                    next = onNext,
                )
            }
        }
    }
}

@Composable
fun HealthCardCanImage() {
    Column(modifier = Modifier.wrapContentHeight()) {
        Image(
            painterResource(R.drawable.card_wall_card_can),
            null,
            contentScale = ContentScale.FillWidth,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
fun CanInputField(
    modifier: Modifier,
    can: String,
    onCanChange: (String) -> Unit,
    next: () -> Unit,
) {
    val canRegex = """^\d{0,6}$""".toRegex()

    OutlinedTextField(
        modifier = modifier
            .fillMaxWidth(),
        value = can,
        onValueChange = {
            if (it.matches(canRegex)) {
                onCanChange(it)
            }
        },
        label = { Text(stringResource(R.string.can_input_field_label)) },
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
            if (can.length == EXPECTED_CAN_LENGTH) {
                next()
            }
        },
    )
}
