# Instrumentation tests

## Compile check (CI)

CI compiles androidTest sources without a device:

```bash
./gradlew :app:compileDebugAndroidTestSources
```

## Test runner

`WorldCountriesTestRunner` swaps in `HiltTestApplication`, which is not the app's WorkManager
`Configuration.Provider`, so the runner initializes WorkManager itself before the application
starts. Without that, `MainActivity` crashes on its first `WorkManager.getInstance` call.

## Local run

Requires emulator or device:

```bash
./gradlew :app:connectedDebugAndroidTest
```

## Smoke flows

Existing tests under `app/src/androidTest/`:

- `CountriesFlowTest` — countries screen via `UiTestTags.COUNTRIES_SCREEN`
- `NavigationFlowTest` — settings, country details, compare navigation
- `ExampleInstrumentedTest` — package context sanity check

Quiz screen uses `UiTestTags.QUIZ_SCREEN` for future navigation coverage.

## CI emulator job

See commented `instrumentation-smoke` job template in `.github/workflows/ci.yml`. Uncomment when `android-emulator-runner` is approved for the repo.
