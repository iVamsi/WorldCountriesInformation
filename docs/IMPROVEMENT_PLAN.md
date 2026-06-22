# World Countries Information — Improvement Plan

_Last updated: 2026-06-21_

A prioritized backlog of optimizations and new features for the
`WorldCountriesInformation` Android app. Each item includes scope, rationale,
and entry points so it can be picked up as a self-contained piece of work.

The project is already on a solid footing: Kotlin 2.4.0, AGP 9.2.1, multi-module
Clean Architecture (`app`, `domain`, `data/countries`, `core/*`, `feature/*`),
Compose BOM 2026.06.00, MVI contracts, baseline profiles, a Macrobenchmark
module, and CI for lint/tests/release. The recommendations below assume that
baseline and propose incremental wins rather than rework.

---

## 1. Snapshot of current state

| Area | Status |
| --- | --- |
| Architecture | MVI contract per feature, UseCases, Repository in domain | 
| DI | Hilt with KSP |
| Persistence | Room + DataStore, cache policy in domain |
| Networking | Retrofit 3 + OkHttp 5 + kotlinx-serialization |
| UI | Compose Material 3 Expressive, Navigation 3, adaptive two-pane on expanded width |
| Performance | `baseline-prof.txt` shipped, `:benchmark` module wired up |
| Testing | Unit tests for ViewModels, UseCases, mappers, repository, AI generator |
| Image loading | Coil 3.x (single loader) |
| `core/ai` | Included in build; on-device summary with template fallback |
| Features shipped | Compare, Quiz, widget, Wear tile, deep links, daily notification, AI summary |
| i18n | de/es/fr/hi for Settings + key Countries strings |
| a11y | Section headings, quiz answer semantics, 48dp alphabet index |
| OSS licenses | `oss-licenses-plugin` + Settings link |

---

## 2. Tier 0 — Quick wins (low risk, high signal)

### 2.1 Decide on a single image-loading library
**Problem.** `libs.versions.toml` pins both Coil 2.7.0 and Glide 5.0.7. README
says "Coil, Glance, ... Glide". Two image loaders means two disk caches, two
memory caches, two sets of ProGuard rules, larger APK, and inconsistent
placeholder/transition behavior.

**Action.**
1. `grep -R "coil\." -R "Glide\." app core feature` to enumerate call sites.
2. Pick Coil (Compose-first, smaller, kotlin-native). Coil 3.x is the current
   line — bump while consolidating.
3. Migrate Glide call sites to `AsyncImage` / `rememberAsyncImagePainter`.
4. Drop the Glide dependency and its ProGuard rules.

**Expected wins.** ~1–2 MB APK, simpler caching story, fewer transitive deps.

### 2.2 Bump Coil to 3.x
Coil 3 supports Compose-first APIs, KMP-friendly, and ships smaller artifacts.
Combine with §2.1 so the migration happens once.

### 2.3 Remove or implement the orphan `core/ai` module
There is a `core/ai/build/` tree on disk but `settings.gradle.kts` does **not**
`include(":core:ai")` and `core/ai/src/main` is empty. Two options:

- **Delete** the directory if AI features are not on the roadmap.
- **Promote** it into `settings.gradle.kts` and ship one of the AI features in
  §5 (e.g. on-device summarization or smart search ranking).

Either way, do not let it linger as dead weight.

### 2.4 Tighten R8 / log-stripping rules
`CLAUDE.md` mandates stripping `Log.d/v/i` in release. Verify this rule lives
in `app/proguard-rules.pro`; if missing, add the
`-assumenosideeffects class android.util.Log { ... }` block. Also confirm
`isMinifyEnabled = true` and `isShrinkResources = true` for the `release`
buildType.

### 2.5 Adopt `collectAsStateWithLifecycle()` everywhere
Sweep all `collectAsState()` usages in `feature/*` and replace with
`collectAsStateWithLifecycle()` — saves work when the screen is off-screen and
matches the project's stated standards.

---

## 3. Tier 1 — Architecture & code quality

### 3.1 Centralize MVI plumbing
Each feature defines its own `MVIIntent / MVIState / MVIEffect` contract via
markers in `core/common/mvi`. Consider a small base `MviViewModel<S, I, E>` in
`core/common` that encapsulates:
- `_state: MutableStateFlow<S>` exposed as `StateFlow`
- `_effects: Channel<E>` exposed as `receiveAsFlow()`
- `dispatch(intent: I)` entry point
- Default error mapping via `Throwable.toAppError()` (see §3.4)

Saves ~30 lines of boilerplate per feature ViewModel and prevents drift between
contracts.

