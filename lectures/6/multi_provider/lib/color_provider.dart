import 'dart:math';

import 'package:flutter/material.dart';

class ColorProvider extends ChangeNotifier {
  Color _color = Colors.blue;

  Color get getColor {
    return _color;
  }

  void changeColor() {
    _color = Color(
      (Random().nextDouble() * 0xFFFFFF).toInt(),
    ).withValues(alpha: 1.0);
    notifyListeners();
  }
}
