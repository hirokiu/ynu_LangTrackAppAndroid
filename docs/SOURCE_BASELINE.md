# Android source baseline (2026-09-16)

The 2025-11-08 Dropbox source tree is the canonical source candidate for the user-supplied KIROKUN 1.1.0 (versionCode 2) debug APK.
APK SHA-256: e17bdce7f30fc4c734f9dcecccf316c990144eb7bec5d1f7e65971dc9e216e55.
Application ID: com.alchembright.dev.langtrackapp. No Google Play release exists.
The APK and pre-consolidation working trees are preserved in the private local consolidation backup, not in this public repository.
Source/binary identity is not established by matching version metadata alone.
Initial build with JDK 21 failed (Kotlin JVM target); original configuration expects JDK 17.

Validation: rebuilding with JDK 17 / Gradle 8.5 succeeded. All 16 classes*.dex files, AndroidManifest.xml and resources.arsc are byte-identical to the user-supplied APK. This establishes matching executable code and compiled resource definitions; ZIP/signature metadata was not claimed identical.
