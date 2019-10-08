import 'package:http/http.dart' as http;
import 'dart:convert';
import 'dart:io';
import 'package:flutter_jwt/portfolio.dart';

class PortfolioService {
  final serverUrl = 'http://10.0.2.2:8080';

  Future<String> auth() async {
    var data = json.encode({
      "Username": "test",
      "Password": "test1", 
    });
    var response = await http.post(
      '$serverUrl/token-auth',
      body: data,
      headers: {
        HttpHeaders.acceptHeader: "application/json",
      },
    );
    if (response!=null) {
      if (response.statusCode == 200) {
        // If the call to the server was successful, parse the JSON
        var token = json.decode(response.body.toString())['token'];
        print('token: $token');
        return token;
      } else {
        print('response: ${response.statusCode}');
        // If that call was not successful, throw an error.
        throw Exception('Failed to authenticate!');
      }
    }
    return null;
  }

  Future<List<Portfolio>> fetchPost(var token) async {
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
}
