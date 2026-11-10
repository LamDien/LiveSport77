import urllib.request
import json
import datetime

# Check what LiveScore returns for date today (20261110)
today_utc = datetime.datetime.now(datetime.timezone.utc).strftime("%Y%m%d")
print(f"Today UTC: {today_utc}")

# Let's test different endpoints:
endpoints = [
    # 1. What our app currently calls:
    f"https://prod-cdn-public-api.lsmedia1.com/v1/api/app/date/soccer/{today_utc}/0.00",
    # 2. Web endpoint without /app/ or with timezone offset?
    f"https://prod-cdn-public-api.lsmedia1.com/v1/api/app/date/soccer/{today_utc}/7.00",
    f"https://prod-cdn-public-api.lsmedia1.com/v1/api/app/live/soccer/0.00",
    f"https://prod-cdn-public-api.lsmedia1.com/v1/api/app/live/soccer/7.00"
]

for ep in endpoints:
    try:
        req = urllib.request.Request(ep, headers={"User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64)"})
        with urllib.request.urlopen(req, timeout=5) as resp:
            data = json.loads(resp.read().decode('utf-8'))
            stages = data.get("Stages", [])
            print(f"\n--- Endpoint: {ep} ---")
            print(f"Total stages: {len(stages)}")
            for s in stages[:8]:
                print(f"  League: {s.get('Snm')} ({s.get('Cnm')}) - {len(s.get('Events', []))} events")
                for e in s.get("Events", [])[:2]:
                    print(f"    Match: {e.get('T1', [{}])[0].get('Nm')} vs {e.get('T2', [{}])[0].get('Nm')} (Esd: {e.get('Esd')}, Eps: {e.get('Eps')})")
    except Exception as e:
        print(f"Error {ep}: {e}")

