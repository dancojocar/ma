import 'package:flutter/material.dart';
import 'package:adaptive_design_app/detail_widget.dart';
class DetailPage extends StatefulWidget {

  final int data;

  const DetailPage(this.data, {Key? key}) : super(key: key);

  @override
  _DetailPageState createState() => _DetailPageState();
}

class _DetailPageState extends State<DetailPage> {
  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(),
      body: DetailWidget(widget.data),
    );
  }
}