# UI Testing Plan for World Countries Information App

## Overview

This document outlines the comprehensive UI testing strategy for the World Countries Information
Android application. The app is built with **Jetpack Compose** and follows the **MVI (
Model-View-Intent)** architecture pattern with **Hilt** for dependency injection.

## Testing Framework Stack

| Component            | Technology                               | Version        |
|----------------------|------------------------------------------|----------------|
| UI Testing           | Compose UI Test                          | BOM 2025.11.01 |
| Dependency Injection | Hilt Android Testing                     | 2.57.2         |
| Test Runner          | AndroidJUnitRunner (HiltTestApplication) | 1.7.0          |
| Assertions           | JUnit 4                                  | 4.13.2         |
| Coroutines Testing   | kotlinx-coroutines-test                  | 1.10.2         |

## Architecture Decisions

### 1. Testing Approach: Compose UI Test with Hilt

We will use **Compose UI Test** (the official testing library for Jetpack Compose) combined with *
*Hilt Testing** for dependency injection in tests.

**Why Compose UI Test over Espresso?**

- Native support for Compose semantics tree
- Better handling of state and recomposition
- No need for Activity/Fragment lifecycle management
- Cleaner, more readable test code
- Better support for animations and async operations

### 2. Test Organization

```
app/
└── src/
    └── androidTest/
        └── java/com/vamsi/worldcountriesinformation/
            ├── WorldCountriesTestRunner.kt      # Existing test runner
            ├── HiltTestActivity.kt              # Test activity for Compose tests
            ├── di/
            │   └── TestRepositoryModule.kt      # Fake repository bindings
            ├── fake/
            │   └── FakeCountriesRepository.kt   # Fake repository implementation
            ├── util/
            │   └── ComposeTestExtensions.kt     # Test utilities
            └── ui/
                ├── CountriesScreenTest.kt       # Countries list tests
                ├── CountryDetailsScreenTest.kt  # Country details tests
                ├── SettingsScreenTest.kt        # Settings screen tests
                └── NavigationTest.kt            # End-to-end navigation tests
```

### 3. Semantic Test Tags Strategy

We will add test tags to key UI elements to enable reliable element identification:

| Screen    | Element             | Test Tag                  |
|-----------|---------------------|---------------------------|
| Countries | Search field        | `countries_search_field`  |
| Countries | Country list        | `countries_list`          |
| Countries | Country card        | `country_card_{code}`     |
| Countries | Settings button     | `settings_button`         |
| Countries | Filter chip         | `region_filter_{region}`  |
| Countries | Sort button         | `sort_button`             |
| Countries | Favorite button     | `favorite_button_{code}`  |
| Details   | Back button         | `details_back_button`     |
| Details   | Country name        | `country_name`            |
| Details   | Share button        | `share_button`            |
| Details   | Favorite button     | `details_favorite_button` |
| Details   | Map card            | `map_card`                |
| Settings  | Back button         | `settings_back_button`    |
| Settings  | Cache policy option | `cache_policy_{policy}`   |
| Settings  | Offline mode switch | `offline_mode_switch`     |
| Settings  | Clear cache button  | `clear_cache_button`      |

## Test Cases

### 1. Countries List Screen Tests

#### Display Tests

- [ ] `test_countriesScreen_displaysCountriesList_whenDataLoaded`
- [ ] `test_countriesScreen_showsLoadingIndicator_whenLoading`
- [ ] `test_countriesScreen_showsErrorState_whenLoadFails`
- [ ] `test_countriesScreen_showsEmptyState_whenNoCountries`

#### Search Tests

- [ ] `test_searchBar_filtersCountries_whenQueryEntered`
- [ ] `test_searchBar_clearsSearch_whenClearButtonClicked`
- [ ] `test_searchBar_showsSearchHistory_whenFocused`
- [ ] `test_searchBar_showsEmptyResults_whenNoMatchFound`

#### Filter Tests

- [ ] `test_regionFilter_filtersCountries_whenChipSelected`
- [ ] `test_regionFilter_showsMultipleRegions_whenMultipleSelected`
- [ ] `test_clearFilters_removesAllFilters_whenClicked`

#### Sort Tests

- [ ] `test_sortSelector_sortsAlphabetically_whenNameAscSelected`
- [ ] `test_sortSelector_sortsByPopulation_whenPopulationSelected`
- [ ] `test_sortSelector_sortsByArea_whenAreaSelected`

#### Interaction Tests

- [ ] `test_countryCard_navigatesToDetails_whenClicked`
- [ ] `test_favoriteButton_togglesFavorite_whenClicked`
- [ ] `test_settingsButton_navigatesToSettings_whenClicked`
- [ ] `test_pullToRefresh_refreshesData_whenPulled`

### 2. Country Details Screen Tests

#### Display Tests

