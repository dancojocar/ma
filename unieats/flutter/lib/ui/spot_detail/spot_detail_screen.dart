import 'dart:async';

import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../data/database/database.dart' show localIdPrefix;
import '../../domain/models.dart';
import '../../providers/providers.dart';
import '../common/spot_badges.dart';
import '../common/status_views.dart';

class SpotDetailScreen extends ConsumerWidget {
  const SpotDetailScreen({super.key, required this.spotId});

  final String spotId;

  Future<void> _refresh(WidgetRef ref) => Future.wait([
    ref.refresh(spotRefreshProvider(spotId).future),
    ref.refresh(reviewsRefreshProvider(spotId).future),
  ])
  // A failed refresh is rendered from the providers' error states.
  .then<void>((_) {}, onError: (_) {});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final spot = ref.watch(spotDetailProvider(spotId));
    final refresh = ref.watch(spotRefreshProvider(spotId));
    final isFavourite = ref.watch(
      spotListProvider.select((s) => s.favouriteIds.contains(spotId)),
    );

    return Scaffold(
      appBar: AppBar(
        title: Text(spot.valueOrNull?.name ?? ''),
        actions: [
          FavouriteButton(
            isFavourite: isFavourite,
            onToggle:
                () =>
                    ref.read(spotListProvider.notifier).toggleFavourite(spotId),
          ),
        ],
      ),
      floatingActionButton:
          spot.valueOrNull == null
              ? null
              : FloatingActionButton.extended(
                onPressed:
                    () => showModalBottomSheet<void>(
                      context: context,
                      isScrollControlled: true,
                      useSafeArea: true,
                      builder: (_) => AddReviewSheet(spotId: spotId),
                    ),
                icon: const Icon(Icons.rate_review_outlined),
                label: const Text('Add review'),
              ),
      body: switch (spot) {
        AsyncValue(valueOrNull: final value?) => RefreshIndicator(
          onRefresh: () => _refresh(ref),
          child: _SpotDetailBody(spot: value),
        ),
        AsyncValue(hasValue: true) when refresh.hasError => ErrorView(
          error: refresh.error!,
          onRetry: () => ref.invalidate(spotRefreshProvider(spotId)),
        ),
        AsyncValue(hasValue: true) when !refresh.isLoading => Center(
          child: Text('Spot "$spotId" not found'),
        ),
        AsyncValue(:final error?) => ErrorView(
          error: error,
          onRetry: () => ref.invalidate(spotDetailProvider(spotId)),
        ),
        _ => const Center(child: CircularProgressIndicator()),
      },
    );
  }
}

class _SpotDetailBody extends ConsumerWidget {
  const _SpotDetailBody({required this.spot});

  final Spot spot;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final theme = Theme.of(context);
    final reviews = ref.watch(reviewsProvider(spot.id));
    final reviewsRefresh = ref.watch(reviewsRefreshProvider(spot.id));
    final isPending =
        ref.watch(pendingSpotIdsProvider).valueOrNull?.contains(spot.id) ??
        false;

    return ListView(
      physics: const AlwaysScrollableScrollPhysics(),
      children: [
        SpotPhoto(url: spot.photoUrl, height: 220),
        Padding(
          padding: const EdgeInsets.all(16),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
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
              const SizedBox(height: 12),
              Wrap(
                spacing: 12,
                crossAxisAlignment: WrapCrossAlignment.center,
                children: [
                  FilledButton.tonalIcon(
                    onPressed:
                        () => showModalBottomSheet<void>(
                          context: context,
                          isScrollControlled: true,
                          useSafeArea: true,
                          builder: (_) => EditSpotSheet(spot: spot),
                        ),
                    icon: const Icon(Icons.edit_outlined),
                    label: const Text('Edit spot'),
                  ),
                  if (isPending)
                    const Chip(
                      avatar: Icon(Icons.cloud_upload_outlined, size: 18),
                      label: Text('Waiting to sync'),
                    ),
                ],
              ),
              const SizedBox(height: 24),
              Text(
                reviews.hasValue
                    ? 'Reviews (${reviews.requireValue.length})'
                    : 'Reviews',
                style: theme.textTheme.titleLarge?.copyWith(
                  fontWeight: FontWeight.bold,
                ),
              ),
              const SizedBox(height: 8),
              if (reviewsRefresh.hasError && !reviewsRefresh.isLoading)
                Row(
                  children: [
                    const Expanded(child: Text("Couldn't refresh reviews.")),
                    TextButton(
                      onPressed:
                          () => ref.invalidate(reviewsRefreshProvider(spot.id)),
                      child: const Text('Retry'),
                    ),
                  ],
                ),
              switch (reviews) {
                AsyncValue(valueOrNull: final value?) when value.isNotEmpty =>
                  Column(
                    children: [for (final r in value) ReviewTile(review: r)],
                  ),
                AsyncValue(hasValue: true) when !reviewsRefresh.isLoading =>
                  const Text('No reviews yet.'),
                AsyncValue(:final error?) => Text(
                  'Reviews unavailable: $error',
                ),
                _ => const Center(child: CircularProgressIndicator()),
              },
              const SizedBox(height: 80),
            ],
          ),
        ),
      ],
    );
  }
}

