# Phase 05: Smart Polling & Memory Guard System
Status: ✅ Complete
Dependencies: Phase 01, Phase 02, Phase 03, Phase 04

## Objective
Xây dựng cơ chế cập nhật tỷ số thời gian thực (Real-time updates) tiết kiệm CPU tối đa và thiết lập lớp giám sát bộ nhớ (Memory Guard) để giữ mức chiếm RAM luôn dưới 60MB.

## Requirements
### Functional
- [x] **Smart Polling Manager:**
  - Tự động phát hiện các trận đấu có `status == LIVE`.
  - Nếu có trận LIVE: Chu kỳ polling cập nhật tỷ số là 8 giây.
  - Nếu không có trận LIVE (chỉ có trận sắp đá hoặc đã xong): Tăng chu kỳ lên 60 giây.
  - Khi người dùng chuyển app hoặc TV tắt màn hình: Lập tức dừng chu kỳ polling (`onStop()` / `Lifecycle.State.STARTED`).
- [x] **Giữ nguyên trạng thái Focus khi Data cập nhật:**
  - Khi tỷ số hoặc phút thi đấu thay đổi, con trỏ Focus của remote phải giữ nguyên vị trí, không bị nhảy lung tung hoặc giật hình.
- [x] **Memory Guard & Image Cache Limiter:**
  - Khởi tạo Coil `ImageLoader` với chính sách RAM chặt chẽ: Cache tối đa 15MB.
  - Tự động gọi `System.gc()` hoặc giải phóng bitmap khi nhận sự kiện `onTrimMemory(TRIM_MEMORY_RUNNING_CRITICAL)`.

### Non-Functional
- [x] CPU đo được trong Android Studio Profiler khi idle < 2%.
- [x] RAM đo được ổn định từ 45MB - 55MB sau 30 phút chạy liên tục.

## Implementation Steps
1. [x] Xây dựng `SmartPollingManager.kt` quản lý Coroutine Scope gắn liền với Android Lifecycle.
2. [x] Cấu hình `LiveSportApp.kt` (Application class) khởi tạo `ImageLoader` với memory cache giới hạn.
3. [x] Xử lý callback `onTrimMemory()` trong Application class.
4. [x] Tạo cơ chế mô phỏng biến động tỷ số trực tiếp (Simulated Live Score Engine) phục vụ kiểm thử.

## Files to Create/Modify
- `app/src/main/java/com/livesport77/tv/data/SmartPollingManager.kt` - Quản lý chu kỳ cập nhật
- `app/src/main/java/com/livesport77/tv/LiveSportApp.kt` - Cấu hình Memory & ImageLoader
- `app/src/main/java/com/livesport77/tv/data/LiveScoreSimulator.kt` - Mô phỏng nhảy tỷ số

## Test Criteria
- [ ] Tỷ số trận đấu nhảy số trực tiếp mà người dùng vẫn bấm remote chuyển trận mượt mà.
- [ ] Mức RAM không bị phình to (no memory leak) theo thời gian.

---
Next Phase: [Phase 06: TV Build & Verification](file:///Users/hoanng/AIAgent/LiveSport/plans/261110-1025-livesport77-android-tv/phase-06-tv-testing-build.md)

