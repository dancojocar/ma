import 'package:accessibility_tools/accessibility_tools.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:observable_accessibility/common/navigation/cubit/navigation_cubit.dart';
import 'package:observable_accessibility/common/navigation/pages/main_page.dart';
import 'package:observable_accessibility/common/services/appointment_service.dart';
import 'package:observable_accessibility/features/appointment_list/cubit/appointment_list_cubit.dart';
import 'package:observable_accessibility/features/appointment_scheduler/cubit/appointment_cubit.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:provider/provider.dart';

void main() async {
  WidgetsFlutterBinding.ensureInitialized();

  final sharedPreferences = await SharedPreferences.getInstance();

  runApp(
    App(
      sharedPreferences: sharedPreferences,
      enableAccessibilityTools: true,
    ),
  );
}

class App extends StatelessWidget {
  const App({
    super.key,
    this.enableAccessibilityTools = false,
    required this.sharedPreferences,
  });

  final bool enableAccessibilityTools;

  final SharedPreferences sharedPreferences;

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      debugShowCheckedModeBanner: false,
      builder: (context, child) =>
          enableAccessibilityTools ? AccessibilityTools(child: child) : child!,
      home: MultiProvider(
        providers: [
          Provider<AppointmentService>(
            create: (context) => AppointmentService(
              sharedPreferences: sharedPreferences,
            ),
          ),
        ],
        child: MultiBlocProvider(
          providers: [
            BlocProvider(
              create: (context) => AppointmentListCubit(
                appointmentService: context.read<AppointmentService>(),
              )..subscribeToAppointments(),
              // We want it to subscribe to appointments right away so we use
              // lazy here.
              lazy: false,
            ),
            BlocProvider(
              create: (context) => AppointmentCubit(
                appointmentService: context.read<AppointmentService>(),
              ),
            ),
            BlocProvider(
              create: (context) => NavigationCubit(),
            ),
          ],
          child: const MainPage(),
        ),
      ),
    );
  }
}
