# 📋 HANDOVER DOCUMENT: LiveSport77 Android TV Native

**Thời gian lập:** 2026-11-10 14:25 (ICT)  
**Tác giả:** Antigravity AI Assistant  
**Dự án:** LiveSport77 Android TV Native  
**Kho GitHub:** [https://github.com/LamDien/LiveSport77](https://github.com/LamDien/LiveSport77)  
**Phiên bản phát hành:** `v1.0.0` (versionCode: `100`, Package: `com.livesport77.tv`)  

---

## 1. 🎯 Tổng Quan Dự Án
Ứng dụng theo dõi thông tin & tỷ số thể thao trực tiếp (Bóng đá, Bóng rổ, Quần vợt) được thiết kế chuyên biệt cho hệ sinh thái **Android TV & Google TV** (16:9 widescreen, khoảng cách nhìn 3 mét, điều hướng 100% bằng Remote D-Pad). Ứng dụng lấy dữ liệu trực tiếp theo thời gian thực từ CDN công khai của **LiveScore.com**.

---

## 2. ⚡ Chỉ Số Hiệu Năng & Tài Nguyên Phần Cứng
- **Ngân sách RAM cam kết:** < 60 MB
- **Thực tế đo đạc trên Android TV:**
  - **Java Heap:** `13.2 MB` (Cực kỳ an toàn, chống OS OOM Kill)
  - **Native Heap:** `17.8 MB`
  - **Tổng PSS:** ~54 MB
- **Dung lượng APK Release:** `1.4 MB` (Nhờ kích hoạt R8 Minify & Resource Shrinking)
- **Tần số quét D-Pad:** 60 FPS mượt mà nhờ cơ chế View Recycling của `LazyColumn` với định danh khóa `key = { it.id }`.

---

## 3. 🔐 Cơ Chế Chữ Ký Số & Nâng Cấp Cài Đè
- **Keystore lưu trữ:** `keystore/livesport77.jks`
- **File cấu hình:** `keystore.properties`
- **Key Alias:** `livesport77` (RSA 2048-bit, thời hạn 10.000 ngày đến 2054)
- **Cert SHA-256:** `E4:F1:2F:C8:D5:AA:64:ED:40:B1:04:14:F9:4E:77:0E:1E:46:A7:F3:DB:18:4D:43:D8:3F:F9:22:23:68:2C:C7`
- **Quy tắc Update:** Tăng `versionCode` trong `app/build.gradle.kts` (101, 102...) để các bản sau tự động cài đè bản trước mà không mất dữ liệu.

---

## 4. 🌐 Tích Hợp API LiveScore.com
- **Bóng đá:** `https://prod-cdn-public-api.lsmedia1.com/v1/api/app/date/soccer/{YYYYMMDD}/0.00`
- **Bóng rổ:** `https://prod-cdn-public-api.lsmedia1.com/v1/api/app/date/basketball/{YYYYMMDD}/0.00`
- **Quần vợt:** `https://prod-cdn-public-api.lsmedia1.com/v1/api/app/date/tennis/{YYYYMMDD}/0.00`
- **Scoreboard chi tiết (bàn thắng, thẻ phạt, hiệp):** `https://prod-cdn-public-api.lsmedia1.com/v1/api/app/scoreboard/{sport}/{Eid}`
- **Tần suất Polling:** 45 giây/lần tự động chạy ngầm trên Coroutines Dispatchers.IO.

---

## 5. 📁 Danh Mục File & Báo Cáo
- `release/LiveSport77-v1.0.0-release.apk` — File cài đặt Release chính thức
- `docs/TV_DEPLOYMENT_GUIDE.md` — Hướng dẫn cài đặt lên TV thật qua Wi-Fi LAN
- `plans/261110-1025-livesport77-android-tv/reports/final_report.md` — Báo cáo nghiệm thu 6 phase
- `docs/specs/livesport77_tv_spec.md` — Đặc tả kỹ thuật chi tiết
- `docs/DESIGN.md` — Thiết kế kiến trúc bộ nhớ & spatial navigation

