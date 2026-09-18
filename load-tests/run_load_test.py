#!/usr/bin/env python3
"""
Sentinel AI Baseline & Load Testing Engine
Simulates 100 concurrent virtual users for 60 seconds continuously,
measuring real-time Requests Per Second (RPS), latency percentiles (min, avg, p50, p90, p95, p99, max),
HTTP status codes, and per-endpoint performance.
"""

import os
import sys
import time
import json
import asyncio
import statistics
from typing import List, Dict, Any
from datetime import datetime

# Configure resilient stdout/stderr encoding on Windows
if hasattr(sys.stdout, "reconfigure"):
    sys.stdout.reconfigure(encoding="utf-8", errors="replace")
if hasattr(sys.stderr, "reconfigure"):
    sys.stderr.reconfigure(encoding="utf-8", errors="replace")

try:
    import aiohttp
except ImportError:
    print("[Error] aiohttp is required. Please run: pip install aiohttp")
    sys.exit(1)

# Default benchmark parameters
DEFAULT_CONCURRENT_USERS = 100
DEFAULT_DURATION_SECONDS = 60
DEFAULT_TARGET_URL = "http://127.0.0.1:8000"

# Realistic traffic distribution across API endpoints
REQUEST_WEIGHTS = [
    {"endpoint": "/api/health", "method": "GET", "weight": 35, "json": None},
    {"endpoint": "/", "method": "GET", "weight": 25, "json": None},
    {"endpoint": "/api/scan/url", "method": "POST", "weight": 20, "json": {"url": "https://google.com"}},
    {"endpoint": "/api/scan/fraud", "method": "POST", "weight": 10, "json": {"content": "Your banking code is 584920", "scan_type": "SMS"}},
    {"endpoint": "/api/analytics/metrics", "method": "GET", "weight": 10, "json": None},
]

class LoadTestMetrics:
    def __init__(self):
        self.records: List[Dict[str, Any]] = []

    def record(self, endpoint: str, method: str, status: int, latency_ms: float, timestamp: float):
        self.records.append({
            "endpoint": endpoint,
            "method": method,
            "status": status,
            "latency_ms": latency_ms,
            "timestamp": timestamp
        })

async def worker_user(user_id: int, session: aiohttp.ClientSession, base_url: str, stop_time: float, metrics: LoadTestMetrics):
    """Simulates a single virtual user continuously sending requests until stop_time."""
    endpoints = []
    for item in REQUEST_WEIGHTS:
        endpoints.extend([item] * item["weight"])

    idx = user_id % len(endpoints)
    while time.perf_counter() < stop_time:
        target = endpoints[idx % len(endpoints)]
        idx += 1
        
        full_url = f"{base_url}{target['endpoint']}"
        start_t = time.perf_counter()
        status_code = 0
        try:
            if target["method"] == "GET":
                async with session.get(full_url, timeout=aiohttp.ClientTimeout(total=5.0)) as resp:
                    status_code = resp.status
                    await resp.read()
            elif target["method"] == "POST":
                async with session.post(full_url, json=target["json"], timeout=aiohttp.ClientTimeout(total=5.0)) as resp:
                    status_code = resp.status
                    await resp.read()
        except Exception:
            status_code = 0

        end_t = time.perf_counter()
        latency_ms = (end_t - start_t) * 1000.0
        metrics.record(target["endpoint"], target["method"], status_code, latency_ms, end_t)

        # Micro-yield to allow asynchronous cooperative multitasking across 100 concurrent workers
        await asyncio.sleep(0.001)

async def progress_reporter(metrics: LoadTestMetrics, start_time: float, duration: int):
    """Prints live terminal telemetry every 5 seconds."""
    last_count = 0
    last_time = start_time
    while True:
        await asyncio.sleep(5)
        now = time.perf_counter()
        elapsed = now - start_time
        if elapsed > duration:
            break
        current_count = len(metrics.records)
        interval_requests = current_count - last_count
        interval_time = now - last_time
        current_rps = interval_requests / interval_time if interval_time > 0 else 0
        
        recent_latencies = [r["latency_ms"] for r in metrics.records[-interval_requests:]] if interval_requests > 0 else []
        avg_lat = statistics.mean(recent_latencies) if recent_latencies else 0

        print(f"  [TIME] [{int(elapsed):02d}s / {duration}s] Completed: {current_count:,} reqs | Instant RPS: {current_rps:.1f} req/s | Interval Avg Latency: {avg_lat:.1f}ms")
        last_count = current_count
        last_time = now

