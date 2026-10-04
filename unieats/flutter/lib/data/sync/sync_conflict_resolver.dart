import '../../domain/models.dart';

enum ConflictWinner { server, client }

/// Last-write-wins on `updatedAt` (CONTRACT, offline sync §5): the server's
/// copy wins only when it is strictly newer; a tie keeps the client's edit.
class SyncConflictResolver {
  const SyncConflictResolver();

  ConflictWinner resolve({required Spot local, required Spot server}) =>
      server.updatedAt > local.updatedAt
          ? ConflictWinner.server
          : ConflictWinner.client;
}
