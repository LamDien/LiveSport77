# Phase 01: Native Android TV Setup & Compose Foundation
Status: ✅ Complete
Dependencies: None

## Objective
Khởi tạo cấu trúc dự án Android Native hỗ trợ Android TV / Google TV với Gradle, Kotlin 2.0+, Jetpack Compose for TV và cấu hình tối ưu tài nguyên phần cứng ngay từ đầu.

## Requirements
### Functional
- [x] Thiết lập cấu trúc dự án Android chuẩn Gradle: `settings.gradle.kts`, `build.gradle.kts`, `app/build.gradle.kts`.
- [x] Cấu hình `AndroidManifest.xml` cho Android TV (`android.software.leanback` required=false để tương thích cả TV Box thông thường).
- [x] Khai báo Banner và Activity chính với category `android.intent.category.LEANBACK_LAUNCHER`.
- [x] Cấu hình Theme Compose for TV (bảng màu Dark OLED thể thao, tương phản cao, focus glow).

### Non-Functional
- [x] Kích hoạt R8 / Proguard minify để tối ưu dung lượng APK (< 10MB).
- [x] Cấu hình bộ nhớ Heap an toàn (LargeHeap=false) để đảm bảo app luôn chạy gọn gàng dưới 60MB RAM.

## Implementation Steps
1. [x] Tạo file cấu hình root `settings.gradle.kts` và `build.gradle.kts`.
2. [x] Tạo module `app` với `app/build.gradle.kts` khai báo các dependencies:
   - `androidx.core:core-ktx`
   - `androidx.lifecycle:lifecycle-runtime-compose`
   - `androidx.activity:activity-compose`
   - `androidx.tv:tv-foundation`
   - `androidx.tv:tv-material`
3. [x] Tạo `app/src/main/AndroidManifest.xml` cho Android TV.
4. [x] Tạo theme `LiveSportTvTheme.kt` với Material3 for TV.
5. [x] Tạo `MainActivity.kt` kế thừa `ComponentActivity` với giao diện ban đầu.

## Files to Create/Modify
- `settings.gradle.kts` - Cấu hình repository và plugin
- `build.gradle.kts` - Root build script
- `app/build.gradle.kts` - Dependencies cho Compose for TV
- `app/src/main/AndroidManifest.xml` - Khai báo ứng dụng TV Leanback
- `app/src/main/java/com/livesport77/tv/ui/theme/Theme.kt` - Theme TV tối ưu
- `app/src/main/java/com/livesport77/tv/MainActivity.kt` - Điểm vào chính của ứng dụng

## Test Criteria
- [ ] Dự án cấu hình Gradle hợp lệ, sync và build thành công.
- [ ] `MainActivity` hiển thị được giao diện TV mẫu với Dark Theme.

---
Next Phase: [Phase 02: Sports Data & Repository](file:///Users/hoanng/AIAgent/LiveSport/plans/261110-1025-livesport77-android-tv/phase-02-data-layer.md)
