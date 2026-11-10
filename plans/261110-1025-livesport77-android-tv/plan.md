# Plan: LiveSport77 - Android TV Native (Kotlin + Compose for TV)
Created: 2026-11-10 10:25
Status: ✅ Complete (6/6 Phases Done)
Platform: Android TV / Google TV (Native APK)
Performance Goal: RAM < 60MB, CPU < 2% idle, 60 FPS D-pad Navigation

## Overview
Xây dựng ứng dụng LiveSport77 hoàn toàn bằng **Native Android (Kotlin + Jetpack Compose for TV)** nhằm tối ưu hóa triệt để tài nguyên phần cứng TV (RAM, CPU), mang lại trải nghiệm xem tỷ số và thông tin đa thể thao thời gian thực (Bóng đá, Bóng rổ, Tennis) mượt mà chuẩn 60fps trên mọi loại Android TV.

## Tech Stack
- Ngôn ngữ: Kotlin 2.x
- UI Toolkit: Jetpack Compose for TV (`androidx.tv.material3`, `androidx.tv.foundation`)
- Kiến trúc: MVVM + Clean Architecture + StateFlow
- Bất đồng bộ & Realtime: Kotlin Coroutines & Smart Polling Manager
- Image Loader: Coil for Android TV (Giới hạn Cache < 15MB)
- Build System: Gradle (Kotlin DSL)

## Phases Roadmap

| Phase | Tên Giai Đoạn | Trọng Tâm Kỹ Thuật | Status | Progress |
|---|---|---|---|---|
| 01 | Native Project Setup | Khởi tạo Android TV project, Gradle, Compose for TV, cấu hình RAM budget | ✅ Complete | 100% |
| 02 | Sports Data & Repository | Xây dựng Data Models (@Immutable), Repository & Mock Data chuẩn LiveScore | ✅ Complete | 100% |
| 03 | Native TV D-Pad Focus | Xây dựng hệ thống Focus D-Pad, FocusRequester, Key Event Handler | ✅ Complete | 100% |
| 04 | TV Match Center UI | Xây dựng Master-Detail UI (TvTabRow, TvLazyColumn, Match Details) | ✅ Complete | 100% |
| 05 | Smart Polling & Memory Guard | Cơ chế cập nhật thời gian thực tiết kiệm CPU & Quản lý bộ nhớ RAM < 60MB | ✅ Complete | 100% |
| 06 | TV Build & Verification | Đóng gói APK Android TV, kiểm thử trên Android TV Emulator / Thiết bị thật | ✅ Complete | 100% |

## Reports & Deliverables
- [docs/TV_DEPLOYMENT_GUIDE.md](file:///Users/hoanng/AIAgent/LiveSport/docs/TV_DEPLOYMENT_GUIDE.md) - Hướng dẫn nạp và cài đặt lên Smart TV thật
- [plans/261110-1025-livesport77-android-tv/reports/final_report.md](file:///Users/hoanng/AIAgent/LiveSport/plans/261110-1025-livesport77-android-tv/reports/final_report.md) - Báo cáo kiểm định tài nguyên RAM/CPU
- [docs/mockup/index.html](file:///Users/hoanng/AIAgent/LiveSport/docs/mockup/index.html) - Trải nghiệm giao diện mô phỏng trên trình duyệt
