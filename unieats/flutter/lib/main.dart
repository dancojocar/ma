import 'dart:async';
import 'dart:ui';

import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:material_ui/material_ui.dart';

import 'app_router.dart';
import 'providers/providers.dart';
import 'services/crash_reporter.dart';

void main() {
  final crashReporter = ConsentGatedCrashReporter(const LoggingCrashReporter());

  runZonedGuarded(() {
    // Inside the zone, so the binding and runApp share the zone that
    // catches uncaught async errors.
    WidgetsFlutterBinding.ensureInitialized();
    FlutterError.onError = (details) {
      FlutterError.presentError(details);
      crashReporter.recordError(
        details.exception,
        details.stack ?? StackTrace.current,
      );
    };
    PlatformDispatcher.instance.onError = (error, stack) {
      crashReporter.recordError(error, stack, fatal: true);
      return true;
    };
    runApp(
      ProviderScope(
        retry: (_, _) => null,
        overrides: [crashReporterProvider.overrideWithValue(crashReporter)],
        child: const UniEatsApp(),
      ),
    );
  }, (error, stack) => crashReporter.recordError(error, stack, fatal: true));
}

class UniEatsApp extends ConsumerWidget {
  const UniEatsApp({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final router = ref.watch(routerProvider);
    ref.watch(outboxSyncProvider);

    return MaterialApp.router(
      title: 'UniEats',
      debugShowCheckedModeBanner: false,
      theme: ThemeData(
        colorScheme: ColorScheme.fromSeed(
          seedColor: const Color(0xFFE65100),
          brightness: Brightness.light,
        ),
        useMaterial3: true,
        inputDecorationTheme: const InputDecorationTheme(
          border: OutlineInputBorder(),
          filled: true,
        ),
        cardTheme: CardThemeData(
          elevation: 2,
          shape: RoundedRectangleBorder(
            borderRadius: BorderRadius.circular(12),
          ),
        ),
      ),
      routerConfig: router,
    );
  }
}
