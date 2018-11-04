import 'dart:async';
import 'dart:convert';
import 'dart:io';

import 'package:flutter/material.dart';
import 'package:http/http.dart' as http;

const server = '10.0.2.2:8080';
const serverUrl = 'http://$server';
var token = '';

void auth() async {
  var data = json.encode({
    "Username": "test",
    "Password": "test1",
  });
  http.post(
    '$serverUrl/token-auth',
    body: data,
    headers: {
      HttpHeaders.acceptHeader: "application/json",
    },
  ).then((response) {
    if (response.statusCode == 200) {
      // If the call to the server was successful, parse the JSON
      token = json.decode(response.body.toString())['token'];
      print('token: $token');
    } else {
      print('response: ${response.statusCode}');
      // If that call was not successful, throw an error.
      throw Exception('Failed to authenticate!');
    }
  });
}

Future<List<Portfolio>> fetchPost() async {
  print('sending with token: $token');
  final response = await http.get(
    '$serverUrl/p',
    // Send authorization headers to your backend
    headers: {
      HttpHeaders.acceptHeader: "application/json",
      HttpHeaders.contentTypeHeader: "application/json",
      HttpHeaders.authorizationHeader: 'Bearer $token',
    },
  );

  if (response.statusCode == 200) {
    var decodedBody = json.decode(response.body);
    print('response body: $decodedBody');
    var portfolios = Portfolio.fromJsonList(decodedBody);
    print('portfolios: $portfolios');
    return portfolios;
  } else {
    print('error response: ${response.statusCode}');
    // If that call was not successful, throw an error.
    throw Exception('Failed to load portfolios');
  }
}

class Portfolio {
  final int id;
  final String name;
  final int lastModified;

  Portfolio({this.id, this.name, this.lastModified});

  factory Portfolio.fromJson(Map<String, dynamic> json) {
    return Portfolio(
      id: json['id'],
      name: json['name'],
      lastModified: json['lastModified'],
    );
  }

  static List<Portfolio> fromJsonList(List<dynamic> json) {
    List<Portfolio> portfolios = List();
    json.forEach((item) => portfolios.add(Portfolio.fromJson(item)));
    return portfolios;
  }

  @override
  String toString() {
    return 'Portfolio{id: $id, name: $name}';
  }
}

void main() {
  final portfolioService = PortfolioService();
  final PortfolioBloc portfolioBloc;

  runApp(MyApp(portfolioBloc));
}

class MyApp extends StatelessWidget {
  final PortfolioBloc portfolioBloc;

  MyApp(this.portfolioBloc);

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Fetch Data Example',
      theme: ThemeData(
        primarySwatch: Colors.blue,
      ),
      home: Scaffold(
        appBar: AppBar(
          title: Text('Fetch Data Example'),
        ),
        body: Center(
          child: Column(
            children: <Widget>[
              RaisedButton(
                child: Text('Auth'),
                onPressed: auth,
              ),
              PortfolioList(),
            ],
          ),
        ),
      ),
    );
  }
}

class PortfolioList extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return StreamBuilder<List<Portfolio>>(
        stream: portfolioBloc.items,
        initialData: portfolioBloc.items.value,
        builder: (context, snapshot) {
          switch (snapshot.connectionState) {
            case ConnectionState.none:
            case ConnectionState.waiting:
              return Center(child: CircularProgressIndicator());
            default:
              if (snapshot.hasData) {
                return ListView.builder(
                  itemCount: snapshot.data.length,
                  itemBuilder: (context, index) {
                    return Text(
                      '${snapshot.data[index].id} - ${snapshot.data[index]
                          .name}',
                      style: Theme
                          .of(context)
                          .textTheme
                          .display1,
                    );
                  },
                );
              }
              // By default, show a loading spinner
              return CircularProgressIndicator();
          }
        });
  }
}
