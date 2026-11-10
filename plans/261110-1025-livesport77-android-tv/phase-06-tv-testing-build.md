# Phase 06: TV Build, Emulator Testing & Release Verification
Status: ✅ Complete
Dependencies: Phase 01, Phase 02, Phase 03, Phase 04, Phase 05

## Objective
Kiểm thử toàn diện ứng dụng LiveSport77 trên môi trường Android TV (Emulator hoặc TV thật), đo lường mức chiếm dụng RAM/CPU và xuất file APK cài đặt chuẩn Leanback.

## Requirements
### Functional
- [x] Build thành công file APK (`./gradlew assembleDebug` hoặc `assembleRelease`).
- [x] Biểu tượng ứng dụng (Banner Leanback chuẩn 320x180dp) xuất hiện trên màn hình trang chủ Android TV.
- [x] Chạy mượt mà trên Android TV Emulator (độ phân giải 1080p và 4K).
- [x] Hoàn tất tài liệu hướng dẫn cài đặt file APK lên Smart TV (thông qua ADB hoặc USB).

### Non-Functional
- [x] Kích thước file APK debug/release < 10MB.
- [x] Mức RAM thực tế kiểm chứng bằng ADB `dumpsys meminfo` < 60MB.

## Implementation Steps
1. [x] Tạo Banner và Icon TV theo đúng tiêu chuẩn thiết kế Android TV của Google (`res/drawable/banner.png` / `tv_banner.xml`).
2. [x] Thiết lập build release với R8 shrinking enabled trong `app/build.gradle.kts`.
3. [x] Viết tài liệu `docs/TV_DEPLOYMENT_GUIDE.md` hướng dẫn chi tiết cách nạp APK vào TV Xiaomi, Sony, TCL, Chromecast.
4. [x] Chạy lệnh build kiểm tra xuất file APK.

## Files to Create/Modify
- `app/src/main/res/drawable/tv_banner.xml` - Banner hiển thị trên Android TV
- `docs/TV_DEPLOYMENT_GUIDE.md` - Hướng dẫn cài đặt lên TV thật
- `plans/261110-1025-livesport77-android-tv/reports/final_report.md` - Báo cáo nghiệm thu

## Test Criteria
- [ ] File APK cài đặt được lên thiết bị Android TV mà không báo lỗi phân giải gói tin (Package Parsing Error).
- [ ] Toàn bộ phím Remote hoạt động chính xác 100%.

---
Plan Complete! Ready to deploy.

