# Changelog

## 1.2.0

- **FIX**: Fixed memory detection to show actual device memory instead of memory class
- **FIX**: Corrected memory calculation using `ActivityManager.MemoryInfo().totalMem` for accurate results
- **IMPROVEMENT**: Removed all debug logging for cleaner production code
- **IMPROVEMENT**: Optimized memory calculation performance
- **CLEANUP**: Removed unnecessary debug statements from both Android and Dart code
- **CLEANUP**: Simplified code structure and improved readability

## 1.1.0

- **BREAKING**: Changed `getPerformanceClass()` to return `Future<PerformanceClass>` enum directly instead of raw integer values
- **BREAKING**: Made `getDeviceInfo()` and `getDetailedDeviceInfo()` async for better platform channel integration
- **IMPROVEMENT**: Implemented hybrid architecture using platform channels for Android-specific features
- **IMPROVEMENT**: Enhanced developer experience with type-safe enum-based API
- **IMPROVEMENT**: Added direct enum comparison support for better UI integration
- **IMPROVEMENT**: Exported `PerformanceClass` enum from main library for easy access
- **IMPROVEMENT**: Updated `getDetailedDeviceInfo()` to use enum values for cleaner boolean checks
- **FIX**: Fixed Android system property access using native platform channels
- **FIX**: Improved media performance class detection for Android 12+ devices
- **DOCS**: Updated README with new async usage examples and switch statement patterns
- **DOCS**: Added comprehensive examples for UI integration with performance classes
- **DOCS**: Documented hybrid architecture approach

## 1.0.0

- Initial release of the Android device performance classification plugin
- **Android-only support**: Plugin is specifically designed for Android devices
- Performance classification algorithm based on hardware and software factors:
  - CPU cores and frequency analysis
  - Memory capacity detection
  - Android version detection
  - Media performance class support (Android 12+)
- Four performance categories: Unknown, Low, Average, High
- Caching mechanism for improved performance
- Comprehensive device information gathering
- Platform channel implementation for native Android functionality
- Type-safe enum-based API for better developer experience
