import 'dart:math';

import 'package:flutter/material.dart';
import 'package:flutter/scheduler.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:observable_accessibility/common/models/appointment.dart';
import 'package:observable_accessibility/common/widgets/primary_button.dart';
import 'package:observable_accessibility/features/appointment_scheduler/cubit/appointment_cubit.dart';
import 'package:observable_accessibility/features/appointment_scheduler/widgets/appointment_date_field.dart';
import 'package:observable_accessibility/features/appointment_scheduler/widgets/appointment_name_field.dart';
import 'package:observable_accessibility/features/appointment_scheduler/widgets/appointment_phone_number_field.dart';
import 'package:observable_accessibility/features/appointment_scheduler/widgets/appointment_terms_field.dart';
import 'package:observable_accessibility/features/appointment_scheduler/widgets/appointment_time_field.dart';
import 'package:clock/clock.dart';

class ScheduleAppointmentForm extends StatefulWidget {
  const ScheduleAppointmentForm({super.key});

  @override
  State<ScheduleAppointmentForm> createState() =>
      _ScheduleAppointmentFormState();
}

class _ScheduleAppointmentFormState extends State<ScheduleAppointmentForm> {
  DateTime selectedDate = clock.now();
  TimeOfDay selectedTime = TimeOfDay(
    hour: clock.now().hour,
    minute: clock.now().minute,
  );
  String phoneNumber = '';

  final _formKey = GlobalKey<FormState>();

  final _dateController = TextEditingController();
  final _phoneController = TextEditingController();
  final _timeController = TextEditingController();
  final _nameController = TextEditingController();

  final _dateFieldFocusNode = FocusNode();
  final _timeFieldFocusNode = FocusNode();
  final _phoneFieldFocusNode = FocusNode();
  final _termsFieldFocusNode = FocusNode();
  final _bookButtonFocusNode = FocusNode();

  bool _isTermsChecked = false;

  bool get _canBookAppointment => phoneNumber.isNotEmpty && _isTermsChecked;

  @override
  void dispose() {
    _dateController.dispose();
    _timeController.dispose();
    _phoneController.dispose();
    _nameController.dispose();

    _dateFieldFocusNode.dispose();
    _timeFieldFocusNode.dispose();
    _phoneFieldFocusNode.dispose();
    _termsFieldFocusNode.dispose();
    _bookButtonFocusNode.dispose();

    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Form(
      key: _formKey,
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: <Widget>[
          const Text(
            'Choose your slot:',
            style: TextStyle(
              fontSize: 18,
              fontWeight: FontWeight.bold,
            ),
          ),
          const SizedBox(
            height: 16,
          ),
          AppointmentNameField(
            controller: _nameController,
            onEditingComplete: () {
              _dateFieldFocusNode.requestFocus();
            },
          ),
          AppointmentDateField(
            controller: _dateController,
            focusNode: _dateFieldFocusNode,
            initialDate: selectedDate,
            onDateSelected: (date) {
              setState(() {
                selectedDate = date;
                _dateController.text =
                    selectedDate.toLocal().toString().split(' ')[0];
              });

              _timeFieldFocusNode.requestFocus();
            },
          ),
          AppointmentTimeField(
            controller: _timeController,
            focusNode: _timeFieldFocusNode,
            initialTime: selectedTime,
            onTimeSelected: (time) {
              setState(() {
                selectedTime = TimeOfDay(hour: time.hour, minute: time.minute);
                _timeController.text = selectedTime.format(context);
              });

              _phoneFieldFocusNode.requestFocus();
            },
          ),
          AppointmentPhoneNumberField(
            controller: _phoneController,
            focusNode: _phoneFieldFocusNode,
            onChanged: (number) {
              setState(() {
                phoneNumber = number; // Store the entered phone number
              });

              _termsFieldFocusNode.requestFocus();
            },
          ),
          const SizedBox(height: 10),
          AppointmentTermsField(
            isChecked: _isTermsChecked,
            onChanged: (value) {
              setState(() {
                _isTermsChecked = value ?? false;
              });

              _bookButtonFocusNode.requestFocus();
            },
          ),
          const SizedBox(height: 24),
          PrimaryButton(
            onPressed: _canBookAppointment ? _bookAppointment : null,
            focusNode: _bookButtonFocusNode,
            text: 'Book slot!',
          ),
          _BookingResultIndicator(
            onResetBookingForm: _resetBookingForm,
          ),
        ],
      ),
    );
  }

  void _bookAppointment() {
    if (_formKey.currentState!.validate()) {
      final appointment = Appointment(
        name: _nameController.text,
        timeSlot: _mergeDateTimeAndTimeOfDay(selectedDate, selectedTime),
        code: Random().nextInt(1000).toString(),
      );

      FocusScope.of(context).unfocus();

      context.read<AppointmentCubit>().bookAppointment(appointment);
    }
  }

  DateTime _mergeDateTimeAndTimeOfDay(DateTime date, TimeOfDay time) {
    return DateTime(date.year, date.month, date.day, time.hour, time.minute);
  }

  void _resetBookingForm() {
    setState(() {
      _timeController.clear();
      _dateController.clear();
      _phoneController.clear();
      _nameController.clear();
      _isTermsChecked = false;
    });

    context.read<AppointmentCubit>().resetState();
  }
}

class _BookingResultIndicator extends StatelessWidget {
  const _BookingResultIndicator({
    Key? key,
    required this.onResetBookingForm,
  }) : super(key: key);

  final VoidCallback onResetBookingForm;

  @override
  Widget build(BuildContext context) {
    return BlocListener<AppointmentCubit, AppointmentState>(
      listener: (context, state) {
        if (state is AppointmentBookingSuccess) {
          SchedulerBinding.instance.addPostFrameCallback(
            (_) async {
              await showDialog<AlertDialog>(
                context: context,
                builder: (BuildContext context) {
                  return AlertDialog(
                    title: const Text('Success'),
                    content: const Text('Appointment was booked'),
                    actions: [
                      TextButton(
                        onPressed: Navigator.of(context).pop,
                        child: const Text('OK'),
                      ),
                    ],
                  );
                },
              );

              onResetBookingForm();
            },
          );
        }
      },
      child: BlocBuilder<AppointmentCubit, AppointmentState>(
        builder: (context, state) {
          if (state is AppointmentBookingInProgress) {
            return const Center(child: CircularProgressIndicator());
          } else if (state is AppointmentBookingFailure) {
            return Text(state.error);
          } else {
            return const SizedBox();
          }
        },
      ),
    );
  }
}
