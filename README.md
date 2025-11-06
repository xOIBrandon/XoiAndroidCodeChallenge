# XOi Android Code Challenge — Concurrent Resource Loader

Welcome!  
This repository contains the starter project for the **live coding exercise** in your XOi interview.

---

## 🧠 Challenge Overview

Implement an **asynchronous image/file loader** that:
- Deduplicates simultaneous requests for the same resource.
- Limits total concurrent network requests to `k`.
- Caches successful results in memory.
- Cancels the underlying fetch if all callers cancel.

This should demonstrate your ability to reason about concurrency, resource sharing, and structured cancellation in Kotlin coroutines.

---

## 🧩 Prompt

> Implement an async image/file loader that deduplicates concurrent requests for the same resource and limits total concurrency to `k`.

### Requirements
1. If multiple callers request the same URL concurrently, **only one network fetch** should occur — others should await that same result.
2. **Limit concurrent network requests** to `k` total.
3. **Cache** successful results in memory (you can use a simple `MutableMap` or `LruCache`).
4. **Support cancellation** — if all callers cancel, the underlying fetch should also cancel.

---

## Live Coding Exercise — Concurrent Resource Loader

### Your Task
Implement **`ConcurrentResourceLoader`** in `app/src/main/java/com/example/xoiandroidcodechallenge/loader/ConcurrentResourceLoader.kt`.

**Requirements**
1. Deduplicate concurrent requests for the same URL (share a single in-flight fetch).
2. Limit total concurrent network requests to **k**.
3. Cache successful results in memory.
4. Support cancellation — if all callers cancel, cancel the underlying fetch.

### Test Harness (Compose UI)
Run the app and use the interactive controls:

**Configuration:**
- **URL input** — Enter the base URL to fetch
- **k (concurrency)** — Set the maximum concurrent network requests
- **# of callers** — Choose how many concurrent callers to launch (1, 3, 5, or 10)
- **Distinct URLs** — Choose how many unique URLs to generate (1, 2, or 3)

**Actions:**
- **Fetch X same URL** — Launch X concurrent requests for the same URL (tests deduplication)
- **Fetch X Y distinct URL(s)** — Launch X requests across Y distinct URLs (tests concurrency limit)
- **Cancel all** — Cancel all running jobs (tests cancellation propagation)
- **Clear logs** — Clear the log output

**Logs:**
- Real-time logging with timestamps
- **Request: #N** entries show when a fetch is initiated
- **Response: #N** entries show success/failure results
- Color-coded rows for easier scanning

> UI is for visualization only; you don't need to modify it.

### Test Suite
A comprehensive test suite is provided at:
`app/src/test/java/com/example/xoiandroidcodechallenge/ConcurrentResourceLoaderTest.kt`

**Run tests:**
```bash
./gradlew test
```

**Test coverage:**
- Deduplication of concurrent same-URL requests
- Concurrent requests completing together
- Concurrency limit enforcement (k)
- Caching of successful results
- Cancellation propagation when all callers cancel
- Non-caching of failures
- Per-URL deduplication
- High concurrency load handling

All tests will pass once the `ConcurrentResourceLoader` is correctly implemented.
