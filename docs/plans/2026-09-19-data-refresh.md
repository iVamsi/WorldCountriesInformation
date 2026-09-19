# Fetch Once, Refresh Only On Change — Implementation Plan

> **For agentic workers:** Use superpowers:executing-plans to implement this plan phase by phase. Steps use checkbox (`- [ ]`) syntax for tracking. Each phase ends with unit tests, a device check, and a commit.

**Goal:** After the first successful load, the app runs from Room. It re-checks the sources on a schedule the user picks in Settings (default weekly), and it rewrites the database only when the merged data actually differs.

**Architecture:** Freshness moves from "age of the newest Room row" to a `lastCheckedAt` timestamp kept in DataStore next to a content fingerprint. `CountriesRepositoryImpl.sync()` fetches both sources, fingerprints the merged list, and writes Room only on a mismatch. A WorkManager periodic job runs `sync()` at the chosen interval; the on-open `CACHE_FIRST` path calls the same function when the cache is past due. Per-row `lastUpdated` keeps meaning "data last changed", which is what the "Updated …" chip shows.

**Tech stack:** existing Room, DataStore (Preferences), WorkManager (already used by `feature/widget`), OkHttp disk cache. No new dependencies.

**Related:** `docs/plans/2026-09-19-ui-redesign.md` (UI), `PRODUCT.md` (offline-after-first-load is a product principle).

## Facts this plan relies on (verified 2026-09-19)

- `raw.githubusercontent.com/mledoze/countries/master/countries.json`: 1.4 MB, `ETag` present, `If-None-Match` → **304**. `Cache-Control: max-age=300`.
- `api.worldbank.org/.../SP.POP.TOTL?…mrv=1&per_page=400`: ~90 KB, `Last-Modified` present, `Cache-Control: public, max-age=78362`. No ETag.
- `NetworkModule` wraps both in one OkHttp client with a 10 MB disk cache, rewrites `Cache-Control` to `max-age=1h`, `max-stale=7d`. After the hour, OkHttp revalidates with `If-None-Match` itself and serves the cached body on 304. The app cannot see whether a 200 came from a 304, which is why change detection is done on content.
- Room today: `refreshCountries()` = `DELETE` + `INSERT` all rows with a fresh `lastUpdated`; freshness = `MIN(lastUpdated)` vs `CachePolicy.DEFAULT_CACHE_VALIDITY_MS` (24 h). 33 call sites use `isCacheFresh`/`getCacheAge`.
- `UserPreferences` (DataStore) already carries `cachePolicy`, `offlineMode`, `lastCacheClearTimestamp`. Settings renders radios/switches via `RadioRow`/`SwitchRow`.
- `WidgetUpdateWorker` is a periodic WorkManager job with a `CONNECTED` constraint; `WorldCountriesApplication` is the WorkManager `Configuration.Provider`.

## Global constraints

- No schema migration: the new state lives in DataStore, not in Room.
- Existing users must not be forced into a refetch on upgrade: missing `lastCheckedAt` falls back to `MIN(lastUpdated)`.
- `CACHE_ONLY` and offline mode never touch the network. Pull-to-refresh (`FORCE_REFRESH`) always checks, but still writes Room only on change.
- A failed check never clears the cache and never blocks the UI; it leaves `lastCheckedAt` alone so the next opportunity retries.
- New strings ship in en, de, es, fr, hi. Detekt, Spotless, lint, unit tests, and connected tests pass at every commit.
- Ponytail: reuse `UserPreferences`, `PreferencesDataSource`, the existing worker pattern, and `FactTile` for the new stats. No new abstractions for one caller.

---

## Design

### Refresh interval (Settings)

`domain/preferences/RefreshInterval.kt`:

```kotlin
enum class RefreshInterval(val millis: Long) {
    DAILY(TimeUnit.DAYS.toMillis(1)),
    WEEKLY(TimeUnit.DAYS.toMillis(7)),
    MONTHLY(TimeUnit.DAYS.toMillis(30)),
}
```

`UserPreferences.refreshInterval: RefreshInterval = WEEKLY`. Settings gets a radio group "Check for updates" (Daily / Weekly / Monthly) under "Data and cache", after the cache-policy radios and before the Offline mode switch. Copy: "Weekly (recommended). Country facts rarely change; a check downloads nothing when they haven't."

### Sync state (DataStore)

Three keys in the existing preferences store, read through `UserPreferences`-style accessors on `PreferencesDataSource`:

| Key | Type | Meaning |
| --- | --- | --- |
| `sync_last_checked_at` | Long | Last time both sources were fetched successfully (changed or not). Drives freshness. |
| `sync_fingerprint` | String | SHA-256 of the canonical merged country list last written to Room. |
| `sync_last_changed_at` | Long | Last time Room was rewritten. Shown in Settings; mirrors `MAX(lastUpdated)`. |

