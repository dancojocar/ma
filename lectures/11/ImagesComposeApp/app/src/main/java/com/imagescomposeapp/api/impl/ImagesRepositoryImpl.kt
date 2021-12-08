package com.imagescomposeapp.api.impl

import com.imagescomposeapp.api.ImageData
import com.imagescomposeapp.api.ImagesRepository
import kotlinx.coroutines.delay
import java.util.concurrent.TimeUnit

class ImagesRepositoryImpl : ImagesRepository {

    private val images = listOf(
        ImageData(
            id = 0,
            location = "Amsterdam, Netherlands",
            tags = listOf(
                "amsterdam",
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
            ),
            url = "file:///android_asset/image_1.jpg",
            isLiked = false
        ),
        ImageData(
            id = 1,
            location = "Amsterdam, Netherlands",
            tags = listOf(
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
            ),
            url = "file:///android_asset/image_2.jpg",
            isLiked = false
        ),
        ImageData(
            id = 2,
            location = "Amsterdam, Netherlands",
            tags = listOf(
                "amsterdam",
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
            ),
            url = "file:///android_asset/image_3.jpg",
            isLiked = false
        ),
        ImageData(
            id = 3,
            location = "Amsterdam, Netherlands",
            tags = listOf(
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
            ),
            url = "file:///android_asset/image_4.jpg",
            isLiked = false
        ),
        ImageData(
            id = 4,
            location = "Stockholm, Sweden",
            tags = listOf(
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
            ),
            url = "file:///android_asset/image_5.jpg",
            isLiked = false
        ),
        ImageData(
            id = 5,
            location = "Stockholm, Sweden",
            tags = listOf(
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
            ),
            url = "file:///android_asset/image_6.jpg",
            isLiked = false
        ),
        ImageData(
            id = 6,
            location = "Stockholm, Sweden",
            tags = listOf(
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
            ),
            url = "file:///android_asset/image_7.jpg",
            isLiked = false
        ),
        ImageData(
            id = 7,
            location = "New York, NY",
            tags = listOf(
                "financial district",
                "building",
                "downtown",
                "nyc",
                "urban",
                "manhattan",
                "skyscraper"
            ),
            url = "file:///android_asset/image_8.jpg",
            isLiked = false
        ),
        ImageData(
            id = 8,
            location = "New York, NY",
            tags = listOf(
                "street",
                "manhattan",
                "nyc"
            ),
            url = "file:///android_asset/image_9.jpg",
            isLiked = false
        ),
        ImageData(
            id = 9,
            location = "New York, NY",
            tags = listOf(
                "bridge",
                "brooklyn"
            ),
            url = "file:///android_asset/image_10.jpg",
            isLiked = false
        ),
        ImageData(
            id = 10,
            location = "Kiawah Island, South Carolina",
            tags = listOf("golf"),
            url = "file:///android_asset/image_11.jpg",
            isLiked = false
        ),
        ImageData(
            id = 11,
            location = "Constitution Square - Tower I, Ottawa, Canada",
            tags = listOf(
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
            ),
            url = "file:///android_asset/image_12.jpg",
            isLiked = false
        ),
        ImageData(
            id = 12,
            location = "The Rock and Roll Hall of Fame and Museum, Cleveland, United States",
            tags = listOf(
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
            ),
            url = "file:///android_asset/image_13.jpg",
            isLiked = false
        ),
        ImageData(
            id = 13,
            location = "Melbourne VIC, Australia",
            tags = listOf(
                "indoors",
                "interior",
                "melbourne vic"
            ),
            url = "file:///android_asset/image_14.jpg",
            isLiked = false
        ),
        ImageData(
            id = 14,
            location = "New York, NY",
            tags = listOf("nyc"),
            url = "file:///android_asset/image_15.jpg",
            isLiked = false
        ),
        ImageData(
            id = 15,
            location = "Melbourne VIC, Australia",
            tags = listOf(
                "melbourne vic",
                "australia",
                "housing"
            ),
            url = "file:///android_asset/image_16.jpg",
            isLiked = false
        ),
        ImageData(
            id = 16,
            location = "Charnwood, United Kingdom",
            tags = listOf(
                "home",
                "charnwood"
            ),
            url = "file:///android_asset/image_17.jpg",
            isLiked = false
        ),
        ImageData(
            id = 17,
            location = "Knoxville, Tennessee",
            tags = listOf(
                "rain",
                "cozy"
            ),
            url = "file:///android_asset/image_18.jpg",
            isLiked = false
        ),
        ImageData(
            id = 18,
            location = "Lviv, Ukraine",
            tags = listOf(
                "reading"
            ),
            url = "file:///android_asset/image_19.jpg",
            isLiked = false
        ),
        ImageData(
            id = 19,
            location = null,
            tags = listOf("window"),
            url = "file:///android_asset/image_20.jpg",
            isLiked = false
        ),
        ImageData(
            id = 20,
            location = null,
            tags = listOf("window"),
            url = "file:///android_asset/image_21.jpg",
            isLiked = false
        ),
        ImageData(
            id = 21,
            location = "Trinity College, Dublin, Ireland",
            tags = listOf(
                "library",
                "dublin"
            ),
            url = "file:///android_asset/image_22.jpg",
            isLiked = false
        ),
        ImageData(
            id = 22,
            location = "Paris, France",
            tags = listOf(
                "library"
            ),
            url = "file:///android_asset/image_23.jpg",
            isLiked = false
        ),
        ImageData(
            id = 23,
            location = "Rijksmuseum, Amsterdam, Netherlands",
            tags = listOf("library"),
            url = "file:///android_asset/image_24.jpg",
            isLiked = false
        ),
        ImageData(
            id = 24,
            location = "Trinity, Ireland",
            tags = listOf(
                "library",
                "trinity"
            ),
            url = "file:///android_asset/image_25.jpg",
            isLiked = false
        )
    )

    override suspend fun getImage(imageId: Int?): ImageData? {
        delay(TimeUnit.SECONDS.toMillis(2))
        println(imageId)
        return images.find { it.id == imageId }
    }

    override suspend fun getImages(): List<ImageData> {
        delay(TimeUnit.SECONDS.toMillis(2))
        return images
    }

    override suspend fun likeImage(imageId: Int): Result<ImageData> {
        delay(TimeUnit.SECONDS.toMillis(2))
        val imageData = images.firstOrNull { imageId == it.id }

        return if (imageData != null) {
            imageData.isLiked = !imageData.isLiked
            Result.success(imageData)
        } else {
            Result.failure(Exception())
        }
    }

    override suspend fun searchImages(query: String): List<ImageData> {
        delay(TimeUnit.SECONDS.toMillis(2))
        return images.filter { it.tags.contains(query) }
    }
}
