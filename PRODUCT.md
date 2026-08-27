# Product

<!-- impeccable:product-schema 1 -->

## Platform

android

## Users

Primary users are students and curious learners. They open the app to look up country facts — name, capital, region, languages, currencies, calling code, population, flag, map, and neighbors — for schoolwork or personal knowledge.

Quiz, compare, Country of the Day (home-screen widget, Wear tile, and optional daily notification), and favorites are secondary loops around that same fact set. Travel booking, news, and social sharing-as-a-network are out of scope.

## Product Purpose

World Countries Information is an Android reference app for country facts. Success is: the user finds a fact quickly, can keep using the app after the first successful cache without an account, and can practice or compare from the same data on phone, tablet, widget, and Wear.

The app is a public open-source project and a Google Play consumer app. Store-ready quality applies even though the listing may still be forthcoming.

## Positioning

The product is account-free country reference that works offline after cache, with no API keys for maps or country data, plus a learning loop (quiz, 2–3 country compare, Country of the Day) on the same dataset across phone, tablet, Glance widget, and Wear. On-device AI summaries are an opt-in extra, off by default, with a template fallback; they are not the reason the app exists.

A neighboring atlas or quiz app that requires an account, a paid API key, or online-only data cannot truthfully copy that combination.

## Operating Context

- Android phone (compact) and tablet (expanded two-pane list + detail; Settings, Quiz, and Compare keep the list pane).
- Home-screen Glance widget and Wear OS tile, both titled Country of the Day.
- First load needs a network; later use follows cache policy and an offline-mode switch (Room cache).
- Maps: OpenStreetMap via osmdroid; optional country border overlays.
- Deep links: `https://worldcountries.vamsi.dev/country/{code}` and `wci://country/{code}`.
- Voice search uses on-device / system speech recognition (`RECORD_AUDIO`).
- Daily country notification is opt-in (`POST_NOTIFICATIONS`).
- Theme: system / light / dark; Material You dynamic color on Android 12+ with a static Explorer fallback.
- Locales: English plus German, Spanish, French, and Hindi for key screens.
- Min SDK 26, target SDK 37. Application id `com.vamsi.worldcountriesinformation`. Version 1.0.0 as of this record.

## Capabilities and Constraints

Confirmed capabilities:

- Countries list: search (history, suggestions, voice), region filters, sort, alphabet jump, pull-to-refresh, favorites, compare multi-select.
- Details: flag, facts listed above, OSM map, share, favorites, neighbors, optional on-device AI summary.
- Compare: side-by-side table for 2–3 countries; optional template insight when AI summaries are on.
- Quiz: guess flag, capital, or region; score and streak persisted in DataStore.
- Settings: cache policy (cache-first, network-first, cache-only), offline mode, theme, dynamic color, AI toggle, daily notification, map borders, cache stats, OSS licenses, data attribution.
- Widget and Wear: Country of the Day.

Constraints:

- Apache 2.0 for the app (README). Country facts from [mledoze/countries](https://github.com/mledoze/countries) under ODbL 1.0; OSM data is ODbL; flag emoji in that dataset are not covered by ODbL. Attribution stays in Settings and THIRD_PARTY_NOTICES.md.
- No user accounts. No first-party backend. Maps and country data need no API keys.
- AI stays off by default, on-device only, with a template fallback when the device cannot generate a summary. Copy states that no data leaves the device.
- Do not drop de/es/fr/hi for key screens.
- TalkBack, large text (sp / font scale), and 48 dp minimum touch targets are product requirements.

Undecided:

- Whether a Play Store listing is live today is not confirmed; consumer-store quality is still required.
- No pricing, analytics, or third-party identity provider is in scope unless later confirmed.

## Brand Commitments

- Product name: World Countries Information (untranslated in `app_name`).
- In-app Settings copy names the static color fallback **Explorer** (also “Refined Explorer” in theme code). Treat that as the existing identity name; do not invent a new product name.
- Voice in UI copy is direct and functional (search hints, cache policy descriptions, quiz prompts). Keep that register.
- Privacy claim already in Settings: on-device AI summaries; no data leaves the device. Do not weaken or contradict it.

## Evidence on Hand

- Country dataset: mledoze/countries (v3.1-compatible JSON), documented in README and Settings attribution.
- Map tiles and borders: OpenStreetMap / osmdroid; credit in-app where required.
- Open-source license screen via `oss-licenses-plugin`.
- THIRD_PARTY_NOTICES.md for third-party notices.
- Repo: https://github.com/iVamsi/WorldCountriesInformation
- No customer testimonials, press, usage metrics, or store reviews exist in the repo. Future work must not fabricate them.

## Product Principles

1. **Look up first.** The primary job is finding a country fact; quiz, compare, and Country of the Day serve that same fact set, they do not replace it.
2. **No account, no key, still useful offline.** After a successful cache, the learner can work without a login, a paid API, or a live network (subject to cache policy).
3. **One dataset, four surfaces.** Phone, tablet, widget, and Wear show the same country truth; do not fork facts per surface.
4. **AI is optional and local.** Summaries stay off until the user opts in, stay on-device, and degrade to a template rather than blocking the screen.
5. **Inclusive by default.** TalkBack, large text, 48 dp targets, and the existing locales are part of the product, not a later pass.

## Accessibility & Inclusion

TalkBack labels, large text (system font scale), and 48 dp minimum touch targets are required. RTL is enabled in the manifest (`supportsRtl`). Key screens ship in English, German, Spanish, French, and Hindi. No further WCAG level was confirmed as a release gate beyond those requirements; theme code comments mention WCAG AA contrast for the Explorer palette as an implementation note, not a separately confirmed legal standard.
