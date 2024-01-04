# Device Security Rating - Android Client

Device Security Rating (DSR) is a Proof of Concept to demonstrate the secure access to services using Zero Trust design principles. In contrast to enterprise-centric Zero Trust architectures, where devices need to be owned and/or managed by a company, the DSR PoC is designed in a way that allows participants from different legal and organisational entities without the need of giving up the ownership of their devices.

This repository contains a proof of concept implementation of an Android client. The whole system architecture and other implementations of the PoC can be found at [https://dsr.gematik.solutions](https://dsr.gematik.solutions).

## About this App

To run this app, you need an Android device with Android >= 12 ( API level >= 33) and StrongBox (eSE, iSE, TRH) support.

The PoC endpoints are not reachable from this demo app as a server certificate chain and a Google Cloud project number are required.

The steps to build/run the app in your own PoC infrastructure are listed below:

```
Steps to run the app:
    - add the correct certificate chain as `gms_cert_chain.pem` to the `common/androidMain/res/raw` folder
    - add the correct cloud project number `const val CLOUD_PROJECT_NUMBER = [the cloud project number]` in the file `CloudProjectNumber.kt` in `common/commonMain/../playIntegrityApi`
```

If you are interested in testing the app. Please contact us via GitHub Issue.

The repository contains a certificate and a private key to mimic an eGK in software.
## Structure

```text
|-- android
|   `-- src
|       |-- components
|       |-- deviceAttestationAndList
|       |-- deviceRegistration
|       |-- theme
|-- common
|   `-- src
|       |-- androidMain
|           |-- deviceAttestation
|           |-- deviceList
|           |-- deviceRegistration
|           |-- fd
|           |-- gms
|           |-- utils
|       |-- commonMain
|           |-- crypto
|           |-- csr
|           |-- deviceAttestation
|           |-- deviceList
|           |-- jwt
|           |-- keystore
|           |-- playIntegrityApi
|           |-- prescription
|           |-- utils
```
This project is a kotlin-multiplatform-project and therefore split into two sections: android and common.

The android folder contains the implementation of screens to visualize how the DSR might work on Android.

The common folder contains the implementation of the DSR. It is split into androidMain and commonMain.
AndroidMain contains android specific logic and general processes of the dsr, 
while commonMain mainly contains helper classes and objects to enable the processes in android and androidMain.

## Contributing / Security Policy

Since this software is not a productive version, please submit an issue or pull request for any bugs or vulnerabilities you find.

In case of a responsible disclosure, please follow instructions on https://www.gematik.de/datensicherheit#c1227.

## License

Copyright 2023 gematik GmbH

The POC DSR Android App is licensed under the European Union Public Licence (EUPL); every use of the E-Rezept App Sourcecode must be in compliance with the EUPL.

You will find more details about the EUPL here: https://joinup.ec.europa.eu/collection/eupl

Unless required by applicable law or agreed to in writing, software distributed under the EUPL is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the EUPL for the specific language governing permissions and limitations under the License.