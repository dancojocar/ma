// swift-tools-version: 6.0
import PackageDescription

let package = Package(
    name: "UniEatsDomain",
    platforms: [.iOS(.v18), .macOS(.v14)],
    products: [
        .library(name: "UniEatsDomain", targets: ["UniEatsDomain"]),
    ],
    targets: [
        .target(name: "UniEatsDomain"),
    ]
)
