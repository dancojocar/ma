import 'dart:async';

import 'package:flutter/material.dart';
import 'package:path/path.dart';
import 'package:sqflite/sqflite.dart';

void main() async {
  WidgetsFlutterBinding.ensureInitialized();
  final database = openDatabase(
    join(await getDatabasesPath(), 'dogs_database.db'),
    onCreate: (db, version) {
      return db.execute(
        'CREATE TABLE dogs(id INTEGER PRIMARY KEY, name TEXT, age INTEGER)',
      );
    },
    version: 1,
  );

  Future<void> insertDog(Dog dog) async {
    final db = await database;
    await db.insert(
      'dogs',
      dog.toMap(),
      conflictAlgorithm: ConflictAlgorithm.replace,
    );
  }

  Future<List<Dog>> dogs() async {
    final db = await database;
    final List<Map<String, dynamic>> maps = await db.query('dogs');
    return List.generate(maps.length, (i) {
      return Dog(
        id: maps[i]['id'] as int,
        name: maps[i]['name'] as String,
        age: maps[i]['age'] as int,
      );
    });
  }

  Future<void> updateDog(Dog dog) async {
    final db = await database;
    await db.update(
      'dogs',
      dog.toMap(),
      where: 'id = ?',
      whereArgs: [dog.id],
    );
  }

  Future<void> deleteDog(int id) async {
    final db = await database;
    await db.delete(
      'dogs',
      where: 'id = ?',
      whereArgs: [id],
    );
  }

  var fido = const Dog(
    id: 0,
    name: 'Fido',
    age: 35,
  );

  await insertDog(fido);

  print(await dogs()); // Prints a list that include Fido.

  fido = Dog(
    id: fido.id,
    name: fido.name,
    age: fido.age + 7,
  );
  await updateDog(fido);

  print(await dogs()); // Prints Fido with age 42.

  await deleteDog(fido.id);

  await insertDog(fido);
  print(await dogs());

  runApp(MyApp(database));
}

class MyApp extends StatefulWidget {
  final Future<Database> database;

  const MyApp(this.database, {Key? key}) : super(key: key);

  @override
  State<MyApp> createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  final _formKey = GlobalKey<FormState>();
  final _nameController = TextEditingController();
  final _ageController = TextEditingController();

  Future<void> _addDog(BuildContext context) async {
    if (_formKey.currentState!.validate()) {
      final db = await widget.database;
      final id = await db.insert('dogs', {
        'name': _nameController.text,
        'age': int.parse(_ageController.text),
      });
      final newDog = Dog(
        id: id,
        name: _nameController.text,
        age: int.parse(_ageController.text),
      );
      WidgetsBinding.instance.addPostFrameCallback((_) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text('Added dog: ${newDog.name}')),
        );
      });
      _nameController.clear();
      _ageController.clear();
      setState(() {}); // Refresh the UI
    }
  }

  Future<List<Dog>> _fetchDogs() => dogs(widget.database);

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Dog List'),
        ),
        body: Column(
          children: [
            Form(
              key: _formKey,
              child: Padding(
                padding: const EdgeInsets.all(8.0),
                child: Column(
                  children: [
                    TextFormField(
                      controller: _nameController,
                      decoration: const InputDecoration(labelText: 'Dog Name'),
                      validator: (value) {
                        if (value == null || value.isEmpty) {
                          return 'Please enter a name';
                        }
                        return null;
                      },
                    ),
                    TextFormField(
                      controller: _ageController,
                      decoration: const InputDecoration(labelText: 'Dog Age'),
                      keyboardType: TextInputType.number,
                      validator: (value) {
                        if (value == null || value.isEmpty) {
                          return 'Please enter an age';
                        }
                        return null;
                      },
                    ),
                    ElevatedButton(
                      onPressed: () => {
                        setState(() {
                          _addDog(context);
                        })
                      },
                      child: const Text('Add Dog'),
                    ),
                  ],
                ),
              ),
            ),
            Expanded(
              child: FutureBuilder<List<Dog>>(
                future: _fetchDogs(),
                builder: (context, snapshot) {
                  if (snapshot.hasData) {
                    return ListView.builder(
                      itemCount: snapshot.data!.length,
                      itemBuilder: (context, index) {
                        return ListTile(
                          title: Text(snapshot.data![index].name),
                          subtitle: Text('Age: ${snapshot.data![index].age}'),
                        );
                      },
                    );
                  } else if (snapshot.hasError) {
                    return Center(child: Text('Error: ${snapshot.error}'));
                  } else {
                    return const Center(child: CircularProgressIndicator());
                  }
                },
              ),
            ),
          ],
        ),
      ),
    );
  }
}

Future<List<Dog>> dogs(Future<Database> database) async {
  final db = await database;
  final List<Map<String, dynamic>> maps = await db.query('dogs');
  return List.generate(maps.length, (i) {
    return Dog(
      id: maps[i]['id'] as int,
      name: maps[i]['name'] as String,
      age: maps[i]['age'] as int,
    );
  });
}

class Dog {
  final int id;
  final String name;
  final int age;

  const Dog({
    required this.id,
    required this.name,
    required this.age,
  });

  Map<String, dynamic> toMap() {
    return {
      'id': id,
      'name': name,
      'age': age,
    };
  }

  @override
  String toString() {
    return 'Dog{id: $id, name: $name, age: $age}';
  }
}
