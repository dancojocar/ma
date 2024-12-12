part of 'appointment_cubit.dart';

@freezed
class AppointmentState with _$AppointmentState {
  const factory AppointmentState.initialLoading() = AppointmentInitialLoading;

  const factory AppointmentState.bookingInProgress() =
      AppointmentBookingInProgress;
  const factory AppointmentState.bookingSuccess(String code) =
      AppointmentBookingSuccess;
  const factory AppointmentState.bookingFailure(String error) =
      AppointmentBookingFailure;

  const factory AppointmentState.removingInProgress() =
      AppointmentRemovingInProgress;
  const factory AppointmentState.removingSuccess() = AppointmentRemovingSuccess;
  const factory AppointmentState.removingFailure(String error) =
      AppointmentRemovingFailure;
}
