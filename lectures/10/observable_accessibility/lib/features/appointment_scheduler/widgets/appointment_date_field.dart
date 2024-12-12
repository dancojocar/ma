import 'package:flutter/material.dart';
import 'package:clock/clock.dart';

class AppointmentDateField extends StatelessWidget {
  const AppointmentDateField({
    super.key,
    required this.controller,
    required this.initialDate,
    required this.onDateSelected,
    required this.focusNode,
  });

  final TextEditingController controller;

  final FocusNode focusNode;

  final DateTime initialDate;

  final ValueChanged<DateTime> onDateSelected;

  @override
  Widget build(BuildContext context) {
    return TextFormField(
      key: const Key('APPOINTMENT_DATE_TEXT_FIELD'),
      onTap: () => _selectDate(context),
      controller: controller,
      readOnly: true,
      decoration: const InputDecoration(
        labelText: 'Date',
        hintText: 'Select date',
        suffixIcon: Icon(Icons.calendar_today),
      ),
      focusNode: focusNode,
    );
  }

  Future<void> _selectDate(BuildContext context) async {
    final picked = await showDatePicker(
      context: context,
      initialDate: initialDate,
      firstDate: initialDate,
      lastDate: clock.now().add(
            const Duration(days: 365),
          ),
    );

    if (picked != null) {
      onDateSelected(picked);
    }
  }
}
