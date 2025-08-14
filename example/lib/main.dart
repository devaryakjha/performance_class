import 'package:flutter/material.dart';

import 'package:anpec/anpec.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatefulWidget {
  const MyApp({super.key});

  @override
  State<MyApp> createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  Map<String, dynamic>? deviceInfo;
  PerformanceClass? performanceClass;
  int? cpuFreq;
  bool isLoading = true;
  String? error;

  @override
  void initState() {
    super.initState();
    _loadDeviceInfo();
  }

  Future<void> _loadDeviceInfo() async {
    try {
      setState(() {
        isLoading = true;
        error = null;
      });

      final classifier = PerformanceClassifier.instance;

      // Get detailed device information
      deviceInfo = await classifier.getDetailedDeviceInfo();

      // Get performance class enum directly
      performanceClass = await classifier.getPerformanceClass();

      // Get CPU frequency
      cpuFreq = await classifier.readAverageMaxCpuFreq(deviceInfo!['cpuCount']);

      setState(() {
        isLoading = false;
      });
    } catch (e) {
      setState(() {
        isLoading = false;
        error = e.toString();
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    const textStyle = TextStyle(fontSize: 16);
    const titleStyle = TextStyle(fontSize: 18, fontWeight: FontWeight.bold);
    const spacerSmall = SizedBox(height: 10);
    const spacerMedium = SizedBox(height: 20);

    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Performance Class Plugin'),
          backgroundColor: Colors.blue,
          foregroundColor: Colors.white,
          actions: [
            IconButton(
              icon: const Icon(Icons.refresh),
              onPressed: _loadDeviceInfo,
            ),
          ],
        ),
        body: isLoading
            ? const Center(child: CircularProgressIndicator())
            : error != null
            ? Center(
                child: Column(
                  mainAxisAlignment: MainAxisAlignment.center,
                  children: [
                    const Icon(Icons.error, size: 64, color: Colors.red),
                    const SizedBox(height: 16),
                    Text('Error: $error', style: textStyle),
                    const SizedBox(height: 16),
                    ElevatedButton(
                      onPressed: _loadDeviceInfo,
                      child: const Text('Retry'),
                    ),
                  ],
                ),
              )
            : SingleChildScrollView(
                child: Container(
                  padding: const EdgeInsets.all(16),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      const Text(
                        'Android Device Performance Classification',
                        style: TextStyle(
                          fontSize: 24,
                          fontWeight: FontWeight.bold,
                        ),
                      ),
                      spacerMedium,

                      // Android-only warning
                      Container(
                        padding: const EdgeInsets.all(16),
                        decoration: BoxDecoration(
                          color: Colors.orange.shade50,
                          borderRadius: BorderRadius.circular(8),
                          border: Border.all(color: Colors.orange.shade200),
                        ),
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            Row(
                              children: [
                                Icon(
                                  Icons.warning,
                                  color: Colors.orange.shade700,
                                ),
                                const SizedBox(width: 8),
                                const Text(
                                  'Android Only',
                                  style: TextStyle(
                                    fontSize: 18,
                                    fontWeight: FontWeight.bold,
                                    color: Colors.orange,
                                  ),
                                ),
                              ],
                            ),
                            spacerSmall,
                            const Text(
                              'This plugin is designed specifically for Android devices only. '
                              'While it may run on other platforms, it will not provide accurate '
                              'performance classification.',
                              style: TextStyle(fontSize: 14),
                            ),
                          ],
                        ),
                      ),
                      spacerMedium,

                      const Text(
                        'This plugin analyzes your Android device\'s hardware and software capabilities '
                        'to classify it into a performance category. The classification is based on factors '
                        'like CPU cores, memory, Android version, and media performance class.',
                        style: textStyle,
                      ),
                      spacerMedium,

                      // Performance Class Section
                      Container(
                        padding: const EdgeInsets.all(16),
                        decoration: BoxDecoration(
                          color: Colors.blue.shade50,
                          borderRadius: BorderRadius.circular(8),
                          border: Border.all(color: Colors.blue.shade200),
                        ),
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            const Text(
                              'Performance Classification',
                              style: titleStyle,
                            ),
                            spacerSmall,
                            Text(
                              'Class: ${performanceClass?.name ?? 'Unknown'}',
                              style: textStyle,
                            ),
                            Text(
                              'Display Name: ${deviceInfo?['performanceClass'] ?? 'Unknown'}',
                              style: textStyle,
                            ),
                            Text(
                              'Raw Value: ${deviceInfo?['performanceClassRaw'] ?? 'Unknown'}',
                              style: textStyle,
                            ),
                            Text(
                              'Is High-End: ${deviceInfo?['isHighEnd'] ?? 'Unknown'}',
                              style: textStyle,
                            ),
                            Text(
                              'Is Low-End: ${deviceInfo?['isLowEnd'] ?? 'Unknown'}',
                              style: textStyle,
                            ),
                          ],
                        ),
                      ),
                      spacerMedium,

                      // Device Information Section
                      Container(
                        padding: const EdgeInsets.all(16),
                        decoration: BoxDecoration(
                          color: Colors.green.shade50,
                          borderRadius: BorderRadius.circular(8),
                          border: Border.all(color: Colors.green.shade200),
                        ),
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            const Text('Device Information', style: titleStyle),
                            spacerSmall,
                            Text(
                              'Android Version: ${deviceInfo?['androidVersion'] ?? 'Unknown'}',
                              style: textStyle,
                            ),
                            Text(
                              'CPU Cores: ${deviceInfo?['cpuCount'] ?? 'Unknown'}',
                              style: textStyle,
                            ),
                            Text(
                              'Memory: ${deviceInfo?['memoryClassMb'] ?? 'Unknown'} MB',
                              style: textStyle,
                            ),
                            Text(
                              'Max CPU Frequency: ${deviceInfo?['maxCpuFreqMhz'] ?? 'Unknown'} MHz',
                              style: textStyle,
                            ),
                            Text(
                              'Media Performance Class: ${deviceInfo?['mediaPerformanceClass'] ?? 'Unknown'}',
                              style: textStyle,
                            ),
                            Text(
                              'Read CPU Frequency: ${cpuFreq == null || cpuFreq == -1 ? 'Unable to read' : '$cpuFreq MHz'}',
                              style: textStyle,
                            ),
                          ],
                        ),
                      ),
                    ],
                  ),
                ),
              ),
      ),
    );
  }
}
