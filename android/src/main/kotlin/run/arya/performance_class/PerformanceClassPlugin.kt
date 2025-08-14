package run.arya.performance_class

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import java.io.RandomAccessFile
import java.util.Locale
import kotlin.math.ceil

class PerformanceClassPlugin: FlutterPlugin, MethodCallHandler {
  private lateinit var channel : MethodChannel
  private lateinit var context: Context

  override fun onAttachedToEngine(flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
    channel = MethodChannel(flutterPluginBinding.binaryMessenger, "performance_class")
    channel.setMethodCallHandler(this)
    context = flutterPluginBinding.applicationContext
  }

  override fun onMethodCall(call: MethodCall, result: Result) {
    when (call.method) {
      "getPerformanceClass" -> {
        try {
          val performanceClass = DevicePerformanceClass.getDevicePerformanceClass(context)
          result.success(performanceClass.ordinal)
        } catch (e: Exception) {
          result.error("PERFORMANCE_CLASS_ERROR", "Failed to get performance class", e.message)
        }
      }
      "getDeviceInfo" -> {
        try {
          val deviceInfo = getDeviceInfo()
          result.success(deviceInfo)
        } catch (e: Exception) {
          result.error("DEVICE_INFO_ERROR", "Failed to get device info", e.message)
        }
      }
      "readAverageMaxCpuFreq" -> {
        try {
          val cpuCount = call.argument<Int>("cpuCount") ?: Runtime.getRuntime().availableProcessors()
          val cpuFreq = readAverageMaxCpuFreq(cpuCount)
          result.success(cpuFreq)
        } catch (e: Exception) {
          result.error("CPU_FREQ_ERROR", "Failed to read CPU frequency", e.message)
        }
      }
      else -> {
        result.notImplemented()
      }
    }
  }

  override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
    channel.setMethodCallHandler(null)
  }

  private fun getDeviceInfo(): Map<String, Any> {
    val androidVersion = Build.VERSION.SDK_INT
    val cpuCount = Runtime.getRuntime().availableProcessors()
    val totalMemoryMB = getTotalMemoryMB()
    val maxCpuFreq = readAverageMaxCpuFreq(cpuCount)
    val mediaPerformanceClass = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
      Build.VERSION.MEDIA_PERFORMANCE_CLASS
    } else {
      -1
    }

    return mapOf(
      "android_version" to androidVersion,
      "cpu_count" to cpuCount,
      "memory_class_mb" to totalMemoryMB,
      "max_cpu_freq_mhz" to maxCpuFreq,
      "media_performance_class" to mediaPerformanceClass
    )
  }

  private fun getTotalMemoryMB(): Int {
    val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    val memoryInfo = ActivityManager.MemoryInfo()
    activityManager.getMemoryInfo(memoryInfo)
    
    // Convert from bytes to MB
    return (memoryInfo.totalMem / (1024 * 1024)).toInt()
  }

  private fun readAverageMaxCpuFreq(cpuCount: Int): Int {
    var totalCpuFreq = 0
    var freqResolved = 0
    val maxRetries = 3
    
    for (retry in 0 until maxRetries) {
      try {
        for (i in 0 until cpuCount) {
          try {
            val reader = RandomAccessFile(
              String.format(
                Locale.ENGLISH,
                "/sys/devices/system/cpu/cpu%d/cpufreq/cpuinfo_max_freq",
                i
              ), "r"
            )
            val line = reader.readLine()
            if (line != null && line.isNotEmpty()) {
              val freq = line.toInt()
              if (freq > 0) {
                totalCpuFreq += freq / 1000 // Convert from kHz to MHz
                freqResolved++
              }
            }
            reader.close()
          } catch (ignore: Throwable) {
            // Continue with next CPU core
          }
        }
        
        // If we got at least one valid frequency reading, break
        if (freqResolved > 0) break
        
      } catch (e: Exception) {
        // Retry on next iteration
      }
    }
    
    return if (freqResolved == 0) -1 else ceil((totalCpuFreq / freqResolved.toFloat()).toDouble()).toInt()
  }
}

