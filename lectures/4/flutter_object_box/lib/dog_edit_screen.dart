
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'dog.dart';

class DogEditScreen extends StatefulWidget {
  final Dog? dog;

  const DogEditScreen({super.key, this.dog});

  @override
  DogEditScreenState createState() => DogEditScreenState();
}

class DogEditScreenState extends State<DogEditScreen> {
  final _formKey = GlobalKey<FormState>();
  late TextEditingController _nameController;
  late TextEditingController _ageController;

  @override
  void initState() {
    super.initState();
    _nameController = TextEditingController(text: widget.dog?.name ?? '');
    _ageController = TextEditingController(text: widget.dog?.age.toString() ?? '');
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text(widget.dog == null ? 'Add Dog' : 'Edit Dog'),
      ),
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
                inputFormatters: [FilteringTextInputFormatter.digitsOnly],
                validator: (value) {
                  if (value == null || value.isEmpty) {
                    return 'Please enter an age';
                  }
                  return null;
                },
              ),
              const SizedBox(height: 20),
              ElevatedButton(
                onPressed: () {
                  if (_formKey.currentState!.validate()) {
                    final dog = Dog(
                      id: widget.dog?.id ?? 0,
                      name: _nameController.text,
                      age: int.parse(_ageController.text),
                    );
                    Navigator.of(context).pop(dog);
                  }
                },
                child: const Text('Save'),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
