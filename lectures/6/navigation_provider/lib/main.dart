import 'package:flutter/material.dart';
import 'package:navigation_provider/details.dart';
import 'package:navigation_provider/home.dart';
import 'package:provider/provider.dart';
import 'package:navigation_provider/counter.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MultiProvider(
      providers: [
        ChangeNotifierProvider.value(
          value: Counter(),
        ),
      ],
      child: MaterialApp(
        title: 'Flutter Demo',
        theme: ThemeData(
          primarySwatch: Colors.blue,
          visualDensity: VisualDensity.adaptivePlatformDensity,
        ),
        initialRoute: '/',
        routes: {
          '/': (context) => const MyHomePage(title: "Provider Pattern"),
          '/second': (context) => const DetailScreen(),
        },
      ),
    );
  }
}
