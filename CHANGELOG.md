# Changelog

## 1.0.0

- Initial release of the Android device performance classification plugin.
- Android-only support.
- Classifies devices into Unknown, Low, Average, or High performance categories based on CPU, memory, Android version, and media performance class (Android 12+).
- Provides type-safe enum-based API and direct enum comparison for easy UI integration.
- Exposes `PerformanceClass` enum from the main library.
- Uses platform channels for native Android functionality and accurate system property access.
- Async API for device info and performance class retrieval.
- Caching for improved performance.
- Optimized memory detection using `ActivityManager.MemoryInfo().totalMem`.
- Cleaned up debug logging and simplified code structure.
- Comprehensive device information gathering.
- Updated documentation and usage examples for async API and enum-based patterns.
