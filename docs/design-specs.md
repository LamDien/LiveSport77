# Design Specifications: LiveSport77 Android TV (10-Foot UI)

## 🎨 1. Bảng Màu (Color Palette - OLED High Contrast)

Bảng màu được thiết kế để chống chói, tối ưu cho màn hình TV OLED/LED phòng khách với khoảng cách nhìn 2 - 4 mét.

| Tên Màu | Mã HEX | Mục Đích Sử Dụng |
|---|---|---|
| **TV Background (OLED Deep Black)** | `#0B0F17` | Nền tổng thể toàn màn hình, dịu mắt, tiết kiệm điện |
| **Card Surface (Dark Slate)** | `#161F2E` | Bề mặt các thẻ trận đấu ở trạng thái bình thường |
| **Card Surface (Focused)** | `#1E293B` | Bề mặt thẻ khi được Remote D-pad trỏ tới |
| **TV Focus Glow (Gold Highlight)** | `#FFD700` | Viền phát sáng chủ đạo khi có Focus (nhận diện rõ từ 3-4m) |
| **TV Focus Secondary (Neon Green)** | `#00E676` | Viền trạng thái thể thao năng động |
| **Live Pulse Red** | `#EF4444` | Đèn nhấp nháy báo hiệu trận đấu đang diễn ra (LIVE) |
| **Primary Text** | `#F8FAFC` | Tên đội bóng, tỷ số lớn, thông tin quan trọng |
| **Muted Text** | `#94A3B8` | Thời gian, tên giải đấu, thông số phụ |
| **Divider & Subtle Border** | `#1E293B` | Đường phân tách nhẹ giữa các khối |

---

## 📝 2. Hệ Thống Font Chữ (Typography cho TV 10-Foot Experience)

Trên màn hình TV cách xa 2 - 4m, các kích thước chữ nhỏ dưới 14sp sẽ gây mỏi mắt. Toàn bộ chữ đều được tăng tỷ lệ hiển thị.

| Thành Phần | Cỡ Chữ (sp/px) | Độ Đậm (Weight) | Mục Đích |
|---|---|---|---|
| **Big Score (Fullscreen)** | `48sp - 64sp` | Bold (700) | Tỷ số nhìn thấy cực rõ từ xa trong quán cafe / phòng khách |
| **Match Title / Header** | `28sp` | SemiBold (600) | Tên 2 đội bóng ở Màn hình chi tiết |
| **Sport Tab / Rail Title** | `20sp` | SemiBold (600) | Tên môn thể thao, tên giải đấu |
| **Card Score & Team Name** | `18sp` | Medium (500) | Danh sách trận ở cột trái |
| **Status / Minute Badge** | `14sp` | Bold (700) | Huy hiệu "78' LIVE", "FT", "22:30" |
| **Stats & Timeline Detail** | `15sp` | Regular (400) | Cầu thủ ghi bàn, tỷ lệ kiểm soát bóng |

---

## 📐 3. Bố Cục Không Gian (Layout & Spatial Ratio)

- **Tỷ lệ hiển thị:** 16:9 ngang (1920x1080 chuẩn Full HD & 3840x2160 chuẩn 4K).
- **TV Safe Area Inset:** Cách lề viền màn hình vật lý tối thiểu **48dp** (để tránh hiện tượng Overscan của các dòng TV đời cũ làm mất nội dung).
- **Phân chia không gian:**
  - **Zone 1 (Header Bar):** Cao `90dp` cố định ở cạnh trên.
  - **Zone 2 (Match List Rail):** Chiếm `36%` chiều ngang màn hình.
  - **Zone 3 (Match Detail Panel):** Chiếm `64%` chiều ngang màn hình.
- **Card Spacing:** Khoảng cách giữa các thẻ trận đấu là `12dp`.

---

## 🔲 4. Hiệu Ứng Focus & Remote Animation (D-Pad Motion)

- **Scale Up:** Khi nhận Focus, thẻ phóng to `1.04x` (tăng 4%) với chuyển động mượt mà `duration = 150ms`, easing `FastOutSlowInEasing`.
- **Focus Border:** Viền phát sáng vàng kim `#FFD700` dày `2.5dp`, kèm bóng mờ nhẹ (`box-shadow: 0 0 15px rgba(255, 215, 0, 0.45)`).
- **Auto Scroll:** Khi bấm remote di chuyển đến item thứ 6 trong danh sách, `TvLazyColumn` tự động cuộn mượt để item đang focus luôn nằm ở vị trí trung tâm tầm nhìn.

---

## 📊 5. Trạng Thái Giao Diện (UI States)

1. **Trạng thái LIVE (Đang đá):** Huy hiệu đỏ nhấp nháy (`pulse animation` 1.5s), số phút thi đấu màu vàng chanh sáng.
2. **Trạng thái Chờ (Scheduled):** Hiển thị giờ thi đấu (ví dụ: `22:30`), tỷ số hiển thị `- vs -`.
3. **Trạng thái Kết Thúc (FT):** Hiển thị nhãn xám `FT` (Full Time), tỷ số in đậm màu trắng.
4. **Trạng thái Toàn Màn Hình (Big Scoreboard):** Khi bấm `OK/Select`, mở modal phóng to tỷ số chiếm 100% diện tích, ẩn thanh tabs.

