import 'package:flutter/material.dart';
import 'package:observable_accessibility/common/widgets/page_indicator.dart';
import 'package:observable_accessibility/features/onboarding/models/onboarding_step.dart';
import 'package:observable_accessibility/features/onboarding/widgets/onbording_step_page_view.dart';

class OnboardingPage extends StatefulWidget {
  const OnboardingPage({super.key});

  @override
  State<OnboardingPage> createState() => _OnboardingPageState();
}

class _OnboardingPageState extends State<OnboardingPage> {
  late PageController _pageController;

  @override
  void initState() {
    super.initState();
    _pageController = PageController(initialPage: 0);
  }

  @override
  void dispose() {
    _pageController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Tutorial'),
      ),
      body: Stack(
        children: [
          PageView.builder(
            controller: _pageController,
            itemCount: OnboardingStep.values.length,
            itemBuilder: (context, index) => OnboardingStepPageView(
              onboardingStep: OnboardingStep.values[index],
            ),
          ),
          Align(
            alignment: Alignment.bottomCenter,
            child: SizedBox(
              height: 128,
              child: PageIndicator(
                pagesCount: OnboardingStep.values.length,
                controller: _pageController,
              ),
            ),
          ),
        ],
      ),
    );
  }
}
