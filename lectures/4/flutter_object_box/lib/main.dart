import 'dart:async';
import 'package:flutter/material.dart';
import 'package:path_provider/path_provider.dart';
import 'objectbox.g.dart';
import 'dog.dart';
import 'dog_edit_screen.dart';

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
  List<Dog> _dogs = [];

  @override
  void initState() {
    super.initState();
    _refreshDogs();
  }

  void _refreshDogs() async {
    final dogs = await getDogs();
    setState(() {
      _dogs = dogs;
    });
  }

  void _navigateToScreen(BuildContext context, {Dog? dog}) async {
    final result = await Navigator.push(
      context,
      MaterialPageRoute(builder: (context) => DogEditScreen(dog: dog)),
    );

    if (result != null) {
      if (dog != null) {
        await updateDog(result as Dog);
      } else {
        await insertDog(result as Dog);
      }
      _refreshDogs();
    }
  }

  void _deleteDog(int id) async {
    await deleteDog(id);
    _refreshDogs();
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Builder(
        builder: (context) => Scaffold(
          appBar: AppBar(title: const Text('Dog List')),
          body: ListView.builder(
            itemCount: _dogs.length,
            itemBuilder: (context, index) {
              final dog = _dogs[index];
              return Dismissible(
                key: Key(dog.id.toString()),
                direction: DismissDirection.endToStart,
                onDismissed: (_) {
                  setState(() {
                    _dogs.removeAt(index);
                  });
                  _deleteDog(dog.id);
                },
                background: Container(
                  color: Colors.red,
                  alignment: Alignment.centerRight,
                  padding: const EdgeInsets.symmetric(horizontal: 20.0),
                  child: const Icon(Icons.delete, color: Colors.white),
                ),
                child: ListTile(
                  title: Text(dog.name),
                  subtitle: Text('Age: ${dog.age}'),
                  onTap: () => _navigateToScreen(context, dog: dog),
                ),
              );
            },
          ),
          floatingActionButton: FloatingActionButton(
            onPressed: () => _navigateToScreen(context),
            child: const Icon(Icons.add),
          ),
        ),
      ),
    );
  }
}
