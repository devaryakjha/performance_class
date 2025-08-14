import 'package:flutter_test/flutter_test.dart';
import 'package:performance_class/performance_class.dart';
import 'package:performance_class/performance_class_platform_interface.dart';
import 'package:performance_class/performance_class_method_channel.dart';
import 'package:plugin_platform_interface/plugin_platform_interface.dart';

class MockPerformanceClassPlatform
    with MockPlatformInterfaceMixin
    implements PerformanceClassPlatform {

  @override
  Future<String?> getPlatformVersion() => Future.value('42');
}

void main() {
  final PerformanceClassPlatform initialPlatform = PerformanceClassPlatform.instance;

  test('$MethodChannelPerformanceClass is the default instance', () {
    expect(initialPlatform, isInstanceOf<MethodChannelPerformanceClass>());
  });

  test('getPlatformVersion', () async {
    PerformanceClass performanceClassPlugin = PerformanceClass();
    MockPerformanceClassPlatform fakePlatform = MockPerformanceClassPlatform();
    PerformanceClassPlatform.instance = fakePlatform;

    expect(await performanceClassPlugin.getPlatformVersion(), '42');
  });
}
