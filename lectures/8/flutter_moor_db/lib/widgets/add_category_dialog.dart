import 'package:flutter/material.dart';
import 'package:flutter_moor_db/bloc.dart';
import 'package:provider/provider.dart';

class AddCategoryDialog extends StatefulWidget {
  const AddCategoryDialog({super.key});

  @override
  _AddCategoryDialogState createState() => _AddCategoryDialogState();
}

class _AddCategoryDialogState extends State<AddCategoryDialog> {
  final TextEditingController _controller = TextEditingController();

  @override
  Widget build(BuildContext context) {
    return Dialog(
      child: Padding(
        padding: const EdgeInsets.all(8),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            Padding(
              padding: const EdgeInsets.all(8),
              child: Text(
                'Add a category',
                style: Theme.of(context).textTheme.bodyMedium,
              ),
            ),
            TextField(
              controller: _controller,
              autofocus: true,
              decoration: const InputDecoration(
                labelText: 'Name of the category',
              ),
              onSubmitted: (_) => _addEntry(),
            ),
            OverflowBar(
              children: [
                TextButton(
                  style: ButtonStyle(
                    foregroundColor: WidgetStateProperty.all(
                      Theme.of(context).colorScheme.secondary,
                    ),
                  ),
                  onPressed: _addEntry,
                  child: const Text('Add'),
                ),
              ],
            ),
          ],
        ),
      ),
    );
  }

  void _addEntry() {
    if (_controller.text.isNotEmpty) {
      Provider.of<TodoAppBloc>(context, listen: false)
          .addCategory(_controller.text);
      Navigator.of(context).pop();
    }
  }
}
