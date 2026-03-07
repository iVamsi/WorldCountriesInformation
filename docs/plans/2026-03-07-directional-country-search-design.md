# Directional Country Search Design

**Goal:** Support deterministic natural-language queries such as `southernmost country in South America` using the existing local country cache, without requiring an on-device LLM.

## Context

The current natural-language search path falls back to a rule-based interpreter because the on-device LLM runtime is intentionally a no-op. That rule set understands a small set of population and area phrases, but it does not recognize directional intents or map phrases like `South America` onto the app's current `Americas` region model.

The app already stores `latitude`, `longitude`, `region`, and `subregion` for every cached country. That is enough to answer directional queries deterministically from local data.

## Recommended Approach

Keep interpretation and execution separate.

- Extend `StructuredCountryQuery` with an internal directional ranking signal.
- Teach `RuleBasedCountryQueryInterpreter` to detect `north/south/east/west` phrases and to normalize common regional aliases such as `South America`.
- Update `ExecuteStructuredCountryQueryUseCase` to apply directional ranking after text filtering and region/subregion filtering.
- Preserve the existing keyword and structured search behavior for all other queries.

## Alternatives Considered

### 1. Special-case directional phrases directly in the executor

This would be the smallest code change, but it mixes parsing and execution rules in one place and becomes harder to extend as more natural-language patterns are added.

### 2. Wait for a real on-device LLM

This would eventually improve flexibility, but it does not solve the immediate product problem and still needs deterministic execution based on cached fields.

## Data Flow

1. User enters a search query.
2. `RuleBasedCountryQueryInterpreter` detects directional intent and optional region aliases.
3. The interpreter returns a `StructuredCountryQuery` with:
   - region filters when applicable
   - a result limit of `1` for superlative directional queries
   - a directional ranking hint
4. `ExecuteStructuredCountryQueryUseCase` filters cached countries and sorts by `latitude` or `longitude`.
5. The UI receives a ranked deterministic result from local data.

## Query Scope

Version 1 should support:

- `southernmost country`
- `northernmost country`
- `easternmost country`
- `westernmost country`
- natural variants such as `south most`, `north most`, `east most`, `west most`
- region phrases such as `South America` by mapping them to `Americas`

Version 1 does not need to infer hemisphere-specific semantics beyond those phrases, and it does not need generated explanations.

## Error Handling

- If a directional phrase is not recognized, the app continues to use the existing keyword and rule-based behavior.
- If a region alias is not recognized, the query falls back to literal text filtering instead of failing.
- Countries with valid zero coordinates remain eligible; the sort should operate on the stored coordinate values without special-case exclusion.

## Testing Strategy

- Add interpreter tests for directional phrase parsing and region alias normalization.
- Add executor tests that prove the correct country is selected for directional ranking.
- Re-run the existing targeted NL-search test suites and a debug install.

## Expected Outcome

Queries like `south most country in south america` should return the southernmost cached country in the `Americas` region using local `latitude`, with no model dependency.
