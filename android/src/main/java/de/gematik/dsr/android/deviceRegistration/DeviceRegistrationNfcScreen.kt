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
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import de.gematik.dsr.android.DebugButton
import de.gematik.dsr.android.R
import de.gematik.dsr.android.components.AnimatedElevationScaffold
import de.gematik.dsr.android.components.DeviceRegistrationBottomBar
import de.gematik.dsr.android.components.NavigationBarMode
import de.gematik.dsr.android.components.annotatedStringResource
import kotlin.math.PI
import kotlin.math.cos

/**
 * DeviceRegistrationNfcScreen.kt
 *
 * This file contains the DeviceRegistrationNfcScreen,
 * which displays best position of the phone to read data from the healthcard via NFC to the user.
 * Since this app uses a virtual health card, no data is transferred here, so you can hit the next button.
 */

private const val LowerPos = 1 / 3f
private const val HigherPos = 2 / 3f
private const val x1 = 0.5
private const val x2 = 0.5
private const val y1 = 0.3
private const val y2 = 0.3
private val PosRange = LowerPos..HigherPos

@Suppress("LongMethod")
@Composable
fun DeviceRegistrationNfcScreen(
    onBack: () -> Unit,
    onNext: () -> Unit,
    onOpenDebug: () -> Unit,
) {
    val useDarkIcons = MaterialTheme.colors.isLight
    AppTheme(
        darkTheme = true,
    ) {
        val systemUiController = rememberSystemUiController()
        DisposableEffect(Unit) {
            systemUiController.setSystemBarsColor(Color.Transparent, darkIcons = false)
            onDispose { systemUiController.setSystemBarsColor(Color.Transparent, darkIcons = useDarkIcons) }
        }
        val listState = rememberLazyListState()
        AnimatedElevationScaffold(
            modifier = Modifier,
            topBarTitle = "",
            topBarColor = MaterialTheme.colors.background,
            navigationMode = NavigationBarMode.Back,
            listState = listState,
            actions = {
                DebugButton(onOpenDebug)
            },
            bottomBar = {
                DeviceRegistrationBottomBar(
                    onNext = onNext,
                    nextEnabled = true,
                    aligment = Alignment.CenterHorizontally,
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

            var phoneImgSize by remember { mutableStateOf(IntSize.Zero) }
            var titleHeight by remember { mutableStateOf(0) }
            var subTitleHeight by remember { mutableStateOf(0) }
            var descriptionHeight by remember { mutableStateOf(0) }
            val nfcXPos by remember { mutableStateOf((x1 + x2) / 2) }
            val nfcYPos by remember { mutableStateOf((y1 + y2) / 2) }

            LazyColumn(
                state = listState,
                modifier = Modifier
                    .padding(contentPadding)
                    .fillMaxSize()
                    .navigationBarsPadding()
                    .semantics(mergeDescendants = true) {},
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                item {
                    Text(
                        stringResource(R.string.nfc_instruction_headline),
                        style = AppTheme.typography.h6,
                        modifier = Modifier
                            .padding(
                                all = PaddingDefaults.Medium,
                            )
                            .onGloballyPositioned { titleHeight = it.size.height }
                            .fillMaxWidth(),
                        textAlign = TextAlign.Center,
                    )

                    AnimatedVisibility(
                        modifier = Modifier.fillMaxWidth(),
                        visible = with(LocalDensity.current) {
                            (1.5f * phoneImgSize.height + titleHeight + subTitleHeight + descriptionHeight).toDp()
                        } <= LocalConfiguration.current.screenHeightDp.dp,
                    ) {
                        Text(
                            stringResource(R.string.nfc_instruction_time_hint),
                            style = AppTheme.typography.body2l,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .padding(
                                    bottom = PaddingDefaults.Large,
                                    start = PaddingDefaults.Medium,
                                    end = PaddingDefaults.Medium,
                                )
                                .onGloballyPositioned { subTitleHeight = it.size.height }
                                .fillMaxWidth(),
                        )
                    }
                }
                item {
                    CardOnPhone(
                        nfcXPos = nfcXPos,
                        nfcYPos = nfcYPos,
                        phoneImgSize = phoneImgSize,
                        onPhoneSizeChanged = { phoneImgSize = it },
                    )
                }
                item {
                    val cardPosDescr = when {
                        nfcXPos < LowerPos && nfcYPos < LowerPos -> stringResource(
                            R.string.nfc_instruction_chip_location_top_left,
                        )

                        nfcXPos < LowerPos && nfcYPos in PosRange -> stringResource(
                            R.string.nfc_instruction_chip_location_middle_left,
                        )

                        nfcXPos < LowerPos && nfcYPos > HigherPos -> stringResource(
                            R.string.nfc_instruction_chip_location_bot_left,
                        )

                        nfcXPos in PosRange && nfcYPos < LowerPos -> stringResource(
                            R.string.nfc_instruction_chip_location_top_central,
                        )

                        nfcXPos in PosRange && nfcYPos in PosRange -> stringResource(
                            R.string.nfc_instruction_chip_location_middle,
                        )

                        nfcXPos in PosRange && nfcYPos > HigherPos -> stringResource(
                            R.string.nfc_instruction_chip_location_bot_central,
                        )

                        nfcXPos > HigherPos && nfcYPos < LowerPos -> stringResource(
                            R.string.nfc_instruction_chip_location_top_right,
                        )

                        nfcXPos > HigherPos && nfcYPos in PosRange -> stringResource(
                            R.string.nfc_instruction_chip_location_middle_right,
                        )

                        nfcXPos > HigherPos && nfcYPos > HigherPos -> stringResource(
                            R.string.nfc_instruction_chip_location_bot_right,
                        )

                        else -> ""
                    }
                    AnimatedVisibility(
                        modifier = Modifier.fillMaxWidth(),
                        visible = with(LocalDensity.current) {
                            (1.5f * phoneImgSize.height + titleHeight + subTitleHeight + descriptionHeight).toDp()
                        } <= LocalConfiguration.current.screenHeightDp.dp,
                    ) {
                        Text(
                            annotatedStringResource(
                                R.string.nfc_instruction_chip_location,
                                cardPosDescr,
                            ),
                            style = AppTheme.typography.subtitle2l,
                            modifier = Modifier
                                .padding(
                                    vertical = PaddingDefaults.Large,
                                    horizontal = PaddingDefaults.Medium,
                                )
                                .onGloballyPositioned { descriptionHeight = it.size.height }
                                .fillMaxWidth(),
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CardOnPhone(
    nfcXPos: Double,
    nfcYPos: Double,
    phoneImgSize: IntSize,
    onPhoneSizeChanged: (IntSize) -> Unit,
) {
    BoxWithConstraints(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        /*
        The first part of the offset calculation (in front of +) is the shift in the x-axis
        (x-shift: 1/3 * x * max width & 1/3 * x * max height).
        The second part of the offset calculation (after +) is the shift in the y-axis
        (y-shift: -2/3 * y * max width & 2/3 * y * max height).
        Cos-function is used to swap the offset direction (cos(0 pi) = 1, cos(1 pi) = -1),
        since the images are centered all functions were halved and inverted.
         */
        CardAndAnimation(
            modifier = Modifier.offset {
                IntOffset(
                    x = (
                        (phoneImgSize.width * -cos(nfcXPos * PI) / 6) +
                            (phoneImgSize.width * cos(nfcYPos * PI) / 3)
                        ).toInt(),
                    y = (
                        (phoneImgSize.height * -cos(nfcXPos * PI).toFloat() / 6) +
                            (phoneImgSize.height * -cos(nfcYPos * PI).toFloat() / 3)
                        ).toInt(),
                )
            },
        )
        PhoneWithScaling(modifier = Modifier.width(maxWidth * 2 / 3), onPhoneSizeChanged = onPhoneSizeChanged)
    }
}

@Composable
private fun CardAndAnimation(modifier: Modifier) {
    val animationComposition = rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.animation_pulse_lottie))
    val healthCardLottie = rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.healthcard_lottie))
    val progress by animateLottieCompositionAsState(
        animationComposition.value,
        iterations = LottieConstants.IterateForever,
    )
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        LottieAnimation(
            animationComposition.value,
            progress,
        )
        LottieAnimation(
            healthCardLottie.value,
        )
    }
}

@Composable
private fun PhoneWithScaling(modifier: Modifier, onPhoneSizeChanged: (IntSize) -> Unit) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        val phoneLottie = rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.device_lottie))
        LottieAnimation(
            phoneLottie.value,
            contentScale = ContentScale.FillWidth,
            modifier = Modifier.onGloballyPositioned {
                onPhoneSizeChanged(it.size)
            },
        )
    }
}
