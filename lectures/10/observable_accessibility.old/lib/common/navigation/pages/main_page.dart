import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:observable_accessibility/common/navigation/cubit/navigation_cubit.dart';
import 'package:observable_accessibility/common/navigation/models/bottom_tab.dart';
import 'package:observable_accessibility/common/navigation/widgets/bottom_nav_bar.dart';
import 'package:observable_accessibility/features/appointment_list/pages/appointment_list_page.dart';
import 'package:observable_accessibility/features/appointment_scheduler/pages/appointment_page.dart';
import 'package:observable_accessibility/features/onboarding/pages/onboarding_page.dart';

class MainPage extends StatelessWidget {
  const MainPage({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: BlocBuilder<NavigationCubit, NavigationState>(
        builder: (context, state) => switch (state.bottomTab) {
          BottomTab.addAppointment => const AppointmentPage(),
          BottomTab.appointmentList => const AppointmentListPage(),
          BottomTab.tutorial => const OnboardingPage(),
        },
      ),
      bottomNavigationBar: const BottomNavBar(),
    );
  }
}
