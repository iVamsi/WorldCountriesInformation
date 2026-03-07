# Directional Country Search Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Add deterministic support for directional natural-language country queries such as `southernmost country in South America` using the existing local cache.

**Architecture:** Extend the structured query model with a directional ranking hint, let the rule-based interpreter detect directional phrases and normalize region aliases, and let the structured executor rank cached countries by latitude or longitude after applying filters.

**Tech Stack:** Kotlin, coroutines `Flow`, Room-backed cached country data, existing domain/data search use cases, JUnit 4.

---

### Task 1: Interpreter Red-Green for Directional Queries

**Files:**
- Modify: `data/countries/src/test/kotlin/com/vamsi/worldcountriesinformation/data/countries/search/RuleBasedCountryQueryInterpreterTest.kt`
- Modify: `data/countries/src/main/kotlin/com/vamsi/worldcountriesinformation/data/countries/search/RuleBasedCountryQueryInterpreter.kt`
- Modify: `domain/src/main/java/com/vamsi/worldcountriesinformation/domain/search/StructuredCountryQuery.kt`

**Step 1: Write the failing test**

Add tests that assert:
- `south most country in south america` maps to a directional ranking hint
- `South America` becomes the existing `Americas` region filter
- directional superlative queries return `limit = 1`

**Step 2: Run test to verify it fails**

Run: `./gradlew :data:countries:testDebugUnitTest --tests "*RuleBasedCountryQueryInterpreterTest*"`

Expected: FAIL because the current structured query model and interpreter do not support directional ranking or region alias normalization.

**Step 3: Write minimal implementation**

Add the minimal structured query field and interpreter rules needed to satisfy the new tests.

**Step 4: Run test to verify it passes**

Run: `./gradlew :data:countries:testDebugUnitTest --tests "*RuleBasedCountryQueryInterpreterTest*"`

Expected: PASS.

### Task 2: Executor Red-Green for Directional Ranking

**Files:**
- Modify: `domain/src/test/java/com/vamsi/worldcountriesinformation/domain/search/ExecuteStructuredCountryQueryUseCaseTest.kt`
- Modify: `domain/src/main/java/com/vamsi/worldcountriesinformation/domain/search/ExecuteStructuredCountryQueryUseCase.kt`

**Step 1: Write the failing test**

Add tests that assert directional ranking works on cached coordinates, for example:
- the southernmost country in `Americas` is selected by minimum latitude
- the easternmost country is selected by maximum longitude

**Step 2: Run test to verify it fails**

Run: `./gradlew :domain:test --tests "*ExecuteStructuredCountryQueryUseCaseTest*"`

Expected: FAIL because the executor currently only applies text filtering plus `SearchFilters.sortOrder`.

**Step 3: Write minimal implementation**

Apply directional ordering after text filtering and filter application, then respect the existing `limit`.

**Step 4: Run test to verify it passes**

Run: `./gradlew :domain:test --tests "*ExecuteStructuredCountryQueryUseCaseTest*"`

Expected: PASS.

### Task 3: Search Flow Integration Verification

**Files:**
- Modify: `feature/countries/src/main/kotlin/com/vamsi/worldcountriesinformation/feature/countries/CountriesViewModel.kt` if needed
- Modify: `feature/countries/src/test/kotlin/com/vamsi/worldcountriesinformation/feature/countries/CountriesViewModelTest.kt` if needed

**Step 1: Verify whether search mode detection needs a directional-query update**

Check whether the current `determineSearchMode()` logic should consider the new directional ranking signal.

**Step 2: Add or update a test if behavior changes**

Only add coverage if the directional ranking signal changes how the UI classifies NL search.

**Step 3: Implement the smallest required fix**

Keep the change minimal and avoid unrelated refactors.

**Step 4: Run the relevant feature tests**

Run: `./gradlew :feature:countries:testDebugUnitTest --tests "*CountriesViewModelTest*"`

Expected: PASS.

### Task 4: Full Verification and Device Check

**Files:**
- No new code files required

**Step 1: Run targeted verification**

Run: `./gradlew :data:countries:testDebugUnitTest :domain:test :feature:countries:testDebugUnitTest :app:installDebug`

Expected: PASS / BUILD SUCCESSFUL.

**Step 2: Launch on the connected device**

Run: `adb shell am start -n com.vamsi.worldcountriesinformation/.ui.MainActivity`

Expected: the app opens on the connected device.

### Task 5: Commit and Push

**Files:**
- Stage only the relevant feature files

**Step 1: Review git state**

Run:
- `git status --short`
- `git diff --staged`
- `git log --oneline -5`

**Step 2: Commit**

Create a focused feature commit describing deterministic directional NL search support.

**Step 3: Push**

Run: `git push -u origin feature/nl-country-search`

Expected: branch is available on `origin`.
