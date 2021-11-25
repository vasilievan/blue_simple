import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:blue_simple/blue_simple.dart';

void main() {
  const MethodChannel channel = MethodChannel('blue_simple');

  TestWidgetsFlutterBinding.ensureInitialized();

  setUp(() {
    channel.setMockMethodCallHandler((MethodCall methodCall) async {
      return '42';
    });
  });

  tearDown(() {
    channel.setMockMethodCallHandler(null);
  });

  test('getPlatformVersion', () async {
    expect(await BlueSimple.platformVersion, '42');
  });
}
