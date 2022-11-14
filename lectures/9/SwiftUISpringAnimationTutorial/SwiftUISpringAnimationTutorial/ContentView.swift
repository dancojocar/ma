import SwiftUI

struct ContentView: View {
    
    @State private var rotatingAngle: Double = 0
    @State private var color: Int = 0
    private let colors = [Color.red, Color.green, Color.blue]
    
    var body: some View {
        Button(action: {
            self.rotatingAngle += 90
            self.color = (self.color + 1) % self.colors.count
        }) {
            Rectangle()
                .fill(self.colors[color])
                .frame(width: 200, height: 200)
                .rotationEffect(.degrees(rotatingAngle))
                .animation(.interpolatingSpring(mass: 1, stiffness: 1, damping: 0.5, initialVelocity: 1))
        }
    }
}

struct ContentView_Previews: PreviewProvider {
    static var previews: some View {
        ContentView()
    }
}
