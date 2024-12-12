import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import '../../../utils/widget_test_helpers.dart';

Future<void> addAppointment(WidgetTester tester) async {
  await fillOutAppointmentForm(tester);
  await tapSubmitButton(tester);
  await tester.tap(find.text('OK'));
  await tester.pumpAndSettle();
}

Future<void> submitAppointmentForm(WidgetTester tester) async {
  await fillOutAppointmentForm(tester);
  await tapSubmitButton(tester);
}

Future<void> fillOutAppointmentForm(WidgetTester tester) async {
  await enterName(tester, 'Rocoo');
  await selectDate(tester);
  await selectTime(tester);
  await enterPhoneNumber(tester);
  await tapTermsCheckbox(tester);
}

Future<void> enterName(WidgetTester tester, String name) async {
  await enterText(
    tester,
    textFieldKey: 'APPOINTMENT_NAME_TEXT_FIELD',
    text: name,
  );
  await tester.pump();
}

Future<void> selectDate(WidgetTester tester) async {
  await tester.tap(
    find.byKey(const Key('APPOINTMENT_DATE_TEXT_FIELD')),
  );
  await tester.pump();

  await tester.tap(find.text('OK'));
  await tester.pump();
}

Future<void> selectTime(WidgetTester tester) async {
  await tester.tap(
    find.byKey(const Key('APPOINTMENT_TIME_TEXT_FIELD')),
  );
  await tester.pump();

  await tester.tap(find.text('OK'));
  await tester.pump();
}

Future<void> enterPhoneNumber(WidgetTester tester) async {
  await enterText(
    tester,
    textFieldKey: 'APPOINTMENT_PHONE_TEXT_FIELD',
    text: '12345678',
  );
  await tester.pump();
}

Future<void> tapTermsCheckbox(WidgetTester tester) async {
  await tester.tap(find.byKey(const Key('APPOINTMENT_TERMS_CHECKBOX')));
  await tester.pump();
}

Future<void> tapSubmitButton(WidgetTester tester) async {
  await tester.tap(find.text('Book slot!'));
  await tester.pumpAndSettle();
}

Future<void> tapDismissButton(WidgetTester tester) async {
  await tester.tap(find.text('OK'));
  await tester.pump();
}
