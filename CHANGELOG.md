# Changelog

All notable changes to **LiveSport77** are documented in this file.
The format follows [Keep a Changelog](https://keepachangelog.com/en/1.1.0/) and [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.1.0] - 2026-11-27

### Added
- Automated versioned APK output naming (`LiveSport77-v<versionName>-<buildType>.apk`) with `versionCode = 110`.
- HTTPS-only `network_security_config.xml` enforcing system CAs and blocking cleartext traffic.
- Project quality contract in `CONSTRAINTS.md` and architecture decision record `docs/adr/0001-hanoi-timezone-and-livescore-parsing.md`.
- Unit test suite in `LiveScoreApiServiceTest` covering Hanoi UTC+7 timestamps, nested incident player names, and security input/payload guards.
- `keystore.properties.example` template for local release signing.

### Changed
- Aligned all match schedules, date navigation, and header clock strictly to Hanoi, Vietnam (`Asia/Ho_Chi_Minh`, UTC+7).
- Upgraded `FullscreenScoreboardModal` for responsive TV & tablet layouts (`usePlatformDefaultWidth = false`, weighted team columns, touch + D-Pad parity).
- Updated `scripts/launch_tv_and_deploy.sh` to automatically pick up versioned APK outputs.

### Fixed
- Fixed +7h double timezone offset when parsing LiveScore `Esd` timestamps.
- Fixed missing scorer/assist names (`"Player"`) by parsing nested LiveScore `Incs` arrays (`Pn` → `Fn + Ln` → `Pnm`) and stoppage time (`MinEx`).

### Security
- Disabled Android backup (`android:allowBackup="false"`) and cleartext traffic (`android:usesCleartextTraffic="false"`).
- Validated `dateStr`, `matchId`, and team logo paths, disabled HTTP redirects, and added bounded response stream limits (5 MB / 1 MB) in `LiveScoreApiService`.
- Stripped `Log.v/d/i` calls in release builds via R8 rules.
- Fixed ADB command injection, CSRF, and `0.0.0.0` binding in `scripts/tv_remote_server.py`.
- Removed hardcoded signing passwords from `app/build.gradle.kts`, gitignored keystore files, and purged signing secrets from git history.

## [1.0.0] - 2026-11-10

### Added
- Initial native Android TV release built with Kotlin and Jetpack Compose for TV.
- Live scores and schedules for Football, Hockey, Basketball, Tennis, and Cricket via LiveScore CDN.
- League-grouped match list, LIVE filter toggle, date switcher, match detail timeline, and D-Pad navigation.
- Coil image loader capped at 10% RAM and R8 full-mode minification.
