import 'package:flutter/material.dart';
import 'ui/home_screen.dart';

void main() => runApp(const MyApp());

class MyApp extends StatelessWidget {
  const MyApp({Key? key}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      debugShowCheckedModeBanner: false,
      theme: ThemeData(
          brightness: Brightness.dark,
          primaryColor: const Color(0xff3A3B56),
          cardColor: const Color(0xff40415D),
          scaffoldBackgroundColor: const Color(0xff3A3B56)),
      home: const HomeScreen(),
    );
  }
}