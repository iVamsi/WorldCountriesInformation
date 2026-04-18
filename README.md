# World Countries Information

Android app for browsing country data from the [REST Countries](https://restcountries.com/) API v3.1. Clean Architecture, multi-module Gradle project, Jetpack Compose UI, and offline-friendly caching.

[![Kotlin](https://img.shields.io/badge/Kotlin-2.3.20-blue.svg)](https://kotlinlang.org)
[![AGP](https://img.shields.io/badge/AGP-9.1.0-green.svg)](https://developer.android.com/studio/releases/gradle-plugin)
[![Min SDK](https://img.shields.io/badge/Min%20SDK-26-orange.svg)](https://developer.android.com/about/versions/oreo)
[![Target SDK](https://img.shields.io/badge/Target%20SDK-36-orange.svg)](https://developer.android.com/about/versions)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)

## Features

- **Countries** — Search (with history/suggestions), filters, sort, pull-to-refresh, favorites.
- **Details** — Flag, facts, map (OpenStreetMap / osmdroid, no API key), share, favorites, neighbors in region.
- **Settings** — Cache policy, offline toggle, cache stats, clear cache.
- **Widget** — “Country of the Day” (Glance), periodic updates.
- **Offline** — Room cache; behavior depends on policy + offline switch.
- **UI** — Material 3, system light/dark. **Navigation 3** for navigation.

## Tech stack

- Kotlin **2.3.20**, AGP **9.1.0**, Gradle **9.3.1** (wrapper)
- Compose (BOM **2026.03.00**), Material 3, Hilt **2.59.2**
- Retrofit **3.0.0**, OkHttp, kotlinx-serialization, Coroutines
- Room **2.8.4**, DataStore preferences
- Coil, Glance, osmdroid **6.1.20**, Timber, SnapNotify (in-app messaging)

## Modules

```
app
benchmark
domain
data/countries
core: common, model, designsystem, navigation, network, database, datastore
feature: countries, countrydetails, settings, widget
tests-shared
```

## Build

Use a recent Android Studio and an SDK that can build API **36**. JDK **17+**.

```bash
git clone https://github.com/iVamsi/WorldCountriesInformation.git
cd WorldCountriesInformation
./gradlew assembleDebug
```

Maps use OpenStreetMap; no API keys are required. Add `sdk.dir` in `local.properties` if Android Studio has not created it.

**Tests:** `./gradlew test` (and `connectedAndroidTest` if you want instrumentation on a device).

**CI** (see [`.github/workflows/ci.yml`](.github/workflows/ci.yml)): lint, unit tests, `assembleDebug`, `assembleRelease` (R8). Release shrinking uses [`app/proguard-rules.pro`](app/proguard-rules.pro).

**Baseline profiles:** the app ships a hand-maintained [`app/src/main/baseline-prof.txt`](app/src/main/baseline-prof.txt). There is a [`:benchmark`](benchmark/) Macrobenchmark module with a small startup generator—plug in a device or emulator and run `./gradlew :benchmark:connectedDebugAndroidTest` if you want to capture new rules; fold anything useful back into `baseline-prof.txt`. [Macrobenchmark overview](https://developer.android.com/topic/performance/benchmarking/macrobenchmark-overview) has the details.

## License

Apache 2.0 — see [LICENSE](LICENSE).

OpenStreetMap data is [ODbL](https://www.openstreetmap.org/copyright); map tiles are credited in-app where required.

## Acknowledgments

REST Countries, OpenStreetMap, osmdroid, Android Jetpack.

**Contact / repo:** [github.com/iVamsi/WorldCountriesInformation](https://github.com/iVamsi/WorldCountriesInformation)
