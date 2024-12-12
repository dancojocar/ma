import 'package:flutter/material.dart';

class AppointmentNameField extends StatelessWidget {
  const AppointmentNameField({
    super.key,
    required this.controller,
    this.onEditingComplete,
  });

  final TextEditingController controller;

  final VoidCallback? onEditingComplete;

  @override
  Widget build(BuildContext context) {
    return TextFormField(
      key: const Key('APPOINTMENT_NAME_TEXT_FIELD'),
      decoration: const InputDecoration(
        labelText: 'Name',
        hintText: 'Enter name',
      ),
      keyboardType: TextInputType.name,
      autofillHints: const [AutofillHints.name],
      textInputAction: TextInputAction.next,
      controller: controller,
      onEditingComplete: onEditingComplete,
    );
  }
}
