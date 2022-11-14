package com.example.dan.memoryleakdemo

import android.view.View

/**
 * Fake class for the purpose of demonstrating a leak.
 */
class HttpRequestHelper internal constructor(private val button: View)