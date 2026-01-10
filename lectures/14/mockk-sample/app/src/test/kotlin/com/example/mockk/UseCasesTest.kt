package com.example.mockk

import com.google.common.truth.Truth.assertThat
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.mockk
import io.mockk.slot
import io.mockk.spyk
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * Demonstrates MockK - a Kotlin-first mocking library.
 *
 * Key MockK features:
 * - mockk<T>(): Create a mock
 * - every { } returns: Stub method calls
 * - coEvery { } returns: Stub suspend functions
 * - verify { }: Verify method was called
 * - slot<T>() / capture(): Capture arguments
 * - relaxed = true: Auto-stub all methods
 * - spyk(): Spy on real objects
 */
class RegisterUserUseCaseTest {

  // Using annotations for mocks
  @MockK
  lateinit var userRepository: UserRepository

  @MockK
  lateinit var notificationService: NotificationService

  @RelaxedMockK  // Relaxed mock - auto-stubs all methods
  lateinit var analyticsTracker: AnalyticsTracker

  private lateinit var useCase: RegisterUserUseCase

  @Before
  fun setup() {
    MockKAnnotations.init(this)
    useCase = RegisterUserUseCase(userRepository, notificationService, analyticsTracker)
  }

  @After
  fun tearDown() {
    unmockkAll()
  }

  // ============================================
  // Basic Mocking with coEvery (for suspend funs)
  // ============================================

  @Test
  fun `register success - saves user and sends notification`() = runTest {
    // Given - stub the suspend function with coEvery
    coEvery { userRepository.saveUser(any()) } returns true
    every { notificationService.sendEmail(any(), any(), any()) } returns true

    // When
    val result = useCase.register("John Doe", "john@example.com")

    // Then
    assertThat(result.isSuccess).isTrue()
    assertThat(result.getOrNull()?.name).isEqualTo("John Doe")
  }

  // ============================================
  // Verification with verify { }
  // ============================================

  @Test
  fun `register calls analytics tracker for started and completed events`() = runTest {
    // Given
    coEvery { userRepository.saveUser(any()) } returns true
    every { notificationService.sendEmail(any(), any(), any()) } returns true

    // When
    useCase.register("Jane Doe", "jane@example.com")

    // Then - verify specific calls were made
    verify {
      analyticsTracker.trackEvent("registration_started", any())
      analyticsTracker.trackEvent("registration_completed", any())
    }

    // Verify exact number of calls
    verify(exactly = 2) { analyticsTracker.trackEvent(any(), any()) }
  }

  // ============================================
  // Argument Capturing with slot()
  // ============================================

  @Test
  fun `register captures saved user correctly`() = runTest {
    // Given - capture the argument
    val userSlot = slot<User>()
    coEvery { userRepository.saveUser(capture(userSlot)) } returns true
    every { notificationService.sendEmail(any(), any(), any()) } returns true

    // When
    useCase.register("Captured User", "captured@test.com")

    // Then - verify captured value
    val capturedUser = userSlot.captured
    assertThat(capturedUser.name).isEqualTo("Captured User")
    assertThat(capturedUser.email).isEqualTo("captured@test.com")
    assertThat(capturedUser.id).isNotEmpty()
  }

  // ============================================
  // Verification with argument matchers
  // ============================================

  @Test
  fun `register sends welcome email with correct content`() = runTest {
    // Given
    coEvery { userRepository.saveUser(any()) } returns true
    every { notificationService.sendEmail(any(), any(), any()) } returns true

    // When
    useCase.register("Alice", "alice@example.com")

    // Then - verify with specific argument matchers
    verify {
      notificationService.sendEmail(
        to = eq("alice@example.com"),
        subject = eq("Welcome!"),
        body = match { it.contains("Alice") }
      )
    }
  }

  // ============================================
  // Testing Error Cases
  // ============================================

  @Test
  fun `register fails when save fails`() = runTest {
    // Given
    coEvery { userRepository.saveUser(any()) } returns false

    // When
    val result = useCase.register("Test", "test@test.com")

    // Then
    assertThat(result.isFailure).isTrue()

    // Verify notification was NOT sent
    verify(exactly = 0) { notificationService.sendEmail(any(), any(), any()) }

    // Verify failure was tracked
    verify { analyticsTracker.trackEvent("registration_failed", any()) }
  }

  @Test
  fun `register returns failure for blank name`() = runTest {
    // When
    val result = useCase.register("", "test@test.com")

    // Then
    assertThat(result.isFailure).isTrue()
    assertThat(result.exceptionOrNull()?.message).contains("Name cannot be blank")
  }

  @Test
  fun `register returns failure for invalid email`() = runTest {
    // When
    val result = useCase.register("Test", "invalid-email")

    // Then
    assertThat(result.isFailure).isTrue()
    assertThat(result.exceptionOrNull()?.message).contains("Invalid email")
  }
}

/**
 * Additional MockK features demonstration.
 */
class GetUserProfileUseCaseTest {

  @Test
  fun `creates mock inline without annotations`() = runTest {
    // Create mock inline
    val repository = mockk<UserRepository>()

    // Stub
    coEvery { repository.getUser("123") } returns User(
      id = "123",
      name = "Test User",
      email = "test@test.com",
      isPremium = true
    )

    // Use
    val useCase = GetUserProfileUseCase(repository)
    val profile = useCase.getProfile("123")

    // Assert
    assertThat(profile?.displayName).isEqualTo("⭐ Test User")
    assertThat(profile?.features).contains("Priority support")
  }

  @Test
  fun `returns null for non-existent user`() = runTest {
    val repository = mockk<UserRepository>()
    coEvery { repository.getUser(any()) } returns null

    val useCase = GetUserProfileUseCase(repository)
    val profile = useCase.getProfile("unknown")

    assertThat(profile).isNull()
  }

  @Test
  fun `demonstrates spyk for partial mocking`() = runTest {
    // Create a real object
    val realUser = User("1", "Real User", "real@test.com", isPremium = false)

    // Spy on it - allows partial mocking
    val spiedUser = spyk(realUser)

    // Override specific property
    every { spiedUser.isPremium } returns true

    // Real properties still work
    assertThat(spiedUser.name).isEqualTo("Real User")

    // Overridden property returns mocked value
    assertThat(spiedUser.isPremium).isTrue()
  }
}
