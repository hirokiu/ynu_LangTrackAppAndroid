# KIROKUN for Android

KIROKUN is a Japanese research participant app derived from Lang-Track-App.

## Development

Use JDK 17 and Android SDK 37 (Android 17), with build-tools 36.0.0.
Set `sdk.dir` in local.properties to your Android SDK location.

```sh
./gradlew :app:assembleDebug :app:lintDebug
./gradlew :app:connectedDebugAndroidTest
```

The application ID stays `com.alchembright.dev.langtrackapp` to preserve installed-app/Firebase compatibility. The next development build is 1.1.1 (3). This app is not published on Google Play. The existing debug signing setup is retained for local APK distribution; never commit a signing private key.

The `source-baseline-2026-09-16` tag preserves the source corresponding to the supplied 2025-11-08 APK. See [source provenance](docs/SOURCE_BASELINE.md).

Dependencies are pinned in Gradle. AGP 9.4/Gradle 9.6 use explicit Kotlin 2.2.10 integration temporarily because built-in Kotlin did not activate Parcelize generation in this Data Binding project. Migrate this before AGP 10.

## Provenance

Original project: https://github.com/HumlabLu/HumlabLu
Original authors and research references are retained. KIROKUN branding changes do not rename Firebase or application identifiers.

## Validation and known limits

Android 17/API 37 debug build and lint complete with zero errors. Android 17 emulator tests passed: login screen displays without submitting credentials, and answer data survives Parcelable serialization. Existing warnings (including incomplete translations with English fallback) remain visible; MissingTranslation is a warning, not an error. The supplied APK and rebuilt baseline have identical DEX files, Manifest and compiled resources. The update APK uses the same signing certificate and the unchanged application ID, with a higher versionCode.

Real-account login, study delivery, answer submission, and remote Push receipt require device acceptance testing before distributing an update to research participants.
