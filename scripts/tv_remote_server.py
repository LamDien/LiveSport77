#!/usr/bin/env python3
"""
LiveSport77 TV - Web-based Virtual Remote Controller
Provides a sleek visual D-Pad remote control in the browser that controls the Android TV emulator via ADB.
"""

import http.server
import socketserver
import subprocess
import urllib.parse
import json
import os
import sys

PORT = 7788
ADB_PATH = "/opt/homebrew/share/android-commandlinetools/platform-tools/adb"

HTML_CONTENT = """<!DOCTYPE html>
<html lang="vi">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>🎮 LiveSport77 - Remote Điều Khiển Android TV</title>
  <style>
    * { box-sizing: border-box; margin: 0; padding: 0; user-select: none; }
    body {
      background: #0B0E14;
      font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      min-height: 100vh;
      color: #E2E8F0;
      padding: 16px;
    }
    .remote-case {
      width: 320px;
      background: linear-gradient(180deg, #1A1F2C 0%, #11151F 100%);
      border-radius: 40px;
      padding: 28px 24px 36px;
      box-shadow: 0 20px 50px rgba(0,0,0,0.8), inset 0 1px 1px rgba(255,255,255,0.1), 0 0 0 1px #2D3748;
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 20px;
    }
    .remote-header {
      display: flex;
      align-items: center;
      justify-content: space-between;
      width: 100%;
      padding: 0 8px;
    }
    .brand {
      font-weight: 900;
      font-size: 14px;
      letter-spacing: 1.5px;
      color: #F59E0B;
    }
    .brand span { color: #10B981; }
    .status-pill {
      display: flex;
      align-items: center;
      gap: 6px;
      background: rgba(16, 185, 129, 0.15);
      border: 1px solid rgba(16, 185, 129, 0.4);
      padding: 4px 10px;
      border-radius: 20px;
      font-size: 11px;
      color: #10B981;
      font-weight: bold;
    }
    .status-dot {
      width: 6px;
      height: 6px;
      background: #10B981;
      border-radius: 50%;
      box-shadow: 0 0 8px #10B981;
    }
    
    /* D-PAD WHEEL */
    .dpad-container {
      position: relative;
      width: 220px;
      height: 220px;
      background: #1E2538;
      border-radius: 50%;
      box-shadow: inset 0 4px 10px rgba(0,0,0,0.6), 0 4px 15px rgba(0,0,0,0.4), 0 0 0 2px #2A344D;
      display: flex;
      align-items: center;
      justify-content: center;
      margin: 10px 0;
    }
    .dpad-btn {
      position: absolute;
      background: none;
      border: none;
      color: #94A3B8;
      font-size: 20px;
      cursor: pointer;
      display: flex;
      align-items: center;
      justify-content: center;
      transition: all 0.1s ease;
      touch-action: manipulation;
    }
    .dpad-btn:active {
      color: #F59E0B;
      transform: scale(0.92);
    }
    .dpad-up { top: 12px; left: 50%; transform: translateX(-50%); width: 80px; height: 50px; }
    .dpad-down { bottom: 12px; left: 50%; transform: translateX(-50%); width: 80px; height: 50px; }
    .dpad-left { left: 12px; top: 50%; transform: translateY(-50%); width: 50px; height: 80px; }
    .dpad-right { right: 12px; top: 50%; transform: translateY(-50%); width: 50px; height: 80px; }
    
    .dpad-center {
      width: 84px;
      height: 84px;
      background: linear-gradient(135deg, #F59E0B 0%, #D97706 100%);
      color: #0B0E14;
      font-weight: 900;
      font-size: 16px;
      border-radius: 50%;
      border: none;
      cursor: pointer;
      box-shadow: 0 4px 14px rgba(245, 158, 11, 0.4), inset 0 2px 2px rgba(255,255,255,0.4);
      display: flex;
      align-items: center;
      justify-content: center;
      transition: all 0.1s ease;
    }
    .dpad-center:active {
      transform: scale(0.92);
      box-shadow: 0 2px 6px rgba(245, 158, 11, 0.2);
    }

    /* ACTION BUTTONS */
    .button-row {
      display: flex;
      width: 100%;
      justify-content: space-around;
      gap: 12px;
    }
    .action-btn {
      flex: 1;
      height: 48px;
      background: #242C3F;
      border: 1px solid #334155;
      color: #CBD5E1;
      border-radius: 14px;
      font-size: 13px;
      font-weight: 600;
      cursor: pointer;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      gap: 2px;
      transition: all 0.1s ease;
      box-shadow: 0 2px 6px rgba(0,0,0,0.3);
    }
    .action-btn:active {
      background: #F59E0B;
      color: #0F172A;
      transform: scale(0.95);
    }
    .action-btn span { font-size: 16px; }

    /* SHORTCUT TABS */
    .shortcuts-label {
      font-size: 11px;
      color: #64748B;
      font-weight: bold;
      text-transform: uppercase;
      letter-spacing: 1px;
      align-self: flex-start;
      margin-top: 4px;
    }
    .sport-btn {
      background: #192132;
      border: 1px solid #283548;
      border-radius: 10px;
      padding: 10px 12px;
      color: #E2E8F0;
      font-size: 12px;
      font-weight: 600;
      cursor: pointer;
      flex: 1;
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 6px;
      transition: all 0.1s;
    }
    .sport-btn:active {
      border-color: #F59E0B;
      color: #F59E0B;
      background: #242F45;
    }
    
    .tip-box {
      margin-top: 16px;
      font-size: 12px;
      color: #94A3B8;
      text-align: center;
      max-width: 320px;
      line-height: 1.5;
    }
    .key-badge {
      background: #1E293B;
      border: 1px solid #334155;
      padding: 2px 6px;
      border-radius: 4px;
      font-family: monospace;
      color: #F59E0B;
    }
  </style>
</head>
<body>

  <div class="remote-case">
    <!-- Header -->
    <div class="remote-header">
      <div class="brand">LIVESPORT<span>77</span></div>
      <div class="status-pill">
        <div class="status-dot"></div>
        <span>TV CONNECTED</span>
      </div>
    </div>

    <!-- Directional Pad (D-PAD) -->
    <div class="dpad-container">
      <button class="dpad-btn dpad-up" onclick="sendKey(19)" title="Lên">▲</button>
      <button class="dpad-btn dpad-left" onclick="sendKey(21)" title="Trái">◀</button>
      <button class="dpad-center" onclick="sendKey(23)" title="OK / Chọn">OK</button>
      <button class="dpad-btn dpad-right" onclick="sendKey(22)" title="Phải">▶</button>
      <button class="dpad-btn dpad-down" onclick="sendKey(20)" title="Xuống">▼</button>
    </div>

    <!-- Navigation Action Row -->
    <div class="button-row">
      <button class="action-btn" onclick="sendKey(4)" title="Quay lại">
        <span>↩</span>
        Back
      </button>
      <button class="action-btn" onclick="sendKey(3)" title="Màn hình chính">
        <span>⌂</span>
        Home
      </button>
      <button class="action-btn" onclick="sendKey(66)" title="Enter">
        <span>⏎</span>
        Enter
      </button>
    </div>

    <!-- Volume & Power Row -->
    <div class="button-row">
      <button class="action-btn" onclick="sendKey(25)" title="Giảm âm lượng">
        <span>🔉</span>
        Vol -
      </button>
      <button class="action-btn" onclick="sendKey(24)" title="Tăng âm lượng">
        <span>🔊</span>
        Vol +
      </button>
      <button class="action-btn" onclick="restartApp()" title="Khởi động lại App">
        <span>🔄</span>
        Restart
      </button>
    </div>

    <!-- Sport Quick Switchers -->
    <div class="shortcuts-label">Chuyển môn thể thao nhanh</div>
    <div class="button-row">
      <button class="sport-btn" onclick="switchToSport('soccer')">⚽ Bóng đá</button>
      <button class="sport-btn" onclick="switchToSport('basketball')">🏀 Bóng rổ</button>
      <button class="sport-btn" onclick="switchToSport('tennis')">🎾 Tennis</button>
    </div>
  </div>

  <div class="tip-box">
    💡 <b>Mẹo:</b> Bạn cũng có thể dùng trực tiếp bàn phím: Phím mũi tên <span class="key-badge">↑ ↓ ← →</span> để di chuyển, <span class="key-badge">Enter</span> để chọn, <span class="key-badge">Esc</span> để quay lại.
  </div>

  <script>
    function sendKey(code) {
      if (navigator.vibrate) navigator.vibrate(20);
      fetch('/key?code=' + code)
        .catch(err => console.error('Error sending key:', err));
    }

    function restartApp() {
      fetch('/restart')
        .then(() => alert('Đang khởi động lại ứng dụng LiveSport77 trên TV...'))
        .catch(err => console.error(err));
    }

    function switchToSport(sport) {
      fetch('/sport?type=' + sport)
        .catch(err => console.error(err));
    }

    // Keyboard support when focused on remote tab
    window.addEventListener('keydown', (e) => {
      switch(e.key) {
        case 'ArrowUp': sendKey(19); break;
        case 'ArrowDown': sendKey(20); break;
        case 'ArrowLeft': sendKey(21); break;
        case 'ArrowRight': sendKey(22); break;
        case 'Enter': sendKey(23); break;
        case 'Escape':
        case 'Backspace': sendKey(4); break;
      }
    });
  </script>
</body>
</html>
"""