### 3.2 Extract a `:core:ui` (or expand `:core:designsystem`)
`feature/countries/component/CountryCardShimmer.kt` and
`feature/countrydetails/component/CountryDetailsShimmer.kt` are siblings in
spirit. As features multiply, the shimmer/empty-state/error widgets should
live in a shared design-system module. Look for at least: shimmer primitives,
empty-state composable, error scaffold, snackbar host wrapper.

### 3.3 Unify error model
Today error strings are hand-passed through `errorMessage: String?` in each
state. Introduce the `AppError` sealed interface from the team standard in
`core/common`, expose it in `MVIState`, and have the UI layer translate it to
strings via `stringResource`. This removes hardcoded user-facing copy from the
ViewModel and centralizes retry/auth logic.

### 3.4 Split the `Country` domain model from list/detail concerns
`core/model/.../Country.kt` is referenced from list, search, suggestions,
recently-viewed, favorites, and details. Heavy detail fields (translations,
regional blocs, languages) get hauled into list state even when the list only
needs name/flag/region. Introduce `CountrySummary` (light) and `CountryDetails`
(full) and let the repository decide which to fetch. Reduces memory pressure on
the list and simplifies test fixtures.

### 3.5 Add Detekt + spotless to CI
README and team standards call for Detekt and ktlint. Confirm both run in
`.github/workflows/ci.yml`, fail the build on regressions, and publish SARIF
reports.

---

## 4. Tier 2 — Performance

### 4.1 Pre-warm the Room cache on cold start
The widget's `WidgetUpdateWorker` already touches the cache. On main activity
launch, kick off a `viewModelScope.launch` (or `App.Startup` initializer) that
asks `CountriesRepository` for the snapshot in IO. Combined with baseline
profiles, this can shave perceptible latency off the countries list.

### 4.2 Stable keys + `contentType` on `LazyColumn`
Audit `CountriesScreen` and `CountryDetailsScreen` for:
- `items(list, key = { it.alpha3Code })`
- `contentType` on heterogeneous rows (header vs. card vs. footer)
This is a known Compose perf foot-gun and your lists are large (>250 countries).

### 4.3 Defer heavy `derivedStateOf` to ViewModel
`CountriesContract.State` already exposes `hasActiveFilters`,
`activeFilterCount`, etc. as `get()` properties. They run on every recomposition
that reads them. For the ones used in multiple places per frame, expose them as
precomputed `val`s (since `data class` copies are cheap) instead of computed
properties — or wrap the screen-side calls in `remember(state) { ... }`.

### 4.4 Generate a fresh baseline profile after each major UI change
You ship a hand-maintained `baseline-prof.txt`. Add a CI job (manual trigger or
weekly) that runs `:benchmark:connectedDebugAndroidTest`, captures a new
profile, opens a PR. Prevents the profile from drifting once UI evolves.

### 4.5 Add `StartupBenchmark` and `ScrollBenchmark` macrobenchmarks
The `:benchmark` module appears to host only a startup generator. Add:
- `coldStartup` — measures `TimeToInitialDisplay`
- `countriesScroll` — measures jank on the country list
- `detailsOpen` — measures details screen render
And gate PR merges on no >10% regression in these.

### 4.6 Network: response caching at OkHttp layer
REST Countries v3.1 responses are static for hours. Add an OkHttp `Cache`
(disk-backed) and a network interceptor that injects
`Cache-Control: public, max-age=3600` for the `all` endpoint so the second
launch in an hour is instant even before the Room cache hydrates.

### 4.7 Audit Compose stability reports
Per CLAUDE.md, generate stability reports
(`./gradlew assembleRelease -PcomposeCompilerReports=true`) and resolve any
unstable parameters in hot composables (Country list rows, details header).
Wrap immutable lists from the ViewModel in `ImmutableList` (kotlinx.collections
.immutable) or annotate state holders `@Immutable`.

---

## 5. Tier 3 — New features

Ranked by user impact ÷ implementation cost.

### 5.1 Compare two (or more) countries side-by-side
**Why.** Distinct from "details" and very googled by students/travelers.
**Scope.** New `feature:compare` module. Pick 2–3 countries from a multiselect
on the list screen → navigate to a side-by-side table (population, area,
currency, languages, etc.) using existing repository data only. No new API.

### 5.2 Quiz / "guess the country" mode
**Why.** Increases retention; trivial server cost; good widget tie-in.
**Scope.** Modes: guess-the-flag, guess-the-capital, guess-the-region.
Persist score in DataStore. Reuse country dataset already in Room.

### 5.3 Country-of-the-day push (extending the existing widget)
**Why.** The widget already computes COTD; lift it to a `WorkManager` daily
notification (opt-in in Settings). Discoverability for the widget itself is
low; a notification surfaces the feature to all users.

