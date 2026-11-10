# Phase 04: TV Match Center UI (Master-Detail Layout)
Status: ✅ Complete
Dependencies: Phase 01, Phase 02, Phase 03

## Objective
Xây dựng giao diện TV tỷ lệ 16:9 dạng Master-Detail tối ưu cho khoảng cách nhìn 2-4m, bao gồm thanh chọn môn thể thao, danh sách trận cuộn mượt bằng `TvLazyColumn`, và màn hình chi tiết trận đấu tức thì.

## Requirements
### Functional
- [x] **Top Header & Sport Tabs (`TvTabRow`):**
  - Hiển thị logo LiveSport77, đồng hồ số thời gian thực.
  - Các Tab chọn môn: ⚽ Bóng đá, 🏀 Bóng rổ, 🎾 Quần vợt.
  - Bộ lọc trạng thái: TẤT CẢ, ĐANG ĐÁ (LIVE), ĐÃ XONG (FT).
- [x] **Cột Trái - Match List Rail (`TvLazyColumn`):**
  - Hiển thị danh sách trận đấu theo giải (Premier League, La Liga, NBA...).
  - Card trận đấu gồm: Tên 2 đội, tỷ số hiện tại, phút thi đấu (nhấp nháy đỏ nếu LIVE), thời gian bắt đầu.
  - Ảo hóa danh sách (View Recycling) để đảm bảo RAM luôn ổn định.
- [x] **Cột Phải - Match Detail Panel:**
  - Header trận: Tên đội, logo (Coil cached), tỷ số kích thước lớn.
  - Timeline diễn biến: Danh sách bàn thắng (cầu thủ + phút), thẻ vàng/đỏ.
  - Thống kê trực tiếp (Stats Bar): Thanh tỷ lệ kiểm soát bóng (Possession), số cú sút, phạt góc.
  - Đội hình ra sân (Lineups) nếu có.

### Non-Functional
- [x] Giao diện tối màu (Dark Theme) bảo vệ mắt và tiết kiệm điện năng cho màn hình TV OLED/LED.
- [x] Kích thước chữ (Typography) tối thiểu 16sp cho thông tin phụ, 24sp-48sp cho tỷ số chính.

## Implementation Steps
1. [x] Xây dựng Composable `TvHeaderBar.kt` và `SportTabRow.kt`.
2. [x] Xây dựng Composable `MatchItemCard.kt` hiển thị một trận đấu trong danh sách.
3. [x] Xây dựng Composable `MatchDetailView.kt` hiển thị chi tiết trận đấu ở cột phải.
4. [x] Ghép nối thành màn hình chính `HomeScreen.kt` theo cấu trúc 35% cột trái - 65% cột phải.

## Files to Create/Modify
- `app/src/main/java/com/livesport77/tv/ui/components/TvHeaderBar.kt`
- `app/src/main/java/com/livesport77/tv/ui/components/MatchItemCard.kt`
- `app/src/main/java/com/livesport77/tv/ui/detail/MatchDetailView.kt`
- `app/src/main/java/com/livesport77/tv/ui/home/HomeScreen.kt`

## Test Criteria
- [ ] Màn hình chia tỉ lệ 16:9 chuẩn xác trên độ phân giải Full HD (1920x1080) và 4K.
- [ ] Khi remote trỏ đến trận nào, màn hình chi tiết bên phải cập nhật tức thì trận đó.

---
Next Phase: [Phase 05: Smart Polling & Memory Guard](file:///Users/hoanng/AIAgent/LiveSport/plans/261110-1025-livesport77-android-tv/phase-05-realtime-integration.md)

