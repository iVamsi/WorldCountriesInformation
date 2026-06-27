# World Countries Information

Android app for browsing country data from the [REST Countries](https://restcountries.com/) API v3.1. Clean Architecture, multi-module Gradle project, Jetpack Compose UI, and offline-friendly caching.

[![Kotlin](https://img.shields.io/badge/Kotlin-2.4.0-blue.svg)](https://kotlinlang.org)
[![AGP](https://img.shields.io/badge/AGP-9.2.1-green.svg)](https://developer.android.com/studio/releases/gradle-plugin)
[![Min SDK](https://img.shields.io/badge/Min%20SDK-26-orange.svg)](https://developer.android.com/about/versions/oreo)
[![Target SDK](https://img.shields.io/badge/Target%20SDK-37-orange.svg)](https://developer.android.com/about/versions)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)

## Features

- **Countries** — Search (history/suggestions/voice), filters, sort, alphabet index, pull-to-refresh, favorites, compare multiselect.
- **Details** — Flag, facts, map (OpenStreetMap / osmdroid), share, favorites, neighbors, optional on-device AI summary (template fallback).
- **Compare** — Side-by-side table for 2–3 countries; optional template insight when AI summaries enabled in Settings.
- **Quiz** — Guess flag, capital, or region; score persisted in DataStore.
- **Settings** — Cache policy, offline mode, theme/dynamic color, AI summaries toggle, daily notification, map borders, cache stats, OSS licenses.
- **Widget & Wear** — Country of the Day (Glance widget + Wear tile).
- **Adaptive UI** — Two-pane list + detail on expanded width; list pane kept for Settings, Quiz, and Compare.
- **Localization** — Static strings in English plus de/es/fr/hi for key screens.
- **Offline** — Room cache; behavior depends on policy + offline switch.
- **UI** — Material 3 Expressive, Navigation 3.

## Tech stack

- Kotlin **2.4.0**, AGP **9.2.1**, Gradle wrapper
- Compose BOM **2026.06.00**, Material 3, Hilt **2.59.2**
- Retrofit **3.0.0**, OkHttp **5.4.0**, kotlinx-serialization, Coroutines
- Room **2.8.4**, DataStore preferences
- Coil **3.5.0**, Glance, osmdroid **6.1.20**, Generative AI client (on-device summary), Timber, SnapNotify

## Modules

```
app
benchmark
domain
data/countries
core: common, model, designsystem, navigation, network, database, datastore, ai
feature: countries, countrydetails, compare, quiz, settings, widget, wear
tests-shared
```

## Build

Use a recent Android Studio and an SDK that can build API **37**. JDK **17+**.

```bash
git clone https://github.com/iVamsi/WorldCountriesInformation.git
cd WorldCountriesInformation
./gradlew assembleDebug
```

Maps use OpenStreetMap; no API keys are required. Add `sdk.dir` in `local.properties` if Android Studio has not created it.

**Tests:** `./gradlew test` (and `connectedAndroidTest` for instrumentation on a device).

**CI** (see [`.github/workflows/ci.yml`](.github/workflows/ci.yml)): lint, unit tests, `assembleDebug`, `assembleRelease` (R8).

**Baseline profiles:** hand-maintained [`app/src/main/baseline-prof.txt`](app/src/main/baseline-prof.txt). Run `./gradlew :benchmark:connectedDebugAndroidTest` on a device to capture updates.

**Open source licenses:** generated via `com.google.android.gms.oss-licenses-plugin`; linked from Settings → About.

## License

Apache 2.0 — see [LICENSE](LICENSE).

OpenStreetMap data is [ODbL](https://www.openstreetmap.org/copyright); map tiles are credited in-app where required.

## Acknowledgments

REST Countries, OpenStreetMap, osmdroid, Android Jetpack.

**Contact / repo:** [github.com/iVamsi/WorldCountriesInformation](https://github.com/iVamsi/WorldCountriesInformation)
