import 'dart:io';
import 'package:flutter/services.dart';

/// Performance classification for Android devices.
///
/// ⚠️ **IMPORTANT**: This plugin is designed specifically for Android devices only.
/// While it may compile and run on other platforms, it will not provide accurate
/// performance classification and should only be used on Android devices.
///
/// This class provides methods to classify Android devices into performance categories
/// based on various hardware and software factors including CPU, memory, Android version,
/// and media performance class.
class PerformanceClassifier {
  static final PerformanceClassifier _instance =
      PerformanceClassifier._internal();
  factory PerformanceClassifier() => _instance;
  PerformanceClassifier._internal();

  // Platform channel for Android-specific features
  static const MethodChannel _channel = MethodChannel('anpec');

  /// Get the singleton instance of PerformanceClassifier
  static PerformanceClassifier get instance => _instance;

  /// Get the device performance class.
  ///
  /// This method analyzes the device capabilities and returns a performance classification.
  /// The result is cached for subsequent calls to improve performance.
  ///
  /// ⚠️ **Android only**: This function is designed for Android devices and may not
  /// provide accurate results on other platforms.
  ///
  /// Returns a [PerformanceClass] enum value representing the device's performance category.
  Future<PerformanceClass> getPerformanceClass() async {
    if (Platform.isAndroid) {
      try {
        final result = await _channel.invokeMethod<int>('getPerformanceClass');
        return PerformanceClass.fromValue(result ?? 0);
      } catch (e) {
        // Return unknown if platform channel fails
        return PerformanceClass.unknown;
      }
    } else {
      // Non-Android platforms return unknown
      return PerformanceClass.unknown;
    }
  }

  /// Get detailed device information.
  ///
  /// ⚠️ **Android only**: This function is designed for Android devices and may not
  /// provide accurate results on other platforms.
  ///
  /// Returns a map containing information about the device's hardware and software capabilities.
  Future<Map<String, dynamic>> getDeviceInfo() async {
    if (Platform.isAndroid) {
      try {
        final result = await _channel.invokeMethod('getDeviceInfo');

        // Convert the result to the correct type
        if (result is Map) {
          final deviceInfo = <String, dynamic>{};
          result.forEach((key, value) {
            if (key is String) {
              deviceInfo[key] = value;
            }
          });
          return deviceInfo;
        }

        return _getDefaultDeviceInfo();
      } catch (e) {
        return _getDefaultDeviceInfo();
      }
    } else {
      return _getDefaultDeviceInfo();
    }
  }

  /// Read CPU frequency information.
  ///
  /// This method reads the maximum CPU frequency from system files.
  /// ⚠️ **Android only**: This function only works on Android systems.
  ///
  /// [cpuCount] - The number of CPU cores to check
  /// Returns the average maximum CPU frequency in MHz, or -1 if unable to read
  Future<int> readAverageMaxCpuFreq(int cpuCount) async {
    if (Platform.isAndroid) {
      try {
        final result = await _channel.invokeMethod<int>(
          'readAverageMaxCpuFreq',
          {'cpuCount': cpuCount},
        );
        return result ?? -1;
      } catch (e) {
        return -1;
      }
    } else {
      return -1;
    }
  }

  /// Get a human-readable string representation of the performance class.
  ///
  /// [performanceClass] - The performance class enum value
  /// Returns a string describing the performance class
  String getPerformanceClassString(PerformanceClass performanceClass) {
    switch (performanceClass) {
      case PerformanceClass.unknown:
        return 'Unknown';
      case PerformanceClass.low:
        return 'Low';
      case PerformanceClass.average:
        return 'Average';
      case PerformanceClass.high:
        return 'High';
    }
  }

  /// Get a detailed description of the device capabilities.
  ///
  /// ⚠️ **Android only**: This function is designed for Android devices and may not
  /// provide accurate results on other platforms.
  ///
  /// Returns a map containing detailed information about the device's performance characteristics
  Future<Map<String, dynamic>> getDetailedDeviceInfo() async {
    final deviceInfo = await getDeviceInfo();
    final performanceClass = await getPerformanceClass();

    return {
      'performanceClass': getPerformanceClassString(performanceClass),
      'performanceClassRaw': performanceClass.value,
      'androidVersion': deviceInfo['android_version'],
      'cpuCount': deviceInfo['cpu_count'],
      'memoryClassMb': deviceInfo['memory_class_mb'],
      'maxCpuFreqMhz': deviceInfo['max_cpu_freq_mhz'],
      'mediaPerformanceClass': deviceInfo['media_performance_class'],
      'isHighEnd': performanceClass == PerformanceClass.high,
      'isLowEnd': performanceClass == PerformanceClass.low,
    };
  }

  /// Get default device info for non-Android platforms
  Map<String, dynamic> _getDefaultDeviceInfo() {
    return {
      'android_version': -1,
      'cpu_count': 0,
      'memory_class_mb': 0,
      'max_cpu_freq_mhz': -1,
      'media_performance_class': -1,
    };
  }
}

/// Performance class enum representing device performance categories
enum PerformanceClass {
  unknown(0),
  low(1),
  average(2),
  high(3);

  const PerformanceClass(this.value);
  final int value;

  static PerformanceClass fromValue(int value) {
    return PerformanceClass.values.firstWhere(
      (e) => e.value == value,
      orElse: () => PerformanceClass.unknown,
    );
  }
}
