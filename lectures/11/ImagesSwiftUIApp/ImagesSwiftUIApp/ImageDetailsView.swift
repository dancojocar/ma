//
//  ImageDetailsView.swift
//  ImagesSwiftUIApp
//
//  Created by Dan Cojocar on 08.12.2021.
//

import SwiftUI

struct ImageDetailsView: View {
    
    @ObservedObject var repository: ImagesRepository
    
    @State var isLiked: Bool = false
    
    @State var likeTask: Task<Void, Never>? = nil
    
    let imageData: ImageData
    
    var body: some View {
        ScrollView {
            VStack {
                Image(imageData.url)
                    .resizable()
                    .scaledToFill()
                    .frame(height: 200)
                    .clipped()
                HStack {
                    Image(systemName: "location.north.circle")
                    if let location = imageData.location {
                        Text(location)
                    }
                    Spacer()
                }.padding()
                if (imageData.tags.isEmpty == false) {
                    VStack(alignment: .leading) {
                        Text("Tags")
                            .bold()
                            .padding(.horizontal, 20)
                        FlexibleView(
                          data: imageData.tags,
                          spacing: 10,
                          alignment: .leading
                        ) { item in
                          Text(verbatim: item)
                            .padding(8)
                            .background(
                              RoundedRectangle(cornerRadius: 8)
                                .fill(Color.gray.opacity(0.2))
                             )
                        }.padding(.horizontal, 20)
                    }
                }
            }
        }
        .toolbar {
            ToolbarItem(placement: .navigationBarTrailing) {
                Button(action: {
                    isLiked.toggle()
                }) {
                    Image(systemName:  isLiked ? "heart.fill" : "heart")
                        .font(.title)
                        .foregroundColor(.red)
                }
            }
        }
        .onAppear {
            isLiked = imageData.isLiked
        }
        .onDisappear {
            likeTask?.cancel()
        }
        .onChange(of: imageData.isLiked) { newValue in
            isLiked = newValue
        }.onChange(of: isLiked) { newValue in
            if (newValue) {
                likeTask?.cancel()
                likeTask = Task {
                    await repository.likeImage(id: imageData.id)
                }
            }
        }
    }
}

struct ImageDetailsView_Previews: PreviewProvider {
    static var previews: some View {
        NavigationView {
            ImageDetailsView(
                repository: ImagesRepository(),
                imageData: ImageData(
                    location: "Trinity, Ireland",
                    tags: [
                        "library",
                        "trinity"
                    ],
                    url: "image_25",
                    isLiked: false
                )
            )
        }
    }
}
