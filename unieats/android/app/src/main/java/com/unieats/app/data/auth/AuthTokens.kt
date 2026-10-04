package com.unieats.app.data.auth

/** What the data layer needs from the session: the current JWT, whose it is, and a way to end it. */
interface AuthTokens {
    /** Waits for the stored session to load, so a request made at startup still gets the token. */
    suspend fun token(): String?
    suspend fun userId(): String?
    suspend fun clear()
}
