package com.unieats.app.data.auth

/** What the network layer needs from the session: the current JWT and a way to end it. */
interface AuthTokens {
    val token: String?
    fun clear()
}
