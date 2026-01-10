package com.example.mockk

/**
 * Use case for user registration.
 * Demonstrates a class with multiple dependencies that need mocking.
 */
class RegisterUserUseCase(
  private val userRepository: UserRepository,
  private val notificationService: NotificationService,
  private val analyticsTracker: AnalyticsTracker
) {

  /**
   * Registers a new user.
   * @return Result indicating success or failure with error message
   */
  suspend fun register(name: String, email: String): Result<User> {
    // Track registration attempt
    analyticsTracker.trackEvent("registration_started", mapOf("email" to email))

    // Validate input
    if (name.isBlank()) {
      return Result.failure(IllegalArgumentException("Name cannot be blank"))
    }
    if (!email.contains("@")) {
      return Result.failure(IllegalArgumentException("Invalid email format"))
    }

    // Create user
    val user = User(
      id = generateId(),
      name = name,
      email = email
    )

    // Save user
    val saved = userRepository.saveUser(user)
    if (!saved) {
      analyticsTracker.trackEvent("registration_failed", mapOf("reason" to "save_failed"))
      return Result.failure(Exception("Failed to save user"))
    }

    // Send welcome email
    notificationService.sendEmail(
      to = email,
      subject = "Welcome!",
      body = "Hello $name, welcome to our app!"
    )

    // Track success
    analyticsTracker.trackEvent("registration_completed", mapOf("userId" to user.id))

    return Result.success(user)
  }

  private fun generateId(): String = java.util.UUID.randomUUID().toString().take(8)
}

/**
 * Use case for fetching user profile with premium check.
 */
class GetUserProfileUseCase(
  private val userRepository: UserRepository
) {
  suspend fun getProfile(userId: String): UserProfile? {
    val user = userRepository.getUser(userId) ?: return null

    return UserProfile(
      user = user,
      displayName = if (user.isPremium) "⭐ ${user.name}" else user.name,
      features = if (user.isPremium) premiumFeatures else freeFeatures
    )
  }

  companion object {
    private val freeFeatures = listOf("Basic access", "Limited storage")
    private val premiumFeatures = listOf("Full access", "Unlimited storage", "Priority support")
  }
}

data class UserProfile(
  val user: User,
  val displayName: String,
  val features: List<String>
)
