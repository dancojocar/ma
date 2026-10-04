import Testing
import UniEatsDomain

@Suite("SpotConflictResolver — last-write-wins on updatedAt")
struct SpotConflictResolverTests {
    private func spot(name: String, updatedAt: Double) -> Spot {
        Spot(
            id: "spot-1", name: name, category: "canteen", rating: 3.8, priceLevel: 1,
            lat: 44.4268, lng: 26.1025, openNow: true, photoUrl: "", description: "",
            updatedAt: updatedAt
        )
    }

    @Test("server wins when the server updatedAt is newer")
    func serverWinsWhenNewer() {
        let local = spot(name: "Local edit", updatedAt: 1_000)
        let server = spot(name: "Server edit", updatedAt: 2_000)

        #expect(SpotConflictResolver.winner(local: local, server: server) == .server)
        #expect(SpotConflictResolver.resolve(local: local, server: server).name == "Server edit")
    }

    @Test("client wins when the local updatedAt is newer")
    func clientWinsWhenNewer() {
        let local = spot(name: "Local edit", updatedAt: 9_000)
        let server = spot(name: "Server edit", updatedAt: 1_000)

        #expect(SpotConflictResolver.winner(local: local, server: server) == .client)
        #expect(SpotConflictResolver.resolve(local: local, server: server).name == "Local edit")
    }

    @Test("client wins on equal updatedAt (tie goes to the client)")
    func clientWinsOnTie() {
        let local = spot(name: "Local edit", updatedAt: 5_000)
        let server = spot(name: "Server edit", updatedAt: 5_000)

        #expect(SpotConflictResolver.winner(local: local, server: server) == .client)
        #expect(SpotConflictResolver.resolve(local: local, server: server).name == "Local edit")
    }
}
