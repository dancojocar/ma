import 'package:material_ui/material_ui.dart';

import '../../domain/models.dart';
import '../common/spot_badges.dart';
import '../common/spot_photo.dart';

class SpotDetailScreen extends StatelessWidget {
  const SpotDetailScreen({super.key, required this.spot});

  final Spot spot;

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return Scaffold(
      appBar: AppBar(title: Text(spot.name)),
      body: ListView(
        padding: const EdgeInsets.all(16),
        children: [
          ClipRRect(
            borderRadius: BorderRadius.circular(12),
            child: SpotPhoto(
              url: spot.photoUrl,
              height: 220,
              semanticLabel: 'Photo of ${spot.name}',
            ),
          ),
          const SizedBox(height: 16),
          Row(
            children: [
              Expanded(
                child: Text(
                  spot.name,
                  style: theme.textTheme.headlineSmall?.copyWith(
                    fontWeight: FontWeight.bold,
                  ),
                ),
              ),
              OpenBadge(openNow: spot.openNow),
            ],
          ),
          const SizedBox(height: 8),
          SpotMetaRow(spot: spot),
          const SizedBox(height: 12),
          Text(spot.description, style: theme.textTheme.bodyMedium),
        ],
      ),
    );
  }
}
