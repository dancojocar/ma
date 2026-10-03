import SwiftData
import SwiftUI

struct SyncStatusBanner: View {
    let repository: SpotRepository
    @Query private var outbox: [OutboxEntry]

    init(repository: SpotRepository) {
        self.repository = repository
    }

    var body: some View {
        if !outbox.isEmpty {
            HStack(spacing: 8) {
                if repository.isSyncing {
                    ProgressView()
                } else {
                    Image(systemName: "arrow.triangle.2.circlepath")
                }
                Text(outbox.count == 1 ? "1 change pending" : "\(outbox.count) changes pending")
                    .font(.footnote.weight(.medium))
                Spacer()
                Button("Sync now") { Task { await repository.sync() } }
                    .font(.footnote)
                    .disabled(repository.isSyncing)
            }
            .padding(.horizontal)
            .padding(.vertical, 8)
            .background(.orange.opacity(0.15))
        }
    }
}
