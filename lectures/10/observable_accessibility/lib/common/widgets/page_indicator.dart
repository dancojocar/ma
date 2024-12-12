import 'package:flutter/material.dart';
import 'package:smooth_page_indicator/smooth_page_indicator.dart';

class PageIndicator extends StatelessWidget {
  const PageIndicator({
    super.key,
    required this.pagesCount,
    required this.controller,
  });

  final int pagesCount;
  final PageController controller;

  @override
  Widget build(BuildContext context) {
    return Center(
      child: SmoothPageIndicator(
        controller: controller,
        count: pagesCount,
        effect: const ExpandingDotsEffect(
          dotWidth: 8,
          dotHeight: 8,
          strokeWidth: 4,
          dotColor: Colors.grey,
          activeDotColor: Colors.amber,
          expansionFactor: 2,
        ),
      ),
    );
  }
}
