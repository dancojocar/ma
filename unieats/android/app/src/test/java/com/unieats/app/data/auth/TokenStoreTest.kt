package com.unieats.app.data.auth

import android.content.Context
import android.security.keystore.KeyPermanentlyInvalidatedException
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.test.core.app.ApplicationProvider
import app.cash.turbine.test
import com.unieats.app.R
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.File
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Robolectric has no AndroidKeyStore, so both ciphers are replaced: the session cipher by a
 * reversible fake, the biometric one by the real class on a software AES key (the prompt is
 * skipped: the cipher it would authenticate is used as is).
 */
@RunWith(RobolectricTestRunner::class)
class TokenStoreTest {

    @get:Rule
    val folder = TemporaryFolder()

    private val context: Context = ApplicationProvider.getApplicationContext()
    private val student = Session(token = "jwt-user-1", displayName = "Student", userId = "user-1")

    private val keystoreCipher = mockk<KeystoreCipher> {
        every { encrypt(any()) } answers { "sealed:" + firstArg<String>().reversed() }
        every { decrypt(any()) } answers { firstArg<String>().removePrefix("sealed:").reversed() }
    }

    private val biometricKey: SecretKey = newAesKey()
    private val biometricCipher = spyk(BiometricKeystoreCipher("test-biometric-key")) {
        every { encryptCipher() } answers { Cipher.getInstance(AES_GCM).apply { init(Cipher.ENCRYPT_MODE, biometricKey) } }
        every { decryptCipher(any()) } answers { decryptCipherFor(firstArg(), biometricKey) }
        every { deleteKey() } just Runs
    }

    private var storeJob: Job? = null

    /** A new TokenStore on the same file, as after the process was killed. */
    private suspend fun coldStart(): TokenStore {
        storeJob?.cancelAndJoin()
        val job = SupervisorJob().also { storeJob = it }
        val scope = CoroutineScope(job + Dispatchers.IO)
        val dataStore = PreferenceDataStoreFactory.create(scope = scope) {
            File(folder.root, "session.preferences_pb")
        }
        return TokenStore(context, dataStore, keystoreCipher, biometricCipher, scope)
    }

    @After
    fun stopStore() = runBlocking { storeJob?.cancelAndJoin() ?: Unit }

    private suspend fun TokenStore.loaded(): SessionState = session.first { it != SessionState.Loading }

    private suspend fun TokenStore.signIn() {
        save(student)
        awaitSession(SessionState.SignedIn(student))
    }

    /** Changes reach [TokenStore.session] on DataStore's thread; Turbine gives up after 3 s. */
    private suspend fun TokenStore.awaitSession(expected: SessionState) = session.test {
        while (awaitItem() != expected) continue
        cancelAndIgnoreRemainingEvents()
    }

    @Test
    fun `nothing stored means signed out`() = runTest {
        val store = coldStart()

        assertEquals(SessionState.SignedOut, store.loaded())
        assertNull(store.token())
        assertFalse(store.biometricEnabled.value)
    }

    @Test
    fun `a saved session survives a cold start`() = runTest {
        coldStart().signIn()

        val store = coldStart()

        assertEquals(SessionState.SignedIn(student), store.loaded())
        assertEquals(student.token, store.token())
        assertEquals(student.userId, store.userId())
    }

    @Test
    fun `clear signs out and deletes the biometric key`() = runTest {
        val store = coldStart()
        store.signIn()

        store.clear()

        store.awaitSession(SessionState.SignedOut)
        assertNull(store.token())
        verify { biometricCipher.deleteKey() }
    }

    @Test
    fun `turning biometric unlock on keeps the user signed in and locks the next cold start`() = runTest {
        val store = coldStart()
        store.signIn()

        store.enableBiometric(store.biometricEnableCipher()!!)

        assertEquals(SessionState.SignedIn(student), store.loaded())
        assertTrue(store.biometricEnabled.first { it })
        val restarted = coldStart()
        assertEquals(SessionState.Locked("Student"), restarted.loaded())
        assertNull(restarted.token())
    }

    @Test
    fun `the cipher the prompt authenticated unlocks the stored token`() = runTest {
        coldStart().apply {
            signIn()
            enableBiometric(biometricEnableCipher()!!)
        }
        val store = coldStart()

        val cipher = store.unlockCipher()
        assertNotNull(cipher)
        assertTrue(store.completeUnlock(cipher!!))

        store.awaitSession(SessionState.SignedIn(student))
        assertEquals(student.token, store.token())
    }

    @Test
    fun `a cipher for another key cannot unlock`() = runTest {
        coldStart().apply {
            signIn()
            enableBiometric(biometricEnableCipher()!!)
        }
        val store = coldStart()
        val genuine = store.unlockCipher()!!
        val forged = Cipher.getInstance(AES_GCM).apply {
            init(Cipher.DECRYPT_MODE, newAesKey(), GCMParameterSpec(128, genuine.iv))
        }

        assertFalse(store.completeUnlock(forged))
        assertEquals(SessionState.Locked("Student"), store.loaded())
    }

    @Test
    fun `a new enrollment invalidates the key and signs out with a notice`() = runTest {
        coldStart().apply {
            signIn()
            enableBiometric(biometricEnableCipher()!!)
        }
        every { biometricCipher.decryptCipher(any()) } throws KeyPermanentlyInvalidatedException()
        val store = coldStart()

        assertNull(store.unlockCipher())

        store.awaitSession(SessionState.SignedOut)
        assertEquals(context.getString(R.string.biometrics_changed_notice), store.notice.value)
        verify { biometricCipher.deleteKey() }
    }

    @Test
    fun `turning biometric unlock off stores the token normally again`() = runTest {
        val store = coldStart()
        store.signIn()
        store.enableBiometric(store.biometricEnableCipher()!!)

        store.disableBiometric()

        assertFalse(store.biometricEnabled.first { !it })
        assertEquals(SessionState.SignedIn(student), store.loaded())
        assertEquals(SessionState.SignedIn(student), coldStart().loaded())
        verify { biometricCipher.deleteKey() }
    }

    private fun decryptCipherFor(sealed: String, key: SecretKey): Cipher {
        val iv = Base64.getDecoder().decode(sealed).copyOfRange(0, 12)
        return Cipher.getInstance(AES_GCM).apply { init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(128, iv)) }
    }

    private companion object {
        const val AES_GCM = "AES/GCM/NoPadding"

        fun newAesKey(): SecretKey = KeyGenerator.getInstance("AES").apply { init(256) }.generateKey()
    }
}