class RemoteHandler(http.server.BaseHTTPRequestHandler):
    def log_message(self, format, *args):
        pass  # Quiet logs

    def do_GET(self):
        parsed = urllib.parse.urlparse(self.path)
        path = parsed.path
        query = urllib.parse.parse_qs(parsed.query)

        if path == "/":
            self.send_response(200)
            self.send_header("Content-Type", "text/html; charset=utf-8")
            self.end_headers()
            self.wfile.write(HTML_CONTENT.encode("utf-8"))

        elif path == "/key":
            code = query.get("code", ["23"])[0]
            try:
                subprocess.run([ADB_PATH, "shell", "input", "keyevent", code], timeout=3)
                self.send_json({"status": "ok", "key": code})
            except Exception as e:
                self.send_json({"status": "error", "message": str(e)}, 500)

        elif path == "/restart":
            try:
                subprocess.run([ADB_PATH, "shell", "am", "force-stop", "com.livesport77.tv"], timeout=3)
                subprocess.run([ADB_PATH, "shell", "am", "start", "-n", "com.livesport77.tv/.MainActivity"], timeout=3)
                self.send_json({"status": "ok", "action": "restart"})
            except Exception as e:
                self.send_json({"status": "error", "message": str(e)}, 500)

        elif path == "/sport":
            stype = query.get("type", ["soccer"])[0]
            try:
                # Key sequence to focus header tabs
                # Navigate: UP to header, then LEFT or RIGHT to select tab
                if stype == "soccer":
                    subprocess.run([ADB_PATH, "shell", "input", "keyevent", "19"], timeout=2) # UP
                    subprocess.run([ADB_PATH, "shell", "input", "keyevent", "21"], timeout=2) # LEFT
                    subprocess.run([ADB_PATH, "shell", "input", "keyevent", "21"], timeout=2) # LEFT
                    subprocess.run([ADB_PATH, "shell", "input", "keyevent", "66"], timeout=2) # ENTER
                elif stype == "basketball":
                    subprocess.run([ADB_PATH, "shell", "input", "keyevent", "19"], timeout=2) # UP
                    subprocess.run([ADB_PATH, "shell", "input", "keyevent", "21"], timeout=2) # LEFT
                    subprocess.run([ADB_PATH, "shell", "input", "keyevent", "22"], timeout=2) # RIGHT
                    subprocess.run([ADB_PATH, "shell", "input", "keyevent", "66"], timeout=2) # ENTER
                elif stype == "tennis":
                    subprocess.run([ADB_PATH, "shell", "input", "keyevent", "19"], timeout=2) # UP
                    subprocess.run([ADB_PATH, "shell", "input", "keyevent", "22"], timeout=2) # RIGHT
                    subprocess.run([ADB_PATH, "shell", "input", "keyevent", "22"], timeout=2) # RIGHT
                    subprocess.run([ADB_PATH, "shell", "input", "keyevent", "66"], timeout=2) # ENTER
                self.send_json({"status": "ok", "sport": stype})
            except Exception as e:
                self.send_json({"status": "error", "message": str(e)}, 500)
        else:
            self.send_response(404)
            self.end_headers()

    def send_json(self, data, code=200):
        self.send_response(code)
        self.send_header("Content-Type", "application/json")
        self.end_headers()
        self.wfile.write(json.dumps(data).encode("utf-8"))

def main():
    socketserver.TCPServer.allow_reuse_address = True
    with socketserver.TCPServer(("", PORT), RemoteHandler) as httpd:
        print(f"Remote Server running at http://localhost:{PORT}")
        httpd.serve_forever()

if __name__ == "__main__":
    main()

