
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:next_gen_ui/title_screen/title_screen_ui.dart';

void main() {
  testWidgets('TitleScreenUi has no overflow errors', (WidgetTester tester) async {
    await tester.pumpWidget(
      MaterialApp(
        home: Scaffold(
          body: TitleScreenUi(
            difficulty: 1,
            onDifficultyFocused: (_) {},
            onDifficultyPressed: (_) {},
            onStartPressed: () {},
          ),
        ),
      ),
    );
    await tester.pumpAndSettle();

    final overflowErrors = tester.takeException();
    expect(overflowErrors, isNull);
  });
}
