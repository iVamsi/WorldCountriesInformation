# World Countries Information

Android app for browsing country data from the [REST Countries](https://restcountries.com/) API v3.1. Clean Architecture, multi-module Gradle project, Jetpack Compose UI, and offline-friendly caching.

[![Kotlin](https://img.shields.io/badge/Kotlin-2.3.20-blue.svg)](https://kotlinlang.org)
[![AGP](https://img.shields.io/badge/AGP-9.1.0-green.svg)](https://developer.android.com/studio/releases/gradle-plugin)
[![Min SDK](https://img.shields.io/badge/Min%20SDK-24-orange.svg)](https://developer.android.com/about/versions/nougat)
[![Target SDK](https://img.shields.io/badge/Target%20SDK-36-orange.svg)](https://developer.android.com/about/versions)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)

## Features

- **Countries list** — Search with suggestions and history, region filters, sort order, pull-to-refresh; favorite countries from the list.
- **Country details** — Flag, facts, OpenStreetMap via osmdroid (no API key), share as text, favorites, and other countries in the same region.
- **Settings** — Cache policy, offline mode, cache statistics, clear cache.
- **Widget** — “Country of the Day” home screen widget (Jetpack Glance), periodic updates.
- **Offline-first** — Room holds cached data; behavior controlled by cache policy and offline toggle.
- **UI** — Material 3, light/dark follows the system theme. Navigation uses **Navigation 3**.

## Tech stack

- Kotlin **2.3.20**, AGP **9.1.0**, Gradle **9.3.1** (wrapper)
- Compose (BOM **2026.03.00**), Material 3, Hilt **2.59.2**
- Retrofit **3.0.0**, OkHttp, kotlinx-serialization, Coroutines
- Room **2.8.4**, DataStore preferences
- Coil, Glance, osmdroid **6.1.20**, Timber, SnapNotify (in-app messaging)

## Modules

```
app
domain
data/countries
core: common, model, designsystem, navigation, network, database, datastore
feature: countries, countrydetails, settings, widget
tests-shared
```

## Getting started

**Requirements:** Android Studio with JDK 17+, Android SDK for compile API 36.

```bash
git clone https://github.com/iVamsi/WorldCountriesInformation.git
cd WorldCountriesInformation
./gradlew assembleDebug
```

Maps use OpenStreetMap; no API keys are required. Add `sdk.dir` in `local.properties` if Android Studio has not created it.

**Tests:** `./gradlew test` · `./gradlew connectedAndroidTest`

## License

Apache 2.0 — see [LICENSE](LICENSE).

OpenStreetMap data is [ODbL](https://www.openstreetmap.org/copyright); map tiles are credited in-app where required.

## Acknowledgments

REST Countries, OpenStreetMap, osmdroid, Android Jetpack.

**Contact / repo:** [github.com/iVamsi/WorldCountriesInformation](https://github.com/iVamsi/WorldCountriesInformation)