enum class DevicePerformanceClass {
    PERFORMANCE_CLASS_UNDEFINED,
    PERFORMANCE_CLASS_LOW,
    PERFORMANCE_CLASS_AVERAGE,
    PERFORMANCE_CLASS_HIGH;

    companion object {
        private var _devicePerformanceClass: DevicePerformanceClass = PERFORMANCE_CLASS_UNDEFINED
        private var _performanceCacheValid = false

        fun getDevicePerformanceClass(context: Context): DevicePerformanceClass {
            if (_devicePerformanceClass == PERFORMANCE_CLASS_UNDEFINED || !_performanceCacheValid) {
                try {
                    val androidVersion = Build.VERSION.SDK_INT
                    val cpuCount = Runtime.getRuntime().availableProcessors()
                    val totalMemoryMB = getTotalMemoryMB(context)

                    _devicePerformanceClass = when {
                        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> { // API 31 (Android 12) and above
                            val mediaPerformanceClass = Build.VERSION.MEDIA_PERFORMANCE_CLASS
                            when {
                                mediaPerformanceClass >= 33 -> PERFORMANCE_CLASS_HIGH
                                mediaPerformanceClass >= 31 -> PERFORMANCE_CLASS_AVERAGE
                                else -> fallbackToCpuCheck(androidVersion, cpuCount, totalMemoryMB)
                            }
                        }
                        else -> {
                            fallbackToCpuCheck(androidVersion, cpuCount, totalMemoryMB)
                        }
                    }
                    _performanceCacheValid = true
                } catch (e: Exception) {
                    // Fallback to conservative performance class if detection fails
                    _devicePerformanceClass = PERFORMANCE_CLASS_LOW
                    _performanceCacheValid = true
                }
            }

            return _devicePerformanceClass
        }

        private fun getTotalMemoryMB(context: Context): Int {
            val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val memoryInfo = ActivityManager.MemoryInfo()
            activityManager.getMemoryInfo(memoryInfo)
            
            // Convert from bytes to MB
            return (memoryInfo.totalMem / (1024 * 1024)).toInt()
        }

        private fun fallbackToCpuCheck(androidVersion: Int, cpuCount: Int, totalMemoryMB: Int): DevicePerformanceClass {
            val maxCpuFreq = readAverageMaxCpuFreq(cpuCount)

            return when {
                // Conservative checks for low-end devices
                androidVersion < 26 || cpuCount <= 4 || totalMemoryMB <= 2048 ||
                        (maxCpuFreq != -1 && maxCpuFreq <= 1500) -> PERFORMANCE_CLASS_LOW

                // Medium performance devices
                cpuCount <= 6 || totalMemoryMB <= 4096 ||
                        (maxCpuFreq != -1 && maxCpuFreq <= 2500) -> PERFORMANCE_CLASS_AVERAGE

                // High performance devices
                else -> PERFORMANCE_CLASS_HIGH
            }
        }

        private fun readAverageMaxCpuFreq(cpuCount: Int): Int {
            var totalCpuFreq = 0
            var freqResolved = 0
            val maxRetries = 3
            
            for (retry in 0 until maxRetries) {
                try {
                    for (i in 0 until cpuCount) {
                        try {
                            val reader = RandomAccessFile(
                                String.format(
                                    Locale.ENGLISH,
                                    "/sys/devices/system/cpu/cpu%d/cpufreq/cpuinfo_max_freq",
                                    i
                                ), "r"
                            )
                            val line = reader.readLine()
                            if (line != null && line.isNotEmpty()) {
                                val freq = line.toInt()
                                if (freq > 0) {
                                    totalCpuFreq += freq / 1000 // Convert from kHz to MHz
                                    freqResolved++
                                }
                            }
                            reader.close()
                        } catch (ignore: Throwable) {
                            // Continue with next CPU core
                        }
                    }
                    
                    // If we got at least one valid frequency reading, break
                    if (freqResolved > 0) break
                    
                } catch (e: Exception) {
                    // Retry on next iteration
                }
            }
            
            return if (freqResolved == 0) -1 else ceil((totalCpuFreq / freqResolved.toFloat()).toDouble()).toInt()
        }

        fun invalidateCache() {
            _performanceCacheValid = false
        }
    }
}
