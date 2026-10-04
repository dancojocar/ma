public enum ConflictWinner: Sendable, Equatable {
    case server
    case client
}

/// Last-write-wins on `updatedAt` (CONTRACT §5): the server wins only when its version is
/// strictly newer; a tie goes to the client.
public enum SpotConflictResolver {
    public static func winner(local: Spot, server: Spot) -> ConflictWinner {
        server.updatedAt > local.updatedAt ? .server : .client
    }

    public static func resolve(local: Spot, server: Spot) -> Spot {
        winner(local: local, server: server) == .server ? server : local
    }
}
