package com.example.mockk

/**
 * Data class representing a User.
 */
data class User(
  val id: String,
  val name: String,
  val email: String,
  val isPremium: Boolean = false
)

/**
 * Repository interface for user data operations.
 * This will be mocked in tests.
 */
interface UserRepository {
  suspend fun getUser(id: String): User?
  suspend fun saveUser(user: User): Boolean
  suspend fun deleteUser(id: String): Boolean
  suspend fun getAllUsers(): List<User>
}

/**
 * Service for sending notifications.
 * This will be mocked in tests.
 */
interface NotificationService {
  fun sendEmail(to: String, subject: String, body: String): Boolean
  fun sendPush(userId: String, message: String): Boolean
}

/**
 * Analytics tracker for logging events.
 * This will be mocked to verify interactions.
 */
interface AnalyticsTracker {
  fun trackEvent(eventName: String, properties: Map<String, Any> = emptyMap())
  fun trackScreen(screenName: String)
}