async def run_load_test(
    concurrency: int = DEFAULT_CONCURRENT_USERS,
    duration: int = DEFAULT_DURATION_SECONDS,
    base_url: str = DEFAULT_TARGET_URL
) -> Dict[str, Any]:
    print("=" * 75)
    print("SENTINEL AI - HIGH-CONCURRENCY BASELINE LOAD TEST")
    print("=" * 75)
    print(f"Target URL:         {base_url}")
    print(f"Concurrent Users:   {concurrency} virtual users")
    print(f"Test Duration:      {duration} seconds (1 continuous minute)")
    print(f"Start Timestamp:    {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}")
    print("-" * 75)

    metrics = LoadTestMetrics()
    conn = aiohttp.TCPConnector(limit=concurrency * 2, ttl_dns_cache=300, enable_cleanup_closed=True)
    
    start_perf = time.perf_counter()
    stop_perf = start_perf + duration

    async with aiohttp.ClientSession(connector=conn) as session:
        reporter_task = asyncio.create_task(progress_reporter(metrics, start_perf, duration))
        
        workers = [
            worker_user(i, session, base_url, stop_perf, metrics)
            for i in range(concurrency)
        ]
        
        await asyncio.gather(*workers)
        reporter_task.cancel()

    total_time_elapsed = time.perf_counter() - start_perf
    total_requests = len(metrics.records)
    print("-" * 75)
    print(f"[OK] Load test completed in {total_time_elapsed:.2f} seconds.")
    print(f"Total Requests Executed: {total_requests:,}")

    # Process Statistics
    latencies = [r["latency_ms"] for r in metrics.records] if metrics.records else [0]
    latencies.sort()
    
    min_lat = min(latencies)
    max_lat = max(latencies)
    avg_lat = statistics.mean(latencies)
    median_lat = statistics.median(latencies)
    p90_lat = latencies[int(len(latencies) * 0.90)] if latencies else 0
    p95_lat = latencies[int(len(latencies) * 0.95)] if latencies else 0
    p99_lat = latencies[int(len(latencies) * 0.99)] if latencies else 0
    overall_rps = total_requests / total_time_elapsed if total_time_elapsed > 0 else 0

    status_counts = {}
    for r in metrics.records:
        st = r["status"]
        status_counts[st] = status_counts.get(st, 0) + 1

    success_count = sum(cnt for st, cnt in status_counts.items() if 200 <= st < 400)
    failed_count = total_requests - success_count
    success_rate = (success_count / total_requests) * 100 if total_requests else 0

    # Per-Endpoint Breakdown
    endpoints_data = {}
    for r in metrics.records:
        ep = r["endpoint"]
        if ep not in endpoints_data:
            endpoints_data[ep] = {"count": 0, "latencies": [], "statuses": {}}
        endpoints_data[ep]["count"] += 1
        endpoints_data[ep]["latencies"].append(r["latency_ms"])
        st = r["status"]
        endpoints_data[ep]["statuses"][st] = endpoints_data[ep]["statuses"].get(st, 0) + 1

    endpoint_summary = {}
    for ep, data in endpoints_data.items():
        ep_lats = sorted(data["latencies"])
        endpoint_summary[ep] = {
            "requests": data["count"],
            "rps": round(data["count"] / total_time_elapsed, 1),
            "min_ms": round(min(ep_lats), 2),
            "avg_ms": round(statistics.mean(ep_lats), 2),
            "median_ms": round(statistics.median(ep_lats), 2),
            "p95_ms": round(ep_lats[int(len(ep_lats) * 0.95)], 2) if ep_lats else 0,
            "max_ms": round(max(ep_lats), 2),
            "success_rate": round((sum(cnt for s, cnt in data["statuses"].items() if 200 <= s < 400) / data["count"]) * 100, 2)
        }

    # Timeline distribution by second
    second_buckets = {}
    for r in metrics.records:
        sec = int(r["timestamp"] - start_perf)
        if 0 <= sec < int(total_time_elapsed) + 1:
            if sec not in second_buckets:
                second_buckets[sec] = []
            second_buckets[sec].append(r["latency_ms"])

    timeline = []
    for s in range(int(total_time_elapsed) + 1):
        if s in second_buckets and second_buckets[s]:
            l_list = second_buckets[s]
            timeline.append({
                "second": s + 1,
                "requests": len(l_list),
                "rps": len(l_list),
                "avg_ms": round(statistics.mean(l_list), 2),
                "p95_ms": round(sorted(l_list)[int(len(l_list)*0.95)], 2),
                "max_ms": round(max(l_list), 2)
            })

    results = {
        "metadata": {
            "target_url": base_url,
            "concurrency": concurrency,
            "duration_seconds": duration,
            "actual_duration_seconds": round(total_time_elapsed, 2),
            "timestamp": datetime.now().isoformat()
        },
        "summary": {
            "total_requests": total_requests,
            "successful_requests": success_count,
            "failed_requests": failed_count,
            "success_rate_percent": round(success_rate, 2),
            "rps": round(overall_rps, 1),
            "response_time_ms": {
                "min": round(min_lat, 2),
                "avg": round(avg_lat, 2),
                "median": round(median_lat, 2),
                "p90": round(p90_lat, 2),
                "p95": round(p95_lat, 2),
                "p99": round(p99_lat, 2),
                "max": round(max_lat, 2)
            },
            "status_codes": status_counts
        },
        "endpoints": endpoint_summary,
        "timeline": timeline
    }

    # Save benchmark JSON
    output_dir = os.path.join(os.path.dirname(os.path.abspath(__file__)))
    json_path = os.path.join(output_dir, "load_test_data.json")
    with open(json_path, "w", encoding="utf-8") as f:
        json.dump(results, f, indent=2)

    # Print Terminal Executive Summary
    print("\n" + "=" * 75)
    print("BASELINE LOAD TESTING BENCHMARK RESULTS")
    print("=" * 75)
    print(f"Throughput (RPS):       {results['summary']['rps']} req/sec")
    print(f"Total Requests Sent:    {total_requests:,}")
    print(f"Successful (2xx-3xx):   {success_count:,} ({results['summary']['success_rate_percent']}%)")
    print(f"Failed (4xx/5xx/Err):   {failed_count:,}")
    print("-" * 75)
    print("Response Times:")
    print(f"   - Minimum:             {results['summary']['response_time_ms']['min']}ms")
    print(f"   - Average:             {results['summary']['response_time_ms']['avg']}ms")
    print(f"   - Median (p50):        {results['summary']['response_time_ms']['median']}ms")
    print(f"   - 90th Percentile:     {results['summary']['response_time_ms']['p90']}ms")
    print(f"   - 95th Percentile:     {results['summary']['response_time_ms']['p95']}ms")
    print(f"   - 99th Percentile:     {results['summary']['response_time_ms']['p99']}ms")
    print(f"   - Maximum (Slowest):   {results['summary']['response_time_ms']['max']}ms")
    print("-" * 75)
    print("Per-Endpoint Breakdown:")
    for ep, data in results["endpoints"].items():
        print(f"   {ep:<24} | {data['requests']:>6} reqs | {data['rps']:>5} RPS | Avg: {data['avg_ms']:>6.1f}ms | p95: {data['p95_ms']:>6.1f}ms | Success: {data['success_rate']}%")
    print("=" * 75)

    return results

if __name__ == "__main__":
    users = int(sys.argv[1]) if len(sys.argv) > 1 else DEFAULT_CONCURRENT_USERS
    dur = int(sys.argv[2]) if len(sys.argv) > 2 else DEFAULT_DURATION_SECONDS
    url = sys.argv[3] if len(sys.argv) > 3 else DEFAULT_TARGET_URL
    asyncio.run(run_load_test(concurrency=users, duration=dur, base_url=url))
