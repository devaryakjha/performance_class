# performance_class

A Flutter plugin for **Android device performance classification**.

## Overview

This plugin analyzes Android devices and classifies them into performance categories based on various hardware and software factors including:

- CPU cores and frequency
- Memory capacity
- Android version
- Media performance class (Android 12+)

**⚠️ Important: This plugin is designed specifically for Android devices only. It will not provide accurate performance classification on other platforms.**

## Performance Classes

The plugin classifies devices into four performance categories:

- **Unknown**: Unable to determine performance class
- **Low**: Low-end devices with limited resources
- **Average**: Mid-range devices with moderate performance
- **High**: High-end devices with excellent performance

## Installation

Add this dependency to your `pubspec.yaml`:

```yaml
dependencies:
  performance_class: ^1.0.0
```

## Usage

### Basic Usage

```dart
import 'package:performance_class/performance_class.dart';

void main() async {
  final classifier = PerformanceClassifier.instance;
  
  // Get performance class as enum
  final performanceClass = await classifier.getPerformanceClass();
  print('Performance Class: ${performanceClass.name}'); // e.g., "high"
  
  // Get human-readable string
  final displayName = classifier.getPerformanceClassString(performanceClass);
  print('Display Name: $displayName'); // e.g., "High"
  
  // Get device information
  final deviceInfo = await classifier.getDeviceInfo();
  print('CPU Cores: ${deviceInfo['cpu_count']}');
  print('Memory: ${deviceInfo['memory_class_mb']} MB');
}
```

### Detailed Device Information

```dart
final classifier = PerformanceClassifier.instance;
final detailedInfo = await classifier.getDetailedDeviceInfo();

print('Performance Class: ${detailedInfo['performanceClass']}');
print('Android Version: ${detailedInfo['androidVersion']}');
print('CPU Cores: ${detailedInfo['cpuCount']}');
print('Memory: ${detailedInfo['memoryClassMb']} MB');
print('Max CPU Frequency: ${detailedInfo['maxCpuFreqMhz']} MHz');
print('Is High-End: ${detailedInfo['isHighEnd']}');
print('Is Low-End: ${detailedInfo['isLowEnd']}');
```

### Using Performance Class Enum

```dart
import 'package:performance_class/performance_class.dart';

final classifier = PerformanceClassifier.instance;
final performanceClass = await classifier.getPerformanceClass();

// Direct enum comparison
if (performanceClass == PerformanceClass.high) {
  print('This is a high-end device!');
} else if (performanceClass == PerformanceClass.low) {
  print('This is a low-end device');
}

// Switch statement
switch (performanceClass) {
  case PerformanceClass.unknown:
    print('Unable to determine performance class');
    break;
  case PerformanceClass.low:
    print('Low performance device');
    break;
  case PerformanceClass.average:
    print('Average performance device');
    break;
  case PerformanceClass.high:
    print('High performance device');
    break;
}
```

### CPU Frequency Reading

```dart
final classifier = PerformanceClassifier.instance;
final cpuFreq = await classifier.readAverageMaxCpuFreq(4); // Check 4 CPU cores

if (cpuFreq != -1) {
  print('Average Max CPU Frequency: $cpuFreq MHz');
} else {
  print('Unable to read CPU frequency');
}
```

## API Reference

### PerformanceClassifier

Singleton class that provides performance classification functionality.

#### Methods

- `getPerformanceClass()`: Returns a [Future<PerformanceClass>] enum value representing the device's performance category
- `getDeviceInfo()`: Returns a [Future<Map<String, dynamic>>] with device capabilities
- `readAverageMaxCpuFreq(int cpuCount)`: Reads CPU frequency from system files (Android only)
- `getPerformanceClassString(PerformanceClass)`: Converts performance class enum to human-readable string
- `getDetailedDeviceInfo()`: Returns a [Future<Map<String, dynamic>>] with comprehensive device information

### Data Structures

#### PerformanceClass Enum

```dart
enum PerformanceClass {
  unknown(0),
  low(1),
  average(2),
  high(3);
}
```

#### Device Info Map

```dart
{
  'android_version': int,
  'cpu_count': int,
  'memory_class_mb': int,
  'max_cpu_freq_mhz': int,
  'media_performance_class': int,
}
```

## Classification Algorithm

The plugin uses a multi-tiered approach to classify device performance:

1. **Android 12+ (API 31+)**: Uses the system's `media_performance_class` property when available
2. **Fallback Algorithm**: Analyzes hardware specifications:
   - **Low**: Android < 26, ≤4 CPU cores, ≤2GB RAM, or ≤1.5GHz CPU
   - **Average**: ≤6 CPU cores, ≤4GB RAM, or ≤2.5GHz CPU
   - **High**: All other devices

## Platform Support

- **Android**: ✅ Full support with native performance analysis
- **iOS**: ❌ Not supported (returns unknown)
- **Linux**: ❌ Not supported (returns unknown)
- **Windows**: ❌ Not supported (returns unknown)
- **macOS**: ❌ Not supported (returns unknown)

**Note**: This plugin is specifically designed for Android devices. While it may compile and run on other platforms, it will not provide accurate performance classification and should only be used on Android devices.

## Example

See the `example/` directory for a complete Flutter application demonstrating the plugin's capabilities.

## License

This project is licensed under the MIT License - see the LICENSE file for details.

