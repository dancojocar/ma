import 'dart:convert';

import 'package:observable_accessibility/common/models/appointment.dart';
import 'package:rxdart/rxdart.dart';
import 'package:shared_preferences/shared_preferences.dart';

class AppointmentService {
  AppointmentService({required this.sharedPreferences});

  final SharedPreferences sharedPreferences;

  static const _appointmentsKey = 'appointments';

  // Stream controller for all appointments.
  final _controller = BehaviorSubject<List<Appointment>>();

  Stream<List<Appointment>> watchAllAppointments() {
    _updateAppointmentsStream();

    return _controller.stream;
  }

  Future<String> bookAppointment(Appointment appointment) async {
    final appointments = await _getAppointments();

    await _saveAppointments(appointments..add(appointment));

    return appointment.code;
  }

  Future<void> removeAppointment(String code) async {
    final appointments = await _getAppointments();

    final appointmentCanBeRemoved = appointments.any(
      (appointment) => appointment.code == code,
    );

    if (!appointmentCanBeRemoved) {
      throw Exception();
    }

    await _saveAppointments(
      appointments.where((appointment) => appointment.code != code).toList(),
    );
  }

  Future<bool> isAvailable(Appointment appointment) async {
    final storedAppointments = await _getAppointments();

    final foundSameTimeSlotAppointment = storedAppointments.any(
      (storedAppointment) => storedAppointment.timeSlot == appointment.timeSlot,
    );

    return !foundSameTimeSlotAppointment;
  }

  // Return list of all stored appointment.
  Future<List<Appointment>> _getAppointments() async {
    final appointmentsJson = await _getAppointmentsJsonList() ?? [];

    return appointmentsJson
        .map(
          (json) => Appointment.fromJson(json as Map<String, dynamic>),
        )
        .toList();
  }

  // Converts the stored appointments from json to list and return them.
  Future<List?> _getAppointmentsJsonList() async {
    final result = sharedPreferences.getString(_appointmentsKey);

    if (result != null) {
      final json = jsonDecode(result) as List;

      return json;
    } else {
      return null;
    }
  }

  // Converts the appointments list to json and stores it.
  Future<bool> _saveAppointments(List<Appointment> appointments) async {
    final jsonString = jsonEncode(appointments);

    final result = sharedPreferences.setString(_appointmentsKey, jsonString);

    _updateAppointmentsStream();

    return result;
  }

  void _updateAppointmentsStream() async {
    final appointments = await _getAppointments();

    _controller.add(appointments);
  }
}
