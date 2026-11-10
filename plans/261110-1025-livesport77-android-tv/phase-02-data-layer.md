# Phase 02: Sports Data Models & Repository Layer
Status: ✅ Complete
Dependencies: Phase 01

## Objective
Xây dựng Data Layer đa môn thể thao (Bóng đá, Bóng rổ, Tennis) bằng Kotlin Data Class tối ưu bộ nhớ (`@Immutable`), kèm Repository và Mock Data phong phú chuẩn LiveScore.com.

## Requirements
### Functional
- [x] Định nghĩa các Model: `SportType`, `Match`, `Team`, `MatchScore`, `MatchEvent`, `MatchStats`.
- [x] Áp dụng Annotation `@Immutable` của Compose trên tất cả các Model để triệt tiêu Recomposition thừa.
- [x] Cung cấp Mock Data chi tiết:
  - Bóng đá: Trận đang đá (Arsenal 2-1 Chelsea 78'), trận sắp diễn ra, trận đã xong.
  - Bóng rổ: Trận NBA (Lakers vs Warriors) với điểm số từng hiệp (Q1-Q4).
  - Tennis: Trận ATP / Grand Slam với tỷ số theo Set và Game.
- [x] Xây dựng `SportsRepository` phát StateFlow danh sách trận theo môn thể thao được chọn.

### Non-Functional
- [x] Dữ liệu In-Memory nhẹ nhàng (< 500KB bộ nhớ), cấu trúc linh hoạt để kết nối REST API / WebSocket sau này.

## Implementation Steps
1. [x] Tạo package `com.livesport77.tv.model` chứa toàn bộ Data Models.
2. [x] Tạo `MockSportsDataSource.kt` cung cấp dữ liệu mô phỏng chính xác.
3. [x] Tạo `SportsRepository.kt` xử lý lấy trận theo môn thể thao và lọc theo trạng thái (LIVE, TODAY, FINISHED).
4. [x] Tạo `SportsViewModel.kt` quản lý StateFlow cho UI.

## Files to Create/Modify
- `app/src/main/java/com/livesport77/tv/model/SportsModels.kt` - Data Classes tối ưu
- `app/src/main/java/com/livesport77/tv/data/MockSportsDataSource.kt` - Dữ liệu giả lập
- `app/src/main/java/com/livesport77/tv/repository/SportsRepository.kt` - Repository interface & implementation
- `app/src/main/java/com/livesport77/tv/viewmodel/SportsViewModel.kt` - ViewModel quản lý UI state

## Test Criteria
- [ ] ViewModel phát ra dữ liệu chính xác khi chuyển đổi giữa Bóng đá, Bóng rổ, Quần vợt.
- [ ] Dữ liệu trận đấu chứa đầy đủ thông tin: Tỷ số, cầu thủ ghi bàn, thẻ phạt, thống kê.

---
Next Phase: [Phase 03: Native TV D-Pad Focus](file:///Users/hoanng/AIAgent/LiveSport/plans/261110-1025-livesport77-android-tv/phase-03-tv-navigation-engine.md)
