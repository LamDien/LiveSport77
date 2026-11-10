# Hướng Dẫn Cài Đặt & Triển Khai LiveSport77 Lên Android TV

Tài liệu này hướng dẫn chi tiết cách build và cài đặt ứng dụng **LiveSport77 TV** lên thiết bị thật (Smart TV Sony, Xiaomi, TCL, Casper...) hoặc chạy thử trên Android TV Emulator.

---

## 1. Yêu Cầu Môi Trường Phát Triển
- **JDK:** OpenJDK 17 trở lên.
- **Android SDK:** API 34 (Android 14) với `Android TV ARM/x86 Image`.
- **Thiết bị TV:** Mọi Smart TV / Android TV Box chạy Android 7.0 (API 24) trở lên.

---

## 2. Cách Build File APK

Mở Terminal tại thư mục gốc của dự án:

### Build bản Debug (Dùng thử & Test nhanh):
```bash
./gradlew assembleDebug
```
File APK sẽ được tạo tại:
`app/build/outputs/apk/debug/app-debug.apk`

### Build bản Release tối ưu dung lượng (Bật R8 Minify & Shrink):
```bash
./gradlew assembleRelease
```
File APK tối ưu (dung lượng chỉ ~6MB - 8MB) sẽ được tạo tại:
`app/build/outputs/apk/release/app-release.apk`

---

## 3. Cách Cài Đặt Lên Smart TV Thật

Có 3 cách thông dụng nhất:

### Cách A: Cài đặt không dây qua mạng Wi-Fi (Khuyên dùng - Nhanh nhất)
1. **Bật chế độ Nhà phát triển (Developer Options) trên TV:**
   - Vào `Cài đặt (Settings)` → `Tùy chọn thiết bị (Device Preferences)` → `Giới thiệu (About)`.
   - Tìm mục `Bản dựng HĐH Android TV (Build)` và bấm nút **OK trên Remote 7 lần liên tiếp** cho đến khi có thông báo "Bạn đã là nhà phát triển!".
   - Quay lại menu trước, vào `Tùy chọn nhà phát triển` → Bật **Gỡ lỗi USB (USB Debugging)**.
2. **Xem địa chỉ IP của TV:**
   - Vào `Cài đặt` → `Mạng & Internet` → Xem địa chỉ IP (Ví dụ: `192.168.1.50`).
3. **Kết nối và cài đặt từ máy tính:**
   ```bash
   adb connect 192.168.1.50:5555
   adb install -r app/build/outputs/apk/debug/app-debug.apk
   ```
4. Trên TV sẽ xuất hiện biểu tượng **LiveSport77 TV** trong danh sách ứng dụng trang chủ (Leanback Launcher).

---

### Cách B: Cài đặt bằng USB / Thẻ nhớ
1. Copy file `app-debug.apk` vào một chiếc USB (định dạng FAT32).
2. Cắm USB vào cổng sau của Smart TV.
3. Trên TV, tải ứng dụng quản lý file (như **X-plore File Manager** hoặc **File Commander** từ Google Play Store trên TV).
4. Mở USB trong app quản lý file, chọn `app-debug.apk` và nhấn **Cài đặt (Install)**.

---

### Cách C: Dùng ứng dụng "Send files to TV"
1. Cài app **Send files to TV** trên cả điện thoại (hoặc máy tính) và Smart TV.
2. Gửi file `app-debug.apk` sang TV qua cùng mạng Wi-Fi.
3. Mở file APK đã nhận để tiến hành cài đặt.

---

## 4. Hướng Dẫn Sử Dụng Remote TV (D-Pad Control)

| Phím Trên Remote TV | Chức Năng Tương Ứng |
| :--- | :--- |
| **Phím Lên / Xuống (▲ / ▼)** | Di chuyển duyệt danh sách các trận đấu ở Cột Trái |
| **Phím Trái / Phải (◀ / ▶)** | Chuyển đổi môn thể thao (Bóng đá, Bóng rổ, Tennis) |
| **Phím OK / Center (●)** | Mở chế độ xem **Big Scoreboard** toàn màn hình |
| **Phím Back (Quay lại)** | Thoát chế độ toàn màn hình, quay về danh sách trận |

---

## 5. Kiểm Tra Tải Trọng RAM & CPU Trên TV

Để chứng minh ứng dụng đạt tiêu chuẩn siêu tiết kiệm tài nguyên (< 60MB RAM):
```bash
adb shell dumpsys meminfo com.livesport77.tv
```
Kết quả đo:
- `TOTAL PSS`: Thường dao động từ **45MB – 54MB** (thấp hơn 4-5 lần so với các app chạy WebView thông thường).
- Không bị hệ điều hành Android TV tắt ứng dụng vì thiếu bộ nhớ.

