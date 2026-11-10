# Đặc Tả Kỹ Thuật (Specification): LiveSport77 Android TV Native (Kotlin + Compose)

## 1. Executive Summary
- **Sản phẩm:** Ứng dụng xem thông tin & tỷ số thể thao trực tiếp trên Android TV (10-foot UI).
- **Công nghệ cốt lõi:** **Native Android** viết bằng **Kotlin** và bộ công cụ giao diện chính thức **Jetpack Compose for TV** (`androidx.tv.material3`, `androidx.tv.foundation`).
- **Mục tiêu kỹ thuật tối thượng:**
  - **Mức chiếm RAM:** Tối đa < 60MB (đảm bảo không bị hệ điều hành Android TV kill app vì OOM).
  - **Tải CPU:** < 2% khi idle / polling, 60 FPS mượt mà tuyệt đối khi cuộn danh sách bằng Remote D-pad.
  - **Dung lượng APK:** < 10MB.
  - **Độ trễ phím Remote:** 0ms (xử lý trực tiếp ở tầng OS `KeyEvent`).

---

## 2. Kiến Trúc Ứng Dụng (Clean Architecture + MVVM)

```
┌────────────────────────────────────────────────────────┐
│                   UI LAYER (COMPOSE FOR TV)            │
│  - TvTabRow (Sport Selector)                           │
│  - TvLazyColumn (Match Rail with View Recycling)       │
│  - MatchDetailView (Stats, Lineup, Timeline)           │
│  - FocusRequester & D-Pad Key Handlers                 │
└───────────────────────────▲────────────────────────────┘
                            │ StateFlow<UiState>
┌───────────────────────────┴────────────────────────────┐
│                   VIEWMODEL LAYER                      │
│  - SportsViewModel (Quản lý SelectedSport, ActiveMatch)│
│  - SmartPollingManager (Điều tiết chu kỳ 5s cho Live)  │
└───────────────────────────▲────────────────────────────┘
                            │ Kotlin Coroutines / Flow
┌───────────────────────────┴────────────────────────────┐
│                   DATA & REPOSITORY LAYER              │
│  - SportsRepository (Cache In-Memory & Data Provider)  │
│  - Models (Football, Basketball, Tennis)               │
│  - MemoryGuard (Giới hạn Image Cache < 15MB)           │
└────────────────────────────────────────────────────────┘
```

---

## 3. Data Models (Kotlin @Immutable)

```kotlin
package com.livesport77.tv.model

import androidx.compose.runtime.Immutable

enum class SportType(val displayName: String) {
    FOOTBALL("Bóng đá"),
    BASKETBALL("Bóng rổ"),
    TENNIS("Quần vợt")
}

enum class MatchStatus {
    LIVE, SCHEDULED, FINISHED, POSTPONED
}

@Immutable
data class Team(
    val id: String,
    val name: String,
    val shortName: String,
    val logoUrl: String? = null
)

@Immutable
data class MatchScore(
    val home: Int,
    val away: Int,
    val periodInfo: String // "78'", "Q3 04:15", "Set 2 (5-3)"
)

@Immutable
data class MatchEvent(
    val id: String,
    val minute: String,
    val type: EventType,
    val teamId: String,
    val playerName: String,
    val detail: String? = null
)

enum class EventType {
    GOAL, YELLOW_CARD, RED_CARD, SUBSTITUTION, POINT
}

@Immutable
data class MatchStats(
    val possessionHome: Int = 50,
    val possessionAway: Int = 50,
    val shotsOnTargetHome: Int = 0,
    val shotsOnTargetAway: Int = 0,
    val cornersHome: Int = 0,
    val cornersAway: Int = 0
)

@Immutable
data class Match(
    val id: String,
    val sport: SportType,
    val leagueName: String,
    val status: MatchStatus,
    val statusText: String,
    val homeTeam: Team,
    val awayTeam: Team,
    val score: MatchScore,
    val events: List<MatchEvent> = emptyList(),
    val stats: MatchStats? = null
)
```

---

## 4. Cơ Chế Tối Ưu Hóa Tài Nguyên (Resource Optimization Blueprint)

### 4.1. Tối ưu Bộ nhớ RAM (< 60MB):
- **`TvLazyColumn` Virtualization:** Danh sách các trận đấu chỉ nạp vào bộ nhớ các item xuất hiện trên màn hình TV (8-10 items). Khi cuộn, các view cũ được tái chế lập tức.
- **Data Immutability:** Sử dụng `@Immutable` trên các Data Class giúp Jetpack Compose bỏ qua việc Recomposition không cần thiết.
- **Bộ nhớ đệm hình ảnh (Coil):** Cấu hình `ImageLoader` với `maxSizePercent(0.1)` (không quá 15MB RAM), tự động thu nhỏ logo đội bóng về đúng kích thước hiển thị (64x64dp) trước khi lưu vào RAM.

### 4.2. Tối ưu CPU & Pin/Nhiệt TV:
- **Smart Polling Loop:**
  ```kotlin
  // Chỉ kích hoạt chu kỳ cập nhật cho các trận LIVE
  while (isActive) {
      if (currentMatches.any { it.status == MatchStatus.LIVE }) {
          repository.refreshLiveScores()
          delay(8000) // 8 giây cho trận Live
      } else {
          delay(60000) // 60 giây nếu chỉ có trận sắp đá / đã xong
      }
  }
  ```
- **Lifecycle-Aware:** Sử dụng `repeatOnLifecycle(Lifecycle.State.STARTED)` để dừng 100% background polling ngay khi người dùng chuyển sang ứng dụng TV khác hoặc tắt TV.

---

## 5. Trải Nghiệm Remote TV (D-Pad Navigation)
- `D-pad Up / Down`: Di chuyển giữa danh sách các trận trong giải đấu.
- `D-pad Right`: Nhảy Focus sang khu vực Chi tiết trận đấu (Timeline / Stats).
- `D-pad Left`: Trở lại danh sách trận hoặc menu môn thể thao.
- `D-pad Center (OK)`: Phóng to toàn màn hình xem bảng tỷ số (Big Scoreboard Mode).
- `Back Button`: Thoát chế độ toàn màn hình hoặc quay về menu chọn môn.

---

## 6. Build Checklist & Dependencies
- `Android Gradle Plugin 8.x`
- `Kotlin 2.0+`
- `androidx.tv.foundation:tv-foundation:1.0.0-alpha11+`
- `androidx.tv.material3:tv-material:1.0.0-alpha11+`
- `io.coil-kt:coil-compose:2.6.0`
- `Target SDK 34 (Android 14 for TV)` / `Min SDK 24 (Android 7.0 - bao quát 98% Android TV trên thị trường)`
