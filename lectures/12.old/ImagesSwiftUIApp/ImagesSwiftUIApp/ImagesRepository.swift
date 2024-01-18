

import Foundation

@MainActor
class ImagesRepository: ObservableObject {
    
    @Published var images: [ImageData] = []

    func getImages() async {
        await Task.sleep(3 * 1_000_000_000)
        images = [
            ImageData(
                location: "Amsterdam, Netherlands",
                tags: ["amsterdam",
                       "netherlands",
                       "building",
                       "Tree Images & Pictures",
                        "europe",
                        "urban",
                        "neighborhood",
                        "bike",
                        "bicycle",
                        "House Images",
                        "home",
                        "apartment building",
                        "road",
                        "street",
                        "Travel Images",
                        "canal",
                        "wall",
                        "shop",
                        "store",
                        "Backgrounds"
                      ],
                url: "image_1",
                isLiked: false
            ),
            ImageData(
                location: "Amsterdam, Netherlands",
                tags: [
                    "amsterdam",
                    "netherlands",
                    "HD Water Wallpapers",
                    "boat",
                    "Travel Images",
                    "outdoors",
                    "building",
                    "Tree Images & Pictures",
                    "sail",
                    "ripple",
                    "calm",
                    "perspective",
                    "transport",
                    "architecture",
                    "traditional design",
                    "trip",
                    "sunlight",
                    "Tree Images & Pictures"
                  ],
                url: "image_2",
                isLiked: false
            ),
            ImageData(
                location: "Amsterdam, Netherlands",
                tags: ["amsterdam",
                        "netherlands",
                        "canal",
                        "building",
                        "river",
                        "architecture",
                        "boat",
                        "reflection",
                        "evening",
                        "dusk",
                        "boats",
                        "House Images",
                        "outdoors",
                        "automobile",
                        "vehicle",
                        "Car Images & Pictures",
                        "transportation"
                      ],
                url: "image_3",
                isLiked: false
            ),
            ImageData(
                location: "Amsterdam, Netherlands",
                tags: [
                    "amsterdam",
                    "netherlands",
                    "nederland",
                    "holland",
                    "canalsm",
                    "outdoors",
                    "canal",
                    "street",
                    "town",
                    "urban",
                    "building",
                    "alleyway",
                    "alley",
                    "road",
                    "transportation",
                    "vessel",
                    "watercraft"

                ],
                url: "image_4",
                isLiked: false
            ),
            ImageData(
                location: "Stockholm, Sweden",
                tags: [
                    "stockholm",
                    "schweden",
                    "downtown",
                    "street",
                    "sweden",
                    "old city",
                    "Travel Images",
                    "alley",
                    "historic city",
                    "traveling",
                    "old",
                    "old town",
                    "historic",
                    "nordic",
                    "building",
                    "urban"
                ],
                url: "image_5",
                isLiked: false
            ),
            ImageData(
                location: "Stockholm, Sweden",
                tags: [
                    "stockholm",
                    "sweden",
                    "walkway",
                    "pavement",
                    "sidewalk",
                    "cobblestone",
                    "building",
                    "town",
                    "street",
                    "road",
                    "urban",
                    "outdoors"
                ],
                url: "image_6",
                isLiked: false
            ),
            ImageData(
                location: "Stockholm, Sweden",
                tags: [
                    "stockholm",
                    "sweden",
                    "urban",
                    "architecture",
                    "streets",
                    "bridge",
                    "lifestyle",
                    "adventure",
                    "building",
                    "town",
                    "downtown",
                    "road"
                ],
                url: "image_7",
                isLiked: false
            ),
            ImageData(
                location: "New York, NY",
                tags: [
                    "financial district",
                    "building",
                    "downtown",
                    "nyc",
                    "urban",
                    "manhattan",
                    "skyscraper"
                ],
                url: "image_8",
                isLiked: false
            ),
            ImageData(
                location: "New York, NY",
                tags: [
                    "street",
                    "manhattan",
                    "nyc"
                ],
                url: "image_9",
                isLiked: false
            ),
            ImageData(
                location: "New York, NY",
                tags: [
                    "bridge",
                    "brooklyn"
                ],
                url: "image_10",
                isLiked: false
            ),
            ImageData(
                location: "Kiawah Island, South Carolina",
                tags: ["golf"],
                url: "image_11",
                isLiked: false
            ),
            ImageData(
                location: "Constitution Square - Tower I, Ottawa, Canada",
                tags: [
                    "architecture",
                    "ottawa",
                    "canada",
                    "building",
                    "constitution square - tower i",
                    "urban",
                    "skyscraper",
                    "tower",
                    "glass",
                    "wide",
                    "symmetrical"
                ],
                url: "image_12",
                isLiked: false
            ),
            ImageData(
                location: "The Rock and Roll Hall of Fame and Museum, Cleveland, United States",
                tags: [
                    "architecture",
                    "building",
                    "cleveland",
                    "the rock and roll hall of fame and museum",
                    "minimalistic",
                    "minimalism",
                    "minimal",
                    "geometry",
                    "grid",
                    "line",
                    "urban",
                    "contemporary",
                    "structure",
                    "hall",
                    "contrast"
                ],
                url: "image_13",
                isLiked: false
            ),
            ImageData(
                location: "Melbourne VIC, Australia",
                tags: [
                    "indoors",
                    "interior",
                    "melbourne vic"
                ],
                url: "image_14",
                isLiked: false
            ),
            ImageData(
                location: "New York, NY",
                tags: ["nyc"],
                url: "image_15",
                isLiked: false
            ),
            ImageData(
                location: "Melbourne VIC, Australia",
                tags: [
                    "melbourne vic",
                    "australia",
                    "housing"
                ],
                url: "image_16",
                isLiked: false
            ),
            ImageData(
                location: "Charnwood, United Kingdom",
                tags: [
                    "home",
                    "charnwood"
                ],
                url: "image_17",
                isLiked: false
            ),
            ImageData(
                location: "Knoxville, Tennessee",
                tags: [
                    "rain",
                    "cozy"
                ],
                url: "image_18",
                isLiked: false
            ),
            ImageData(
                location: "Lviv, Ukraine",
                tags: [
                    "reading"
                ],
                url: "image_19",
                isLiked: false
            ),
            ImageData(
                location: nil,
                tags: ["window"],
                url: "image_20",
                isLiked: false
            ),
            ImageData(
                location: nil,
                tags: ["window"],
                url: "image_21",
                isLiked: false
            ),
            ImageData(
                location: "Trinity College, Dublin, Ireland",
                tags: [
                    "library",
                    "dublin"
                ],
                url: "image_22",
                isLiked: false
            ),
            ImageData(
                location: "Paris, France",
                tags: [
                    "library"
                ],
                url: "image_23",
                isLiked: false
            ),
            ImageData(
                location: "Rijksmuseum, Amsterdam, Netherlands",
                tags: ["library"],
                url: "image_24",
                isLiked: false
            ),
            ImageData(
                location: "Trinity, Ireland",
                tags: [
                    "library",
                    "trinity"
                ],
                url: "image_25",
                isLiked: false
            )
        ]
    }
    
    func likeImage(id: UUID) async {
        await Task.sleep(2 * 1_000_000_000)
        
        guard !Task.isCancelled else {
            return
        }
        
        let imageData = images.first {
            $0.id == id
        }

        if let imageData = imageData {
            objectWillChange.send()
            imageData.isLiked.toggle()
        }
    }
    
}

class ImageData {
    
    let id = UUID()
    let location: String?
    let tags: [String]
    let url: String
    var isLiked: Bool
    
    init(location: String?, tags: [String], url: String, isLiked: Bool) {
        self.location = location
        self.tags = tags
        self.url = url
        self.isLiked = isLiked
    }
    
}
