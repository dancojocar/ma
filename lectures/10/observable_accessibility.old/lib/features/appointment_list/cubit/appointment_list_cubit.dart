import 'dart:async';

import 'package:bloc/bloc.dart';
import 'package:freezed_annotation/freezed_annotation.dart';
import 'package:observable_accessibility/common/models/appointment.dart';
import 'package:observable_accessibility/common/services/appointment_service.dart';

part 'appointment_list_state.dart';
part 'appointment_list_cubit.freezed.dart';

class AppointmentListCubit extends Cubit<AppointmentListState> {
  AppointmentListCubit({
    required AppointmentService appointmentService,
  })  : _appointmentService = appointmentService,
        super(const AppointmentListState.initial());

  final AppointmentService _appointmentService;

  StreamSubscription<List<Appointment>>? _appointmentsStreamSubscription;

  void subscribeToAppointments() {
    emit(
      const AppointmentListState.loading(),
    );

    try {
      _appointmentsStreamSubscription =
          _appointmentService.watchAllAppointments().listen(
                (appointments) => emit(
                  AppointmentListState.loaded(appointments),
                ),
              )..onError(
              (error) {
                emit(const AppointmentListState.failure());
              },
            );
    } catch (e) {
      emit(const AppointmentListState.failure());
    }
  }

  @override
  Future<void> close() {
    _appointmentsStreamSubscription?.cancel();

    return super.close();
  }
}
