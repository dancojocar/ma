import 'package:flutter/material.dart';

class AppointmentTermsField extends StatelessWidget {
  const AppointmentTermsField({
    super.key,
    required this.isChecked,
    required this.onChanged,
  });

  final bool isChecked;
  final ValueChanged<bool?> onChanged;

  @override
  Widget build(BuildContext context) {
    return Row(
      children: [
        Checkbox(
          key: const Key('APPOINTMENT_TERMS_CHECKBOX'),
          value: isChecked,
          onChanged: onChanged,
          semanticLabel: 'Accept terms and conditions',
        ),
        RichText(
          text: const TextSpan(
            text: 'I accept the ',
            style: TextStyle(
              fontSize: 16.0,
              color: Colors.black,
            ),
            children: <TextSpan>[
              TextSpan(
                text: 'terms and conditions',
                style: TextStyle(
                  fontSize: 16.0,
                  color: Colors.blue,
                  decoration: TextDecoration.underline,
                ),
              ),
            ],
          ),
        ),
      ],
    );
  }
}
