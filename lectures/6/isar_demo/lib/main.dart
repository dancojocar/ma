import 'package:flutter/material.dart';
import 'package:isar/isar.dart';
import 'package:path_provider/path_provider.dart';

part 'main.g.dart';

@collection
class Count {
  Id id = Isar.autoIncrement;

  int step;

  Count(this.step);
}

void main() async {
  runApp(const CounterApp());
}

class CounterApp extends StatefulWidget {
  const CounterApp({super.key});

  @override
  State<CounterApp> createState() => _CounterAppState();
}

class _CounterAppState extends State<CounterApp> {
  late Isar _isar;

  @override
  void initState() {
    super.initState();
    _openIsar();
  }

  Future<void> _openIsar() async {
    final dir = await getApplicationDocumentsDirectory();
    _isar = await Isar.open([CountSchema], directory: dir.path);
    setState(() {});
  }

  void _incrementCounter() {
    // Persist counter value to database
    _isar.writeTxnSync(() {
      _isar.counts.putSync(Count(1));
    });

    setState(() {});
  }

  @override
  Widget build(BuildContext context) {
    // This is just for demo purposes. You shouldn't perform database queries
    // in the build method.
    final count = _isar.counts.where().stepProperty().sumSync();
    final theme = ThemeData(
      colorScheme: ColorScheme.fromSeed(seedColor: Colors.cyan),
      useMaterial3: true,
    );
    return MaterialApp(
      title: 'Isar Counter',
      theme: theme,
      home: Scaffold(
        appBar: AppBar(title: const Text('Isar Counter')),
        body: Center(
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: <Widget>[
              const Text('You have pushed the button this many times:'),
              Text('$count', style: theme.textTheme.headlineMedium),
            ],
          ),
        ),
        floatingActionButton: FloatingActionButton(
          onPressed: _incrementCounter,
          child: const Icon(Icons.add),
        ),
      ),
    );
  }
}
