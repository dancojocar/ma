import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:observable_accessibility/common/models/appointment.dart';
import 'package:observable_accessibility/features/appointment_list/cubit/appointment_list_cubit.dart';
import 'package:observable_accessibility/features/appointment_list/widgets/appointment_list_item.dart';
import 'package:observable_accessibility/features/appointment_scheduler/cubit/appointment_cubit.dart';

class AppointmentListPage extends StatelessWidget {
  const AppointmentListPage({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Appointment List'),
      ),
      body: SafeArea(
        child: BlocBuilder<AppointmentListCubit, AppointmentListState>(
          builder: (context, state) {
            return state.maybeMap(
              loaded: (loaded) {
                if (loaded.appointments.isNotEmpty) {
                  return _AppointmentsList(
                    appointments: loaded.appointments,
                  );
                } else {
                  return const Center(
                    child: Text('No appointments added yet'),
                  );
                }
              },
              failure: (failure) => const Center(
                child: Text(
                  'Error loading appointments:',
                  style: TextStyle(color: Colors.red),
                ),
              ),
              orElse: () => const Center(
                child: CircularProgressIndicator(),
              ),
            );
          },
        ),
      ),
    );
  }
}

class _AppointmentsList extends StatelessWidget {
  const _AppointmentsList({
    required this.appointments,
  });

  final List<Appointment> appointments;

  @override
  Widget build(BuildContext context) {
    return CustomScrollView(
      slivers: <Widget>[
        SliverList(
          delegate: SliverChildBuilderDelegate(
            (context, index) {
              final appointment = appointments[index];

              return Padding(
                padding: const EdgeInsets.only(left: 16, top: 16, right: 16),
                child: AppointmentListItem(
                  appointment: appointment,
                  onTap: () =>
                      context.read<AppointmentCubit>().removeAppointment(
                            appointment.code,
                          ),
                ),
              );
            },
            childCount: appointments.length,
          ),
        ),
      ],
    );
  }
}
