import 'package:flutter/material.dart';

class AppointmentNameField extends StatelessWidget {
  const AppointmentNameField({
    Key? key,
    required this.controller,
    this.onEditingComplete,
  }) : super(key: key);

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
