# Project Constraints & Quality Bar (LiveSport77)

Established per **Addy Osmani's `agent-skills`** (`using-agent-skills`, `constraint-driven-development`, `incremental-implementation`, `test-driven-development`, `security-and-hardening`, `git-workflow-and-versioning`, and `definition-of-done`). **Standing Rule:** All changes on this repository MUST follow Addy Osmani's `agent-skills` workflow and quality thresholds without silently lowering them.

## 1. Runtime & Resource Budget
- **Memory (RAM):** < 60 MB steady-state on Android TV / tablet (Coil image memory cache capped at 10% / ~15 MB in `LiveSportApp`).
- **APK Size:** < 10 MB for release minified APK (`isMinifyEnabled = true`, `isShrinkResources = true`).
- **UI Smoothness:** 60 FPS scrolling via `LazyColumn` virtualization and `@Immutable` models.

## 2. Data & Timezone Invariants
- **Timezone:** All match dates, kickoff times, and header clock MUST strictly use Hanoi, Vietnam (`Asia/Ho_Chi_Minh`, UTC+7).
- **No Double Offset:** LiveScore CDN `/date/{sport}/{yyyyMMdd}/7.00` returns `Esd` already shifted to UTC+7; parsers MUST parse `Esd` in `Asia/Ho_Chi_Minh`, never UTC.
- **Incident Player Names:** `parseIncidents` MUST extract real player names from both flat incident objects and nested `Incs` arrays (`Pn` → `Fn + Ln` → `Pnm`) and never regress to generic `"Player"`.
- **No Local Match Cache:** Match data is fetched fresh from LiveScore CDN without stale disk caching.

## 3. UI & Layout Invariants
- **Modal Dialogs:** Fullscreen modals (`FullscreenScoreboardModal`) MUST set `DialogProperties(usePlatformDefaultWidth = false)` and balanced `Modifier.weight(1f)` columns with `TextOverflow.Ellipsis` so long team names never break layout on TV or tablets.
- **Touch + D-Pad Parity:** Selecting/tapping a match card MUST update `activeMatch` before opening fullscreen so both TV remotes and touchscreens show the exact selected match.

## 4. Testing & Verification Gate
- **TDD Prove-It Pattern:** Every bug fix or parser/ViewModel behavior change MUST include a unit test in `app/src/test/` that fails before the fix and passes after.
- **Zero Suppressions:** Do not add `@Suppress` or disable/skip tests to achieve a green build.
- **Verification Command:** `./gradlew testDebugUnitTest assembleDebug` MUST pass with 0 failures before claiming completion.
