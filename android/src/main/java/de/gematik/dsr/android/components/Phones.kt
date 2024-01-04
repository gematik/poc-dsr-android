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

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import de.gematik.dsr.android.R
import de.gematik.dsr.common.utils.state.DeviceAttestationState
import de.gematik.dsr.common.utils.state.ScreenState
import de.gematik.dsr.common.utils.state.UIState

/**
 * Phone.kt
 *
 * This file contains screen contents to show the user whether their phone is secure or not
 */

@Composable
private fun PhoneAndIcon(
    icon: @Composable () -> Unit = { Icon(Icons.Rounded.Shield, null) },
) {
    Box(
        modifier = Modifier.fillMaxWidth().wrapContentHeight(),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painterResource(R.drawable.phone),
            null,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = PaddingDefaults.Medium),
            contentScale = ContentScale.FillWidth,
        )
        icon()
    }
}

@Composable
fun PhoneWithStateIcon(state: UIState) {
    when (state) {
        ScreenState.Neutral -> PhoneAndIcon {
            Icon(
                Icons.Outlined.Shield,
                null,
                modifier = Modifier.size(40.dp),
                tint = AppTheme.colors.neutral000,
            )
        }
        ScreenState.Success -> PhoneAndIcon {
            Icon(
                Icons.Outlined.VerifiedUser,
                null,
                modifier = Modifier.size(40.dp),
                tint = AppTheme.colors.green600,
            )
        }
        ScreenState.Loading -> PhoneAndIcon {
            CircularProgressIndicator(
                modifier = Modifier.size(40.dp),
                strokeWidth = 4.dp,
            )
        }
        is ScreenState.Error -> PhoneAndIcon {
            Icon(
                Icons.Outlined.WarningAmber,
                null,
                modifier = Modifier.size(40.dp),
                tint = AppTheme.colors.red500,
            )
        }
        DeviceAttestationState.BiometryError -> PhoneAndIcon {
            Icon(
                Icons.Outlined.WarningAmber,
                null,
                modifier = Modifier.size(40.dp),
                tint = AppTheme.colors.red500,
            )
        }
    }
}
