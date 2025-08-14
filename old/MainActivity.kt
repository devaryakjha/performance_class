package run.arya.anpec

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import java.io.RandomAccessFile
import java.util.Locale
import kotlin.math.ceil


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
                    val memoryClass = (context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager).memoryClass

                    _devicePerformanceClass = when {
                        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> { // API 31 (Android 12) and above
                            val mediaPerformanceClass = Build.VERSION.MEDIA_PERFORMANCE_CLASS
                            when {
                                mediaPerformanceClass >= 33 -> PERFORMANCE_CLASS_HIGH
                                mediaPerformanceClass >= 31 -> PERFORMANCE_CLASS_AVERAGE
                                else -> fallbackToCpuCheck(androidVersion, cpuCount, memoryClass)
                            }
                        }
                        else -> {
                            fallbackToCpuCheck(androidVersion, cpuCount, memoryClass)
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

        private fun fallbackToCpuCheck(androidVersion: Int, cpuCount: Int, memoryClass: Int): DevicePerformanceClass {
            val maxCpuFreq = readAverageMaxCpuFreq(cpuCount)
            val totalMemoryMB = memoryClass * 16 // Convert memory class to approximate MB

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

private class PerformanceClassApiImpl(val context: Context): PerformanceClassHostApi {
    override fun getPerformanceClass(): PerformanceClassResult {
        try {
            val deviceClass = DevicePerformanceClass.getDevicePerformanceClass(context)
            val performanceClass = when (deviceClass) {
                DevicePerformanceClass.PERFORMANCE_CLASS_LOW -> PerformanceClass.LOW
                DevicePerformanceClass.PERFORMANCE_CLASS_AVERAGE -> PerformanceClass.AVERAGE
                DevicePerformanceClass.PERFORMANCE_CLASS_HIGH -> PerformanceClass.HIGH
                else -> PerformanceClass.UNKNOWN
            }
            return PerformanceClassResult(performanceClass)
        } catch (e: Exception) {
            // Return LOW as safe fallback
            return PerformanceClassResult(PerformanceClass.LOW)
        }
    }
}

class MainActivity: FlutterActivity() {
    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)
        val api = PerformanceClassApiImpl(applicationContext)
        PerformanceClassHostApi.setUp(flutterEngine.dartExecutor.binaryMessenger, api)
    }
}