class EditSpotSheet extends ConsumerStatefulWidget {
  const EditSpotSheet({super.key, required this.spot});

  final Spot spot;

  @override
  ConsumerState<EditSpotSheet> createState() => _EditSpotSheetState();
}

class _EditSpotSheetState extends ConsumerState<EditSpotSheet> {
  final _formKey = GlobalKey<FormState>();
  late final _name = TextEditingController(text: widget.spot.name);
  late final _description = TextEditingController(
    text: widget.spot.description,
  );
  late bool _openNow = widget.spot.openNow;

  @override
  void dispose() {
    _name.dispose();
    _description.dispose();
    super.dispose();
  }

  Future<void> _save() async {
    if (!_formKey.currentState!.validate()) return;
    final repo = ref.read(spotRepositoryProvider);
    await repo.editSpot(
      widget.spot.id,
      name: _name.text.trim(),
      description: _description.text.trim(),
      openNow: _openNow,
    );
    unawaited(repo.syncOutbox());
    if (mounted) Navigator.of(context).pop();
  }

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: EdgeInsets.fromLTRB(
        16,
        16,
        16,
        MediaQuery.viewInsetsOf(context).bottom + 16,
      ),
      child: Form(
        key: _formKey,
        child: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            Text('Edit spot', style: Theme.of(context).textTheme.titleLarge),
            const SizedBox(height: 16),
            TextFormField(
              controller: _name,
              decoration: const InputDecoration(labelText: 'Name'),
              validator:
                  (v) =>
                      (v == null || v.trim().isEmpty)
                          ? 'Name is required'
                          : null,
            ),
            const SizedBox(height: 12),
            TextFormField(
              controller: _description,
              maxLines: 3,
              decoration: const InputDecoration(labelText: 'Description'),
            ),
            SwitchListTile(
              contentPadding: EdgeInsets.zero,
              title: const Text('Open now'),
              value: _openNow,
              onChanged: (v) => setState(() => _openNow = v),
            ),
            const SizedBox(height: 8),
            FilledButton(onPressed: _save, child: const Text('Save')),
          ],
        ),
      ),
    );
  }
}

class ReviewTile extends StatelessWidget {
  const ReviewTile({super.key, required this.review});

  final Review review;

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final date = DateTime.fromMillisecondsSinceEpoch(review.createdAt);
    return Card(
      margin: const EdgeInsets.only(bottom: 8),
      child: ListTile(
        title: Row(
          children: [
            Expanded(child: Text(review.author)),
            for (var i = 1; i <= 5; i++)
              Icon(
                i <= review.stars
                    ? Icons.star_rounded
                    : Icons.star_outline_rounded,
                size: 16,
                color: Colors.amber[700],
              ),
          ],
        ),
        subtitle: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            if (review.text.isNotEmpty) Text(review.text),
            Text(
              review.id.startsWith(localIdPrefix)
                  ? 'Waiting to sync'
                  : '${date.day}/${date.month}/${date.year}',
              style: theme.textTheme.labelSmall,
            ),
          ],
        ),
      ),
    );
  }
}

class AddReviewSheet extends ConsumerStatefulWidget {
  const AddReviewSheet({super.key, required this.spotId});

  final String spotId;

  @override
  ConsumerState<AddReviewSheet> createState() => _AddReviewSheetState();
}

class _AddReviewSheetState extends ConsumerState<AddReviewSheet> {
  final _text = TextEditingController();
  int _stars = 5;

  @override
  void dispose() {
    _text.dispose();
    super.dispose();
  }

  Future<void> _submit() async {
    final user = ref.read(authProvider).valueOrNull;
    if (user == null) return;
    await ref
        .read(reviewRepositoryProvider)
        .addReview(
          widget.spotId,
          stars: _stars,
          text: _text.text.trim(),
          author: user.displayName,
        );
    unawaited(ref.read(spotRepositoryProvider).syncOutbox());
    if (mounted) Navigator.of(context).pop();
  }

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: EdgeInsets.fromLTRB(
        16,
        16,
        16,
        MediaQuery.viewInsetsOf(context).bottom + 16,
      ),
      child: Column(
        mainAxisSize: MainAxisSize.min,
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          Text('Add review', style: Theme.of(context).textTheme.titleLarge),
          const SizedBox(height: 8),
          Row(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              for (var i = 1; i <= 5; i++)
                IconButton(
                  tooltip: '$i stars',
                  onPressed: () => setState(() => _stars = i),
                  icon: Icon(
                    i <= _stars
                        ? Icons.star_rounded
                        : Icons.star_outline_rounded,
                    color: Colors.amber[700],
                    size: 32,
                  ),
                ),
            ],
          ),
          TextField(
            controller: _text,
            maxLines: 3,
            decoration: const InputDecoration(labelText: 'Your review'),
          ),
          const SizedBox(height: 12),
          FilledButton(onPressed: _submit, child: const Text('Post review')),
        ],
      ),
    );
  }
}
