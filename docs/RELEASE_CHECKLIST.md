# Release & Rollback Checklist (LiveSport77)

Established per `shipping-and-launch` and `CONSTRAINTS.md`. Every release MUST pass all gates below before distributing an APK.

## 1. Pre-Launch Quality & Security Gates
- [ ] All unit tests pass: `./gradlew testDebugUnitTest` (0 failures, 0 skipped tests, 0 `@Suppress` additions).
- [ ] Architecture & manifest checks pass: `python3 scripts/verify_project.py` (6/6 suites).
- [ ] Release APK builds cleanly with R8 minification: `./gradlew assembleRelease`.
- [ ] Release APK size is `< 10 MB` (`ls -lh app/build/outputs/apk/release/LiveSport77-v*-release.apk`).
- [ ] Secret scan clean: `keystore.properties` and `*.jks` are untracked/gitignored; no hardcoded passwords in `app/build.gradle.kts`.
- [ ] `versionCode` incremented and `versionName` bumped per SemVer; `CHANGELOG.md` updated.

## 2. Staged Rollout & Device Smoke Test
1. **Stage 1 — Local Tablet / TV Canary:**
   ```bash
   adb install -r app/build/outputs/apk/release/LiveSport77-v<VERSION>-release.apk
   adb shell am start -n com.livesport77.tv/.MainActivity
   ```
2. **Verify Runtime Invariants on Device:**
   - [ ] Installed version matches target: `adb shell dumpsys package com.livesport77.tv | grep -E "versionCode|versionName"`.
   - [ ] Header clock and match kickoff times match Hanoi (UTC+7).
   - [ ] Switching sports (`Football / Hockey / Basketball / Tennis / Cricket`), `LIVE` filter, and `Yesterday / Today / Tomorrow` loads cleanly.
   - [ ] Selecting a match (D-Pad `OK` or touch tap) opens `FullscreenScoreboardModal` with real scorer names and non-overlapping team names.
   - [ ] Steady-state RAM stays `< 60 MB`: `adb shell dumpsys meminfo com.livesport77.tv`.
   - [ ] Zero crash logs: `adb logcat -d -s AndroidRuntime:E`.
3. **Stage 2 — Tag & Publish:**
   - Create annotated git tag `v<VERSION>` and push `main --follow-tags`.
   - Confirm GitHub Actions `Android CI` workflow is green.

## 3. Rollback Plan
If a release exhibits a crash, timezone regression, or layout defect after install:
1. Reinstall the previous known-good signed APK from `release/` (e.g., `release/LiveSport77-v1.0.0-release.apk`) with downgrade flag if needed:
   ```bash
   adb install -r -d release/LiveSport77-v<PREVIOUS>-release.apk
   adb shell am start -n com.livesport77.tv/.MainActivity
   ```
2. Revert the offending commit on `main` (`git revert <sha>`) — never force-push a shared branch for a rollback.
3. Capture logcat (`adb logcat -d`) and add a regression unit test before shipping the fix release.
