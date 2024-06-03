# React Native library for generating standard requests of Android's Play Integrity API
The title says it all. Check [Android official documentation on Play Integrity standard requests](https://developer.android.com/google/play/integrity/standard) to understand how this API works and what it does.



## Pre-requisites

You will need to use the [bare workflow](https://docs.expo.dev/archive/managed-vs-bare) of Expo, because this library uses native APIs.



## Technical notes

We have used https://github.com/MrToph/react-native-android-library-boilerplate as a template for constructing libraries.

See also https://github.com/jeffDevelops/expo-app-integrity for a library that supports classic requests of Android's Play Integrity and iOS App Attest integrity requests. Please, first read jeffDevelops intro on attestation in Android platforms, to get familiar with the topic.

SIDE NOTE:
Unfortunately, standard requests do not seem to be supported in Kotlin (oddly enough, the [Android official documentation](https://developer.android.com/google/play/integrity/standard) does not give code snippets for Kotlin, and we have checked empirically that the Kotlin compiler does not accept the classes required for the standard requests of the API), so we have written a separate library with Java code. We also found difficulties in adapting jeffDevelops library to merge with Java, so we ended up writing a new library.



## Installation

```
npm install app-integrity-android-standard
```



## Usage

You need to call and await the DoWarmup function before calling the GetToken function for standard requests; otherwise you will get a promise rejection.

#### `DoWarmup()`

Does the warmup to prepare the device to issue standard requests. This requires network connection to the Google servers via internet.

```
import * as AppIntegrityAndroidStandard from 'app-integrity-android-standard';

await AppIntegrityAndroidStandard.DoWarmup(
  GoogleCloudProjectNumber  // in String format
);
```

#### `GetToken()`

Generates an attestation object for a standard request, from the hash of the operation you want to attest (or from the nonce that the user obtained from your app server). You will need to pass this object to your app server to verify its validity (I suggest [this library](https://github.com/sam-maverick/server-side-app-integrity-check)).

```
import * as AppIntegrityAndroidStandard from 'app-integrity-android-standard';

let attestationTokenObject = await AppIntegrityAndroidStandard.GetToken(
  clientHash,  // Hash of the operation or nonce
  GoogleCloudProjectNumber  // in String format
);
```

​    

## How to debug

Change the `logLevel` to something appropriate for you, in your `node_modules/app-integrity-android-standard/android/src/main/java/pt/lasige/appintegrityandroidstandard/Module.java`

Make sure your device is connected to your computer and that it is recognized when issuing `adb devices`. Also make sure that no Android emulators are running.

Run `adb logcat -s "AppIntegrityAndroidStandard"` on the command line.

Execute your bare workflow to build and run the app.

​     

## Acknowledgements

The project that gave rise to these results received the support of a fellowship from ”la Caixa” Foundation (ID 100010434). The fellowship code is LCF/BQ/DI22/11940036. This work was also supported by FCT through the LASIGE Research Unit (UIDB/00408/2020 and UIDP/00408/2020).

​    

## License

This work is licensed under the MIT license. See [LICENSE](LICENSE) for details.