Fingerprint input: the entity list sorted by `threeLetterCode`, each row rendered as a fixed-order string of its fields (name, capital, region, population, languages, currencies, callingCode, latlng, codes). Hash with `MessageDigest.getInstance("SHA-256")`. ~250 rows; sub-millisecond.

### Freshness

`CachePolicy.isCacheFresh(lastUpdated, validityPeriodMs, nowMillis)` stays; callers pass `lastCheckedAt` (falling back to `MIN(lastUpdated)` when 0) and `refreshInterval.millis`. The two ViewModel helpers (`getCacheAge()`/`isCacheFresh()` in countries and details) read `lastCheckedAt` from the repository snapshot instead of computing from `lastUpdated`.

### `sync()` in the repository

```kotlin
/** Fetches both sources; writes Room only when the merged data differs from the last write. */
override suspend fun sync(): Result<SyncOutcome> = try {
    val entities = fetchCountriesWithPopulation().toEntityList(now = clock.millis())
    val fingerprint = entities.fingerprint()
    val changed = fingerprint != syncState.fingerprint()
    if (changed) {
        countryDao.refreshCountries(entities)
        syncState.recordChange(fingerprint, at = clock.millis())
    }
    syncState.recordCheck(at = clock.millis())
    Result.success(if (changed) SyncOutcome.Updated else SyncOutcome.Unchanged)
} catch (e: CancellationException) { throw e }
  catch (e: IOException) { Result.failure(e) }
  catch (e: HttpException) { Result.failure(e) }
  catch (e: SerializationException) { Result.failure(e) }
  catch (e: SQLiteException) { Result.failure(e) }
```

`forceRefresh()` becomes `sync().map { }`. The `getCountries()` network branch calls `sync()` instead of `refreshCountries()` directly. `fetchCountryFromNetwork()` (single country by code) stays as is; it is a cache-miss path.

`toEntityList` must not stamp `lastUpdated` with `now` before fingerprinting, or every check would look like a change. Fingerprint excludes `lastUpdated`.

### Background check

`data/countries/sync/CountriesSyncWorker` (Hilt `@HiltWorker`, same shape as `WidgetUpdateWorker`):

- `doWork()`: if `offlineMode` or `cachePolicy == CACHE_ONLY` → `Result.success()` without network. Else `repository.sync()`; failure → `Result.retry()` (WorkManager backoff), success → `Result.success()`.
- Scheduling: `CountriesSyncScheduler.schedule(interval)` enqueues `PeriodicWorkRequestBuilder<CountriesSyncWorker>(interval.millis, ms)` with `Constraints(NetworkType.CONNECTED, requiresBatteryNotLow = true)`, unique name `countries-sync`, policy `UPDATE` (keeps the schedule position when the interval is unchanged, re-plans when it changes). Called from `WorldCountriesApplication.onCreate()` and from `SettingsViewModel` when the interval preference changes.
- WorkManager's minimum period is 15 minutes; all three intervals are far above it.

### On-open path

Unchanged shape. `CACHE_FIRST`: emit cache, then if `!isCacheFresh(lastCheckedAt, interval)` → `sync()`. If the worker has been running, this rarely fires; it covers devices where background work was deferred.

### Settings statistics

Cache tiles become: Countries · Size · Last checked · Data changed. "Clear cache" also clears the three sync keys so the next load rewrites and re-fingerprints.

### States and copy

- Details/list "Updated 2 hours ago" chip: unchanged meaning (data changed). Subtitle on Countries stays as is.
- Settings: `settings_refresh_interval` "Check for updates", `refresh_interval_daily` "Daily", `refresh_interval_weekly` "Weekly", `refresh_interval_monthly` "Monthly", `refresh_interval_desc` "Country facts rarely change. A check downloads nothing when they haven't.", `settings_last_checked` "Last checked", `settings_last_changed` "Data changed".
- Pull-to-refresh with no change: existing success SnapNotify text stays; no "nothing changed" toast (noise).

---

## Phases

### Phase A — Interval preference and freshness

**Files**
- Create: `domain/src/main/java/.../preferences/RefreshInterval.kt`
- Modify: `domain/.../preferences/UserPreferences.kt` (+ `refreshInterval`), `core/datastore/.../PreferencesDataSource.kt` (key `refresh_interval`, read/write), the preferences port/use case that Settings uses for other toggles (`SettingsContract.Intent.UpdateRefreshInterval`, `SettingsViewModel`)
- Modify: `feature/settings/.../SettingsScreen.kt` (radio group), `feature/settings/src/main/res/values{,-de,-es,-fr,-hi}/strings.xml`
- Modify: `CountriesViewModel.isCacheFresh()/getCacheAge()`, `CountryDetailsViewModel` equivalents, `CountriesRepositoryImpl` `CACHE_FIRST` staleness: pass `userPreferences.refreshInterval.millis` as `validityPeriodMs`
- Test: `SettingsViewModelTest` (update delegates to port), `CachePolicyTest` (weekly interval keeps a 3-day-old cache fresh), datastore round trip if a test exists for other keys

