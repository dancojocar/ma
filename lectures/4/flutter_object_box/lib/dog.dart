import 'package:objectbox/objectbox.dart';

@Entity()
class Dog {
  int id; // ObjectBox manages the id automatically
  String name;
  int age;

  Dog({
    this.id = 0,
    required this.name,
    required this.age,
  });
}
