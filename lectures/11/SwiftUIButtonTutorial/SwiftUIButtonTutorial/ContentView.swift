
import SwiftUI

struct ContentView: View {
    @State var counter = 0
    var body: some View {
        
        VStack {
            Button(action:
                { self.counter+=1
            }) {
                HStack {
                    Image(systemName: "plus.circle")
                    Text("Add 1")
                }
            }
            .padding()
            .background(Color.blue)
            
            Text("\(counter)")
                
            .padding()
        }
        .foregroundColor(Color.black)
        .font(.title)
    }
}

#if DEBUG
struct ContentView_Previews: PreviewProvider {
    static var previews: some View {
        ContentView()
    }
}
#endif
