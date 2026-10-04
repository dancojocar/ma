import 'dart:convert';

import 'package:flutter_test/flutter_test.dart';
import 'package:unieats/data/network/sse.dart';
import 'package:unieats_data/unieats_data.dart';

const pizzaStopJson = {
  'id': 'spot-3',
  'name': 'Pizza Stop',
  'category': 'fastfood',
  'rating': 4.1,
  'priceLevel': 2,
  'lat': 44.426,
  'lng': 26.1015,
  'openNow': true,
  'photoUrl': 'https://picsum.photos/seed/spot-3/400/300',
  'description': 'Pizza by the slice for students on the go.',
  'updatedAt': 1700000002000,
};

void main() {
  group('Spot JSON', () {
    test('parses the contract shape', () {
      final spot = Spot.fromJson(pizzaStopJson);

      expect(spot.id, 'spot-3');
      expect(spot.category, SpotCategory.fastfood);
      expect(spot.rating, 4.1);
      expect(spot.openNow, isTrue);
      expect(spot.updatedAt, 1700000002000);
    });

    test('toJson round-trips every field', () {
      expect(Spot.fromJson(pizzaStopJson).toJson(), pizzaStopJson);
    });

    test('integer numbers are accepted for double fields', () {
      final spot = Spot.fromJson({...pizzaStopJson, 'rating': 4, 'lat': 44});

      expect(spot.rating, 4.0);
      expect(spot.lat, 44.0);
    });

    test('missing photoUrl and description become empty strings', () {
      final json =
          Map<String, dynamic>.of(pizzaStopJson)
            ..remove('photoUrl')
            ..remove('description');

      final spot = Spot.fromJson(json);

      expect(spot.photoUrl, '');
      expect(spot.description, '');
    });

    test('an unknown category is rejected', () {
      expect(
        () => Spot.fromJson({...pizzaStopJson, 'category': 'sushi'}),
        throwsArgumentError,
      );
    });
  });

  test('SpotsPage reads spots, page and hasNextPage', () {
    final page = SpotsPage.fromJson({
      'spots': [pizzaStopJson],
      'page': 2,
      'hasNextPage': false,
    });

    expect(page.spots.single.name, 'Pizza Stop');
    expect(page.page, 2);
    expect(page.hasNextPage, isFalse);
  });

  test('Review and User parse the contract shape', () {
    final review = Review.fromJson({
      'id': 'review-4',
      'spotId': 'spot-3',
      'author': 'Andrei',
      'stars': 4,
      'text': 'Crispy crust every time.',
      'createdAt': 1700000300000,
    });
    final user = User.fromJson({
      'id': 'user-1',
      'email': 'student@unieats.app',
      'displayName': 'Demo Student',
    });

    expect(review.stars, 4);
    expect(review.text, 'Crispy crust every time.');
    expect(user.displayName, 'Demo Student');
    expect(User.fromJson(user.toJson()).email, user.email);
  });

  group('LiveEvent.fromMessage', () {
    test('spot.updated and spot.created carry the spot', () {
      final updated = LiveEvent.fromMessage(
        jsonEncode({'type': 'spot.updated', 'spot': pizzaStopJson}),
      );
      final created = LiveEvent.fromMessage(
        jsonEncode({'type': 'spot.created', 'spot': pizzaStopJson}),
      );

      expect(
        updated,
        isA<SpotChanged>().having((e) => e.created, 'created', isFalse),
      );
      expect(
        created,
        isA<SpotChanged>().having((e) => e.created, 'created', isTrue),
      );
    });

    test('spot.deleted carries the id', () {
      final event = LiveEvent.fromMessage(
        jsonEncode({'type': 'spot.deleted', 'id': 'spot-3'}),
      );

      expect(event, isA<SpotDeleted>().having((e) => e.id, 'id', 'spot-3'));
    });

    test('malformed or unknown messages are skipped', () {
      expect(LiveEvent.fromMessage('{not json'), isNull);
      expect(LiveEvent.fromMessage(jsonEncode({'type': 'hello'})), isNull);
      expect(
        LiveEvent.fromMessage(
          jsonEncode({
            'type': 'spot.updated',
            'spot': {'id': 1},
          }),
        ),
        isNull,
      );
    });
  });

  test('parseSse splits deltas and the done event', () async {
    const body =
        'data: {"delta":"Pizza Stop is "}\n\n'
        'data: {"delta":"open."}\n\n'
        'event: done\ndata: {"source":"template"}\n\n';

    final frames = await parseSse(Stream.value(utf8.encode(body))).toList();

    expect(frames.map((f) => f.event), ['message', 'message', 'done']);
    expect(jsonDecode(frames.first.data), {'delta': 'Pizza Stop is '});
  });
}
