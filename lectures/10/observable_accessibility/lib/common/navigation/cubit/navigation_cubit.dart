import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:freezed_annotation/freezed_annotation.dart';
import 'package:observable_accessibility/common/navigation/models/bottom_tab.dart';

part 'navigation_cubit.freezed.dart';
part 'navigation_state.dart';

/// [NavigationCubit] is responsible for navigation within the app.
///
class NavigationCubit extends Cubit<NavigationState> {
  NavigationCubit() : super(const NavigationState(BottomTab.addAppointment));

  void setCurrentBottomTab(BottomTab tab) => emit(NavigationState(tab));
}
