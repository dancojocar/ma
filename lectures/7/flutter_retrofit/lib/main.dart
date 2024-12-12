import 'package:flutter/material.dart';
import 'package:dio/dio.dart';
import 'package:flutter_retrofit/repository/model/data.dart';
import 'package:flutter_retrofit/repository/retrofit/api_client.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Flutter Demo',
      theme: ThemeData(
        primarySwatch: Colors.green,
        visualDensity: VisualDensity.adaptivePlatformDensity,
      ),
      home: const MyHomePage(title: 'API Demo Page'),
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
  FutureBuilder<ResponseData> _buildBody(BuildContext context) {
    final client = ApiClient(Dio(BaseOptions(
      contentType: Headers.jsonContentType,
      responseType: ResponseType.json,
    )));

    return FutureBuilder<ResponseData>(
      future: _fetchUsers(client),
      builder: (context, snapshot) {
        if (snapshot.connectionState == ConnectionState.done) {
          if (snapshot.hasError) {
            return Center(child: Text('Error: ${snapshot.error}'));
          }
          final ResponseData? posts = snapshot.data;
          return posts != null
              ? _buildListView(context, posts)
              : const Center(child: Text("No data found"));
        } else {
          return const Center(
            child: CircularProgressIndicator(),
          );
        }
      },
    );
  }

  Future<ResponseData> _fetchUsers(ApiClient client) async {
    try {
      return await client.getUsers();
    } on DioException catch (e) {
      throw Exception('Failed to load users: ${e.message}');
    }
  }

  Widget _buildListView(BuildContext context, ResponseData posts) {
    return ListView.builder(
      itemCount: posts.data.length,
      itemBuilder: (context, index) {
        return Card(
          child: ListTile(
            leading: const Icon(
              Icons.account_box,
              color: Colors.green,
              size: 50,
            ),
            title: Text(
              posts.data[index]['name'],
              style: const TextStyle(fontSize: 20),
            ),
            subtitle: Text(posts.data[index]['email']),
          ),
        );
      },
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Colors.white,
      appBar: AppBar(
        title: Text(widget.title),
      ),
      body: _buildBody(context),
      floatingActionButton: FloatingActionButton.extended(
        onPressed: () {},
        label: const Icon(Icons.cancel),
        backgroundColor: Colors.green,
      ),
    );
  }
}
