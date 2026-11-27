# ADR 0001: Hanoi Timezone Alignment & Nested LiveScore Incident Parsing

- **Status:** Accepted
- **Date:** 2026-11-27

## Context
LiveSport77 fetches live schedules and match events directly from LiveScore.com public CDN endpoints:
- `GET /v1/api/app/date/{sport}/{yyyyMMdd}/{tzOffset}`
- `GET /v1/api/app/scoreboard/{sport}/{matchId}`

Two production defects occurred:
1. **+7 Hour Timezone Shift:** Passing `tzOffset = 7.00` causes LiveScore CDN to return `Esd` timestamps (`yyyyMMddHHmmss`) already shifted to UTC+7. Previously, `formatStartTime()` parsed `Esd` as `UTC` and converted to `TimeZone.getDefault()`, applying +7 hours twice (e.g., `19:15` Hanoi rendered as `02:15`).
2. **Missing Player Names (`"Player"`):** For assisted goals and substitutions, LiveScore nests player entries inside a child `"Incs"` array while leaving `"Pn"` and `"IT"` absent on the parent object. Reading `inc.optString("Pn", "Player")` only on the parent produced `"Player"` for every assisted goal.

## Decision
1. Pin all date calculations, API requests (`7.00`), `Esd` parsing/formatting, and header clock formatting to `TimeZone.getTimeZone("Asia/Ho_Chi_Minh")` (`LiveScoreApiService.HANOI_TIMEZONE`). Parse `Esd` directly in `HANOI_TIMEZONE` with zero secondary shift.
2. In `LiveScoreApiService.parseIncidents()`, inspect nested `"Incs"` arrays to resolve the primary event (goal, card, substitution) and attach secondary records (`IT == 63` assist, `IT == 41` sub-out) to `detail`. Resolve player names in order: `Pn` → `Fn + Ln` → `Pnm`, and format stoppage time via `MinEx` (`90+3'`).

## Consequences
- Match kickoff times and calendar days always match Hanoi, Vietnam time regardless of device locale/timezone.
- Match timeline displays accurate scorer names, assist attributions, and stoppage-time minutes.
- Guarded by deterministic JVM unit tests in `LiveScoreApiServiceTest`.
