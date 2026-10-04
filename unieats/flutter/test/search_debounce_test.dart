import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:unieats/providers/providers.dart';
import 'package:unieats_data/unieats_data.dart';

/// Records the query of every page request.
class RecordingSpotRepository implements SpotRepository {
  final requests = <({int page, String query, SpotCategory? category})>[];
  bool hasNextPage = false;

  @override
  Future<SpotsPage> refreshPage({
    required int page,
    required int limit,
    String query = '',
    SpotCategory? category,
  }) async {
    requests.add((page: page, query: query, category: category));
    return SpotsPage(spots: const [], page: page, hasNextPage: hasNextPage);
  }

  @override
  Object? noSuchMethod(Invocation invocation) => super.noSuchMethod(invocation);
}

void main() {
  late RecordingSpotRepository repo;
  late ProviderContainer c;

  setUp(() async {
    repo = RecordingSpotRepository();
    c = ProviderContainer.test(
      retry: (_, _) => null,
      overrides: [spotRepositoryProvider.overrideWithValue(repo)],
    );
    c.listen(spotPagingProvider, (_, _) {});
    await c.read(spotPagingProvider.future);
  });

  Future<void> type(String query) async {
    c.read(spotListProvider.notifier).setSearchQuery(query);
    c.read(spotPagingProvider);
    await Future<void>.delayed(const Duration(milliseconds: 50));
  }

  test('the first page loads at once, without a query', () {
    expect(repo.requests.single, (page: 1, query: '', category: null));
  });

  test('typing quickly sends one request, for the last query', () async {
    for (final query in ['p', 'pi', 'piz', 'pizza']) {
      await type(query);
    }
    final paging = await c.read(spotPagingProvider.future);
    await Future<void>.delayed(const Duration(milliseconds: 400));

    expect(repo.requests.map((r) => r.query), ['', 'pizza']);
    expect(paging.page, 1);
    expect(c.read(spotPagingProvider).hasError, isFalse);
  });

  test('a category chip applies without the search delay', () async {
    c.read(spotListProvider.notifier).setCategoryFilter(SpotCategory.bakery);
    await c.read(spotPagingProvider.future);

    expect(repo.requests.last, (
      page: 1,
      query: '',
      category: SpotCategory.bakery,
    ));
  });

  test('the next page keeps the filter and stops at the last page', () async {
    repo.hasNextPage = true;
    c.invalidate(spotPagingProvider);
    await c.read(spotPagingProvider.future);
    repo.hasNextPage = false;

    await c.read(spotPagingProvider.notifier).loadNextPage();
    await c.read(spotPagingProvider.notifier).loadNextPage();

    expect(repo.requests.map((r) => r.page), [1, 1, 2]);
    expect(c.read(spotPagingProvider).value?.hasNextPage, isFalse);
  });
}
