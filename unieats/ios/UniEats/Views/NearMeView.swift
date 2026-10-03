import CoreLocation
import SwiftData
import SwiftUI

struct NearMeView: View {
    static let radiusMeters: CLLocationDistance = 2_000

    let repository: SpotRepository
    @State private var location = LocationManager()
    @Query(sort: \SpotEntity.name) private var spots: [SpotEntity]

    private var nearby: [(spot: Spot, distance: CLLocationDistance)] {
        guard let here = location.location else { return [] }
        return spots
            .map { ($0.spot, here.distance(from: CLLocation(latitude: $0.lat, longitude: $0.lng))) }
            .filter { $0.1 <= Self.radiusMeters }
            .sorted { $0.1 < $1.1 }
    }

    var body: some View {
        NavigationStack {
            content
                .navigationTitle("Near Me")
                .navigationDestination(for: String.self) { spotId in
                    SpotDetailView(spotId: spotId, repository: repository)
                }
        }
        .onAppear { location.start() }
        .onDisappear { location.stop() }
        .task { await repository.fetchAllPages() }
    }

    @ViewBuilder
    private var content: some View {
        switch location.authorizationStatus {
        case .denied, .restricted:
            ContentUnavailableView(
                "Location is off",
                systemImage: "location.slash",
                description: Text("Allow UniEats to use your location in Settings to see spots within 2 km.")
            )
        case .notDetermined:
            ContentUnavailableView {
                Label("Spots near you", systemImage: "location.circle")
            } description: {
                Text("UniEats uses your location only while this screen is open.")
            } actions: {
                Button("Enable location") { location.start() }
                    .buttonStyle(.borderedProminent)
            }
        default:
            if location.location == nil {
                ProgressView("Finding your location…")
            } else if nearby.isEmpty {
                ContentUnavailableView(
                    "Nothing within 2 km",
                    systemImage: "mappin.slash",
                    description: Text("Simulator: Features › Location › Custom Location… 44.427, 26.103")
                )
            } else {
                List(nearby, id: \.spot.id) { item in
                    NavigationLink(value: item.spot.id) {
                        HStack {
                            VStack(alignment: .leading) {
                                Text(item.spot.name).font(.headline)
                                Text(item.spot.category.capitalized).font(.caption).foregroundStyle(.secondary)
                            }
                            Spacer()
                            Text(Measurement(value: item.distance, unit: UnitLength.meters),
                                 format: .measurement(width: .abbreviated, usage: .road))
                                .font(.callout)
                                .foregroundStyle(.orange)
                        }
                    }
                }
                .listStyle(.plain)
            }
        }
    }
}
