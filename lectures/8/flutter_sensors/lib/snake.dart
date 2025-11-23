// Copyright 2017 The Chromium Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

// ignore_for_file: public_member_api_docs

import 'dart:async';
import 'dart:math' as math;

import 'package:flutter/material.dart';
import 'package:sensors_plus/sensors_plus.dart';

class Snake extends StatefulWidget {
  Snake({super.key, this.rows = 20, this.columns = 20, this.cellSize = 10.0}) {
    assert(10 <= rows);
    assert(10 <= columns);
    assert(5.0 <= cellSize);
  }

  final int rows;
  final int columns;
  final double cellSize;

  @override
  State<StatefulWidget> createState() => SnakeState(rows, columns, cellSize);
}

class SnakeBoardPainter extends CustomPainter {
  SnakeBoardPainter(this.state, this.cellSize);

  GameState state;
  double cellSize;

  @override
  void paint(Canvas canvas, Size size) {
    final Paint blackLine = Paint()..color = Colors.black;
    final Paint blackFilled = Paint()
      ..color = Colors.black
      ..style = PaintingStyle.fill;
    final Paint redFilled = Paint()
      ..color = Colors.red
      ..style = PaintingStyle.fill;
    canvas.drawRect(
      Rect.fromPoints(Offset.zero, size.bottomLeft(Offset.zero)),
      blackLine,
    );
    for (math.Point<int> p in state.body) {
      final Offset a = Offset(cellSize * p.x, cellSize * p.y);
      final Offset b = Offset(cellSize * (p.x + 1), cellSize * (p.y + 1));

      canvas.drawRect(Rect.fromPoints(a, b), blackFilled);
    }

    final math.Point<int> food = state.food;
    final Offset fa = Offset(cellSize * food.x, cellSize * food.y);
    final Offset fb = Offset(cellSize * (food.x + 1), cellSize * (food.y + 1));
    canvas.drawRect(Rect.fromPoints(fa, fb), redFilled);
  }

  @override
  bool shouldRepaint(CustomPainter oldDelegate) {
    return true;
  }
}

class SnakeState extends State<Snake> {
  SnakeState(int rows, int columns, this.cellSize) {
    state = GameState(rows, columns);
  }

  double cellSize;
  late GameState state;
  AccelerometerEvent? acceleration;
  GyroscopeEvent? gyroscope;
  math.Point<int>? manualDirection;

  Timer? _timer;
  double _speed = 1.0; // 1.0x base speed


  @override
  Widget build(BuildContext context) {
    return CustomPaint(painter: SnakeBoardPainter(state, cellSize));
  }

  @override
  void initState() {
    super.initState();
    print('SnakeState initState called');

    accelerometerEventStream().listen((AccelerometerEvent event) {
      acceleration = event;
      // Debug: print a few samples
      // print('acc: x=${event.x}, y=${event.y}, z=${event.z}');
    });

    gyroscopeEventStream().listen((GyroscopeEvent event) {
      gyroscope = event;
      // Debug: print a few samples
      // print('gyro: x=${event.x}, y=${event.y}, z=${event.z}');
    });

    _restartTimer();
  }

  void _step() {
    // print('SnakeState _step tick, gyro=$gyroscope');
    final gyro = gyroscope;

    const double rotThreshold = 0.5; // rad/s, adjust to tune sensitivity

    math.Point<int>? newDirection;

    if (manualDirection != null) {
      // Manual direction from on-screen controls takes priority.
      newDirection = manualDirection;
      manualDirection = null; // one-shot override
    } else if (gyro != null &&
        (gyro.x.abs() > rotThreshold || gyro.y.abs() > rotThreshold)) {
      // X rotation controls up/down
      // Y rotation controls left/right
      if (gyro.x.abs() > gyro.y.abs()) {
        // More rotation around X: up/down
        newDirection = const math.Point<int>(0, -1);
      } else {
        // More rotation around Y: left/right
        newDirection = math.Point<int>(gyro.y > 0 ? 1 : -1, 0);
      }
    } else {
      // No strong rotation and no manual input – keep current direction.
      newDirection = null;
    }

    state.step(newDirection);
  }

  // Manual control methods for on-screen buttons
  void moveUp() {
    manualDirection = const math.Point<int>(0, -1);
  }

  void moveDown() {
    manualDirection = const math.Point<int>(0, 1);
  }

  void moveLeft() {
    manualDirection = const math.Point<int>(-1, 0);
  }

  void moveRight() {
    manualDirection = const math.Point<int>(1, 0);
  }

  int get foodCount => state.foodEaten;

  void updateSpeed(double newSpeed) {
    _speed = newSpeed;
    _restartTimer();
  }

  void _restartTimer() {
    _timer?.cancel();
    final double factor = _speed.clamp(0.2, 5.0);
    final int millis = (200 ~/ factor).clamp(50, 1000);
    _timer = Timer.periodic(Duration(milliseconds: millis), (_) {
      setState(() {
        _step();
      });
    });
  }

  @override
  void dispose() {
    _timer?.cancel();
    super.dispose();
  }
}

class GameState {
  GameState(this.rows, this.columns) {
    snakeLength = math.min(rows, columns) - 5;
    _spawnFood();
  }

  int rows;
  int columns;
  late int snakeLength;

  List<math.Point<int>> body = <math.Point<int>>[const math.Point<int>(0, 0)];
  math.Point<int> direction = const math.Point<int>(1, 0);

  late math.Point<int> food;
  int foodEaten = 0;
  final math.Random _random = math.Random();

  void _spawnFood() {
    math.Point<int> candidate;
    do {
      candidate = math.Point<int>(_random.nextInt(columns), _random.nextInt(rows));
    } while (body.contains(candidate));
    food = candidate;
  }

  void step(math.Point<int>? newDirection) {
    math.Point<int> next = body.last + direction;
    next = math.Point<int>(next.x % columns, next.y % rows);

    if (next == food) {
      foodEaten++;
      _spawnFood();
    }

    body.add(next);
    if (body.length > snakeLength) body.removeAt(0);
    direction = newDirection ?? direction;
  }
}