- [ ] `test_countryDetails_displaysAllInformation_whenLoaded`
- [ ] `test_countryDetails_showsFlag_whenLoaded`
- [ ] `test_countryDetails_showsMap_whenCoordinatesAvailable`
- [ ] `test_countryDetails_showsNearbyCountries_whenAvailable`

#### Interaction Tests

- [ ] `test_backButton_navigatesBack_whenClicked`
- [ ] `test_shareButton_opensShareSheet_whenClicked`
- [ ] `test_favoriteButton_togglesFavorite_whenClicked`
- [ ] `test_refreshButton_refreshesData_whenClicked`
- [ ] `test_nearbyCountry_navigatesToDetails_whenClicked`

#### Error Handling Tests

- [ ] `test_errorState_showsRetryButton_whenLoadFails`
- [ ] `test_retryButton_reloadsData_whenClicked`

### 3. Settings Screen Tests

#### Display Tests

- [ ] `test_settingsScreen_displaysAllSections_whenLoaded`
- [ ] `test_cachePolicySection_showsAllOptions`
- [ ] `test_cacheStatistics_displaysStats_whenLoaded`
- [ ] `test_aboutSection_displaysAppInfo`

#### Interaction Tests

- [ ] `test_backButton_navigatesBack_whenClicked`
- [ ] `test_cachePolicyRadio_updatesSelection_whenClicked`
- [ ] `test_offlineModeSwitch_togglesMode_whenClicked`
- [ ] `test_clearCacheButton_showsDialog_whenClicked`
- [ ] `test_clearCacheDialog_clearsCache_whenConfirmed`
- [ ] `test_clearCacheDialog_dismisses_whenCancelled`

### 4. Navigation Tests (End-to-End)

- [ ] `test_navigation_fromCountriesToDetails_andBack`
- [ ] `test_navigation_fromCountriesToSettings_andBack`
- [ ] `test_navigation_fromDetailsToNearbyCountry`
- [ ] `test_deepLink_opensCountryDetails_whenProvided`

## Implementation Steps

### Phase 1: Setup (Dependencies & Infrastructure)

1. **Add Compose UI Test dependencies**
    - Add `ui-test-junit4` to app module
    - Add `ui-test-manifest` to debug build type
    - Ensure Hilt testing dependencies are configured

2. **Create HiltTestActivity**
    - Empty activity annotated with `@AndroidEntryPoint`
    - Register in debug AndroidManifest.xml

3. **Create Fake Repository**
    - `FakeCountriesRepository` with controllable behavior
    - Support for success, error, and loading states
    - Predefined test data

4. **Create Test Module**
    - `@TestInstallIn` module to replace real repository
    - Bind fake repository instead of real implementation

### Phase 2: Add Semantic Test Tags

1. **Countries Screen**
    - Add `testTag` modifier to search field, list, cards, buttons

2. **Country Details Screen**
    - Add `testTag` modifier to key elements

3. **Settings Screen**
    - Add `testTag` modifier to settings controls

### Phase 3: Implement Tests

1. **Countries Screen Tests** (Priority: High)
2. **Settings Screen Tests** (Priority: Medium)
3. **Country Details Screen Tests** (Priority: Medium)
4. **Navigation Tests** (Priority: Low)

## Test Data

### Sample Countries for Testing

```kotlin
val testCountries = listOf(
    Country(
        name = "United States",
        capital = "Washington, D.C.",
        twoLetterCode = "US",
        threeLetterCode = "USA",
        population = 331000000,
        region = "Americas",
        // ...
    ),
    Country(
        name = "Japan",
        capital = "Tokyo",
        twoLetterCode = "JP",
        threeLetterCode = "JPN",
        population = 125000000,
        region = "Asia",
        // ...
    ),
    // Add more test countries...
)
```

## Best Practices

1. **Use semantic matchers** (`hasTestTag`, `hasText`, `hasContentDescription`)
2. **Wait for async operations** (`waitUntil`, `advanceUntilIdle`)
3. **Isolate tests** - each test should be independent
4. **Use fake repositories** - don't hit real network in tests
5. **Test accessibility** - verify content descriptions
6. **Keep tests focused** - one assertion per test when possible

## Running Tests

```bash
# Run all UI tests
./gradlew connectedAndroidTest

# Run specific test class
./gradlew connectedAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.vamsi.worldcountriesinformation.ui.CountriesScreenTest

# Run with coverage
./gradlew createDebugCoverageReport
```

## Success Criteria

- [ ] All test cases pass on emulator/device
- [ ] Build completes successfully with tests
- [ ] Tests run in under 5 minutes total
- [ ] No flaky tests (consistent pass rate)

## References

- [Compose Testing Documentation](https://developer.android.com/develop/ui/compose/testing)
- [Hilt Testing Guide](https://developer.android.com/training/dependency-injection/hilt-testing)
- [Testing Cheat Sheet](https://developer.android.com/develop/ui/compose/testing-cheatsheet)
