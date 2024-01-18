import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:observable_accessibility/common/navigation/models/bottom_tab.dart';
import 'package:observable_accessibility/main.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:clock/clock.dart';

Future<void> pumpApp(WidgetTester tester) async {
  TestWidgetsFlutterBinding.ensureInitialized();

  SharedPreferences.setMockInitialValues({});
  final sharedPreferences = await SharedPreferences.getInstance();

  await withClock(
    Clock.fixed(DateTime(2023)),
    () async {
      await tester.pumpWidget(
        App(
          sharedPreferences: sharedPreferences,
        ),
      );
    },
  );
}

Future<void> selectTab(WidgetTester tester, BottomTab tab) async {
  await tester.tap(find.text(tab.title));
  await tester.pump();
}

Future<void> enterText(
  WidgetTester tester, {
  required String textFieldKey,
  required String text,
}) async {
  await tester.enterText(
    find.byKey(Key(textFieldKey)),
    text,
  );
  await tester.pump();
}
