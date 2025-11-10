import 'package:dio/dio.dart';

class AppRepository {
  late Dio dio;

  AppRepository() {
    dio = Dio();
  }
}
