import 'package:test/test.dart';
import 'package:unieats_data/unieats_data.dart';

Spot spot({required int updatedAt}) => Spot(
  id: 'spot-3',
  name: 'Pizza Stop',
  category: SpotCategory.fastfood,
  rating: 4.1,
  priceLevel: 2,
  lat: 44.426,
  lng: 26.1015,
  openNow: true,
  photoUrl: '',
  description: 'Pizza by the slice',
  updatedAt: updatedAt,
);

void main() {
  const resolver = SyncConflictResolver();

  test('a strictly newer server copy wins', () {
    expect(
      resolver.resolve(
        local: spot(updatedAt: 1000),
        server: spot(updatedAt: 1001),
      ),
      ConflictWinner.server,
    );
  });

  test('a tie keeps the client edit', () {
    expect(
      resolver.resolve(
        local: spot(updatedAt: 1000),
        server: spot(updatedAt: 1000),
      ),
      ConflictWinner.client,
    );
  });

  test('an older server copy loses to the client', () {
    expect(
      resolver.resolve(
        local: spot(updatedAt: 2000),
        server: spot(updatedAt: 1000),
      ),
      ConflictWinner.client,
    );
  });
}
