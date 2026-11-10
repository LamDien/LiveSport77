# Phase 03: Native TV D-Pad Focus & Key Event Engine
Status: ✅ Complete
Dependencies: Phase 01, Phase 02

## Objective
Xây dựng hạt nhân điều hướng TV chuẩn Native Android, xử lý trực tiếp các sự kiện phím Remote D-Pad (Up, Down, Left, Right, Center, Back) với độ trễ 0ms và hiệu ứng Focus viền sáng trực quan.

## Requirements
### Functional
- [x] Quản lý vị trí Focus bằng `FocusRequester` và `Modifier.focusRestorer()` của Compose for TV.
- [x] Khi remote bấm phím Trái / Phải ở thanh trên: Chuyển đổi giữa Bóng đá, Bóng rổ, Tennis.
- [x] Khi remote bấm phím Xuống: Nhảy Focus vào danh sách trận đấu ở Cột Trái.
- [x] Khi duyệt danh sách trận (Lên/Xuống): Cột phải lập tức tự động cập nhật chi tiết của trận đang được focus.
- [x] Khi bấm phím Phải từ danh sách trận: Focus nhảy sang màn hình chi tiết bên phải để xem kỹ thống kê / đội hình.
- [x] Khi bấm phím Trái từ màn hình chi tiết: Focus quay trở lại đúng vị trí trận đấu đang chọn ở cột trái.
- [x] Bấm phím OK/Center: Mở chế độ xem toàn màn hình (Big Scoreboard).

### Non-Functional
- [x] Phản hồi tức thì ở tốc độ 60 FPS, không có độ trễ hay mất focus.
- [x] Viền Focus (Focus Glow / Highlight) tương phản cao, dễ nhận biết từ khoảng cách 3-4 mét.

## Implementation Steps
1. [x] Xây dựng Composable `TvFocusCard` bọc các card trận đấu với animation scale nhẹ (+3%) và viền phát sáng khi focus.
2. [x] Xử lý `FocusRequester` liên kết giữa Cột Trái (Match List) và Cột Phải (Detail Panel).
3. [x] Xử lý phím cứng Back/Escape bằng `BackHandler` để quay lại danh sách hoặc thoát ứng dụng an toàn.

## Files to Create/Modify
- `app/src/main/java/com/livesport77/tv/ui/components/TvFocusCard.kt` - Card có hiệu ứng focus TV
- `app/src/main/java/com/livesport77/tv/ui/navigation/TvFocusManager.kt` - Quản lý điều hướng D-pad

## Test Criteria
- [ ] Bấm các phím D-pad trên bàn phím (hoặc TV remote) di chuyển mượt mà khắp các thành phần giao diện.
- [ ] Trạng thái Focus không bao giờ bị mất khi cuộn danh sách dài.

---
Next Phase: [Phase 04: TV Match Center UI](file:///Users/hoanng/AIAgent/LiveSport/plans/261110-1025-livesport77-android-tv/phase-04-ui-match-center.md)
