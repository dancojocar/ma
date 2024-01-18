part of 'appointment_list_cubit.dart';

@freezed
class AppointmentListState with _$AppointmentListState {
  const factory AppointmentListState.initial() = AppointmentListInitial;
  const factory AppointmentListState.loading() = AppointmentListLoading;
  const factory AppointmentListState.loaded(List<Appointment> appointments) =
      AppointmentListLoaded;
  const factory AppointmentListState.failure() = AppointmentListFailure;
}
