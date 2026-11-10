#!/bin/bash
set -e

export ANDROID_HOME=/opt/homebrew/share/android-commandlinetools
export ANDROID_SDK_ROOT=/opt/homebrew/share/android-commandlinetools
export ANDROID_AVD_HOME=/Users/hoanng/AIAgent/LiveSport/.android/avd
export ANDROID_USER_HOME=/Users/hoanng/AIAgent/LiveSport/.android
export PATH=$ANDROID_HOME/emulator:$ANDROID_HOME/platform-tools:$PATH

echo "============================================================"
echo "🚀 LIVESPORT77 ANDROID TV: KHỞI ĐỘNG VÀ DEPLOY APK"
echo "============================================================"

APK_PATH="/Users/hoanng/AIAgent/LiveSport/app/build/outputs/apk/debug/app-debug.apk"

if [ ! -f "$APK_PATH" ]; then
    echo "❌ Không tìm thấy file APK tại: $APK_PATH"
    exit 1
fi

echo "📦 Đã tìm thấy file APK: $(ls -lh "$APK_PATH" | awk '{print $5, $9}')"

# Check if an emulator or device is already connected
EXISTING_DEVICE=$(adb devices | grep -E "emulator-[0-9]+" | awk '{print $1}' | head -n 1 || true)

if [ -n "$EXISTING_DEVICE" ]; then
    echo "✅ Đã phát hiện Android TV Emulator đang chạy: $EXISTING_DEVICE"
    TARGET_DEVICE="$EXISTING_DEVICE"
else
    echo "📺 Đang khởi động Android TV ảo (AndroidTV_1080p)..."
    # Launch emulator in background with hardware acceleration
    emulator -avd AndroidTV_1080p -gpu auto -no-boot-anim &
    EMULATOR_PID=$!
    echo "⏳ Emulator PID: $EMULATOR_PID. Đang chờ thiết bị khởi động..."
    
    adb wait-for-device
    echo "📲 Thiết bị đã kết nối ADB. Đang đợi nạp hệ điều hành TV..."
    
    # Wait until boot is completed
    BOOT_COMPLETED=""
    ATTEMPTS=0
    while [ "$BOOT_COMPLETED" != "1" ] && [ $ATTEMPTS -lt 60 ]; do
        BOOT_COMPLETED=$(adb shell getprop sys.boot_completed 2>/dev/null | tr -d '\r' || true)
        if [ "$BOOT_COMPLETED" != "1" ]; then
            sleep 2
            ATTEMPTS=$((ATTEMPTS + 1))
        fi
    done
    echo "✅ Hệ điều hành Android TV đã sẵn sàng (boot completed)!"
fi

echo "📲 Đang nạp và cài đặt LiveSport77 APK lên Android TV..."
adb install -r "$APK_PATH"

echo "🎉 Cài đặt thành công! Đang kích hoạt ứng dụng trên màn hình TV..."
adb shell am start -n com.livesport77.tv/.MainActivity

echo "============================================================"
echo "✨ LIVESPORT77 TV ĐÃ ĐƯỢC MỞ TRÊN MÀN HÌNH ANDROID TV!"
echo "============================================================"

