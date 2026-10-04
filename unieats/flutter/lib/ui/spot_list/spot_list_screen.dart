import 'dart:async';

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
      ref.read(spotPagingProvider.notifier).loadNextPage();
    }
  }

  Future<void> _refresh() => ref
      .refresh(spotPagingProvider.future)
      // A failed refresh is rendered from the provider's error state.
      .then<void>((_) {}, onError: (_) {});

  @override
  Widget build(BuildContext context) {
    ref.watch(liveSyncProvider);
    final ui = ref.watch(spotListProvider);
    final notifier = ref.read(spotListProvider.notifier);
    final spots = ref.watch(visibleSpotsProvider);
    final paging = ref.watch(spotPagingProvider);
    final pendingIds =
        ref.watch(pendingSpotIdsProvider).valueOrNull ?? const <String>{};
    final pendingCount = ref.watch(pendingChangesCountProvider).valueOrNull;
    final pagingError = paging.isLoading ? null : paging.error;
    void retry() => ref.invalidate(spotPagingProvider);

    return Scaffold(
      appBar: AppBar(
        title: const Text('UniEats'),
        actions: [
          IconButton(
            tooltip: 'Spots near me',
            icon: const Icon(Icons.near_me_outlined),
            onPressed: () => context.push('/nearby'),
          ),
          IconButton(
            tooltip: 'Sign out',
            icon: const Icon(Icons.logout),
            onPressed: () => ref.read(authProvider.notifier).logout(),
          ),
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
          if (pendingCount != null && pendingCount > 0)
            PendingSyncBanner(
              count: pendingCount,
              onSyncNow: () => ref.read(spotRepositoryProvider).syncOutbox(),
            ),
          if (pagingError != null && (spots.valueOrNull?.isNotEmpty ?? false))
            OfflineBanner(error: pagingError, onRetry: retry),
          SizedBox(
            height: 4,
            child: paging.isLoading ? const LinearProgressIndicator() : null,
          ),
          Expanded(
            child: RefreshIndicator(
              onRefresh: _refresh,
              child: switch (spots) {
                AsyncValue(valueOrNull: final list?) when list.isNotEmpty =>
                  _SpotList(
                    controller: _scrollController,
                    spots: list,
                    paging: paging.valueOrNull,
                    favouriteIds: ui.favouriteIds,
                    pendingIds: pendingIds,
                  ),
                AsyncValue(valueOrNull: _?) when pagingError != null =>
                  _Scrollable(
                    child: ErrorView(error: pagingError, onRetry: retry),
                  ),
                AsyncValue(valueOrNull: _?) when !paging.isLoading =>
                  const _Scrollable(child: EmptySpotsMessage()),
                AsyncValue(:final error?) => _Scrollable(
                  child: ErrorView(
                    error: error,
                    onRetry: () => ref.invalidate(visibleSpotsProvider),
                  ),
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
    required this.spots,
    required this.paging,
    required this.favouriteIds,
    required this.pendingIds,
  });

  final ScrollController controller;
  final List<Spot> spots;
  final SpotPaging? paging;
  final Set<String> favouriteIds;
  final Set<String> pendingIds;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final showFooter = paging?.hasNextPage ?? false;
    return ListView.builder(
      controller: controller,
      physics: const AlwaysScrollableScrollPhysics(),
      padding: const EdgeInsets.fromLTRB(16, 0, 16, 16),
      itemCount: spots.length + (showFooter ? 1 : 0),
      itemBuilder: (context, index) {
        if (index == spots.length) {
          return _PageFooter(
            error: paging?.loadMoreError,
            onRetry: () => ref.read(spotPagingProvider.notifier).loadNextPage(),
          );
        }
        final spot = spots[index];
        return AnimatedEntrance(
          key: ValueKey(spot.id),
          index: index,
          child: SpotCard(
            spot: spot,
            isFavourite: favouriteIds.contains(spot.id),
            isPending: pendingIds.contains(spot.id),
            onFavouriteToggle:
                () => ref
                    .read(spotListProvider.notifier)
                    .toggleFavourite(spot.id),
            onTap: () => context.push('/spots/${spot.id}'),
          ),
        );
      },
    );
  }
}

/// Fades and slides a card in the first time it is built. The first ten
/// cards start 40 ms apart (a stagger); cards built later while scrolling
/// start at once. Skipped when the OS asks for reduced motion.
class AnimatedEntrance extends StatefulWidget {
  const AnimatedEntrance({super.key, required this.index, required this.child});

  final int index;
  final Widget child;

  @override
  State<AnimatedEntrance> createState() => _AnimatedEntranceState();
}

class _AnimatedEntranceState extends State<AnimatedEntrance>
    with SingleTickerProviderStateMixin {
  late final _controller = AnimationController(
    vsync: this,
    duration: const Duration(milliseconds: 350),
  );
  late final _curve = CurvedAnimation(
    parent: _controller,
    curve: Curves.easeOutCubic,
  );
  late final _slide = Tween(
    begin: const Offset(0, 0.08),
    end: Offset.zero,
  ).animate(_curve);
  Timer? _start;

  @override
  void didChangeDependencies() {
    super.didChangeDependencies();
    if (_controller.isAnimating || _controller.isCompleted || _start != null) {
      return;
    }
    if (MediaQuery.disableAnimationsOf(context)) {
      _controller.value = 1;
      return;
    }
    final delay = widget.index < 10 ? widget.index * 40 : 0;
    _start = Timer(Duration(milliseconds: delay), _controller.forward);
  }

  @override
  void dispose() {
    _start?.cancel();
    _curve.dispose();
    _controller.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) => FadeTransition(
    opacity: _curve,
    child: SlideTransition(position: _slide, child: widget.child),
  );
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
    this.isPending = false,
  });

  final Spot spot;
  final bool isFavourite;
  final bool isPending;
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
            Hero(
              tag: 'spot-photo-${spot.id}',
              child: SpotPhoto(url: spot.photoUrl, height: 140),
            ),
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
                      if (isPending)
                        const Padding(
                          padding: EdgeInsets.only(right: 6),
                          child: Tooltip(
                            message: 'Waiting to sync',
                            child: Icon(Icons.cloud_upload_outlined, size: 18),
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
