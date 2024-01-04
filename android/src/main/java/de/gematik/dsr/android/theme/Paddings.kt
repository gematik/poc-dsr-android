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

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Padding.kt
 *
 * This file contains different Paddings and Spacings which might be used in the app
 */

object PaddingDefaults {
    val Tiny = 4.dp
    val Small = 8.dp
    val ShortMedium = 12.dp
    val Medium = 16.dp
    val Large = 24.dp
    val XLarge = 32.dp
    val XXLarge = 40.dp
    val XXLargeMedium = 56.dp
}

@Composable
fun SpacerTiny() =
    Spacer(modifier = Modifier.size(PaddingDefaults.Tiny))

@Composable
fun SpacerSmall() =
    Spacer(modifier = Modifier.size(PaddingDefaults.Small))

@Composable
fun SpacerMedium() =
    Spacer(modifier = Modifier.size(PaddingDefaults.Medium))

@Composable
fun SpacerShortMedium() =
    Spacer(modifier = Modifier.size(PaddingDefaults.ShortMedium))

@Composable
fun SpacerLarge() =
    Spacer(modifier = Modifier.size(PaddingDefaults.Large))

@Composable
fun SpacerXLarge() =
    Spacer(modifier = Modifier.size(PaddingDefaults.XLarge))

@Composable
fun SpacerXXLarge() =
    Spacer(modifier = Modifier.size(PaddingDefaults.XXLarge))

@Composable
fun SpacerXXLargeMedium() =
    Spacer(modifier = Modifier.size(PaddingDefaults.XXLargeMedium))