### 5.4 Smart search ranking
**Why.** Search is the most-used flow.
**Scope.** Replace lexicographic match with: prefix match boost > substring
match > fuzzy (Jaro-Winkler) > recent/favorite boost. Implement as a
`SearchRanker` in `domain/search`. Pure Kotlin, fully testable.

### 5.5 Map clustering and country borders
**Why.** Today `osmdroid` shows a single pin per country. Add a polygon overlay
for country borders and cluster pins when zoomed out. Borders can come from a
small bundled GeoJSON (or ne_50m countries) — keeps offline behavior intact.

### 5.6 Shareable deep links
**Why.** "Look up Iceland" links should open the details screen.
**Scope.** Add `<intent-filter android:autoVerify="true">` for
`https://restcountries.com/v3.1/alpha/{code}` (or your own canonical host) and
route through `Navigation 3` to `CountryDetails`.

### 5.7 Wear OS / Auto Cards (stretch)
The widget code already speaks Glance. A Wear tile and a foldable/large-screen
two-pane layout are largely free with some Compose work.

### 5.8 On-device AI summary (resurrects `core/ai`)
Use Gemini Nano via the AICore APIs (`com.google.ai.client.generativeai`) when
available, with graceful fallback. Generate a one-paragraph "About this
country" summary from the structured fields. Strict opt-in, on-device only,
no PII transmitted.

### 5.9 Localization expansion
The REST Countries v3 payload already carries translations. Surface them based
on `Locale.getDefault()` in `ApiV3ResponseMapper.kt`. Then add Spanish,
French, German, Hindi resources for static strings. Big audience win for
modest cost.

### 5.10 Accessibility audit
- Run TalkBack on the top three screens, file gaps as tickets.
- Verify 4.5:1 contrast on M3 colors.
- 48dp minimum touch targets — search history rows look tight.
- Add `@PreviewScreenSizes`, `@PreviewFontScale` previews on each Screen.

---

## 6. Tier 4 — Build & DX

- **Version-catalog hygiene.** Confirm Renovate or Dependabot covers
  `libs.versions.toml`. Recent commits (`#23`–`#27`) suggest bot-driven PRs are
  flowing — keep it that way.
- **Module Graph plot.** Add `dependencyGraph` plugin to publish a Graphviz of
  `:app → :feature/* → :data/* → :domain → :core/*` to `docs/`. Useful for
  catching layer leaks at review time.
- **Convention plugins.** `build-logic` exists; ensure shared Compose, Hilt,
  test, and lint configuration lives there so feature `build.gradle.kts` files
  stay tiny.
- **JVM toolchain pin.** Pin to JDK 21 in `gradle/foojay` config to remove
  per-machine drift.
- **Build cache + configuration cache.** Verify both are on in
  `gradle.properties` (`org.gradle.caching=true`,
  `org.gradle.configuration-cache=true`).
- **Shared test fixtures.** `tests-shared` exists — make sure each module's
  `testImplementation` references it for fakes (`FakeCountriesRepository` etc.)
  rather than redefining them per module.

---

## 7. Tier 5 — Security & Privacy

- **Network security config.** Confirm `cleartextTrafficPermitted="false"` and
  no debug overrides leak into release.
- **No analytics PII.** The app collects search history; ensure no remote
  analytics SDK ingests it. If one is added later, document the schema.
- **DataStore audit.** Search history and favorites in plain DataStore is fine;
  if any auth-like data appears later, switch to `EncryptedSharedPreferences` or
  `EncryptedFile`-backed proto store.
- **OSS license screen.** Generate via `com.google.android.gms.oss-licenses`
  plugin and link from Settings.

---

## 8. Suggested rollout order (3 sprints)

**Sprint 1 — Hygiene.**
- 2.1 + 2.2 (image-loader consolidation), 2.3 (`core/ai` decision),
  2.5 (lifecycle collect), 4.2 (LazyColumn keys), 4.6 (OkHttp cache),
  6 (build cache + convention cleanup).

**Sprint 2 — Performance & polish.**
- 3.1 (MVI base), 3.4 (Country split), 4.1 (cache pre-warm), 4.5 (benchmarks),
  4.7 (stability sweep), 5.10 (accessibility audit).

**Sprint 3 — New features.**
- 5.1 (compare), 5.4 (smart search), 5.6 (deep links), 5.9 (localization).
  Defer 5.2 / 5.5 / 5.8 to a later sprint depending on appetite.

---

## 9. Open questions for the team

1. Is AI scope (`core/ai`) actually planned, or should it be deleted?
2. Are we comfortable dropping Glide entirely, or does the widget rely on it?
3. Roadmap for Wear OS / Auto / large screens — worth investing now?
4. Is there a target install size we are protecting (e.g. <10 MB)?
5. Which markets are next — that decides 5.9's localization priority.
