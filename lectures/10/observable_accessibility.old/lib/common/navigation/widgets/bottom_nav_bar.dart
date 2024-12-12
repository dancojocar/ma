import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:observable_accessibility/common/navigation/cubit/navigation_cubit.dart';
import 'package:observable_accessibility/common/navigation/models/bottom_tab.dart';

class BottomNavBar extends StatelessWidget {
  const BottomNavBar({super.key});

  @override
  Widget build(BuildContext context) {
    return BlocBuilder<NavigationCubit, NavigationState>(
      builder: (context, state) {
        return BottomNavigationBar(
          currentIndex: state.bottomTab.index,
          unselectedItemColor: Colors.grey,
          selectedItemColor: Colors.amber,
          items: _buildBottomTabItems(),
          onTap: (index) => context
              .read<NavigationCubit>()
              .setCurrentBottomTab(BottomTab.values[index]),
        );
      },
    );
  }

  List<BottomNavigationBarItem> _buildBottomTabItems() {
    return BottomTab.values
        .map(
          (bottomTab) => BottomNavigationBarItem(
            icon: Icon(
              switch (bottomTab) {
                BottomTab.tutorial => Icons.info_outline,
                BottomTab.addAppointment => Icons.add,
                BottomTab.appointmentList => Icons.list,
              },
            ),
            label: bottomTab.title,
          ),
        )
        .toList();
  }
}
