import 'dart:async';
import 'dart:io';
import 'package:flutter/material.dart';
import 'package:path_provider/path_provider.dart'; // For locating directories
import 'dog.dart';
import 'objectbox.g.dart'; // Replace with the actual generated file for ObjectBox

late final Store store;
late final Box<Dog> dogBox;

void main() async {
  WidgetsFlutterBinding.ensureInitialized();
  // Get the app's documents directory
  final appDocDir = await getApplicationDocumentsDirectory();
  // Initialize ObjectBox with the documents directory path
  store = await openStore(directory: '${appDocDir.path}/objectbox');
  dogBox = store.box<Dog>();

  runApp(const MyApp());
}

Future<void> insertDog(Dog dog) async {
  dogBox.put(dog);
}

Future<List<Dog>> getDogs() async {
  return dogBox.getAll();
}

Future<void> updateDog(Dog dog) async {
  dogBox.put(dog);
}

Future<void> deleteDog(int id) async {
  dogBox.remove(id);
}

class MyApp extends StatefulWidget {
  const MyApp({super.key});

  @override
  State<MyApp> createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  final _formKey = GlobalKey<FormState>();
  final _nameController = TextEditingController();
  final _ageController = TextEditingController();

  Future<void> _addDog(BuildContext context) async {
    if (_formKey.currentState!.validate()) {
      final newDog = Dog(
        name: _nameController.text,
        age: int.parse(_ageController.text),
      );
      insertDog(newDog);

      WidgetsBinding.instance.addPostFrameCallback((_) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text('Added dog: ${newDog.name}')),
        );
      });

      _nameController.clear();
      _ageController.clear();
      setState(() {});
    }
  }

  Future<List<Dog>> _fetchDogs() async {
    return getDogs();
  }

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
                      onPressed: () => _addDog(context),
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