- [ ] Add enum, preference field, DataStore key, Settings intent + radios + strings.
- [ ] Thread `refreshInterval.millis` into every `isCacheFresh` call that decides whether to fetch (grep: 33 sites; only the ones that gate a network call change; display-only `getCacheAge` sites stay).
- [ ] Tests green: `./gradlew :feature:settings:testDebugUnitTest :domain:test :data:countries:testDebugUnitTest`.
- [ ] Device: set Weekly, relaunch, confirm no network call in `adb logcat | grep OkHttp` when cache is < 7 days old. Screenshot goldens for Settings regenerated.
- [ ] Commit: `feat(settings): refresh interval preference drives cache freshness`

### Phase B — Fingerprint and write-on-change

**Files**
- Create: `core/datastore/.../SyncStateDataSource.kt` (three keys; `fingerprint()`, `lastCheckedAt()`, `lastChangedAt()`, `recordCheck(at)`, `recordChange(fingerprint, at)`, `clear()`)
- Create: `data/countries/.../sync/Fingerprint.kt` (`fun List<CountryEntity>.fingerprint(): String`)
- Modify: `domain/.../CountriesRepository.kt` (+ `suspend fun sync(): Result<SyncOutcome>`, `enum class SyncOutcome { Updated, Unchanged }`; `CountryCacheSnapshot` gains `lastCheckedAt`, `lastChangedAt`)
- Modify: `CountriesRepositoryImpl` (`sync()`, `forceRefresh()` delegates, `getCountries()` network branch calls `sync()`, `clearCountryCache()` also `syncState.clear()`)
- Modify: `feature/settings/.../SettingsScreen.kt` (tiles), `SettingsViewModel` (snapshot fields), strings
- Test: `FingerprintTest` (order-independent, ignores `lastUpdated`, changes when population changes), `CountriesRepositoryImplTest` (`sync unchanged → no refreshCountries, lastCheckedAt set`, `sync changed → refreshCountries once, fingerprint stored`, `sync failure → state untouched`), existing tests updated for the new constructor parameter with a fake `SyncStateDataSource` (fakes over mocks; it is a small interface)

- [ ] Implement, keeping `refreshCountries()` as the only Room write path.
- [ ] Upgrade path: when `lastCheckedAt == 0` and Room has rows, treat `MIN(lastUpdated)` as the last check (no forced refetch).
- [ ] Tests green; `adb shell pm clear` then launch twice with logcat open: second launch shows the OkHttp 304 revalidation (or no request inside the hour) and no Room rewrite (`Timber` line "sync: unchanged").
- [ ] Commit: `feat(data): fingerprint merged country data and rewrite Room only on change`

### Phase C — Scheduled background check

**Files**
- Create: `data/countries/.../sync/CountriesSyncWorker.kt` (`@HiltWorker`), `CountriesSyncScheduler.kt`
- Modify: `app/.../WorldCountriesApplication.kt` (schedule on create with the stored interval), `SettingsViewModel` (reschedule on interval change), `data/countries/build.gradle.kts` (`work-runtime-ktx`, `hilt-work` if not already on the classpath; both already used by `feature/widget`, move the catalog aliases)
- Test: `CountriesSyncWorkerTest` with `TestListenableWorkerBuilder` (offline mode → success without calling `sync()`; `sync()` failure → retry), `CountriesSyncSchedulerTest` (interval → period, unique name, `UPDATE` policy) using `WorkManagerTestInitHelper`

- [ ] Implement worker and scheduler; add `docs/INSTRUMENTATION_TESTS.md` note that the test runner initializes WorkManager.
- [ ] Device: `adb shell dumpsys jobscheduler | grep countries-sync` shows the job; `adb shell cmd jobscheduler run -f com.vamsi.worldcountriesinformation <jobId>` triggers it; logcat shows one check and "unchanged".
- [ ] Commit: `feat(data): periodic countries sync at the user's refresh interval`

### Phase D — Verification and docs

- [ ] `./gradlew testDebugUnitTest detekt spotlessCheck lintDebug validateDebugScreenshotTest assembleDebug assembleRelease` and `:app:connectedDebugAndroidTest`.
- [ ] Edge passes: airplane mode on second launch (cache serves, no error); Clear cache then relaunch offline (error state with retry); change interval Daily → Weekly (worker rescheduled, no duplicate jobs); pull-to-refresh with unchanged data (no Room rewrite, "Last checked" updates).
- [ ] README "Offline" bullet: describe the weekly check and the change-only write. `PRODUCT.md` Operating Context: replace "later use follows cache policy" with the interval sentence.
- [ ] Commit: `docs: describe scheduled sync and change-only cache writes`

## Out of scope (say so in the PR)

- Localizing `CachePolicy.getCacheAgeDescription()` (English-only today; pre-existing).
- Per-field diffing or partial Room updates; the table is 250 rows and a full rewrite on real change is cheaper than tracking deltas.
- A visible "checking…" indicator for the background job.
