# 💡 BRIEF: LiveSport77 - Android TV Multi-Sport LiveScore (Native Performance Edition)

**Ngày cập nhật:** 2026-11-10  
**Định vị:** Ứng dụng theo dõi tỷ số và thông tin đa thể thao thời gian thực trên Android TV (10-foot UI).  
**Triết lý kiến trúc:** **Performance First** — Tối ưu tuyệt đối tài nguyên phần cứng TV (RAM < 60MB, CPU idle < 2%, 60fps mượt mà trên remote D-pad).  
**Học hỏi & Benchmark:** LiveScore.com Mobile App.

---

## 1. VẤN ĐỀ CẦN GIẢI QUYẾT
- LiveScore.com và các ứng dụng thể thao chỉ tối ưu cho Mobile (chạm, màn hình dọc).
- Các ứng dụng chạy trên Android TV nếu dùng WebView/JS nặng nề thường chiếm 200MB - 300MB RAM, dẫn đến việc bị hệ điều hành Android TV tự động ép tắt (OOM Kill) và gây hiện tượng trễ phím remote (input lag).
- Người dùng cần một ứng dụng nhẹ, khởi động tức thì, điều khiển 100% bằng Remote TV từ khoảng cách 2 - 4 mét.

## 2. GIẢI PHÁP ĐỀ XUẤT
- Xây dựng **LiveSport77 TV Native** bằng **Kotlin + Jetpack Compose for TV** (Thư viện chính thức của Google dành riêng cho Android TV).
- Bố cục **Master-Detail (Split Screen 16:9)**:
  - Thanh trên (TvTabRow): Chọn môn thể thao (Bóng đá, Bóng rổ, Tennis).
  - Cột trái (TvLazyColumn): Danh sách trận đấu, chỉ render các item hiển thị trên màn hình để tiết kiệm RAM.
  - Cột phải: Xem trực tiếp diễn biến, đội hình, thống kê trận đấu đang focus.
- **Hệ thống Focus Native:** Xử lý trực tiếp ở tầng Android OS `KeyEvent` / `FocusRequester` — đảm bảo độ trễ 0ms khi bấm remote.

## 3. CHIẾN LƯỢC TỐI ƯU TÀI NGUYÊN (RAM, CPU, PIN/NHIỆT)
1. **Quản lý bộ nhớ (RAM Budget < 60MB):**
   - Áp dụng `TvLazyColumn` ảo hóa danh sách: Dù giải đấu có 100 trận, chỉ nạp vào RAM đúng 8–10 trận đang nằm trong khung nhìn.
   - Giới hạn Image Cache (Coil TV) tối đa 15MB, tự động xóa logo cũ.
2. **Tiết kiệm CPU & Băng thông mạng:**
   - **Smart Polling:** Chỉ kích hoạt vòng lặp cập nhật cho các trận đấu có cờ `isLive == true`.
   - Các trận đấu đã kết thúc (FT) hoặc chưa diễn ra chỉ nạp 1 lần duy nhất vào bộ nhớ đệm.
   - Tự động tạm ngắt (Pause) toàn bộ Coroutine khi TV tắt màn hình hoặc chuyển sang app khác.
3. **Recomposition tinh gọn:**
   - Sử dụng `@Immutable` và `@Stable` trong Compose để tránh vẽ lại toàn bộ màn hình khi chỉ có 1 ô tỷ số thay đổi.

## 4. TÍNH NĂNG SẢN PHẨM (MVP)
1. **Multi-Sport Switcher:** Chuyển nhanh giữa Bóng đá (Football), Bóng rổ (Basketball), Quần vợt (Tennis).
2. **Match List Rail (Cột trái):**
   - Phân loại: Đang đá (LIVE), Hôm nay, Đã kết thúc.
   - Nhóm theo giải đấu (Premier League, La Liga, NBA, ATP...).
   - Hiển thị tỷ số, phút thi đấu và trạng thái thẻ/bàn thắng.
3. **Instant Match Center (Cột phải):**
   - Timeline diễn biến (Bàn thắng, thẻ phạt, thay người).
   - Thống kê thời gian thực (Possession, Shots, Fouls...).
   - Đội hình ra sân (Lineups & Sơ đồ).
4. **Chế độ Big Scoreboard:** Phóng to xem tỷ số từ xa khi bấm OK/Select.

## 5. CÔNG NGHỆ CHỌN LỰA
- **Ngôn ngữ:** Kotlin 2.x
- **UI Toolkit:** Jetpack Compose for TV (`androidx.tv.material3`, `androidx.tv.foundation`)
- **Quản lý State & Bất đồng bộ:** Kotlin Coroutines & StateFlow (MVVM)
- **Image Loading:** Coil for Android (Tối ưu Memory Cache)
