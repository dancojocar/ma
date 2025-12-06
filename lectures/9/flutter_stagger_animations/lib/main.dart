import 'dart:async';
import 'package:flutter/material.dart';
import 'package:flutter/scheduler.dart' show timeDilation;

class StaggerAnimation extends StatelessWidget {
  StaggerAnimation({super.key, required this.controller})
    : opacity = Tween<double>(begin: 0.0, end: 1.0).animate(
        CurvedAnimation(
          parent: controller,
          curve: const Interval(0.0, 0.100, curve: Curves.ease),
        ),
      ),
      width = Tween<double>(begin: 50.0, end: 150.0).animate(
        CurvedAnimation(
          parent: controller,
          curve: const Interval(0.125, 0.250, curve: Curves.ease),
        ),
      ),
      height = Tween<double>(begin: 50.0, end: 150.0).animate(
        CurvedAnimation(
          parent: controller,
          curve: const Interval(0.250, 0.375, curve: Curves.ease),
        ),
      ),
      padding =
          EdgeInsetsTween(
            begin: const EdgeInsets.only(bottom: 16.0),
            end: const EdgeInsets.only(bottom: 75.0),
          ).animate(
            CurvedAnimation(
              parent: controller,
              curve: const Interval(0.250, 0.375, curve: Curves.ease),
            ),
          ),
      borderRadius =
          BorderRadiusTween(
            begin: BorderRadius.circular(4.0),
            end: BorderRadius.circular(75.0),
          ).animate(
            CurvedAnimation(
              parent: controller,
              curve: const Interval(0.375, 0.500, curve: Curves.ease),
            ),
          ),
      color = ColorTween(begin: Colors.indigo[100], end: Colors.orange[400])
          .animate(
            CurvedAnimation(
              parent: controller,
              curve: const Interval(0.500, 0.750, curve: Curves.ease),
            ),
          );

  final Animation<double> controller;
  final Animation<double> opacity;
  final Animation<double> width;
  final Animation<double> height;
  final Animation<EdgeInsets> padding;
  final Animation<BorderRadius?> borderRadius;
  final Animation<Color?> color;

  Widget _buildAnimation(BuildContext context, Widget? child) {
    return Container(
      padding: padding.value,
      alignment: Alignment.bottomCenter,
      child: Opacity(
        opacity: opacity.value,
        child: Container(
          width: width.value,
          height: height.value,
          decoration: BoxDecoration(
            color: color.value,
            border: Border.all(color: Colors.indigo, width: 3.0),
            borderRadius: borderRadius.value,
          ),
        ),
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    return AnimatedBuilder(builder: _buildAnimation, animation: controller);
  }
}

class StaggerDemo extends StatefulWidget {
  const StaggerDemo({super.key});

  @override
  _StaggerDemoState createState() => _StaggerDemoState();
}

class _StaggerDemoState extends State<StaggerDemo>
    with TickerProviderStateMixin {
  late AnimationController _controller;
  double _playbackSpeed = 1.0; // Default speed (1x)

  @override
  void initState() {
    super.initState();
    _controller = AnimationController(
      duration: const Duration(milliseconds: 2000),
      vsync: this,
    );
  }

  @override
  void dispose() {
    _controller.dispose();
    super.dispose();
  }

  Future<void> _playAnimation() async {
    try {
      await _controller.forward().orCancel;
    } on TickerCanceled {
      // Animation canceled
    }
  }

  Future<void> _reverseAnimation() async {
    try {
      await _controller.reverse().orCancel;
    } on TickerCanceled {
      // Animation canceled
    }
  }

  @override
  Widget build(BuildContext context) {
    // Dynamic time dilation based on slider
    timeDilation = _playbackSpeed;

    return Scaffold(
      appBar: AppBar(title: const Text('Staggered Animation Demo')),
      body: Column(
        children: [
          // 1. The Animation Area
          Expanded(
            child: Center(
              child: Container(
                width: 300.0,
                height: 300.0,
                decoration: BoxDecoration(
                  color: Colors.black.withOpacity(0.1),
                  border: Border.all(color: Colors.black.withOpacity(0.5)),
                ),
                child: StaggerAnimation(controller: _controller.view),
              ),
            ),
          ),

          // 2. The Instructor Controls
          Container(
            padding: const EdgeInsets.all(16.0),
            color: Colors.grey[200],
            child: Column(
              children: [
                // Status Indicator
                AnimatedBuilder(
                  animation: _controller,
                  builder: (context, child) => Text(
                    "Status: ${_controller.status.name.toUpperCase()}  |  Value: ${_controller.value.toStringAsFixed(2)}",
                    style: const TextStyle(fontWeight: FontWeight.bold),
                  ),
                ),
                const SizedBox(height: 10),

                // Speed Slider
                Row(
                  children: [
                    const Text("Speed:"),
                    Expanded(
                      child: Slider(
                        value: _playbackSpeed,
                        min: 1.0,
                        max: 20.0,
                        divisions: 19,
                        label: "${_playbackSpeed.toInt()}x Slower",
                        onChanged: (val) {
                          setState(() {
                            _playbackSpeed = val;
                          });
                        },
                      ),
                    ),
                    Text("${_playbackSpeed.toInt()}x Slower"),
                  ],
                ),

                // Playback Buttons
                Row(
                  mainAxisAlignment: MainAxisAlignment.spaceEvenly,
                  children: [
                    ElevatedButton.icon(
                      onPressed: _playAnimation,
                      icon: const Icon(Icons.play_arrow),
                      label: const Text("Play"),
                    ),
                    ElevatedButton.icon(
                      onPressed: _reverseAnimation,
                      icon: const Icon(Icons.replay),
                      label: const Text("Reverse"),
                    ),
                    OutlinedButton(
                      onPressed: () => _controller.reset(),
                      child: const Text("Reset"),
                    ),
                  ],
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }
}

void main() {
  runApp(const MaterialApp(home: StaggerDemo()));
}
