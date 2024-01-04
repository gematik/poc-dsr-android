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

package de.gematik.dsr.common.prescription

import kotlinx.serialization.Serializable

/**
 * PrescriptionListObject.kt
 *
 * This file contains PrescriptionListObject data class.
 * The PrescriptionListObject data class is used while parsing the prescriptions from the fd.
 */

@Serializable
data class PrescriptionListObject(
    val id: String,
    val issuedAt: String,
    val patient: Patient,
    val doctor: Doctor,
    val prescription: Prescription,
)

@Serializable
data class Patient(
    val name: String,
    val address: String,
    val contact: String,
)

@Serializable
data class Doctor(
    val name: String,
    val address: String,
    val contact: String,
)

@Serializable
data class Prescription(
    val medication: String,
    val strength: String,
    val packSize: String,
    val dosageInstruction: String,
)
