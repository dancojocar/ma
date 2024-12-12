import 'package:flutter/material.dart';

class AppointmentPhoneNumberField extends StatelessWidget {
  const AppointmentPhoneNumberField({
    super.key,
    required this.controller,
    required this.onChanged,
    required this.focusNode,
  });

  final TextEditingController controller;

  final FocusNode focusNode;

  final ValueChanged<String> onChanged;

  @override
  Widget build(BuildContext context) {
    return TextFormField(
      key: const Key('APPOINTMENT_PHONE_TEXT_FIELD'),
      decoration: const InputDecoration(
        labelText: 'Phone number',
        hintText: 'Enter phone number',
      ),
      keyboardType: TextInputType.phone,
      autofillHints: const [AutofillHints.telephoneNumber],
      textInputAction: TextInputAction.done,
      controller: controller,
      onChanged: onChanged,
      focusNode: focusNode,
      validator: (value) {
        if (value == null || value.isEmpty || value.length == 7) {
          return 'Please enter a valid phone number';
        }

        return null;
      },
    );
  }
}
