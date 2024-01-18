import 'package:flutter/material.dart';
import 'package:flutter_svg/flutter_svg.dart';
import 'package:observable_accessibility/features/onboarding/models/onboarding_step.dart';

class OnboardingStepPageView extends StatelessWidget {
  const OnboardingStepPageView({
    super.key,
    required this.onboardingStep,
  });

  final OnboardingStep onboardingStep;

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.all(16),
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          SvgPicture.asset(
            onboardingStep.image,
            height: 200,
          ),
          const SizedBox(height: 20),
          Text(
            onboardingStep.title,
            textAlign: TextAlign.center,
            style: const TextStyle(
              fontSize: 24,
              fontWeight: FontWeight.bold,
            ),
          ),
          const SizedBox(height: 10),
          Text(
            onboardingStep.subtitle,
            style: const TextStyle(
              fontSize: 16,
              color: Colors.grey,
            ),
            textAlign: TextAlign.center,
          ),
        ],
      ),
    );
  }
}
