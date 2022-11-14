import 'package:dio/dio.dart';
import 'package:flutter/material.dart';
import 'package:flutter_retrofit/repository/model/data.dart';
import 'package:flutter_retrofit/repository/retrofit/api_client.dart';

void main() {
  runApp(MyApp());
}

class MyApp extends StatelessWidget {
// to set the root of app.
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
  const MyHomePage({Key key=const Key("any key"), required this.title}) : super(key: key);

  final String title;

  @override
  _MyHomePageState createState() => _MyHomePageState();
}

class _MyHomePageState extends State<MyHomePage> {

  @override
  Widget build(BuildContext context) {

    return Scaffold(
      backgroundColor: Colors.white,
      appBar: AppBar(
        title: const Text("Flutter - Retrofit Implementation"),
      ),
      body: _buildBody(context),
      floatingActionButton: FloatingActionButton.extended(
        onPressed: () {

        },
        label:const Icon(Icons.cancel),
        backgroundColor: Colors.green,
      ),
    );
  }

  // build list view & manage states
  FutureBuilder<ResponseData> _buildBody(BuildContext context) {
    final client = ApiClient(Dio(BaseOptions(contentType: "application/json")));
    return FutureBuilder<ResponseData>(
      future: client.getUsers(),
      builder: (context, snapshot) {
        if (snapshot.connectionState == ConnectionState.done) {
          final ResponseData? posts = snapshot.data;
          return _buildListView(context, posts!);
        } else {
          return const Center(
            child: CircularProgressIndicator(),
          );
        }
      },
    );
  }

  // build list view & its tile
  Widget _buildListView(BuildContext context, ResponseData posts) {
    return
      ListView.builder(itemBuilder: (context,index){
        return Card(
          child: ListTile(
            leading: const Icon(Icons.account_box,color: Colors.green,size: 50,),
            title: Text(posts.data[index]['name'],style: const TextStyle(fontSize: 20),),
            subtitle: Text(posts.data[index]['email']),
          ),
        );
      },itemCount: posts.data.length,
      );
  }

}
