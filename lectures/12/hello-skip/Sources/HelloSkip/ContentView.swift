import SwiftUI

public struct ContentView: View {
    @AppStorage("setting") var setting = true

    public init() {
    }

    public var body: some View {
        TabView {
            VStack {
                Text("Welcome Skipper!")
                Image(systemName: "heart.fill")
                    .foregroundColor(.red)
            }
            .font(.largeTitle)
            .tabItem { Label("Welcome", systemImage: "heart.fill") }

            NavigationStack {
                List {
                    ForEach(1..<100) { i in
                        NavigationLink("Home \(i)", value: i)
                    }
                }
                .navigationTitle("Navigation")
                .navigationDestination(for: Int.self) { i in
                    Text("Destination \(i)")
                        .font(.title)
                        .navigationTitle("Navigation \(i)")
                }
            }
            .tabItem { Label("Home", systemImage: "house.fill") }

            Form {
                Text("Settings")
                    .font(.largeTitle)
                Toggle("Option", isOn: $setting)
            }
            .tabItem { Label("Settings", systemImage: "gearshape.fill") }
        }
    }
}

#Preview {
    ContentView()
}
