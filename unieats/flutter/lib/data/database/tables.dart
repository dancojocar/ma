import 'package:drift/drift.dart';

@DataClassName('SpotRow')
class Spots extends Table {
  TextColumn get id => text()();
  TextColumn get name => text()();
  TextColumn get category => text()();
  RealColumn get rating => real()();
  IntColumn get priceLevel => integer()();
  RealColumn get lat => real()();
  RealColumn get lng => real()();
  BoolColumn get openNow => boolean()();
  TextColumn get photoUrl => text()();
  TextColumn get description => text()();
  IntColumn get updatedAt => integer()();

  /// True while a local edit has not been confirmed by the server.
  BoolColumn get pendingSync => boolean().withDefault(const Constant(false))();

  @override
  Set<Column<Object>> get primaryKey => {id};
}

@DataClassName('ReviewRow')
class Reviews extends Table {
  TextColumn get id => text()();
  TextColumn get spotId => text()();
  TextColumn get author => text()();
  IntColumn get stars => integer()();
  TextColumn get body => text()();
  IntColumn get createdAt => integer()();

  @override
  Set<Column<Object>> get primaryKey => {id};
}

/// Pending writes, replayed in [seq] order. Survives app restarts.
@DataClassName('OutboxOp')
class Outbox extends Table {
  IntColumn get seq => integer().autoIncrement()();
  TextColumn get opId => text().unique()();
  TextColumn get type => text()();
  TextColumn get entityId => text()();
  TextColumn get payload => text()();
  IntColumn get createdAt => integer()();
}
