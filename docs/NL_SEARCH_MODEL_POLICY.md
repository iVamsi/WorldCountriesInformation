# On-Device Natural Language Search Model Policy

This app only permits fully free, permissively licensed on-device AI components.

## Allowed

- Runtime license must be `Apache-2.0` or `MIT`.
- Model weight license must be `Apache-2.0` or `MIT`.
- Redistribution must be allowed through the APK or an app-managed local download flow.
- Search results must be executed deterministically against local country data.

## Preferred Candidates

- `MediaPipe LLM Inference` runtime with a permissively licensed small model.
- `TinyLlama`-class models for natural-language intent parsing.

## Secondary Candidate

- `Phi-2` can be evaluated if device performance and package size are still acceptable.

## Excluded

- Any cloud-hosted API dependency.
- Any paid or gated inference service.
- Any model family with custom usage terms instead of `Apache-2.0` or `MIT`.
- `Gemma` model weights.
- `Gemini Nano` / `AICore` distribution paths.

## Product Rule

The model may help interpret a user query, but it must not be trusted to produce the final answer directly. Final country results must always come from structured execution over the local database.

## Background Work Decision

Version 1 does not add a dedicated `WorkManager` job for natural-language search.

- The enriched `/all` sync now provides the local fields needed for structured execution.
- Search works from the existing cache once country data is present.
- Background work should only be reconsidered later for model pre-download or periodic cache refresh, not for one-request-per-country enrichment.
