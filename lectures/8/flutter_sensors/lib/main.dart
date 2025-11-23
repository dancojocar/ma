import 'dart:async';
import 'package:flutter/material.dart';
import 'package:sensors_plus/sensors_plus.dart';

import 'snake.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Sensors Demo',
      theme: ThemeData(primarySwatch: Colors.blue),
      home: const MyHomePage(title: 'Flutter Demo Home Page'),
    );
  }
}

class MyHomePage extends StatefulWidget {
  const MyHomePage({super.key, required this.title});

  final String title;

  @override
  _MyHomePageState createState() => _MyHomePageState();
}

class _MyHomePageState extends State<MyHomePage> {
  static const int _snakeRows = 20;
  static const int _snakeColumns = 20;
  static const double _snakeCellSize = 10.0;

  final GlobalKey<SnakeState> _snakeKey = GlobalKey<SnakeState>();

  double _speed = 1.0;

  List<double>? _accelerometerValues;
  List<double>? _userAccelerometerValues;
  List<double>? _gyroscopeValues;
  final List<StreamSubscription<dynamic>> _streamSubscriptions =
      <StreamSubscription<dynamic>>[];

  @override
  Widget build(BuildContext context) {
    final accelerometer = _accelerometerValues
        ?.map((double v) => v.toStringAsFixed(1))
        .toList();
    final gyroscope = _gyroscopeValues
        ?.map((double v) => v.toStringAsFixed(1))
        .toList();
    final userAccelerometer = _userAccelerometerValues
        ?.map((double v) => v.toStringAsFixed(1))
        .toList();

    return Scaffold(
      appBar: AppBar(title: const Text('Sensor Example')),
      body: SingleChildScrollView(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.spaceEvenly,
          children: <Widget>[
            Center(
              child: DecoratedBox(
                decoration: BoxDecoration(
                  border: Border.all(width: 1.0, color: Colors.black38),
                ),
                child: SizedBox(
                  height: _snakeRows * _snakeCellSize,
                  width: _snakeColumns * _snakeCellSize,
                  child: Snake(
                    key: _snakeKey,
                    rows: _snakeRows,
                    columns: _snakeColumns,
                    cellSize: _snakeCellSize,
                  ),
                ),
              ),
            ),
            const SizedBox(height: 8),
            Text('Food eaten: \'${_snakeKey.currentState?.foodCount ?? 0}\''),
            const SizedBox(height: 8),
            // Speed slider
            Padding(
              padding: const EdgeInsets.symmetric(horizontal: 16.0),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: <Widget>[
                  const Text('Speed'),
                  Slider(
                    min: 0.5,
                    max: 3.0,
                    divisions: 5,
                    label: _speed.toStringAsFixed(1),
                    value: _speed,
                    onChanged: (double value) {
                      setState(() {
                        _speed = value;
                      });
                      _snakeKey.currentState?.updateSpeed(value);
                    },
                  ),
                ],
              ),
            ),
            const SizedBox(height: 8),
            // On-screen directional controls
            Column(
              children: <Widget>[
                Row(
                  mainAxisAlignment: MainAxisAlignment.center,
                  children: <Widget>[
                    IconButton(
                      icon: const Icon(Icons.arrow_upward),
                      onPressed: () => _snakeKey.currentState?.moveUp(),
                    ),
                  ],
                ),
                Row(
                  mainAxisAlignment: MainAxisAlignment.center,
                  children: <Widget>[
                    IconButton(
                      icon: const Icon(Icons.arrow_back),
                      onPressed: () => _snakeKey.currentState?.moveLeft(),
                    ),
                    const SizedBox(width: 24),
                    IconButton(
                      icon: const Icon(Icons.arrow_forward),
                      onPressed: () => _snakeKey.currentState?.moveRight(),
                    ),
                  ],
                ),
                Row(
                  mainAxisAlignment: MainAxisAlignment.center,
                  children: <Widget>[
                    IconButton(
                      icon: const Icon(Icons.arrow_downward),
                      onPressed: () => _snakeKey.currentState?.moveDown(),
                    ),
                  ],
                ),
              ],
            ),
            const SizedBox(height: 16),
            Padding(
              padding: const EdgeInsets.all(16.0),
              child: Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: <Widget>[
                  Flexible(
                    child: Text(
                      'Accelerometer: $accelerometer',
                      overflow: TextOverflow.ellipsis,
                    ),
                  ),
                ],
              ),
            ),
            Padding(
              padding: const EdgeInsets.all(16.0),
              child: Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: <Widget>[
                  Flexible(
                    child: Text(
                      'UserAccelerometer: $userAccelerometer',
                      overflow: TextOverflow.ellipsis,
                    ),
                  ),
                ],
              ),
            ),
            Padding(
              padding: const EdgeInsets.all(16.0),
              child: Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: <Widget>[
                  Flexible(
                    child: Text(
                      'Gyroscope: $gyroscope',
                      overflow: TextOverflow.ellipsis,
                    ),
                  ),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }

  @override
  void dispose() {
    super.dispose();
    for (StreamSubscription<dynamic> subscription in _streamSubscriptions) {
      subscription.cancel();
    }
  }

  @override
  void initState() {
    super.initState();
    _streamSubscriptions.add(
      accelerometerEventStream().listen((AccelerometerEvent event) {
        setState(() {
          _accelerometerValues = <double>[event.x, event.y, event.z];
        });
      }),
    );
    _streamSubscriptions.add(
      gyroscopeEventStream().listen((GyroscopeEvent event) {
        setState(() {
          _gyroscopeValues = <double>[event.x, event.y, event.z];
        });
      }),
    );
    _streamSubscriptions.add(
      userAccelerometerEventStream().listen((UserAccelerometerEvent event) {
        setState(() {
          _userAccelerometerValues = <double>[event.x, event.y, event.z];
        });
      }),
    );
  }
}
