enum OnboardingStep {
  scheduleAppointment(
    title: 'Schedule an appointment',
    subtitle: 'It is very easy to schedule an appointment',
    image: 'assets/undraw_booked.svg',
  ),
  selectDate(
    title: 'Select a date',
    subtitle: 'First you can select a date',
    image: 'assets/undraw_booking.svg',
  ),
  selectTime(
    title: 'Select time',
    subtitle: 'Then select a time',
    image: 'assets/undraw_time.svg',
  ),
  seeAllAppointments(
    title: 'See all your appointments',
    subtitle: 'Check all your appointments and delete them if needed',
    image: 'assets/undraw_online_calendar.svg',
  ),
  getNotified(
    title: 'Get notified',
    subtitle: 'If you schedule on web you\'ll receive a push notification',
    image: 'assets/undraw_push_notifications.svg',
  );

  const OnboardingStep({
    required this.title,
    required this.subtitle,
    required this.image,
  });

  final String title;
  final String subtitle;
  final String image;
}
