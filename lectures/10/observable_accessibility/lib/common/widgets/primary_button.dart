import 'package:flutter/material.dart';

class PrimaryButton extends StatelessWidget {
  const PrimaryButton({
    super.key,
    this.onPressed,
    this.focusNode,
    required this.text,
  });

  final VoidCallback? onPressed;

  final String text;

  final FocusNode? focusNode;

  @override
  Widget build(BuildContext context) {
    return ElevatedButton(
      onPressed: onPressed,
      focusNode: focusNode,
      child: Text(text),
    );
  }
}
