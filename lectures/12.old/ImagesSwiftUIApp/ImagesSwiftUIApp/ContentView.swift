//
//  ContentView.swift
//  ImagesSwiftUIApp
//
//  Created by Dan Cojocar on 08.12.2021.
//

import SwiftUI

struct ContentView: View {
    
    @StateObject var repository = ImagesRepository()
    
    @State var showProgressBar = true
    
    @State private var searchText = ""
    
    @State var gridLayout: [GridItem] = [ GridItem() ]
    
    var body: some View {
        
        let _ = print("Body Eval")
        
        NavigationView {
            
            let _ = print("Nav View Eval")
            
            ZStack {
                if (showProgressBar) {
                    ProgressView()
                        .scaleEffect(
                            2.0,
                            anchor: .center
                        )
                        .progressViewStyle(CircularProgressViewStyle(tint: .blue))
                }
                
                ScrollView {
                    LazyVGrid(columns: gridLayout, alignment: .center, spacing: 10) {
                        ForEach(searchResults, id: \.id) { item in
                            NavigationLink(destination: ImageDetailsView(
                                repository: repository,
                                imageData: item
                            ).id(item.id)) {
                                Image(item.url)
                                    .renderingMode(.original)
                                    .resizable()
                                    .scaledToFill()
                                    .frame(minWidth: 0, maxWidth: .infinity)
                                    .frame(height: gridLayout.count == 1 ? 200 : 100)
                                    .cornerRadius(10)
                                    .shadow(color: Color.primary.opacity(0.3), radius: 1)
                            }
                        }
                    }.padding(.horizontal)
                        .navigationTitle("Images")
                        .navigationBarTitleDisplayMode(.inline)
                     .searchable(text: $searchText)
                }
                .toolbar {
                    ToolbarItem(placement: .navigationBarTrailing) {
                        Button(action: {
                            if (gridLayout.count > 1) {
                                self.gridLayout = Array(repeating: .init(), count: 1)
                            } else {
                                self.gridLayout = Array(repeating: .init(.flexible()), count: 3)
                            }
                        }) {
                            Image(systemName: "square.grid.2x2")
                                .font(.title)
                                .foregroundColor(.primary)
                        }
                    }
                }
            }
        }.task {
            await repository.getImages()
            showProgressBar = false
        }
    }
    
    var searchResults: [ImageData] {
            if searchText.isEmpty {
                return repository.images
            } else {
                return repository.images.filter { $0.tags.contains(searchText.lowercased()) }
            }
        }
    
}

struct ContentView_Previews: PreviewProvider {
    static var previews: some View {
        ContentView()
    }
}
