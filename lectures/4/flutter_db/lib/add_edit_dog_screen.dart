import 'package:flutter/material.dart';
import 'main.dart';
import 'package:flutter/services.dart';
import 'package:sqflite/sqflite.dart';

class AddEditDogScreen extends StatefulWidget {
  final Future<Database> database;
  final Dog? dog;

  const AddEditDogScreen({Key? key, required this.database, this.dog})
    : super(key: key);

  @override
  _AddEditDogScreenState createState() => _AddEditDogScreenState();
}

class _AddEditDogScreenState extends State<AddEditDogScreen> {
  final _formKey = GlobalKey<FormState>();
  late TextEditingController _nameController;
  late TextEditingController _ageController;

  @override
  void initState() {
    super.initState();
    _nameController = TextEditingController(text: widget.dog?.name ?? '');
    _ageController = TextEditingController(
      text: widget.dog?.age.toString() ?? '',
    );
  }

  Future<void> _saveDog() async {
    if (_formKey.currentState!.validate()) {
      final db = await widget.database;
      final name = _nameController.text;
      final age = int.parse(_ageController.text);

      if (widget.dog == null) {
        // Add new dog
        final newDog = Dog(name: name, age: age);
        await db.insert(
          'dogs',
          newDog.toMap(),
          conflictAlgorithm: ConflictAlgorithm.replace,
        );
      } else {
        // Update existing dog
        final updatedDog = Dog(id: widget.dog!.id, name: name, age: age);
        await db.update(
          'dogs',
          updatedDog.toMap(),
          where: 'id = ?',
          whereArgs: [updatedDog.id],
        );
      }
      Navigator.of(context).pop();
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: Text(widget.dog == null ? 'Add Dog' : 'Edit Dog')),
      body: Padding(
        padding: const EdgeInsets.all(16.0),
        child: Form(
          key: _formKey,
          child: Column(
            children: [
              TextFormField(
                controller: _nameController,
                decoration: const InputDecoration(labelText: 'Name'),
                validator: (value) {
                  if (value == null || value.isEmpty) {
                    return 'Please enter a name';
                  }
                  return null;
                },
              ),
              TextFormField(
                controller: _ageController,
                decoration: const InputDecoration(labelText: 'Age'),
                keyboardType: TextInputType.number,
                inputFormatters: <TextInputFormatter>[
                  FilteringTextInputFormatter.digitsOnly,
                ],
                validator: (value) {
                  if (value == null || value.isEmpty) {
                    return 'Please enter an age';
                  }
                  return null;
                },
              ),
              const SizedBox(height: 16),
              ElevatedButton(onPressed: _saveDog, child: const Text('Save')),
            ],
          ),
        ),
      ),
    );
  }
}
