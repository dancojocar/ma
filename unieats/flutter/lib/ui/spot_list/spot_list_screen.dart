import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../data/network/live_updates.dart';
import '../../domain/models.dart';
import '../../providers/providers.dart';
import '../common/spot_badges.dart';
import '../common/status_views.dart';
import 'spot_list_widgets.dart';

class SpotListScreen extends ConsumerStatefulWidget {
  const SpotListScreen({super.key});

  @override
  ConsumerState<SpotListScreen> createState() => _SpotListScreenState();
}

class _SpotListScreenState extends ConsumerState<SpotListScreen> {
  final _scrollController = ScrollController();

  @override
  void initState() {
    super.initState();
    _scrollController.addListener(_onScroll);
  }

  @override
  void dispose() {
    _scrollController.dispose();
    super.dispose();
  }

  void _onScroll() {
    final position = _scrollController.position;
    if (position.pixels >= position.maxScrollExtent - 300) {
      ref.read(spotPagesProvider.notifier).loadNextPage();
    }
  }

  Future<void> _refresh() => ref
      .refresh(spotPagesProvider.future)
      // A failed refresh is rendered from the provider's error state.
      .then<void>((_) {}, onError: (_) {});

  @override
  Widget build(BuildContext context) {
    final ui = ref.watch(spotListProvider);
    final notifier = ref.read(spotListProvider.notifier);
    final pages = ref.watch(spotPagesProvider);

    return Scaffold(
      appBar: AppBar(
        title: const Text('UniEats'),
        actions: [
          LiveIndicator(
            connected: switch (ref.watch(liveEventsProvider).valueOrNull) {
              null || LiveDisconnected() => false,
              _ => true,
            },
          ),
        ],
      ),
      body: Column(
        children: [
          SpotSearchBar(
            query: ui.searchQuery,
            onQueryChange: notifier.setSearchQuery,
          ),
          CategoryFilterRow(
            selected: ui.categoryFilter,
            onSelected: notifier.setCategoryFilter,
          ),
          SizedBox(
            height: 4,
            child:
                pages.isLoading && pages.hasValue
                    ? const LinearProgressIndicator()
                    : null,
          ),
          Expanded(
            child: RefreshIndicator(
              onRefresh: _refresh,
              child: switch (pages) {
                AsyncValue(:final error?) when !pages.isLoading => _Scrollable(
                  child: ErrorView(
                    error: error,
                    onRetry: () => ref.invalidate(spotPagesProvider),
                  ),
                ),
                AsyncValue(valueOrNull: final value?)
                    when value.spots.isEmpty =>
                  const _Scrollable(child: EmptySpotsMessage()),
                AsyncValue(valueOrNull: final value?) => _SpotList(
                  controller: _scrollController,
                  pages: value,
                  favouriteIds: ui.favouriteIds,
                ),
                _ => const Center(child: CircularProgressIndicator()),
              },
            ),
          ),
        ],
      ),
    );
  }
}

class _Scrollable extends StatelessWidget {
  const _Scrollable({required this.child});

  final Widget child;

  @override
  Widget build(BuildContext context) => LayoutBuilder(
    builder:
        (context, constraints) => SingleChildScrollView(
          physics: const AlwaysScrollableScrollPhysics(),
          child: SizedBox(height: constraints.maxHeight, child: child),
        ),
  );
}

class _SpotList extends ConsumerWidget {
  const _SpotList({
    required this.controller,
    required this.pages,
    required this.favouriteIds,
  });

  final ScrollController controller;
  final SpotPages pages;
  final Set<String> favouriteIds;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final spots = pages.spots;
    final showFooter =
        pages.hasNextPage || pages.isLoadingMore || pages.loadMoreError != null;
    return ListView.builder(
      controller: controller,
      physics: const AlwaysScrollableScrollPhysics(),
      padding: const EdgeInsets.fromLTRB(16, 0, 16, 16),
      itemCount: spots.length + (showFooter ? 1 : 0),
      itemBuilder: (context, index) {
        if (index == spots.length) {
          return _PageFooter(
            error: pages.loadMoreError,
            onRetry: () => ref.read(spotPagesProvider.notifier).loadNextPage(),
          );
        }
        final spot = spots[index];
        return SpotCard(
          key: ValueKey(spot.id),
          spot: spot,
          isFavourite: favouriteIds.contains(spot.id),
          onFavouriteToggle:
              () =>
                  ref.read(spotListProvider.notifier).toggleFavourite(spot.id),
          onTap: () => context.push('/spots/${spot.id}'),
        );
      },
    );
  }
}

class _PageFooter extends StatelessWidget {
  const _PageFooter({required this.error, required this.onRetry});

  final Object? error;
  final VoidCallback onRetry;

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 16),
      child: Center(
        child:
            error == null
                ? const CircularProgressIndicator()
                : TextButton.icon(
                  onPressed: onRetry,
                  icon: const Icon(Icons.refresh),
                  label: const Text("Couldn't load more. Retry"),
                ),
      ),
    );
  }
}

class SpotCard extends StatelessWidget {
  const SpotCard({
    super.key,
    required this.spot,
    required this.isFavourite,
    required this.onFavouriteToggle,
    required this.onTap,
  });

  final Spot spot;
  final bool isFavourite;
  final VoidCallback onFavouriteToggle;
  final VoidCallback onTap;

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return Card(
      margin: const EdgeInsets.only(top: 12),
      clipBehavior: Clip.antiAlias,
      child: InkWell(
        onTap: onTap,
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            SpotPhoto(url: spot.photoUrl, height: 140),
            Padding(
              padding: const EdgeInsets.fromLTRB(12, 4, 4, 12),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Row(
                    children: [
                      Expanded(
                        child: Text(
                          spot.name,
                          style: theme.textTheme.titleMedium?.copyWith(
                            fontWeight: FontWeight.bold,
                          ),
                          maxLines: 1,
                          overflow: TextOverflow.ellipsis,
                        ),
                      ),
                      OpenBadge(openNow: spot.openNow),
                      FavouriteButton(
                        isFavourite: isFavourite,
                        onToggle: onFavouriteToggle,
                      ),
                    ],
                  ),
                  SpotMetaRow(spot: spot),
                  const SizedBox(height: 6),
                  Padding(
                    padding: const EdgeInsets.only(right: 8),
                    child: Text(
                      spot.description,
                      maxLines: 2,
                      overflow: TextOverflow.ellipsis,
                      style: theme.textTheme.bodySmall?.copyWith(
                        color: theme.colorScheme.onSurfaceVariant,
                      ),
                    ),
                  ),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }
}
