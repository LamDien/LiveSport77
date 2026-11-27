# LiveSport77 (Android TV & Tablet)

Native Android application built with **Kotlin** and **Jetpack Compose for TV** (`androidx.tv.material3`, `androidx.tv.foundation`) providing real-time sports scores, schedules, and match timelines directly from LiveScore.com CDN.

## Features
- **5 Sports:** Football, Hockey, Basketball, Tennis, Cricket.
- **Hanoi Timezone (UTC+7):** All match schedules, date navigation (`Yesterday / Today / Tomorrow`), and header clock are aligned to `Asia/Ho_Chi_Minh`.
- **Master-Detail & Fullscreen Scoreboard:** League-grouped match list, `LIVE` filter toggle, match timeline with scorer/assist details, and responsive fullscreen scoreboard modal (`OK` / tap).
- **Resource-Conscious:** Coil memory cache capped at 10% RAM, `@Immutable` state models, D-pad & touch support.

## Architecture
- `app/src/main/java/com/livesport77/tv/data/api/LiveScoreApiService.kt` — Direct LiveScore CDN client, Hanoi UTC+7 parser, nested incident parser.
- `app/src/main/java/com/livesport77/tv/repository/SportsRepository.kt` — In-memory repository without stale disk cache.
- `app/src/main/java/com/livesport77/tv/viewmodel/SportsViewModel.kt` — `StateFlow<SportsUiState>`, league grouping, filtering, and 45s auto-refresh.
- `app/src/main/java/com/livesport77/tv/ui/` — Compose for TV screens (`HomeScreen`, `MatchDetailView`, `FullscreenScoreboardModal`) and components.

## Development & Verification
```bash
# Run unit tests
./gradlew testDebugUnitTest

# Build debug APK
./gradlew assembleDebug

# Build minified release APK
./gradlew assembleRelease

# Deploy versioned release APK to connected Android TV / Tablet via ADB
adb install -r app/build/outputs/apk/release/LiveSport77-v1.2.0-release.apk
adb shell am start -n com.livesport77.tv/.MainActivity
```

## Documentation
- [Changelog](CHANGELOG.md)
- [Technical Spec](docs/specs/livesport77_tv_spec.md)
- [Quality Constraints](CONSTRAINTS.md)
- [ADR 0001: Hanoi Timezone & Incident Parsing](docs/adr/0001-hanoi-timezone-and-livescore-parsing.md)
- [TV Deployment Guide](docs/TV_DEPLOYMENT_GUIDE.md)
