// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'appointment.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

_$AppointmentImpl _$$AppointmentImplFromJson(Map<String, dynamic> json) =>
    _$AppointmentImpl(
      name: json['name'] as String,
      timeSlot: DateTime.parse(json['timeSlot'] as String),
      code: json['code'] as String,
    );

Map<String, dynamic> _$$AppointmentImplToJson(_$AppointmentImpl instance) =>
    <String, dynamic>{
      'name': instance.name,
      'timeSlot': instance.timeSlot.toIso8601String(),
      'code': instance.code,
    };
