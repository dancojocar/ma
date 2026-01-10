import SwiftUI
import shared

struct ContentView: View {
    @State private var text = "Loading..."
    let greeting = Greeting()

    var body: some View {
        VStack {
            Image(systemName: "globe")
                .imageScale(.large)
                .foregroundStyle(.tint)
            Text(text)
        }
        .padding()
        .task {
            for await phrase in greeting.greet() {
                text = phrase
            }
        }
    }
}
