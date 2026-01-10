package com.example.kmpshowcase

actual class Platform {
    actual val platform: String = "Android ${android.os.Build.VERSION.SDK_INT}"
}
