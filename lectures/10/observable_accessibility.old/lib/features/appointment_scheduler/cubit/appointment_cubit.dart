import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:observable_accessibility/common/models/appointment.dart';
import 'package:observable_accessibility/common/services/appointment_service.dart';
import 'package:freezed_annotation/freezed_annotation.dart';

part 'appointment_state.dart';
part 'appointment_cubit.freezed.dart';

class AppointmentCubit extends Cubit<AppointmentState> {
  AppointmentCubit({
    required AppointmentService appointmentService,
  })  : _appointmentService = appointmentService,
        super(const AppointmentState.initialLoading());

  final AppointmentService _appointmentService;

  /// Adds an [Appointment].
  ///
  /// This method sets the state to [AppointmentRemovingInProgress] and
  /// checks if appointment is available.
  /// [AppointmentBookingSuccess] is yielded after successful booking. In case
  /// of an error or an unavailable [AppointmentBookingFailure] is yielded.
  Future<void> bookAppointment(Appointment appointment) async {
    emit(const AppointmentState.bookingInProgress());

    try {
      if (await _appointmentService.isAvailable(appointment)) {
        final code = await _appointmentService.bookAppointment(appointment);

        emit(
          AppointmentState.bookingSuccess(code),
        );
      } else {
        emit(
          const AppointmentState.bookingFailure(
            'Slot was already taken. Please choose another slot',
          ),
        );
      }
    } catch (error) {
      emit(
        const AppointmentState.bookingFailure('Booking appointment failed.'),
      );
    }
  }

  /// Remove an appointment from database.
  ///
  /// This method sets the state to [AppointmentRemovingInProgress] and to
  /// [AppointmentRemovingSuccess] after successful removal and
  /// [AppointmentRemovingFailure] if it is unsuccessful.
  Future<void> removeAppointment(String code) async {
    emit(const AppointmentState.removingInProgress());

    if (code.isNotEmpty) {
      try {
        await _appointmentService.removeAppointment(code);

        emit(const AppointmentState.removingSuccess());
      } catch (error) {
        emit(
          const AppointmentState.removingFailure(
            'Removing appointment failed.',
          ),
        );
      }
    } else {
      emit(
        const AppointmentState.removingFailure('No code found'),
      );
    }
  }

  /// Reset the state to [AppointmentInitialLoading].
  ///
  void resetState() => emit(const AppointmentState.initialLoading());
}
