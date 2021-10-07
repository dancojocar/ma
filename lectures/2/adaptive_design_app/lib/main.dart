import 'package:flutter/material.dart';
import 'package:adaptive_design_app/master_detail_page.dart';

void main() => runApp(const MyApp());

class MyApp extends StatelessWidget {
  const MyApp({Key? key}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Adaptive List Demo',
      theme: ThemeData(
        primarySwatch: Colors.blue,
      ),
      home: const MasterDetailPage(),
      debugShowCheckedModeBanner: false,
    );
  }
}