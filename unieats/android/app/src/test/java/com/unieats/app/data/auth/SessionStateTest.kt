package com.unieats.app.data.auth

import org.junit.Assert.assertEquals
import org.junit.Test

class SessionStateTest {

    private fun state(token: String? = null, biometricToken: Boolean = false, unlockedToken: String? = null) =
        sessionStateOf(token, biometricToken, unlockedToken, displayName = "Student", userId = "user-1")

    @Test
    fun `no token at all is signed out`() {
        assertEquals(SessionState.SignedOut, state())
    }

    @Test
    fun `a plain token is signed in`() {
        assertEquals(SessionState.SignedIn(Session("jwt", "Student", "user-1")), state(token = "jwt"))
    }

    @Test
    fun `a biometric token nobody unlocked yet is locked`() {
        assertEquals(SessionState.Locked("Student"), state(biometricToken = true))
    }

    @Test
    fun `an unlocked biometric token is signed in`() {
        assertEquals(
            SessionState.SignedIn(Session("jwt", "Student", "user-1")),
            state(biometricToken = true, unlockedToken = "jwt")
        )
    }

    @Test
    fun `an unlocked token left over after biometrics were turned off does not sign in`() {
        assertEquals(SessionState.SignedOut, state(unlockedToken = "jwt"))
    }
}
