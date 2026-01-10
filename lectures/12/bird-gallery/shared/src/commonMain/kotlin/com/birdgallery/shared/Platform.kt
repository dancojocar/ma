package com.birdgallery.shared

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform
